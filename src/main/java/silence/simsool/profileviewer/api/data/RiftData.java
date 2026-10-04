package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RiftData {
	public static final JsonObject CATALOG = ProfileJson.catalog("rift_catalog");
	public final List<String> foundCats = new ArrayList<>();
	public final List<String> unlockedEyes = new ArrayList<>();
	public int visits;
	public int secondsSitting;
	public int grubberStacks;
	public String montezumaRarity = "";
	public double montezumaExperience;

	public int motes = 0;
	public int lifetimeMotes;
	public InventoryData inventory = new InventoryData();
	public int timecharms = 0;
	public int enigmaSouls = 0;
	public int porhtalProgress = 0;

	public List<String> unlockedTimecharms = new ArrayList<>();
	public Map<String, Integer> cruxKills = new LinkedHashMap<>();

	public static final Map<String, String> TROPHIES = loadTrophies();
	public static final String[] TIMECHARMS = TROPHIES.values().toArray(String[]::new);

	private static Map<String, String> loadTrophies() {
		Map<String, String> trophies = new LinkedHashMap<>();
		for (var entry : ProfileJson.array(CATALOG, "trophies")) {
			JsonObject trophy = entry.getAsJsonObject();
			trophies.put(ProfileJson.string(trophy, "id"), ProfileJson.string(trophy, "name"));
		}
		return trophies;
	}

	public static String timecharmId(String name) {
		return TROPHIES.entrySet().stream().filter(e -> e.getValue().equals(name)).map(e -> "RIFT_TROPHY_" + e.getKey().toUpperCase(Locale.ROOT)).findFirst().orElse("");
	}
	public static RiftData fromJson(JsonObject member) {
		RiftData d = new RiftData();
		d.visits = (int) ProfileJson.number(member, "player_stats", "rift", "visits");
		d.secondsSitting = (int) ProfileJson.number(member, "rift", "village_plaza", "lonely", "seconds_sitting");
		d.grubberStacks = (int) ProfileJson.number(member, "rift", "castle", "grubber_stacks");
		for (var cat : ProfileJson.array(member, "rift", "dead_cats", "found_cats")) {
			if (!d.foundCats.contains(cat.getAsString())) d.foundCats.add(cat.getAsString());
		}
		JsonObject montezuma = ProfileJson.object(member, "rift", "dead_cats", "montezuma");
		d.montezumaRarity = ProfileJson.string(montezuma, "tier");
		d.montezumaExperience = montezuma.has("exp") ? montezuma.get("exp").getAsDouble() : 0;
		if (member == null) return d;

		if (member.has("currencies") && member.get("currencies").isJsonObject()) {
			JsonObject cur = member.getAsJsonObject("currencies");
			if (cur.has("motes_purse") && cur.get("motes_purse").isJsonPrimitive()) {
				d.motes = cur.get("motes_purse").getAsInt();
			}
		}

		if (member.has("player_stats") && member.get("player_stats").isJsonObject()) {
			JsonObject ps = member.getAsJsonObject("player_stats");
			if (ps.has("rift") && ps.get("rift").isJsonObject()) {
				JsonObject pr = ps.getAsJsonObject("rift");
				if (pr.has("lifetime_motes_earned")) {
					d.lifetimeMotes = pr.get("lifetime_motes_earned").getAsInt();
				}
			}
		}

		if (member.has("rift") && member.get("rift").isJsonObject()) {
			JsonObject r = member.getAsJsonObject("rift");
			d.inventory = InventoryData.fromJson(r);
			if (r.has("motes") && r.get("motes").isJsonPrimitive()) d.motes = r.get("motes").getAsInt();
			if (r.has("motes_purse") && r.get("motes_purse").isJsonPrimitive()) d.motes = r.get("motes_purse").getAsInt();
			if (r.has("enigma") && r.get("enigma").isJsonObject()) {
				JsonObject en = r.getAsJsonObject("enigma");
				if (en.has("found_souls") && en.get("found_souls").isJsonArray()) {
					d.enigmaSouls = en.getAsJsonArray("found_souls").size();
				}
			}
			if (r.has("gallery") && r.get("gallery").isJsonObject()) {
				JsonObject gal = r.getAsJsonObject("gallery");
				if (gal.has("secured_trophies") && gal.get("secured_trophies").isJsonArray()) {
					for (JsonElement el : gal.getAsJsonArray("secured_trophies")) {
						if (el.isJsonObject() && el.getAsJsonObject().has("type")) {
							String tName = formatTimecharmName(el.getAsJsonObject().get("type").getAsString());
							if (!tName.isEmpty() && !d.unlockedTimecharms.contains(tName)) d.unlockedTimecharms.add(tName);
						}
					}
				}
			}
			d.timecharms = d.unlockedTimecharms.size();

			if (r.has("wither_cage") && r.get("wither_cage").isJsonObject()) {
				JsonObject wc = r.getAsJsonObject("wither_cage");
				if (wc.has("killed_eyes") && wc.get("killed_eyes").isJsonArray()) {
					for (var eye : wc.getAsJsonArray("killed_eyes")) {
						if (!d.unlockedEyes.contains(eye.getAsString())) d.unlockedEyes.add(eye.getAsString());
					}
					d.porhtalProgress = d.unlockedEyes.size();
				}
			}
		}

		return d;
	}

	private static String formatTimecharmName(String raw) {
		return TROPHIES.getOrDefault(raw.toLowerCase(Locale.ROOT), "");
	}
}