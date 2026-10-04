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

	private static final JsonObject CATALOG = ProfileJson.catalog("collections").getAsJsonObject("collections");
	public boolean available;
	public Map<String, Long> rawCollections = new LinkedHashMap<>();
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
			d.available = true;
			JsonObject col = member.getAsJsonObject("collection");
			for (var e : col.entrySet()) {
				if (e.getValue().isJsonPrimitive()) {
					try {
						d.rawCollections.put(e.getKey(), e.getValue().getAsLong());
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
		for (var category : CATALOG.entrySet()) {
			List<CollectionItem> target = switch (category.getKey()) {
				case "FARMING" -> farmingCollections;
				case "MINING" -> miningCollections;
				case "COMBAT" -> combatCollections;
				case "FORAGING" -> foragingCollections;
				case "FISHING" -> fishingCollections;
				default -> null;
			};
			if (target == null) continue;
			for (var entry : category.getValue().getAsJsonObject().getAsJsonObject("items").entrySet()) {
				JsonObject definition = entry.getValue().getAsJsonObject();
				long amount = rawCollections.getOrDefault(entry.getKey(), 0L);
				long maximum = 0;
				int tier = 0;
				for (var step : definition.getAsJsonArray("tiers")) {
					JsonObject threshold = step.getAsJsonObject();
					long required = threshold.get("amountRequired").getAsLong();
					maximum = Math.max(maximum, required);
					if (amount >= required) tier = Math.max(tier, threshold.get("tier").getAsInt());
				}
				int maxTier = definition.get("maxTiers").getAsInt();
				float progress = maximum == 0 ? 0 : Math.clamp((float) amount / maximum, 0f, 1f);
				target.add(new CollectionItem(entry.getKey(), definition.get("name").getAsString(), category.getKey(), amount, tier, maxTier, progress, tier >= maxTier));
			}
		}
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