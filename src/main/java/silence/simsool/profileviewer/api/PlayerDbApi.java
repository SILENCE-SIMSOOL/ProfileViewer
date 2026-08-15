package silence.simsool.profileviewer.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

public class PlayerDbApi {

	private static final HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private static final Map<String, GameProfile> cache = new ConcurrentHashMap<>();

	public static CompletableFuture<GameProfile> resolveGameProfile(String query) {
		String key = query.toLowerCase().trim();
		if (cache.containsKey(key)) {
			return CompletableFuture.completedFuture(cache.get(key));
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				String url = "https://playerdb.co/api/player/minecraft/" + key;
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(url))
						.timeout(Duration.ofSeconds(10))
						.header("User-Agent", "ProfileViewer/1.0.0")
						.GET()
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
					if (root.get("success").getAsBoolean()) {
						JsonObject player = root.getAsJsonObject("data").getAsJsonObject("player");
						String name = player.get("username").getAsString();
						String idStr = player.get("id").getAsString();
						UUID uuid = UUID.fromString(idStr);
						GameProfile profile = new GameProfile(uuid, name);
						cache.put(key, profile);
						cache.put(name.toLowerCase(), profile);
						cache.put(uuid.toString().toLowerCase(), profile);
						return profile;
					}
				}
			} catch (Exception ignored) {}

			try {
				UUID uuid = UUID.fromString(query);
				GameProfile profile = new GameProfile(uuid, query);
				return profile;
			} catch (Exception ignored) {}

			return null;
		});
	}
}