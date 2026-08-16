package silence.simsool.profileviewer.ui.tabs;

import java.util.Map;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.PlayerStatus;
import silence.simsool.profileviewer.api.data.SkillsData;
import silence.simsool.profileviewer.api.data.SlayerData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class OverviewTabRenderer {

	public static class PlayerBounds {
		public float x, y, w, h;
		public boolean visible = false;
	}

	public static final PlayerBounds playerBounds = new PlayerBounds();

	public static float render(String username, MemberData data, PlayerStatus status, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;

		float leftW = 230f;
		float rightX = startX + leftW + 16f;
		float rightW = width - leftW - 16f;

		// ==========================================
		// LEFT COLUMN: Nameplate, 3D Player, Online Status
		// ==========================================
		float leftY = curY;

		// 1. Nameplate: [Level] Username
		float nameH = 32f;
		RenderHelper.drawNameplate(username, data.skyBlockLevel, startX, leftY, leftW, nameH);
		leftY += nameH + 8f;

		// 2. 3D Player Container Card
		float playerH = 260f;
		boolean hovPlayer = isHovered(startX, leftY, leftW, playerH, mouseX, mouseY);
		RenderHelper.drawModernCard(startX, leftY, leftW, playerH, 10f, hovPlayer);

		// Record bounds for 3D entity rendering pass
		playerBounds.x = startX;
		playerBounds.y = leftY;
		playerBounds.w = leftW;
		playerBounds.h = playerH;
		playerBounds.visible = true;

		leftY += playerH + 8f;

		// 3. Online Status Card
		float statusH = 34f;
		boolean hovStatus = isHovered(startX, leftY, leftW, statusH, mouseX, mouseY);
		RenderHelper.drawModernCard(startX, leftY, leftW, statusH, 8f, hovStatus);

		boolean isOnline = (status != null && status.status == PlayerStatus.Status.ONLINE);
		int statusDotCol = isOnline ? 0xFF10B981 : 0xFF6B7280; // Green / Gray
		NVGRenderer.circle(startX + 16f, leftY + statusH / 2f, 4f, statusDotCol);

		String statusTxt = status != null ? status.getDisplayText() : "Offline";
		int statusTxtCol = isOnline ? 0xFF34D399 : RenderHelper.FONT_MUTED;
		NVGRenderer.text(statusTxt, startX + 28f, leftY + (statusH - RenderHelper.FS_BUTTON) / 2f + 1f, Fonts.PRETENDARD_MEDIUM, statusTxtCol, RenderHelper.FS_BUTTON);

		leftY += statusH + 12f;

		// ==========================================
		// RIGHT COLUMN: Key Stats Grid, Skills, Slayer
		// ==========================================
		float rightY = curY;

		// 1. Key Stats Grid (3 Columns x 2 Rows)
		float statColW = (rightW - 2 * 10f) / 3f;
		float statCardH = 58f;

		// Row 1: Purse, Bank, Networth
		drawStatCard(rightX, rightY, statColW, statCardH, L10n.translate("pv.overview.purse"), RenderHelper.formatCoins(data.purse), 0xFFFFD700, mouseX, mouseY);
		drawStatCard(rightX + statColW + 10f, rightY, statColW, statCardH, L10n.translate("pv.overview.bank"), data.networth.bank > 0 ? RenderHelper.formatCoins(data.networth.bank) : "0", 0xFFFFD700, mouseX, mouseY);
		drawStatCard(rightX + (statColW + 10f) * 2, rightY, statColW, statCardH, L10n.translate("pv.overview.networth"), data.networth.total > 0 ? RenderHelper.formatCoins(data.networth.total) : "N/A", 0xFFF59E0B, mouseX, mouseY);
		rightY += statCardH + 8f;

		// Row 2: SkyBlock Level, Skill Avg, Fairy Souls
		drawLevelStatCard(rightX, rightY, statColW, statCardH, L10n.translate("pv.overview.skyblock_level"), data.skyBlockLevel, data.skyBlockLevelProgress, mouseX, mouseY);
		drawStatCard(rightX + statColW + 10f, rightY, statColW, statCardH, L10n.translate("pv.overview.skill_average"), data.skills.skillAverage > 0 ? String.format("%.2f", data.skills.skillAverage) : "0.00", 0xFF38BDF8, mouseX, mouseY);
		drawStatCard(rightX + (statColW + 10f) * 2, rightY, statColW, statCardH, L10n.translate("pv.overview.fairy_souls"), String.valueOf(data.fairySouls), 0xFFF472B6, mouseX, mouseY);
		rightY += statCardH + 16f;

		// 2. Skills Grid Section
		NVGRenderer.text(L10n.translate("pv.overview.skills_progress"), rightX + 2f, rightY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		rightY += 20f;

		float skillColW = (rightW - 2 * 8f) / 3f;
		float skillCardH = 46f;
		int skillIdx = 0;

		for (Map.Entry<String, SkillsData.SkillInfo> entry : data.skills.skills.entrySet()) {
			SkillsData.SkillInfo s = entry.getValue();
			float sx = rightX + (skillIdx % 3) * (skillColW + 8f);
			float sy = rightY + (skillIdx / 3) * (skillCardH + 6f);

			boolean hov = isHovered(sx, sy, skillColW, skillCardH, mouseX, mouseY);
			RenderHelper.drawModernCard(sx, sy, skillColW, skillCardH, 6f, hov);

			NVGRenderer.text(s.name, sx + 10f, sy + 7f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
			String lvlStr = "Lv. " + s.level;
			int lvlColor = s.level >= s.maxLevel ? 0xFFFBBF24 : 0xFF38BDF8;
			NVGRenderer.text(lvlStr, sx + skillColW - 10f - NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION), sy + 7f, Fonts.PRETENDARD_SEMIBOLD, lvlColor, RenderHelper.FS_CAPTION);

			RenderHelper.drawProgressBar(sx + 10f, sy + 28f, skillColW - 20f, 4f, s.progress, UIColors.ACCENT_BLUE, 0xFF38BDF8);

			skillIdx++;
		}
		rightY += ((skillIdx + 2) / 3) * (skillCardH + 6f) + 14f;

		// 3. Slayer Grid Section
		NVGRenderer.text(L10n.translate("pv.overview.slayer"), rightX + 2f, rightY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		rightY += 20f;

		float slayerColW = (rightW - 2 * 8f) / 3f;
		float slayerCardH = 48f;
		int slayerIdx = 0;

		String[] slayerKeys = {"zombie", "spider", "wolf", "enderman", "blaze", "vampire"};
		String[] slayerNames = {"Revenant", "Tarantula", "Sven", "Voidgloom", "Inferno", "Riftstalker"};

		for (int i = 0; i < slayerKeys.length; i++) {
			String key = slayerKeys[i];
			String sName = slayerNames[i];
			SlayerData.SlayerBoss boss = data.slayer.bosses.get(key);

			int lvl = boss != null ? boss.level : 0;
			double xp = boss != null ? boss.totalXp : 0;

			float bx = rightX + (slayerIdx % 3) * (slayerColW + 8f);
			float by = rightY + (slayerIdx / 3) * (slayerCardH + 6f);

			boolean hov = isHovered(bx, by, slayerColW, slayerCardH, mouseX, mouseY);
			RenderHelper.drawModernCard(bx, by, slayerColW, slayerCardH, 6f, hov);

			NVGRenderer.text(sName, bx + 10f, by + 8f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
			String bLvlStr = "Lv. " + lvl;
			int bLvlCol = (lvl >= 9) ? 0xFFF59E0B : 0xFFA78BFA;
			NVGRenderer.text(bLvlStr, bx + slayerColW - 10f - NVGRenderer.textWidth(bLvlStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION), by + 8f, Fonts.PRETENDARD_SEMIBOLD, bLvlCol, RenderHelper.FS_CAPTION);

			String xpStr = RenderHelper.formatCoins(xp) + " XP";
			NVGRenderer.text(xpStr, bx + 10f, by + 28f, Fonts.PRETENDARD, RenderHelper.FONT_MUTED, 11f);

			slayerIdx++;
		}
		rightY += ((slayerIdx + 2) / 3) * (slayerCardH + 6f) + 10f;

		return Math.max(leftY, rightY) - startY;
	}

	private static void drawStatCard(float x, float y, float w, float h, String label, String val, int valColor, float mx, float my) {
		boolean hov = isHovered(x, y, w, h, mx, my);
		RenderHelper.drawModernCard(x, y, w, h, 8f, hov);
		NVGRenderer.text(label, x + 12f, y + 8f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(val, x + 12f, y + 26f, Fonts.PRETENDARD_SEMIBOLD, valColor, RenderHelper.FS_H3);
	}

	private static void drawLevelStatCard(float x, float y, float w, float h, String label, int level, int progress, float mx, float my) {
		boolean hov = isHovered(x, y, w, h, mx, my);
		RenderHelper.drawModernCard(x, y, w, h, 8f, hov);
		NVGRenderer.text(label, x + 12f, y + 8f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);

		int lvlCol = RenderHelper.getSkyBlockLevelColor(level);
		String lvlStr = "Lv. " + level;
		NVGRenderer.text(lvlStr, x + 12f, y + 26f, Fonts.PRETENDARD_SEMIBOLD, lvlCol, RenderHelper.FS_H3);

		// Mini progress bar in card bottom right
		float pbW = w - 12f - (NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_H3) + 24f);
		if (pbW > 30f) {
			RenderHelper.drawProgressBar(x + w - pbW - 12f, y + 36f, pbW, 4f, progress / 100f, lvlCol);
		}
	}

	private static boolean isHovered(float x, float y, float w, float h, float mx, float my) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}
}