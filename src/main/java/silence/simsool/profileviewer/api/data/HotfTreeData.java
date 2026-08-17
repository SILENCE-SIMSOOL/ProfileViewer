package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public class HotfTreeData {

	public enum NodeType {
		CORE, ABILITY, PERK, UNLEVELABLE, TIER, SPACER
	}

	public enum WhisperType {
		FOREST("Forest", 0xFF00AA00),
		DESERT("Desert", 0xFFFFAA00),
		FREE("Free", 0xFFFFFFFF);

		public final String name;
		public final int color;
		WhisperType(String name, int color) {
			this.name = name;
			this.color = color;
		}
	}

	public interface TooltipEvaluator {
		List<String> getTooltip(int level, int treeLevel);
	}

	public static class HotfNode {
		public String id;
		public String[] aliases;
		public String name;
		public NodeType type;
		public int x, y; // location: [x, y], y=0..6
		public int maxLevel;
		public WhisperType whisper;
		public TooltipEvaluator evaluator;

		public HotfNode(String id, String[] aliases, String name, NodeType type, int x, int y, int maxLevel, WhisperType whisper, TooltipEvaluator evaluator) {
			this.id = id;
			this.aliases = aliases;
			this.name = name;
			this.type = type;
			this.x = x;
			this.y = y;
			this.maxLevel = maxLevel;
			this.whisper = whisper;
			this.evaluator = evaluator;
		}

		public boolean isMaxed(int level) {
			if (type == NodeType.UNLEVELABLE) return level > 0;
			return maxLevel > 0 && level >= maxLevel;
		}

		public int getNodeLevel(Map<String, Integer> activeNodes) {
			if (activeNodes == null) return -1;
			if (activeNodes.containsKey(id)) return activeNodes.get(id);
			if (aliases != null) {
				for (String alias : aliases) {
					if (activeNodes.containsKey(alias)) return activeNodes.get(alias);
				}
			}
			return -1;
		}

		public boolean matchesAbility(String abilityId) {
			if (abilityId == null || abilityId.isEmpty()) return false;
			if (id.equalsIgnoreCase(abilityId)) return true;
			if (aliases != null) {
				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(abilityId)) return true;
				}
			}
			return false;
		}

		public ItemStack getItemIcon(int level, boolean disabled, String selectedAbility) {
			if (type == NodeType.TIER) {
				return isMaxed(level) ? new ItemStack(Items.EMERALD) : (isMaxed(level + 1) ? new ItemStack(Items.GOLD_INGOT) : new ItemStack(Items.REDSTONE));
			}
			if (type == NodeType.CORE) {
				if (isMaxed(level)) return new ItemStack(Items.OAK_WOOD);
				if (level <= 0) return new ItemStack(Items.STRIPPED_PALE_OAK_WOOD);
				if (level == 1) return new ItemStack(Items.STRIPPED_BIRCH_WOOD);
				return new ItemStack(Items.STRIPPED_OAK_WOOD);
			}
			if (type == NodeType.ABILITY) {
				if (matchesAbility(selectedAbility)) return new ItemStack(Items.OAK_SAPLING);
				if (level > 0) return new ItemStack(Items.CHERRY_SAPLING);
				return new ItemStack(Items.PALE_OAK_SAPLING);
			}
			// PERK & UNLEVELABLE
			if (disabled) return new ItemStack(Items.STRIPPED_MANGROVE_LOG);
			if (level < 0) return new ItemStack(Items.PALE_OAK_BUTTON);
			if (isMaxed(level)) return new ItemStack(Items.OAK_LOG);
			return new ItemStack(Items.STRIPPED_OAK_LOG);
		}

		public ItemStack getItemIcon(int level, boolean isSelectedAbility) {
			return getItemIcon(level, false, isSelectedAbility ? id : "");
		}

		public ParsedItem createParsedItem(int level, boolean isSelectedAbility) {
			return createParsedItem(level, false, isSelectedAbility ? id : "", 8);
		}

		public ParsedItem createParsedItem(int level, boolean disabled, String selectedAbility) {
			return createParsedItem(level, disabled, selectedAbility, 8);
		}

		public ParsedItem createParsedItem(int level, boolean disabled, String selectedAbility, int treeLevel) {
			ParsedItem pi = new ParsedItem();
			pi.itemStack = getItemIcon(level, disabled, selectedAbility);
			pi.count = (level > 1 && type != NodeType.TIER && type != NodeType.UNLEVELABLE) ? level : 1;

			// 1. Title matching SkillTreeScreen.kt
			if (disabled || level == -1) {
				pi.displayName = "§c" + name;
			} else {
				pi.displayName = "§a" + name;
			}

			pi.lore = new ArrayList<>();

			// 2. Level info matching SkillTreeScreen.kt
			if (type == NodeType.PERK || type == NodeType.CORE) {
				if (level >= maxLevel) {
					pi.lore.add("§7Level " + level);
				} else {
					int curLvl = Math.max(0, level);
					pi.lore.add("§7Level " + curLvl + "/§8" + maxLevel);
				}
				pi.lore.add("");
			}

			// 3. Evaluated API tooltip lines matching hotf.json
			if (evaluator != null) {
				List<String> lines = evaluator.getTooltip(level, treeLevel);
				if (lines != null) {
					for (String line : lines) {
						pi.lore.add(formatTags(line));
					}
				}
			}

			// 4. Status matching SkillTreeScreen.kt
			if (type == NodeType.ABILITY) {
				if (!disabled && level > 0) {
					pi.lore.add("");
					pi.lore.add("§a§lSELECTED");
				}
			} else if (type != NodeType.CORE && level > 0) {
				pi.lore.add("");
				if (disabled) {
					pi.lore.add("§c§lDISABLED");
				} else {
					pi.lore.add("§a§lENABLED");
				}
			}

			return pi;
		}
	}

	public static String formatTags(String text) {
		if (text == null) return "";
		return text
			.replace("<black>", "§0")
			.replace("<dark_blue>", "§1")
			.replace("<dark_green>", "§2")
			.replace("<dark_aqua>", "§3")
			.replace("<dark_red>", "§4")
			.replace("<dark_purple>", "§5")
			.replace("<gold>", "§6")
			.replace("<gray>", "§7")
			.replace("<dark_gray>", "§8")
			.replace("<blue>", "§9")
			.replace("<green>", "§a")
			.replace("<aqua>", "§b")
			.replace("<red>", "§c")
			.replace("<light_purple>", "§d")
			.replace("<purple>", "§d")
			.replace("<yellow>", "§e")
			.replace("<white>", "§f")
			.replace("<bold>", "§l")
			.replace("<strikethrough>", "§m")
			.replace("<underline>", "§n")
			.replace("<italic>", "§o")
			.replace("<reset>", "§r")
			.replaceAll("</[a-zA-Z0-9_]+>", "§r");
	}

	public static final List<HotfNode> ALL_NODES = new ArrayList<>();
	public static final Map<String, HotfNode> NODES_BY_ID = new HashMap<>();

	static {
		// Tier 1 (y = 0)
		addNode(new HotfNode("sweep", null, "Sweep", NodeType.PERK, 3, 0, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Increase <dark_green>∮ Sweep</dark_green> by <green>" + l + "</green>.</gray>");
		}));

		// Tier 2 (y = 1)
		addNode(new HotfNode("damage_boost", null, "Damage Boost", NodeType.ABILITY, 1, 1, 1, WhisperType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Axe Ability: Damage Boost</gold>",
				"<gray>Your axe deals double </gray><red>❁ Damage </red><gray>to</gray>",
				"<gray>creatures on </gray><dark_green>Galatea </dark_green><gray>for </gray><green>10s</green><gray>.</gray>",
				"<dark_gray>Cooldown: </dark_gray><green>" + (120 - eff * 5) + "s</green>"
			);
		}));
		addNode(new HotfNode("strength_boost", null, "Strength Boost", NodeType.PERK, 2, 1, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Increase <red>❁ Strength</red> by <green>" + (l * 2) + "</green> while on</gray><dark_green>Foraging Islands</dark_green><gray>.</gray>");
		}));
		addNode(new HotfNode("foraging_fortune", null, "Foraging Fortune", NodeType.PERK, 3, 1, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Increase </gray><gold>☘ Foraging Fortune </gold><gray>by </gray><green>" + (l * 3) + "</green><gray>.</gray>");
		}));
		addNode(new HotfNode("speed_boost", null, "Speed Boost", NodeType.PERK, 4, 1, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Increase <white>✦ Speed </white>by <green>" + l + "</green> while on</gray><dark_green>Foraging Islands</dark_green><gray>.</gray>");
		}));
		addNode(new HotfNode("axe_toss", null, "Axe Toss", NodeType.ABILITY, 5, 1, 1, WhisperType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Axe Ability: Axe Toss</gold>",
				"<gray>Throwing your Axe has no </gray><dark_green>∮ Sweep</dark_green>",
				"<gray>penalty for </gray><green>10s</green><gray>.</gray>",
				"<dark_gray>Cooldown: </dark_gray><green>" + (120 - eff * 4) + "s</green>"
			);
		}));

		// Tier 3 (y = 2)
		addNode(new HotfNode("luck_of_the_forest", null, "Luck of the Forest", NodeType.PERK, 1, 2, 40, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Tree Gifts grant </gray><green>" + String.format("%.1f", l * 0.5) + "% </green><gray>more loot.</gray>");
		}));
		addNode(new HotfNode("daily_wishes", null, "Daily Wishes", NodeType.PERK, 3, 2, 100, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			int rew = l * 400;
			return List.of(
				"<gray>You gain </gray><green>" + rew + " </green><dark_aqua>Forest Whispers </dark_aqua><gray>from</gray>",
				"<gray>the first type of log you cut on</gray>",
				"<dark_green>Galatea </dark_green><gray>every day.</gray>",
				"",
				"<dark_gray><strikethrough>Fig Log: +" + rew + " Forest Whispers</strikethrough></dark_gray>",
				"<yellow>Mangrove Log</yellow><gray>: </gray><green>+" + rew + " </green><dark_aqua>Forest Whispers</dark_aqua>"
			);
		}));
		addNode(new HotfNode("250_gifts", null, "250 Gifts", NodeType.PERK, 5, 2, 40, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Your first </gray><green>250 </green><gray>Tree Gifts of the day</gray>",
				"<gray>grant </gray><green>" + l + "% </green><gray>more loot and </gray><green>20 </green><gray>extra</gray>",
				"<dark_aqua>Forest Whispers</dark_aqua><gray>.</gray>"
			);
		}));

		// Tier 4 (y = 3)
		addNode(new HotfNode("lottery", null, "Lottery", NodeType.UNLEVELABLE, 0, 3, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>Every SkyBlock day, you receive a</gray>",
			"<gray>random effect.</gray>",
			"",
			"<gray>Possible Buffs</gray>",
			"<dark_gray> ■ </dark_gray><gray>Gain </gray><green>+50 </green><gold>☘ Fig Fortune</gold><gray>.</gray>",
			"<dark_gray> ■ </dark_gray><gray>Gain </gray><green>+50 </green><gold>☘ Mangrove Fortune</gold><gray>.</gray>",
			"<dark_gray> ■ </dark_gray><gray>Gain </gray><green>+5% </green><dark_green>∮ Sweep</dark_green><gray>.</gray>"
		)));
		addNode(new HotfNode("foraging_madness", null, "Foraging Madness", NodeType.UNLEVELABLE, 1, 3, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>Increase </gray><dark_green>∮ Sweep </dark_green><gray>by </gray><green>10</green><gray> and </gray><gold>☘</gold>",
			"<gold>Foraging Fortune </gold><gray>by </gray><green>50</green><gray>.</gray>"
		)));
		addNode(new HotfNode("deep_waters", null, "Deep Waters", NodeType.PERK, 2, 3, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>You gain </gray><green>" + l + " </green><blue>❍ Pressure Resistance</blue><gray>.</gray>");
		}));
		addNode(new HotfNode("efficient_forager", null, "Efficient Forager", NodeType.PERK, 3, 3, 100, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants </gray><dark_aqua>" + String.format("%.1f", 5 + l * 0.1) + "☯ Foraging Wisdom</dark_aqua><gray>.</gray>");
		}));
		addNode(new HotfNode("collector", null, "Collector", NodeType.PERK, 4, 3, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Berries and Island Resources have</gray>",
				"<gray>a </gray><green>" + (l * 2) + "% </green><gray>chance to drop double</gray>",
				"<gray>resource.</gray>"
			);
		}));
		addNode(new HotfNode("early_bird", null, "Early Bird", NodeType.UNLEVELABLE, 5, 3, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>Increase </gray><dark_green>∮ Sweep </dark_green><gray>by </gray><green>20</green><gray> and </gray><gold>☘</gold>",
			"<gold>Foraging Fortune </gold><gray>by </gray><green>100</green><gray> for the</gray>",
			"<gray>first </gray><green>250 </green><gray>trees cut every day.</gray>"
		)));
		addNode(new HotfNode("precision_cutting", null, "Precision Cutting", NodeType.UNLEVELABLE, 6, 3, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>A particle appears on every nearby</gray>",
			"<gray>tree. Cutting the marked log grants</gray>",
			"<gray>you a </gray><green>+10</green><gray> </gray><dark_green>∮ Sweep </dark_green><gray>on that hit.</gray>"
		)));

		// Tier 5 (y = 4)
		addNode(new HotfNode("monster_hunter", null, "Monster Hunter", NodeType.UNLEVELABLE, 1, 4, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>Hunting a monster grants </gray><green>+40 </green><gray>extra</gray>",
			"<dark_aqua>Forest Whispers</dark_aqua><gray>.</gray>"
		)));
		addNode(new HotfNode("center_of_the_forest", new String[]{"core_of_the_forest"}, "Center of the Forest", NodeType.CORE, 3, 4, 5, WhisperType.FREE, (lvl, tl) -> {
			List<String> list = new ArrayList<>();
			list.add("<dark_gray>+</dark_gray><red>1 Axe Ability Level</red>");
			list.add("<dark_gray>+</dark_gray><green>1 Token of the Forest</green>");
			if (lvl >= 2) list.add("<dark_gray>+</dark_gray><dark_green>5% ∮ Sweep</dark_green><gray>.</gray>");
			if (lvl >= 3) {
				list.add("<dark_gray>+</dark_gray><dark_aqua>20 Forest Whispers </dark_aqua><gray>per Tree Gift.</gray>");
				list.add("<dark_gray>+</dark_gray><dark_aqua>2 Forest Whispers </dark_aqua><gray>per Fig/Mangrove logs cut.</gray>");
			}
			if (lvl >= 4) list.add("<dark_gray>+</dark_gray><dark_green>10% ∮ Sweep</dark_green><gray>.</gray>");
			if (lvl >= 5) {
				list.add("<dark_gray>+</dark_gray><red>1 Axe Ability Level</red>");
				list.add("<dark_gray>+</dark_gray><green>2 Token of the Forest</green>");
			}
			return list;
		}));
		addNode(new HotfNode("tree_whisperer", null, "Tree Whisperer", NodeType.UNLEVELABLE, 5, 4, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>Tree Gifts give </gray><green>+200 </green><dark_aqua>Forest Whispers</dark_aqua><gray>.</gray>"
		)));

		// Tier 6 (y = 5)
		addNode(new HotfNode("homing_axe", null, "Homing Axe", NodeType.UNLEVELABLE, 0, 5, 1, WhisperType.FREE, (lvl, tl) -> List.of(
			"<gray>Your Throwing Axes gain permanent</gray>",
			"<gray>homing abilities towards trees on</gray>",
			"<dark_green>Galatea</dark_green><gray>.</gray>"
		)));
		addNode(new HotfNode("forest_strength", null, "Forest Strength", NodeType.PERK, 1, 5, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain </gray><green>" + String.format("%.1f", l * 0.1) + "% </green><gray>of your total </gray><red>❁ Strength</red>",
				"<gray>as </gray><gold>☘ Foraging Fortune </gold><gray>and </gray><dark_green>∮</dark_green>",
				"<dark_green>Sweep</dark_green><gray>. </gray><dark_gray>(Caps at 1,000 Strength).</dark_gray>"
			);
		}));
		addNode(new HotfNode("hunters_luck", null, "Hunter's Luck", NodeType.PERK, 2, 5, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>You gain </gray><green>" + l + " </green><light_purple>☘ Hunter Fortune</light_purple><gray>.</gray>");
		}));
		addNode(new HotfNode("galateas_might", null, "Galatea's Might", NodeType.PERK, 3, 5, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Gain </gray><green>" + String.format("%.1f", l * 0.5) + "% </green><gray>Combat Stats on </gray><dark_green>Galatea</dark_green><gray>.</gray>");
		}));
		addNode(new HotfNode("essence_fortune", null, "Essence Fortune", NodeType.PERK, 4, 5, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Every time you obtain </gray><light_purple>Forest</light_purple>",
				"<light_purple>Essence</light_purple><gray>, there is a </gray><green>" + String.format("%.1f", l * 0.5) + "%</green><gray> chance for",
				"<gray>it to double.</gray>"
			);
		}));
		addNode(new HotfNode("forest_speed", null, "Forest Speed", NodeType.PERK, 5, 5, 50, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain </gray><green>" + String.format("%.1f", l * 0.2) + "% </green><gray>of your total </gray><white>✦ Speed </white><gray>as</gray>",
				"<gold>☘ Foraging Fortune </gold><gray>and </gray><dark_green>∮ Sweep</dark_green><gray>.</gray>",
				"<dark_gray>(Caps at 500 Speed).</dark_gray>"
			);
		}));
		addNode(new HotfNode("maniac_slicer", null, "Maniac Slicer", NodeType.ABILITY, 6, 5, 1, WhisperType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Axe Ability: Maniac Slicer</gold>",
				"<gray>Throwing your axe consumes </gray><green>100%</green>",
				"<gray>of your total mana. You gain </gray><dark_green>1∮</dark_green>",
				"<dark_green>Sweep </dark_green><gray>for every </gray><aqua>100 Mana </aqua><gray>used, for</gray>",
				"<green>" + (15 + eff * 5) + " </green><gray>seconds.</gray>",
				"<dark_gray>Cooldown: </dark_gray><green>" + (60 - eff * 2) + "s</green>"
			);
		}));

		// Tier 7 (y = 6)
		addNode(new HotfNode("half_empty", null, "Half Empty", NodeType.PERK, 1, 6, 25, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain </gray><green>" + (l * 2) + " </green><gold>☘ Foraging Fortune </gold><gray>and </gray><green>" + l + " </green><dark_green>∮</dark_green>",
				"<dark_green>Sweep </dark_green><gray>when within </gray><green>16 </green><gray>Blocks of a</gray>",
				"<gray>player with Half Full enabled.</gray>"
			);
		}));
		addNode(new HotfNode("ricochet", null, "Ricochet", NodeType.PERK, 3, 6, 10, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Your Axe has a </gray><green>" + l + "% </green><gray>chance to bounce</gray>",
				"<gray>to a nearby tree when thrown.</gray>"
			);
		}));
		addNode(new HotfNode("half_full", null, "Half Full", NodeType.PERK, 5, 6, 25, WhisperType.FOREST, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain </gray><green>" + (l * 2) + " </green><gold>☘ Foraging Fortune </gold><gray>and </gray><green>" + l + " </green><dark_green>∮</dark_green>",
				"<dark_green>Sweep </dark_green><gray>when within </gray><green>16 </green><gray>Blocks of a</gray>",
				"<gray>player with Half Empty enabled.</gray>"
			);
		}));
	}

	private static void addNode(HotfNode node) {
		ALL_NODES.add(node);
		NODES_BY_ID.put(node.id, node);
	}
}
