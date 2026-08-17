package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

	public Map<String, Integer> nodes = new LinkedHashMap<>();
	public Map<Integer, Map<String, Integer>> presetNodes = new LinkedHashMap<>();
	public Map<Integer, String> presetAbilities = new LinkedHashMap<>();

	// HOTF (Foraging) data
	public int hotfLevel = 0;
	public double hotfExperience = 0;
	public String selectedForagingAbility = "";
	public Map<String, Integer> foragingNodes = new LinkedHashMap<>();
	public Map<Integer, Map<String, Integer>> foragingPresetNodes = new LinkedHashMap<>();
	public Map<Integer, String> foragingPresetAbilities = new LinkedHashMap<>();

	public Map<String, Boolean> crystals = new LinkedHashMap<>();

	public static MiningData fromJson(JsonObject member) {
		MiningData d = new MiningData();
		if (member == null) return d;

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
				JsonObject nodesObj = core.getAsJsonObject("nodes");
				parseNodesRecursively(nodesObj, d.nodes);
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

		// Also parse skill_tree for multi-preset mining and foraging trees
		if (member.has("skill_tree") && member.get("skill_tree").isJsonObject()) {
			JsonObject st = member.getAsJsonObject("skill_tree");

			// Mining presets (1..5)
			for (int slot = 1; slot <= 5; slot++) {
				String suffix = slot == 1 ? "" : ("_" + slot);
				String nodeKey = "nodes.mining" + suffix;
				Map<String, Integer> pMap = new LinkedHashMap<>();
				if (st.has(nodeKey) && st.get(nodeKey).isJsonObject()) {
					parseNodesRecursively(st.getAsJsonObject(nodeKey), pMap);
				}
				if (!pMap.isEmpty()) {
					d.presetNodes.put(slot, pMap);
				}

				String abKey = "selected_ability.mining" + suffix;
				if (st.has(abKey) && st.get(abKey).isJsonPrimitive()) {
					d.presetAbilities.put(slot, st.get(abKey).getAsString());
				}
			}

			// Foraging presets (1..5)
			for (int slot = 1; slot <= 5; slot++) {
				String suffix = slot == 1 ? "" : ("_" + slot);
				String nodeKey = "nodes.foraging" + suffix;
				Map<String, Integer> pMap = new LinkedHashMap<>();
				if (st.has(nodeKey) && st.get(nodeKey).isJsonObject()) {
					parseNodesRecursively(st.getAsJsonObject(nodeKey), pMap);
				}
				if (!pMap.isEmpty()) {
					d.foragingPresetNodes.put(slot, pMap);
				}

				String abKey = "selected_ability.foraging" + suffix;
				if (st.has(abKey) && st.get(abKey).isJsonPrimitive()) {
					d.foragingPresetAbilities.put(slot, st.get(abKey).getAsString());
				}
			}
		}

		// Default fallback for slot 1
		if (!d.nodes.isEmpty() && !d.presetNodes.containsKey(1)) {
			d.presetNodes.put(1, d.nodes);
		}
		if (!d.selectedAbility.isEmpty() && !d.presetAbilities.containsKey(1)) {
			d.presetAbilities.put(1, d.selectedAbility);
		}

		// Fallback for foraging core
		if (member.has("foraging_core") && member.get("foraging_core").isJsonObject()) {
			JsonObject fc = member.getAsJsonObject("foraging_core");
			if (fc.has("nodes") && fc.get("nodes").isJsonObject()) {
				parseNodesRecursively(fc.getAsJsonObject("nodes"), d.foragingNodes);
			}
			if (fc.has("selected_ability") && fc.get("selected_ability").isJsonPrimitive()) {
				d.selectedForagingAbility = fc.get("selected_ability").getAsString();
			}
		}
		if (!d.foragingNodes.isEmpty() && !d.foragingPresetNodes.containsKey(1)) {
			d.foragingPresetNodes.put(1, d.foragingNodes);
		}

		return d;
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
		0, 3000, 9000, 25000, 60000, 100000, 150000, 210000, 290000, 400000
	};

	public static int calcHotmLevel(double xp) {
		for (int i = 0; i < HOTM_XP_TABLE.length; i++) {
			if (xp < HOTM_XP_TABLE[i]) {
				return i;
			}
		}
		return 10;
	}
}