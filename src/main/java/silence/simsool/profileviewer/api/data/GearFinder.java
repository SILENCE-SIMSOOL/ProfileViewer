package silence.simsool.profileviewer.api.data;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.LinkedHashSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import java.util.Comparator;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public final class GearFinder {

	public static ItemStack findFarmingItem(MemberData data, Set<String> ids) {
		return getAllPlayerItems(data).stream()
			.filter(item -> !item.isEmpty() && ids.contains(item.skyblockId))
			.max(Comparator.comparingInt(GearFinder::farmingScore))
			.map(item -> item.itemStack).orElse(ItemStack.EMPTY);
	}

	public static ItemStack findRarestItem(MemberData data, Set<String> ids) {
		return getAllPlayerItems(data).stream()
			.filter(item -> !item.isEmpty() && ids.contains(item.skyblockId))
			.max(Comparator.comparingInt(item -> rarityScore(item.rarity)))
			.map(item -> item.itemStack).orElse(ItemStack.EMPTY);
	}

	public static int farmingScore(ParsedItem item) {
		int score = rarityScore(item.rarity);
		var custom = item.itemStack.get(DataComponents.CUSTOM_DATA);
		if (custom == null) return score;
		var attributes = custom.copyTag();
		var enchantments = attributes.getCompound("enchantments").orElse(null);
		if (enchantments != null) for (String enchant : enchantments.keySet()) score += enchantments.getInt(enchant).orElse(0);
		if (attributes.getInt("rarity_upgrades").orElse(0) > 0) score++;
		if (!attributes.getString("modifier").orElse("").isEmpty()) score++;
		return score;
	}

	private static int rarityScore(String rarity) {
		return switch (rarity.toUpperCase(Locale.ROOT)) {
			case "UNCOMMON" -> 1;
			case "RARE" -> 2;
			case "EPIC" -> 3;
			case "LEGENDARY" -> 4;
			case "MYTHIC" -> 5;
			case "DIVINE" -> 6;
			case "SPECIAL" -> 7;
			case "VERY_SPECIAL" -> 8;
			default -> 0;
		};
	}


	// =========================================================================
	// 1. FARMING GEAR IDS
	// =========================================================================
	private static final JsonObject GEAR = loadGearCatalog();
	public static final Set<String> FARMING_HELMETS = armorPart("farming", "HELMET");
	public static final Set<String> FARMING_CHESTPLATES = armorPart("farming", "CHESTPLATE");
	public static final Set<String> FARMING_LEGGINGS = armorPart("farming", "LEGGINGS");
	public static final Set<String> FARMING_BOOTS = armorPart("farming", "BOOTS");
	public static final Set<String> FARMING_EQUIPMENT = combined("farming", "necklaces", "cloaks", "belts", "gloves");
	public static final Set<String> FARMING_VACUUMS = ids("farming", "vacuum");
	public static final Set<String> FARMING_WATERING_CANS = ids("farming", "watering_can");
	public static final Set<String> FARMING_PETS = ids("farming", "pets");

	// =========================================================================
	// 2. MINING GEAR IDS
	// =========================================================================
	public static final Set<String> MINING_HELMETS = armorPart("mining", "HELMET");
	public static final Set<String> MINING_CHESTPLATES = armorPart("mining", "CHESTPLATE");
	public static final Set<String> MINING_LEGGINGS = armorPart("mining", "LEGGINGS");
	public static final Set<String> MINING_BOOTS = armorPart("mining", "BOOTS");
	public static final Set<String> MINING_EQUIPMENT = combined("mining", "necklaces", "cloaks", "belts", "gloves");
	public static final Set<String> MINING_PICKAXES = ids("mining", "pickaxes");
	public static final Set<String> MINING_CHISELS = ids("mining", "chisels");
	public static final Set<String> MINING_SCRAP = ids("mining", "suspicious_scrap");

	// =========================================================================
	// 3. FISHING GEAR IDS
	// =========================================================================
	public static final Set<String> FISHING_HELMETS = armorPart("fishing", "HELMET");
	public static final Set<String> FISHING_CHESTPLATES = armorPart("fishing", "CHESTPLATE");
	public static final Set<String> FISHING_LEGGINGS = armorPart("fishing", "LEGGINGS");
	public static final Set<String> FISHING_BOOTS = armorPart("fishing", "BOOTS");
	public static final Set<String> FISHING_EQUIPMENT = combined("fishing", "necklaces", "cloaks", "belts", "gloves");
	public static final Set<String> FISHING_RODS = ids("fishing", "rods");
	public static final Set<String> FISHING_TROPHY_ARMOR = ids("fishing", "trophy_armor");

	// =========================================================================
	// 4. FORAGING GEAR IDS
	// =========================================================================
	public static final Set<String> FORAGING_HELMETS = Set.of("MASTIFF_HELMET", "GROWTH_HELMET");
	public static final Set<String> FORAGING_CHESTPLATES = Set.of("MASTIFF_CHESTPLATE", "GROWTH_CHESTPLATE");
	public static final Set<String> FORAGING_LEGGINGS = Set.of("MASTIFF_LEGGINGS", "GROWTH_LEGGINGS");
	public static final Set<String> FORAGING_BOOTS = Set.of("MASTIFF_BOOTS", "GROWTH_BOOTS");

	private static JsonObject loadGearCatalog() {
		try (var stream = GearFinder.class.getResourceAsStream("/assets/profileviewer/data/gear_catalog.json")) {
			if (stream != null) return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (Exception ignored) {}
		return new JsonObject();
	}

	private static Set<String> ids(String section, String key) {
		if (!GEAR.has(section) || !GEAR.getAsJsonObject(section).has(key)) return Set.of();
		Set<String> result = new LinkedHashSet<>();
		JsonArray array = GEAR.getAsJsonObject(section).getAsJsonArray(key);
		array.forEach(value -> result.add(value.getAsString()));
		return Collections.unmodifiableSet(result);
	}

	private static Set<String> armorPart(String section, String suffix) {
		Set<String> result = new LinkedHashSet<>();
		for (String id : ids(section, "armor")) if (List.of(id.split("_")).contains(suffix)) result.add(id);
		return Collections.unmodifiableSet(result);
	}

	private static Set<String> combined(String section, String... keys) {
		Set<String> result = new LinkedHashSet<>();
		for (String key : keys) result.addAll(ids(section, key));
		return Collections.unmodifiableSet(result);
	}

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
		all.addAll(data.inventory.personalVault);
		all.addAll(data.inventory.fishingBag);
		all.addAll(data.inventory.accessoryBag);
		all.addAll(data.inventory.potionBag);
		all.addAll(data.inventory.quiver);
		for (InventoryData.ArmorSet set : data.inventory.loadouts.armorSets.values()) all.addAll(set.getStacks());
		for (InventoryData.EquipmentSet set : data.inventory.loadouts.equipmentSets.values()) all.addAll(set.getStacks());
		Set<String> seen = new LinkedHashSet<>();
		return all.stream().distinct().filter(item -> item != null && (item.uuid.isEmpty() || seen.add(item.uuid))).toList();
	}

	public static ItemStack findBestItem(MemberData data, Set<String> targetIds) {
		List<ParsedItem> items = findBestItems(data, targetIds, 1);
		ParsedItem best = items.isEmpty() ? null : items.get(0);
		return best == null ? ItemStack.EMPTY : best.itemStack;
	}

	public static List<ParsedItem> findBestItems(MemberData data, Set<String> targetIds, int limit) {
		return getAllPlayerItems(data).stream()
			.filter(item -> item != null && !item.isEmpty() && targetIds.contains(item.skyblockId.toUpperCase(Locale.ROOT)))
			.sorted((a, b) -> Double.compare(gearScore(b), gearScore(a)))
			.limit(limit)
			.toList();
	}

	public static long countItems(MemberData data, Set<String> targetIds) {
		long count = getAllPlayerItems(data).stream()
			.filter(item -> item != null && targetIds.contains(item.skyblockId.toUpperCase(Locale.ROOT)))
			.mapToLong(item -> item.count)
			.sum();
		if (data != null && data.inventory != null) {
			for (String id : targetIds) count += data.inventory.sacks.getOrDefault(id, 0);
		}
		return count;
	}

	private static double gearScore(ParsedItem item) {
		int rarity = switch (item.rarity.toUpperCase(Locale.ROOT)) {
			case "VERY_SPECIAL" -> 9;
			case "SPECIAL" -> 8;
			case "MYTHIC" -> 6;
			case "DIVINE" -> 7;
			case "LEGENDARY" -> 5;
			case "EPIC" -> 4;
			case "RARE" -> 3;
			case "UNCOMMON" -> 2;
			default -> 1;
		};
		return item.upgradeScore + Math.clamp(rarity - 3, 0, 3);
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
		return findEquipmentSet(data, eqIds, false);
	}

	public static List<ItemStack> findEquipmentSet(MemberData data, Set<String> eqIds, boolean farming) {
		List<ItemStack> list = new ArrayList<>();
		for (String slot : List.of("necklaces", "cloaks", "belts", "gloves")) {
			Set<String> matching = new LinkedHashSet<>();
			for (String section : List.of("mining", "fishing", "farming")) {
				for (String id : ids(section, slot)) if (eqIds.contains(id)) matching.add(id);
			}
			list.add(farming ? findFarmingItem(data, matching) : findBestItem(data, matching));
		}
		return list;
	}
}
