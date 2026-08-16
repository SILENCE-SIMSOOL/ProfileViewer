package silence.simsool.profileviewer.api.repo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

public final class ItemRepo {

	private static final String ITEMS_REPO_URL = "https://skyblock-api-repo.thatgravyboat.tech/1_21_5/items.min.json";
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final Gson GSON = new GsonBuilder().create();

	public static class RepoItem {
		public String sbId;
		public String mcId;
		public String name;
		public String texture;
		public String itemModel;
		public Integer dyedColor;
		public boolean glint;
	}

	private static final Map<String, RepoItem> ITEMS = new HashMap<>();
	private static volatile boolean initialized = false;

	static {
		loadInitial();
		syncRemoteAsync();
	}

	public static void init() {
		// Trigger static initializer
	}

	private static void loadInitial() {
		try {
			Path cacheFile = FabricLoader.getInstance().getConfigDir().resolve("profileviewer").resolve("repo").resolve("items.json");
			if (Files.exists(cacheFile)) {
				String cached = Files.readString(cacheFile, StandardCharsets.UTF_8);
				JsonElement root = GSON.fromJson(cached, JsonElement.class);
				if (root != null) {
					parseRepoJson(root);
					initialized = true;
					return;
				}
			}
		} catch (Exception ignored) {}

		try (InputStream stream = ItemRepo.class.getResourceAsStream("/repo/items.min.json")) {
			if (stream != null) {
				JsonElement root = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
				if (root != null) {
					parseRepoJson(root);
					initialized = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void syncRemoteAsync() {
		CompletableFuture.runAsync(() -> {
			try {
				HttpRequest req = HttpRequest.newBuilder()
						.uri(URI.create(ITEMS_REPO_URL))
						.header("User-Agent", "ProfileViewer-RepoLib")
						.GET()
						.build();
				HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
				if (resp.statusCode() == 200 && (resp.body().startsWith("[") || resp.body().startsWith("{"))) {
					JsonElement root = GSON.fromJson(resp.body(), JsonElement.class);
					if (root != null) {
						parseRepoJson(root);
						initialized = true;
						try {
							Path cacheDir = FabricLoader.getInstance().getConfigDir().resolve("profileviewer").resolve("repo");
							Files.createDirectories(cacheDir);
							Files.writeString(cacheDir.resolve("items.json"), resp.body(), StandardCharsets.UTF_8);
						} catch (Exception ignored) {}
					}
				}
			} catch (Exception ignored) {}
		});
	}

	private static synchronized void parseRepoJson(JsonElement root) {
		if (root.isJsonArray()) {
			JsonArray array = root.getAsJsonArray();
			for (JsonElement elem : array) {
				if (!elem.isJsonObject()) continue;
				JsonObject obj = elem.getAsJsonObject();

				String mcId = obj.has("id") ? obj.get("id").getAsString() : "minecraft:paper";
				JsonObject components = obj.has("components") && obj.get("components").isJsonObject() ? obj.getAsJsonObject("components") : null;
				if (components == null) continue;

				JsonObject customData = components.has("minecraft:custom_data") && components.get("minecraft:custom_data").isJsonObject() ? components.getAsJsonObject("minecraft:custom_data") : null;
				if (customData == null || !customData.has("id")) continue;

				String sbId = customData.get("id").getAsString().toUpperCase(Locale.ROOT);

				RepoItem item = new RepoItem();
				item.sbId = sbId;
				item.mcId = mcId;

				if (components.has("minecraft:custom_name") && components.get("minecraft:custom_name").isJsonObject()) {
					JsonObject cn = components.getAsJsonObject("minecraft:custom_name");
					if (cn.has("text")) item.name = cn.get("text").getAsString();
				}

				if (components.has("minecraft:profile") && components.get("minecraft:profile").isJsonObject()) {
					JsonObject prof = components.getAsJsonObject("minecraft:profile");
					if (prof.has("properties") && prof.get("properties").isJsonArray()) {
						for (JsonElement pe : prof.getAsJsonArray("properties")) {
							if (pe.isJsonObject()) {
								JsonObject pobj = pe.getAsJsonObject();
								if (pobj.has("name") && "textures".equals(pobj.get("name").getAsString()) && pobj.has("value")) {
									item.texture = pobj.get("value").getAsString();
									break;
								}
							}
						}
					}
				}

				if (components.has("minecraft:item_model")) {
					try { item.itemModel = components.get("minecraft:item_model").getAsString(); } catch (Exception ignored) {}
				}

				if (components.has("minecraft:dyed_color")) {
					try { item.dyedColor = components.get("minecraft:dyed_color").getAsInt(); } catch (Exception ignored) {}
				}

				if (components.has("minecraft:enchantment_glint_override")) {
					try { item.glint = components.get("minecraft:enchantment_glint_override").getAsBoolean(); } catch (Exception ignored) {}
				}

				ITEMS.put(sbId, item);
			}
		}
	}

	public static RepoItem getItem(String id) {
		if (id == null) return null;
		return ITEMS.get(id.toUpperCase(Locale.ROOT));
	}

	public static ItemStack getItemStack(String sbId) {
		if (sbId == null || sbId.isEmpty()) return ItemStack.EMPTY;
		String cleanId = sbId.toUpperCase(Locale.ROOT);
		RepoItem repo = ITEMS.get(cleanId);
		if (repo != null) {
			Item mcItem = Items.AIR;
			if (repo.mcId != null && !repo.mcId.isEmpty()) {
				String id = repo.mcId.contains(":") ? repo.mcId : "minecraft:" + repo.mcId;
				Identifier res = Identifier.tryParse(id);
				if (res != null) {
					mcItem = BuiltInRegistries.ITEM.getValue(res);
				}
			}

			if (mcItem == Items.AIR) {
				mcItem = (repo.texture != null && !repo.texture.isEmpty()) ? Items.PLAYER_HEAD : Items.PAPER;
			}

			ItemStack stack = new ItemStack(mcItem, 1);

			if (repo.itemModel != null && !repo.itemModel.isEmpty()) {
				Identifier modelId = Identifier.tryParse(repo.itemModel);
				if (modelId != null) {
					stack.set(DataComponents.ITEM_MODEL, modelId);
				}
			}

			if (repo.texture != null && !repo.texture.isEmpty()) {
				PetRepo.applyHeadTexture(stack, repo.texture);
			}

			if (repo.dyedColor != null) {
				stack.set(DataComponents.DYED_COLOR, new DyedItemColor(repo.dyedColor));
			}

			if (repo.glint) {
				stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
			}

			return stack;
		}
		return ItemStack.EMPTY;
	}

	public static int getLoadedItemCount() {
		return ITEMS.size();
	}
}
