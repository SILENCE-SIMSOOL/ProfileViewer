package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.world.item.ItemStack;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public final class GearFinder {

	// =========================================================================
	// 1. FARMING GEAR IDS
	// =========================================================================
	public static final Set<String> FARMING_HELMETS = Set.of(
		"FERMENTO_HELMET", "SQUASH_HELMET", "CROUPIER_HELMET", "MELON_HELMET", "PUMPKIN_HELMET", "FARM_ARMOR_HELMET", "RANCHERS_BOOTS"
	);
	public static final Set<String> FARMING_CHESTPLATES = Set.of(
		"FERMENTO_CHESTPLATE", "SQUASH_CHESTPLATE", "CROUPIER_CHESTPLATE", "MELON_CHESTPLATE", "PUMPKIN_CHESTPLATE", "FARM_ARMOR_CHESTPLATE"
	);
	public static final Set<String> FARMING_LEGGINGS = Set.of(
		"FERMENTO_LEGGINGS", "SQUASH_LEGGINGS", "CROUPIER_LEGGINGS", "MELON_LEGGINGS", "PUMPKIN_LEGGINGS", "FARM_ARMOR_LEGGINGS"
	);
	public static final Set<String> FARMING_BOOTS = Set.of(
		"FERMENTO_BOOTS", "SQUASH_BOOTS", "CROUPIER_BOOTS", "MELON_BOOTS", "PUMPKIN_BOOTS", "FARM_ARMOR_BOOTS", "RANCHERS_BOOTS", "RANCHER_BOOTS", "FARMER_BOOTS"
	);
	public static final Set<String> FARMING_EQUIPMENT = Set.of(
		"LOTUS_NECKLACE", "LOTUS_CLOAK", "LOTUS_BELT", "LOTUS_BRACELET", "PEST_VEST", "PESTERMINATOR_CLOAK", "DELICATE_BRACELET"
	);

	// =========================================================================
	// 2. MINING GEAR IDS
	// =========================================================================
	public static final Set<String> MINING_HELMETS = Set.of(
		"DIVAN_HELMET", "ARMOR_OF_DIVAN_HELMET", "SORROW_HELMET", "GLACITE_HELMET", "MINERAL_HELMET", "YOG_HELMET"
	);
	public static final Set<String> MINING_CHESTPLATES = Set.of(
		"DIVAN_CHESTPLATE", "ARMOR_OF_DIVAN_CHESTPLATE", "SORROW_CHESTPLATE", "GLACITE_CHESTPLATE", "MINERAL_CHESTPLATE", "YOG_CHESTPLATE"
	);
	public static final Set<String> MINING_LEGGINGS = Set.of(
		"DIVAN_LEGGINGS", "ARMOR_OF_DIVAN_LEGGINGS", "SORROW_LEGGINGS", "GLACITE_LEGGINGS", "MINERAL_LEGGINGS", "YOG_LEGGINGS"
	);
	public static final Set<String> MINING_BOOTS = Set.of(
		"DIVAN_BOOTS", "ARMOR_OF_DIVAN_BOOTS", "SORROW_BOOTS", "GLACITE_BOOTS", "MINERAL_BOOTS", "YOG_BOOTS"
	);
	public static final Set<String> MINING_EQUIPMENT = Set.of(
		"GLACITE_AMULET", "GLACITE_CLOAK", "GLACITE_BELT", "GLACITE_GLOVES", "TITANIUM_NECKLACE", "TITANIUM_CLOAK", "TITANIUM_BELT", "TITANIUM_GLOVES", "GLACITE_BRACELET"
	);

	// =========================================================================
	// 3. FISHING GEAR IDS
	// =========================================================================
	public static final Set<String> FISHING_HELMETS = Set.of(
		"MAGMA_LORD_HELMET", "THUNDER_HELMET", "SHARK_SCALE_HELMET", "SPONGE_HELMET", "DIVER_HELMET", "WATER_HYDRA_HEAD", "SALMON_HELMET", "ANGLER_HELMET"
	);
	public static final Set<String> FISHING_CHESTPLATES = Set.of(
		"MAGMA_LORD_CHESTPLATE", "THUNDER_CHESTPLATE", "SHARK_SCALE_CHESTPLATE", "SPONGE_CHESTPLATE", "DIVER_CHESTPLATE", "SALMON_CHESTPLATE", "ANGLER_CHESTPLATE"
	);
	public static final Set<String> FISHING_LEGGINGS = Set.of(
		"MAGMA_LORD_LEGGINGS", "THUNDER_LEGGINGS", "SHARK_SCALE_LEGGINGS", "SPONGE_LEGGINGS", "DIVER_LEGGINGS", "SALMON_LEGGINGS", "ANGLER_LEGGINGS"
	);
	public static final Set<String> FISHING_BOOTS = Set.of(
		"MAGMA_LORD_BOOTS", "THUNDER_BOOTS", "SHARK_SCALE_BOOTS", "SPONGE_BOOTS", "DIVER_BOOTS", "SQUID_BOOTS", "SALMON_BOOTS", "ANGLER_BOOTS"
	);
	public static final Set<String> FISHING_EQUIPMENT = Set.of(
		"THUNDER_NECKLACE", "THUNDER_CLOAK", "THUNDER_BELT", "THUNDER_GLOVES", "FINWAVE_NECKLACE", "FINWAVE_CLOAK", "FINWAVE_BELT", "FINWAVE_GLOVES", "ICICLE_NECKLACE", "ICICLE_CLOAK", "ICICLE_BELT", "ICICLE_GLOVES", "DELICATES_BRACELET"
	);

	// =========================================================================
	// 4. FORAGING GEAR IDS
	// =========================================================================
	public static final Set<String> FORAGING_HELMETS = Set.of("MASTIFF_HELMET", "GROWTH_HELMET");
	public static final Set<String> FORAGING_CHESTPLATES = Set.of("MASTIFF_CHESTPLATE", "GROWTH_CHESTPLATE");
	public static final Set<String> FORAGING_LEGGINGS = Set.of("MASTIFF_LEGGINGS", "GROWTH_LEGGINGS");
	public static final Set<String> FORAGING_BOOTS = Set.of("MASTIFF_BOOTS", "GROWTH_BOOTS");

	public static List<ParsedItem> getAllPlayerItems(MemberData data) {
		if (data == null || data.inventory == null) return Collections.emptyList();
		List<ParsedItem> all = new ArrayList<>();
		if (data.inventory.inventory != null) all.addAll(data.inventory.inventory);
		if (data.inventory.armor != null) all.addAll(data.inventory.armor);
		if (data.inventory.equipment != null) all.addAll(data.inventory.equipment);
		if (data.inventory.wardrobe != null) all.addAll(data.inventory.wardrobe);
		if (data.inventory.enderchest != null) all.addAll(data.inventory.enderchest);
		if (data.inventory.backpacks != null) {
			for (List<ParsedItem> bp : data.inventory.backpacks) {
				if (bp != null) all.addAll(bp);
			}
		}
		return all;
	}

	public static ItemStack findBestItem(MemberData data, Set<String> targetIds) {
		List<ParsedItem> all = getAllPlayerItems(data);
		for (ParsedItem pi : all) {
			if (pi != null && !pi.isEmpty() && targetIds.contains(pi.skyblockId.toUpperCase(Locale.ROOT))) {
				return pi.itemStack;
			}
		}
		return ItemStack.EMPTY;
	}


	public static List<ItemStack> findArmorSet(MemberData data, Set<String> helmets, Set<String> chests, Set<String> legs, Set<String> boots) {
		List<ItemStack> set = new ArrayList<>(4);
		set.add(findBestItem(data, helmets));
		set.add(findBestItem(data, chests));
		set.add(findBestItem(data, legs));
		set.add(findBestItem(data, boots));
		return set;
	}

	public static List<ItemStack> findEquipmentSet(MemberData data, Set<String> eqIds) {
		List<ItemStack> list = new ArrayList<>();
		List<ParsedItem> all = getAllPlayerItems(data);
		for (ParsedItem pi : all) {
			if (pi != null && !pi.isEmpty() && eqIds.contains(pi.skyblockId.toUpperCase(Locale.ROOT))) {
				list.add(pi.itemStack);
				if (list.size() >= 4) break;
			}
		}
		while (list.size() < 4) {
			list.add(ItemStack.EMPTY);
		}
		return list;
	}
}
