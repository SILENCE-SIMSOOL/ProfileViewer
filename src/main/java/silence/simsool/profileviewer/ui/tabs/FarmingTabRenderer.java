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

		// Row 1: Garden Overview (3 Stat Cards)
		float colW = (width - 24f) / 3f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Card 1: Garden Level
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float ix1 = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(ix1, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF10B981, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE56C", ix1 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 21f);

		float tx1 = ix1 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.farming.garden_level"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + g.gardenLevel, tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 19f);
		NVGRenderer.text(L10n.translate("pv.farming.copper") + ": " + RenderHelper.formatNumber(g.copper), tx1, curY + 58f, Fonts.PRETENDARD_MEDIUM, 0xFFF97316, 12.5f);

		// Card 2: Visitors
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		NVGRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE88A", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.farming.garden_visitors"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(g.completedVisitors) + " " + L10n.translate("pv.farming.served"), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);
		NVGRenderer.text(L10n.translate("pv.farming.unique_visitors") + ": " + g.uniqueVisitors, tx2, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 3: Jacob's Medals
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		NVGRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFBBF24, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE838", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.farming.jacobs_medals"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		String medalStr = String.format("§6%d G  §7%d S  §c%d B", g.jacobGold, g.jacobSilver, g.jacobBronze);
		RenderHelper.drawColoredText(medalStr, tx3, curY + 36f, 17f, RenderHelper.FONT_PRIMARY);
		NVGRenderer.text(L10n.translate("pv.farming.contests_completed"), tx3, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		curY += cardH + 24f;

		// 10 Crop Milestones Grid
		NVGRenderer.text("\uE56C", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 18f);
		NVGRenderer.text(L10n.translate("pv.farming.crop_milestones"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float cropCardW = (width - 16f) / 2f;
		float cropCardH = 64f;
		int idx = 0;

		for (Map.Entry<String, Long> entry : g.cropMilestones.entrySet()) {
			float cx = startX + (idx % 2) * (cropCardW + 16f);
			float cy = curY + (idx / 2) * (cropCardH + 12f);

			boolean hov = mouseX >= cx && mouseX <= cx + cropCardW && mouseY >= cy && mouseY <= cy + cropCardH;
			RenderHelper.drawModernCard(cx, cy, cropCardW, cropCardH, 10f, hov);

			String cropName = formatCropName(entry.getKey());
			NVGRenderer.text(cropName, cx + 14f, cy + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14.5f);

			long count = entry.getValue();
			int milestoneTier = calcCropTier(count);
			float progress = calcCropProgress(count, milestoneTier);

			String tierStr = L10n.translate("pv.ui.tier") + " " + milestoneTier;
			float tw = NVGRenderer.textWidth(tierStr, Fonts.PRETENDARD_SEMIBOLD, 13f);
			NVGRenderer.text(tierStr, cx + cropCardW - 14f - tw, cy + 12f, Fonts.PRETENDARD_SEMIBOLD, milestoneTier >= 16 ? 0xFFFFAA00 : 0xFF10B981, 13f);

			String cntStr = RenderHelper.formatNumber(count) + " " + L10n.translate("pv.farming.harvested");
			NVGRenderer.text(cntStr, cx + 14f, cy + 30f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

			if (milestoneTier >= 16) {
				RenderHelper.drawRainbowProgressBar(cx + 14f, cy + 48f, cropCardW - 28f, 5f, 1.0f);
			} else {
				RenderHelper.drawProgressBar(cx + 14f, cy + 48f, cropCardW - 28f, 5f, progress, 0xFF059669, 0xFF10B981);
			}

			idx++;
		}

		curY += ((idx + 1) / 2) * (cropCardH + 12f) + 20f;

		// Composter Upgrades
		NVGRenderer.text("\uE5D5", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
		NVGRenderer.text(L10n.translate("pv.farming.composter_upgrades"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float compW = (width - 3 * 10f) / 4f;
		float compH = 58f;

		RenderHelper.drawModernCard(startX, curY, compW, compH, 10f, false);
		NVGRenderer.text("Speed", startX + 14f, curY + 11f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("Lv. " + g.composterSpeed, startX + 14f, curY + 31f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 16f);

		RenderHelper.drawModernCard(startX + compW + 10f, curY, compW, compH, 10f, false);
		NVGRenderer.text("Multi Drop", startX + compW + 24f, curY + 11f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("Lv. " + g.composterMultiDrop, startX + compW + 24f, curY + 31f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 16f);

		RenderHelper.drawModernCard(startX + 2 * (compW + 10f), curY, compW, compH, 10f, false);
		NVGRenderer.text("Fuel Cap", startX + 2 * (compW + 10f) + 14f, curY + 11f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("Lv. " + g.composterFuelCap, startX + 2 * (compW + 10f) + 14f, curY + 31f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 16f);

		RenderHelper.drawModernCard(startX + 3 * (compW + 10f), curY, compW, compH, 10f, false);
		NVGRenderer.text("Cost Reduction", startX + 3 * (compW + 10f) + 14f, curY + 11f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("Lv. " + g.composterCostReduction, startX + 3 * (compW + 10f) + 14f, curY + 31f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF472B6, 16f);

		curY += compH + 20f;

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