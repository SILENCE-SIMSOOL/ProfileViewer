package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CollectionData {

	public static class CollectionItem {
		public final String id;
		public final String name;
		public final String category;
		public final long amount;
		public final int tier;
		public final int maxTier;
		public final float progress;
		public final boolean isMax;

		public CollectionItem(String id, String name, String category, long amount, int tier, int maxTier, float progress, boolean isMax) {
			this.id = id;
			this.name = name;
			this.category = category;
			this.amount = amount;
			this.tier = tier;
			this.maxTier = maxTier;
			this.progress = progress;
			this.isMax = isMax;
		}
	}

	public Map<String, Integer> rawCollections = new LinkedHashMap<>();
	public List<CollectionItem> farmingCollections = new ArrayList<>();
	public List<CollectionItem> miningCollections = new ArrayList<>();
	public List<CollectionItem> combatCollections = new ArrayList<>();
	public List<CollectionItem> foragingCollections = new ArrayList<>();
	public List<CollectionItem> fishingCollections = new ArrayList<>();

	public List<String> craftedMinions = new ArrayList<>();
	public int unlockedMinions = 0;
	public int minionSlots = 5;

	public static CollectionData fromJson(JsonObject member) {
		CollectionData d = new CollectionData();
		if (member == null) return d;

		if (member.has("collection") && member.get("collection").isJsonObject()) {
			JsonObject col = member.getAsJsonObject("collection");
			for (var e : col.entrySet()) {
				if (e.getValue().isJsonPrimitive()) {
					try {
						d.rawCollections.put(e.getKey(), e.getValue().getAsInt());
					} catch (Exception ignored) {}
				}
			}
		}

		if (member.has("player_data") && member.get("player_data").isJsonObject()) {
			JsonObject pd = member.getAsJsonObject("player_data");
			if (pd.has("crafted_generators") && pd.get("crafted_generators").isJsonArray()) {
				JsonArray arr = pd.getAsJsonArray("crafted_generators");
				d.unlockedMinions = arr.size();
				for (JsonElement el : arr) {
					d.craftedMinions.add(el.getAsString());
				}
			}
		}

		d.minionSlots = calcMinionSlots(d.unlockedMinions);
		d.populateCategories();

		return d;
	}

	private void populateCategories() {
		// Farming
		addCol(farmingCollections, "WHEAT", "Wheat", "FARMING", 100000, 9);
		addCol(farmingCollections, "CARROT_ITEM", "Carrot", "FARMING", 100000, 9);
		addCol(farmingCollections, "POTATO_ITEM", "Potato", "FARMING", 100000, 9);
		addCol(farmingCollections, "PUMPKIN", "Pumpkin", "FARMING", 100000, 9);
		addCol(farmingCollections, "MELON", "Melon", "FARMING", 250000, 9);
		addCol(farmingCollections, "SEEDS", "Seeds", "FARMING", 50000, 6);
		addCol(farmingCollections, "MUSHROOM_COLLECTION", "Mushroom", "FARMING", 50000, 8);
		addCol(farmingCollections, "INK_SACK:3", "Cocoa Beans", "FARMING", 100000, 9);
		addCol(farmingCollections, "CACTUS", "Cactus", "FARMING", 100000, 9);
		addCol(farmingCollections, "SUGAR_CANE", "Sugar Cane", "FARMING", 100000, 9);
		addCol(farmingCollections, "FEATHER", "Feather", "FARMING", 50000, 9);
		addCol(farmingCollections, "LEATHER", "Leather", "FARMING", 50000, 9);
		addCol(farmingCollections, "PORK", "Raw Porkchop", "FARMING", 70000, 9);
		addCol(farmingCollections, "RAW_CHICKEN", "Raw Chicken", "FARMING", 70000, 9);
		addCol(farmingCollections, "MUTTON", "Mutton", "FARMING", 70000, 9);
		addCol(farmingCollections, "RABBIT", "Raw Rabbit", "FARMING", 70000, 9);
		addCol(farmingCollections, "NETHER_STALK", "Nether Wart", "FARMING", 150000, 9);

		// Mining
		addCol(miningCollections, "COBBLESTONE", "Cobblestone", "MINING", 250000, 9);
		addCol(miningCollections, "COAL", "Coal", "MINING", 100000, 9);
		addCol(miningCollections, "IRON_INGOT", "Iron Ingot", "MINING", 150000, 9);
		addCol(miningCollections, "GOLD_INGOT", "Gold Ingot", "MINING", 150000, 9);
		addCol(miningCollections, "DIAMOND", "Diamond", "MINING", 250000, 9);
		addCol(miningCollections, "INK_SACK:4", "Lapis Lazuli", "MINING", 150000, 9);
		addCol(miningCollections, "EMERALD", "Emerald", "MINING", 150000, 9);
		addCol(miningCollections, "REDSTONE", "Redstone", "MINING", 400000, 11);
		addCol(miningCollections, "QUARTZ", "Nether Quartz", "MINING", 150000, 9);
		addCol(miningCollections, "OBSIDIAN", "Obsidian", "MINING", 50000, 9);
		addCol(miningCollections, "GLOWSTONE_DUST", "Glowstone", "MINING", 150000, 9);
		addCol(miningCollections, "GRAVEL", "Gravel", "MINING", 100000, 9);
		addCol(miningCollections, "ICE", "Ice", "MINING", 250000, 10);
		addCol(miningCollections, "NETHERRACK", "Netherrack", "MINING", 50000, 6);
		addCol(miningCollections, "SAND", "Sand", "MINING", 100000, 9);
		addCol(miningCollections, "ENDER_STONE", "End Stone", "MINING", 150000, 9);
		addCol(miningCollections, "MITHRIL_ORE", "Mithril", "MINING", 250000, 9);
		addCol(miningCollections, "HARD_STONE", "Hard Stone", "MINING", 3000000, 9);
		addCol(miningCollections, "GEMSTONE_COLLECTION", "Gemstone", "MINING", 500000, 11);
		addCol(miningCollections, "GLACITE", "Glacite", "MINING", 1000000, 9);

		// Combat
		addCol(combatCollections, "ROTTEN_FLESH", "Rotten Flesh", "COMBAT", 100000, 9);
		addCol(combatCollections, "BONE", "Bone", "COMBAT", 100000, 9);
		addCol(combatCollections, "STRING", "String", "COMBAT", 100000, 9);
		addCol(combatCollections, "SPIDER_EYE", "Spider Eye", "COMBAT", 70000, 9);
		addCol(combatCollections, "SULPHUR", "Gunpowder", "COMBAT", 100000, 9);
		addCol(combatCollections, "ENDER_PEARL", "Ender Pearl", "COMBAT", 150000, 9);
		addCol(combatCollections, "GHAST_TEAR", "Ghast Tear", "COMBAT", 25000, 9);
		addCol(combatCollections, "SLIME_BALL", "Slimeball", "COMBAT", 100000, 9);
		addCol(combatCollections, "BLAZE_ROD", "Blaze Rod", "COMBAT", 100000, 9);
		addCol(combatCollections, "MAGMA_CREAM", "Magma Cream", "COMBAT", 100000, 9);

		// Foraging
		addCol(foragingCollections, "LOG", "Oak Wood", "FORAGING", 100000, 9);
		addCol(foragingCollections, "LOG:1", "Spruce Wood", "FORAGING", 100000, 9);
		addCol(foragingCollections, "LOG:2", "Birch Wood", "FORAGING", 100000, 9);
		addCol(foragingCollections, "LOG_2:1", "Dark Oak Wood", "FORAGING", 100000, 9);
		addCol(foragingCollections, "LOG_2", "Acacia Wood", "FORAGING", 100000, 9);
		addCol(foragingCollections, "LOG:3", "Jungle Wood", "FORAGING", 100000, 9);

		// Fishing
		addCol(fishingCollections, "RAW_FISH", "Raw Fish", "FISHING", 100000, 9);
		addCol(fishingCollections, "RAW_FISH:1", "Raw Salmon", "FISHING", 100000, 9);
		addCol(fishingCollections, "RAW_FISH:2", "Clownfish", "FISHING", 25000, 9);
		addCol(fishingCollections, "RAW_FISH:3", "Pufferfish", "FISHING", 25000, 9);
		addCol(fishingCollections, "PRISMARINE_SHARD", "Prismarine Shard", "FISHING", 50000, 9);
		addCol(fishingCollections, "PRISMARINE_CRYSTALS", "Prismarine Crystals", "FISHING", 25000, 9);
		addCol(fishingCollections, "CLAY_BALL", "Clay", "FISHING", 100000, 9);
		addCol(fishingCollections, "WATER_LILY", "Water Lily", "FISHING", 25000, 9);
		addCol(fishingCollections, "INK_SACK", "Ink Sack", "FISHING", 75000, 9);
		addCol(fishingCollections, "SPONGE", "Sponge", "FISHING", 25000, 9);
	}

	private void addCol(List<CollectionItem> list, String id, String name, String category, long maxAmount, int maxTier) {
		long amount = rawCollections.getOrDefault(id, 0);
		float progress = Math.min(1.0f, (float) amount / (float) maxAmount);
		int tier = (int) (progress * maxTier);
		boolean isMax = amount >= maxAmount;
		list.add(new CollectionItem(id, name, category, amount, tier, maxTier, progress, isMax));
	}

	public static int calcMinionSlots(int craftedMinions) {
		int[] thresholds = {5, 15, 30, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 350, 400, 450, 500, 550, 600, 650};
		int slots = 5;
		for (int t : thresholds) {
			if (craftedMinions >= t) {
				slots++;
			} else {
				break;
			}
		}
		return slots;
	}
}