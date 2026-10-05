package silence.simsool.profileviewer.api.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class HotfTreeData {
	public enum NodeType { CORE, PERK, ABILITY, BUTTON }

	public static class HotfNode {
		public final String id, name;
		public final int tier, row, col, maxLevel;
		public final NodeType type;
		private final JsonObject definition;

		private HotfNode(JsonObject data) {
			definition = data;
			id = data.get("id").getAsString();
			name = data.get("name").getAsString();
			col = data.getAsJsonArray("location").get(0).getAsInt();
			tier = data.getAsJsonArray("location").get(1).getAsInt() + 1;
			row = 8 - tier;
			type = switch (data.get("type").getAsString()) {
				case "CORE" -> NodeType.CORE;
				case "ABILITY" -> NodeType.ABILITY;
				case "UNLEVELABLE" -> NodeType.BUTTON;
				default -> NodeType.PERK;
			};
			maxLevel = type == NodeType.CORE ? data.getAsJsonArray("level").size()
				: data.has("max_level") ? data.get("max_level").getAsInt() : 1;
		}
	}

	public static final List<HotfNode> NODES;
	private static final Map<String, HotfNode> NODE_MAP = new HashMap<>();
	static {
		List<HotfNode> nodes = new ArrayList<>();
		for (JsonElement entry : ProfileJson.array(ProfileJson.catalog("hotf"), "nodes")) {
			JsonObject data = entry.getAsJsonObject();
			if (!data.has("id")) continue;
			HotfNode node = new HotfNode(data);
			nodes.add(node);
			NODE_MAP.put(node.id, node);
		}
		NODES = List.copyOf(nodes);
	}

	public static HotfNode getNode(String id) { return NODE_MAP.get(id); }

	public static HotfNode getNodeAt(int row, int col) {
		for (HotfNode node : NODES) if (node.row == row && node.col == col) return node;
		return null;
	}

	public static List<String> tooltip(HotfNode node, int level, int coreLevel) {
		level = Math.max(0, level);
		List<String> lines = new ArrayList<>();
		JsonObject data = node.definition;
		if (node.type == NodeType.CORE) {
			int shown = Math.max(1, Math.min(level, node.maxLevel));
			JsonObject current = data.getAsJsonArray("level").get(shown - 1).getAsJsonObject();
			for (JsonElement included : ProfileJson.array(current, "include"))
				addLines(lines, data.getAsJsonArray("level").get(included.getAsInt() - 1).getAsJsonObject().get("reward"));
			addLines(lines, current.get("reward"));
		} else {
			addLines(lines, data.get("tooltip"));
			JsonElement formulas = data.get("reward_formula");
			if (formulas != null) {
				JsonObject replacements = new JsonObject();
				if (formulas.isJsonObject()) replacements = formulas.getAsJsonObject();
				else replacements.add("reward", formulas);
				for (var entry : replacements.entrySet()) {
					String value = BigDecimal.valueOf(CatalogFormula.evaluate(entry.getValue().getAsString(), level, coreLevel > 0 ? 2 : 1)).stripTrailingZeros().toPlainString();
					String placeholder = "%" + entry.getKey() + "%";
					lines.replaceAll(line -> line.replace(placeholder, value));
				}
			}
		}
		lines.add("Level: " + level + "/" + node.maxLevel);
		if (level < node.maxLevel) {
			JsonObject cost = node.type == NodeType.CORE
				? ProfileJson.object(data.getAsJsonArray("level").get(Math.max(0, level)).getAsJsonObject(), "cost")
				: ProfileJson.object(data, "cost");
			long amount = data.has("cost_formula") ? (long) CatalogFormula.evaluate(data.get("cost_formula").getAsString(), level, 1)
				: ProfileJson.number(cost, "amount");
			if (amount > 0) lines.add("Next level: " + String.format("%,d", amount) + " " + ProfileJson.string(cost, "kind") + " Whispers");
		}
		return lines;
	}

	private static void addLines(List<String> lines, JsonElement text) {
		if (text == null) return;
		if (text.isJsonArray()) for (JsonElement line : text.getAsJsonArray()) addLines(lines, line);
		else lines.add(text.getAsString().replaceAll("<[^>]+>", ""));
	}

	public static ItemStack createNodeStack(HotfNode node, int level, boolean selected, int hotfLevel) {
		return createNodeStack(node, level, selected, hotfLevel, 0);
	}

	public static ItemStack createNodeStack(HotfNode node, int level, boolean selected, int hotfLevel, int coreLevel) {
		return createNodeStack(node, level, selected, hotfLevel, coreLevel, false);
	}

	public static ItemStack createNodeStack(HotfNode node, int level, boolean selected, int hotfLevel, int coreLevel, boolean disabled) {
		boolean unlocked = level >= 0 || selected;
		boolean maxed = level >= node.maxLevel;
		ItemStack stack = new ItemStack(switch (node.type) {
			case CORE -> maxed ? Items.OAK_WOOD : unlocked ? Items.STRIPPED_OAK_WOOD
				: hotfLevel >= node.tier ? Items.STRIPPED_BIRCH_WOOD : Items.STRIPPED_PALE_OAK_WOOD;
			case ABILITY -> selected ? Items.OAK_SAPLING : unlocked ? Items.CHERRY_SAPLING : Items.PALE_OAK_SAPLING;
			default -> disabled ? Items.STRIPPED_MANGROVE_LOG : maxed ? Items.OAK_LOG : unlocked ? Items.STRIPPED_OAK_LOG : Items.PALE_OAK_BUTTON;
		});
		stack.set(DataComponents.CUSTOM_NAME, Component.literal((disabled ? "§c" : selected ? "§a" : maxed ? "§6" : unlocked ? "§e" : "§c") + node.name));
		List<Component> lore = new ArrayList<>();
		for (String line : tooltip(node, level, coreLevel)) lore.add(Component.literal("§7" + line));
		if (disabled) lore.add(Component.literal("§cDISABLED"));
		else if (selected) lore.add(Component.literal("§aSELECTED"));
		if (!unlocked) lore.add(Component.literal("§cNot unlocked"));
		if (hotfLevel < node.tier) lore.add(Component.literal("§cRequires Heart of the Forest " + node.tier));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}
}
