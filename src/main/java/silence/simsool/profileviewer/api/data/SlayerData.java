package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonObject;

public class SlayerData {

	public static class SlayerBoss {
		public String name;
		public int level = 0;
		public double totalXp = 0;
		public double currentLevelXp = 0;
		public double nextLevelXp = 0;
		public float progress = 0f;
		public boolean maxed = false;
		public int[] tierKills = new int[5];

		public SlayerBoss(String name) {
			this.name = name;
		}
	}

	public Map<String, SlayerBoss> bosses = new LinkedHashMap<>();
	public double totalSlayerXp = 0;

	public SlayerData() {
		bosses.put("zombie", new SlayerBoss("Revenant Horror"));
		bosses.put("spider", new SlayerBoss("Tarantula Broodfather"));
		bosses.put("wolf", new SlayerBoss("Sven Packmaster"));
		bosses.put("enderman", new SlayerBoss("Voidgloom Seraph"));
		bosses.put("blaze", new SlayerBoss("Inferno Demonlord"));
		bosses.put("vampire", new SlayerBoss("Riftstalker Bloodfiend"));
	}

	public static SlayerData fromJson(JsonObject member) {
		SlayerData data = new SlayerData();
		if (member == null) return data;

		JsonObject slayerObj = member.has("slayer") && member.get("slayer").isJsonObject()
				? member.getAsJsonObject("slayer")
				: (member.has("slayer_bosses") ? member : null);

		if (slayerObj != null && slayerObj.has("slayer_bosses") && slayerObj.get("slayer_bosses").isJsonObject()) {
			JsonObject bossesObj = slayerObj.getAsJsonObject("slayer_bosses");
			for (var entry : data.bosses.entrySet()) {
				String key = entry.getKey();
				SlayerBoss b = entry.getValue();
				if (bossesObj.has(key) && bossesObj.get(key).isJsonObject()) {
					JsonObject bo = bossesObj.getAsJsonObject(key);
					if (bo.has("xp") && bo.get("xp").isJsonPrimitive()) {
						b.totalXp = bo.get("xp").getAsDouble();
					}
					calcSlayerProgress(b, key);
					data.totalSlayerXp += b.totalXp;

					for (int t = 0; t < 5; t++) {
						String tk1 = "boss_kills_tier_" + t;
						String tk2 = "boss_kills_tier_" + (t + 1);
						if (bo.has(tk1) && bo.get(tk1).isJsonPrimitive()) {
							b.tierKills[t] = bo.get(tk1).getAsInt();
						} else if (bo.has(tk2) && bo.get(tk2).isJsonPrimitive()) {
							b.tierKills[t] = bo.get(tk2).getAsInt();
						}
					}
				}
			}
		}

		return data;
	}

	public static SlayerData fromSkyCrypt(JsonObject obj) {
		return new SlayerData();
	}

	private static final int[] STANDARD_XP = {0, 5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000};
	private static final int[] ENDERMAN_BLAZE_XP = {0, 10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000};
	private static final int[] VAMPIRE_XP = {0, 20, 75, 240, 840, 2400};

	private static void calcSlayerProgress(SlayerBoss b, String boss) {
		int[] xpTable = switch (boss.toLowerCase()) {
			case "enderman", "blaze" -> ENDERMAN_BLAZE_XP;
			case "vampire" -> VAMPIRE_XP;
			default -> STANDARD_XP;
		};

		int lvl = 0;
		for (int i = xpTable.length - 1; i >= 0; i--) {
			if (b.totalXp >= xpTable[i]) {
				lvl = i;
				break;
			}
		}
		b.level = lvl;

		if (lvl >= xpTable.length - 1) {
			b.maxed = true;
			b.progress = 1.0f;
			b.currentLevelXp = b.totalXp;
			b.nextLevelXp = xpTable[xpTable.length - 1];
		} else {
			b.maxed = false;
			int currentReq = xpTable[lvl];
			int nextReq = xpTable[lvl + 1];
			b.currentLevelXp = b.totalXp - currentReq;
			b.nextLevelXp = nextReq - currentReq;
			b.progress = (float) Math.max(0.0, Math.min(1.0, b.currentLevelXp / b.nextLevelXp));
		}
	}
}