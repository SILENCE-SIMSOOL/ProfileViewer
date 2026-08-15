package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GardenData {

	public int gardenLevel = 0;
	public double gardenExperience = 0;
	public float gardenProgress = 0f;
	public int copper = 0;
	public int jacobGold = 0;
	public int jacobSilver = 0;
	public int jacobBronze = 0;

	public int completedVisitors = 0;
	public int uniqueVisitors = 0;

	// Composter upgrades
	public int composterSpeed = 0;
	public int composterMultiDrop = 0;
	public int composterFuelCap = 0;
	public int composterCostReduction = 0;

	// 10 crops milestone: crop_id -> total collected
	public Map<String, Long> cropMilestones = new LinkedHashMap<>();

	public static GardenData fromJson(JsonObject json) {
		GardenData d = new GardenData();
		if (json == null) return d;

		JsonObject gd = json.has("garden_player_data") && json.get("garden_player_data").isJsonObject() ? json.getAsJsonObject("garden_player_data") : json;

		if (gd.has("copper") && gd.get("copper").isJsonPrimitive()) d.copper = gd.get("copper").getAsInt();
		if (gd.has("experience") && gd.get("experience").isJsonPrimitive()) {
			d.gardenExperience = gd.get("experience").getAsDouble();
			d.gardenLevel = calcGardenLevel(d.gardenExperience);
		} else if (gd.has("level") && gd.get("level").isJsonPrimitive()) {
			d.gardenLevel = gd.get("level").getAsInt();
		}

		if (gd.has("completed_visitors") && gd.get("completed_visitors").isJsonObject()) {
			JsonObject cv = gd.getAsJsonObject("completed_visitors");
			d.uniqueVisitors = cv.size();
			int sum = 0;
			for (Map.Entry<String, JsonElement> e : cv.entrySet()) {
				if (e.getValue().isJsonPrimitive()) {
					sum += e.getValue().getAsInt();
				}
			}
			d.completedVisitors = sum;
		}

		if (gd.has("composter_data") && gd.get("composter_data").isJsonObject()) {
			JsonObject cd = gd.getAsJsonObject("composter_data");
			if (cd.has("speed_upgrade")) d.composterSpeed = cd.get("speed_upgrade").getAsInt();
			if (cd.has("multi_drop_upgrade")) d.composterMultiDrop = cd.get("multi_drop_upgrade").getAsInt();
			if (cd.has("fuel_cap_upgrade")) d.composterFuelCap = cd.get("fuel_cap_upgrade").getAsInt();
			if (cd.has("cost_reduction_upgrade")) d.composterCostReduction = cd.get("cost_reduction_upgrade").getAsInt();
		}

		if (gd.has("crop_milestones") && gd.get("crop_milestones").isJsonObject()) {
			JsonObject cm = gd.getAsJsonObject("crop_milestones");
			for (Map.Entry<String, JsonElement> e : cm.entrySet()) {
				if (e.getValue().isJsonPrimitive()) {
					d.cropMilestones.put(e.getKey().toLowerCase(), e.getValue().getAsLong());
				}
			}
		}

		JsonObject jc = json.has("jacobs_contest") && json.get("jacobs_contest").isJsonObject()
				? json.getAsJsonObject("jacobs_contest")
				: (gd.has("jacobs_contest") && gd.get("jacobs_contest").isJsonObject()
				? gd.getAsJsonObject("jacobs_contest")
				: (json.has("medals_inv") ? json : null));

		if (jc != null && jc.has("medals_inv") && jc.get("medals_inv").isJsonObject()) {
			JsonObject mi = jc.getAsJsonObject("medals_inv");
			if (mi.has("gold") && mi.get("gold").isJsonPrimitive()) d.jacobGold = mi.get("gold").getAsInt();
			if (mi.has("silver") && mi.get("silver").isJsonPrimitive()) d.jacobSilver = mi.get("silver").getAsInt();
			if (mi.has("bronze") && mi.get("bronze").isJsonPrimitive()) d.jacobBronze = mi.get("bronze").getAsInt();
		}

		// Ensure 10 crop entries
		String[] crops = {"wheat", "carrot", "potato", "pumpkin", "melon", "sugar_cane", "cactus", "cocoa_beans", "mushroom", "nether_wart"};
		for (String c : crops) {
			d.cropMilestones.putIfAbsent(c, 0L);
		}

		return d;
	}

	private static final double[] GARDEN_XP_TABLE = {
		0, 70, 210, 490, 970, 1770, 3070, 5070, 8070, 12570, 19070, 28070, 40070, 56070, 76070, 100070
	};

	public static int calcGardenLevel(double xp) {
		for (int i = 0; i < GARDEN_XP_TABLE.length; i++) {
			if (xp < GARDEN_XP_TABLE[i]) {
				return i;
			}
		}
		return 15;
	}
}