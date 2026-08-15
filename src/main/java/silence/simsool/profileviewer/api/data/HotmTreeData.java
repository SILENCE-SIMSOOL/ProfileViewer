package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HotmTreeData {

	public enum NodeType {
		CORE, ABILITY, PERK, TIER
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

	public static class HotmNode {
		public String id;
		public String name;
		public NodeType type;
		public int x, y; // 0..8 (x=0..6 for nodes, x=tier for tier bar)
		public int tier; // 1..10
		public int maxLevel;
		public PowderType powder;
		public String description;

		public HotmNode(String id, String name, NodeType type, int x, int y, int tier, int maxLevel, PowderType powder, String description) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.x = x;
			this.y = y;
			this.tier = tier;
			this.maxLevel = maxLevel;
			this.powder = powder;
			this.description = description;
		}

		public ItemStack getItemIcon(int currentLevel, boolean isSelectedAbility) {
			if (type == NodeType.TIER) {
				return currentLevel >= tier ? new ItemStack(Items.EMERALD) : (currentLevel == tier - 1 ? new ItemStack(Items.GOLD_INGOT) : new ItemStack(Items.REDSTONE));
			}
			if (type == NodeType.CORE) {
				return currentLevel >= maxLevel ? new ItemStack(Items.DIAMOND_BLOCK) : (currentLevel > 0 ? new ItemStack(Items.RAW_COPPER_BLOCK) : new ItemStack(Items.BEDROCK));
			}
			if (type == NodeType.ABILITY) {
				if (isSelectedAbility) return new ItemStack(Items.EMERALD_BLOCK);
				return currentLevel > 0 ? new ItemStack(Items.REDSTONE_BLOCK) : new ItemStack(Items.COAL_BLOCK);
			}
			// Perk
			if (currentLevel >= maxLevel) return new ItemStack(Items.DIAMOND);
			if (currentLevel > 0) return new ItemStack(Items.EMERALD);
			return new ItemStack(Items.COAL);
		}
	}

	public static final List<HotmNode> ALL_NODES = new ArrayList<>();
	public static final Map<String, HotmNode> NODES_BY_ID = new HashMap<>();

	static {
		// Tier Nodes (Left bar, y: 1..10)
		for (int t = 1; t <= 10; t++) {
			addNode(new HotmNode("tier_" + t, "Heart of the Mountain " + t, NodeType.TIER, 0, t - 1, t, 1, PowderType.FREE, "Unlocks Tier " + t + " perks and rewards."));
		}

		// Tier 1 (y = 0)
		addNode(new HotmNode("mining_speed", "Mining Speed", NodeType.PERK, 3, 0, 1, 50, PowderType.MITHRIL, "+%d Mining Speed."));

		// Tier 2 (y = 1)
		addNode(new HotmNode("mining_fortune", "Mining Fortune", NodeType.PERK, 3, 1, 2, 50, PowderType.MITHRIL, "+%d Mining Fortune."));
		addNode(new HotmNode("quick_forge", "Quick Forge", NodeType.PERK, 2, 1, 2, 20, PowderType.MITHRIL, "Decreases the time it takes to forge items by %d%%."));
		addNode(new HotmNode("titanium_insanium", "Titanium Insanium", NodeType.PERK, 4, 1, 2, 50, PowderType.MITHRIL, "+%d%% chance to spawn Titanium when mining Mithril."));
		addNode(new HotmNode("mining_speed_boost", "Mining Speed Boost", NodeType.ABILITY, 1, 1, 2, 1, PowderType.FREE, "Grants +300% Mining Speed for 15 seconds."));
		addNode(new HotmNode("pickobulus", "Pickobulus", NodeType.ABILITY, 5, 1, 2, 1, PowderType.FREE, "Throws a pickaxe that explodes blocks in a 3-block radius."));

		// Tier 3 (y = 2)
		addNode(new HotmNode("luck_of_the_cave", "Luck of the Cave", NodeType.PERK, 3, 2, 3, 45, PowderType.MITHRIL, "+%d%% chance to trigger Mithril Powder Powder Orbs."));
		addNode(new HotmNode("daily_powder", "Daily Powder", NodeType.PERK, 2, 2, 3, 100, PowderType.MITHRIL, "+%d Powder from the first node of each type mined each day."));
		addNode(new HotmNode("crystallized", "Crystallized", NodeType.PERK, 4, 2, 3, 30, PowderType.MITHRIL, "+%d Mining Speed and Fortune in Crystal Hollows."));

		// Tier 4 (y = 3)
		addNode(new HotmNode("efficient_miner", "Efficient Miner", NodeType.PERK, 3, 3, 4, 100, PowderType.MITHRIL, "+%d%% chance to mine adjacent blocks."));
		addNode(new HotmNode("front_loaded", "Front Loaded", NodeType.PERK, 2, 3, 4, 1, PowderType.MITHRIL, "Grants 3x Gemstone Powder and +150 Mining Fortune for first 2,500 Gemstones."));
		addNode(new HotmNode("sky_mall", "Sky Mall", NodeType.PERK, 4, 3, 4, 1, PowderType.MITHRIL, "Every SkyBlock day, gain a random mining buff."));
		addNode(new HotmNode("maniac_miner", "Maniac Miner", NodeType.ABILITY, 1, 3, 4, 1, PowderType.FREE, "Consumes Mana to grant massive Mining Speed."));
		addNode(new HotmNode("sheer_force", "Sheer Force", NodeType.ABILITY, 5, 3, 4, 1, PowderType.FREE, "Grants +%d Mining Spread."));

		// Tier 5 (y = 4)
		addNode(new HotmNode("peak_of_the_mountain", "Peak of the Mountain", NodeType.CORE, 3, 4, 5, 10, PowderType.FREE, "Increases maximum token counts, forge slots, and commissions."));
		addNode(new HotmNode("daily_grind", "Daily Grind", NodeType.PERK, 2, 4, 5, 100, PowderType.GEMSTONE, "+%d Gemstone Powder from first Gemstone mined each day."));
		addNode(new HotmNode("goblin_killer", "Goblin Killer", NodeType.PERK, 4, 4, 5, 1, PowderType.GEMSTONE, "Slain Goblins drop 2x Powder."));

		// Tier 6 (y = 5)
		addNode(new HotmNode("mole", "Mole", NodeType.PERK, 3, 5, 6, 190, PowderType.GEMSTONE, "+%d%% chance to dig out surrounding blocks in Crystal Hollows."));
		addNode(new HotmNode("professional", "Professional", NodeType.PERK, 2, 5, 6, 140, PowderType.GEMSTONE, "+%d Mining Speed when mining Gemstones."));
		addNode(new HotmNode("fortunate", "Fortunate", NodeType.PERK, 4, 5, 6, 20, PowderType.GEMSTONE, "+%d Mining Fortune when mining Gemstones."));
		addNode(new HotmNode("anomalous_desire", "Anomalous Desire", NodeType.ABILITY, 1, 5, 6, 1, PowderType.FREE, "Spawns 5 rare ores around you."));
		addNode(new HotmNode("hazardous_miner", "Hazardous Miner", NodeType.ABILITY, 5, 5, 6, 1, PowderType.FREE, "Increases heat reduction and grants buffs in Magma Fields."));

		// Tier 7 (y = 6)
		addNode(new HotmNode("powder_buff", "Powder Buff", NodeType.PERK, 3, 6, 7, 50, PowderType.GEMSTONE, "+%d%% Powder gained from all sources."));
		addNode(new HotmNode("great_explorer", "Great Explorer", NodeType.PERK, 2, 6, 7, 20, PowderType.GEMSTONE, "+%d%% chance to find Treasure Chests in Crystal Hollows."));
		addNode(new HotmNode("lonesome_miner", "Lonesome Miner", NodeType.PERK, 4, 6, 7, 45, PowderType.GEMSTONE, "+%d Mining Stats while alone in Crystal Hollows."));

		// Tier 8 (y = 7)
		addNode(new HotmNode("mining_master", "Mining Master", NodeType.PERK, 3, 7, 8, 10, PowderType.GLACITE, "+%d Pristine and Glacite Fortune."));
		addNode(new HotmNode("mineshaft_mayhem", "Mineshaft Mayhem", NodeType.PERK, 2, 7, 8, 1, PowderType.GLACITE, "Grants buffs when exploring Glacite Mineshafts."));
		addNode(new HotmNode("miners_blessing", "Miner's Blessing", NodeType.PERK, 4, 7, 8, 50, PowderType.GLACITE, "+%d Glacite Powder gained."));

		// Tier 9 (y = 8)
		addNode(new HotmNode("glacite_fortune", "Glacite Fortune", NodeType.PERK, 3, 8, 9, 50, PowderType.GLACITE, "+%d Glacite Fortune."));
		addNode(new HotmNode("cold_efficiency", "Cold Efficiency", NodeType.PERK, 2, 8, 9, 50, PowderType.GLACITE, "+%d Mining Speed in Glacite Tunnels."));
		addNode(new HotmNode("crystallurgy", "Crystallurgy", NodeType.PERK, 4, 8, 9, 30, PowderType.GLACITE, "+%d Glacite stats."));

		// Tier 10 (y = 9)
		addNode(new HotmNode("dead_mans_chest", "Dead Man's Chest", NodeType.PERK, 3, 9, 10, 50, PowderType.GLACITE, "Corpse loot quality and quantity increased by +%d%%."));
		addNode(new HotmNode("subzero_mining", "Subzero Mining", NodeType.PERK, 2, 9, 10, 1, PowderType.GLACITE, "Grants immunity to cold while mining in Glacite Mineshafts."));
		addNode(new HotmNode("surveyor", "Surveyor", NodeType.PERK, 4, 9, 10, 20, PowderType.GLACITE, "+%d%% chance to locate Frozen Corpses in Glacite Mineshafts."));
	}

	private static void addNode(HotmNode node) {
		ALL_NODES.add(node);
		NODES_BY_ID.put(node.id, node);
	}
}