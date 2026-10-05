package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.profileviewer.api.data.DungeonData;
import silence.simsool.profileviewer.api.data.ProfileJson;
import silence.simsool.profileviewer.api.repo.CatalogIcons;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.ItemCategoryGrid;

public final class BestiaryTabRenderer {
	private static final JsonObject CATALOG = ProfileJson.catalog("bestiary");
	private static final ItemCategoryGrid GRID = new ItemCategoryGrid();
	private static final List<ItemCategoryGrid.Page> pages = new ArrayList<>();
	private static DungeonData cached;
	private static long revision = -1;

	public static long kills(JsonObject mob, DungeonData data) {
		long total = 0;
		for (var id : mob.getAsJsonArray("mobs")) total += data.bestiaryMobKills.getOrDefault(id.getAsString(), 0);
		return total;
	}

	public static List<Integer> thresholds(JsonObject mob) {
		String bracket = mob.get("bracket").getAsString();
		String type = ProfileJson.string(mob, "bracketType").toUpperCase(Locale.ROOT);
		if (type.equals("CRITTER")) type = "CRITTERS";
		JsonArray source = type.isEmpty() ? ProfileJson.array(CATALOG, "brackets", bracket) : ProfileJson.array(CATALOG, "bracketSets", type, bracket);
		int cap = mob.get("cap").getAsInt();
		List<Integer> result = new ArrayList<>();
		for (var value : source) if (value.getAsInt() < cap) result.add(value.getAsInt());
		if (!source.isEmpty()) result.add(cap);
		return result;
	}

	private static void addMobs(JsonObject category, DungeonData data, List<ItemCategoryGrid.Cell> cells) {
		for (var entry : ProfileJson.array(category, "mobs")) {
			JsonObject mob = entry.getAsJsonObject();
			long kills = kills(mob, data);
			List<Integer> steps = thresholds(mob);
			int level = (int) steps.stream().filter(step -> kills >= step).count();
			var icon = CatalogIcons.icon(mob, mob.get("name").getAsString());
			List<Component> lore = new ArrayList<>();
			lore.add(Component.literal("§7Kills: §e" + String.format("%,d", kills)));
			lore.add(Component.literal("§7Level: §a" + level + "§7 / §a" + steps.size()));
			if (level < steps.size()) lore.add(Component.literal("§7Next Tier: §e" + String.format("%,d", steps.get(level))));
			icon.set(DataComponents.LORE, new ItemLore(lore));
			cells.add(new ItemCategoryGrid.Cell(icon, String.valueOf(level), level == steps.size() ? 0xFF55FF55 : 0xFFFFFFFF));
		}
	}

	public static float render(DungeonData data, float x, float y, float width, float mx, float my) {
		return GRID.render(getPages(data), x, y, width, mx, my);
	}

	public static List<ItemCategoryGrid.Page> getPages(DungeonData data) {
		if (cached != data || revision != ItemRepo.getRevision()) {
			revision = ItemRepo.getRevision();
			cached = data;
			pages.clear();
			for (var entry : CATALOG.entrySet()) {
				if (!entry.getValue().isJsonObject()) continue;
				JsonObject category = entry.getValue().getAsJsonObject();
				if (!category.has("name") || !category.has("icon")) continue;
				List<ItemCategoryGrid.Cell> cells = new ArrayList<>();
				if (category.has("mobs")) addMobs(category, data, cells);
				List<ItemCategoryGrid.Page> children = new ArrayList<>();
				if (!category.has("mobs")) for (var child : category.entrySet()) {
					if (!child.getValue().isJsonObject()) continue;
					JsonObject subcategory = child.getValue().getAsJsonObject();
					if (!subcategory.has("mobs")) continue;
					List<ItemCategoryGrid.Cell> subcells = new ArrayList<>();
					addMobs(subcategory, data, subcells);
					String name = subcategory.get("name").getAsString();
					children.add(new ItemCategoryGrid.Page(name, CatalogIcons.icon(subcategory.getAsJsonObject("icon"), name), subcells));
				}
				String title = category.get("name").getAsString();
				pages.add(new ItemCategoryGrid.Page(title, CatalogIcons.icon(category.getAsJsonObject("icon"), title), cells, children));
			}
		}
		return List.copyOf(pages);
	}

	public static boolean mouseClicked(float x, float y) { return GRID.mouseClicked(x, y); }
}
