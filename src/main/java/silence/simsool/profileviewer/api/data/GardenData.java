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
	public int goldMedals = 0;
	public int silverMedals = 0;
	public int bronzeMedals = 0;
	public int unlockedPlots = 0;

	public int completedVisitors = 0;
	public int uniqueVisitors = 0;

	// Composter info
	public double composterOrganicMatter = 0;
	public double composterFuel = 0;
	public int composterOrganicCapUpgrade = 0;
	public int composterCostReductionUpgrade = 0;
	public int composterFuelCapUpgrade = 0;
	public int composterSpeedUpgrade = 0;

	// 10 crops milestone / resources collected: crop_id -> total collected
	public Map<String, Long> cropMilestones = new LinkedHashMap<>();
	public Map<String, Long> personalBests = new LinkedHashMap<>();

	public static GardenData fromJson(JsonObject json) {
		GardenData d = new GardenData();
		if (json == null) return d;

		JsonObject gd = json.has("garden") && json.get("garden").isJsonObject() ? json.getAsJsonObject("garden") : json;

		// Garden XP & Level
		if (gd.has("garden_experience") && gd.get("garden_experience").isJsonPrimitive()) {
			d.gardenExperience = gd.get("garden_experience").getAsDouble();
			d.gardenLevel = calcGardenLevel(d.gardenExperience);
		} else if (gd.has("experience") && gd.get("experience").isJsonPrimitive()) {
			d.gardenExperience = gd.get("experience").getAsDouble();
			d.gardenLevel = calcGardenLevel(d.gardenExperience);
		} else if (gd.has("level") && gd.get("level").isJsonPrimitive()) {
			d.gardenLevel = gd.get("level").getAsInt();
		}

		// Copper
		if (gd.has("copper") && gd.get("copper").isJsonPrimitive()) {
			d.copper = gd.get("copper").getAsInt();
		}

		// Unlocked Plots
		if (gd.has("unlocked_plots_ids") && gd.get("unlocked_plots_ids").isJsonArray()) {
			d.unlockedPlots = gd.getAsJsonArray("unlocked_plots_ids").size();
		}

		// Resources Collected / Crop Milestones
		JsonObject rc = gd.has("resources_collected") && gd.get("resources_collected").isJsonObject()
				? gd.getAsJsonObject("resources_collected")
				: (gd.has("crop_milestones") && gd.get("crop_milestones").isJsonObject() ? gd.getAsJsonObject("crop_milestones") : null);

		if (rc != null) {
			for (Map.Entry<String, JsonElement> e : rc.entrySet()) {
				if (e.getValue().isJsonPrimitive()) {
					d.cropMilestones.put(e.getKey().toLowerCase(), e.getValue().getAsLong());
				}
			}
		}

		// Visitors
		if (gd.has("commission_data") && gd.get("commission_data").isJsonObject()) {
			JsonObject cd = gd.getAsJsonObject("commission_data");
			if (cd.has("visits") && cd.get("visits").isJsonObject()) {
				JsonObject visits = cd.getAsJsonObject("visits");
				d.uniqueVisitors = visits.size();
				int sum = 0;
				for (Map.Entry<String, JsonElement> e : visits.entrySet()) {
					if (e.getValue().isJsonPrimitive()) sum += e.getValue().getAsInt();
				}
				d.completedVisitors = sum;
			} else if (cd.has("completed_orders") && cd.get("completed_orders").isJsonPrimitive()) {
				d.completedVisitors = cd.get("completed_orders").getAsInt();
			}
		} else if (gd.has("completed_visitors") && gd.get("completed_visitors").isJsonObject()) {
			JsonObject cv = gd.getAsJsonObject("completed_visitors");
			d.uniqueVisitors = cv.size();
			int sum = 0;
			for (Map.Entry<String, JsonElement> e : cv.entrySet()) {
				if (e.getValue().isJsonPrimitive()) sum += e.getValue().getAsInt();
			}
			d.completedVisitors = sum;
		}

		// Composter Data
		if (gd.has("composter_data") && gd.get("composter_data").isJsonObject()) {
			JsonObject comp = gd.getAsJsonObject("composter_data");
			if (comp.has("organic_matter") && comp.get("organic_matter").isJsonPrimitive()) {
				d.composterOrganicMatter = comp.get("organic_matter").getAsDouble();
			}
			if (comp.has("fuel_units") && comp.get("fuel_units").isJsonPrimitive()) {
				d.composterFuel = comp.get("fuel_units").getAsDouble();
			}
			if (comp.has("upgrades") && comp.get("upgrades").isJsonObject()) {
				JsonObject up = comp.getAsJsonObject("upgrades");
				if (up.has("speed")) d.composterSpeedUpgrade = up.get("speed").getAsInt();
				if (up.has("organic_matter_cap")) d.composterOrganicCapUpgrade = up.get("organic_matter_cap").getAsInt();
				if (up.has("cost_reduction")) d.composterCostReductionUpgrade = up.get("cost_reduction").getAsInt();
				if (up.has("fuel_cap")) d.composterFuelCapUpgrade = up.get("fuel_cap").getAsInt();
			}
		}

		// Jacob's Contest / Medals
		JsonObject jc = json.has("jacob2") && json.get("jacob2").isJsonObject()
				? json.getAsJsonObject("jacob2")
				: (json.has("jacobs_contest") && json.get("jacobs_contest").isJsonObject()
				? json.getAsJsonObject("jacobs_contest")
				: (gd.has("jacob2") && gd.get("jacob2").isJsonObject() ? gd.getAsJsonObject("jacob2") : null));

		if (jc != null) {
			if (jc.has("medals_inv") && jc.get("medals_inv").isJsonObject()) {
				JsonObject mi = jc.getAsJsonObject("medals_inv");
				if (mi.has("gold") && mi.get("gold").isJsonPrimitive()) d.goldMedals = mi.get("gold").getAsInt();
				if (mi.has("silver") && mi.get("silver").isJsonPrimitive()) d.silverMedals = mi.get("silver").getAsInt();
				if (mi.has("bronze") && mi.get("bronze").isJsonPrimitive()) d.bronzeMedals = mi.get("bronze").getAsInt();
			}
			if (jc.has("personal_bests") && jc.get("personal_bests").isJsonObject()) {
				JsonObject pb = jc.getAsJsonObject("personal_bests");
				for (Map.Entry<String, JsonElement> e : pb.entrySet()) {
					if (e.getValue().isJsonPrimitive()) {
						d.personalBests.put(e.getKey().toLowerCase(), e.getValue().getAsLong());
					}
				}
			}
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
			if (xp < GARDEN_XP_TABLE[i]) return i;
		}
		return GARDEN_XP_TABLE.length;
	}
}