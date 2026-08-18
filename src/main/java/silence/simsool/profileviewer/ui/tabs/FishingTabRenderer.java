package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.FishingData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class FishingTabRenderer {

	public enum FishingSubTab {
		OVERVIEW("pv.fishing.subtab.overview", "\uEA40"),
		TROPHY_FISH("pv.fishing.subtab.trophy_fish", "\uE87D"),
		SEA_CREATURES("pv.fishing.subtab.sea_creatures", "\uE834");

		public final String translationKey;
		public final String icon;
		FishingSubTab(String translationKey, String icon) {
			this.translationKey = translationKey;
			this.icon = icon;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static class TrophySlotInfo {
		public float x, y, size;
		public String fishName;
		public int tierIndex;
		public int count;
		public ItemStack stack;

		public TrophySlotInfo(float x, float y, float size, String fishName, int tierIndex, int count, ItemStack stack) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.fishName = fishName;
			this.tierIndex = tierIndex;
			this.count = count;
			this.stack = stack;
		}
	}

	public static FishingSubTab activeSubTab = FishingSubTab.OVERVIEW;
	public static final List<TrophySlotInfo> visibleTrophySlots = new ArrayList<>();
	public static TrophySlotInfo hoveredTrophySlot = null;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		FishingData f = data.fishing;
		visibleTrophySlots.clear();
		hoveredTrophySlot = null;

		// Sub-tabs bar (Modern Pills)
		float subTabH = 32f;
		float subTabX = startX;
		for (FishingSubTab st : FishingSubTab.values()) {
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

		switch (activeSubTab) {
			case OVERVIEW -> curY += renderOverviewView(f, startX, curY, width, mouseX, mouseY);
			case TROPHY_FISH -> curY += renderTrophyFishView(f, startX, curY, width, mouseX, mouseY);
			case SEA_CREATURES -> curY += renderSeaCreaturesView(f, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderOverviewView(FishingData f, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Row (Bronze, Silver, Gold, Diamond)
		NVGRenderer.text("\uE87D", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 18f);
		String hdr = L10n.translate("pv.fishing.trophy_overview") + " (" + RenderHelper.formatNumber(f.totalCatches) + " " + L10n.translate("pv.fishing.total_catches") + ")";
		NVGRenderer.text(hdr, startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float colW = (width - 36f) / 4f;
		float tH = 72f;
		float iconBoxSize = 36f;

		// Bronze
		RenderHelper.drawModernCard(startX, curY, colW, tH, 12f, false);
		float iy = curY + (tH - iconBoxSize) / 2f;
		NVGRenderer.rect(startX + 12f, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFCD7F32, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", startX + 12f + 8.5f, iy + 9f, Fonts.MATERIAL_ICONS_ROUND, 0xFFCD7F32, 19f);
		float tx1 = startX + 12f + iconBoxSize + 10f;
		NVGRenderer.text("Bronze", tx1, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(String.valueOf(f.bronzeTrophy), tx1, curY + 34f, Fonts.PRETENDARD_SEMIBOLD, 0xFFCD7F32, 18f);

		// Silver
		float sX = startX + colW + 12f;
		RenderHelper.drawModernCard(sX, curY, colW, tH, 12f, false);
		NVGRenderer.rect(sX + 12f, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFE5E7EB, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", sX + 12f + 8.5f, iy + 9f, Fonts.MATERIAL_ICONS_ROUND, 0xFFE5E7EB, 19f);
		float tx2 = sX + 12f + iconBoxSize + 10f;
		NVGRenderer.text("Silver", tx2, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(String.valueOf(f.silverTrophy), tx2, curY + 34f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE5E7EB, 18f);

		// Gold
		float gX = sX + colW + 12f;
		RenderHelper.drawModernCard(gX, curY, colW, tH, 12f, false);
		NVGRenderer.rect(gX + 12f, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFBBF24, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", gX + 12f + 8.5f, iy + 9f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 19f);
		float tx3 = gX + 12f + iconBoxSize + 10f;
		NVGRenderer.text("Gold", tx3, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(String.valueOf(f.goldTrophy), tx3, curY + 34f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 18f);

		// Diamond
		float dX = gX + colW + 12f;
		RenderHelper.drawModernCard(dX, curY, colW, tH, 12f, false);
		NVGRenderer.rect(dX + 12f, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", dX + 12f + 8.5f, iy + 9f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 19f);
		float tx4 = dX + 12f + iconBoxSize + 10f;
		NVGRenderer.text("Diamond", tx4, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(String.valueOf(f.diamondTrophy), tx4, curY + 34f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 18f);

		curY += tH + 24f;

		// Stats & Info Cards
		float infoW = (width - 16f) / 2f;
		float infoH = 80f;

		RenderHelper.drawModernCard(startX, curY, infoW, infoH, 12f, false);
		float bigIconBox = 40f;
		float bigIy = curY + (infoH - bigIconBox) / 2f;
		NVGRenderer.rect(startX + 14f, bigIy, bigIconBox, bigIconBox, UIColors.withAlpha(0xFF818CF8, 32), bigIconBox / 2f);
		NVGRenderer.text("\uE834", startX + 14f + 9.5f, bigIy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 21f);
		float txi1 = startX + 14f + bigIconBox + 12f;
		NVGRenderer.text(L10n.translate("pv.fishing.sea_creatures_kills"), txi1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(f.seaCreaturesKilled), txi1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 19f);

		float c2X = startX + infoW + 16f;
		RenderHelper.drawModernCard(c2X, curY, infoW, infoH, 12f, false);
		NVGRenderer.rect(c2X + 14f, bigIy, bigIconBox, bigIconBox, UIColors.withAlpha(0xFF10B981, 32), bigIconBox / 2f);
		NVGRenderer.text("\uEA40", c2X + 14f + 9.5f, bigIy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 21f);
		float txi2 = c2X + 14f + bigIconBox + 12f;
		NVGRenderer.text(L10n.translate("pv.fishing.trophy_overview"), txi2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(f.totalCatches), txi2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 19f);

		curY += infoH + 20f;
		return curY - y0;
	}

	private static float renderTrophyFishView(FishingData f, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		NVGRenderer.text("\uE87D", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 18f);
		String titleStr = L10n.translate("pv.fishing.trophy_fish") + " (" + f.trophyFishCounts.size() + ")";
		NVGRenderer.text(titleStr, startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float cardW = (width - 14f) / 2f;
		float cardH = 62f;
		int idx = 0;

		String[] tierLabels = {"Bronze", "Silver", "Gold", "Diamond"};
		int[] tierColors = {0xFFCD7F32, 0xFFE5E7EB, 0xFFFBBF24, 0xFF38BDF8};

		for (Map.Entry<String, int[]> entry : f.trophyFishCounts.entrySet()) {
			String fishName = entry.getKey();
			int[] counts = entry.getValue();

			float cx = startX + (idx % 2) * (cardW + 14f);
			float cy = curY + (idx / 2) * (cardH + 12f);

			RenderHelper.drawModernCard(cx, cy, cardW, cardH, 10f, false);

			// Fish Name
			NVGRenderer.text(fishName, cx + 14f, cy + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14.5f);

			// 4 Mini Slots for Bronze, Silver, Gold, Diamond
			float slotS = 32f;
			float slotG = 6f;
			float startSlotX = cx + cardW - 4 * (slotS + slotG) - 8f;
			float slotY = cy + 15f;

			for (int t = 0; t < 4; t++) {
				float sx = startSlotX + t * (slotS + slotG);
				int count = counts[t];
				boolean hasFish = count > 0;
				boolean hov = mx >= sx && mx <= sx + slotS && my >= slotY && my <= slotY + slotS;

				int bgCol = hasFish ? 0xFF1C2234 : 0x5511131E;
				int borderCol = hasFish ? tierColors[t] : (hov ? 0x66FFFFFF : 0x22FFFFFF);

				NVGRenderer.rect(sx, slotY, slotS, slotS, bgCol, 6f);
				NVGRenderer.outlineRect(sx, slotY, slotS, slotS, 1f, borderCol, 6f);

				ItemStack itemIcon = hasFish ? new ItemStack(Items.COD) : new ItemStack(Items.GUNPOWDER);
				TrophySlotInfo sInfo = new TrophySlotInfo(sx, slotY, slotS, fishName + " (" + tierLabels[t] + ")", t, count, itemIcon);
				visibleTrophySlots.add(sInfo);
				if (hov) hoveredTrophySlot = sInfo;

				if (hasFish) {
					String cStr = String.valueOf(count);
					float cw = NVGRenderer.textWidth(cStr, Fonts.PRETENDARD_SEMIBOLD, 9.5f);
					NVGRenderer.text(cStr, sx + slotS - cw - 3f, slotY + slotS - 9.5f, Fonts.PRETENDARD_SEMIBOLD, tierColors[t], 9.5f);
				}
			}

			idx++;
		}

		curY += ((idx + 1) / 2) * (cardH + 12f) + 16f;
		return curY - y0;
	}

	private static float renderSeaCreaturesView(FishingData f, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (f.seaCreatureKills.isEmpty()) {
			RenderHelper.drawModernCard(startX, curY, width, 70f, 12f, false);
			NVGRenderer.text(L10n.translate("pv.fishing.sea_creatures_kills"), startX + 20f, curY + 28f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return 86f;
		}

		NVGRenderer.text("\uE834", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
		String titleStr = L10n.translate("pv.fishing.sea_creatures_kills") + " (" + RenderHelper.formatNumber(f.seaCreaturesKilled) + " " + L10n.translate("pv.fishing.total_catches") + ")";
		NVGRenderer.text(titleStr, startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float scColW = (width - 24f) / 3f;
		float scH = 50f;
		int scIdx = 0;

		for (Map.Entry<String, Integer> entry : f.seaCreatureKills.entrySet()) {
			float scx = startX + (scIdx % 3) * (scColW + 12f);
			float scy = curY + (scIdx / 3) * (scH + 10f);

			RenderHelper.drawModernCard(scx, scy, scColW, scH, 8f, false);
			NVGRenderer.text(entry.getKey(), scx + 14f, scy + 10f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
			NVGRenderer.text(RenderHelper.formatNumber(entry.getValue()) + " kills", scx + 14f, scy + 28f, Fonts.PRETENDARD_MEDIUM, 0xFF38BDF8, 12.5f);

			scIdx++;
		}
		curY += ((scIdx + 2) / 3) * (scH + 10f) + 12f;
		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 32f;
		float subTabX = startX;
		for (FishingSubTab st : FishingSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}