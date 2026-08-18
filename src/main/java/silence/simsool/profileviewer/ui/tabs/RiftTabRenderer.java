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
			NVGRenderer.text(L10n.translate("pv.rift.no_data"), startX + 14f, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return 40f;
		}

		RiftData r = data.rift;

		// Summary Row (3 Stat Cards)
		float colW = (width - 24f) / 3f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Card 1: Motes
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float ix1 = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(ix1, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFDA70D6, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE3E8", ix1 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFDA70D6, 21f);

		float tx1 = ix1 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.rift.motes"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(r.motes), tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFDA70D6, 19f);

		// Card 2: Enigma Souls
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		NVGRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF818CF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.rift.enigma_souls"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(r.enigmaSouls + " / 42 " + L10n.translate("pv.rift.found"), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 19f);

		// Card 3: Timecharms
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		NVGRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE838", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.rift.timecharms"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(r.timecharms + " / 8 " + L10n.translate("pv.ui.unlocked"), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);

		curY += cardH + 24f;

		// Timecharms Grid (8 Charms)
		NVGRenderer.text("\uE838", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 18f);
		NVGRenderer.text(L10n.translate("pv.rift.timecharms_grid"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float tcW = (width - 3 * 10f) / 4f;
		float tcH = 54f;

		for (int i = 0; i < RiftData.TIMECHARMS.length; i++) {
			String tc = RiftData.TIMECHARMS[i];
			boolean unlocked = r.unlockedTimecharms.contains(tc);

			float tcx = startX + (i % 4) * (tcW + 10f);
			float tcy = curY + (i / 4) * (tcH + 10f);

			RenderHelper.drawModernCard(tcx, tcy, tcW, tcH, 10f, false);
			NVGRenderer.text(tc, tcx + 12f, tcy + 10f, Fonts.PRETENDARD_SEMIBOLD, unlocked ? 0xFF38BDF8 : RenderHelper.FONT_MUTED, 13.5f);
			String statusStr = unlocked ? L10n.translate("pv.ui.unlocked") : L10n.translate("pv.ui.locked");
			int statCol = unlocked ? 0xFF10B981 : RenderHelper.FONT_DISABLED;
			NVGRenderer.text(statusStr, tcx + 12f, tcy + 29f, Fonts.PRETENDARD_MEDIUM, statCol, 12.5f);
		}

		curY += 2 * (tcH + 10f) + 20f;
		return curY - startY;
	}
}