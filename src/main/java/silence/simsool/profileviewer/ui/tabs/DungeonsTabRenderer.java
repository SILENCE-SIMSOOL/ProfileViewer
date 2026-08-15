package silence.simsool.profileviewer.ui.tabs;

import java.util.Map;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.DungeonData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class DungeonsTabRenderer {

	public enum CombatSubTab {
		DUNGEONS("pv.dungeons.subtab.dungeons"),
		BESTIARY("pv.dungeons.subtab.bestiary"),
		CRIMSON_ISLE("pv.dungeons.subtab.crimson_isle");

		public final String translationKey;
		CombatSubTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static CombatSubTab activeSubTab = CombatSubTab.DUNGEONS;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		DungeonData d = data.dungeons;

		// Sub-tabs bar
		float subTabH = 28f;
		float subTabX = startX;
		for (CombatSubTab st : CombatSubTab.values()) {
			String title = st.getTitle();
			float stW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, UIColors.ACCENT_BLUE, 6f);
			} else if (hov) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0x22FFFFFF, 6f);
			}
			NVGRenderer.text(title, subTabX + 10f, curY + 7f, Fonts.PRETENDARD_MEDIUM, active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED), RenderHelper.FS_BUTTON);

			subTabX += stW + 8f;
		}

		curY += subTabH + 18f;

		switch (activeSubTab) {
			case DUNGEONS -> curY += renderDungeonsView(d, startX, curY, width, mouseX, mouseY);
			case BESTIARY -> curY += renderBestiaryView(d, startX, curY, width, mouseX, mouseY);
			case CRIMSON_ISLE -> curY += renderCrimsonIsleView(d, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderDungeonsView(DungeonData d, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// 1. Overview Row (3 Cards)
		float colW = (width - 24) / 3f;
		float cardH = 92f;

		// Card 1: Catacombs Level
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.dungeons.catacombs"), startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		String cataLvl = L10n.translate("pv.ui.level") + " " + d.catacombsLevel;
		NVGRenderer.text(cataLvl, startX + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF5555, RenderHelper.FS_H2);
		String cataProgStr = d.catacombsLevel >= 50 ? L10n.translate("pv.ui.max") : String.format("%.1f%%", d.catacombsProgress * 100);
		NVGRenderer.text(cataProgStr, startX + colW - 14 - NVGRenderer.textWidth(cataProgStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION), curY + 38, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_CAPTION);
		RenderHelper.drawProgressBar(startX + 14, curY + 68, colW - 28, 5.5f, d.catacombsProgress, 0xFFFF5555, 0xFFFF8888);

		// Card 2: Secrets & Efficiency
		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.dungeons.secrets_runs"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(d.secretsFound), c2X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H2);
		String runInfo = String.format(L10n.translate("pv.dungeons.secrets_per_run") + ": %.2f  |  " + L10n.translate("pv.dungeons.total_runs") + ": %d", d.secretsPerRun, (d.totalNormalRuns + d.totalMasterRuns));
		NVGRenderer.text(runInfo, c2X + 14, curY + 66, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

		// Card 3: Class Average
		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.dungeons.class_average"), c3X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(String.format("%.2f", d.classAverage), c3X + 14, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_H2);
		String selCls = d.selectedClass.isEmpty() ? L10n.translate("pv.ui.none") : d.selectedClass.substring(0, 1).toUpperCase() + d.selectedClass.substring(1);
		NVGRenderer.text(L10n.translate("pv.ui.selected") + ": " + selCls, c3X + 14, curY + 66, Fonts.PRETENDARD, 0xFF55FF55, RenderHelper.FS_CAPTION);

		curY += cardH + 24;

		// 2. 5 Dungeon Classes Cards
		NVGRenderer.text(L10n.translate("pv.dungeons.classes") + " (5)", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float clsW = (width - 4 * 10) / 5f;
		float clsH = 74f;
		int cIdx = 0;

		for (DungeonData.ClassInfo ci : d.classes.values()) {
			float cx = startX + cIdx * (clsW + 10);
			boolean isSel = ci.isSelected;
			RenderHelper.drawModernCard(cx, curY, clsW, clsH, 8f, isSel);

			int nameColor = isSel ? 0xFF55FF55 : RenderHelper.FONT_PRIMARY;
			NVGRenderer.text(ci.name, cx + 12, curY + 12, Fonts.PRETENDARD_SEMIBOLD, nameColor, RenderHelper.FS_BUTTON);
			if (isSel) {
				NVGRenderer.text("★", cx + clsW - 20, curY + 12, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_CAPTION);
			}

			String lvlStr = "Lv. " + ci.level;
			NVGRenderer.text(lvlStr, cx + 12, curY + 32, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H3);

			RenderHelper.drawProgressBar(cx + 12, curY + 56, clsW - 24, 4f, ci.progress, isSel ? 0xFF55FF55 : UIColors.ACCENT_BLUE);

			cIdx++;
		}

		curY += clsH + 26;

		// 3. Dungeon Runs Floor Breakdown (Detailed Catacombs vs Master Mode Table)
		NVGRenderer.text(L10n.translate("pv.dungeons.floor_runs"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float tblW = width;
		float rowH = 34f;
		String[] floorKeys = {"F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7"};
		String[] floorNames = {"Entrance", "Floor 1 (Bonzo)", "Floor 2 (Scarf)", "Floor 3 (Professor)", "Floor 4 (Thorn)", "Floor 5 (Livid)", "Floor 6 (Sadan)", "Floor 7 (Necron)"};

		// Table Header
		RenderHelper.drawModernCard(startX, curY, tblW, rowH, 6f, false);
		NVGRenderer.text("Floor", startX + 16, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text("Normal Runs", startX + 220, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text("Fastest (S+)", startX + 340, curY + 10, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text("Master Runs", startX + 480, curY + 10, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, RenderHelper.FS_CAPTION);
		NVGRenderer.text("Master Fastest (S+)", startX + 600, curY + 10, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, RenderHelper.FS_CAPTION);

		curY += rowH + 6f;

		for (int i = 0; i < floorKeys.length; i++) {
			String fKey = floorKeys[i];
			String mKey = "M" + i;
			DungeonData.FloorStats norm = d.normalFloors.getOrDefault(fKey, new DungeonData.FloorStats());
			DungeonData.FloorStats mast = (i == 0) ? null : d.masterFloors.getOrDefault(mKey, new DungeonData.FloorStats());

			boolean hov = mx >= startX && mx <= startX + tblW && my >= curY && my <= curY + rowH;
			RenderHelper.drawModernCard(startX, curY, tblW, rowH, 6f, hov);

			NVGRenderer.text(floorNames[i], startX + 16, curY + 10, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);

			// Normal runs & S+
			String normRuns = norm.completions > 0 ? RenderHelper.formatNumber(norm.completions) + " runs" : "-";
			NVGRenderer.text(normRuns, startX + 220, curY + 10, Fonts.PRETENDARD_SEMIBOLD, norm.completions > 0 ? UIColors.ACCENT_BLUE : RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);

			String normSplus = norm.fastestTimeSplusMs > 0 ? norm.getFastestSPlusFormatted() : (norm.fastestTimeMs > 0 ? norm.getFastestTimeFormatted() : "-");
			NVGRenderer.text(normSplus, startX + 340, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);

			// Master runs & S+
			if (mast != null) {
				String mastRuns = mast.completions > 0 ? RenderHelper.formatNumber(mast.completions) + " runs" : "-";
				NVGRenderer.text(mastRuns, startX + 480, curY + 10, Fonts.PRETENDARD_SEMIBOLD, mast.completions > 0 ? 0xFFFF5555 : RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);

				String mastSplus = mast.fastestTimeSplusMs > 0 ? mast.getFastestSPlusFormatted() : (mast.fastestTimeMs > 0 ? mast.getFastestTimeFormatted() : "-");
				NVGRenderer.text(mastSplus, startX + 600, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
			} else {
				NVGRenderer.text("N/A", startX + 480, curY + 10, Fonts.PRETENDARD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
			}

			curY += rowH + 4f;
		}

		curY += 16f;
		return curY - y0;
	}

	private static float renderBestiaryView(DungeonData d, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float cardH = 76f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 10f, false);
		NVGRenderer.text("BESTIARY OVERVIEW", startX + 18, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(d.bestiaryKills) + " Total Kills", startX + 18, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, RenderHelper.FS_H2);
		NVGRenderer.text("Unlocked Species: " + d.bestiaryMobKills.size(), startX + width - 200, curY + 38, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);

		curY += cardH + 24;

		if (!d.bestiaryMobKills.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.dungeons.bestiary_families"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			curY += 22;

			float bColW = (width - 24) / 3f;
			float bH = 46f;
			int idx = 0;

			for (Map.Entry<String, Integer> entry : d.bestiaryMobKills.entrySet()) {
				float bx = startX + (idx % 3) * (bColW + 12);
				float by = curY + (idx / 3) * (bH + 8);

				RenderHelper.drawModernCard(bx, by, bColW, bH, 6f, false);
				String mName = entry.getKey().replace("_", " ").toLowerCase();
				mName = mName.substring(0, 1).toUpperCase() + mName.substring(1);
				NVGRenderer.text(mName, bx + 12, by + 8, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
				NVGRenderer.text(RenderHelper.formatNumber(entry.getValue()) + " kills", bx + 12, by + 26, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF8888, RenderHelper.FS_CAPTION);

				idx++;
			}
			curY += ((idx + 2) / 3) * (bH + 8) + 10;
		}

		return curY - y0;
	}

	private static float renderCrimsonIsleView(DungeonData d, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Row
		float colW = (width - 24) / 3f;
		float cardH = 80f;

		RenderHelper.drawModernCard(startX, curY, colW, cardH, 10f, false);
		NVGRenderer.text("SELECTED FACTION", startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(d.crimsonFaction, startX + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF5555, RenderHelper.FS_H3);

		float c2X = startX + colW + 12;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.dungeons.mage_rep"), c2X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(d.mageReputation), c2X + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_H3);

		float c3X = c2X + colW + 12;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.dungeons.barb_rep"), c3X + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(RenderHelper.formatNumber(d.barbarianReputation), c3X + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, RenderHelper.FS_H3);

		curY += cardH + 24;

		// Kuudra Tier Completions
		NVGRenderer.text(L10n.translate("pv.dungeons.kuudra_tiers"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float kColW = (width - 4 * 10) / 5f;
		float kH = 60f;
		String[] tiers = {"basic", "hot", "burning", "fiery", "infernal"};
		String[] tierNames = {"Basic", "Hot", "Burning", "Fiery", "Infernal"};

		for (int i = 0; i < tiers.length; i++) {
			float kx = startX + i * (kColW + 10);
			RenderHelper.drawModernCard(kx, curY, kColW, kH, 8f, false);
			NVGRenderer.text(tierNames[i], kx + 12, curY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
			int comps = d.kuudraCompletions.getOrDefault(tiers[i], 0);
			NVGRenderer.text(comps + " clears", kx + 12, curY + 32, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, RenderHelper.FS_BUTTON);
		}

		curY += kH + 24;

		// Dojo Scores
		if (!d.dojoScores.isEmpty()) {
			NVGRenderer.text("DOJO SCORES", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			curY += 22;

			float dColW = (width - 3 * 10) / 4f;
			float dH = 46f;
			int idx = 0;

			for (Map.Entry<String, Integer> entry : d.dojoScores.entrySet()) {
				float dx = startX + (idx % 4) * (dColW + 10);
				float dy = curY + (idx / 4) * (dH + 8);

				RenderHelper.drawModernCard(dx, dy, dColW, dH, 6f, false);
				String djName = entry.getKey().replace("_", " ").toLowerCase();
				djName = djName.substring(0, 1).toUpperCase() + djName.substring(1);
				NVGRenderer.text(djName, dx + 10, dy + 8, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);
				NVGRenderer.text(entry.getValue() + " pts", dx + 10, dy + 26, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_CAPTION);

				idx++;
			}
			curY += ((idx + 3) / 4) * (dH + 8) + 10;
		}

		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 28f;
		float subTabX = startX;
		for (CombatSubTab st : CombatSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}