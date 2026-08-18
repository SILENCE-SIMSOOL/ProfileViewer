package silence.simsool.profileviewer.ui.tabs;

import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.CfData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class ChocolateFactoryTabRenderer {

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		CfData cf = data.cf;

		if (cf.totalChocolate <= 0 && cf.prestigeLevel <= 0 && cf.rabbits.isEmpty()) {
			RenderHelper.drawModernCard(startX, curY, width, 80f, 12f, false);
			NVGRenderer.text("\uE5D2", startX + 24f, curY + 28f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFFAA00, 24f);
			NVGRenderer.text(L10n.translate("pv.cf.no_data_title"), startX + 60f, curY + 22f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			NVGRenderer.text(L10n.translate("pv.cf.no_data_desc"), startX + 60f, curY + 44f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
			return 90f;
		}

		// Row 1: 3 Summary Cards
		float colW = (width - 24f) / 3f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Card 1: Current Chocolate
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float ix1 = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(ix1, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFD2691E, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE5D2", ix1 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFD2691E, 21f);

		float tx1 = ix1 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.cf.current_chocolate"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(cf.chocolate), tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFD2691E, 19f);
		NVGRenderer.text(L10n.translate("pv.dungeons.total_runs") + ": " + RenderHelper.formatNumber(cf.totalChocolate), tx1, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 2: Prestige & Barn
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		NVGRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE838", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.cf.factory_prestige"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text("Prestige " + cf.prestigeLevel, tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);
		NVGRenderer.text(L10n.translate("pv.cf.barn_capacity") + " Lv. " + cf.barnCapacityLevel, tx2, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 3: Rabbits Collected
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		NVGRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFBBF24, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE91D", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.cf.rabbits_collected"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(cf.rabbits.size() + " " + L10n.translate("pv.cf.unique_rabbits"), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 19f);
		NVGRenderer.text(L10n.translate("pv.cf.since_prestige") + ": " + RenderHelper.formatNumber(cf.chocolateSincePrestige), tx3, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		curY += cardH + 24f;

		// Section: Factory Upgrades
		NVGRenderer.text("\uE5D5", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
		NVGRenderer.text(L10n.translate("pv.cf.factory_upgrades"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float uColW = (width - 24f) / 3f;
		float uCardH = 68f;

		RenderHelper.drawModernCard(startX, curY, uColW, uCardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.cf.click_upgrades"), startX + 14f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + cf.clickUpgrades, startX + 14f, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 17f);

		RenderHelper.drawModernCard(c2X, curY, uColW, uCardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.cf.multiplier_upgrades"), c2X + 14f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + cf.chocolateMultiplierUpgrades, c2X + 14f, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 17f);

		RenderHelper.drawModernCard(c3X, curY, uColW, uCardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.cf.rarity_upgrades"), c3X + 14f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + cf.rabbitRarityUpgrades, c3X + 14f, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF472B6, 17f);

		curY += uCardH + 24f;

		// Section: Employees
		if (!cf.employees.isEmpty()) {
			NVGRenderer.text("\uE91D", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 18f);
			NVGRenderer.text(L10n.translate("pv.cf.employees") + " (" + cf.employees.size() + ")", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 24f;

			float eColW = (width - 16f) / 2f;
			float eH = 52f;
			int idx = 0;

			for (CfData.RabbitEmployee emp : cf.employees) {
				float ex = startX + (idx % 2) * (eColW + 16f);
				float ey = curY + (idx / 2) * (eH + 10f);

				RenderHelper.drawModernCard(ex, ey, eColW, eH, 10f, false);
				String empName = formatRabbitName(emp.id);
				NVGRenderer.text(empName, ex + 14f, ey + 16f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
				String lvlStr = "Lv. " + emp.level;
				float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 14f);
				NVGRenderer.text(lvlStr, ex + eColW - 14f - lw, ey + 16f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, 14f);

				idx++;
			}
			curY += ((idx + 1) / 2) * (eH + 10f) + 20f;
		}

		return curY - startY;
	}

	private static String formatRabbitName(String id) {
		String cleaned = id.replace("rabbit_", "").replace("_", " ");
		String[] words = cleaned.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}
}