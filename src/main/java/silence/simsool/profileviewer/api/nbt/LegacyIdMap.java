package silence.simsool.profileviewer.api.nbt;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class LegacyIdMap {

	private static final Map<Integer, Item> SIMPLE_MAP = new HashMap<>();

	private static final String[] COLOR_NAMES = new String[] {
		"white", "orange", "magenta", "light_blue",
		"yellow", "lime", "pink", "gray",
		"light_gray", "cyan", "purple", "blue",
		"brown", "green", "red", "black"
	};

	private static final String[] DYE_NAMES = new String[] {
		"ink_sac", "red_dye", "green_dye", "cocoa_beans",
		"lapis_lazuli", "purple_dye", "cyan_dye", "light_gray_dye",
		"gray_dye", "pink_dye", "lime_dye", "yellow_dye",
		"light_blue_dye", "magenta_dye", "orange_dye", "bone_meal"
	};

	private static final String[] FLOWER_NAMES = new String[] {
		"poppy", "blue_orchid", "allium", "azure_bluet",
		"red_tulip", "orange_tulip", "white_tulip", "pink_tulip",
		"oxeye_daisy"
	};

	private static final String[] DOUBLE_PLANT_NAMES = new String[] {
		"sunflower", "lilac", "tall_grass", "large_fern",
		"rose_bush", "peony"
	};

	private static final String[] SLAB_NAMES = new String[] {
		"smooth_stone_slab", "sandstone_slab", "petrified_oak_slab", "cobblestone_slab",
		"brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab"
	};

	private static final String[] WOOD_SLAB_NAMES = new String[] {
		"oak_slab", "spruce_slab", "birch_slab", "jungle_slab",
		"acacia_slab", "dark_oak_slab"
	};

	private static Item getByMcId(String name) {
		if (name == null || name.isEmpty()) return Items.AIR;
		String id = name.contains(":") ? name : "minecraft:" + name;
		Identifier res = Identifier.tryParse(id);
		if (res != null) {
			Item item = BuiltInRegistries.ITEM.getValue(res);
			if (item != null && item != Items.AIR) return item;
		}
		return Items.AIR;
	}

	static {
		// Basic Blocks
		map(1, "stone");
		map(2, "grass_block");
		map(3, "dirt");
		map(4, "cobblestone");
		map(5, "oak_planks");
		map(6, "oak_sapling");
		map(7, "bedrock");
		map(8, "water_bucket");
		map(9, "water_bucket");
		map(10, "lava_bucket");
		map(11, "lava_bucket");
		map(12, "sand");
		map(13, "gravel");
		map(14, "gold_ore");
		map(15, "iron_ore");
		map(16, "coal_ore");
		map(17, "oak_log");
		map(18, "oak_leaves");
		map(19, "sponge");
		map(20, "glass");
		map(21, "lapis_ore");
		map(22, "lapis_block");
		map(23, "dispenser");
		map(24, "sandstone");
		map(25, "note_block");
		map(26, "red_bed");
		map(27, "powered_rail");
		map(28, "detector_rail");
		map(29, "sticky_piston");
		map(30, "cobweb");
		map(31, "short_grass");
		map(32, "dead_bush");
		map(33, "piston");
		map(37, "dandelion");
		map(38, "poppy");
		map(39, "brown_mushroom");
		map(40, "red_mushroom");
		map(41, "gold_block");
		map(42, "iron_block");
		map(43, "smooth_stone_slab");
		map(44, "stone_slab");
		map(45, "bricks");
		map(46, "tnt");
		map(47, "bookshelf");
		map(48, "mossy_cobblestone");
		map(49, "obsidian");
		map(50, "torch");
		map(52, "spawner");
		map(53, "oak_stairs");
		map(54, "chest");
		map(56, "diamond_ore");
		map(57, "diamond_block");
		map(58, "crafting_table");
		map(61, "furnace");
		map(65, "ladder");
		map(66, "rail");
		map(67, "cobblestone_stairs");
		map(69, "lever");
		map(70, "stone_pressure_plate");
		map(72, "oak_pressure_plate");
		map(73, "redstone_ore");
		map(76, "redstone_torch");
		map(77, "stone_button");
		map(78, "snow");
		map(79, "ice");
		map(80, "snow_block");
		map(81, "cactus");
		map(82, "clay");
		map(84, "jukebox");
		map(85, "oak_fence");
		map(86, "carved_pumpkin");
		map(87, "netherrack");
		map(88, "soul_sand");
		map(89, "glowstone");
		map(91, "jack_o_lantern");
		map(92, "cake");
		map(96, "oak_trapdoor");
		map(98, "stone_bricks");
		map(99, "brown_mushroom_block");
		map(100, "red_mushroom_block");
		map(101, "iron_bars");
		map(102, "glass_pane");
		map(103, "melon");
		map(106, "vine");
		map(107, "oak_fence_gate");
		map(108, "brick_stairs");
		map(109, "stone_brick_stairs");
		map(110, "mycelium");
		map(111, "lily_pad");
		map(112, "nether_bricks");
		map(113, "nether_brick_fence");
		map(114, "nether_brick_stairs");
		map(116, "enchanting_table");
		map(117, "brewing_stand");
		map(118, "cauldron");
		map(120, "end_portal_frame");
		map(121, "end_stone");
		map(122, "dragon_egg");
		map(123, "redstone_lamp");
		map(126, "oak_slab");
		map(127, "cocoa_beans");
		map(128, "sandstone_stairs");
		map(129, "emerald_ore");
		map(130, "ender_chest");
		map(131, "tripwire_hook");
		map(133, "emerald_block");
		map(134, "spruce_stairs");
		map(135, "birch_stairs");
		map(136, "jungle_stairs");
		map(137, "command_block");
		map(138, "beacon");
		map(139, "cobblestone_wall");
		map(140, "flower_pot");
		map(141, "carrot");
		map(142, "potato");
		map(143, "oak_button");
		map(145, "anvil");
		map(146, "trapped_chest");
		map(147, "light_weighted_pressure_plate");
		map(148, "heavy_weighted_pressure_plate");
		map(151, "daylight_detector");
		map(152, "redstone_block");
		map(153, "nether_quartz_ore");
		map(154, "hopper");
		map(155, "quartz_block");
		map(156, "quartz_stairs");
		map(157, "activator_rail");
		map(158, "dropper");
		map(165, "slime_block");
		map(166, "barrier");
		map(167, "iron_trapdoor");
		map(168, "prismarine");
		map(169, "sea_lantern");
		map(170, "hay_block");
		map(172, "terracotta");
		map(173, "coal_block");
		map(174, "packed_ice");
		map(179, "red_sandstone");
		map(180, "red_sandstone_stairs");
		map(182, "red_sandstone_slab");
		map(183, "spruce_fence_gate");
		map(184, "birch_fence_gate");
		map(185, "jungle_fence_gate");
		map(186, "dark_oak_fence_gate");
		map(187, "acacia_fence_gate");
		map(188, "spruce_fence");
		map(189, "birch_fence");
		map(190, "jungle_fence");
		map(191, "dark_oak_fence");
		map(192, "acacia_fence");

		// Items
		map(256, "iron_shovel");
		map(257, "iron_pickaxe");
		map(258, "iron_axe");
		map(259, "flint_and_steel");
		map(260, "apple");
		map(261, "bow");
		map(262, "arrow");
		map(263, "coal");
		map(264, "diamond");
		map(265, "iron_ingot");
		map(266, "gold_ingot");
		map(267, "iron_sword");
		map(268, "wooden_sword");
		map(269, "wooden_shovel");
		map(270, "wooden_pickaxe");
		map(271, "wooden_axe");
		map(272, "stone_sword");
		map(273, "stone_shovel");
		map(274, "stone_pickaxe");
		map(275, "stone_axe");
		map(276, "diamond_sword");
		map(277, "diamond_shovel");
		map(278, "diamond_pickaxe");
		map(279, "diamond_axe");
		map(280, "stick");
		map(281, "bowl");
		map(282, "mushroom_stew");
		map(283, "golden_sword");
		map(284, "golden_shovel");
		map(285, "golden_pickaxe");
		map(286, "golden_axe");
		map(287, "string");
		map(288, "feather");
		map(289, "gunpowder");
		map(290, "wooden_hoe");
		map(291, "stone_hoe");
		map(292, "iron_hoe");
		map(293, "diamond_hoe");
		map(294, "golden_hoe");
		map(295, "wheat_seeds");
		map(296, "wheat");
		map(297, "bread");
		map(298, "leather_helmet");
		map(299, "leather_chestplate");
		map(300, "leather_leggings");
		map(301, "leather_boots");
		map(302, "chainmail_helmet");
		map(303, "chainmail_chestplate");
		map(304, "chainmail_leggings");
		map(305, "chainmail_boots");
		map(306, "iron_helmet");
		map(307, "iron_chestplate");
		map(308, "iron_leggings");
		map(309, "iron_boots");
		map(310, "diamond_helmet");
		map(311, "diamond_chestplate");
		map(312, "diamond_leggings");
		map(313, "diamond_boots");
		map(314, "golden_helmet");
		map(315, "golden_chestplate");
		map(316, "golden_leggings");
		map(317, "golden_boots");
		map(318, "flint");
		map(319, "porkchop");
		map(320, "cooked_porkchop");
		map(321, "painting");
		map(322, "golden_apple");
		map(323, "oak_sign");
		map(324, "oak_door");
		map(325, "bucket");
		map(326, "water_bucket");
		map(327, "lava_bucket");
		map(328, "minecart");
		map(329, "saddle");
		map(330, "iron_door");
		map(331, "redstone");
		map(332, "snowball");
		map(333, "oak_boat");
		map(334, "leather");
		map(335, "milk_bucket");
		map(336, "brick");
		map(337, "clay_ball");
		map(338, "sugar_cane");
		map(339, "paper");
		map(340, "book");
		map(341, "slime_ball");
		map(342, "chest_minecart");
		map(343, "furnace_minecart");
		map(344, "egg");
		map(345, "compass");
		map(346, "fishing_rod");
		map(347, "clock");
		map(348, "glowstone_dust");
		map(352, "bone");
		map(353, "sugar");
		map(354, "cake");
		map(355, "red_bed");
		map(356, "repeater");
		map(357, "cookie");
		map(358, "filled_map");
		map(359, "shears");
		map(360, "melon_slice");
		map(361, "pumpkin_seeds");
		map(362, "melon_seeds");
		map(363, "beef");
		map(364, "cooked_beef");
		map(365, "chicken");
		map(366, "cooked_chicken");
		map(367, "rotten_flesh");
		map(368, "ender_pearl");
		map(369, "blaze_rod");
		map(370, "ghast_tear");
		map(371, "gold_nugget");
		map(372, "nether_wart");
		map(373, "potion");
		map(374, "glass_bottle");
		map(375, "spider_eye");
		map(376, "fermented_spider_eye");
		map(377, "blaze_powder");
		map(378, "magma_cream");
		map(379, "brewing_stand");
		map(380, "cauldron");
		map(381, "ender_eye");
		map(382, "glistering_melon_slice");
		map(384, "experience_bottle");
		map(385, "fire_charge");
		map(386, "writable_book");
		map(387, "written_book");
		map(388, "emerald");
		map(389, "item_frame");
		map(390, "flower_pot");
		map(391, "carrot");
		map(392, "potato");
		map(393, "baked_potato");
		map(394, "poisonous_potato");
		map(395, "map");
		map(396, "golden_carrot");
		map(398, "carrot_on_a_stick");
		map(399, "nether_star");
		map(400, "pumpkin_pie");
		map(401, "firework_rocket");
		map(402, "firework_star");
		map(403, "enchanted_book");
		map(404, "comparator");
		map(405, "nether_brick");
		map(406, "quartz");
		map(407, "tnt_minecart");
		map(408, "hopper_minecart");
		map(409, "prismarine_shard");
		map(410, "prismarine_crystals");
		map(411, "rabbit");
		map(412, "cooked_rabbit");
		map(413, "rabbit_stew");
		map(414, "rabbit_foot");
		map(415, "rabbit_hide");
		map(416, "armor_stand");
		map(417, "iron_horse_armor");
		map(418, "golden_horse_armor");
		map(419, "diamond_horse_armor");
		map(420, "lead");
		map(421, "name_tag");
		map(423, "mutton");
		map(424, "cooked_mutton");
		map(427, "spruce_door");
		map(428, "birch_door");
		map(429, "jungle_door");
		map(430, "acacia_door");
		map(431, "dark_oak_door");
	}

	private static void map(int id, String name) {
		Item item = getByMcId(name);
		if (item != Items.AIR) {
			SIMPLE_MAP.put(id, item);
		}
	}

	public static Item getItem(int numId, int damage) {
		int safeDamage = Math.max(0, damage);

		// Multi-meta blocks & items
		switch (numId) {
			case 1: // Stone
				return switch (safeDamage) {
					case 1 -> getByMcId("granite");
					case 2 -> getByMcId("polished_granite");
					case 3 -> getByMcId("diorite");
					case 4 -> getByMcId("polished_diorite");
					case 5 -> getByMcId("andesite");
					case 6 -> getByMcId("polished_andesite");
					default -> getByMcId("stone");
				};
			case 3: // Dirt
				return switch (safeDamage) {
					case 1 -> getByMcId("coarse_dirt");
					case 2 -> getByMcId("podzol");
					default -> getByMcId("dirt");
				};
			case 5: // Planks
				return switch (safeDamage) {
					case 1 -> getByMcId("spruce_planks");
					case 2 -> getByMcId("birch_planks");
					case 3 -> getByMcId("jungle_planks");
					case 4 -> getByMcId("acacia_planks");
					case 5 -> getByMcId("dark_oak_planks");
					default -> getByMcId("oak_planks");
				};
			case 6: // Sapling
				return switch (safeDamage) {
					case 1 -> getByMcId("spruce_sapling");
					case 2 -> getByMcId("birch_sapling");
					case 3 -> getByMcId("jungle_sapling");
					case 4 -> getByMcId("acacia_sapling");
					case 5 -> getByMcId("dark_oak_sapling");
					default -> getByMcId("oak_sapling");
				};
			case 12: // Sand
				return safeDamage == 1 ? getByMcId("red_sand") : getByMcId("sand");
			case 17: // Wood Log
				return switch (safeDamage % 4) {
					case 1 -> getByMcId("spruce_log");
					case 2 -> getByMcId("birch_log");
					case 3 -> getByMcId("jungle_log");
					default -> getByMcId("oak_log");
				};
			case 18: // Leaves
				return switch (safeDamage % 4) {
					case 1 -> getByMcId("spruce_leaves");
					case 2 -> getByMcId("birch_leaves");
					case 3 -> getByMcId("jungle_leaves");
					default -> getByMcId("oak_leaves");
				};
			case 19: // Sponge
				return safeDamage == 1 ? getByMcId("wet_sponge") : getByMcId("sponge");
			case 24: // Sandstone
				return switch (safeDamage) {
					case 1 -> getByMcId("chiseled_sandstone");
					case 2 -> getByMcId("cut_sandstone");
					default -> getByMcId("sandstone");
				};
			case 31: // Tall grass
				return safeDamage == 2 ? getByMcId("fern") : (safeDamage == 0 ? getByMcId("dead_bush") : getByMcId("short_grass"));
			case 35: // Wool
				if (safeDamage < COLOR_NAMES.length) {
					return getByMcId(COLOR_NAMES[safeDamage] + "_wool");
				}
				return getByMcId("white_wool");
			case 38: // Flowers
				if (safeDamage < FLOWER_NAMES.length) {
					return getByMcId(FLOWER_NAMES[safeDamage]);
				}
				return getByMcId("poppy");
			case 44: // Slabs
				int slabIdx = safeDamage % 8;
				return getByMcId(SLAB_NAMES[slabIdx]);
			case 95: // Stained Glass
				if (safeDamage < COLOR_NAMES.length) {
					return getByMcId(COLOR_NAMES[safeDamage] + "_stained_glass");
				}
				return getByMcId("white_stained_glass");
			case 97: // Infested
				return getByMcId("infested_stone");
			case 98: // Stone Bricks
				return switch (safeDamage) {
					case 1 -> getByMcId("mossy_stone_bricks");
					case 2 -> getByMcId("cracked_stone_bricks");
					case 3 -> getByMcId("chiseled_stone_bricks");
					default -> getByMcId("stone_bricks");
				};
			case 125: // Double Wood Slab
			case 126: // Wood Slab
				int wIdx = safeDamage % 6;
				return getByMcId(WOOD_SLAB_NAMES[wIdx]);
			case 139: // Cobblestone Wall
				return safeDamage == 1 ? getByMcId("mossy_cobblestone_wall") : getByMcId("cobblestone_wall");
			case 155: // Quartz Block
				return switch (safeDamage) {
					case 1 -> getByMcId("chiseled_quartz_block");
					case 2 -> getByMcId("quartz_pillar");
					default -> getByMcId("quartz_block");
				};
			case 159: // Terracotta / Stained Clay
				if (safeDamage < COLOR_NAMES.length) {
					return getByMcId(COLOR_NAMES[safeDamage] + "_terracotta");
				}
				return getByMcId("terracotta");
			case 160: // Stained Glass Pane
				if (safeDamage < COLOR_NAMES.length) {
					return getByMcId(COLOR_NAMES[safeDamage] + "_stained_glass_pane");
				}
				return getByMcId("white_stained_glass_pane");
			case 161: // Leaves 2
				return (safeDamage % 2) == 1 ? getByMcId("dark_oak_leaves") : getByMcId("acacia_leaves");
			case 162: // Log 2
				return (safeDamage % 2) == 1 ? getByMcId("dark_oak_log") : getByMcId("acacia_log");
			case 168: // Prismarine
				return switch (safeDamage) {
					case 1 -> getByMcId("prismarine_bricks");
					case 2 -> getByMcId("dark_prismarine");
					default -> getByMcId("prismarine");
				};
			case 171: // Carpet
				if (safeDamage < COLOR_NAMES.length) {
					return getByMcId(COLOR_NAMES[safeDamage] + "_carpet");
				}
				return getByMcId("white_carpet");
			case 175: // Double Plants
				if (safeDamage < DOUBLE_PLANT_NAMES.length) {
					return getByMcId(DOUBLE_PLANT_NAMES[safeDamage]);
				}
				return getByMcId("sunflower");
			case 179: // Red Sandstone
				return switch (safeDamage) {
					case 1 -> getByMcId("chiseled_red_sandstone");
					case 2 -> getByMcId("cut_red_sandstone");
					default -> getByMcId("red_sandstone");
				};
			case 263: // Coal
				return safeDamage == 1 ? getByMcId("charcoal") : getByMcId("coal");
			case 322: // Golden Apple
				return safeDamage == 1 ? getByMcId("enchanted_golden_apple") : getByMcId("golden_apple");
			case 349: // Raw Fish
				return switch (safeDamage) {
					case 1 -> getByMcId("salmon");
					case 2 -> getByMcId("tropical_fish");
					case 3 -> getByMcId("pufferfish");
					default -> getByMcId("cod");
				};
			case 350: // Cooked Fish
				return safeDamage == 1 ? getByMcId("cooked_salmon") : getByMcId("cooked_cod");
			case 351: // Dye
				if (safeDamage < DYE_NAMES.length) {
					return getByMcId(DYE_NAMES[safeDamage]);
				}
				return getByMcId("ink_sac");
			case 373: // Potion
				return Items.POTION;
			case 383: // Spawn Egg
				return Items.PIG_SPAWN_EGG;
			case 397: // Skull
				return switch (safeDamage) {
					case 0 -> getByMcId("skeleton_skull");
					case 1 -> getByMcId("wither_skeleton_skull");
					case 2 -> getByMcId("zombie_head");
					case 3 -> getByMcId("player_head");
					case 4 -> getByMcId("creeper_head");
					case 5 -> getByMcId("dragon_head");
					default -> getByMcId("player_head");
				};
			default:
				return SIMPLE_MAP.getOrDefault(numId, Items.AIR);
		}
	}
}