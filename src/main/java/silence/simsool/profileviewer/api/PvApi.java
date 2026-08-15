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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import silence.simsool.profileviewer.api.data.SkyBlockProfileData;

public class PvApi {

	private static final String PV_API_BASE = "https://skyblock-pv.thatgravyboat.tech";
	private static final HttpClient client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(12))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private static final Map<UUID, List<SkyBlockProfileData>> profilesCache = new ConcurrentHashMap<>();

	public static CompletableFuture<List<SkyBlockProfileData>> fetchProfilesAsync(UUID uuid, boolean forceRefresh) {
		if (!forceRefresh && profilesCache.containsKey(uuid)) {
			return CompletableFuture.completedFuture(profilesCache.get(uuid));
		}

		return CompletableFuture.supplyAsync(() -> {
			// 1. Try SkyBlock PV API
			List<SkyBlockProfileData> result = fetchFromPvApi(uuid);
			if (result != null && !result.isEmpty()) {
				profilesCache.put(uuid, result);
				return result;
			}

			// 2. Try Slothpixel Profiles API (Public Hypixel API mirror)
			System.out.println("[ProfileViewer/PvApi] Trying Slothpixel API for UUID: " + uuid);
			result = fetchFromSlothpixel(uuid);
			if (result != null && !result.isEmpty()) {
				profilesCache.put(uuid, result);
				return result;
			}

			// 3. Try SkyCrypt API
			System.out.println("[ProfileViewer/PvApi] Trying SkyCrypt API for UUID: " + uuid);
			result = fetchFromSkyCrypt(uuid);
			if (result != null && !result.isEmpty()) {
				profilesCache.put(uuid, result);
				return result;
			}

			return new ArrayList<>();
		});
	}

	private static List<SkyBlockProfileData> fetchFromPvApi(UUID uuid) {
		try {
			String token = PvAuth.getToken();
			if (token == null) {
				token = PvAuth.authenticateAsync().join();
			}

			if (token == null) {
				System.out.println("[ProfileViewer/PvApi] PvAuth token is null, cannot call SkyBlock PV API.");
				return null;
			}

			String url = PV_API_BASE + "/profiles/" + uuid.toString();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.header("User-Agent", "SkyBlockPV/1.2.0/1.21.4")
					.header("Authorization", token)
					.header("X-Intent", "profile-viewer")
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 401) {
				System.out.println("[ProfileViewer/PvApi] Received 401 Unauthorized, re-authenticating...");
				PvAuth.invalidateToken();
				token = PvAuth.authenticateAsync().join();
				if (token != null) {
					HttpRequest retryRequest = HttpRequest.newBuilder()
							.uri(URI.create(url))
							.timeout(Duration.ofSeconds(10))
							.header("User-Agent", "SkyBlockPV/1.2.0/1.21.4")
							.header("Authorization", token)
							.header("X-Intent", "profile-viewer")
							.GET()
							.build();
					response = client.send(retryRequest, HttpResponse.BodyHandlers.ofString());
				}
			}

			System.out.println("[ProfileViewer/PvApi] SkyBlock PV API Response Status: " + response.statusCode());

			if (response.statusCode() == 200 && response.body() != null) {
				JsonElement parsed = JsonParser.parseString(response.body());
				if (parsed.isJsonObject()) {
					JsonObject root = parsed.getAsJsonObject();
					List<SkyBlockProfileData> list = parseProfiles(root, uuid);
					if (!list.isEmpty()) {
						System.out.println("[ProfileViewer/PvApi] Successfully loaded " + list.size() + " profiles from SkyBlock PV API.");
						// Fetch Garden and Museum in parallel for each profile
						for (SkyBlockProfileData p : list) {
							fetchAdditionalPvData(p, token, uuid);
						}
						return list;
					} else {
						System.out.println("[ProfileViewer/PvApi] Parsed 0 profiles from response: " + response.body());
					}
				}
			} else {
				System.out.println("[ProfileViewer/PvApi] SkyBlock PV API error response: " + response.body());
			}
		} catch (Exception e) {
			System.out.println("[ProfileViewer/PvApi] Failed to fetch from PV API: " + e.getMessage());
		}
		return null;
	}

	private static void fetchAdditionalPvData(SkyBlockProfileData profile, String token, UUID uuid) {
		if (profile == null || profile.profileId.isEmpty()) return;
		try {
			// 1. Garden API: /garden/<profile_id>
			String gardenUrl = PV_API_BASE + "/garden/" + profile.profileId;
			HttpRequest gReq = HttpRequest.newBuilder()
					.uri(URI.create(gardenUrl))
					.timeout(Duration.ofSeconds(6))
					.header("User-Agent", "SkyBlockPV/1.2.0/1.21.4")
					.header("Authorization", token)
					.header("X-Intent", "profile-viewer")
					.GET()
					.build();

			client.sendAsync(gReq, HttpResponse.BodyHandlers.ofString()).thenAccept(res -> {
				if (res.statusCode() == 200 && res.body() != null) {
					try {
						JsonObject gRoot = JsonParser.parseString(res.body()).getAsJsonObject();
						if (gRoot.has("garden") && gRoot.get("garden").isJsonObject()) {
							profile.member.garden = silence.simsool.profileviewer.api.data.GardenData.fromJson(gRoot.getAsJsonObject("garden"));
						}
					} catch (Exception ignored) {}
				}
			});

			// 2. Museum API: /museum/<profile_id>
			String museumUrl = PV_API_BASE + "/museum/" + profile.profileId;
			HttpRequest mReq = HttpRequest.newBuilder()
					.uri(URI.create(museumUrl))
					.timeout(Duration.ofSeconds(6))
					.header("User-Agent", "SkyBlockPV/1.2.0/1.21.4")
					.header("Authorization", token)
					.header("X-Intent", "profile-viewer")
					.GET()
					.build();

			client.sendAsync(mReq, HttpResponse.BodyHandlers.ofString()).thenAccept(res -> {
				if (res.statusCode() == 200 && res.body() != null) {
					try {
						JsonObject mRoot = JsonParser.parseString(res.body()).getAsJsonObject();
						if (mRoot.has("members") && mRoot.get("members").isJsonObject()) {
							profile.member.museum = silence.simsool.profileviewer.api.data.MuseumData.fromJson(mRoot.getAsJsonObject("members"), uuid);
						}
					} catch (Exception ignored) {}
				}
			});
		} catch (Exception ignored) {}
	}

	private static List<SkyBlockProfileData> fetchFromSlothpixel(UUID uuid) {
		try {
			String url = "https://api.slothpixel.me/api/skyblock/profiles/" + uuid.toString().replace("-", "");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofSeconds(12))
					.header("User-Agent", "SkyBlockPV/1.2.0/1.21.4")
					.header("Accept", "application/json")
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200 && response.body() != null) {
				JsonElement parsed = JsonParser.parseString(response.body());
				if (parsed.isJsonObject()) {
					JsonObject obj = parsed.getAsJsonObject();
					List<SkyBlockProfileData> list = new ArrayList<>();

					// Slothpixel returns a map of profile_id -> profileObject or a list
					for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
						if (entry.getValue().isJsonObject()) {
							JsonObject pJson = entry.getValue().getAsJsonObject();
							SkyBlockProfileData data = SkyBlockProfileData.fromJson(pJson, uuid);
							if (data != null) {
								list.add(data);
							}
						}
					}
					if (!list.isEmpty()) {
						list.sort((a, b) -> Boolean.compare(b.selected, a.selected));
						System.out.println("[ProfileViewer/PvApi] Successfully loaded " + list.size() + " profiles from Slothpixel.");
						return list;
					}
				}
			} else {
				System.out.println("[ProfileViewer/PvApi] Slothpixel returned status: " + response.statusCode());
			}
		} catch (Exception e) {
			System.out.println("[ProfileViewer/PvApi] Failed to fetch from Slothpixel: " + e.getMessage());
		}
		return null;
	}

	private static List<SkyBlockProfileData> parseProfiles(JsonObject root, UUID targetUser) {
		List<SkyBlockProfileData> list = new ArrayList<>();
		if (root == null) return list;

		JsonArray profiles = root.getAsJsonArray("profiles");
		if (profiles != null) {
			for (JsonElement elem : profiles) {
				if (elem.isJsonObject()) {
					SkyBlockProfileData data = SkyBlockProfileData.fromJson(elem.getAsJsonObject(), targetUser);
					if (data != null) {
						list.add(data);
					}
				}
			}
		}
		list.sort((a, b) -> Boolean.compare(b.selected, a.selected));
		return list;
	}

	private static List<SkyBlockProfileData> fetchFromSkyCrypt(UUID uuid) {
		try {
			String url = "https://sky.shiiyu.moe/api/v2/profile/" + uuid.toString().replace("-", "");
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofSeconds(15))
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
					.header("Accept-Language", "en-US,en;q=0.9")
					.header("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
					.header("Sec-Ch-Ua-Mobile", "?0")
					.header("Sec-Ch-Ua-Platform", "\"Windows\"")
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200 && response.body() != null) {
				JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
				JsonObject profiles = root.getAsJsonObject("profiles");
				if (profiles != null) {
					List<SkyBlockProfileData> list = new ArrayList<>();
					for (Map.Entry<String, JsonElement> entry : profiles.entrySet()) {
						if (entry.getValue().isJsonObject()) {
							JsonObject pObj = entry.getValue().getAsJsonObject();
							SkyBlockProfileData data = null;
							if (pObj.has("raw") && pObj.get("raw").isJsonObject()) {
								JsonObject raw = pObj.getAsJsonObject("raw");
								data = SkyBlockProfileData.fromJson(raw, uuid);
								if (data != null) {
									if (pObj.has("cute_name")) data.cuteName = pObj.get("cute_name").getAsString();
									if (pObj.has("current")) data.selected = pObj.get("current").getAsBoolean();
								}
							}
							if (data == null) {
								data = SkyBlockProfileData.fromSkyCryptJson(pObj, uuid);
							}
							if (data != null) list.add(data);
						}
					}
					if (!list.isEmpty()) {
						list.sort((a, b) -> Boolean.compare(b.selected, a.selected));
						System.out.println("[ProfileViewer/PvApi] Successfully loaded " + list.size() + " profiles from SkyCrypt.");
						return list;
					}
				}
			} else {
				System.out.println("[ProfileViewer/PvApi] SkyCrypt returned status: " + response.statusCode());
			}
		} catch (Exception e) {
			System.out.println("[ProfileViewer/PvApi] Failed to fetch from SkyCrypt: " + e.getMessage());
		}

		return new ArrayList<>();
	}
}