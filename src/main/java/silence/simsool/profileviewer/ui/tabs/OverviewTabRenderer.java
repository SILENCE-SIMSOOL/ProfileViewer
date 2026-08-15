package silence.simsool.profileviewer.ui.tabs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.SkillsData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class OverviewTabRenderer {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;

		float colW = (width - 24) / 3f;
		float cardH = 96f;

		// Card 1: Networth
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, isHovered(startX, curY, colW, cardH, mouseX, mouseY));
		NVGRenderer.text(L10n.translate("pv.overview.networth"), startX + 16, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		String nwStr = data.networth.total > 0 ? RenderHelper.formatCoins(data.networth.total) : "N/A";
		NVGRenderer.text(nwStr, startX + 16, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFD700, RenderHelper.FS_H2);
		String purseStr = data.purse > 0 ? RenderHelper.formatCoins(data.purse) : "0";
		String bankStr = data.networth.bank > 0 ? RenderHelper.formatCoins(data.networth.bank) : "N/A";
		NVGRenderer.text(L10n.translate("pv.overview.purse") + ": " + purseStr + "  |  " + L10n.translate("pv.overview.bank") + ": " + bankStr, startX + 16, curY + 68, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		// Card 2: Skill Average
		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, isHovered(c2X, curY, colW, cardH, mouseX, mouseY));
		NVGRenderer.text(L10n.translate("pv.overview.skill_average"), c2X + 16, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		String saStr = data.skills.skillAverage > 0 ? String.format("%.2f", data.skills.skillAverage) : "N/A";
		NVGRenderer.text(saStr, c2X + 16, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.overview.fairy_souls") + ": " + data.fairySouls + "  |  " + L10n.translate("pv.overview.slayer_xp") + ": " + RenderHelper.formatNumber((long) data.slayer.totalSlayerXp), c2X + 16, curY + 68, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		// Card 3: Skyblock Level
		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, isHovered(c3X, curY, colW, cardH, mouseX, mouseY));
		NVGRenderer.text(L10n.translate("pv.overview.skyblock_level"), c3X + 16, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		String lvlStr = data.skyBlockLevel > 0 ? "Lv. " + data.skyBlockLevel : "N/A";
		NVGRenderer.text(lvlStr, c3X + 16, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_H2);
		RenderHelper.drawProgressBar(c3X + 16, curY + 70, colW - 32, 6f, data.skyBlockLevelProgress / 100f, 0xFF55FFFF, 0xFF00AAFF);

		curY += cardH + 24;

		// Section: Networth Breakdown
		if (data.networth.total > 0 && !data.networth.categories.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.overview.networth_breakdown"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			curY += 22;

			float nwColW = (width - 3 * 10) / 4f;
			float nwH = 56f;
			int nIdx = 0;

			for (var entry : data.networth.categories.entrySet()) {
				if (entry.getValue() <= 0) continue;
				float nx = startX + (nIdx % 4) * (nwColW + 10);
				float ny = curY + (nIdx / 4) * (nwH + 8);

				RenderHelper.drawModernCard(nx, ny, nwColW, nwH, 8f, false);
				NVGRenderer.text(entry.getKey(), nx + 12, ny + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
				NVGRenderer.text(RenderHelper.formatCoins(entry.getValue()), nx + 12, ny + 28, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFD700, RenderHelper.FS_BODY);

				nIdx++;
			}
			curY += ((nIdx + 3) / 4) * (nwH + 8) + 20;
		}

		// Section: Skills Progress
		NVGRenderer.text(L10n.translate("pv.overview.skills_progress"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float sColW = (width - 16) / 2f;
		float sCardH = 54f;
		int idx = 0;

		for (Map.Entry<String, SkillsData.SkillInfo> entry : data.skills.skills.entrySet()) {
			SkillsData.SkillInfo s = entry.getValue();
			float sx = startX + (idx % 2) * (sColW + 16);
			float sy = curY + (idx / 2) * (sCardH + 10);

			boolean hov = isHovered(sx, sy, sColW, sCardH, mouseX, mouseY);
			RenderHelper.drawModernCard(sx, sy, sColW, sCardH, 8f, hov);

			NVGRenderer.text(s.name, sx + 14, sy + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BODY);
			String sLvlStr = "Lv. " + s.level + (s.level >= s.maxLevel ? " (" + L10n.translate("pv.ui.max") + ")" : "");
			NVGRenderer.text(sLvlStr, sx + sColW - 14 - NVGRenderer.textWidth(sLvlStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_BUTTON), sy + 10, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_BUTTON);

			RenderHelper.drawProgressBar(sx + 14, sy + 34, sColW - 28, 5f, s.progress, UIColors.ACCENT_BLUE, 0xFF38BDF8);

			idx++;
		}

		curY += ((idx + 1) / 2) * (sCardH + 10) + 20;
		return curY - startY;
	}

	private static boolean isHovered(float x, float y, float w, float h, float mx, float my) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}
}