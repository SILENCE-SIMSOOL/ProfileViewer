package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MuseumData;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;
import silence.simsool.profileviewer.ui.RenderHelper;

public class MuseumTabRenderer {

	public enum MuseumSubTab {
		WEAPONS("pv.museum.subtab.weapons"),
		ARMOR_SETS("pv.museum.subtab.armor_sets"),
		RARITIES("pv.museum.subtab.rarities"),
		SPECIAL("pv.museum.subtab.special");

		public final String translationKey;
		MuseumSubTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static class MuseumSlotInfo {
		public float x, y, size;
		public String name;
		public ItemStack stack;

		public MuseumSlotInfo(float x, float y, float size, String name, ItemStack stack) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.name = name;
			this.stack = stack;
		}
	}

	public static MuseumSubTab activeSubTab = MuseumSubTab.WEAPONS;
	public static final List<MuseumSlotInfo> visibleMuseumSlots = new ArrayList<>();
	public static MuseumSlotInfo hoveredMuseumSlot = null;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		visibleMuseumSlots.clear();
		hoveredMuseumSlot = null;

		if (data == null || data.museum == null) {
			NVGRenderer.text(L10n.translate("pv.museum.no_data"), startX + 10, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 40f;
		}

		MuseumData m = data.museum;

		// Summary Row
		float colW = (width - 16) / 2f;
		float cardH = 76f;

		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.museum.valuation"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(m.totalValue > 0 ? RenderHelper.formatCoins(m.totalValue) : "N/A", startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFD700, RenderHelper.FS_H2);

		float c2X = startX + colW + 16;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.museum.donated_items"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(m.donatedItems + " Items Donated", c2X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);

		curY += cardH + 20;

		// Sub-tabs bar
		float subTabH = 28f;
		float subTabX = startX;
		for (MuseumSubTab st : MuseumSubTab.values()) {
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

		List<String> currentList = switch (activeSubTab) {
			case WEAPONS -> m.weapons;
			case ARMOR_SETS -> m.armorSets;
			case RARITIES -> m.rarities;
			case SPECIAL -> m.special;
		};

		curY += renderItemSlotGrid(activeSubTab.getTitle(), currentList, startX, curY, width, mouseX, mouseY);

		return curY - startY;
	}

	private static float renderItemSlotGrid(String title, List<String> items, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (items.isEmpty()) {
			RenderHelper.drawModernCard(startX, curY, width, 70f, 10f, false);
			NVGRenderer.text(L10n.translate("pv.museum.no_items"), startX + 20, curY + 28, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 86f;
		}

		float slotSize = 42f;
		float slotGap = 8f;
		int cols = Math.max(1, (int) ((width - 24f + slotGap) / (slotSize + slotGap)));
		int rows = (int) Math.ceil((double) items.size() / cols);
		float gridH = Math.max(100f, rows * (slotSize + slotGap) + 24f);

		RenderHelper.drawModernCard(startX, curY, width, gridH, 10f, false);

		for (int i = 0; i < items.size(); i++) {
			String itemName = items.get(i);
			int col = i % cols;
			int row = i / cols;

			float sx = startX + 12f + col * (slotSize + slotGap);
			float sy = curY + 12f + row * (slotSize + slotGap);

			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			NVGRenderer.rect(sx, sy, slotSize, slotSize, hov ? 0xFF2A364F : 0x55151620, 6f);
			NVGRenderer.outlineRect(sx, sy, slotSize, slotSize, 1f, hov ? UIColors.ACCENT_BLUE : 0x33555566, 6f);

			ItemStack stack = NbtItemParser.resolveItemStack(0, "", itemName, 0, 1);
			if (stack.isEmpty()) stack = new ItemStack(Items.ITEM_FRAME);

			MuseumSlotInfo sInfo = new MuseumSlotInfo(sx, sy, slotSize, itemName, stack);
			visibleMuseumSlots.add(sInfo);
			if (hov) hoveredMuseumSlot = sInfo;
		}

		curY += gridH + 16f;
		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 28f;
		float subTabX = startX;
		float subTabY = startY + 76f + 20f;

		for (MuseumSubTab st : MuseumSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= subTabY && my <= subTabY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}