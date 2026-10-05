package silence.simsool.profileviewer.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;

public final class ItemCategoryGrid {
	public record Cell(ItemStack stack, String count, int color) {}
	public record Page(String title, ItemStack icon, List<Cell> items, List<Page> children) {
		public Page(String title, ItemStack icon, List<Cell> items) { this(title, icon, items, List.of()); }
	}
	private record Button(float x, float y, float size, int index) {}
	private final List<Button> buttons = new ArrayList<>();
	private int selected;
	private Page childPage;
	private ItemCategoryGrid childGrid;

	public float render(List<Page> pages, float x, float y, float width, float mx, float my) {
		buttons.clear();
		if (pages.isEmpty()) {
			SkijaRenderer.text("No data available", x + 16f, y + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 16f);
			return 56f;
		}
		selected = Math.min(selected, pages.size() - 1);
		float size = 44f;
		float gap = 6f;
		int columns = Math.min(9, Math.max(1, (int) ((width - 28f) / (size + gap))));
		float gridWidth = columns * (size + gap) - gap;
		float origin = x + (width - gridWidth) / 2f;
		for (int i = 0; i < pages.size(); i++) {
			float sx = origin + i % columns * (size + gap);
			float sy = y + i / columns * (size + gap);
			boolean hover = mx >= sx && mx <= sx + size && my >= sy && my <= sy + size;
			RenderHelper.drawItemSlotBg(sx, sy, size, hover, i == selected ? 0xFF818CF8 : 0x33FFFFFF, i == selected ? 0xFF282444 : 0x5514151E, 7f);
			RenderHelper.registerItemSlot(sx, sy, size, pages.get(i).icon());
			buttons.add(new Button(sx, sy, size, i));
		}
		float bodyY = y + ((pages.size() + columns - 1) / columns) * (size + gap) + 18f;
		Page page = pages.get(selected);
		if (!page.children().isEmpty()) {
			if (childPage != page) {
				childPage = page;
				childGrid = new ItemCategoryGrid();
			}
			SkijaRenderer.text(page.title(), origin, bodyY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 18f);
			return bodyY + 30f - y + childGrid.render(page.children(), x, bodyY + 30f, width, mx, my);
		}
		childPage = null;
		childGrid = null;
		int rows = Math.max(1, (page.items().size() + columns - 1) / columns);
		float height = rows * (size + gap) + 48f;
		RenderHelper.drawModernCard(origin - 14f, bodyY, gridWidth + 28f, height, 12f, false);
		SkijaRenderer.text(page.title(), origin, bodyY + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 18f);
		for (int i = 0; i < rows * columns; i++) {
			float sx = origin + i % columns * (size + gap);
			float sy = bodyY + 42f + i / columns * (size + gap);
			boolean hover = mx >= sx && mx <= sx + size && my >= sy && my <= sy + size;
			RenderHelper.drawItemSlotBg(sx, sy, size, hover, 0x33FFFFFF, 0x5514151E, 6f);
			if (i < page.items().size()) {
				Cell cell = page.items().get(i);
				RenderHelper.registerItemSlot(sx, sy, size, cell.stack(), cell.count(), cell.color());
			}
		}
		return bodyY + height + 16f - y;
	}

	public boolean mouseClicked(float x, float y) {
		for (Button button : buttons) {
			if (x >= button.x && x <= button.x + button.size && y >= button.y && y <= button.y + button.size) {
				if (selected != button.index) { childPage = null; childGrid = null; }
				selected = button.index;
				return true;
			}
		}
		return childGrid != null && childGrid.mouseClicked(x, y);
	}
}
