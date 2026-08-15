package silence.simsool.profileviewer.ui.tabs;

import java.util.Map;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.GardenData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class FarmingTabRenderer {

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		GardenData g = data.garden;

		// Card 1: Garden Overview (Level & Copper & Visitors)
		float colW = (width - 24) / 3f;
		float cardH = 86f;

		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.farming.garden_level"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + g.gardenLevel, startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.farming.copper") + ": " + RenderHelper.formatNumber(g.copper), startX + 14, curY + 62, Fonts.PRETENDARD, 0xFFFF8844, RenderHelper.FS_CAPTION);

		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.farming.garden_visitors"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(g.completedVisitors) + " " + L10n.translate("pv.farming.served"), c2X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.farming.unique_visitors") + ": " + g.uniqueVisitors, c2X + 14, curY + 62, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.farming.jacobs_medals"), c3X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		String medalStr = String.format("§6%d Gold  §7%d Silver  §c%d Bronze", g.jacobGold, g.jacobSilver, g.jacobBronze);
		RenderHelper.drawColoredText(medalStr, c3X + 14, curY + 36, RenderHelper.FS_BUTTON, RenderHelper.FONT_PRIMARY);
		NVGRenderer.text(L10n.translate("pv.farming.contests_completed"), c3X + 14, curY + 62, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		curY += cardH + 24;

		// 10 Crop Milestones Grid
		NVGRenderer.text(L10n.translate("pv.farming.crop_milestones"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float cropCardW = (width - 16) / 2f;
		float cropCardH = 56f;
		int idx = 0;

		for (Map.Entry<String, Long> entry : g.cropMilestones.entrySet()) {
			float cx = startX + (idx % 2) * (cropCardW + 16);
			float cy = curY + (idx / 2) * (cropCardH + 10);

			boolean hov = mouseX >= cx && mouseX <= cx + cropCardW && mouseY >= cy && mouseY <= cy + cropCardH;
			RenderHelper.drawModernCard(cx, cy, cropCardW, cropCardH, 8f, hov);

			String cropName = formatCropName(entry.getKey());
			NVGRenderer.text(cropName, cx + 14, cy + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);

			long count = entry.getValue();
			int milestoneTier = calcCropTier(count);
			float progress = calcCropProgress(count, milestoneTier);

			String tierStr = L10n.translate("pv.ui.tier") + " " + milestoneTier;
			NVGRenderer.text(tierStr, cx + cropCardW - 14 - NVGRenderer.textWidth(tierStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION), cy + 10, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_CAPTION);

			String cntStr = RenderHelper.formatNumber(count) + " " + L10n.translate("pv.farming.harvested");
			NVGRenderer.text(cntStr, cx + 14, cy + 28, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

			RenderHelper.drawProgressBar(cx + 14, cy + 42, cropCardW - 28, 4.5f, progress, 0xFF55FF55, 0xFF00AA00);

			idx++;
		}

		curY += ((idx + 1) / 2) * (cropCardH + 10) + 20;

		// Composter Upgrades
		NVGRenderer.text(L10n.translate("pv.farming.composter_upgrades"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float compW = (width - 3 * 10) / 4f;
		float compH = 54f;

		RenderHelper.drawModernCard(startX, curY, compW, compH, 8f, false);
		NVGRenderer.text("Speed", startX + 12, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text("Lv. " + g.composterSpeed, startX + 12, curY + 28, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_BODY);

		RenderHelper.drawModernCard(startX + compW + 10, curY, compW, compH, 8f, false);
		NVGRenderer.text("Multi Drop", startX + compW + 22, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text("Lv. " + g.composterMultiDrop, startX + compW + 22, curY + 28, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_BODY);

		RenderHelper.drawModernCard(startX + 2 * (compW + 10), curY, compW, compH, 8f, false);
		NVGRenderer.text("Fuel Cap", startX + 2 * (compW + 10) + 12, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text("Lv. " + g.composterFuelCap, startX + 2 * (compW + 10) + 12, curY + 28, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, RenderHelper.FS_BODY);

		RenderHelper.drawModernCard(startX + 3 * (compW + 10), curY, compW, compH, 8f, false);
		NVGRenderer.text("Cost Reduction", startX + 3 * (compW + 10) + 12, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text("Lv. " + g.composterCostReduction, startX + 3 * (compW + 10) + 12, curY + 28, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF55FF, RenderHelper.FS_BODY);

		curY += compH + 20;

		return curY - startY;
	}

	private static String formatCropName(String key) {
		String[] words = key.replace("_", " ").toLowerCase().split(" ");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}

	private static int calcCropTier(long amount) {
		long[] brackets = {1000, 5000, 15000, 35000, 75000, 150000, 300000, 600000, 1200000, 2500000, 5000000, 10000000, 20000000, 40000000, 80000000, 150000000};
		for (int i = 0; i < brackets.length; i++) {
			if (amount < brackets[i]) {
				return i;
			}
		}
		return brackets.length;
	}

	private static float calcCropProgress(long amount, int tier) {
		long[] brackets = {1000, 5000, 15000, 35000, 75000, 150000, 300000, 600000, 1200000, 2500000, 5000000, 10000000, 20000000, 40000000, 80000000, 150000000};
		if (tier >= brackets.length) return 1.0f;
		long prev = tier == 0 ? 0 : brackets[tier - 1];
		long next = brackets[tier];
		return Math.min(1.0f, Math.max(0.0f, (float)(amount - prev) / (float)(next - prev)));
	}
}