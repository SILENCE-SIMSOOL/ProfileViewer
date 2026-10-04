package silence.simsool.profileviewer.api.data;

import silence.simsool.profileviewer.api.nbt.ParsedItem;

public class NetworthData {

	public double total = 0;
	public double purse = 0;
	public double bank = 0;
	public double armor = 0;
	public double equipment = 0;
	public double wardrobe = 0;
	public double inventory = 0;
	public double enderchest = 0;
	public double pets = 0;
	public double talismans = 0;
	public double sacks = 0;
	public double museum = 0;
	public java.util.Map<String, Double> categories = new java.util.LinkedHashMap<>();

	public static NetworthData fromJson(com.google.gson.JsonObject json, double bankBalance) {
		NetworthData nw = new NetworthData();
		if (json == null) return nw;

		if (json.has("networth")) nw.total = json.get("networth").getAsDouble();
		else if (json.has("total")) nw.total = json.get("total").getAsDouble();

		if (json.has("purse")) nw.purse = json.get("purse").getAsDouble();
		if (json.has("bank")) nw.bank = json.get("bank").getAsDouble();
		else nw.bank = bankBalance;

		if (json.has("armor")) nw.armor = json.get("armor").getAsDouble();
		if (json.has("equipment")) nw.equipment = json.get("equipment").getAsDouble();
		if (json.has("wardrobe")) nw.wardrobe = json.get("wardrobe").getAsDouble();
		if (json.has("inventory")) nw.inventory = json.get("inventory").getAsDouble();
		if (json.has("enderchest")) nw.enderchest = json.get("enderchest").getAsDouble();
		if (json.has("pets")) nw.pets = json.get("pets").getAsDouble();
		if (json.has("accessories")) nw.talismans = json.get("accessories").getAsDouble();
		else if (json.has("talismans")) nw.talismans = json.get("talismans").getAsDouble();
		if (json.has("sacks")) nw.sacks = json.get("sacks").getAsDouble();
		if (json.has("museum")) nw.museum = json.get("museum").getAsDouble();

		if (json.has("categories") && json.get("categories").isJsonObject()) {
			com.google.gson.JsonObject catObj = json.getAsJsonObject("categories");
			for (var entry : catObj.entrySet()) {
				if (entry.getValue().isJsonPrimitive()) {
					nw.categories.put(entry.getKey(), entry.getValue().getAsDouble());
				}
			}
		}

		if (nw.categories.isEmpty()) {
			nw.categories.put("Armor", nw.armor);
			nw.categories.put("Equipment", nw.equipment);
			nw.categories.put("Wardrobe", nw.wardrobe);
			nw.categories.put("Inventory", nw.inventory);
			nw.categories.put("Ender Chest", nw.enderchest);
			nw.categories.put("Accessories", nw.talismans);
			nw.categories.put("Pets", nw.pets);
			nw.categories.put("Purse & Bank", nw.purse + nw.bank);
		}

		if (nw.total <= 0) {
			nw.total = nw.purse + nw.bank + nw.armor + nw.equipment + nw.wardrobe + nw.inventory + nw.enderchest + nw.pets + nw.talismans + nw.sacks;
		}

		return nw;
	}

	public static NetworthData calculate(MemberData member, double bankBalance) {
		NetworthData nw = new NetworthData();
		if (member == null) return nw;

		nw.purse = member.purse;
		nw.bank = bankBalance;

		if (member.inventory != null) {
			for (ParsedItem item : member.inventory.armor) {
				nw.armor += estimateItemValue(item);
			}
			for (ParsedItem item : member.inventory.equipment) {
				nw.equipment += estimateItemValue(item);
			}
			for (ParsedItem item : member.inventory.inventory) {
				nw.inventory += estimateItemValue(item);
			}
			for (ParsedItem item : member.inventory.wardrobe) {
				nw.wardrobe += estimateItemValue(item);
			}
			for (ParsedItem item : member.inventory.enderchest) {
				nw.enderchest += estimateItemValue(item);
			}
			for (ParsedItem item : member.inventory.accessoryBag) {
				nw.talismans += estimateAccessoryValue(item);
			}
			if (member.inventory.backpacks != null) {
				for (var bp : member.inventory.backpacks) {
					for (ParsedItem item : bp) {
						nw.inventory += estimateItemValue(item);
					}
				}
			}
			if (member.inventory.personalVault != null) {
				for (ParsedItem item : member.inventory.personalVault) {
					nw.enderchest += estimateItemValue(item);
				}
			}
		}

		if (member.pets != null && member.pets.pets != null) {
			for (PetData.PetItem pet : member.pets.pets) {
				nw.pets += estimatePetValue(pet);
			}
		}

		if (member.museum != null) {
			if (member.museum.totalValue > 0) {
				nw.museum = member.museum.totalValue;
			} else {
				if (member.museum.donatedItemsMap != null) {
					for (var list : member.museum.donatedItemsMap.values()) {
						if (list != null) {
							for (ParsedItem item : list) {
								nw.museum += estimateItemValue(item);
							}
						}
					}
				}
				if (member.museum.specialItems != null) {
					for (ParsedItem item : member.museum.specialItems) {
						nw.museum += estimateItemValue(item);
					}
				}
			}
		}

		nw.total = nw.purse + nw.bank + nw.armor + nw.equipment + nw.wardrobe + nw.inventory + nw.enderchest + nw.pets + nw.talismans + nw.sacks + nw.museum;

		nw.categories.put("Armor", nw.armor);
		nw.categories.put("Equipment", nw.equipment);
		nw.categories.put("Wardrobe", nw.wardrobe);
		nw.categories.put("Inventory", nw.inventory);
		nw.categories.put("Ender Chest", nw.enderchest);
		nw.categories.put("Accessories", nw.talismans);
		nw.categories.put("Pets", nw.pets);
		if (nw.museum > 0) nw.categories.put("Museum", nw.museum);
		nw.categories.put("Purse & Bank", nw.purse + nw.bank);

		return nw;
	}

	private static double estimateAccessoryValue(ParsedItem item) {
		if (item == null || item.isEmpty()) return 0;
		double base = switch (item.rarity.toUpperCase()) {
			case "VERY SPECIAL", "SPECIAL" -> 450_000_000.0;
			case "DIVINE" -> 250_000_000.0;
			case "MYTHIC" -> 90_000_000.0;
			case "LEGENDARY" -> 35_000_000.0;
			case "EPIC" -> 8_000_000.0;
			case "RARE" -> 2_500_000.0;
			case "UNCOMMON" -> 800_000.0;
			default -> 150_000.0;
		};

		String id = item.skyblockId.toUpperCase();
		if (id.contains("HEGEMONY")) base = 1_200_000_000.0;
		if (id.contains("PANDORA")) base = 600_000_000.0;
		if (id.contains("MASTER_SKULL_TIER_7")) base = 800_000_000.0;
		if (id.contains("RELIC_OF_POWER") || id.contains("ARTIFACT_OF_POWER")) base += 120_000_000.0;

		for (String line : item.lore) {
			if (line.contains("Recombobulated")) base += 12_000_000.0;
			if (line.contains("Perfect")) base += 25_000_000.0;
		}

		item.estimatedValue = base;
		return item.estimatedValue;
	}

	private static double estimateItemValue(ParsedItem item) {
		if (item == null || item.isEmpty()) return 0;
		double base = switch (item.rarity.toUpperCase()) {
			case "DIVINE", "SPECIAL", "VERY SPECIAL" -> 150_000_000.0;
			case "MYTHIC" -> 60_000_000.0;
			case "LEGENDARY" -> 25_000_000.0;
			case "EPIC" -> 8_000_000.0;
			case "RARE" -> 1_500_000.0;
			case "UNCOMMON" -> 300_000.0;
			default -> 50_000.0;
		};

		String sbId = item.skyblockId.toUpperCase();
		if (sbId.contains("HYPERION") || sbId.contains("ASTRAEA") || sbId.contains("SCYLLA") || sbId.contains("VALKYRIE") || sbId.contains("NECRON_BLADE")) {
			base = 2_200_000_000.0;
		} else if (sbId.contains("TERMINATOR")) {
			base = 1_400_000_000.0;
		} else if (sbId.contains("DARK_CLAYMORE")) {
			base = 450_000_000.0;
		} else if (sbId.contains("DIVAN_HELMET") || sbId.contains("DIVAN_CHESTPLATE") || sbId.contains("DIVAN_LEGGINGS") || sbId.contains("DIVAN_BOOTS")) {
			base = 200_000_000.0;
		} else if (sbId.contains("DIVAN_DRILL")) {
			base = 1_500_000_000.0;
		} else if (sbId.contains("TITANIUM_DRILL_655") || sbId.contains("655")) {
			base = 350_000_000.0;
		} else if (sbId.contains("NECRON") || sbId.contains("STORM") || sbId.contains("MAXOR") || sbId.contains("GOLDOR")) {
			base = 120_000_000.0;
		} else if (sbId.contains("CRIMSON") || sbId.contains("AURORA") || sbId.contains("TERROR") || sbId.contains("FERMENTO")) {
			base = 150_000_000.0;
		}

		for (String line : item.lore) {
			if (line.contains("✪")) base += 10_000_000.0;
			if (line.contains("➊") || line.contains("➋") || line.contains("➌") || line.contains("➍") || line.contains("➎")) base += 45_000_000.0;
			if (line.contains("Recombobulated")) base += 12_000_000.0;
			if (line.contains("Chimera")) base += 250_000_000.0;
			if (line.contains("Legion V") || line.contains("Fatal Tempo")) base += 70_000_000.0;
			if (line.contains("Overload V") || line.contains("Soul Eater V") || line.contains("Duplex")) base += 35_000_000.0;
			if (line.contains("Perfect") && line.contains("Gem")) base += 30_000_000.0;
			if (line.contains("Flawless") && line.contains("Gem")) base += 6_000_000.0;
		}

		item.estimatedValue = base * item.count;
		return item.estimatedValue;
	}

	private static double estimatePetValue(PetData.PetItem pet) {
		if (pet == null) return 0;
		double base = switch (pet.rarity.toUpperCase()) {
			case "MYTHIC" -> 120_000_000.0;
			case "LEGENDARY" -> 45_000_000.0;
			case "EPIC" -> 12_000_000.0;
			case "RARE" -> 3_000_000.0;
			case "UNCOMMON" -> 800_000.0;
			default -> 200_000.0;
		};

		String type = pet.type.toUpperCase();
		if (type.contains("GOLDEN_DRAGON")) base = 1_800_000_000.0;
		else if (type.contains("ENDER_DRAGON")) base = 900_000_000.0;
		else if (type.contains("SCATHA")) base = 500_000_000.0;
		else if (type.contains("KUUDRA")) base = 400_000_000.0;
		else if (type.contains("GRIFFIN") && "MYTHIC".equalsIgnoreCase(pet.rarity)) base = 250_000_000.0;
		else if (type.contains("BABY_YETI") || type.contains("BLACK_CAT") || type.contains("MITHRIL_GOLEM") || type.contains("SHEEP") || type.contains("WOLF")) base += 30_000_000.0;

		if (pet.heldItem != null && !pet.heldItem.isEmpty()) {
			if (pet.heldItem.contains("TIER_BOOST")) base += 80_000_000.0;
			if (pet.heldItem.contains("MINOS_RELIC")) base += 140_000_000.0;
			if (pet.heldItem.contains("CLOVER") || pet.heldItem.contains("EXP_SHARE")) base += 20_000_000.0;
		}

		return base * (0.4 + (pet.level / 150.0));
	}
}