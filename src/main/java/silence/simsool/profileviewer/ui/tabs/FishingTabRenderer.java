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
		OVERVIEW("pv.fishing.subtab.overview"),
		TROPHY_FISH("pv.fishing.subtab.trophy_fish"),
		SEA_CREATURES("pv.fishing.subtab.sea_creatures");

		public final String translationKey;
		FishingSubTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static class TrophySlotInfo {
		public float x, y, size;
		public String fishName;
		public int tierIndex; // 0=Bronze, 1=Silver, 2=Gold, 3=Diamond
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

		// Sub-tabs bar
		float subTabH = 28f;
		float subTabX = startX;
		for (FishingSubTab st : FishingSubTab.values()) {
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
			case OVERVIEW -> curY += renderOverviewView(f, startX, curY, width, mouseX, mouseY);
			case TROPHY_FISH -> curY += renderTrophyFishView(f, startX, curY, width, mouseX, mouseY);
			case SEA_CREATURES -> curY += renderSeaCreaturesView(f, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderOverviewView(FishingData f, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Row (Bronze, Silver, Gold, Diamond)
		NVGRenderer.text(L10n.translate("pv.fishing.trophy_overview") + " (" + RenderHelper.formatNumber(f.totalCatches) + " " + L10n.translate("pv.fishing.total_catches") + ")", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 24;

		float colW = (width - 36) / 4f;
		float tH = 68f;

		RenderHelper.drawModernCard(startX, curY, colW, tH, 8f, false);
		NVGRenderer.text("Bronze", startX + 14, curY + 12, Fonts.PRETENDARD_SEMIBOLD, 0xFFCD7F32, RenderHelper.FS_BUTTON);
		NVGRenderer.text(String.valueOf(f.bronzeTrophy), startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		float sX = startX + colW + 12;
		RenderHelper.drawModernCard(sX, curY, colW, tH, 8f, false);
		NVGRenderer.text("Silver", sX + 14, curY + 12, Fonts.PRETENDARD_SEMIBOLD, 0xFFCCCCCC, RenderHelper.FS_BUTTON);
		NVGRenderer.text(String.valueOf(f.silverTrophy), sX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		float gX = sX + colW + 12;
		RenderHelper.drawModernCard(gX, curY, colW, tH, 8f, false);
		NVGRenderer.text("Gold", gX + 14, curY + 12, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFD700, RenderHelper.FS_BUTTON);
		NVGRenderer.text(String.valueOf(f.goldTrophy), gX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		float dX = gX + colW + 12;
		RenderHelper.drawModernCard(dX, curY, colW, tH, 8f, false);
		NVGRenderer.text("Diamond", dX + 14, curY + 12, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_BUTTON);
		NVGRenderer.text(String.valueOf(f.diamondTrophy), dX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		curY += tH + 26;

		// Stats & Info Cards
		float infoW = (width - 16) / 2f;
		float infoH = 80f;

		RenderHelper.drawModernCard(startX, curY, infoW, infoH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.fishing.sea_creatures_kills"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(f.seaCreaturesKilled), startX + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);

		float c2X = startX + infoW + 16;
		RenderHelper.drawModernCard(c2X, curY, infoW, infoH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.fishing.trophy_overview"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(f.totalCatches), c2X + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_H2);

		curY += infoH + 20;
		return curY - y0;
	}

	private static float renderTrophyFishView(FishingData f, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		NVGRenderer.text(L10n.translate("pv.fishing.trophy_fish") + " (" + f.trophyFishCounts.size() + ")", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float cardW = (width - 14) / 2f;
		float cardH = 58f;
		int idx = 0;

		String[] tierLabels = {"Bronze", "Silver", "Gold", "Diamond"};
		int[] tierColors = {0xFFCD7F32, 0xFFCCCCCC, 0xFFFFD700, 0xFF55FFFF};

		for (Map.Entry<String, int[]> entry : f.trophyFishCounts.entrySet()) {
			String fishName = entry.getKey();
			int[] counts = entry.getValue(); // B, S, G, D

			float cx = startX + (idx % 2) * (cardW + 14);
			float cy = curY + (idx / 2) * (cardH + 10);

			RenderHelper.drawModernCard(cx, cy, cardW, cardH, 8f, false);

			// Fish Name
			NVGRenderer.text(fishName, cx + 12, cy + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);

			// 4 Mini Slots for Bronze, Silver, Gold, Diamond
			float slotS = 30f;
			float slotG = 5f;
			float startSlotX = cx + cardW - 4 * (slotS + slotG) - 8f;
			float slotY = cy + 14f;

			for (int t = 0; t < 4; t++) {
				float sx = startSlotX + t * (slotS + slotG);
				int count = counts[t];
				boolean hasFish = count > 0;
				boolean hov = mx >= sx && mx <= sx + slotS && my >= slotY && my <= slotY + slotS;

				int bgCol = hasFish ? 0xFF1E2838 : 0x44111218;
				int borderCol = hasFish ? tierColors[t] : (hov ? 0x66FFFFFF : 0x22FFFFFF);

				NVGRenderer.rect(sx, slotY, slotS, slotS, bgCol, 5f);
				NVGRenderer.outlineRect(sx, slotY, slotS, slotS, 1f, borderCol, 5f);

				ItemStack itemIcon = hasFish ? new ItemStack(Items.COD) : new ItemStack(Items.GUNPOWDER);
				TrophySlotInfo sInfo = new TrophySlotInfo(sx, slotY, slotS, fishName + " (" + tierLabels[t] + ")", t, count, itemIcon);
				visibleTrophySlots.add(sInfo);
				if (hov) hoveredTrophySlot = sInfo;

				if (hasFish) {
					String cStr = String.valueOf(count);
					float cw = NVGRenderer.textWidth(cStr, Fonts.PRETENDARD_SEMIBOLD, 9f);
					NVGRenderer.text(cStr, sx + slotS - cw - 2f, slotY + slotS - 9f, Fonts.PRETENDARD_SEMIBOLD, tierColors[t], 9f);
				}
			}

			idx++;
		}

		curY += ((idx + 1) / 2) * (cardH + 10) + 16;
		return curY - y0;
	}

	private static float renderSeaCreaturesView(FishingData f, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (f.seaCreatureKills.isEmpty()) {
			RenderHelper.drawModernCard(startX, curY, width, 70f, 10f, false);
			NVGRenderer.text(L10n.translate("pv.fishing.sea_creatures_kills"), startX + 20, curY + 28, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 86f;
		}

		NVGRenderer.text(L10n.translate("pv.fishing.sea_creatures_kills") + " (" + RenderHelper.formatNumber(f.seaCreaturesKilled) + " " + L10n.translate("pv.fishing.total_catches") + ")", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float scColW = (width - 24) / 3f;
		float scH = 44f;
		int scIdx = 0;

		for (Map.Entry<String, Integer> entry : f.seaCreatureKills.entrySet()) {
			float scx = startX + (scIdx % 3) * (scColW + 12);
			float scy = curY + (scIdx / 3) * (scH + 8);

			RenderHelper.drawModernCard(scx, scy, scColW, scH, 6f, false);
			NVGRenderer.text(entry.getKey(), scx + 12, scy + 8, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			NVGRenderer.text(RenderHelper.formatNumber(entry.getValue()) + " kills", scx + 12, scy + 24, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_CAPTION);

			scIdx++;
		}
		curY += ((scIdx + 2) / 3) * (scH + 8) + 10;
		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 28f;
		float subTabX = startX;
		for (FishingSubTab st : FishingSubTab.values()) {
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