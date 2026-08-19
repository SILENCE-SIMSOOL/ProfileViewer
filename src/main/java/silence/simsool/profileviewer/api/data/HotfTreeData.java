package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class HotfTreeData {

	public static class HotfNode {
		public final String id;
		public final String name;
		public final int tier; // 1 to 8 (8 is top, 1 is bottom)
		public final int row;  // 0 to 7 (0 is top row = tier 8, 7 is bottom row = tier 1)
		public final int col;  // 0 to 6
		public final NodeType type;
		public final int maxLevel;
		public final String description;

		public HotfNode(String id, String name, int tier, int row, int col, NodeType type, int maxLevel, String description) {
			this.id = id;
			this.name = name;
			this.tier = tier;
			this.row = row;
			this.col = col;
			this.type = type;
			this.maxLevel = maxLevel;
			this.description = description;
		}
	}

	public enum NodeType {
		CORE,
		PERK,
		ABILITY,
		BUTTON
	}

	public static final List<HotfNode> NODES = new ArrayList<>();
	private static final Map<String, HotfNode> NODE_MAP = new HashMap<>();

	static {
		// Tier 8 (Row 0)
		addNode(new HotfNode("hotf_t8_0", "Tree Whisperer", 8, 0, 0, NodeType.BUTTON, 50, "Increases Foraging Wisdom by +1."));
		addNode(new HotfNode("hotf_t8_1", "Lumberjack Luck", 8, 0, 1, NodeType.BUTTON, 50, "Grants +0.5% chance for double drops."));
		addNode(new HotfNode("hotf_t8_2", "Forest Echo", 8, 0, 2, NodeType.BUTTON, 50, "Boosts whisper drop rates."));
		addNode(new HotfNode("hotf_t8_3", "Arborist", 8, 0, 3, NodeType.BUTTON, 50, "Increases wood gathering speed."));
		addNode(new HotfNode("hotf_t8_4", "Canopy Sight", 8, 0, 4, NodeType.BUTTON, 50, "Grants bonus Foraging Fortune."));
		addNode(new HotfNode("hotf_t8_5", "Galatea Gift", 8, 0, 5, NodeType.BUTTON, 50, "Increases rare item drop chance."));
		addNode(new HotfNode("hotf_t8_6", "Nature Blessing", 8, 0, 6, NodeType.BUTTON, 50, "Reduces foraging ability cooldowns."));

		// Tier 7 (Row 1)
		addNode(new HotfNode("hotf_t7_1", "Woodland Swiftness", 7, 1, 1, NodeType.BUTTON, 50, "Grants +1 Speed while foraging."));
		addNode(new HotfNode("hotf_t7_3", "Timber Master", 7, 1, 3, NodeType.BUTTON, 50, "Increases tree sweep width."));
		addNode(new HotfNode("hotf_t7_5", "Bark Armor", 7, 1, 5, NodeType.BUTTON, 50, "Grants +2 Defense in foraging areas."));

		// Tier 6 (Row 2)
		addNode(new HotfNode("hotf_t6_0", "Sap Collector", 6, 2, 0, NodeType.BUTTON, 50, "Collects tree sap automatically."));
		addNode(new HotfNode("hotf_t6_1", "Leaf Blower", 6, 2, 1, NodeType.BUTTON, 50, "Clears surrounding leaves instantly."));
		addNode(new HotfNode("hotf_t6_2", "Root Growth", 6, 2, 2, NodeType.BUTTON, 50, "Increases tree growth speed."));
		addNode(new HotfNode("hotf_t6_3", "Deep Forest", 6, 2, 3, NodeType.BUTTON, 50, "Grants bonus wisdom in ancient woods."));
		addNode(new HotfNode("hotf_t6_4", "Bark Harvester", 6, 2, 4, NodeType.BUTTON, 50, "Increases bark drop quantity."));
		addNode(new HotfNode("hotf_t6_5", "Spirit of Galatea", 6, 2, 5, NodeType.BUTTON, 50, "Increases foraging stats by +5%."));
		addNode(new HotfNode("hotf_t6_6", "Ancient Sprout", 6, 2, 6, NodeType.ABILITY, 1, "Spawns ancient sprouts when chopping logs."));

		// Tier 5 (Row 3)
		addNode(new HotfNode("hotf_t5_1", "Log Master", 5, 3, 1, NodeType.PERK, 50, "Grants +10 Foraging Fortune."));
		addNode(new HotfNode("hotf_t5_3", "Tree Splitting", 5, 3, 3, NodeType.PERK, 50, "Splits logs into extra materials."));
		addNode(new HotfNode("hotf_t5_5", "Forest Heartbeat", 5, 3, 5, NodeType.BUTTON, 50, "Increases sweep rate by +2%."));

		// Tier 4 (Row 4)
		addNode(new HotfNode("hotf_t4_0", "Efficient Forester", 4, 4, 0, NodeType.PERK, 50, "Decreases stamina consumption."));
		addNode(new HotfNode("hotf_t4_1", "Lumber Power", 4, 4, 1, NodeType.PERK, 50, "Increases chopping power."));
		addNode(new HotfNode("hotf_t4_2", "Forest Focus", 4, 4, 2, NodeType.PERK, 50, "Focuses swing power on larger trunks."));
		addNode(new HotfNode("hotf_t4_3", "Sweep Mastery", 4, 4, 3, NodeType.PERK, 50, "Increases axe sweep angle."));
		addNode(new HotfNode("hotf_t4_4", "Trunk Shatter", 4, 4, 4, NodeType.BUTTON, 50, "Chance to fell an entire tree in one hit."));
		addNode(new HotfNode("hotf_t4_5", "Grove Blessing", 4, 4, 5, NodeType.BUTTON, 50, "Grants passive health regen in woods."));
		addNode(new HotfNode("hotf_t4_6", "Wild Stamina", 4, 4, 6, NodeType.BUTTON, 50, "Regenerates stamina faster."));

		// Tier 3 (Row 5)
		addNode(new HotfNode("hotf_t3_1", "Woodcutting Speed", 3, 5, 1, NodeType.BUTTON, 50, "Increases Woodcutting Speed by +15."));
		addNode(new HotfNode("hotf_t3_3", "Chop Master", 3, 5, 3, NodeType.PERK, 50, "Grants +15 Foraging Fortune."));
		addNode(new HotfNode("hotf_t3_5", "Forest Walker", 3, 5, 5, NodeType.BUTTON, 50, "Walk through leaves without penalty."));

		// Tier 2 (Row 6) - Shifted right by 1 (Cols 1 to 5)
		addNode(new HotfNode("hotf_t2_0", "Maniac Slicer", 2, 6, 1, NodeType.ABILITY, 1, "Axe Ability: Consumes mana to boost axe sweep power."));
		addNode(new HotfNode("hotf_t2_1", "Swift Chopper", 2, 6, 2, NodeType.BUTTON, 50, "Grants +10 Foraging Speed."));
		addNode(new HotfNode("hotf_t2_2", "Foraging Fortune", 2, 6, 3, NodeType.PERK, 50, "Grants +1 Foraging Fortune per level."));
		addNode(new HotfNode("hotf_t2_3", "Foraging Wisdom", 2, 6, 4, NodeType.PERK, 50, "Grants +0.2 Foraging Wisdom per level."));
		addNode(new HotfNode("hotf_t2_4", "Axe Toss", 2, 6, 5, NodeType.ABILITY, 1, "Throwing your Axe has no Sweep penalty for 10s.\nCooldown: 116s"));

		// Tier 1 (Row 7) - Center column (Col 3)
		addNode(new HotfNode("center_of_the_forest", "Center of the Forest", 1, 7, 3, NodeType.CORE, 50, "Heart of the Forest core.\nUnlocks new tiers and perks."));

	}

	private static void addNode(HotfNode n) {
		NODES.add(n);
		NODE_MAP.put(n.id, n);
	}

	public static HotfNode getNode(String id) {
		return NODE_MAP.get(id);
	}

	public static HotfNode getNodeAt(int row, int col) {
		for (HotfNode n : NODES) {
			if (n.row == row && n.col == col) return n;
		}
		return null;
	}

	public static ItemStack createNodeStack(HotfNode node, int level, boolean isSelected, int hotfLevel) {
		ItemStack stack;
		boolean unlocked = level > 0 || (node.tier <= hotfLevel && node.row == 7);
		boolean isMaxed = level >= node.maxLevel;

		switch (node.type) {
			case CORE -> {
				stack = new ItemStack(isMaxed ? Items.OAK_WOOD : (unlocked ? Items.STRIPPED_OAK_WOOD : Items.STRIPPED_PALE_OAK_WOOD));
			}
			case ABILITY -> {
				if (isSelected) {
					stack = new ItemStack(Items.OAK_SAPLING);
				} else if (unlocked) {
					stack = new ItemStack(Items.CHERRY_SAPLING);
				} else {
					stack = new ItemStack(Items.PALE_OAK_SAPLING);
				}
			}
			case PERK -> {
				if (node.row == 4 && (node.col == 0 || node.col == 1)) {
					stack = new ItemStack(Items.OAK_LOG);
				} else if (node.row == 3 && node.col == 1) {
					stack = new ItemStack(Items.OAK_LOG);
				} else if (isMaxed) {
					stack = new ItemStack(Items.OAK_LOG);
				} else if (unlocked) {
					stack = new ItemStack(Items.STRIPPED_OAK_LOG);
				} else {
					stack = new ItemStack(Items.PALE_OAK_BUTTON);
				}
			}
			case BUTTON -> {
				if (unlocked) {
					stack = new ItemStack(Items.OAK_BUTTON);
				} else {
					stack = new ItemStack(Items.PALE_OAK_BUTTON);
				}
			}
			default -> stack = new ItemStack(Items.OAK_BUTTON);
		}

		String colorCode = isSelected ? "§a" : (isMaxed ? "§6" : (unlocked ? "§e" : "§c"));
		stack.set(DataComponents.CUSTOM_NAME, Component.literal(colorCode + node.name));

		List<Component> lore = new ArrayList<>();
		if (node.type == NodeType.ABILITY) {
			lore.add(Component.literal("§6Axe Ability: " + node.name));
		}
		for (String descLine : node.description.split("\n")) {
			lore.add(Component.literal("§7" + descLine));
		}

		if (node.type != NodeType.ABILITY && node.maxLevel > 1) {
			lore.add(Component.literal(""));
			lore.add(Component.literal("§7Level: §a" + level + "§7/§e" + node.maxLevel));
		}

		if (isSelected) {
			lore.add(Component.literal(""));
			lore.add(Component.literal("§a§lSELECTED"));
		}

		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}
}
