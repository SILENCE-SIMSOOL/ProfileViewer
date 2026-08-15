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
	public Map<String, Boolean> crystals = new LinkedHashMap<>();

	public static MiningData fromJson(JsonObject member) {
		MiningData d = new MiningData();
		if (member == null) return d;

		JsonObject core = member.has("mining_core") && member.get("mining_core").isJsonObject() ? member.getAsJsonObject("mining_core") : member;
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
				parseNodesRecursively(nodesObj, d);
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

		return d;
	}

	private static void parseNodesRecursively(JsonObject nodesObj, MiningData d) {
		for (Map.Entry<String, JsonElement> e : nodesObj.entrySet()) {
			if (e.getValue().isJsonPrimitive()) {
				d.nodes.put(e.getKey(), e.getValue().getAsInt());
				if ("special_0".equalsIgnoreCase(e.getKey()) || "core_of_the_mountain".equalsIgnoreCase(e.getKey())) {
					d.peakOfTheMountain = e.getValue().getAsInt();
				}
			} else if (e.getValue().isJsonObject()) {
				parseNodesRecursively(e.getValue().getAsJsonObject(), d);
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