package silence.simsool.profileviewer.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import silence.simsool.profileviewer.api.data.HotfTreeData;
import silence.simsool.profileviewer.api.data.CatalogFormula;
import silence.simsool.profileviewer.api.data.ProfileJson;
import silence.simsool.profileviewer.ui.tabs.BestiaryTabRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.CustomData;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.io.InputStreamReader;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.api.repo.CatalogIcons;
import com.google.gson.JsonObject;
import silence.simsool.profileviewer.api.data.GardenData;
import silence.simsool.profileviewer.api.data.MiningData;
import silence.simsool.profileviewer.api.data.CollectionData;
import silence.simsool.profileviewer.api.data.RiftData;
import silence.simsool.profileviewer.api.data.CfData;
import silence.simsool.profileviewer.api.data.DungeonData;
import silence.simsool.profileviewer.api.data.ForagingData;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.profileviewer.api.data.FishingData;
import silence.simsool.profileviewer.api.data.GearFinder;
import silence.simsool.profileviewer.api.data.InventoryData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public final class FeatureRegression {
	private static final List<String> failures = new ArrayList<>();
	private static int checks;

	private static void check(boolean condition, String message) {
		checks++;
		if (!condition) failures.add(message);
	}

	public static void run() throws Exception {
		Items.STONE.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		checkFishing();
		checkGear();
		checkRarity();
		checkUpgradeScore();
		checkProgression();
		checkRiftAndStorage();
		checkHotfCatalog();
		checkCatalogTextures();
		checkRemainingFeatures();
		if (!failures.isEmpty()) throw new AssertionError(String.join("\n", failures));
		System.out.println("Feature regression checks passed: " + checks);
	}


	private static void checkHotfCatalog() {
		var core = HotfTreeData.getNode("center_of_the_forest");
		check(core.row == 3 && core.col == 3 && core.maxLevel == 5, "HOTF core uses real tier and max level");
		var sweep = HotfTreeData.getNodeAt(7, 3);
		check(sweep.id.equals("sweep") && sweep.maxLevel == 50, "HOTF bottom node is Sweep");
		check(HotfTreeData.getNode("hotf_t8_0") == null, "No fabricated HOTF IDs");
		for (var node : HotfTreeData.NODES) {
			check(node.row >= 0 && node.row < 8 && node.col >= 0 && node.col < 7, "HOTF coordinates: " + node.id);
			for (int level : new int[]{0, 1, node.maxLevel}) {
				check(HotfTreeData.tooltip(node, level, 5).stream().noneMatch(line -> line.matches(".*%[a-z_]+%.*")), "Resolved HOTF tooltip: " + node.id);
			}
		}
		check(HotfTreeData.tooltip(HotfTreeData.getNode("axe_toss"), 1, 0).contains("Cooldown: 116s"), "Base axe ability cooldown");
		check(HotfTreeData.tooltip(HotfTreeData.getNode("axe_toss"), 1, 1).contains("Cooldown: 112s"), "Core upgrades axe ability");
		check(CatalogFormula.evaluate("floor((level + 1)^3.07)", 3, 1) == 70, "HOTF whisper cost formula");
		check(CatalogFormula.evaluate("5 + (level * 0.1)", 12, 1) == 6.2, "HOTF decimal reward formula");
		var mob = json("{\"mobs\":[\"a\",\"b\"],\"bracket\":2,\"cap\":50}");
		DungeonData bestiary = new DungeonData();
		bestiary.bestiaryMobKills.put("a", 12);
		bestiary.bestiaryMobKills.put("b", 40);
		check(BestiaryTabRenderer.kills(mob, bestiary) == 52, "Bestiary combines mob variants");
		check(BestiaryTabRenderer.thresholds(mob).getLast() == 50, "Bestiary threshold respects mob cap");
		for (var texture : ProfileJson.catalog("chocolate_textures").entrySet()) {
			String decoded = new String(Base64.getDecoder().decode(NbtItemParser.fixBase64Padding(texture.getValue().getAsString())), StandardCharsets.UTF_8);
			check(json(decoded).has("textures"), "Employee texture: " + texture.getKey());
		}
		ParsedItem item = new ParsedItem();
		item.rarity = "LEGENDARY";
		item.itemStack = new ItemStack(Items.STONE);
		CompoundTag attributes = new CompoundTag();
		CompoundTag enchantments = new CompoundTag();
		enchantments.putInt("cultivating", 10);
		enchantments.putInt("harvesting", 6);
		attributes.put("enchantments", enchantments);
		attributes.putInt("rarity_upgrades", 1);
		attributes.putString("modifier", "rooted");
		item.itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(attributes));
		check(GearFinder.farmingScore(item) == 22, "Farming score uses full enchant levels and rarity");
	}


	private static void checkCatalogTextures() throws Exception {
		Items.PLAYER_HEAD.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		verifyBestiaryTextures(ProfileJson.catalog("bestiary"));
		Method parse = ItemRepo.class.getDeclaredMethod("parseRepoJson", JsonElement.class);
		parse.setAccessible(true);
		try (var stream = FeatureRegression.class.getResourceAsStream("/repo/items.min.json");
			 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			parse.invoke(null, JsonParser.parseReader(reader));
		}
		for (String id : List.of("COBBLESTONE_GENERATOR_11", "WHEAT_GENERATOR_12", "DIAMOND_GENERATOR_11")) {
			check(ItemRepo.getItemStack(id).has(DataComponents.PROFILE), "Minion has a textured profile: " + id);
		}
		check(NbtItemParser.createSkull(ProfileJson.string(ProfileJson.catalog("skull_textures"), "coach_jackrabbit"), 1).has(DataComponents.PROFILE), "Coach Jackrabbit has a texture");
	}

	private static void verifyBestiaryTextures(JsonElement value) {
		if (value.isJsonArray()) {
			for (JsonElement child : value.getAsJsonArray()) verifyBestiaryTextures(child);
		} else if (value.isJsonObject()) {
			var object = value.getAsJsonObject();
			if (object.has("texture")) check(CatalogIcons.icon(object, "Bestiary").has(DataComponents.PROFILE), "Bestiary texture: " + ProfileJson.string(object, "name"));
			for (var child : object.entrySet()) verifyBestiaryTextures(child.getValue());
		}
	}


	private static void checkRemainingFeatures() {
		MiningData trees = MiningData.fromJson(json("""
			{"skill_tree":{"selected_slot":{"foraging":2},"nodes":{
			"foraging":{"sweep":10,"toggle_sweep":false,"toggle_daily_wishes":true},
			"foraging_2":{"sweep":20},
			"mining":{"mining_speed":30,"toggle_mining_speed":false}}}}
			"""));
		check(trees.disabledForagingNodes.contains("sweep") && !trees.disabledForagingNodes.contains("daily_wishes"), "Foraging toggle state is separate from node levels");
		check(!trees.foragingNodes.containsKey("toggle_sweep"), "Toggle fields never become levels");
		check(trees.disabledMiningNodes.contains("mining_speed"), "Mining disabled state is preserved");
		check(MiningData.fromJson(json("{}")).disabledForagingNodes.isEmpty(), "New profiles do not inherit disabled nodes");
		Items.STRIPPED_MANGROVE_LOG.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		Items.STRIPPED_OAK_LOG.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		Items.PALE_OAK_BUTTON.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		var sweep = HotfTreeData.getNode("sweep");
		var disabled = HotfTreeData.createNodeStack(sweep, 20, false, 8, 5, true);
		check(disabled.is(Items.STRIPPED_MANGROVE_LOG), "Disabled HOTF node uses original disabled item");
		check(disabled.get(DataComponents.LORE).lines().stream().anyMatch(line -> line.getString().contains("DISABLED")), "Disabled HOTF tooltip");
		check(HotfTreeData.createNodeStack(sweep, 0, false, 8, 5).is(Items.STRIPPED_OAK_LOG), "Present level-zero HOTF node is unlocked");
		check(HotfTreeData.createNodeStack(sweep, -1, false, 8, 5).is(Items.PALE_OAK_BUTTON), "Absent HOTF node stays locked");
		CfData factory = CfData.fromJson(json("{\"events\":{\"easter\":{\"employees\":{\"rabbit_bro\":42,\"metadata\":3}}}}"));
		check(factory.available && factory.employees.size() == 7 && factory.employees.getFirst().id.equals("rabbit_bro"), "Factory uses catalog order and includes all employees");
		check(factory.employees.getFirst().level == 42 && factory.employees.get(1).level == 0, "Unhired employees have level zero");
		check(CfData.employeeName("rabbit_father").equals("Rabbit Daddy"), "Original employee display names");
		check(!CfData.fromJson(json("{}")).available && CfData.fromJson(json("{}")).employees.isEmpty(), "Missing factory remains no data");
		DungeonData combat = DungeonData.fromJson(json("""
			{"nether_island_player_data":{"dojo":{"dojo_points_mob_kb":1000,"dojo_time_mob_kb":9999},
			"kuudra_completed_tiers":{"none":12,"hot":4}}}
			"""));
		check(combat.dojoScores.size() == 7 && combat.dojoScores.get("mob_kb") == 1000 && combat.dojoScores.get("archer") == -1, "Dojo parses scores only and distinguishes missing records");
		check(DungeonData.fromJson(json("{}")).dojoScores.values().stream().allMatch(value -> value == -1), "Missing Dojo records stay unplayed");
		check(DungeonData.dojoGrade(999).equals("A") && DungeonData.dojoGrade(1000).equals("S") && DungeonData.dojoGrade(-1).equals("Not played"), "Dojo grade thresholds");
		check(combat.kuudraCompletions.get("none") == 12, "Basic Kuudra uses the API none key");
		for (var item : BuiltInRegistries.ITEM) item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
		var pages = BestiaryTabRenderer.getPages(combat);
		var fishing = pages.stream().filter(page -> page.children().size() == 6).findFirst().orElseThrow();
		var safari = pages.stream().filter(page -> page.children().size() == 4).findFirst().orElseThrow();
		check(fishing.items().isEmpty() && safari.items().isEmpty(), "Nested Bestiary categories are not flattened");
		check(fishing.children().stream().allMatch(page -> !page.items().isEmpty()), "Fishing subcategories retain their own mobs");
		check(safari.children().stream().allMatch(page -> !page.items().isEmpty()), "Safari subcategories retain their own mobs");
	}

	private static JsonObject json(String source) {
		return JsonParser.parseString(source).getAsJsonObject();
	}

	private static void checkProgression() {
		check(GardenData.calcGardenLevel(69) == 1 && GardenData.calcGardenLevel(70) == 2 && GardenData.calcGardenLevel(140) == 3, "Garden XP uses cumulative thresholds");
		check(GardenData.chipSowdust(1) == 0 && GardenData.chipSowdust(3) == 300000, "Chip cost counts purchased levels");
		GardenData member = GardenData.fromJson(json("""
			{"garden":{"copper":321,"larva_consumed":4},
			"jacob2":{"medals_inv":{"gold":7},"contests":{"a":{},"b":{}},"perks":{"farming_level_cap":3,"double_drops":8},"personal_bests":{"CARROT_ITEM":900}},
			"player_data":{"garden_chips":{"vermin_vaporizer":5}}}
			"""));
		GardenData shared = GardenData.fromJson(json("""
			{"garden_experience":140,"resources_collected":{"CARROT_ITEM":1000},
			"commission_data":{"visits":{"bob":10},"completed":{"bob":2}},
			"composter_data":{"compost_items":3,"upgrades":{"speed":4}}}
			"""));
		shared.copyMemberData(member);
		check(shared.copper == 321 && shared.goldMedals == 7 && shared.gardenLevel == 3, "Shared garden merge preserves member currencies");
		check(shared.contestsParticipated == 2 && shared.farmingLevelCap == 3 && shared.doubleDrops == 8, "Garden contest count and perks are actual data");
		check(shared.completedVisitors == 2 && shared.uniqueVisitors == 1 && shared.visitorVisits.get("bob") == 10, "Visitor visits differ from completed requests");
		check(shared.cropMilestones.get("carrot") == 1000 && shared.personalBests.get("carrot") == 900, "Crop API identifiers normalize consistently");
		check(shared.chipLevels.get("vermin_vaporizer") == 5 && shared.composterItems == 3 && shared.composterSpeedUpgrade == 4, "Garden subpage data survives merge");

		check(MiningData.calcHotfLevel(546999) == 7 && MiningData.calcHotfLevel(547000) == 8, "HOTF level eight boundary");
		MiningData trees = MiningData.fromJson(json("""
			{"mining_core":{"nodes":{"special_0":7},"selected_pickaxe_ability":"old"},
			"skill_tree":{"selected_skill_tree_slot":{"mining":2,"foraging":2},
			"nodes":{"mining":{"special_0":7},"mining_2":{},"foraging":{"center_of_the_forest":3},"foraging_2":{}},
			"selected_ability":{"mining_2":"","foraging_2":""}}}
			"""));
		check(trees.presetNodes.containsKey(2) && trees.nodes.isEmpty() && trees.peakOfTheMountain == 0, "Empty selected mining preset clears nodes and peak");
		check(trees.foragingPresetNodes.containsKey(2) && trees.foragingNodes.isEmpty() && trees.centerOfTheForest == 0 && trees.selectedAbility.isEmpty(), "Empty selected preset clears ability and forest center");
		CollectionData collections = CollectionData.fromJson(json("{\"collection\":{\"WHEAT\":100,\"COBBLESTONE\":3000000000}}"));
		var wheat = collections.farmingCollections.stream().filter(c -> c.id.equals("WHEAT")).findFirst().orElseThrow();
		check(wheat.tier == 2 && wheat.maxTier == 11, "Collection tiers use official non-linear thresholds");
		check(collections.rawCollections.get("COBBLESTONE") == 3000000000L, "Collection amounts retain 64-bit counts");
		check(!CollectionData.fromJson(json("{}")).available, "Missing collection API differs from empty collection");
		RiftData rift = RiftData.fromJson(json("""
			{"rift":{"gallery":{"secured_trophies":[{"type":"wyldly_supreme"},{"type":"wyldly_supreme"},{"type":"mountain"},{"type":"unknown"}]}}}
			"""));
		check(rift.timecharms == 2 && rift.unlockedTimecharms.contains("Celestial Timecharm"), "Rift trophies use API IDs and exclude duplicate or unknown entries");
		check(RiftData.timecharmId("Celestial Timecharm").equals("RIFT_TROPHY_MOUNTAIN"), "Timecharm item ID follows catalog ID");

		String rabbit = CfData.CATALOG.getAsJsonObject("rabbits").entrySet().iterator().next().getValue().getAsJsonArray().get(0).getAsString();
		CfData cf = CfData.fromJson(json("{\"events\":{\"easter\":{\"rabbits\":{\"" + rabbit + "\":2,\"eggs_collected\":999},\"time_tower\":{\"level\":3,\"charges\":2}}}}"));
		check(cf.rabbits.size() == 1 && cf.rabbits.get(rabbit) == 2, "Chocolate rabbit collection excludes numeric metadata");
		check(cf.timeTowerLevel == 3 && cf.timeTowerCharges == 2, "Time tower shows actual level and charges");
		ForagingData forest = ForagingData.fromJson(json("""
			{"skill_tree":{"selected_skill_tree_slot":{"foraging":2}},
			"foraging_core":{"whispers":{"forest":{"total":1000,"1":{"spent":100},"2":{"spent":700}}}},
			"foraging":{"tree_gifts":{"FIG":9,"milestone_tier_claimed":{"FIG":2}}}}
			"""));
		check(forest.forestTotal == 1000 && forest.forestWhispers == 300 && forest.treeGifts.get("FIG") == 9 && forest.giftTiers.get("FIG") == 2, "Foraging whispers use selected preset expenditure");
		check(DungeonData.getLevelAndProgress(569809639).first == 49 && DungeonData.getLevelAndProgress(569809640).first == 50, "Catacombs level 50 boundary");
		check(DungeonData.getLevelAndProgress(769809640).first == 51, "Catacombs overflow starts after the level 50 threshold");
		check(DungeonData.getLevelAndProgress(1684640).first == 28, "Catacombs intermediate thresholds follow original catalog");
		DungeonData dungeon = DungeonData.fromJson(json("{\"dungeons\":{\"dungeon_types\":{\"catacombs\":{\"tier_completions\":{\"1\":2,\"7\":3,\"total\":5}}}}}"));
		check(dungeon.totalNormalRuns == 5 && !dungeon.normalFloors.containsKey("Ftotal"), "Dungeon totals must not count API total twice");
		MemberData invalidInventory = MemberData.fromJson(json("{\"inventory\":{\"inv_contents\":{\"data\":\"not-base64\"}}}"), 0);
		check(invalidInventory.sectionErrors.containsKey("inventory"), "Invalid inventory NBT is reported instead of showing an empty inventory");
		MemberData malformed = MemberData.fromJson(json("{\"garden\":{\"copper\":\"bad\"},\"trophy_fish\":{\"total_caught\":5}}"), 0);
		check(malformed.sectionErrors.containsKey("garden") && malformed.fishing.totalCatches == 5, "Invalid section is reported without discarding valid sections");
	}
	private static void checkFishing() {
		MemberData member = MemberData.fromJson(JsonParser.parseString("""
			{"trophy_fish":{"total_caught":12,"blobfish":10,"blobfish_bronze":7,"blobfish_silver":3,
			"obfuscated_fish_1_gold":2,"last_caught":"OBFUSCATED_FISH_1/GOLD","rewards":[1]},
			"player_stats":{"items_fished":{"total":100,"normal":70,"treasure":20,"large_treasure":10},
			"pets":{"milestone":{"sea_creatures_killed":1234}}}}
			""").getAsJsonObject(), 0);
		FishingData fish = member.fishing;
		check(fish.totalCatches == 12, "last_caught must not discard the fishing section");
		check(fish.bronzeTrophy == 7 && fish.silverTrophy == 3 && fish.goldTrophy == 2, "Species totals must not count as bronze");
		check(fish.seaCreaturesKilled == 1234, "Sea creature total uses pet milestones");
		FishingData ordered = FishingData.fromJson(JsonParser.parseString("{\"trophy_fish\":{\"blobfish\":10,\"blobfish_bronze\":7,\"blobfish_silver\":3}}").getAsJsonObject());
		FishingData reversed = FishingData.fromJson(JsonParser.parseString("{\"trophy_fish\":{\"blobfish_silver\":3,\"blobfish_bronze\":7,\"blobfish\":10}}").getAsJsonObject());
		check(ordered.bronzeTrophy == 7 && reversed.bronzeTrophy == 7, "Trophy counts must not depend on JSON key order");
	}

	private static ParsedItem item(String id) {
		ParsedItem item = new ParsedItem();
		item.skyblockId = id;
		item.itemStack = new ItemStack(Items.STONE);
		return item;
	}

	private static void checkGear() {
		MemberData member = new MemberData();
		ParsedItem pendant = item("DIVAN_PENDANT");
		ParsedItem gloves = item("DWARVEN_HANDWARMERS");
		member.inventory.inventory.addAll(List.of(pendant, gloves));
		List<ItemStack> equipment = GearFinder.findEquipmentSet(member, GearFinder.MINING_EQUIPMENT);
		check(equipment.get(0) == pendant.itemStack && equipment.get(3) == gloves.itemStack, "Catalog slots include pendant and handwarmers");
		ParsedItem cape = item("ZORROS_CAPE");
		member.inventory.inventory.add(cape);
		check(GearFinder.findEquipmentSet(member, GearFinder.FARMING_EQUIPMENT).get(1) == cape.itemStack, "Cape belongs to the cloak slot");
		ParsedItem helmet = item("SALMON_HELMET_NEW");
		member.inventory.inventory.add(helmet);
		check(GearFinder.findBestItem(member, GearFinder.FISHING_HELMETS) == helmet.itemStack, "Salmon armor variant remains eligible");
		ParsedItem stored = item("DIVAN_HELMET");
		member.inventory.personalVault.add(stored);
		check(GearFinder.findBestItem(member, GearFinder.MINING_HELMETS) == stored.itemStack, "Gear search includes personal vault");
		InventoryData.EquipmentSet set = new InventoryData.EquipmentSet();
		ParsedItem cloak = item("SAPPHIRE_CLOAK");
		set.slot2 = cloak;
		member.inventory.loadouts.equipmentSets.put(2, set);
		check(GearFinder.findEquipmentSet(member, GearFinder.MINING_EQUIPMENT).get(1) == cloak.itemStack, "Gear search includes saved equipment");
	}

	private static void checkRiftAndStorage() {
		RiftData rift = RiftData.fromJson(json("""
			{"player_stats":{"rift":{"visits":12}},"rift":{"dead_cats":{"found_cats":["a","a","b"],"montezuma":{"tier":"RARE","exp":123.5}},
			"wither_cage":{"killed_eyes":["one","one","two"]},"village_plaza":{"lonely":{"seconds_sitting":100}},"castle":{"grubber_stacks":4}}}
			"""));
		check(rift.visits == 12 && rift.secondsSitting == 100 && rift.grubberStacks == 4, "Rift progress fields use actual API paths");
		check(rift.foundCats.size() == 2 && rift.porhtalProgress == 2 && rift.montezumaExperience == 123.5 && rift.montezumaRarity.equals("RARE"), "Rift collectibles are unique and Montezuma metadata is retained");
		MemberData member = new MemberData();
		ParsedItem item = item("DIVAN_HELMET");
		item.uuid = "same-item";
		ParsedItem duplicate = item("DIVAN_HELMET");
		duplicate.uuid = "same-item";
		member.inventory.inventory.add(item);
		member.inventory.wardrobe.add(duplicate);
		InventoryData.ArmorSet armor = new InventoryData.ArmorSet();
		ParsedItem stored = item("DIVAN_CHESTPLATE");
		armor.chestplate = stored;
		member.inventory.loadouts.armorSets.put(2, armor);
		check(GearFinder.findBestItem(member, GearFinder.MINING_CHESTPLATES) == stored.itemStack, "Saved armor remains searchable when wardrobe data also exists");
		check(GearFinder.findBestItems(member, GearFinder.MINING_HELMETS, 4).size() == 1, "Same UUID in equipped and stored gear is not duplicated");
	}

	private static void checkUpgradeScore() throws Exception {
		Method method = NbtItemParser.class.getDeclaredMethod("calculateUpgradeScore", CompoundTag.class);
		method.setAccessible(true);
		CompoundTag attributes = new CompoundTag();
		CompoundTag gems = new CompoundTag();
		gems.put("unlocked_slots", new ListTag());
		attributes.put("gems", gems);
		check((int) method.invoke(null, attributes) == 0, "Empty gemstone slots must not improve gear score");
		gems.putString("JADE_0", "PERFECT");
		gems.putString("UNIVERSAL_0", "FLAWLESS");
		gems.putString("UNIVERSAL_0_gem", "JASPER");
		check((int) method.invoke(null, attributes) == 2, "Gemstone score follows quality and Jasper bonus");
		CompoundTag enchantments = new CompoundTag();
		enchantments.putInt("ultimate_legion", 5);
		attributes.put("enchantments", enchantments);
		check((int) method.invoke(null, attributes) == 8, "Ultimate enchantments include level and above-four bonus");
	}

	private static void checkRarity() throws Exception {
		Method method = NbtItemParser.class.getDeclaredMethod("detectRarity", ParsedItem.class);
		method.setAccessible(true);
		for (String rarity : List.of("UNCOMMON", "VERY SPECIAL", "DIVINE")) {
			ParsedItem item = item("TEST");
			item.lore.add("§a§l" + rarity + " SWORD");
			method.invoke(null, item);
			check(item.rarity.equals(rarity.replace(' ', '_')), "Rarity parsing: " + rarity);
		}
	}
}
