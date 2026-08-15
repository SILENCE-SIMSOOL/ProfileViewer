package silence.simsool.profileviewer.api.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DungeonData {

	public static final long[] CATACOMBS_XP_TABLE = {
		50, 125, 235, 395, 625, 955, 1425, 2095, 3045, 4385,
		6275, 8940, 12700, 17960, 25340, 35640, 50040, 70040, 97640, 135640,
		188140, 259640, 356640, 488640, 668640, 911640, 1239640, 1681640, 2276640, 3076640,
		4146640, 5576640, 7476640, 9976640, 13276640, 17576640, 23176640, 30376640, 39576640, 51176640,
		65776640, 83976640, 106476640, 134176640, 168076640, 209376640, 259476640, 319876640, 392076640, 477676640, 569809640
	};

	public static class ClassInfo {
		public String name;
		public long xp = 0;
		public int level = 0;
		public float progress = 0f;
		public boolean isSelected = false;

		public ClassInfo(String name) {
			this.name = name;
		}
	}

	public static class FloorStats {
		public int completions = 0;
		public long fastestTimeMs = 0;
		public long fastestTimeSplusMs = 0;
		public int bestScore = 0;

		public String getFastestTimeFormatted() {
			return formatTime(fastestTimeMs);
		}

		public String getFastestSPlusFormatted() {
			return formatTime(fastestTimeSplusMs);
		}

		private static String formatTime(long ms) {
			if (ms <= 0) return "N/A";
			long sec = ms / 1000;
			long m = sec / 60;
			long s = sec % 60;
			return String.format("%02d:%02d", m, s);
		}
	}

	public int catacombsLevel = 0;
	public long catacombsXp = 0;
	public float catacombsProgress = 0f;
	public long secretsFound = 0;
	public double secretsPerRun = 0.0;
	public double classAverage = 0.0;
	public String selectedClass = "";

	public Map<String, ClassInfo> classes = new LinkedHashMap<>();
	public Map<String, FloorStats> normalFloors = new LinkedHashMap<>();
	public Map<String, FloorStats> masterFloors = new LinkedHashMap<>();
	public int totalNormalRuns = 0;
	public int totalMasterRuns = 0;

	// Bestiary
	public long bestiaryKills = 0;
	public Map<String, Integer> bestiaryMobKills = new LinkedHashMap<>();

	// Crimson Isle
	public String crimsonFaction = "None";
	public int mageReputation = 0;
	public int barbarianReputation = 0;
	public Map<String, Integer> kuudraCompletions = new HashMap<>();
	public Map<String, Integer> dojoScores = new HashMap<>();

	public static Pair<Integer, Float> getLevelAndProgress(long xp) {
		if (xp <= 0) return new Pair<>(0, 0f);
		for (int i = 0; i < CATACOMBS_XP_TABLE.length; i++) {
			long req = CATACOMBS_XP_TABLE[i];
			if (xp < req) {
				long prev = (i == 0) ? 0L : CATACOMBS_XP_TABLE[i - 1];
				float prog = (float)(xp - prev) / (float)(req - prev);
				return new Pair<>(i, Math.max(0f, Math.min(1f, prog)));
			}
		}
		// Overflow past 50
		long maxReq = CATACOMBS_XP_TABLE[CATACOMBS_XP_TABLE.length - 1];
		long overflowXp = xp - maxReq;
		long xpPerOverflow = 200_000_000L;
		int overflowLvl = (int)(overflowXp / xpPerOverflow);
		float overflowProg = (float)(overflowXp % xpPerOverflow) / (float) xpPerOverflow;
		return new Pair<>(50 + overflowLvl, Math.max(0f, Math.min(1f, overflowProg)));
	}

	public static class Pair<A, B> {
		public final A first;
		public final B second;
		public Pair(A first, B second) {
			this.first = first;
			this.second = second;
		}
	}

	public static DungeonData fromJson(JsonObject member) {
		DungeonData d = new DungeonData();
		if (member == null) return d;

		// Initialize 5 Classes
		String[] classNames = {"healer", "mage", "berserk", "archer", "tank"};
		for (String c : classNames) {
			d.classes.put(c, new ClassInfo(c.substring(0, 1).toUpperCase() + c.substring(1)));
		}

		// Initialize Floors (F0~F7, M1~M7)
		d.normalFloors.put("F0", new FloorStats());
		for (int i = 1; i <= 7; i++) {
			d.normalFloors.put("F" + i, new FloorStats());
			d.masterFloors.put("M" + i, new FloorStats());
		}

		// Find dungeons JsonObject (could be root, member.dungeons, or member.player_stats.dungeons)
		JsonObject dObj = null;
		if (member.has("dungeons") && member.get("dungeons").isJsonObject()) {
			dObj = member.getAsJsonObject("dungeons");
		} else if (member.has("dungeon_types")) {
			dObj = member;
		} else if (member.has("player_stats") && member.getAsJsonObject("player_stats").has("dungeons")) {
			dObj = member.getAsJsonObject("player_stats").getAsJsonObject("dungeons");
		}

		if (dObj != null) {
			if (dObj.has("selected_dungeon_class") && dObj.get("selected_dungeon_class").isJsonPrimitive()) {
				d.selectedClass = dObj.get("selected_dungeon_class").getAsString();
			}

			if (dObj.has("secrets") && dObj.get("secrets").isJsonPrimitive()) {
				try { d.secretsFound = dObj.get("secrets").getAsLong(); } catch (Exception ignored) {}
			} else if (member.has("player_stats") && member.getAsJsonObject("player_stats").has("secrets")) {
				try { d.secretsFound = member.getAsJsonObject("player_stats").get("secrets").getAsLong(); } catch (Exception ignored) {}
			}

			// Player Classes Experience
			if (dObj.has("player_classes") && dObj.get("player_classes").isJsonObject()) {
				JsonObject pc = dObj.getAsJsonObject("player_classes");
				double classSum = 0;
				for (String cName : classNames) {
					if (pc.has(cName)) {
						JsonElement ce = pc.get(cName);
						long xp = 0;
						if (ce.isJsonObject() && ce.getAsJsonObject().has("experience")) {
							xp = (long) ce.getAsJsonObject().get("experience").getAsDouble();
						} else if (ce.isJsonPrimitive()) {
							xp = (long) ce.getAsDouble();
						}

						ClassInfo ci = d.classes.get(cName);
						if (ci != null) {
							ci.xp = xp;
							Pair<Integer, Float> lp = getLevelAndProgress(xp);
							ci.level = lp.first;
							ci.progress = lp.second;
							ci.isSelected = cName.equalsIgnoreCase(d.selectedClass);
							classSum += Math.min(50, ci.level);
						}
					}
				}
				d.classAverage = classSum / 5.0;
			}

			// Dungeon Types (Catacombs & Master Catacombs)
			if (dObj.has("dungeon_types") && dObj.get("dungeon_types").isJsonObject()) {
				JsonObject dt = dObj.getAsJsonObject("dungeon_types");

				// Normal Catacombs
				if (dt.has("catacombs") && dt.get("catacombs").isJsonObject()) {
					JsonObject cata = dt.getAsJsonObject("catacombs");
					if (cata.has("experience") && cata.get("experience").isJsonPrimitive()) {
						d.catacombsXp = (long) cata.get("experience").getAsDouble();
						Pair<Integer, Float> lp = getLevelAndProgress(d.catacombsXp);
						d.catacombsLevel = lp.first;
						d.catacombsProgress = lp.second;
					}
					parseFloors(cata, d.normalFloors, "F");
				}

				// Master Catacombs
				if (dt.has("master_catacombs") && dt.get("master_catacombs").isJsonObject()) {
					JsonObject mc = dt.getAsJsonObject("master_catacombs");
					parseFloors(mc, d.masterFloors, "M");
				}
			}
		}

		// Calculate total runs and secrets/run
		for (var entry : d.normalFloors.entrySet()) {
			d.totalNormalRuns += entry.getValue().completions;
		}
		for (var entry : d.masterFloors.entrySet()) {
			d.totalMasterRuns += entry.getValue().completions;
		}
		int totalRuns = d.totalNormalRuns + d.totalMasterRuns;
		d.secretsPerRun = totalRuns > 0 ? (double) d.secretsFound / totalRuns : 0.0;

		// Bestiary Parsing (Check member.bestiary, member.bestiary.kills, or member.player_stats.kills)
		JsonObject bestiaryObj = null;
		if (member.has("bestiary") && member.get("bestiary").isJsonObject()) {
			bestiaryObj = member.getAsJsonObject("bestiary");
			if (bestiaryObj.has("kills") && bestiaryObj.get("kills").isJsonObject()) {
				JsonObject k = bestiaryObj.getAsJsonObject("kills");
				for (String key : k.keySet()) {
					try {
						int kills = k.get(key).getAsInt();
						d.bestiaryMobKills.put(key, kills);
						d.bestiaryKills += kills;
					} catch (Exception ignored) {}
				}
			} else {
				for (String key : bestiaryObj.keySet()) {
					try {
						if (bestiaryObj.get(key).isJsonPrimitive()) {
							int kills = bestiaryObj.get(key).getAsInt();
							d.bestiaryMobKills.put(key, kills);
							d.bestiaryKills += kills;
						}
					} catch (Exception ignored) {}
				}
			}
		}

		if (d.bestiaryKills == 0 && member.has("player_stats") && member.getAsJsonObject("player_stats").has("kills")) {
			JsonObject pk = member.getAsJsonObject("player_stats").getAsJsonObject("kills");
			for (String key : pk.keySet()) {
				try {
					int kills = pk.get(key).getAsInt();
					d.bestiaryMobKills.put(key, kills);
					d.bestiaryKills += kills;
				} catch (Exception ignored) {}
			}
		}

		// Crimson Isle & Nether
		JsonObject nether = null;
		if (member.has("nether_island_player_data") && member.get("nether_island_player_data").isJsonObject()) {
			nether = member.getAsJsonObject("nether_island_player_data");
		} else if (member.has("crimson_isle") && member.get("crimson_isle").isJsonObject()) {
			nether = member.getAsJsonObject("crimson_isle");
		}

		if (nether != null) {
			if (nether.has("selected_faction") && nether.get("selected_faction").isJsonPrimitive()) {
				String f = nether.get("selected_faction").getAsString();
				d.crimsonFaction = "mages".equalsIgnoreCase(f) ? "Mages" : ("barbarians".equalsIgnoreCase(f) ? "Barbarians" : f);
			}
			if (nether.has("mage_reputation") && nether.get("mage_reputation").isJsonPrimitive()) {
				d.mageReputation = nether.get("mage_reputation").getAsInt();
			}
			if (nether.has("barbarians_reputation") && nether.get("barbarians_reputation").isJsonPrimitive()) {
				d.barbarianReputation = nether.get("barbarians_reputation").getAsInt();
			}
			if (nether.has("kuudra_completed_tiers") && nether.get("kuudra_completed_tiers").isJsonObject()) {
				JsonObject kc = nether.getAsJsonObject("kuudra_completed_tiers");
				for (String key : kc.keySet()) {
					d.kuudraCompletions.put(key.toLowerCase(), kc.get(key).getAsInt());
				}
			}
			if (nether.has("dojo") && nether.get("dojo").isJsonObject()) {
				JsonObject dojo = nether.getAsJsonObject("dojo");
				for (String key : dojo.keySet()) {
					if (dojo.get(key).isJsonPrimitive()) {
						d.dojoScores.put(key, dojo.get(key).getAsInt());
					}
				}
			}
		}

		return d;
	}

	private static void parseFloors(JsonObject typeObj, Map<String, FloorStats> targetMap, String prefix) {
		if (typeObj.has("tier_completions") && typeObj.get("tier_completions").isJsonObject()) {
			JsonObject tc = typeObj.getAsJsonObject("tier_completions");
			for (String fKey : tc.keySet()) {
				String key = prefix.equals("F") && fKey.equals("0") ? "F0" : (prefix + fKey);
				FloorStats fs = targetMap.computeIfAbsent(key, k -> new FloorStats());
				try { fs.completions = tc.get(fKey).getAsInt(); } catch (Exception ignored) {}
			}
		}
		if (typeObj.has("fastest_time") && typeObj.get("fastest_time").isJsonObject()) {
			JsonObject ft = typeObj.getAsJsonObject("fastest_time");
			for (String fKey : ft.keySet()) {
				String key = prefix.equals("F") && fKey.equals("0") ? "F0" : (prefix + fKey);
				FloorStats fs = targetMap.computeIfAbsent(key, k -> new FloorStats());
				try { fs.fastestTimeMs = (long) ft.get(fKey).getAsDouble(); } catch (Exception ignored) {}
			}
		}
		if (typeObj.has("fastest_time_s_plus") && typeObj.get("fastest_time_s_plus").isJsonObject()) {
			JsonObject fts = typeObj.getAsJsonObject("fastest_time_s_plus");
			for (String fKey : fts.keySet()) {
				String key = prefix.equals("F") && fKey.equals("0") ? "F0" : (prefix + fKey);
				FloorStats fs = targetMap.computeIfAbsent(key, k -> new FloorStats());
				try { fs.fastestTimeSplusMs = (long) fts.get(fKey).getAsDouble(); } catch (Exception ignored) {}
			}
		}
		if (typeObj.has("best_score") && typeObj.get("best_score").isJsonObject()) {
			JsonObject bs = typeObj.getAsJsonObject("best_score");
			for (String fKey : bs.keySet()) {
				String key = prefix.equals("F") && fKey.equals("0") ? "F0" : (prefix + fKey);
				FloorStats fs = targetMap.computeIfAbsent(key, k -> new FloorStats());
				try { fs.bestScore = bs.get(fKey).getAsInt(); } catch (Exception ignored) {}
			}
		}
	}
}