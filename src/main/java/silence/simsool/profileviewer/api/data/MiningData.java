package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MiningData {

	public int hotmLevel = 0;
	public double hotmExperience = 0;
	public int mithrilPowder = 0;
	public int gemstonePowder = 0;
	public int glacitePowder = 0;
	public int peakOfTheMountain = 0;
	public int commissionsCompleted = 0;
	public int nucleusRuns = 0;
	public String selectedAbility = "";
	public int selectedMiningPreset = 1;

	public Map<String, Integer> nodes = new LinkedHashMap<>();
	public Map<Integer, Map<String, Integer>> presetNodes = new LinkedHashMap<>();
	public Map<Integer, String> presetAbilities = new LinkedHashMap<>();

	// HOTF (Foraging) data
	public int hotfLevel = 0;
	public double hotfExperience = 0;
	public String selectedForagingAbility = "";
	public int selectedForagingPreset = 1;
	public int centerOfTheForest = 0;
	public int forestWhispers = 0;
	public int desertWhispers = 0;
	public Map<String, Integer> foragingNodes = new LinkedHashMap<>();
	public Map<Integer, Map<String, Integer>> foragingPresetNodes = new LinkedHashMap<>();
	public Map<Integer, String> foragingPresetAbilities = new LinkedHashMap<>();

	public Map<String, Boolean> crystals = new LinkedHashMap<>();
	public GlaciteData glacite = new GlaciteData();

	public static class GlaciteData {
		public int mineshaftsEntered = 0;
		public Map<String, Integer> corpsesLooted = new LinkedHashMap<>();
		public java.util.List<String> fossilsDonated = new java.util.ArrayList<>();
		public int totalCorpses = 0;
	}

	public static MiningData fromJson(JsonObject member) {
		MiningData d = new MiningData();
		if (member == null) return d;

		// 1. mining_core (Direct Mining Data)
		JsonObject core = member.has("mining_core") && member.get("mining_core").isJsonObject() ? member.getAsJsonObject("mining_core") : null;
		if (core != null) {
			if (core.has("experience")) {
				if (core.get("experience").isJsonPrimitive()) {
					d.hotmExperience = core.get("experience").getAsDouble();
				} else if (core.get("experience").isJsonObject()) {
					JsonObject expObj = core.getAsJsonObject("experience");
					if (expObj.has("mining") && expObj.get("mining").isJsonPrimitive()) {
						d.hotmExperience = expObj.get("mining").getAsDouble();
					}
				}
				d.hotmLevel = calcHotmLevel(d.hotmExperience);
			}

			int mithrilCurrent = core.has("powder_mithril") ? core.get("powder_mithril").getAsInt() : 0;
			int mithrilSpent = core.has("powder_spent_mithril") ? core.get("powder_spent_mithril").getAsInt() : 0;
			d.mithrilPowder = core.has("powder_mithril_total") ? core.get("powder_mithril_total").getAsInt() : (mithrilCurrent + mithrilSpent);

			int gemCurrent = core.has("powder_gemstone") ? core.get("powder_gemstone").getAsInt() : 0;
			int gemSpent = core.has("powder_spent_gemstone") ? core.get("powder_spent_gemstone").getAsInt() : 0;
			d.gemstonePowder = core.has("powder_gemstone_total") ? core.get("powder_gemstone_total").getAsInt() : (gemCurrent + gemSpent);

			int glaciteCurrent = core.has("powder_glacite") ? core.get("powder_glacite").getAsInt() : 0;
			int glaciteSpent = core.has("powder_spent_glacite") ? core.get("powder_spent_glacite").getAsInt() : 0;
			d.glacitePowder = core.has("powder_glacite_total") ? core.get("powder_glacite_total").getAsInt() : (glaciteCurrent + glaciteSpent);

			if (core.has("nodes") && core.get("nodes").isJsonObject()) {
				parseNodesRecursively(core.getAsJsonObject("nodes"), d.nodes);
				if (d.nodes.containsKey("special_0")) d.peakOfTheMountain = d.nodes.get("special_0");
				else if (d.nodes.containsKey("peak_of_the_mountain")) d.peakOfTheMountain = d.nodes.get("peak_of_the_mountain");
				else if (d.nodes.containsKey("core_of_the_mountain")) d.peakOfTheMountain = d.nodes.get("core_of_the_mountain");
			}

			if (core.has("crystals") && core.get("crystals").isJsonObject()) {
				JsonObject cry = core.getAsJsonObject("crystals");
				for (String cName : new String[]{"jade_crystal", "amethyst_crystal", "topaz_crystal", "sapphire_crystal", "amber_crystal", "ruby_crystal", "jasper_crystal", "opal_crystal"}) {
					boolean has = false;
					if (cry.has(cName) && cry.get(cName).isJsonObject()) {
						JsonObject cObj = cry.getAsJsonObject(cName);
						String state = cObj.has("state") ? cObj.get("state").getAsString() : "";
						int totalPlaced = cObj.has("total_placed") ? cObj.get("total_placed").getAsInt() : 0;
						int totalFound = cObj.has("total_found") ? cObj.get("total_found").getAsInt() : 0;
						has = "FOUND".equalsIgnoreCase(state) || "PLACED".equalsIgnoreCase(state) || totalPlaced > 0 || totalFound > 0;
					}
					d.crystals.put(cName, has);
				}
			}

			if (core.has("selected_pickaxe_ability") && core.get("selected_pickaxe_ability").isJsonPrimitive()) {
				d.selectedAbility = core.get("selected_pickaxe_ability").getAsString();
			}

			if (core.has("nucleus_runs")) d.nucleusRuns = core.get("nucleus_runs").getAsInt();
		}

		// 2. foraging_core (Direct Foraging Data)
		if (member.has("foraging_core") && member.get("foraging_core").isJsonObject()) {
			JsonObject fc = member.getAsJsonObject("foraging_core");
			if (fc.has("nodes") && fc.get("nodes").isJsonObject()) {
				parseNodesRecursively(fc.getAsJsonObject("nodes"), d.foragingNodes);
			}
			if (fc.has("selected_ability") && fc.get("selected_ability").isJsonPrimitive()) {
				d.selectedForagingAbility = fc.get("selected_ability").getAsString();
			}
			if (fc.has("experience") && fc.get("experience").isJsonPrimitive()) {
				d.hotfExperience = fc.get("experience").getAsDouble();
				d.hotfLevel = calcHotfLevel(d.hotfExperience);
			}
		}

		// 3. skill_tree (Comprehensive Multi-Tree Support)
		if (member.has("skill_tree") && member.get("skill_tree").isJsonObject()) {
			JsonObject st = member.getAsJsonObject("skill_tree");

			// Selected skill tree slot
			JsonObject ssts = findNestedObject(st, "selected_skill_tree_slot");
			if (ssts != null) {
				if (ssts.has("mining") && ssts.get("mining").isJsonPrimitive()) {
					d.selectedMiningPreset = ssts.get("mining").getAsInt();
				}
				if (ssts.has("foraging") && ssts.get("foraging").isJsonPrimitive()) {
					d.selectedForagingPreset = ssts.get("foraging").getAsInt();
				}
			}

			// Experience
			JsonObject exp = findNestedObject(st, "experience");
			if (exp != null) {
				if (exp.has("mining") && exp.get("mining").isJsonPrimitive()) {
					d.hotmExperience = exp.get("mining").getAsDouble();
					d.hotmLevel = calcHotmLevel(d.hotmExperience);
				}
				if (exp.has("foraging") && exp.get("foraging").isJsonPrimitive()) {
					d.hotfExperience = exp.get("foraging").getAsDouble();
					d.hotfLevel = calcHotfLevel(d.hotfExperience);
				}
			}

			// Mining presets (1..5)
			for (int slot = 1; slot <= 5; slot++) {
				String suffix = slot == 1 ? "" : ("_" + slot);
				JsonObject nodeObj = findNestedPath(st, "nodes", "mining" + suffix);
				if (nodeObj == null) nodeObj = findNestedObject(st, "nodes.mining" + suffix);

				if (nodeObj != null) {
					Map<String, Integer> pMap = new LinkedHashMap<>();
					parseNodesRecursively(nodeObj, pMap);
					d.presetNodes.put(slot, pMap);
					if (slot == 1 && d.nodes.isEmpty()) d.nodes.putAll(pMap);
				}

				String ab = findNestedString(st, "selected_ability", "mining" + suffix);
				if (ab == null) ab = findNestedString(st, "selected_ability.mining" + suffix);
				if (ab != null) {
					d.presetAbilities.put(slot, ab);
					if (slot == 1 && d.selectedAbility.isEmpty()) d.selectedAbility = ab;
				}
			}

			// Foraging presets (1..5)
			for (int slot = 1; slot <= 5; slot++) {
				String suffix = slot == 1 ? "" : ("_" + slot);
				JsonObject nodeObj = findNestedPath(st, "nodes", "foraging" + suffix);
				if (nodeObj == null) nodeObj = findNestedObject(st, "nodes.foraging" + suffix);

				if (nodeObj != null) {
					Map<String, Integer> pMap = new LinkedHashMap<>();
					parseNodesRecursively(nodeObj, pMap);
					d.foragingPresetNodes.put(slot, pMap);
					if (slot == 1 && d.foragingNodes.isEmpty()) d.foragingNodes.putAll(pMap);
				}

				String ab = findNestedString(st, "selected_ability", "foraging" + suffix);
				if (ab == null) ab = findNestedString(st, "selected_ability.foraging" + suffix);
				if (ab != null) {
					d.foragingPresetAbilities.put(slot, ab);
					if (slot == 1 && d.selectedForagingAbility.isEmpty()) d.selectedForagingAbility = ab;
				}
			}
		}

		// Fallback linking for Slot 1
		if (!d.nodes.isEmpty() && !d.presetNodes.containsKey(1)) {
			d.presetNodes.put(1, d.nodes);
		}
		if (!d.selectedAbility.isEmpty() && !d.presetAbilities.containsKey(1)) {
			d.presetAbilities.put(1, d.selectedAbility);
		}
		if (!d.foragingNodes.isEmpty() && !d.foragingPresetNodes.containsKey(1)) {
			d.foragingPresetNodes.put(1, d.foragingNodes);
		}
		if (!d.selectedForagingAbility.isEmpty() && !d.foragingPresetAbilities.containsKey(1)) {
			d.foragingPresetAbilities.put(1, d.selectedForagingAbility);
		}

		if (d.foragingNodes.containsKey("center_of_the_forest")) {
			d.centerOfTheForest = d.foragingNodes.get("center_of_the_forest");
		} else if (d.foragingNodes.containsKey("core_of_the_forest")) {
			d.centerOfTheForest = d.foragingNodes.get("core_of_the_forest");
		}

		if (d.presetNodes.containsKey(d.selectedMiningPreset)) d.nodes = d.presetNodes.get(d.selectedMiningPreset);
		if (d.foragingPresetNodes.containsKey(d.selectedForagingPreset)) d.foragingNodes = d.foragingPresetNodes.get(d.selectedForagingPreset);
		d.selectedAbility = d.presetAbilities.getOrDefault(d.selectedMiningPreset, d.selectedAbility);
		d.selectedForagingAbility = d.foragingPresetAbilities.getOrDefault(d.selectedForagingPreset, d.selectedForagingAbility);
		d.peakOfTheMountain = d.nodes.getOrDefault("core_of_the_mountain", d.nodes.getOrDefault("peak_of_the_mountain", d.nodes.getOrDefault("special_0", 0)));
		d.centerOfTheForest = d.foragingNodes.getOrDefault("center_of_the_forest", d.foragingNodes.getOrDefault("core_of_the_forest", 0));
		// 4. Glacite Mineshafts Data
		if (member.has("glacite_player_data") && member.get("glacite_player_data").isJsonObject()) {
			JsonObject gpd = member.getAsJsonObject("glacite_player_data");
			if (gpd.has("mineshafts_entered") && gpd.get("mineshafts_entered").isJsonPrimitive()) {
				d.glacite.mineshaftsEntered = gpd.get("mineshafts_entered").getAsInt();
			}
			if (gpd.has("corpses_looted") && gpd.get("corpses_looted").isJsonObject()) {
				JsonObject cl = gpd.getAsJsonObject("corpses_looted");
				for (Map.Entry<String, JsonElement> e : cl.entrySet()) {
					if (e.getValue().isJsonPrimitive()) {
						int count = e.getValue().getAsInt();
						d.glacite.corpsesLooted.put(e.getKey().toLowerCase(), count);
						d.glacite.totalCorpses += count;
					}
				}
			}
			if (gpd.has("fossils_donated") && gpd.get("fossils_donated").isJsonArray()) {
				for (JsonElement el : gpd.getAsJsonArray("fossils_donated")) {
					if (el.isJsonPrimitive()) {
						d.glacite.fossilsDonated.add(el.getAsString().toLowerCase());
					}
				}
			}
		}

		return d;
	}

	private static JsonObject findNestedObject(JsonObject root, String key) {
		if (root == null || !root.has(key)) return null;
		JsonElement el = root.get(key);
		return el.isJsonObject() ? el.getAsJsonObject() : null;
	}

	private static JsonObject findNestedPath(JsonObject root, String parentKey, String childKey) {
		JsonObject p = findNestedObject(root, parentKey);
		return (p != null) ? findNestedObject(p, childKey) : null;
	}

	private static String findNestedString(JsonObject root, String parentKey, String childKey) {
		JsonObject p = findNestedObject(root, parentKey);
		if (p != null && p.has(childKey) && p.get(childKey).isJsonPrimitive()) {
			return p.get(childKey).getAsString();
		}
		return null;
	}

	private static String findNestedString(JsonObject root, String key) {
		if (root != null && root.has(key) && root.get(key).isJsonPrimitive()) {
			return root.get(key).getAsString();
		}
		return null;
	}

	private static void parseNodesRecursively(JsonObject nodesObj, Map<String, Integer> target) {
		for (Map.Entry<String, JsonElement> e : nodesObj.entrySet()) {
			if (e.getKey().startsWith("toggle_")) continue;
			if (e.getValue().isJsonPrimitive()) {
				target.put(e.getKey(), e.getValue().getAsInt());
			} else if (e.getValue().isJsonObject()) {
				parseNodesRecursively(e.getValue().getAsJsonObject(), target);
			}
		}
	}

	private static final double[] HOTM_XP_TABLE = {
		0, 3000, 12000, 37000, 97000, 197000, 347000, 557000, 847000, 1247000
	};

	public static int calcHotmLevel(double xp) {
		for (int i = 0; i < HOTM_XP_TABLE.length; i++) {
			if (xp < HOTM_XP_TABLE[i]) {
				return i;
			}
		}
		return 10;
	}

	private static final double[] HOTF_XP_TABLE = {
		0, 3000, 12000, 37000, 97000, 197000, 347000, 547000
	};

	public static int calcHotfLevel(double xp) {
		for (int i = 0; i < HOTF_XP_TABLE.length; i++) {
			if (xp < HOTF_XP_TABLE[i]) {
				return i;
			}
		}
		return 8;
	}
}