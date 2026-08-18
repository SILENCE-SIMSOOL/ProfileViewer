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

		// Section Header: [Icon] Slayer Bosses (Total XP)
		NVGRenderer.text("\uE3AF", startX + 4f, curY + 2f, Fonts.MATERIAL_ICONS_ROUND, 0xFFA855F7, 20f);
		NVGRenderer.text(L10n.translate("pv.slayer.bosses"), startX + 30f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 17f);

		String xpBadge = L10n.translate("pv.slayer.total_xp") + ": " + RenderHelper.formatNumber((long) data.slayer.totalSlayerXp);
		float titleW = NVGRenderer.textWidth(L10n.translate("pv.slayer.bosses"), Fonts.PRETENDARD_SEMIBOLD, 17f);
		RenderHelper.drawBadge(xpBadge, startX + 36f + titleW, curY - 1f, 0x33A855F7, 0xFFA855F7);

		curY += 34f;

		float cardW = (width - 16f) / 2f;
		float cardH = 112f;
		int idx = 0;

		for (var entry : data.slayer.bosses.entrySet()) {
			String key = entry.getKey();
			SlayerData.SlayerBoss boss = entry.getValue();
			float bx = startX + (idx % 2) * (cardW + 16f);
			float by = curY + (idx / 2) * (cardH + 14f);

			boolean hov = mouseX >= bx && mouseX <= bx + cardW && mouseY >= by && mouseY <= by + cardH;
			RenderHelper.drawModernCard(bx, by, cardW, cardH, 14f, hov);

			// Boss Icon / Badge on Left
			float iconBoxSize = 40f;
			float ix = bx + 14f;
			float iy = by + (cardH - iconBoxSize) / 2f;
			int bossCol = getBossColor(key);

			if (boss.maxed) {
				RenderHelper.drawRainbowBorder(ix, iy, iconBoxSize, iconBoxSize, iconBoxSize / 2f, 1.5f);
				NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(bossCol, 32), iconBoxSize / 2f);
			} else {
				NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(bossCol, 32), iconBoxSize / 2f);
			}
			NVGRenderer.text("\uE3AF", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, bossCol, 21f);

			// Name & Level Badge
			float textX = ix + iconBoxSize + 12f;
			NVGRenderer.text(boss.name, textX, by + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

			String lvlStr = L10n.translate("pv.ui.level") + " " + boss.level + (boss.maxed ? " (" + L10n.translate("pv.ui.max") + ")" : "");
			int lvlCol = boss.maxed ? 0xFFFFAA00 : 0xFF818CF8;
			float lvlW = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 14f);
			NVGRenderer.text(lvlStr, bx + cardW - 16f - lvlW, by + 14f, Fonts.PRETENDARD_SEMIBOLD, lvlCol, 14f);

			// Progress Bar
			float barX = textX;
			float barW = bx + cardW - 16f - barX;
			float barY = by + 38f;
			if (boss.maxed) {
				RenderHelper.drawRainbowProgressBar(barX, barY, barW, 5.5f, 1.0f);
			} else {
				RenderHelper.drawProgressBar(barX, barY, barW, 5.5f, boss.progress, 0xFF4F46E5, 0xFF818CF8);
			}

			// XP Progress Text
			String xpText = boss.maxed
					? RenderHelper.formatNumber((long) boss.totalXp) + " XP"
					: RenderHelper.formatNumber((long) boss.currentLevelXp) + " / " + RenderHelper.formatNumber((long) boss.nextLevelXp) + " XP (" + (int)(boss.progress * 100) + "%)";
			NVGRenderer.text(xpText, textX, by + 48f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 13f);

			// Tier Kills Row (T1..T5)
			float killsY = by + 76f;
			float kw = (cardW - 32f) / 5f;
			for (int t = 0; t < 5; t++) {
				float kx = bx + 16f + t * kw;
				String tLabel = "T" + (t + 1);
				String kCount = RenderHelper.formatNumber(boss.tierKills[t]);
				NVGRenderer.text(tLabel, kx, killsY, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 12f);
				NVGRenderer.text(kCount, kx, killsY + 14f, Fonts.PRETENDARD_SEMIBOLD, boss.tierKills[t] > 0 ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_DISABLED, 13f);
			}

			idx++;
		}

		curY += ((idx + 1) / 2) * (cardH + 14f) + 16f;
		return curY - startY;
	}

	private static int getBossColor(String slayerKey) {
		if (slayerKey == null) return 0xFFA855F7;
		return switch (slayerKey.toLowerCase()) {
			case "zombie" -> 0xFF10B981;
			case "spider" -> 0xFFEF4444;
			case "wolf" -> 0xFF9CA3AF;
			case "enderman" -> 0xFFA855F7;
			case "blaze" -> 0xFFF97316;
			case "vampire" -> 0xFFF43F5E;
			default -> 0xFFA855F7;
		};
	}
}