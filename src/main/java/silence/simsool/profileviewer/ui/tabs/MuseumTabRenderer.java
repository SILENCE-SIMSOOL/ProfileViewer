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
		WEAPONS("pv.museum.subtab.weapons", "\uE834"),
		ARMOR_SETS("pv.museum.subtab.armor_sets", "\uE8C9"),
		RARITIES("pv.museum.subtab.rarities", "\uE88F"),
		SPECIAL("pv.museum.subtab.special", "\uE838");

		public final String translationKey;
		public final String icon;
		MuseumSubTab(String translationKey, String icon) {
			this.translationKey = translationKey;
			this.icon = icon;
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
			NVGRenderer.text(L10n.translate("pv.museum.no_data"), startX + 14f, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return 40f;
		}

		MuseumData m = data.museum;

		// Summary Row (2 Cards)
		float colW = (width - 16f) / 2f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Valuation Card
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(startX + 16f, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFBBF24, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE88F", startX + 16f + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 21f);

		float tx1 = startX + 16f + iconBoxSize + 14f;
		NVGRenderer.text(L10n.translate("pv.museum.valuation"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(m.totalValue > 0 ? RenderHelper.formatCoins(m.totalValue) : "N/A", tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 19f);

		// Donated Items Card
		float c2X = startX + colW + 16f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		NVGRenderer.rect(c2X + 16f, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE8C9", c2X + 16f + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx2 = c2X + 16f + iconBoxSize + 14f;
		NVGRenderer.text(L10n.translate("pv.museum.donated_items"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(m.donatedItems + " Items Donated", tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);

		curY += cardH + 20f;

		// Sub-tabs bar (Modern Pills)
		float subTabH = 32f;
		float subTabX = startX;
		for (MuseumSubTab st : MuseumSubTab.values()) {
			String title = st.getTitle();
			float stW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0xBF4F46E5, 8f);
			} else if (hov) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0x1AFFFFFF, 8f);
			}

			int textColor = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);
			NVGRenderer.text(st.icon, subTabX + 10f, curY + 8f, Fonts.MATERIAL_ICONS_ROUND, textColor, 16f);
			NVGRenderer.text(title, subTabX + 30f, curY + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, 14f);

			subTabX += stW + 8f;
		}

		curY += subTabH + 16f;

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
			RenderHelper.drawModernCard(startX, curY, width, 70f, 12f, false);
			NVGRenderer.text(L10n.translate("pv.museum.no_items"), startX + 20f, curY + 28f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return 86f;
		}

		float slotSize = 44f;
		float slotGap = 8f;
		int cols = Math.max(1, (int) ((width - 24f + slotGap) / (slotSize + slotGap)));
		int rows = (int) Math.ceil((double) items.size() / cols);
		float gridH = Math.max(100f, rows * (slotSize + slotGap) + 24f);

		RenderHelper.drawModernCard(startX, curY, width, gridH, 14f, false);

		for (int i = 0; i < items.size(); i++) {
			String itemName = items.get(i);
			int col = i % cols;
			int row = i / cols;

			float sx = startX + 14f + col * (slotSize + slotGap);
			float sy = curY + 14f + row * (slotSize + slotGap);

			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			NVGRenderer.rect(sx, sy, slotSize, slotSize, hov ? 0xFF222638 : 0x6611131E, 8f);
			NVGRenderer.outlineRect(sx, sy, slotSize, slotSize, 1f, hov ? 0xFF818CF8 : 0x22FFFFFF, 8f);

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
		float subTabH = 32f;
		float subTabX = startX;
		float subTabY = startY + 80f + 20f;

		for (MuseumSubTab st : MuseumSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= subTabY && my <= subTabY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}