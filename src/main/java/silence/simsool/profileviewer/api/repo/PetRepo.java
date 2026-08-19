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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;

public final class PetRepo {

	private static final String PETS_REPO_URL = "https://skyblock-api-repo.thatgravyboat.tech/1_21_5/pets.min.json";
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final Gson GSON = new GsonBuilder().create();
	private static final DecimalFormat STAT_FORMATTER = new DecimalFormat("0.####");
	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{(?<key>[a-zA-Z0-9_]+)}");

	private static final Map<String, PetEntry> PETS = new HashMap<>();
	private static volatile boolean initialized = false;

	public static class PetEntry {
		public String id;
		public String name;
		public Map<String, PetTier> tiers = new HashMap<>();
	}

	public static class PetTier {
		public String texture;
		public List<String> lore = new ArrayList<>();
		public Map<String, double[]> variables = new HashMap<>();
		public int variablesOffset = 0;
	}

	static {
		loadInitial();
		syncRemoteAsync();
	}

	public static void init() {
		// Trigger static initializer
	}

	private static void loadInitial() {
		try {
			// 1. Try local cache
			Path cacheFile = FabricLoader.getInstance().getConfigDir().resolve("profileviewer").resolve("repo").resolve("pets.json");
			if (Files.exists(cacheFile)) {
				String cached = Files.readString(cacheFile, StandardCharsets.UTF_8);
				JsonObject obj = GSON.fromJson(cached, JsonObject.class);
				if (obj != null) {
					parseRepoJson(obj);
					initialized = true;
					return;
				}
			}
		} catch (Exception ignored) {}

		// 2. Load bundled resource
		try (InputStream stream = PetRepo.class.getResourceAsStream("/repo/pets.min.json")) {
			if (stream != null) {
				JsonObject obj = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
				if (obj != null) {
					parseRepoJson(obj);
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
						.uri(URI.create(PETS_REPO_URL))
						.header("User-Agent", "ProfileViewer-RepoLib")
						.GET()
						.build();
				HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
				if (resp.statusCode() == 200 && resp.body().startsWith("{")) {
					JsonObject obj = GSON.fromJson(resp.body(), JsonObject.class);
					if (obj != null) {
						parseRepoJson(obj);
						initialized = true;
						try {
							Path cacheDir = FabricLoader.getInstance().getConfigDir().resolve("profileviewer").resolve("repo");
							Files.createDirectories(cacheDir);
							Files.writeString(cacheDir.resolve("pets.json"), resp.body(), StandardCharsets.UTF_8);
						} catch (Exception ignored) {}
					}
				}
			} catch (Exception ignored) {}
		});
	}

	private static synchronized void parseRepoJson(JsonObject root) {
		for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
			if (!entry.getValue().isJsonObject()) continue;
			JsonObject petObj = entry.getValue().getAsJsonObject();

			PetEntry p = new PetEntry();
			p.id = entry.getKey().toUpperCase(Locale.ROOT);
			p.name = petObj.has("name") ? petObj.get("name").getAsString() : p.id;

			if (petObj.has("tiers") && petObj.get("tiers").isJsonObject()) {
				JsonObject tiersObj = petObj.getAsJsonObject("tiers");
				for (Map.Entry<String, JsonElement> tierEntry : tiersObj.entrySet()) {
					if (!tierEntry.getValue().isJsonObject()) continue;
					JsonObject tObj = tierEntry.getValue().getAsJsonObject();

					PetTier tier = new PetTier();
					tier.texture = tObj.has("texture") ? tObj.get("texture").getAsString() : "";

					if (tObj.has("lore") && tObj.get("lore").isJsonArray()) {
						for (JsonElement le : tObj.getAsJsonArray("lore")) {
							tier.lore.add(le.getAsString());
						}
					}

					if (tObj.has("variables") && tObj.get("variables").isJsonObject()) {
						JsonObject varsObj = tObj.getAsJsonObject("variables");
						for (Map.Entry<String, JsonElement> ve : varsObj.entrySet()) {
							if (ve.getValue().isJsonArray()) {
								JsonArray va = ve.getValue().getAsJsonArray();
								if (va.size() >= 2) {
									tier.variables.put(ve.getKey(), new double[]{ va.get(0).getAsDouble(), va.get(1).getAsDouble() });
								}
							}
						}
					}

					tier.variablesOffset = tObj.has("variablesOffset") ? tObj.get("variablesOffset").getAsInt() : 0;
					p.tiers.put(tierEntry.getKey().toUpperCase(Locale.ROOT), tier);
				}
			}

			PETS.put(p.id, p);
		}
	}

	public static PetEntry getPet(String id) {
		if (id == null) return null;
		return PETS.get(id.toUpperCase(Locale.ROOT));
	}

	public static ItemStack getPetItemStack(String petType, String rarity, int level, String skin, String heldItem) {
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD, 1);
		if (petType == null || petType.trim().isEmpty()) return stack;

		String cleanType = petType.trim().replace(" ", "_").toUpperCase(Locale.ROOT);
		String cleanRarity = rarity != null ? rarity.trim().toUpperCase(Locale.ROOT) : "COMMON";
		int safeLevel = Math.max(1, level);

		PetEntry pet = PETS.get(cleanType);
		PetTier tier = pet != null ? pet.tiers.get(cleanRarity) : null;
		if (tier == null && pet != null && !pet.tiers.isEmpty()) {
			tier = pet.tiers.values().iterator().next();
		}

		String texture = tier != null ? tier.texture : null;

		// Apply Profile Component
		if (texture != null && !texture.isEmpty()) {
			applyHeadTexture(stack, texture);
		}

		// Custom Data / ExtraAttributes
		try {
			CompoundTag extra = new CompoundTag();
			extra.putString("id", "PET");
			extra.putString("type", cleanType);
			extra.putString("tier", cleanRarity);
			if (skin != null && !skin.isEmpty()) extra.putString("skin", skin.toUpperCase(Locale.ROOT));
			if (heldItem != null && !heldItem.isEmpty()) extra.putString("heldItem", heldItem.toUpperCase(Locale.ROOT));

			JsonObject petInfo = new JsonObject();
			petInfo.addProperty("type", cleanType);
			petInfo.addProperty("tier", cleanRarity);
			petInfo.addProperty("exp", 0.0);
			petInfo.addProperty("candyUsed", 0);
			extra.putString("petInfo", petInfo.toString());

			CompoundTag root = new CompoundTag();
			root.put("ExtraAttributes", extra);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
		} catch (Exception ignored) {}

		// Custom Name
		char rCode = getRarityCode(cleanRarity);
		String petDisplayName = pet != null ? pet.name : cleanType.replace("_", " ");
		String displayName = "§" + rCode + "[Lv " + safeLevel + "] " + petDisplayName;
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));

		// Formatted Lore
		List<Component> loreList = new ArrayList<>();
		if (tier != null && !tier.lore.isEmpty()) {
			for (String line : tier.lore) {
				String formatted = formatLoreLine(line, tier, safeLevel);
				loreList.add(Component.literal(formatted));
			}
		} else {
			loreList.add(Component.literal("§8" + cleanRarity + " PET"));
		}

		stack.set(DataComponents.LORE, new ItemLore(loreList));
		return stack;
	}

	private static String formatLoreLine(String line, PetTier tier, int level) {
		if (line == null) return "";
		Matcher matcher = VARIABLE_PATTERN.matcher(line);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String key = matcher.group("key");
			if ("LVL".equalsIgnoreCase(key)) {
				matcher.appendReplacement(sb, String.valueOf(level));
			} else if (tier.variables.containsKey(key)) {
				double[] minMax = tier.variables.get(key);
				float pct = Math.clamp(level - 1 - tier.variablesOffset, 0, 99) / 99.0f;
				double stat = minMax[0] + pct * (minMax[1] - minMax[0]);
				stat = Math.floor(stat * 10000.0) / 10000.0;
				matcher.appendReplacement(sb, STAT_FORMATTER.format(stat));
			} else {
				matcher.appendReplacement(sb, "{" + key + "}");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public static void applyHeadTexture(ItemStack stack, String texture) {
		if (stack == null || texture == null || texture.trim().isEmpty()) return;
		try {
			String clean = texture.trim();
			String encoded = null;

			if (clean.startsWith("http://") || clean.startsWith("https://")) {
				String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + clean + "\"}}}";
				encoded = java.util.Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			} else if (clean.matches("^[0-9a-fA-F]{64}$")) {
				String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + clean + "\"}}}";
				encoded = java.util.Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			} else {
				String padded = NbtItemParser.fixBase64Padding(clean);
				encoded = padded;
			}

			if (encoded != null) {
				UUID uuid = UUID.nameUUIDFromBytes(encoded.getBytes(StandardCharsets.UTF_8));
				Property property = new Property("textures", encoded);
				var multimap = com.google.common.collect.ImmutableMultimap.<String, Property>builder().put("textures", property).build();
				PropertyMap propertyMap = new PropertyMap(multimap);

				ResolvableProfile profile = ResolvableProfile.createResolved(new GameProfile(uuid, "_", propertyMap));
				stack.set(DataComponents.PROFILE, profile);
			}
		} catch (Exception ignored) {}
	}



	public static char getRarityCode(String rarity) {
		if (rarity == null) return 'f';
		return switch (rarity.toUpperCase(Locale.ROOT)) {
			case "UNCOMMON" -> 'a';
			case "RARE" -> '9';
			case "EPIC" -> '5';
			case "LEGENDARY" -> '6';
			case "MYTHIC" -> 'd';
			case "DIVINE" -> 'b';
			default -> 'f';
		};
	}
}
