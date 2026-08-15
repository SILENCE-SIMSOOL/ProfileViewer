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
			RenderHelper.drawModernCard(startX, curY, width, 80f, 10f, false);
			NVGRenderer.text("\uE5D2", startX + 24, curY + 30, Fonts.MATERIAL_ICONS_ROUND, 0xFFFFAA00, 24f);
			NVGRenderer.text(L10n.translate("pv.cf.no_data_title"), startX + 60, curY + 28, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BODY);
			NVGRenderer.text(L10n.translate("pv.cf.no_data_desc"), startX + 60, curY + 48, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
			return 90f;
		}

		// Row 1: 3 Summary Cards
		float colW = (width - 24) / 3f;
		float cardH = 88f;

		// Card 1: Current Chocolate
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.cf.current_chocolate"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(cf.chocolate), startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFD2691E, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.dungeons.total_runs") + ": " + RenderHelper.formatNumber(cf.totalChocolate), startX + 14, curY + 64, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		// Card 2: Prestige & Barn
		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.cf.factory_prestige"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text("Prestige " + cf.prestigeLevel, c2X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.cf.barn_capacity") + " Lv. " + cf.barnCapacityLevel, c2X + 14, curY + 64, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		// Card 3: Rabbits Collected
		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.cf.rabbits_collected"), c3X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(cf.rabbits.size() + " " + L10n.translate("pv.cf.unique_rabbits"), c3X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFD700, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.cf.since_prestige") + ": " + RenderHelper.formatNumber(cf.chocolateSincePrestige), c3X + 14, curY + 64, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		curY += cardH + 24;

		// Section: Factory Upgrades
		NVGRenderer.text(L10n.translate("pv.cf.factory_upgrades"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float uColW = (width - 24) / 3f;
		float uCardH = 64f;

		RenderHelper.drawModernCard(startX, curY, uColW, uCardH, 8f, false);
		NVGRenderer.text(L10n.translate("pv.cf.click_upgrades"), startX + 12, curY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + cf.clickUpgrades, startX + 12, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H3);

		RenderHelper.drawModernCard(c2X, curY, uColW, uCardH, 8f, false);
		NVGRenderer.text(L10n.translate("pv.cf.multiplier_upgrades"), c2X + 12, curY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + cf.chocolateMultiplierUpgrades, c2X + 12, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_H3);

		RenderHelper.drawModernCard(c3X, curY, uColW, uCardH, 8f, false);
		NVGRenderer.text(L10n.translate("pv.cf.rarity_upgrades"), c3X + 12, curY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		NVGRenderer.text(L10n.translate("pv.ui.level") + " " + cf.rabbitRarityUpgrades, c3X + 12, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF55FF, RenderHelper.FS_H3);

		curY += uCardH + 24;

		// Section: Employees
		if (!cf.employees.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.cf.employees") + " (" + cf.employees.size() + ")", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			curY += 22;

			float eColW = (width - 16) / 2f;
			float eH = 48f;
			int idx = 0;

			for (CfData.RabbitEmployee emp : cf.employees) {
				float ex = startX + (idx % 2) * (eColW + 16);
				float ey = curY + (idx / 2) * (eH + 8);

				RenderHelper.drawModernCard(ex, ey, eColW, eH, 8f, false);
				String empName = formatRabbitName(emp.id);
				NVGRenderer.text(empName, ex + 14, ey + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
				String lvlStr = "Lv. " + emp.level;
				NVGRenderer.text(lvlStr, ex + eColW - 14 - NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_BUTTON), ey + 14, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, RenderHelper.FS_BUTTON);

				idx++;
			}
			curY += ((idx + 1) / 2) * (eH + 8) + 20;
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