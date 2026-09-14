package silence.simsool.profileviewer.api.data;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.world.item.ItemStack;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public final class GearFinder {

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
		Set<String> result = new java.util.LinkedHashSet<>();
		JsonArray array = GEAR.getAsJsonObject(section).getAsJsonArray(key);
		array.forEach(value -> result.add(value.getAsString()));
		return Collections.unmodifiableSet(result);
	}

	private static Set<String> armorPart(String section, String suffix) {
		Set<String> result = new java.util.LinkedHashSet<>();
		for (String id : ids(section, "armor")) if (id.endsWith(suffix)) result.add(id);
		return Collections.unmodifiableSet(result);
	}

	private static Set<String> combined(String section, String... keys) {
		Set<String> result = new java.util.LinkedHashSet<>();
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
		return all;
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
			case "MYTHIC" -> 7;
			case "DIVINE" -> 6;
			case "LEGENDARY" -> 5;
			case "EPIC" -> 4;
			case "RARE" -> 3;
			case "UNCOMMON" -> 2;
			default -> 1;
		};
		return (item.upgradeScore + rarity) * 1_000_000_000d + item.estimatedValue;
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
		for (String suffix : List.of("NECKLACE", "CLOAK", "BELT", "GLOVES|BRACELET")) {
			Set<String> matching = eqIds.stream().filter(id -> {
				for (String part : suffix.split("\\|")) if (id.endsWith(part)) return true;
				return false;
			}).collect(java.util.stream.Collectors.toSet());
			list.add(findBestItem(data, matching));
		}
		return list;
	}
}
