package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public class InventoryData {

	public boolean available;
	public List<ParsedItem> personalVault = new ArrayList<>();
	public List<ParsedItem> candyBag = new ArrayList<>();
	public List<ParsedItem> carnivalMaskBag = new ArrayList<>();
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

	public LoadoutData loadouts = new LoadoutData();
	public MaxwellData maxwell = new MaxwellData();

	public static class ArmorSet {
		public int id;
		public ParsedItem helmet = ParsedItem.EMPTY;
		public ParsedItem chestplate = ParsedItem.EMPTY;
		public ParsedItem leggings = ParsedItem.EMPTY;
		public ParsedItem boots = ParsedItem.EMPTY;

		public List<ParsedItem> getStacks() {
			List<ParsedItem> list = new ArrayList<>(4);
			list.add(helmet != null ? helmet : ParsedItem.EMPTY);
			list.add(chestplate != null ? chestplate : ParsedItem.EMPTY);
			list.add(leggings != null ? leggings : ParsedItem.EMPTY);
			list.add(boots != null ? boots : ParsedItem.EMPTY);
			return list;
		}
	}

	public static class EquipmentSet {
		public int id;
		public ParsedItem slot1 = ParsedItem.EMPTY;
		public ParsedItem slot2 = ParsedItem.EMPTY;
		public ParsedItem slot3 = ParsedItem.EMPTY;
		public ParsedItem slot4 = ParsedItem.EMPTY;

		public List<ParsedItem> getStacks() {
			List<ParsedItem> list = new ArrayList<>(4);
			list.add(slot1 != null ? slot1 : ParsedItem.EMPTY);
			list.add(slot2 != null ? slot2 : ParsedItem.EMPTY);
			list.add(slot3 != null ? slot3 : ParsedItem.EMPTY);
			list.add(slot4 != null ? slot4 : ParsedItem.EMPTY);
			return list;
		}
	}

	public static class SavedLoadout {
		public int id;
		public String name = "";
		public Integer armorSetId = null;
		public Integer equipmentSlotId = null;
		public Integer miningCoreSelectedSlot = null;
		public Integer foragingCoreSelectedSlot = null;
		public String powerStone = null;
		public Integer tuningPointsSlot = null;
		public String petUuid = null;

		public boolean isEmpty() {
			return armorSetId == null && equipmentSlotId == null && miningCoreSelectedSlot == null
					&& foragingCoreSelectedSlot == null && powerStone == null && tuningPointsSlot == null && petUuid == null
					&& (name == null || name.isEmpty() || name.equals("Loadout " + id) || name.equals("Template " + id));
		}
	}

	public static class LoadoutData {
		public int equippedArmorSet = -1;
		public Map<Integer, ArmorSet> armorSets = new LinkedHashMap<>();
		public int equippedEquipmentSet = -1;
		public Map<Integer, EquipmentSet> equipmentSets = new LinkedHashMap<>();
		public Map<Integer, SavedLoadout> savedLoadouts = new LinkedHashMap<>();
	}

	public static class MaxwellData {
		public String selectedPower = "";
		public int highestMp = 0;
		public int bagUpgrades = 0;
		public boolean consumedRiftPrism = false;
		public int abiphoneContacts = 0;
		public Map<String, Integer> tunings = new LinkedHashMap<>();
	}

	public static InventoryData fromJson(JsonObject member) {
		InventoryData data = new InventoryData();
		if (member == null) return data;

		JsonObject invObj = member.has("inventory") && member.get("inventory").isJsonObject() ? member.getAsJsonObject("inventory") : null;
		JsonObject sharedInv = member.has("shared_inventory") && member.get("shared_inventory").isJsonObject() ? member.getAsJsonObject("shared_inventory") : null;

		data.available = invObj != null || member.has("inv_contents");
		data.personalVault = parseNbtFromAnywhere(member, invObj, sharedInv, "personal_vault_contents");
		data.candyBag = parseNbtFromAnywhere(member, invObj, sharedInv, "candy_inventory_contents");
		data.carnivalMaskBag = parseNbtFromAnywhere(member, invObj, sharedInv, "carnival_mask_inventory_contents");

		// 1. Armor & Equipment
		data.armor = parseNbtFromAnywhere(member, invObj, sharedInv, "inv_armor");
		data.equipment = parseNbtFromAnywhere(member, invObj, sharedInv, "equipment_contents");

		// 2. Main Inventory
		data.inventory = parseNbtFromAnywhere(member, invObj, sharedInv, "inv_contents");

		// 3. Ender Chest
		data.enderchest = parseNbtFromAnywhere(member, invObj, sharedInv, "ender_chest_contents");

		// 4. Wardrobe & Loadouts
		parseLoadoutData(member, data);

		data.wardrobe = parseNbtFromAnywhere(member, invObj, sharedInv, "wardrobe_contents");
		if (data.wardrobe.isEmpty() && !data.loadouts.armorSets.isEmpty()) {
			int maxSet = 0;
			for (int k : data.loadouts.armorSets.keySet()) {
				if (k > maxSet) maxSet = k;
			}
			int totalSets = Math.max(maxSet, 18);
			for (int i = 1; i <= totalSets; i++) {
				ArmorSet as = data.loadouts.armorSets.get(i);
				if (as != null) {
					data.wardrobe.add(as.helmet != null ? as.helmet : ParsedItem.EMPTY);
					data.wardrobe.add(as.chestplate != null ? as.chestplate : ParsedItem.EMPTY);
					data.wardrobe.add(as.leggings != null ? as.leggings : ParsedItem.EMPTY);
					data.wardrobe.add(as.boots != null ? as.boots : ParsedItem.EMPTY);
				} else {
					data.wardrobe.add(ParsedItem.EMPTY);
					data.wardrobe.add(ParsedItem.EMPTY);
					data.wardrobe.add(ParsedItem.EMPTY);
					data.wardrobe.add(ParsedItem.EMPTY);
				}
			}
		}

		// 5. Backpacks
		JsonObject bp = findJsonObject(member, invObj, sharedInv, "backpack_contents");
		if (bp != null) {
			for (Map.Entry<String, JsonElement> entry : bp.entrySet().stream().sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey()))).toList()) {
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
		data.accessoryBag = parseNbtFromAnywhere(bag, null, null, "talisman_bag");
		data.potionBag = parseNbtFromAnywhere(bag, null, null, "potion_bag");
		data.fishingBag = parseNbtFromAnywhere(bag, null, null, "fishing_bag");
		data.quiver = parseNbtFromAnywhere(bag, null, null, "quiver");

		// 7. Sacks
		JsonObject sc = findJsonObject(member, invObj, sharedInv, "sacks_counts");
		if (sc != null) {
			for (Map.Entry<String, JsonElement> entry : sc.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					data.sacks.put(entry.getKey(), entry.getValue().getAsInt());
				}
			}
		}

		// 8. Maxwell & Accessory Bag Storage
		parseMaxwellData(member, data);

		return data;
	}

	private static void parseLoadoutData(JsonObject member, InventoryData data) {
		if (member == null || !member.has("loadout") || !member.get("loadout").isJsonObject()) return;
		JsonObject lo = member.getAsJsonObject("loadout");

		// Armor Sets
		if (lo.has("armor") && lo.get("armor").isJsonObject()) {
			JsonObject arm = lo.getAsJsonObject("armor");
			if (arm.has("equipped_set") && arm.get("equipped_set").isJsonPrimitive()) {
				data.loadouts.equippedArmorSet = arm.get("equipped_set").getAsInt();
			}
			for (Map.Entry<String, JsonElement> entry : arm.entrySet()) {
				if (entry.getKey().equals("equipped_set") || !entry.getValue().isJsonObject()) continue;
				try {
					int keyId = Integer.parseInt(entry.getKey());
					JsonObject setObj = entry.getValue().getAsJsonObject();
					ArmorSet set = new ArmorSet();
					set.id = setObj.has("id") ? setObj.get("id").getAsInt() : keyId;
					set.helmet = parseSlotItem(setObj, "HELMET");
					set.chestplate = parseSlotItem(setObj, "CHESTPLATE");
					set.leggings = parseSlotItem(setObj, "LEGGINGS");
					set.boots = parseSlotItem(setObj, "BOOTS");
					data.loadouts.armorSets.put(keyId, set);
				} catch (Exception ignored) {}
			}
		}

		// Equipment Sets
		if (lo.has("equipment") && lo.get("equipment").isJsonObject()) {
			JsonObject eq = lo.getAsJsonObject("equipment");
			if (eq.has("equipped_set") && eq.get("equipped_set").isJsonPrimitive()) {
				data.loadouts.equippedEquipmentSet = eq.get("equipped_set").getAsInt();
			}
			for (Map.Entry<String, JsonElement> entry : eq.entrySet()) {
				if (entry.getKey().equals("equipped_set") || !entry.getValue().isJsonObject()) continue;
				try {
					int keyId = Integer.parseInt(entry.getKey());
					JsonObject setObj = entry.getValue().getAsJsonObject();
					EquipmentSet set = new EquipmentSet();
					set.id = setObj.has("id") ? setObj.get("id").getAsInt() : keyId;
					set.slot1 = parseSlotItem(setObj, "EQUIPMENT_SLOT_1");
					set.slot2 = parseSlotItem(setObj, "EQUIPMENT_SLOT_2");
					set.slot3 = parseSlotItem(setObj, "EQUIPMENT_SLOT_3");
					set.slot4 = parseSlotItem(setObj, "EQUIPMENT_SLOT_4");
					data.loadouts.equipmentSets.put(keyId, set);
				} catch (Exception ignored) {}
			}
		}

		// Saved Loadouts
		if (lo.has("loadouts") && lo.get("loadouts").isJsonObject()) {
			JsonObject saved = lo.getAsJsonObject("loadouts");
			for (Map.Entry<String, JsonElement> entry : saved.entrySet()) {
				if (!entry.getValue().isJsonObject()) continue;
				try {
					int keyId = Integer.parseInt(entry.getKey());
					JsonObject slObj = entry.getValue().getAsJsonObject();
					SavedLoadout sl = new SavedLoadout();
					sl.id = slObj.has("id") ? slObj.get("id").getAsInt() : keyId;
					sl.name = slObj.has("name") ? slObj.get("name").getAsString() : ("Loadout " + sl.id);
					if (slObj.has("armor_set_id") && !slObj.get("armor_set_id").isJsonNull()) sl.armorSetId = slObj.get("armor_set_id").getAsInt();
					if (slObj.has("equipment_set_id") && !slObj.get("equipment_set_id").isJsonNull()) sl.equipmentSlotId = slObj.get("equipment_set_id").getAsInt();
					if (slObj.has("mining_core_selected_slot") && !slObj.get("mining_core_selected_slot").isJsonNull()) sl.miningCoreSelectedSlot = slObj.get("mining_core_selected_slot").getAsInt();
					if (slObj.has("foraging_core_selected_slot") && !slObj.get("foraging_core_selected_slot").isJsonNull()) sl.foragingCoreSelectedSlot = slObj.get("foraging_core_selected_slot").getAsInt();
					if (slObj.has("power_stone") && !slObj.get("power_stone").isJsonNull()) sl.powerStone = slObj.get("power_stone").getAsString();
					if (slObj.has("tuning_points_slot") && !slObj.get("tuning_points_slot").isJsonNull()) sl.tuningPointsSlot = slObj.get("tuning_points_slot").getAsInt();
					if (slObj.has("pet") && !slObj.get("pet").isJsonNull()) sl.petUuid = slObj.get("pet").getAsString();
					data.loadouts.savedLoadouts.put(keyId, sl);
				} catch (Exception ignored) {}
			}
		}
	}

	private static ParsedItem parseSlotItem(JsonObject parent, String key) {
		if (parent == null || !parent.has(key) || !parent.get(key).isJsonObject()) return ParsedItem.EMPTY;
		JsonObject obj = parent.getAsJsonObject(key);
		if (obj.has("data") && obj.get("data").isJsonPrimitive()) {
			List<ParsedItem> items = NbtItemParser.parseBase64Nbt(obj.get("data").getAsString());
			return (items != null && !items.isEmpty()) ? items.get(0) : ParsedItem.EMPTY;
		}
		return ParsedItem.EMPTY;
	}

	private static void parseMaxwellData(JsonObject member, InventoryData data) {
		if (member == null) return;
		if (member.has("accessory_bag_storage") && member.get("accessory_bag_storage").isJsonObject()) {
			JsonObject abs = member.getAsJsonObject("accessory_bag_storage");
			if (abs.has("selected_power") && abs.get("selected_power").isJsonPrimitive()) {
				data.maxwell.selectedPower = abs.get("selected_power").getAsString();
			}
			if (abs.has("highest_magical_power") && abs.get("highest_magical_power").isJsonPrimitive()) {
				data.maxwell.highestMp = abs.get("highest_magical_power").getAsInt();
			}
			if (abs.has("bag_upgrades_purchased") && abs.get("bag_upgrades_purchased").isJsonPrimitive()) {
				data.maxwell.bagUpgrades = abs.get("bag_upgrades_purchased").getAsInt();
			}
			if (abs.has("tuning") && abs.get("tuning").isJsonObject()) {
				JsonObject tun = abs.getAsJsonObject("tuning");
				if (tun.has("slot_0") && tun.get("slot_0").isJsonObject()) {
					for (var e : tun.getAsJsonObject("slot_0").entrySet()) {
						if (e.getValue().isJsonPrimitive()) {
							data.maxwell.tunings.put(e.getKey(), e.getValue().getAsInt());
						}
					}
				}
			}
		}

		if (member.has("rift") && member.get("rift").isJsonObject()) {
			JsonObject rift = member.getAsJsonObject("rift");
			if (rift.has("access") && rift.get("access").isJsonObject()) {
				JsonObject acc = rift.getAsJsonObject("access");
				if (acc.has("consumed_prism") && acc.get("consumed_prism").isJsonPrimitive()) {
					data.maxwell.consumedRiftPrism = acc.get("consumed_prism").getAsBoolean();
				}
			}
		}

		if (member.has("nether_island_player_data") && member.get("nether_island_player_data").isJsonObject()) {
			JsonObject nipd = member.getAsJsonObject("nether_island_player_data");
			if (nipd.has("abiphone") && nipd.get("abiphone").isJsonObject()) {
				JsonObject abi = nipd.getAsJsonObject("abiphone");
				if (abi.has("active_contacts") && abi.get("active_contacts").isJsonArray()) {
					data.maxwell.abiphoneContacts = abi.getAsJsonArray("active_contacts").size();
				}
			}
		}
	}

	private static List<ParsedItem> parseNbtFromAnywhere(JsonObject m, JsonObject inv, JsonObject shared, String key) {
		if (inv != null && inv.has(key) && inv.get(key).isJsonObject() && inv.getAsJsonObject(key).has("data") && inv.getAsJsonObject(key).get("data").isJsonPrimitive()) {
			return NbtItemParser.parseBase64Nbt(inv.getAsJsonObject(key).get("data").getAsString());
		}
		if (m != null && m.has(key) && m.get(key).isJsonObject() && m.getAsJsonObject(key).has("data") && m.getAsJsonObject(key).get("data").isJsonPrimitive()) {
			return NbtItemParser.parseBase64Nbt(m.getAsJsonObject(key).get("data").getAsString());
		}
		if (shared != null && shared.has(key) && shared.get(key).isJsonObject() && shared.getAsJsonObject(key).has("data") && shared.getAsJsonObject(key).get("data").isJsonPrimitive()) {
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