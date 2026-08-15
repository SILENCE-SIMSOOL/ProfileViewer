package silence.simsool.profileviewer.api.data;

import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonObject;

public class SkillsData {

	public static class SkillInfo {
		public String name;
		public int level;
		public int maxLevel;
		public double currentXp;
		public double nextLevelXp;
		public double totalXp;
		public float progress;

		public SkillInfo(String name, int maxLevel) {
			this.name = name;
			this.maxLevel = maxLevel;
		}
	}

	public Map<String, SkillInfo> skills = new LinkedHashMap<>();
	public double skillAverage = 0.0;

	public SkillsData() {
		initDefault();
	}

	private void initDefault() {
		skills.put("combat", new SkillInfo("Combat", 60));
		skills.put("mining", new SkillInfo("Mining", 60));
		skills.put("farming", new SkillInfo("Farming", 60));
		skills.put("foraging", new SkillInfo("Foraging", 50));
		skills.put("fishing", new SkillInfo("Fishing", 50));
		skills.put("enchanting", new SkillInfo("Enchanting", 60));
		skills.put("alchemy", new SkillInfo("Alchemy", 50));
		skills.put("taming", new SkillInfo("Taming", 60));
		skills.put("carpentry", new SkillInfo("Carpentry", 50));
		skills.put("runecrafting", new SkillInfo("Runecrafting", 25));
		skills.put("social", new SkillInfo("Social", 25));
	}

	public static SkillsData fromJson(JsonObject member) {
		SkillsData data = new SkillsData();
		if (member == null) return data;

		JsonObject playerData = member.has("player_data") && member.get("player_data").isJsonObject() ? member.getAsJsonObject("player_data") : null;
		JsonObject expObj = playerData != null && playerData.has("experience") && playerData.get("experience").isJsonObject() ? playerData.getAsJsonObject("experience") : null;

		double sumLevels = 0;
		int count = 0;

		for (var entry : data.skills.entrySet()) {
			String key = entry.getKey();
			SkillInfo info = entry.getValue();
			String apiKey = "SKILL_" + key.toUpperCase();

			double xp = 0;
			if (expObj != null && expObj.has(apiKey)) {
				xp = expObj.get(apiKey).getAsDouble();
			} else if (playerData != null && playerData.has("experience_" + key)) {
				xp = playerData.get("experience_" + key).getAsDouble();
			}

			calculateSkill(info, xp);
			if (!key.equals("carpentry") && !key.equals("runecrafting") && !key.equals("social")) {
				sumLevels += info.level + info.progress;
				count++;
			}
		}

		data.skillAverage = count > 0 ? (sumLevels / count) : 0.0;
		return data;
	}

	public static SkillsData fromSkyCrypt(JsonObject obj) {
		SkillsData data = new SkillsData();
		if (obj == null) return data;
		if (obj.has("averageSkillLevel")) data.skillAverage = obj.get("averageSkillLevel").getAsDouble();
		return data;
	}

	private static final double[] XP_TABLE = {
		0, 50, 175, 375, 675, 1175, 1925, 2925, 4425, 6425,
		9925, 14925, 22425, 32425, 47425, 67425, 97425, 147425, 222425, 322425,
		522425, 822425, 1222425, 1722425, 2322425, 3022425, 3822425, 4722425, 5722425, 6822425,
		8022425, 9322425, 10722425, 12222425, 13822425, 15522425, 17322425, 19222425, 21222425, 23322425,
		25522425, 27822425, 30222425, 32722425, 35322425, 38072425, 40972425, 44072425, 47472425, 51172425,
		55172425, 59472425, 64072425, 68972425, 74172425, 79672425, 85472425, 91572425, 97972425, 104672425
	};

	private static void calculateSkill(SkillInfo info, double xp) {
		info.totalXp = xp;
		int lvl = 0;
		for (int i = 1; i < XP_TABLE.length && i <= info.maxLevel; i++) {
			if (xp >= XP_TABLE[i]) {
				lvl = i;
			} else {
				break;
			}
		}
		info.level = lvl;
		if (lvl >= info.maxLevel || lvl >= XP_TABLE.length - 1) {
			info.progress = 1.0f;
			info.currentXp = 0;
			info.nextLevelXp = 0;
		} else {
			double base = XP_TABLE[lvl];
			double next = XP_TABLE[lvl + 1];
			double needed = next - base;
			double current = xp - base;
			info.currentXp = current;
			info.nextLevelXp = needed;
			info.progress = (float) Math.max(0.0, Math.min(1.0, current / needed));
		}
	}
}