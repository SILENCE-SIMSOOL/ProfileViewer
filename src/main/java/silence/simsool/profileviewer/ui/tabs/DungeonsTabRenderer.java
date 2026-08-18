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
		DUNGEONS("pv.dungeons.subtab.dungeons", "\uE834"),
		BESTIARY("pv.dungeons.subtab.bestiary", "\uE8E8"),
		CRIMSON_ISLE("pv.dungeons.subtab.crimson_isle", "\uE3E7");

		public final String translationKey;
		public final String icon;
		CombatSubTab(String translationKey, String icon) {
			this.translationKey = translationKey;
			this.icon = icon;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static CombatSubTab activeSubTab = CombatSubTab.DUNGEONS;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		DungeonData d = data.dungeons;

		// Sub-tabs bar (Modern Pills)
		float subTabH = 32f;
		float subTabX = startX;
		for (CombatSubTab st : CombatSubTab.values()) {
			String title = st.getTitle();
			float stW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0xBF4F46E5, 8f);
			} else if (hov) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0x1AFFFFFF, 8f);
			}

			int textColor = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);
			NVGRenderer.text(st.icon, subTabX + 10f, curY + 8f, Fonts.MATERIAL_ICONS_ROUND, textColor, 16f);
			NVGRenderer.text(title, subTabX + 30f, curY + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, 14f);

			subTabX += stW + 8f;
		}

		curY += subTabH + 16f;

		switch (activeSubTab) {
			case DUNGEONS -> curY += renderDungeonsView(d, startX, curY, width, mouseX, mouseY);
			case BESTIARY -> curY += renderBestiaryView(d, startX, curY, width, mouseX, mouseY);
			case CRIMSON_ISLE -> curY += renderCrimsonIsleView(d, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderDungeonsView(DungeonData d, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// 1. Overview Row (3 Stat Cards)
		float colW = (width - 24f) / 3f;
		float cardH = 80f;

		// Card 1: Catacombs Level
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float iconBoxSize = 40f;
		float ix = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		int cataCol = 0xFFEF4444;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(cataCol, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE834", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, cataCol, 21f);

		float tx = ix + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.dungeons.catacombs"), tx, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		String cataLvl = "Lv. " + d.catacombsLevel;
		NVGRenderer.text(cataLvl, tx, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 19f);

		float barW = startX + colW - 14f - tx;
		if (d.catacombsLevel >= 50) {
			RenderHelper.drawRainbowProgressBar(tx, curY + 60f, barW, 4.5f, 1.0f);
		} else {
			RenderHelper.drawProgressBar(tx, curY + 60f, barW, 4.5f, d.catacombsProgress, 0xFF4F46E5, 0xFF818CF8);
		}

		// Card 2: Secrets & Efficiency
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		int secCol = 0xFF38BDF8;
		NVGRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(secCol, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE8B6", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, secCol, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.dungeons.secrets_runs"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(d.secretsFound), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, secCol, 19f);
		String runInfo = String.format("%.2f / run  •  %s runs", d.secretsPerRun, RenderHelper.formatNumber(d.totalNormalRuns + d.totalMasterRuns));
		NVGRenderer.text(runInfo, tx2, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 3: Class Average
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		int clsAvgCol = 0xFFFBBF24;
		NVGRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(clsAvgCol, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE9E4", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, clsAvgCol, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.dungeons.class_average"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(String.format("%.2f", d.classAverage), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, clsAvgCol, 19f);
		String selCls = d.selectedClass.isEmpty() ? L10n.translate("pv.ui.none") : d.selectedClass.substring(0, 1).toUpperCase() + d.selectedClass.substring(1);
		NVGRenderer.text(L10n.translate("pv.ui.selected") + ": " + selCls, tx3, curY + 58f, Fonts.PRETENDARD_MEDIUM, 0xFF10B981, 12.5f);

		curY += cardH + 20f;

		// 2. 5 Dungeon Classes Cards
		NVGRenderer.text("\uE8E8", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
		NVGRenderer.text(L10n.translate("pv.dungeons.classes") + " (5)", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float clsW = (width - 4 * 10f) / 5f;
		float clsH = 76f;
		int cIdx = 0;

		for (DungeonData.ClassInfo ci : d.classes.values()) {
			float cx = startX + cIdx * (clsW + 10f);
			boolean isSel = ci.isSelected;
			RenderHelper.drawModernCard(cx, curY, clsW, clsH, 10f, isSel);

			if (isSel) {
				NVGRenderer.outlineRect(cx, curY, clsW, clsH, 1.2f, 0xFF10B981, 10f);
			}

			int nameColor = isSel ? 0xFF10B981 : RenderHelper.FONT_PRIMARY;
			NVGRenderer.text(ci.name, cx + 12f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, nameColor, 14.5f);
			if (isSel) {
				NVGRenderer.text("★", cx + clsW - 20f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 14f);
			}

			String lvlStr = "Lv. " + ci.level;
			NVGRenderer.text(lvlStr, cx + 12f, curY + 32f, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 16f);

			if (ci.level >= 50) {
				RenderHelper.drawRainbowProgressBar(cx + 12f, curY + 56f, clsW - 24f, 4.5f, 1.0f);
			} else {
				RenderHelper.drawProgressBar(cx + 12f, curY + 56f, clsW - 24f, 4.5f, ci.progress, 0xFF4F46E5, 0xFF818CF8);
			}

			cIdx++;
		}

		curY += clsH + 24f;

		// 3. Dungeon Runs Floor Breakdown Table
		NVGRenderer.text("\uE834", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFEF4444, 18f);
		NVGRenderer.text(L10n.translate("pv.dungeons.floor_runs"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float tblW = width;
		float rowH = 34f;
		String[] floorKeys = {"F0", "F1", "F2", "F3", "F4", "F5", "F6", "F7"};
		String[] floorNames = {"Entrance", "Floor 1 (Bonzo)", "Floor 2 (Scarf)", "Floor 3 (Professor)", "Floor 4 (Thorn)", "Floor 5 (Livid)", "Floor 6 (Sadan)", "Floor 7 (Necron)"};

		// Table Header
		RenderHelper.drawModernCard(startX, curY, tblW, rowH, 8f, false);
		NVGRenderer.text("Floor", startX + 16f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text("Normal Runs", startX + 220f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text("Fastest (S+)", startX + 340f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text("Master Runs", startX + 480f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, 13f);
		NVGRenderer.text("Master Fastest (S+)", startX + 600f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, 13f);

		curY += rowH + 6f;

		for (int i = 0; i < floorKeys.length; i++) {
			String fKey = floorKeys[i];
			String mKey = "M" + i;
			DungeonData.FloorStats norm = d.normalFloors.getOrDefault(fKey, new DungeonData.FloorStats());
			DungeonData.FloorStats mast = (i == 0) ? null : d.masterFloors.getOrDefault(mKey, new DungeonData.FloorStats());

			boolean hov = mx >= startX && mx <= startX + tblW && my >= curY && my <= curY + rowH;
			RenderHelper.drawModernCard(startX, curY, tblW, rowH, 8f, hov);

			NVGRenderer.text(floorNames[i], startX + 16f, curY + 9f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, 13.5f);

			// Normal runs & S+
			String normRuns = norm.completions > 0 ? RenderHelper.formatNumber(norm.completions) + " runs" : "-";
			NVGRenderer.text(normRuns, startX + 220f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, norm.completions > 0 ? 0xFF38BDF8 : RenderHelper.FONT_MUTED, 13.5f);

			String normSplus = norm.fastestTimeSplusMs > 0 ? norm.getFastestSPlusFormatted() : (norm.fastestTimeMs > 0 ? norm.getFastestTimeFormatted() : "-");
			NVGRenderer.text(normSplus, startX + 340f, curY + 9f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 13f);

			// Master runs & S+
			if (mast != null) {
				String mastRuns = mast.completions > 0 ? RenderHelper.formatNumber(mast.completions) + " runs" : "-";
				NVGRenderer.text(mastRuns, startX + 480f, curY + 9f, Fonts.PRETENDARD_SEMIBOLD, mast.completions > 0 ? 0xFFFF5555 : RenderHelper.FONT_MUTED, 13.5f);

				String mastSplus = mast.fastestTimeSplusMs > 0 ? mast.getFastestSPlusFormatted() : (mast.fastestTimeMs > 0 ? mast.getFastestTimeFormatted() : "-");
				NVGRenderer.text(mastSplus, startX + 600f, curY + 9f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 13f);
			} else {
				NVGRenderer.text("N/A", startX + 480f, curY + 9f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_DISABLED, 13f);
			}

			curY += rowH + 4f;
		}

		curY += 16f;
		return curY - y0;
	}

	private static float renderBestiaryView(DungeonData d, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float cardH = 76f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 12f, false);

		float iconBoxSize = 40f;
		float ix = startX + 16f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFF7777, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE8E8", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFF7777, 21f);

		float tx = ix + iconBoxSize + 14f;
		NVGRenderer.text("BESTIARY OVERVIEW", tx, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(d.bestiaryKills) + " Total Kills", tx, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, 19f);
		NVGRenderer.text("Unlocked Species: " + d.bestiaryMobKills.size(), startX + width - 200f, curY + 30f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 14f);

		curY += cardH + 24f;

		if (!d.bestiaryMobKills.isEmpty()) {
			NVGRenderer.text("\uE3E7", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
			NVGRenderer.text(L10n.translate("pv.dungeons.bestiary_families"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 24f;

			float bColW = (width - 24f) / 3f;
			float bH = 52f;
			int idx = 0;

			for (Map.Entry<String, Integer> entry : d.bestiaryMobKills.entrySet()) {
				float bx = startX + (idx % 3) * (bColW + 12f);
				float by = curY + (idx / 3) * (bH + 10f);

				RenderHelper.drawModernCard(bx, by, bColW, bH, 10f, false);
				String mName = entry.getKey().replace("_", " ").toLowerCase();
				mName = mName.substring(0, 1).toUpperCase() + mName.substring(1);
				NVGRenderer.text(mName, bx + 14f, by + 10f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
				NVGRenderer.text(RenderHelper.formatNumber(entry.getValue()) + " kills", bx + 14f, by + 28f, Fonts.PRETENDARD_MEDIUM, 0xFFFF8888, 13f);

				idx++;
			}
			curY += ((idx + 2) / 3) * (bH + 10f) + 12f;
		}

		return curY - y0;
	}

	private static float renderCrimsonIsleView(DungeonData d, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Row (3 Cards)
		float colW = (width - 24f) / 3f;
		float cardH = 80f;

		// Faction Card
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float iconBoxSize = 40f;
		float ix = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFF5555, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE3E7", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFF5555, 21f);
		float tx = ix + iconBoxSize + 12f;
		NVGRenderer.text("SELECTED FACTION", tx, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(d.crimsonFaction, tx, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF5555, 19f);

		// Mage Rep Card
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		NVGRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);
		float tx2 = ix2 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.dungeons.mage_rep"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(d.mageReputation), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);

		// Barb Rep Card
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		NVGRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFF97316, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE52F", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFF97316, 21f);
		float tx3 = ix3 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.dungeons.barb_rep"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(d.barbarianReputation), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF97316, 19f);

		curY += cardH + 24f;

		// Kuudra Tier Completions
		NVGRenderer.text("\uE3AF", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFF7777, 18f);
		NVGRenderer.text(L10n.translate("pv.dungeons.kuudra_tiers"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float kColW = (width - 4 * 10f) / 5f;
		float kH = 64f;
		String[] tiers = {"basic", "hot", "burning", "fiery", "infernal"};
		String[] tierNames = {"Basic", "Hot", "Burning", "Fiery", "Infernal"};

		for (int i = 0; i < tiers.length; i++) {
			float kx = startX + i * (kColW + 10f);
			RenderHelper.drawModernCard(kx, curY, kColW, kH, 10f, false);
			NVGRenderer.text(tierNames[i], kx + 12f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
			int comps = d.kuudraCompletions.getOrDefault(tiers[i], 0);
			NVGRenderer.text(comps + " clears", kx + 12f, curY + 34f, Fonts.PRETENDARD_SEMIBOLD, comps > 0 ? 0xFFFF7777 : RenderHelper.FONT_DISABLED, 15f);
		}

		curY += kH + 24f;

		// Dojo Scores
		if (!d.dojoScores.isEmpty()) {
			NVGRenderer.text("\uE838", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 18f);
			NVGRenderer.text("DOJO SCORES", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 24f;

			float dColW = (width - 3 * 10f) / 4f;
			float dH = 50f;
			int idx = 0;

			for (Map.Entry<String, Integer> entry : d.dojoScores.entrySet()) {
				float dx = startX + (idx % 4) * (dColW + 10f);
				float dy = curY + (idx / 4) * (dH + 8f);

				RenderHelper.drawModernCard(dx, dy, dColW, dH, 8f, false);
				String djName = entry.getKey().replace("_", " ").toLowerCase();
				djName = djName.substring(0, 1).toUpperCase() + djName.substring(1);
				NVGRenderer.text(djName, dx + 12f, dy + 9f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, 13f);
				NVGRenderer.text(entry.getValue() + " pts", dx + 12f, dy + 27f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

				idx++;
			}
			curY += ((idx + 3) / 4) * (dH + 8f) + 12f;
		}

		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 32f;
		float subTabX = startX;
		for (CombatSubTab st : CombatSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}