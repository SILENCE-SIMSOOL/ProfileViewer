package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public class InventoryData {

	public List<ParsedItem> armor = new ArrayList<>();
	public List<ParsedItem> equipment = new ArrayList<>();
	public List<ParsedItem> inventory = new ArrayList<>();
	public List<ParsedItem> enderchest = new ArrayList<>();
	public List<ParsedItem> wardrobe = new ArrayList<>();
	public List<ParsedItem> accessoryBag = new ArrayList<>();
	public List<ParsedItem> potionBag = new ArrayList<>();
	public List<ParsedItem> fishingBag = new ArrayList<>();
	public List<ParsedItem> quiver = new ArrayList<>();
	public List<List<ParsedItem>> backpacks = new ArrayList<>();
	public Map<String, Integer> sacks = new LinkedHashMap<>();

	public static InventoryData fromJson(JsonObject member) {
		InventoryData data = new InventoryData();
		if (member == null) return data;

		JsonObject invObj = member.has("inventory") && member.get("inventory").isJsonObject() ? member.getAsJsonObject("inventory") : null;
		JsonObject sharedInv = member.has("shared_inventory") && member.get("shared_inventory").isJsonObject() ? member.getAsJsonObject("shared_inventory") : null;

		// 1. Armor & Equipment
		data.armor = parseNbtFromAnywhere(member, invObj, sharedInv, "inv_armor");
		data.equipment = parseNbtFromAnywhere(member, invObj, sharedInv, "equipment_contents");

		// 2. Main Inventory
		data.inventory = parseNbtFromAnywhere(member, invObj, sharedInv, "inv_contents");

		// 3. Ender Chest
		data.enderchest = parseNbtFromAnywhere(member, invObj, sharedInv, "ender_chest_contents");

		// 4. Wardrobe
		data.wardrobe = parseNbtFromAnywhere(member, invObj, sharedInv, "wardrobe_contents");
		if (data.wardrobe.isEmpty() && member.has("loadout") && member.get("loadout").isJsonObject()) {
			JsonObject loadout = member.getAsJsonObject("loadout");
			if (loadout.has("armor") && loadout.get("armor").isJsonObject()) {
				JsonObject armorObj = loadout.getAsJsonObject("armor");
				for (int i = 0; i < 18; i++) {
					String key = String.valueOf(i);
					if (armorObj.has(key) && armorObj.get(key).isJsonObject()) {
						JsonObject set = armorObj.getAsJsonObject(key);
						for (String slotKey : new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"}) {
							if (set.has(slotKey) && set.get(slotKey).isJsonObject() && set.getAsJsonObject(slotKey).has("data")) {
								List<ParsedItem> parsed = NbtItemParser.parseBase64Nbt(set.getAsJsonObject(slotKey).get("data").getAsString());
								data.wardrobe.addAll(parsed);
							} else {
								data.wardrobe.add(ParsedItem.EMPTY);
							}
						}
					}
				}
			}
		}

		// 5. Backpacks
		JsonObject bp = findJsonObject(member, invObj, sharedInv, "backpack_contents");
		if (bp != null) {
			for (Map.Entry<String, JsonElement> entry : bp.entrySet()) {
				if (entry.getValue().isJsonObject() && entry.getValue().getAsJsonObject().has("data")) {
					List<ParsedItem> bpItems = NbtItemParser.parseBase64Nbt(entry.getValue().getAsJsonObject().get("data").getAsString());
					if (!bpItems.isEmpty()) {
						data.backpacks.add(bpItems);
					}
				}
			}
		}

		// 6. Bags (Talisman, Potion, Fishing, Quiver)
		JsonObject bag = findJsonObject(member, invObj, sharedInv, "bag_contents");
		if (bag != null) {
			if (bag.has("talisman_bag") && bag.get("talisman_bag").isJsonObject()) {
				data.accessoryBag = NbtItemParser.parseBase64Nbt(bag.getAsJsonObject("talisman_bag").get("data").getAsString());
			}
			if (bag.has("potion_bag") && bag.get("potion_bag").isJsonObject()) {
				data.potionBag = NbtItemParser.parseBase64Nbt(bag.getAsJsonObject("potion_bag").get("data").getAsString());
			}
			if (bag.has("fishing_bag") && bag.get("fishing_bag").isJsonObject()) {
				data.fishingBag = NbtItemParser.parseBase64Nbt(bag.getAsJsonObject("fishing_bag").get("data").getAsString());
			}
			if (bag.has("quiver") && bag.get("quiver").isJsonObject()) {
				data.quiver = NbtItemParser.parseBase64Nbt(bag.getAsJsonObject("quiver").get("data").getAsString());
			}
		}

		// 7. Sacks
		JsonObject sc = findJsonObject(member, invObj, sharedInv, "sacks_counts");
		if (sc != null) {
			for (Map.Entry<String, JsonElement> entry : sc.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					data.sacks.put(entry.getKey(), entry.getValue().getAsInt());
				}
			}
		}

		return data;
	}

	private static List<ParsedItem> parseNbtFromAnywhere(JsonObject m, JsonObject inv, JsonObject shared, String key) {
		if (inv != null && inv.has(key) && inv.get(key).isJsonObject() && inv.getAsJsonObject(key).has("data")) {
			return NbtItemParser.parseBase64Nbt(inv.getAsJsonObject(key).get("data").getAsString());
		}
		if (m != null && m.has(key) && m.get(key).isJsonObject() && m.getAsJsonObject(key).has("data")) {
			return NbtItemParser.parseBase64Nbt(m.getAsJsonObject(key).get("data").getAsString());
		}
		if (shared != null && shared.has(key) && shared.get(key).isJsonObject() && shared.getAsJsonObject(key).has("data")) {
			return NbtItemParser.parseBase64Nbt(shared.getAsJsonObject(key).get("data").getAsString());
		}
		return new ArrayList<>();
	}

	private static JsonObject findJsonObject(JsonObject m, JsonObject inv, JsonObject shared, String key) {
		if (inv != null && inv.has(key) && inv.get(key).isJsonObject()) return inv.getAsJsonObject(key);
		if (m != null && m.has(key) && m.get(key).isJsonObject()) return m.getAsJsonObject(key);
		if (shared != null && shared.has(key) && shared.get(key).isJsonObject()) return shared.getAsJsonObject(key);
		return null;
	}
}