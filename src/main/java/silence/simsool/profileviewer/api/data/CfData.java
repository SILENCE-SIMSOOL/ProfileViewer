package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CfData {
	public static final JsonObject CATALOG = ProfileJson.catalog("chocolate_catalog");
	private static final Map<String, String> RABBIT_RARITIES = rabbitRarities();
	public boolean available;
	public int timeTowerLevel;
	public int timeTowerCharges;
	public int hitmanSlots;
	public int uncollectedEggs;
	public String faction = "";
	public int factionLevel;

	private static Map<String, String> rabbitRarities() {
		Map<String, String> result = new LinkedHashMap<>();
		for (var tier : ProfileJson.object(CATALOG, "rabbits").entrySet()) {
			for (var rabbit : tier.getValue().getAsJsonArray()) result.put(rabbit.getAsString(), tier.getKey());
		}
		return result;
	}

	public static String employeeName(String id) {
		for (var entry : ProfileJson.array(CATALOG, "employees")) {
			var employee = entry.getAsJsonObject();
			if (id.equals(ProfileJson.string(employee, "id"))) return ProfileJson.string(employee, "name");
		}
		return id;
	}

	public static String rabbitRarity(String id) {
		return RABBIT_RARITIES.getOrDefault(id, "");
	}

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
			data.available = true;
			data.timeTowerLevel = (int) ProfileJson.number(cf, "time_tower", "level");
			data.timeTowerCharges = (int) ProfileJson.number(cf, "time_tower", "charges");
			data.hitmanSlots = (int) ProfileJson.number(cf, "rabbit_hitmen", "rabbit_hitmen_slots");
			data.uncollectedEggs = (int) ProfileJson.number(cf, "rabbit_hitmen", "missed_uncollected_eggs");
			data.faction = ProfileJson.string(cf, "rabbits", "selected_faction");
			data.factionLevel = (int) ProfileJson.number(cf, "rabbits", "faction_level");
			if (cf.has("chocolate")) data.chocolate = cf.get("chocolate").getAsLong();
			if (cf.has("total_chocolate")) data.totalChocolate = cf.get("total_chocolate").getAsLong();
			if (cf.has("chocolate_since_prestige")) data.chocolateSincePrestige = cf.get("chocolate_since_prestige").getAsLong();
			if (cf.has("chocolate_level")) data.prestigeLevel = cf.get("chocolate_level").getAsInt();
			if (cf.has("rabbit_barn_capacity_level")) data.barnCapacityLevel = cf.get("rabbit_barn_capacity_level").getAsInt();
			if (cf.has("click_upgrades")) data.clickUpgrades = cf.get("click_upgrades").getAsInt();
			if (cf.has("chocolate_multiplier_upgrades")) data.chocolateMultiplierUpgrades = cf.get("chocolate_multiplier_upgrades").getAsInt();
			if (cf.has("rabbit_rarity_upgrades")) data.rabbitRarityUpgrades = cf.get("rabbit_rarity_upgrades").getAsInt();

			JsonObject employees = ProfileJson.object(cf, "employees");
			for (JsonElement entry : ProfileJson.array(CATALOG, "employees")) {
				String id = entry.getAsJsonObject().get("id").getAsString();
				data.employees.add(new RabbitEmployee(id, (int) ProfileJson.number(employees, id)));
			}

			if (cf.has("rabbits") && cf.get("rabbits").isJsonObject()) {
				JsonObject r = cf.getAsJsonObject("rabbits");
				for (Map.Entry<String, JsonElement> e : r.entrySet()) {
					if (e.getValue() instanceof JsonPrimitive && ((JsonPrimitive) e.getValue()).isNumber()) {
						if (RABBIT_RARITIES.containsKey(e.getKey()) && e.getValue().getAsInt() > 0) data.rabbits.put(e.getKey(), e.getValue().getAsInt());
					}
				}
			}
		}

		return data;
	}
}