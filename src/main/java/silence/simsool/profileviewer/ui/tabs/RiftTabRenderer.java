package silence.simsool.profileviewer.ui.tabs;

import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.RiftData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class RiftTabRenderer {

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;

		if (data == null || data.rift == null) {
			NVGRenderer.text(L10n.translate("pv.rift.no_data"), startX + 10, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return 40f;
		}

		RiftData r = data.rift;

		// Summary Cards (3 cards)
		float colW = (width - 24) / 3f;
		float cardH = 76f;

		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.rift.motes"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(r.motes), startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFDA70D6, RenderHelper.FS_H2);

		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.rift.enigma_souls"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(r.enigmaSouls + " / 42 " + L10n.translate("pv.rift.found"), c2X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);

		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.rift.timecharms"), c3X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(r.timecharms + " / 8 " + L10n.translate("pv.ui.unlocked"), c3X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_H2);

		curY += cardH + 24;

		// Timecharms Grid (8 Charms)
		NVGRenderer.text(L10n.translate("pv.rift.timecharms_grid"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float tcW = (width - 3 * 10) / 4f;
		float tcH = 48f;

		for (int i = 0; i < RiftData.TIMECHARMS.length; i++) {
			String tc = RiftData.TIMECHARMS[i];
			boolean unlocked = r.unlockedTimecharms.contains(tc);

			float tcx = startX + (i % 4) * (tcW + 10);
			float tcy = curY + (i / 4) * (tcH + 8);

			RenderHelper.drawModernCard(tcx, tcy, tcW, tcH, 8f, false);
			NVGRenderer.text(tc, tcx + 10, tcy + 8, Fonts.PRETENDARD_SEMIBOLD, unlocked ? 0xFF55FFFF : RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
			String statusStr = unlocked ? "§a" + L10n.translate("pv.ui.unlocked") : "§7" + L10n.translate("pv.ui.locked");
			RenderHelper.drawColoredText(statusStr, tcx + 10, tcy + 27, RenderHelper.FS_CAPTION, RenderHelper.FONT_MUTED);
		}

		curY += 2 * (tcH + 8) + 20;
		return curY - startY;
	}
}