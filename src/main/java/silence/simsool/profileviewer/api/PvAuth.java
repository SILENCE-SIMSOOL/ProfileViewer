package silence.simsool.profileviewer.api;

import static silence.simsool.lucent.Lucent.mc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PvAuth {

	private static final String API_URL = "https://skyblock-pv.thatgravyboat.tech/authenticate";
	private static final HttpClient client = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private static volatile String token = null;
	private static CompletableFuture<String> authentication;

	public static synchronized CompletableFuture<String> authenticateAsync() {
		if (token != null) return CompletableFuture.completedFuture(token);
		if (authentication != null && !authentication.isDone()) return authentication;

		authentication = CompletableFuture.supplyAsync(() -> {
			try {
				if (mc.getUser() == null) {
					System.out.println("[ProfileViewer/PvAuth] Minecraft User is null, skipping auth.");
					return null;
				}
				String username = mc.getUser().getName();
				String accessToken = mc.getUser().getAccessToken();
				UUID uuid = mc.getUser().getProfileId();

				// Check if this is an offline/dev session
				if (accessToken == null || accessToken.equals("0") || accessToken.equals("fabric") || accessToken.length() < 10) {
					System.out.println("[ProfileViewer/PvAuth] Offline/Dev session detected. Skipping official auth.");
					return null;
				}

				String serverId = UUID.randomUUID().toString();

				try {
					mc.services().sessionService().joinServer(uuid, accessToken, serverId);
				} catch (Exception e) {
					System.out.println("[ProfileViewer/PvAuth] SessionService joinServer warning: " + e.getMessage());
					return null;
				}

				// Allow Mojang session server replication delay
				try {
					Thread.sleep(350);
				} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
					return null;
				}

				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(API_URL))
						.timeout(Duration.ofSeconds(10))
						.header("User-Agent", "ProfileViewer/1.0.0/26.2")
						.header("x-minecraft-username", username)
						.header("x-minecraft-server", serverId)
						.GET()
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				// Retry once on 401 in case of slight replication lag
				if (response.statusCode() == 401) {
					try {
						Thread.sleep(600);
					} catch (InterruptedException interrupted) {
					Thread.currentThread().interrupt();
					return null;
				}
					response = client.send(request, HttpResponse.BodyHandlers.ofString());
				}

				if (response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {
					token = response.body().trim();
					System.out.println("[ProfileViewer/PvAuth] Successfully authenticated with SkyBlock PV API.");
					return token;
				} else {
					System.out.println("[ProfileViewer/PvAuth] Authenticate returned status: " + response.statusCode() + ", body: " + response.body());
				}
			} catch (Exception e) {
				System.out.println("[ProfileViewer/PvAuth] Authentication failed: " + e.getMessage());
			}
			return null;
		});
		return authentication;
	}

	public static String getToken() {
		return token;
	}

	public static synchronized void invalidateToken(String rejectedToken) {
		if (Objects.equals(token, rejectedToken)) token = null;
	}

	public static synchronized void invalidateToken() {
		token = null;
	}
}