package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.profileviewer.api.nbt.ParsedItem;

public class HotmTreeData {

	public enum NodeType {
		CORE, ABILITY, PERK, UNLEVELABLE, TIER, SPACER
	}

	public enum PowderType {
		MITHRIL("Mithril", 0xFF00AA00),
		GEMSTONE("Gemstone", 0xFFFF55FF),
		GLACITE("Glacite", 0xFF55FFFF),
		FREE("Free", 0xFFFFFFFF);

		public final String name;
		public final int color;
		PowderType(String name, int color) {
			this.name = name;
			this.color = color;
		}
	}

	public interface TooltipEvaluator {
		List<String> getTooltip(int level, int treeLevel);
	}

	public static class HotmNode {
		public String id;
		public String[] aliases;
		public String name;
		public NodeType type;
		public int x, y; // location: [x, y], y=0..9
		public int maxLevel;
		public PowderType powder;
		public TooltipEvaluator evaluator;

		public HotmNode(String id, String[] aliases, String name, NodeType type, int x, int y, int maxLevel, PowderType powder, TooltipEvaluator evaluator) {
			this.id = id;
			this.aliases = aliases;
			this.name = name;
			this.type = type;
			this.x = x;
			this.y = y;
			this.maxLevel = maxLevel;
			this.powder = powder;
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
				if (isMaxed(level)) return new ItemStack(Items.DIAMOND_BLOCK);
				if (level <= 0) return new ItemStack(Items.BEDROCK);
				if (level == 1) return new ItemStack(Items.COPPER_BLOCK.weathering().unaffected());
				return new ItemStack(Items.REDSTONE_BLOCK);
			}
			if (type == NodeType.ABILITY) {
				if (matchesAbility(selectedAbility)) return new ItemStack(Items.EMERALD_BLOCK);
				if (level > 0) return new ItemStack(Items.REDSTONE_BLOCK);
				return new ItemStack(Items.COAL_BLOCK);
			}
			// PERK & UNLEVELABLE
			if (disabled) return new ItemStack(Items.REDSTONE);
			if (level < 0) return new ItemStack(Items.COAL);
			if (isMaxed(level)) return new ItemStack(Items.DIAMOND);
			return new ItemStack(Items.EMERALD);
		}

		public ItemStack getItemIcon(int level, boolean isSelectedAbility) {
			return getItemIcon(level, false, isSelectedAbility ? id : "");
		}

		public ParsedItem createParsedItem(int level, boolean isSelectedAbility) {
			return createParsedItem(level, false, isSelectedAbility ? id : "", 10);
		}

		public ParsedItem createParsedItem(int level, boolean disabled, String selectedAbility) {
			return createParsedItem(level, disabled, selectedAbility, 10);
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

			// 3. Evaluated API tooltip lines matching hotm.json
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

	public static final List<HotmNode> ALL_NODES = new ArrayList<>();
	public static final Map<String, HotmNode> NODES_BY_ID = new HashMap<>();

	static {
		// Tier 1 (y = 0)
		addNode(new HotmNode("mining_speed", null, "Mining Speed", NodeType.PERK, 3, 0, 50, PowderType.MITHRIL, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 20) + "⸕ Mining Speed<gray>.");
		}));

		// Tier 2 (y = 1)
		addNode(new HotmNode("mining_speed_boost", null, "Mining Speed Boost", NodeType.ABILITY, 1, 1, 1, PowderType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Pickaxe Ability: Mining Speed Boost <yellow><bold>RIGHT CLICK",
				"<gray>Grants <green>+" + (200 + eff * 50) + "%<gold> ⸕ Mining Speed <gray>for <green>" + (10 + eff * 5) + "s<gray>.",
				"<dark_gray>Cooldown: <green>120s"
			);
		}));
		addNode(new HotmNode("precision_mining", null, "Precision Mining", NodeType.UNLEVELABLE, 2, 1, 1, PowderType.FREE, (lvl, tl) -> List.of(
			"<gray>When Mining <gold>Ores <gray>or <dark_gray>Dwarven Metals<gray>,",
			"<gray>a particle target appears on the",
			"<gray>block that increases your <gold>⸕ Mining",
			"<gold>Speed <gray>by <green>30% <gray>when aiming at it."
		)));
		addNode(new HotmNode("mining_fortune", null, "Mining Fortune", NodeType.PERK, 3, 1, 50, PowderType.MITHRIL, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 2) + "☘ Mining Fortune<gray>.");
		}));
		addNode(new HotmNode("titanium_insanium", null, "Titanium Insanium", NodeType.PERK, 4, 1, 50, PowderType.MITHRIL, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			double rew = 2.0 + (l * 0.1);
			return List.of(
				"<gray>When mining<dark_green> Mithril Ore<gray>, you have a ",
				"<green>" + String.format("%.1f", rew) + "%<gray> chance to convert the block into",
				"<white>Titanium Ore<gray>."
			);
		}));
		addNode(new HotmNode("pickobulus", null, "Pickobulus", NodeType.ABILITY, 5, 1, 1, PowderType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Pickaxe Ability: Pickobulus <yellow><bold>RIGHT CLICK",
				"<gray>Throw your pickaxe to create an",
				"<gray>explosion mining all ores in a <green>3 <gray>block",
				"<gray>radius.",
				"<dark_gray>Cooldown: <green>" + (60 - eff * 10) + "s"
			);
		}));

		// Tier 3 (y = 2)
		addNode(new HotmNode("luck_of_the_cave", null, "Luck of the Cave", NodeType.PERK, 1, 2, 45, PowderType.MITHRIL, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Increases the chance for you to",
				"<gray>trigger rare occurrences in the",
				"<dark_green>Dwarven Mines <gray>by <green>" + (5 + l) + "%<gray>.",
				"",
				"<gray>Rare occurrences include:",
				"<gray> • <gold>Golden Goblins",
				"<gray> • <dark_purple>Fallen Stars",
				"<gray> • <gold>Powder Ghasts"
			);
		}));
		addNode(new HotmNode("efficient_miner", null, "Efficient Miner", NodeType.PERK, 3, 2, 100, PowderType.MITHRIL, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <yellow>+" + (l * 3) + "▚ Mining Spread<gray>.");
		}));
		addNode(new HotmNode("quick_forge", null, "Quick Forge", NodeType.PERK, 5, 2, 20, PowderType.MITHRIL, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			double rew = Math.min(30, 10 + (l * 0.5) + ((l / 20) * 10));
			return List.of("<gray>Decreases the time it takes to forge by <green>" + String.format("%.1f", rew) + "%<gray>.");
		}));

		// Tier 4 (y = 3)
		addNode(new HotmNode("sky_mall", null, "Sky Mall", NodeType.UNLEVELABLE, 0, 3, 1, PowderType.FREE, (lvl, tl) -> List.of(
			"<gray>Every Skyblock day, you receive a",
			"<gray>random buff while on any <aqua>Mining",
			"<aqua>Island<gray>.",
			"",
			"<gray>Possible Buffs",
			"<dark_gray> ■ <gray>Gain <gold>+100⸕ Mining Speed<gray>.",
			"<dark_gray> ■ <gray>Gain <gold>+50☘ Mining Fortune<gray>.",
			"<dark_gray> ■ <gray>Gain <green>+15% <gray>more Powder while mining.",
			"<dark_gray> ■ <green>-20% <gray>Pickaxe Ability cooldowns.",
			"<dark_gray> ■ <green>-20% <gray>chance to find <gold>Golden <gray>and",
			"    <aqua>Diamond Goblins<gray>.",
			"<dark_gray> ■ <green>Gain <green>5x <blue>Titanium <gray>drops."
		)));
		addNode(new HotmNode("old_school", null, "Old-School", NodeType.PERK, 1, 3, 20, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 5) + "☘ Ore Fortune<gray>.");
		}));
		addNode(new HotmNode("professional", null, "Professional", NodeType.PERK, 2, 3, 140, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain <gold>+" + (50 + l * 5) + "⸕ Mining Speed<gray> when mining",
				"<gray>Gemstones."
			);
		}));
		addNode(new HotmNode("mole", null, "Mole", NodeType.PERK, 3, 3, 200, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			double rew = 50 + ((l - 1) * (350.0 / 199.0));
			return List.of(
				"<gray>Grants <yellow>+" + (int) Math.round(rew) + "▚ Mining Spread <gray>when",
				"<gray>mining Hard Stone."
			);
		}));
		addNode(new HotmNode("gem_lover", null, "Gem Lover", NodeType.PERK, 4, 3, 20, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (20 + l * 4) + "☘ Gemstone Fortune<gray>.");
		}));
		addNode(new HotmNode("seasoned_mineman", null, "Seasoned Mineman", NodeType.PERK, 5, 3, 100, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			double rew = 5 + (l * 0.1);
			return List.of("<gray>Grants <dark_aqua>" + String.format("%.1f", rew) + "☯ Mining Wisdom<gray>.");
		}));
		addNode(new HotmNode("front_loaded", null, "Front Loaded", NodeType.UNLEVELABLE, 6, 3, 1, PowderType.FREE, (lvl, tl) -> List.of(
			"<gray>Grants the following buffs for the",
			"<gray>first <green>2,500 <light_purple>Gemstones <gray> you mine each",
			"<gray>day.",
			"",
			"<dark_gray> ■ <light_purple>3x Gemstone Powder",
			"<dark_gray> ■ <gold>+150☘ Gemstone Fortune",
			"<dark_gray> ■ <gold>+250⸕ Mining Speed"
		)));

		// Tier 5 (y = 4)
		addNode(new HotmNode("daily_grind", null, "Daily Grind", NodeType.UNLEVELABLE, 1, 4, 1, PowderType.FREE, (lvl, tl) -> {
			int rew = Math.max(1, tl) * 500;
			return List.of(
				"<gray>Your first daily commission on each",
				"<aqua>Mining Island <gray>grants <blue>+500 Powder<gray>,",
				"<gray>multiplied by your <purple>HOTM <gray>level.",
				"",
				"<dark_green>Dwarven Mines<gray>: <green>" + rew + " <dark_green>Mithril Powder",
				"<dark_purple>Crystal Hollows<gray>: <green>" + rew + " <light_purple>Gemstone Powder",
				"<aqua>Glacite Mines<gray>: <green>" + rew + " <dark_green>Glacite Powder"
			);
		}));
		addNode(new HotmNode("core_of_the_mountain", new String[]{"peak_of_the_mountain", "special_0"}, "Core of the Mountain", NodeType.CORE, 3, 4, 10, PowderType.FREE, (lvl, tl) -> {
			List<String> list = new ArrayList<>();
			list.add("<dark_gray>+<purple>1 Token of the Mountain");
			if (lvl >= 2) list.add("<dark_gray>+<red>1 Pickaxe Ability Level");
			if (lvl >= 3) list.add("<green>+1 Commission Slot");
			if (lvl >= 4) list.add("<dark_gray>+<dark_green>1 Base Mithril Powder <gray>when mining <dark_green>Mithril");
			if (lvl >= 5) list.add("<dark_gray>+<dark_purple>2 Token of the Mountain");
			if (lvl >= 6) list.add("<dark_gray>+<light_purple>2 Base Gemstone Powder <gray>when mining <light_purple>Gemstones");
			if (lvl >= 7) list.add("<dark_gray>+<dark_purple>3 Token of the Mountain");
			if (lvl >= 8) list.add("<dark_gray>+<aqua>3 Base Glacite Powder <gray>when mining <gray>Glacite");
			if (lvl >= 9) list.add("<dark_gray>+<green>10% chance <gray>for <aqua>Glacite Mineshafts <gray>to spawn");
			if (lvl >= 10) list.add("<dark_gray>+<dark_purple>5 Token of the Mountain");
			return list;
		}));
		addNode(new HotmNode("daily_powder", null, "Daily Powder", NodeType.UNLEVELABLE, 5, 4, 1, PowderType.FREE, (lvl, tl) -> {
			int rew = Math.max(1, tl) * 500;
			return List.of(
				"<gray>The first ore you mine each day",
				"<gray>grants <blue>+500 Powder<gray>, multiplied by",
				"<gray>your <purple>HOTM <gray>level.",
				"",
				"<dark_green>Mithril<gray>: <green>" + rew + " <dark_green>Mithril Powder",
				"<light_purple>Gemstone<gray>: <green>" + rew + " <light_purple>Gemstone Powder",
				"<aqua>Glacite<gray>: <green>" + rew + " <dark_green>Glacite Powder"
			);
		}));

		// Tier 6 (y = 5)
		addNode(new HotmNode("anomalous_desire", null, "Anomalous Desire", NodeType.ABILITY, 0, 5, 1, PowderType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Pickaxe Ability: Anomalous Desire <yellow><bold>RIGHT CLICK",
				"<gray>Increases the chances of triggering",
				"<gray>rare occurrences by <green>" + (30 + eff * 10) + "% <gray>for <green>30s<gray>.",
				"",
				"<gray>Rare occurrences include:",
				"<dark_gray> ■ <gold>Golden Goblins",
				"<dark_gray> ■ <purple>Fallen Stars",
				"<dark_gray> ■ <gold>Powder Ghasts",
				"<dark_gray> ■ <gold>Worms",
				"<dark_gray> ■ <aqua>Glacite Mineshafts",
				"<dark_gray>Cooldown: <green>" + (120 - eff * 10) + "s."
			);
		}));
		addNode(new HotmNode("blockhead", null, "Blockhead", NodeType.PERK, 1, 5, 20, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 5) + "☘ Block Fortune<gray>.");
		}));
		addNode(new HotmNode("subterranean_fisher", null, "Subterranean Fisher", NodeType.PERK, 2, 5, 40, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Grants <aqua>+" + String.format("%.1f", 5 + l * 0.5) + " ☂ Fishing Speed and",
				"<dark_aqua>+" + String.format("%.1f", 1 + l * 0.1) + " α Sea Creature Chance <gray>while on",
				"<aqua>Mining Islands<gray>."
			);
		}));
		addNode(new HotmNode("keep_it_cool", null, "Keep It Cool", NodeType.PERK, 3, 5, 50, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <red>+" + String.format("%.1f", l * 0.4) + "♨ Heat Resistance<gray>.");
		}));
		addNode(new HotmNode("lonesome_miner", null, "Lonesome Miner", NodeType.PERK, 4, 5, 45, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Increases <red>❁ Strength<gray>,<blue>☣ Crit",
				"<blue>Chance<gray>, <blue>☠ Crit Damage<gray>, <green>❈ Defense",
				"<gray>and <red>❤ Health <gray>statistics gain by <green>" + String.format("%.1f", 5 + (l - 1) * 0.5) + "%",
				"<gray>while on <aqua>Mining Islands<gray>."
			);
		}));
		addNode(new HotmNode("great_explorer", null, "Great Explorer", NodeType.PERK, 5, 5, 20, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Boosts the chance to find treasure",
				"<gray>chests while mining in the <purple>Crystal",
				"<purple>Hollows <gray>by <green>+" + (20 + 4 * (l - 1)) + "% <gray>and reduces the",
				"<gray>amount of locks on the chest by <green>" + (1 + l / 5) + "<gray>."
			);
		}));
		addNode(new HotmNode("maniac_miner", null, "Maniac Miner", NodeType.ABILITY, 6, 5, 1, PowderType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			int l = Math.max(1, lvl);
			return List.of(
				"<gold>Pickaxe Ability: Maniac Miner <yellow><bold>RIGHT CLICK",
				"<gray>Grants <dark_green>+1Ⓟ Breaking Powder <gray>and a",
				"<gray>stack of <gold>+" + (l * 5) + "☘ Mining Fortune <dark_gray>(caps",
				"<dark_gray>at 1000) <gray>per block broken for <green>" + (25 + eff * 5) + "s<gray>.",
				"<gray>Each block broken consumes <aqua>20 Mana<gray>.",
				"<dark_gray>Cooldown: <green>120s"
			);
		}));

		// Tier 7 (y = 6)
		addNode(new HotmNode("speedy_mineman", null, "Speedy Mineman", NodeType.PERK, 1, 6, 50, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 40) + "⸕ Mining Speed<gray>.");
		}));
		addNode(new HotmNode("powder_buff", null, "Powder Buff", NodeType.PERK, 3, 6, 50, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Gain <green>+" + l + "% <gray>more Powder from any source.");
		}));
		addNode(new HotmNode("fortunate_mineman", null, "Fortunate Mineman", NodeType.PERK, 5, 6, 50, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 3) + "☘ Mining Fortune<gray>.");
		}));

		// Tier 8 (y = 7)
		addNode(new HotmNode("miners_blessing", null, "Miner's Blessing", NodeType.UNLEVELABLE, 0, 7, 1, PowderType.FREE, (lvl, tl) -> List.of(
			"<gray>Grants <aqua>+30✯ Magic Find <gray>on all <aqua>Mining",
			"<aqua>Islands<gray>."
		)));
		addNode(new HotmNode("no_stone_unturned", null, "No Stone Unturned", NodeType.PERK, 1, 7, 50, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Increases your chances of finding a",
				"<blue>Suspicious Scrap <gray>when mining in a",
				"<aqua>Glacite Mineshaft<gray> by <green>" + String.format("%.1f", l * 0.5) + "%<gray>."
			);
		}));
		addNode(new HotmNode("strong_arm", null, "Strong Arm", NodeType.PERK, 2, 7, 100, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain <gold>+" + (l * 5) + "⸕ Mining Speed <gray>when mining",
				"<gold>Dwarven Metals<gray>."
			);
		}));
		addNode(new HotmNode("steady_hand", null, "Steady Hand", NodeType.PERK, 3, 7, 100, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Grants <yellow>+" + String.format("%.1f", l * 0.1) + "▚ Gemstone Spread <gray>while",
				"<gray>in the <aqua>Glacite Mineshafts<gray>."
			);
		}));
		addNode(new HotmNode("warm_heart", null, "Warm Heart", NodeType.PERK, 4, 7, 50, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <aqua>+" + String.format("%.1f", l * 0.4) + "❄ Cold Resistance<gray>.");
		}));
		addNode(new HotmNode("surveyor", null, "Surveyor", NodeType.PERK, 5, 7, 20, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Increases your chances of finding a",
				"<aqua>Glacite Mineshaft <gray>when mining in the",
				"<aqua>Glacite Tunnels <gray>by <green>" + String.format("%.2f", l * 0.75) + "%<gray>."
			);
		}));
		addNode(new HotmNode("mineshaft_mayhem", null, "Mineshaft Mayhem", NodeType.UNLEVELABLE, 6, 7, 1, PowderType.FREE, (lvl, tl) -> List.of(
			"<gray>Every time you enter a <aqua>Glacite",
			"<aqua>Mineshaft<gray>, <gray>you receive a random buff.",
			"",
			"<gray>Possible Buffs",
			"<dark_gray> ■ <green>+5% <gray>chance to find a <blue>Suspicious Scrap<gray>.",
			"<dark_gray> ■ <gray>Gain <gold>+100 ☘ Mining Fortune<gray>.",
			"<dark_gray> ■ <gray>Gain <gold>+200 ⸕ Mining Speed<gray>.",
			"<dark_gray> ■ <gray>Gain <aqua>+10❄ Cold Resistance<gray>.",
			"<dark_gray> ■ <gray>Reduce Pickaxe Ability cooldowns by <green>-25%<gray>"
		)));

		// Tier 9 (y = 8)
		addNode(new HotmNode("metal_head", null, "Metal Head", NodeType.PERK, 1, 8, 20, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 5) + "☘ Dwarven Metal Fortune<gray>.");
		}));
		addNode(new HotmNode("rags_to_riches", null, "Rags to Riches", NodeType.PERK, 3, 8, 50, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 4) + "☘ Mining Fortune<gray> while in a <aqua>Glacite Mineshaft<gray>.");
		}));
		addNode(new HotmNode("eager_adventurer", null, "Eager Adventurer", NodeType.PERK, 5, 8, 100, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <gold>+" + (l * 4) + "⸕ Mining Speed<gray> while in a <aqua>Glacite Mineshaft<gray>.");
		}));

		// Tier 10 (y = 9)
		addNode(new HotmNode("gemstone_infusion", null, "Gemstone Infusion", NodeType.ABILITY, 0, 9, 1, PowderType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Pickaxe Ability: Gemstone Infusion <yellow><bold>RIGHT CLICK",
				"<gray>Increases the effectiveness of",
				"<gold>every Gemstone <gray>in your pick's",
				"<gray>Gemstone Slots by <green>100% <gray>for <green>" + (20 + eff * 5) + "s<gray>.",
				"<dark_gray>Cooldown: <green>120s"
			);
		}));
		addNode(new HotmNode("crystalline", null, "Crystalline", NodeType.PERK, 1, 9, 50, PowderType.GEMSTONE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Increases your chances of finding a",
				"<aqua>Glacite Mineshaft <gray>containing a",
				"<light_purple>Gemstone Crystal <gray>by <green>" + String.format("%.1f", l * 0.5) + "%<gray>."
			);
		}));
		addNode(new HotmNode("gifts_from_the_departed", null, "Gifts from the Departed", NodeType.PERK, 2, 9, 100, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain a <green>" + String.format("%.1f", l * 0.2) + "% <gray>chance to get an extra",
				"<gray>item when looting a <aqua>Frozen Corpse<gray>."
			);
		}));
		addNode(new HotmNode("mining_master", null, "Mining Master", NodeType.PERK, 3, 9, 10, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of("<gray>Grants <purple>+" + String.format("%.1f", l * 0.1) + "✧ Pristine<gray>.");
		}));
		addNode(new HotmNode("dead_mans_chest", null, "Dead Man's Chest", NodeType.PERK, 4, 9, 50, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Gain a <green>" + l + "% <gray>chance to spawn <green>1",
				"<gray>additional <aqua>Frozen Corpse <gray>when you",
				"<gray>enter a <aqua>Glacite Mineshaft<gray>."
			);
		}));
		addNode(new HotmNode("vanguard_seeker", null, "Vanguard Seeker", NodeType.PERK, 5, 9, 50, PowderType.GLACITE, (lvl, tl) -> {
			int l = Math.max(1, lvl);
			return List.of(
				"<gray>Increases your chances of finding a",
				"<aqua>Glacite Mineshaft <gray>containing a",
				"<white>Vanguard Corpse<gray> by <green>" + l + "%<gray>."
			);
		}));
		addNode(new HotmNode("sheer_force", null, "Sheer Force", NodeType.ABILITY, 6, 9, 1, PowderType.FREE, (lvl, tl) -> {
			int eff = Math.max(0, lvl - 1);
			return List.of(
				"<gold>Pickaxe Ability: Sheer Force <yellow><bold>RIGHT CLICK",
				"<gray>Grants <yellow>+200▚ Mining Spread <gray>for <green>" + (20 + eff * 5) + "s<gray>.",
				"<dark_gray>Cooldown: <green>120s"
			);
		}));
	}

	private static void addNode(HotmNode node) {
		ALL_NODES.add(node);
		NODES_BY_ID.put(node.id, node);
	}
}