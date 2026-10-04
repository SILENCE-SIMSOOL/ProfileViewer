package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GardenData {
	public static final JsonObject CATALOG = ProfileJson.catalog("garden_catalog");
	public int gardenLevel;
	public double gardenExperience;
	public float gardenProgress;
	public int copper;
	public int goldMedals;
	public int silverMedals;
	public int bronzeMedals;
	public int unlockedPlots;
	public int completedVisitors;
	public int uniqueVisitors;
	public int contestsParticipated;
	public int larvaConsumed;
	public int farmingLevelCap;
	public int doubleDrops;
	public double composterOrganicMatter;
	public double composterFuel;
	public int composterItems;
	public int composterOrganicCapUpgrade;
	public int composterCostReductionUpgrade;
	public int composterFuelCapUpgrade;
	public int composterSpeedUpgrade;
	public int composterMultiDropUpgrade;
	public Map<String, Long> cropMilestones = new LinkedHashMap<>();
	public Map<String, Long> personalBests = new LinkedHashMap<>();
	public Map<String, Integer> chipLevels = new LinkedHashMap<>();
	public Map<String, Integer> visitorVisits = new LinkedHashMap<>();
	public Map<String, Integer> visitorCompletions = new LinkedHashMap<>();

	public static GardenData fromJson(JsonObject json) {
		GardenData d = new GardenData();
		JsonObject garden = json != null && json.has("garden") ? ProfileJson.object(json, "garden") : json;
		d.gardenExperience = ProfileJson.number(garden, "garden_experience");
		d.gardenLevel = calcGardenLevel(d.gardenExperience);
		d.copper = (int) ProfileJson.number(garden, "copper");
		d.larvaConsumed = (int) ProfileJson.number(garden, "larva_consumed");
		d.unlockedPlots = ProfileJson.array(garden, "unlocked_plots_ids").size();
		ProfileJson.object(garden, "resources_collected").entrySet().forEach(e -> d.cropMilestones.put(cropKey(e.getKey()), e.getValue().getAsLong()));
		JsonObject commissions = ProfileJson.object(garden, "commission_data");
		ProfileJson.object(commissions, "visits").entrySet().forEach(e -> d.visitorVisits.put(e.getKey(), e.getValue().getAsInt()));
		ProfileJson.object(commissions, "completed").entrySet().forEach(e -> d.visitorCompletions.put(e.getKey(), e.getValue().getAsInt()));
		d.completedVisitors = commissions.has("total_completed") ? (int) ProfileJson.number(commissions, "total_completed") : d.visitorCompletions.values().stream().mapToInt(Integer::intValue).sum();
		d.uniqueVisitors = commissions.has("unique_npcs_served") ? (int) ProfileJson.number(commissions, "unique_npcs_served") : (int) d.visitorCompletions.values().stream().filter(v -> v > 0).count();
		JsonObject composter = ProfileJson.object(garden, "composter_data");
		d.composterOrganicMatter = ProfileJson.number(composter, "organic_matter");
		d.composterFuel = ProfileJson.number(composter, "fuel_units");
		d.composterItems = (int) ProfileJson.number(composter, "compost_items");
		JsonObject upgrades = ProfileJson.object(composter, "upgrades");
		d.composterSpeedUpgrade = (int) ProfileJson.number(upgrades, "speed");
		d.composterMultiDropUpgrade = (int) ProfileJson.number(upgrades, "multi_drop");
		d.composterFuelCapUpgrade = (int) ProfileJson.number(upgrades, "fuel_cap");
		d.composterOrganicCapUpgrade = (int) ProfileJson.number(upgrades, "organic_matter_cap");
		d.composterCostReductionUpgrade = (int) ProfileJson.number(upgrades, "cost_reduction");
		JsonObject jacob = ProfileJson.object(json, "jacob2");
		d.goldMedals = (int) ProfileJson.number(jacob, "medals_inv", "gold");
		d.silverMedals = (int) ProfileJson.number(jacob, "medals_inv", "silver");
		d.bronzeMedals = (int) ProfileJson.number(jacob, "medals_inv", "bronze");
		d.contestsParticipated = ProfileJson.object(jacob, "contests").size();
		d.farmingLevelCap = (int) ProfileJson.number(jacob, "perks", "farming_level_cap");
		d.doubleDrops = (int) ProfileJson.number(jacob, "perks", "double_drops");
		ProfileJson.object(jacob, "personal_bests").entrySet().forEach(e -> d.personalBests.put(cropKey(e.getKey()), e.getValue().getAsLong()));
		ProfileJson.object(json, "player_data", "garden_chips").entrySet().forEach(e -> d.chipLevels.put(e.getKey(), e.getValue().getAsInt()));
		return d;
	}

	public void copyMemberData(GardenData member) {
		copper = member.copper;
		larvaConsumed = member.larvaConsumed;
		goldMedals = member.goldMedals;
		silverMedals = member.silverMedals;
		bronzeMedals = member.bronzeMedals;
		contestsParticipated = member.contestsParticipated;
		farmingLevelCap = member.farmingLevelCap;
		doubleDrops = member.doubleDrops;
		personalBests = member.personalBests;
		chipLevels = member.chipLevels;
	}

	public static String cropKey(String id) {
		return switch (id.toUpperCase(Locale.ROOT)) {
			case "CARROT_ITEM" -> "carrot";
			case "POTATO_ITEM" -> "potato";
			case "INK_SACK:3" -> "cocoa_beans";
			case "MUSHROOM_COLLECTION" -> "mushroom";
			case "NETHER_STALK" -> "nether_wart";
			default -> id.toLowerCase(Locale.ROOT);
		};
	}

	public static int calcGardenLevel(double xp) {
		long total = 0;
		int level = 0;
		for (var step : ProfileJson.array(CATALOG, "misc", "garden_level")) {
			total += step.getAsLong();
			if (xp < total) break;
			level++;
		}
		return level;
	}

	public static int milestoneLevel(String cropId, long amount) {
		long total = 0;
		int level = 0;
		for (var step : ProfileJson.array(CATALOG, "crop_milestones", cropId)) {
			total += step.getAsLong();
			if (amount < total) break;
			level++;
		}
		return level;
	}

	public static long chipSowdust(int level) {
		JsonArray costs = ProfileJson.array(CATALOG, "chips");
		long total = 0;
		for (int i = 0; i < Math.min(Math.max(0, level - 1), costs.size()); i++) total += costs.get(i).getAsLong();
		return total;
	}
}
