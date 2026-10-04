package silence.simsool.profileviewer.ui.tabs;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.profileviewer.api.data.InventoryData;
import silence.simsool.profileviewer.api.repo.CatalogIcons;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.ItemCategoryGrid;
import silence.simsool.profileviewer.ui.RenderHelper;

public final class SacksTabRenderer {
	private static final JsonArray CATALOG = load();
	private static final ItemCategoryGrid GRID = new ItemCategoryGrid();
	private static final List<ItemCategoryGrid.Page> pages = new ArrayList<>();
	private static InventoryData cached;
	private static long revision = -1;

	private static JsonArray load() {
		try (var stream = SacksTabRenderer.class.getResourceAsStream("/assets/profileviewer/data/sacks.json")) {
			return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
		} catch (Exception error) { throw new IllegalStateException("Cannot read sacks catalog", error); }
	}

	public static float render(InventoryData data, float x, float y, float width, float mx, float my) {
		if (cached != data || revision != ItemRepo.getRevision()) {
			cached = data;
			revision = ItemRepo.getRevision();
			pages.clear();
			for (var element : CATALOG) {
				var sack = element.getAsJsonObject();
				var items = sack.getAsJsonArray("items");
				boolean nonempty = false;
				for (var id : items) if (data.sacks.getOrDefault(id.getAsString(), 0) > 0) nonempty = true;
				if (!nonempty) continue;
				String id = sack.get("sack").getAsString();
				var icon = CatalogIcons.item(id);
				String name = icon.getHoverName().getString();
				List<ItemCategoryGrid.Cell> cells = new ArrayList<>();
				for (var item : items) {
					String itemId = item.getAsString();
					long amount = data.sacks.getOrDefault(itemId, 0);
					var stack = CatalogIcons.item(itemId);
					stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("§7Amount: §a" + String.format("%,d", amount)))));
					cells.add(new ItemCategoryGrid.Cell(stack, RenderHelper.formatNumber(amount), 0xFFFFFFFF));
				}
				pages.add(new ItemCategoryGrid.Page(name, icon, cells));
			}
		}
		return GRID.render(pages, x, y, width, mx, my);
	}

	public static boolean mouseClicked(float x, float y) { return GRID.mouseClicked(x, y); }
}
