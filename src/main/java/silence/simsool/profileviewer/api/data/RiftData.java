package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RiftData {

	public int motes = 0;
	public int lifetimeMotes;
	public InventoryData inventory = new InventoryData();
	public int timecharms = 0;
	public int enigmaSouls = 0;
	public int porhtalProgress = 0;

	public List<String> unlockedTimecharms = new ArrayList<>();
	public Map<String, Integer> cruxKills = new LinkedHashMap<>();

	public static final String[] TIMECHARMS = {
		"Supreme Timecharm", "Teary Timecharm", "Twilight Timecharm",
		"Vampiric Timecharm", "Unhinged Timecharm", "Mirrorverse Timecharm",
		"Living Timecharm", "Wormhole Timecharm"
	};

	public static RiftData fromJson(JsonObject member) {
		RiftData d = new RiftData();
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
							d.unlockedTimecharms.add(tName);
						}
					}
				}
			}
			d.timecharms = d.unlockedTimecharms.size();

			if (r.has("dead_cats") && r.get("dead_cats").isJsonObject()) {
				JsonObject dc = r.getAsJsonObject("dead_cats");
				if (dc.has("montezuma") && dc.get("montezuma").isJsonObject()) {
					// Montezuma pet
				}
			}

			if (r.has("wither_cage") && r.get("wither_cage").isJsonObject()) {
				JsonObject wc = r.getAsJsonObject("wither_cage");
				if (wc.has("killed_eyes") && wc.get("killed_eyes").isJsonArray()) {
					d.porhtalProgress = wc.getAsJsonArray("killed_eyes").size();
				}
			}
		}

		return d;
	}

	private static String formatTimecharmName(String raw) {
		for (String tc : TIMECHARMS) {
			String key = tc.toLowerCase().replace(" timecharm", "").replace(" ", "_");
			if (raw.toLowerCase().contains(key)) {
				return tc;
			}
		}
		return raw.replace("_", " ");
	}
}