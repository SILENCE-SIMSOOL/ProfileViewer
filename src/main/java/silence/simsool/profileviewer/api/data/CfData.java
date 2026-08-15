package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CfData {

	public long chocolate = 0;
	public long totalChocolate = 0;
	public long chocolateSincePrestige = 0;
	public int prestigeLevel = 0;
	public int barnCapacityLevel = 0;
	public int clickUpgrades = 0;
	public int chocolateMultiplierUpgrades = 0;
	public int rabbitRarityUpgrades = 0;

	public Map<String, Integer> rabbits = new LinkedHashMap<>();
	public List<RabbitEmployee> employees = new ArrayList<>();

	public static class RabbitEmployee {
		public final String id;
		public final int level;

		public RabbitEmployee(String id, int level) {
			this.id = id;
			this.level = level;
		}
	}

	public static CfData fromJson(JsonObject member) {
		CfData data = new CfData();
		if (member == null) return data;

		JsonObject events = member.has("events") && member.get("events").isJsonObject() ? member.getAsJsonObject("events") : null;
		JsonObject cf = null;
		if (events != null && events.has("easter") && events.get("easter").isJsonObject()) {
			cf = events.getAsJsonObject("easter");
		}
		if (cf == null && member.has("chocolate_factory") && member.get("chocolate_factory").isJsonObject()) {
			cf = member.getAsJsonObject("chocolate_factory");
		}

		if (cf != null) {
			if (cf.has("chocolate")) data.chocolate = cf.get("chocolate").getAsLong();
			if (cf.has("total_chocolate")) data.totalChocolate = cf.get("total_chocolate").getAsLong();
			if (cf.has("chocolate_since_prestige")) data.chocolateSincePrestige = cf.get("chocolate_since_prestige").getAsLong();
			if (cf.has("chocolate_level")) data.prestigeLevel = cf.get("chocolate_level").getAsInt();
			if (cf.has("rabbit_barn_capacity_level")) data.barnCapacityLevel = cf.get("rabbit_barn_capacity_level").getAsInt();
			if (cf.has("click_upgrades")) data.clickUpgrades = cf.get("click_upgrades").getAsInt();
			if (cf.has("chocolate_multiplier_upgrades")) data.chocolateMultiplierUpgrades = cf.get("chocolate_multiplier_upgrades").getAsInt();
			if (cf.has("rabbit_rarity_upgrades")) data.rabbitRarityUpgrades = cf.get("rabbit_rarity_upgrades").getAsInt();

			if (cf.has("employees") && cf.get("employees").isJsonObject()) {
				JsonObject emp = cf.getAsJsonObject("employees");
				for (Map.Entry<String, JsonElement> e : emp.entrySet()) {
					if (e.getValue().isJsonPrimitive()) {
						data.employees.add(new RabbitEmployee(e.getKey(), e.getValue().getAsInt()));
					}
				}
			}

			if (cf.has("rabbits") && cf.get("rabbits").isJsonObject()) {
				JsonObject r = cf.getAsJsonObject("rabbits");
				for (Map.Entry<String, JsonElement> e : r.entrySet()) {
					if (e.getValue() instanceof JsonPrimitive && ((JsonPrimitive) e.getValue()).isNumber()) {
						data.rabbits.put(e.getKey(), e.getValue().getAsInt());
					}
				}
			}
		}

		return data;
	}
}