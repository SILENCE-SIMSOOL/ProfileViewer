package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public class ForagingData {
	public static final JsonObject CATALOG = ProfileJson.catalog("foraging_catalog");
	public Map<String, Long> personalBests = new LinkedHashMap<>();
	public Map<String, Long> treeGifts = new LinkedHashMap<>();
	public Map<String, Integer> giftTiers = new LinkedHashMap<>();
	public Map<String, Integer> perks = new LinkedHashMap<>();
	public long forestWhispers;
	public long desertWhispers;
	public long forestTotal;
	public long desertTotal;
	public int dailyTrees;
	public int dailyGifts;

	public static ForagingData fromJson(JsonObject member) {
		ForagingData data = new ForagingData();
		JsonObject foraging = ProfileJson.object(member, "foraging");
		ProfileJson.object(foraging, "starlyn", "personal_bests").entrySet().forEach(e -> data.personalBests.put(e.getKey(), e.getValue().getAsLong()));
		JsonObject gifts = ProfileJson.object(foraging, "tree_gifts");
		for (String type : new String[]{"FIG", "MANGROVE", "HELIX"}) {
			data.treeGifts.put(type, ProfileJson.number(gifts, type));
			data.giftTiers.put(type, (int) ProfileJson.number(gifts, "milestone_tier_claimed", type));
		}
		ProfileJson.object(member, "player_data", "perks").entrySet().forEach(e -> data.perks.put(e.getKey(), e.getValue().getAsInt()));
		JsonObject core = ProfileJson.object(member, "foraging_core");
		data.dailyTrees = (int) ProfileJson.number(core, "daily_trees_cut");
		data.dailyGifts = (int) ProfileJson.number(core, "daily_gifts");
		String slot = String.valueOf(Math.max(1, ProfileJson.number(member, "skill_tree", "selected_skill_tree_slot", "foraging")));
		JsonObject whispers = ProfileJson.object(core, "whispers");
		data.forestTotal = ProfileJson.number(whispers, "forest", "total");
		data.desertTotal = ProfileJson.number(whispers, "desert", "total");
		data.forestWhispers = data.forestTotal - ProfileJson.number(whispers, "forest", slot, "spent");
		data.desertWhispers = data.desertTotal - ProfileJson.number(whispers, "desert", slot, "spent");
		return data;
	}
}
