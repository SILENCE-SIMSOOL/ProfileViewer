package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FishingData {

	public int totalCatches = 0;
	public int bronzeTrophy = 0;
	public int silverTrophy = 0;
	public int goldTrophy = 0;
	public int diamondTrophy = 0;

	// Sea Creatures Kills
	public int seaCreaturesKilled = 0;
	public Map<String, Integer> seaCreatureKills = new LinkedHashMap<>();

	// 18 Trophy fish species: fish_id -> [bronze, silver, gold, diamond]
	public Map<String, int[]> trophyFishCounts = new LinkedHashMap<>();

	public static final String[] TROPHY_FISH_TYPES = {
		"Blobfish", "Gusher", "Sulphur Skitter", "Steaming-Hot Flounder", "Lava Horse",
		"Slugfish", "Flyfish", "Obfuscated 1", "Obfuscated 2", "Obfuscated 3",
		"Volcanic Stonefish", "Vanille", "Skeleton Fish", "Moldfin",
		"Soul Fish", "Karate Fish", "Golden Fish", "Mana Ray"
	};

	public static FishingData fromJson(JsonObject json) {
		FishingData d = new FishingData();
		if (json == null) return d;

		for (String type : TROPHY_FISH_TYPES) {
			d.trophyFishCounts.put(type, new int[4]);
		}

		JsonObject tf = json.has("trophy_fish") && json.get("trophy_fish").isJsonObject()
				? json.getAsJsonObject("trophy_fish")
				: (json.has("total_caught") || json.has("rewards") ? json : null);

		if (tf != null) {
			if (tf.has("total_caught") && tf.get("total_caught").isJsonPrimitive()) {
				d.totalCatches = tf.get("total_caught").getAsInt();
			}

			for (var entry : tf.entrySet()) {
				String key = entry.getKey().toLowerCase();
				if (!entry.getValue().isJsonPrimitive()) continue;
				int count = entry.getValue().getAsInt();

				for (String type : TROPHY_FISH_TYPES) {
					String baseKey = type.toLowerCase().replace(" ", "_").replace("-", "_");
					if (key.startsWith(baseKey) || (baseKey.startsWith("obfuscated") && key.startsWith("obfuscated_fish_" + baseKey.replace("obfuscated_", "")))) {
						int[] counts = d.trophyFishCounts.get(type);
						if (counts != null) {
							if (key.endsWith("_bronze")) counts[0] += count;
							else if (key.endsWith("_silver")) counts[1] += count;
							else if (key.endsWith("_gold")) counts[2] += count;
							else if (key.endsWith("_diamond")) counts[3] += count;
							else if (key.equals(baseKey) && counts[0] == 0 && counts[1] == 0 && counts[2] == 0 && counts[3] == 0) {
								counts[0] = count;
							}
						}
					}
				}
			}

			for (int[] counts : d.trophyFishCounts.values()) {
				d.bronzeTrophy += counts[0];
				d.silverTrophy += counts[1];
				d.goldTrophy += counts[2];
				d.diamondTrophy += counts[3];
			}
		}

		// Sea creature kills from stats / player_stats / bestiary / kills
		JsonObject killsObj = null;
		if (json.has("player_stats") && json.get("player_stats").isJsonObject() && json.getAsJsonObject("player_stats").has("kills")) {
			killsObj = json.getAsJsonObject("player_stats").getAsJsonObject("kills");
		} else if (json.has("stats") && json.get("stats").isJsonObject() && json.getAsJsonObject("stats").has("kills")) {
			killsObj = json.getAsJsonObject("stats").getAsJsonObject("kills");
		} else if (json.has("kills") && json.get("kills").isJsonObject()) {
			killsObj = json.getAsJsonObject("kills");
		}

		if (killsObj != null) {
			int scTotal = 0;
			for (Map.Entry<String, JsonElement> e : killsObj.entrySet()) {
				if (e.getKey().startsWith("sea_creature_") || e.getKey().startsWith("sc_") || isSeaCreature(e.getKey())) {
					if (e.getValue().isJsonPrimitive()) {
						int k = e.getValue().getAsInt();
						d.seaCreatureKills.put(formatScName(e.getKey()), k);
						scTotal += k;
					}
				}
			}
			d.seaCreaturesKilled = scTotal;
		}

		return d;
	}

	private static boolean isSeaCreature(String key) {
		String k = key.toLowerCase();
		return k.contains("squid") || k.contains("guardian") || k.contains("sea_walker") || k.contains("night_squid") ||
				k.contains("sea_guardian") || k.contains("monster_of_the_deep") || k.contains("catfish") ||
				k.contains("sea_witch") || k.contains("sea_archer") || k.contains("rider_of_the_deep") ||
				k.contains("yeti") || k.contains("reindrake") || k.contains("lord_jawbus") || k.contains("thunder");
	}

	private static String formatScName(String key) {
		String cleaned = key.replace("kills_", "").replace("sea_creature_", "").replace("_", " ");
		String[] words = cleaned.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}
}