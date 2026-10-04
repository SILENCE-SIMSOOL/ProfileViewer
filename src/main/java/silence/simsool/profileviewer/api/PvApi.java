package silence.simsool.profileviewer.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import silence.simsool.profileviewer.api.data.GardenData;
import silence.simsool.profileviewer.api.data.MuseumData;
import silence.simsool.profileviewer.api.data.NetworthData;
import silence.simsool.profileviewer.api.data.PlayerStatus;
import silence.simsool.profileviewer.api.data.SkyBlockProfileData;

public class PvApi {
	private static final String BASE = "https://skyblock-pv.thatgravyboat.tech";
	private static final long CACHE_TIME = 5 * 60 * 1000L;
	private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build();
	private record Response(JsonObject data, long expiresAt) {}
	private static final Map<String, Response> cache = new ConcurrentHashMap<>();
	private static final Map<String, CompletableFuture<Response>> requests = new ConcurrentHashMap<>();

	private static synchronized CompletableFuture<Response> get(String path, boolean refresh) {
		Response cached = cache.get(path);
		if (!refresh && cached != null && cached.expiresAt > System.currentTimeMillis()) return CompletableFuture.completedFuture(cached);
		CompletableFuture<Response> pending = requests.get(path);
		if (pending != null) return pending;
		CompletableFuture<Response> future = request(path, false).thenApply(response -> {
			cache.put(path, response);
			return response;
		});
		requests.put(path, future);
		future.whenComplete((result, error) -> requests.remove(path, future));
		return future;
	}

	private static CompletableFuture<Response> request(String path, boolean retried) {
		return PvAuth.authenticateAsync().thenCompose(token -> {
			if (token == null) return CompletableFuture.failedFuture(new IllegalStateException("PV authentication failed. Please sign in again and retry."));
			HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + path)).timeout(Duration.ofSeconds(15))
				.header("User-Agent", "ProfileViewer/1.0.0/26.2").header("Authorization", token)
				.header("X-Intent", "profile-viewer").GET().build();
			return client.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenCompose(response -> {
				if (response.statusCode() == 401 && !retried) {
					PvAuth.invalidateToken(token);
					return request(path, true);
				}
				if (response.statusCode() != 200) return CompletableFuture.failedFuture(new IllegalStateException("PV API HTTP " + response.statusCode()));
				JsonObject data = JsonParser.parseString(response.body()).getAsJsonObject();
				if (data.has("success") && !data.get("success").getAsBoolean()) return CompletableFuture.failedFuture(new IllegalStateException("PV API reported an unsuccessful request"));
				long lifetime = CACHE_TIME;
				try {
					lifetime = Math.max(0, Math.min(CACHE_TIME, response.headers().firstValueAsLong("X-Backend-Expire-In").orElse(CACHE_TIME)));
				} catch (NumberFormatException ignored) {}
				return CompletableFuture.completedFuture(new Response(data, System.currentTimeMillis() + lifetime));
			});
		});
	}

	public static CompletableFuture<PlayerStatus> fetchPlayerStatusAsync(UUID uuid) {
		return fetchPlayerStatusAsync(uuid, false);
	}

	public static CompletableFuture<PlayerStatus> fetchPlayerStatusAsync(UUID uuid, boolean refresh) {
		if (uuid == null) return CompletableFuture.completedFuture(new PlayerStatus());
		return get("/status/" + uuid, refresh).thenApply(response -> PlayerStatus.fromJson(response.data)).exceptionally(error -> new PlayerStatus());
	}

	public static CompletableFuture<List<SkyBlockProfileData>> fetchProfilesAsync(UUID uuid, boolean refresh) {
		if (uuid == null) return CompletableFuture.failedFuture(new IllegalArgumentException("Missing player UUID"));
		return get("/profiles/" + uuid, refresh).thenApplyAsync(response -> parseProfiles(response.data, uuid)).thenCompose(profiles -> {
			List<CompletableFuture<Void>> extras = new ArrayList<>();
			for (SkyBlockProfileData profile : profiles) {
				extras.add(get("/garden/" + profile.profileId, refresh).thenAccept(response -> {
					JsonElement garden = response.data.get("garden");
					if (garden == null || !garden.isJsonObject()) throw new IllegalStateException("Garden data unavailable");
					GardenData shared = GardenData.fromJson(garden.getAsJsonObject());
					shared.copyMemberData(profile.member.garden);
					profile.member.garden = shared;
				}).exceptionally(error -> { profile.gardenError = "Garden data unavailable. Refresh to retry."; return null; }));
				extras.add(get("/museum/" + profile.profileId, refresh).thenAcceptAsync(response -> {
					JsonElement members = response.data.get("members");
					if (members == null || !members.isJsonObject()) throw new IllegalStateException("Museum data unavailable");
					profile.member.museum = MuseumData.fromJson(members.getAsJsonObject(), uuid);
					if (profile.member.networth != null) {
						profile.member.networth = NetworthData.calculate(profile.member, profile.banking != null ? profile.banking.balance : 0.0);
					}
				}).exceptionally(error -> { profile.museumError = "Museum data unavailable. Refresh to retry."; return null; }));
			}
			return CompletableFuture.allOf(extras.toArray(CompletableFuture[]::new)).thenApply(ignored -> profiles);
		});
	}

	static List<SkyBlockProfileData> parseProfiles(JsonObject root, UUID user) {
		List<SkyBlockProfileData> profiles = new ArrayList<>();
		JsonElement value = root.get("profiles");
		if (value == null) throw new IllegalStateException("PV response is missing profiles");
		if (value.isJsonNull()) return profiles;
		if (!value.isJsonArray()) throw new IllegalStateException("Invalid PV profiles response");
		for (JsonElement element : value.getAsJsonArray()) {
			SkyBlockProfileData profile = SkyBlockProfileData.fromJson(element.getAsJsonObject(), user);
			if (profile != null) profiles.add(profile);
		}
		profiles.sort((a, b) -> Boolean.compare(b.selected, a.selected));
		return profiles;
	}
}