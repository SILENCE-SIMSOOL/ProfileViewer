package silence.simsool.profileviewer.ui.tabs;

import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.SlayerData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class SlayerTabRenderer {

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;

		String headerStr = L10n.translate("pv.slayer.bosses") + " (" + L10n.translate("pv.slayer.total_xp") + ": " + RenderHelper.formatNumber((long) data.slayer.totalSlayerXp) + ")";
		NVGRenderer.text(headerStr, startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 24;

		float cardW = (width - 16) / 2f;
		float cardH = 104f;
		int idx = 0;

		for (var entry : data.slayer.bosses.entrySet()) {
			SlayerData.SlayerBoss boss = entry.getValue();
			float bx = startX + (idx % 2) * (cardW + 16);
			float by = curY + (idx / 2) * (cardH + 12);

			RenderHelper.drawModernCard(bx, by, cardW, cardH, 10f, false);

			NVGRenderer.text(boss.name, bx + 16, by + 14, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, RenderHelper.FS_BODY);
			String lvlStr = L10n.translate("pv.ui.level") + " " + boss.level + (boss.maxed ? " (" + L10n.translate("pv.ui.max") + ")" : "");
			NVGRenderer.text(lvlStr, bx + cardW - 16 - NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_BUTTON), by + 14, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, RenderHelper.FS_BUTTON);

			// Progress Bar
			RenderHelper.drawProgressBar(bx + 16, by + 36, cardW - 32, 5f, boss.progress, 0xFFFF7777, 0xFFFFAA00);

			// XP Progress Text
			String xpText = boss.maxed
					? RenderHelper.formatNumber((long) boss.totalXp) + " XP"
					: RenderHelper.formatNumber((long) boss.currentLevelXp) + " / " + RenderHelper.formatNumber((long) boss.nextLevelXp) + " XP (" + (int)(boss.progress * 100) + "%)";
			NVGRenderer.text(xpText, bx + 16, by + 50, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

			String kills = String.format("T1: %d  T2: %d  T3: %d  T4: %d  T5: %d",
				boss.tierKills[0], boss.tierKills[1], boss.tierKills[2], boss.tierKills[3], boss.tierKills[4]);
			NVGRenderer.text(kills, bx + 16, by + 74, Fonts.PRETENDARD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);

			idx++;
		}

		curY += ((idx + 1) / 2) * (cardH + 12) + 20;
		return curY - startY;
	}
}