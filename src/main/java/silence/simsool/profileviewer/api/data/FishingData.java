package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonObject;

public class FishingData {
	public int totalCatches;
	public int bronzeTrophy;
	public int silverTrophy;
	public int goldTrophy;
	public int diamondTrophy;
	public int itemsFishedTotal;
	public int normalCatches;
	public int treasuresCaught;
	public int treasuresFished;
	public int largeTreasuresCaught;
	public int seaCreaturesKilled;
	public int festivalSharksKilled;
	public int drakePiper;
	public int midasLure;
	public int radiantFisher;
	public int trophyRank;
	public String lastCatch = "";
	public Map<String, Integer> seaCreatureKills = new LinkedHashMap<>();
	public Map<String, int[]> trophyFishCounts = new LinkedHashMap<>();
	public Map<String, Integer> trophyFishTotals = new LinkedHashMap<>();

	public static final String[] TROPHY_FISH_TYPES = {
		"Blobfish", "Gusher", "Sulphur Skitter", "Steaming-Hot Flounder", "Lava Horse",
		"Slugfish", "Flyfish", "Obfuscated 1", "Obfuscated 2", "Obfuscated 3",
		"Volcanic Stonefish", "Vanille", "Skeleton Fish", "Moldfin",
		"Soul Fish", "Karate Fish", "Golden Fish", "Mana Ray"
	};

	public static String trophyId(String name) {
		String id = name.toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
		return id.startsWith("OBFUSCATED_") ? id.replace("OBFUSCATED_", "OBFUSCATED_FISH_") : id;
	}

	public static FishingData fromJson(JsonObject json) {
		FishingData d = new FishingData();
		JsonObject tf = object(json, "trophy_fish");
		if (tf == null && json != null && (json.has("total_caught") || json.has("rewards"))) tf = json;
		int countedTotal = 0;
		for (String type : TROPHY_FISH_TYPES) {
			String key = trophyId(type).toLowerCase(Locale.ROOT);
			int[] counts = new int[4];
			String[] tiers = {"bronze", "silver", "gold", "diamond"};
			int sum = 0;
			for (int i = 0; i < tiers.length; i++) {
				counts[i] = number(tf, key + "_" + tiers[i]);
				sum += counts[i];
			}
			int total = tf != null && tf.has(key) ? number(tf, key) : sum;
			d.trophyFishCounts.put(type, counts);
			d.trophyFishTotals.put(type, total);
			countedTotal += total;
			d.bronzeTrophy += counts[0];
			d.silverTrophy += counts[1];
			d.goldTrophy += counts[2];
			d.diamondTrophy += counts[3];
		}
		d.totalCatches = tf != null && tf.has("total_caught") ? number(tf, "total_caught") : countedTotal;
		if (tf != null) {
			if (tf.has("last_caught") && tf.get("last_caught").isJsonPrimitive()) d.lastCatch = tf.get("last_caught").getAsString();
			if (tf.has("rewards") && tf.get("rewards").isJsonArray()) {
				for (var reward : tf.getAsJsonArray("rewards")) {
					if (reward.isJsonPrimitive() && reward.getAsJsonPrimitive().isNumber()) {
						int rank = reward.getAsInt();
						if (rank >= 1 && rank <= 4) d.trophyRank = Math.max(d.trophyRank, rank);
					}
				}
			}
		}
		JsonObject stats = object(json, "player_stats");
		if (stats == null) stats = object(json, "stats");
		JsonObject items = object(stats, "items_fished");
		d.itemsFishedTotal = items == null ? number(stats, "items_fished") : number(items, "total");
		d.normalCatches = number(items, "normal");
		d.treasuresFished = number(items, "treasure");
		d.largeTreasuresCaught = number(items, "large_treasure");
		d.seaCreaturesKilled = number(object(object(stats, "pets"), "milestone"), "sea_creatures_killed");
		d.festivalSharksKilled = number(object(json, "leveling"), "fishing_festival_sharks_killed");
		JsonObject player = object(json, "player_data");
		d.treasuresCaught = number(player, "fishing_treasure_caught");
		JsonObject perks = object(player, "perks");
		d.drakePiper = number(perks, "drake_piper");
		d.midasLure = number(perks, "midas_lure");
		d.radiantFisher = number(perks, "radiant_fisher");
		return d;
	}

	public String dolphinRarity() {
		if (seaCreaturesKilled >= 10000) return "Legendary";
		if (seaCreaturesKilled >= 5000) return "Epic";
		if (seaCreaturesKilled >= 2500) return "Rare";
		if (seaCreaturesKilled >= 1000) return "Uncommon";
		if (seaCreaturesKilled >= 250) return "Common";
		return "None";
	}

	private static JsonObject object(JsonObject parent, String key) {
		return parent != null && parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : null;
	}

	private static int number(JsonObject parent, String key) {
		if (parent == null || !parent.has(key) || !parent.get(key).isJsonPrimitive() || !parent.getAsJsonPrimitive(key).isNumber()) return 0;
		return parent.get(key).getAsInt();
	}
}
