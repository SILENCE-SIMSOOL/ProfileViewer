package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.ui.RenderHelper;

public class GearTabRenderer {

	public static class SlotRenderInfo {
		public float x, y, size;
		public ParsedItem item;
		public SlotRenderInfo(float x, float y, float size, ParsedItem item) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.item = item;
		}
	}

	public enum GearSubTab {
		INVENTORY("pv.gear.subtab.inventory"),
		ENDERCHEST("pv.gear.subtab.enderchest"),
		WARDROBE("pv.gear.subtab.wardrobe"),
		ACCESSORIES("pv.gear.subtab.accessories"),
		BACKPACKS("pv.gear.subtab.backpacks"),
		SACKS("pv.gear.subtab.sacks");

		public final String translationKey;
		GearSubTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static GearSubTab activeSubTab = GearSubTab.INVENTORY;
	public static final List<SlotRenderInfo> visibleSlots = new ArrayList<>();
	public static ParsedItem hoveredItem = null;
	public static float hoveredItemX = 0, hoveredItemY = 0;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		hoveredItem = null;
		visibleSlots.clear();

		if (data.inventory == null) {
			RenderHelper.drawModernCard(startX, curY, width, 80f, 10f, false);
			NVGRenderer.text("\uE000", startX + 24, curY + 30, Fonts.MATERIAL_ICONS_ROUND, 0xFFFFAA00, 24f);
			NVGRenderer.text(L10n.translate("pv.gear.api_disabled_title"), startX + 60, curY + 28, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			NVGRenderer.text(L10n.translate("pv.gear.api_disabled_desc"), startX + 60, curY + 48, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
			return 90f;
		}

		// Sub-tabs bar
		float subTabH = 28f;
		float subTabX = startX;
		for (GearSubTab st : GearSubTab.values()) {
			String title = st.getTitle();
			float stW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, UIColors.ACCENT_BLUE, 6f);
			} else if (hov) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0x22FFFFFF, 6f);
			}
			NVGRenderer.text(title, subTabX + 10f, curY + 7f, Fonts.PRETENDARD_MEDIUM, active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED), RenderHelper.FS_BUTTON);

			subTabX += stW + 8f;
		}

		curY += subTabH + 18f;

		switch (activeSubTab) {
			case INVENTORY -> curY += renderInventoryView(data, startX, curY, width, mouseX, mouseY);
			case ENDERCHEST -> curY += renderEnderChestView(data, startX, curY, width, mouseX, mouseY);
			case WARDROBE -> curY += renderWardrobeView(data, startX, curY, width, mouseX, mouseY);
			case ACCESSORIES -> curY += renderAccessoriesView(data, startX, curY, width, mouseX, mouseY);
			case BACKPACKS -> curY += renderBackpacksView(data, startX, curY, width, mouseX, mouseY);
			case SACKS -> curY += renderSacksView(data, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderInventoryView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float slotSize = 38f;
		float slotGap = 5f;

		// 1. Armor & Equipment Column (2 x 4)
		float armorColX = startX;
		float armorCardW = 2 * slotSize + slotGap + 20f;
		float armorCardH = 4 * slotSize + 3 * slotGap + 40f;

		RenderHelper.drawModernCard(armorColX, curY, armorCardW, armorCardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.gear.active_armor"), armorColX + 10f, curY + 10f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);

		for (int i = 0; i < 4; i++) {
			float sy = curY + 28f + i * (slotSize + slotGap);

			// Armor slot (col 0: Helmet, Chestplate, Leggings, Boots)
			float sxArmor = armorColX + 10f;
			ParsedItem armorItem = (data.inventory.armor.size() > (3 - i)) ? data.inventory.armor.get(3 - i) : ParsedItem.EMPTY;
			drawSlot(sxArmor, sy, slotSize, armorItem, mx, my);

			// Equipment slot (col 1: Necklace, Cloak, Belt, Gloves)
			float sxEq = armorColX + 10f + slotSize + slotGap;
			ParsedItem eqItem = (data.inventory.equipment.size() > i) ? data.inventory.equipment.get(i) : ParsedItem.EMPTY;
			drawSlot(sxEq, sy, slotSize, eqItem, mx, my);
		}

		// 2. Main Inventory Grid (9 x 4) - Top 3 rows (9..35) + Bottom Hotbar (0..8)
		float invX = startX + armorCardW + 14f;
		float invCardW = 9 * slotSize + 8 * slotGap + 20f;
		float invCardH = armorCardH;

		RenderHelper.drawModernCard(invX, curY, invCardW, invCardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.gear.inventory"), invX + 10f, curY + 10f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);

		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 9; c++) {
				// Reorder: r=0..2 -> 9..35 (main inventory), r=3 -> 0..8 (hotbar)
				int slotIdx = (r == 3) ? c : ((r + 1) * 9 + c);
				float sx = invX + 10f + c * (slotSize + slotGap);
				float sy = curY + 28f + r * (slotSize + slotGap);
				ParsedItem item = (data.inventory.inventory.size() > slotIdx) ? data.inventory.inventory.get(slotIdx) : ParsedItem.EMPTY;
				drawSlot(sx, sy, slotSize, item, mx, my);
			}
		}

		curY += armorCardH + 16f;
		return curY - y0;
	}

	private static float renderEnderChestView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (data.inventory.enderchest.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.gear.empty_page"), startX + 10, curY + 20, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 50f;
		}

		float slotSize = 33f;
		float slotGap = 3.5f;
		float pageW = 9 * (slotSize + slotGap);
		float colGap = 20f;
		int total = data.inventory.enderchest.size();
		int pages = (total + 53) / 54;

		for (int p = 0; p < pages; p += 2) {
			float rowY = curY;

			// Left Page (p)
			float leftX = startX;
			NVGRenderer.text("ENDER CHEST - " + L10n.translate("pv.gear.page") + " " + (p + 1), leftX + 2, rowY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
			for (int r = 0; r < 6; r++) {
				for (int c = 0; c < 9; c++) {
					int idx = p * 54 + r * 9 + c;
					float sx = leftX + c * (slotSize + slotGap);
					float sy = rowY + 18f + r * (slotSize + slotGap);
					ParsedItem item = (idx < total) ? data.inventory.enderchest.get(idx) : ParsedItem.EMPTY;
					drawSlot(sx, sy, slotSize, item, mx, my);
				}
			}

			// Right Page (p + 1)
			if (p + 1 < pages) {
				float rightX = startX + pageW + colGap;
				NVGRenderer.text("ENDER CHEST - " + L10n.translate("pv.gear.page") + " " + (p + 2), rightX + 2, rowY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
				for (int r = 0; r < 6; r++) {
					for (int c = 0; c < 9; c++) {
						int idx = (p + 1) * 54 + r * 9 + c;
						float sx = rightX + c * (slotSize + slotGap);
						float sy = rowY + 18f + r * (slotSize + slotGap);
						ParsedItem item = (idx < total) ? data.inventory.enderchest.get(idx) : ParsedItem.EMPTY;
						drawSlot(sx, sy, slotSize, item, mx, my);
					}
				}
			}

			curY += 18f + 6 * (slotSize + slotGap) + 16f;
		}

		return curY - y0;
	}

	private static float renderWardrobeView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (data.inventory.wardrobe.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.gear.empty_slot"), startX + 10, curY + 20, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 50f;
		}

		float slotSize = 36f;
		int sets = data.inventory.wardrobe.size() / 4;
		NVGRenderer.text("WARDROBE (" + sets + " SETS)", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		for (int s = 0; s < sets; s++) {
			float sx = startX + (s % 4) * (4 * (slotSize + 4) + 24);
			float sy = curY + (s / 4) * (slotSize + 30);

			NVGRenderer.text(L10n.translate("pv.gear.wardrobe_slot") + " " + (s + 1), sx, sy, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
			for (int i = 0; i < 4; i++) {
				int idx = s * 4 + (3 - i);
				ParsedItem item = (idx < data.inventory.wardrobe.size()) ? data.inventory.wardrobe.get(idx) : ParsedItem.EMPTY;
				drawSlot(sx + i * (slotSize + 4), sy + 16, slotSize, item, mx, my);
			}
		}
		curY += ((sets + 3) / 4) * (slotSize + 30) + 10;
		return curY - y0;
	}

	private static float renderAccessoriesView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		NVGRenderer.text(L10n.translate("pv.gear.subtab.accessories") + " (" + data.inventory.accessoryBag.size() + " ITEMS)", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		if (data.inventory.accessoryBag.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.gear.empty_slot"), startX + 10, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 40f;
		}

		float slotSize = 36f;
		int cols = 10;
		for (int i = 0; i < data.inventory.accessoryBag.size(); i++) {
			int r = i / cols;
			int c = i % cols;
			float sx = startX + c * (slotSize + 5);
			float sy = curY + r * (slotSize + 5);
			drawSlot(sx, sy, slotSize, data.inventory.accessoryBag.get(i), mx, my);
		}

		curY += ((data.inventory.accessoryBag.size() + cols - 1) / cols) * (slotSize + 5) + 10;
		return curY - y0;
	}

	private static float renderBackpacksView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (data.inventory.backpacks.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.gear.empty_page"), startX + 10, curY + 20, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 50f;
		}

		float slotSize = 33f;
		float slotGap = 3.5f;
		float pageW = 9 * (slotSize + slotGap);
		float colGap = 20f;
		int count = data.inventory.backpacks.size();

		for (int b = 0; b < count; b += 2) {
			float rowY = curY;

			// Left Backpack (b)
			List<ParsedItem> bp1 = data.inventory.backpacks.get(b);
			float leftX = startX;
			NVGRenderer.text(L10n.translate("pv.gear.backpack") + " #" + (b + 1) + " (" + bp1.size() + " slots)", leftX + 2, rowY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
			int rows1 = (bp1.size() + 8) / 9;
			for (int r = 0; r < rows1; r++) {
				for (int c = 0; c < 9; c++) {
					int idx = r * 9 + c;
					float sx = leftX + c * (slotSize + slotGap);
					float sy = rowY + 18f + r * (slotSize + slotGap);
					ParsedItem item = (idx < bp1.size()) ? bp1.get(idx) : ParsedItem.EMPTY;
					drawSlot(sx, sy, slotSize, item, mx, my);
				}
			}

			// Right Backpack (b + 1)
			int maxRows = rows1;
			if (b + 1 < count) {
				List<ParsedItem> bp2 = data.inventory.backpacks.get(b + 1);
				float rightX = startX + pageW + colGap;
				NVGRenderer.text(L10n.translate("pv.gear.backpack") + " #" + (b + 2) + " (" + bp2.size() + " slots)", rightX + 2, rowY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
				int rows2 = (bp2.size() + 8) / 9;
				if (rows2 > maxRows) maxRows = rows2;
				for (int r = 0; r < rows2; r++) {
					for (int c = 0; c < 9; c++) {
						int idx = r * 9 + c;
						float sx = rightX + c * (slotSize + slotGap);
						float sy = rowY + 18f + r * (slotSize + slotGap);
						ParsedItem item = (idx < bp2.size()) ? bp2.get(idx) : ParsedItem.EMPTY;
						drawSlot(sx, sy, slotSize, item, mx, my);
					}
				}
			}

			curY += 18f + maxRows * (slotSize + slotGap) + 16f;
		}

		return curY - y0;
	}

	private static float renderSacksView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		NVGRenderer.text(L10n.translate("pv.gear.sack_items") + " (" + data.inventory.sacks.size() + " ITEMS)", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		if (data.inventory.sacks.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.gear.empty_slot"), startX + 10, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 40f;
		}

		float colW = (width - 24) / 3f;
		float sH = 44f;
		int idx = 0;

		for (Map.Entry<String, Integer> entry : data.inventory.sacks.entrySet()) {
			if (entry.getValue() <= 0) continue;
			float sx = startX + (idx % 3) * (colW + 12);
			float sy = curY + (idx / 3) * (sH + 8);

			RenderHelper.drawModernCard(sx, sy, colW, sH, 6f, false);
			String name = entry.getKey().replace("_", " ").toLowerCase();
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			NVGRenderer.text(name, sx + 12, sy + 8, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
			NVGRenderer.text(RenderHelper.formatNumber(entry.getValue()) + " pcs", sx + 12, sy + 24, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_CAPTION);

			idx++;
		}

		curY += ((idx + 2) / 3) * (sH + 8) + 10;
		return curY - y0;
	}

	private static void drawSlot(float x, float y, float size, ParsedItem item, float mx, float my) {
		boolean hov = mx >= x && mx <= x + size && my >= y && my <= y + size;
		RenderHelper.drawModernCard(x, y, size, size, 6f, hov);

		if (!item.isEmpty()) {
			NVGRenderer.rect(x + 2, y + size - 3.5f, size - 4, 2f, item.rarityColor, 1f);
			visibleSlots.add(new SlotRenderInfo(x, y, size, item));

			if (hov) {
				hoveredItem = item;
				hoveredItemX = mx + 14;
				hoveredItemY = my + 14;
			}
		}
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 28f;
		float subTabX = startX;
		for (GearSubTab st : GearSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}