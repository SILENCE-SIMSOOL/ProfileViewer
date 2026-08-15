package silence.simsool.profileviewer.ui.tabs;

import java.util.List;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.CollectionData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class CollectionsTabRenderer {

	public enum ColCategory {
		FARMING("pv.col.subtab.farming"),
		MINING("pv.col.subtab.mining"),
		COMBAT("pv.col.subtab.combat"),
		FORAGING("pv.col.subtab.foraging"),
		FISHING("pv.col.subtab.fishing"),
		MINIONS("pv.col.subtab.minions");

		public final String translationKey;
		ColCategory(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static ColCategory activeCategory = ColCategory.FARMING;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		CollectionData col = data.collections;

		// Sub-tabs bar
		float subTabH = 28f;
		float subTabX = startX;
		for (ColCategory cat : ColCategory.values()) {
			String title = cat.getTitle();
			float catW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			boolean active = (cat == activeCategory);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + catW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				NVGRenderer.rect(subTabX, curY, catW, subTabH, UIColors.ACCENT_BLUE, 6f);
			} else if (hov) {
				NVGRenderer.rect(subTabX, curY, catW, subTabH, 0x22FFFFFF, 6f);
			}
			NVGRenderer.text(title, subTabX + 10f, curY + 7f, Fonts.PRETENDARD_MEDIUM, active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED), RenderHelper.FS_BUTTON);

			subTabX += catW + 8f;
		}

		curY += subTabH + 18f;

		if (activeCategory == ColCategory.MINIONS) {
			curY += renderMinionsView(col, startX, curY, width, mouseX, mouseY);
		} else {
			List<CollectionData.CollectionItem> items = switch (activeCategory) {
				case FARMING -> col.farmingCollections;
				case MINING -> col.miningCollections;
				case COMBAT -> col.combatCollections;
				case FORAGING -> col.foragingCollections;
				case FISHING -> col.fishingCollections;
				default -> col.farmingCollections;
			};
			curY += renderCollectionItemsGrid(items, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderCollectionItemsGrid(List<CollectionData.CollectionItem> items, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (items == null || items.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.col.no_data"), startX + 10, curY + 20, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 50f;
		}

		float cardW = (width - 16) / 2f;
		float cardH = 58f;
		int idx = 0;

		for (CollectionData.CollectionItem item : items) {
			float cx = startX + (idx % 2) * (cardW + 16);
			float cy = curY + (idx / 2) * (cardH + 10);

			boolean hov = mx >= cx && mx <= cx + cardW && my >= cy && my <= cy + cardH;
			RenderHelper.drawModernCard(cx, cy, cardW, cardH, 8f, hov);

			NVGRenderer.text(item.name, cx + 14, cy + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);

			String status = item.isMax ? "§a" + L10n.translate("pv.ui.max") : String.format(L10n.translate("pv.ui.tier") + " %d/%d (%.1f%%)", item.tier, item.maxTier, item.progress * 100f);
			RenderHelper.drawColoredText(status, cx + cardW - 14 - NVGRenderer.textWidth(status.replace("§a", ""), Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION), cy + 10, RenderHelper.FS_CAPTION, RenderHelper.FONT_SECONDARY);

			String amtStr = RenderHelper.formatNumber(item.amount) + " " + L10n.translate("pv.col.collected");
			NVGRenderer.text(amtStr, cx + 14, cy + 28, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

			int barColor = item.isMax ? 0xFF55FF55 : UIColors.ACCENT_BLUE;
			RenderHelper.drawProgressBar(cx + 14, cy + 44, cardW - 28, 4.5f, item.progress, barColor, 0xFF38BDF8);

			idx++;
		}

		curY += ((idx + 1) / 2) * (cardH + 10) + 10;
		return curY - y0;
	}

	private static float renderMinionsView(CollectionData col, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Row
		float colW = (width - 24) / 3f;
		float cardH = 76f;

		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.col.unlocked_minions"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(col.unlockedMinions + " " + L10n.translate("pv.ui.unique"), startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);

		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.col.minion_slots"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(col.minionSlots + " Slots", c2X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_H2);

		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.col.crafted_generators"), c3X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(col.craftedMinions.size() + " " + L10n.translate("pv.dungeons.total_runs"), c3X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFD700, RenderHelper.FS_H2);

		curY += cardH + 24;

		if (!col.craftedMinions.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.col.crafted_minions_list"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			curY += 22;

			float mColW = (width - 3 * 10) / 4f;
			float mH = 40f;
			int idx = 0;

			for (String minion : col.craftedMinions) {
				float mx_ = startX + (idx % 4) * (mColW + 10);
				float my_ = curY + (idx / 4) * (mH + 8);

				RenderHelper.drawModernCard(mx_, my_, mColW, mH, 6f, false);
				String mName = minion.replace("_GENERATOR_", " ").replace("_", " ").toLowerCase();
				mName = mName.substring(0, 1).toUpperCase() + mName.substring(1);
				NVGRenderer.text(mName, mx_ + 10, my_ + 12, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);

				idx++;
			}
			curY += ((idx + 3) / 4) * (mH + 8) + 10;
		}

		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 28f;
		float subTabX = startX;
		for (ColCategory cat : ColCategory.values()) {
			float catW = NVGRenderer.textWidth(cat.getTitle(), Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			if (mx >= subTabX && mx <= subTabX + catW && my >= startY && my <= startY + subTabH) {
				activeCategory = cat;
				return true;
			}
			subTabX += catW + 8f;
		}
		return false;
	}
}