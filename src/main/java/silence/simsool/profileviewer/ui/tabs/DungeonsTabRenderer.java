package silence.simsool.profileviewer.ui.tabs;

import java.util.Locale;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.DungeonData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.ProfileJson;
import silence.simsool.profileviewer.api.repo.ItemRepo;
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
			float stW = SkijaRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				SkijaRenderer.rect(subTabX, curY, stW, subTabH, 0xBF4F46E5, 8f);
			} else if (hov) {
				SkijaRenderer.rect(subTabX, curY, stW, subTabH, 0x1AFFFFFF, 8f);
			}

			int textColor = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);
			RenderHelper.alignedIcon(st.icon, subTabX + 10f, curY + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, 16f, 14f);
			SkijaRenderer.text(title, subTabX + 30f, curY + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, 14f);

			subTabX += stW + 8f;
		}

		curY += subTabH + 16f;

		switch (activeSubTab) {
			case DUNGEONS -> curY += renderDungeonsView(d, startX, curY, width, mouseX, mouseY);
			case BESTIARY -> curY += BestiaryTabRenderer.render(d, startX, curY, width, mouseX, mouseY);
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
		SkijaRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(cataCol, 32), iconBoxSize / 2f);
		SkijaRenderer.outlineRect(ix, iy, iconBoxSize, iconBoxSize, 1f, UIColors.withAlpha(cataCol, 80), iconBoxSize / 2f);

		float cataItemSize = 28f;
		float cOffX = (iconBoxSize - cataItemSize) / 2f;
		float cOffY = (iconBoxSize - cataItemSize) / 2f;
		RenderHelper.registerItemSlot(ix + cOffX, iy + cOffY, cataItemSize, getCatacombsItemStack(), false);

		float tx = ix + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.dungeons.catacombs"), tx, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		String cataLvl = "Lv. " + d.catacombsLevel;
		SkijaRenderer.text(cataLvl, tx, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 19f);

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
		SkijaRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(secCol, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE8B6", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, secCol, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.dungeons.secrets_runs"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(RenderHelper.formatNumber(d.secretsFound), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, secCol, 19f);
		String runInfo = String.format("%.2f / run  •  %s runs", d.secretsPerRun, RenderHelper.formatNumber(d.totalNormalRuns + d.totalMasterRuns));
		SkijaRenderer.text(runInfo, tx2, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 3: Class Average
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		int clsAvgCol = 0xFFFBBF24;
		SkijaRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(clsAvgCol, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE9E4", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, clsAvgCol, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.dungeons.class_average"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(String.format("%.2f", d.classAverage), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, clsAvgCol, 19f);
		String selCls = d.selectedClass.isEmpty() ? L10n.translate("pv.ui.none") : d.selectedClass.substring(0, 1).toUpperCase() + d.selectedClass.substring(1);
		SkijaRenderer.text(L10n.translate("pv.ui.selected") + ": " + selCls, tx3, curY + 58f, Fonts.PRETENDARD_MEDIUM, 0xFF10B981, 12.5f);

		curY += cardH + 20f;

		// 2. 5 Dungeon Classes Cards
		RenderHelper.alignedIcon("\uE8E8", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 18f, 16f);
		SkijaRenderer.text(L10n.translate("pv.dungeons.classes") + " (5)", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		int classColumns = Math.min(5, Math.max(1, (int) ((width + 10f) / 168f)));
		float clsW = (width - (classColumns - 1) * 10f) / classColumns;
		float clsH = 76f;
		int cIdx = 0;

		for (DungeonData.ClassInfo ci : d.classes.values()) {
			float cx = startX + cIdx % classColumns * (clsW + 10f);
			if (cIdx > 0 && cIdx % classColumns == 0) curY += clsH + 10f;
			boolean isSel = ci.isSelected;
			RenderHelper.drawModernCard(cx, curY, clsW, clsH, 10f, isSel);

			if (isSel) {
				SkijaRenderer.outlineRect(cx, curY, clsW, clsH, 1.2f, 0xFF10B981, 10f);
			}

			// Class Icon Container on the Left
			float cIconBox = 34f;
			float cIx = cx + 10f;
			float cIy = curY + (clsH - cIconBox) / 2f;
			int classCol = getClassColor(ci.name);

			SkijaRenderer.rect(cIx, cIy, cIconBox, cIconBox, UIColors.withAlpha(classCol, 24), 8f);
			SkijaRenderer.outlineRect(cIx, cIy, cIconBox, cIconBox, 1f, UIColors.withAlpha(classCol, 60), 8f);

			float cItemSize = 24f;
			float itemOffX = (cIconBox - cItemSize) / 2f;
			float itemOffY = (cIconBox - cItemSize) / 2f;
			RenderHelper.registerItemSlot(cIx + itemOffX, cIy + itemOffY, cItemSize, getClassItemStack(ci.name), false);

			float textX = cIx + cIconBox + 9f;
			int nameColor = isSel ? 0xFF10B981 : RenderHelper.FONT_PRIMARY;
			SkijaRenderer.text(ci.name, textX, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, nameColor, 14f);
			if (isSel) {
				SkijaRenderer.text("★", cx + clsW - 16f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 13f);
			}

			String lvlStr = "Lv. " + ci.level;
			int lvlColor = ci.level >= 50 ? 0xFFFFAA00 : 0xFF818CF8;
			SkijaRenderer.text(lvlStr, textX, curY + 31f, Fonts.PRETENDARD_SEMIBOLD, lvlColor, 15f);

			float cBarW = cx + clsW - 10f - textX;
			if (ci.level >= 50) {
				RenderHelper.drawRainbowProgressBar(textX, curY + 54f, cBarW, 4.5f, 1.0f);
			} else {
				RenderHelper.drawProgressBar(textX, curY + 54f, cBarW, 4.5f, ci.progress, 0xFF4F46E5, 0xFF818CF8);
			}

			cIdx++;
		}

		curY += clsH + 24f;

		// 3. Dungeon Runs Floor Breakdown (F1 ~ F7)
		RenderHelper.alignedIcon("\uE3AF", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFFEF4444, 18f, 16f);
		SkijaRenderer.text(L10n.translate("pv.dungeons.floor_runs"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		int totalRuns = d.totalNormalRuns + d.totalMasterRuns;
		String runsBadge = "Total: " + RenderHelper.formatNumber(totalRuns) + " Runs";
		float titleW = SkijaRenderer.textWidth(L10n.translate("pv.dungeons.floor_runs"), Fonts.PRETENDARD_SEMIBOLD, 16f);
		RenderHelper.drawBadge(runsBadge, startX + 34f + titleW, curY - 1f, 0x33EF4444, 0xFFFF7777);

		curY += 24f;

		float floorW = width * 0.24f;
		float modeW = (width - floorW - 24f) / 2f;
		float normalX = startX + floorW + 12f;
		float masterX = normalX + modeW + 12f;
		SkijaRenderer.text("Floor", startX + 14f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 15f);
		SkijaRenderer.text("Normal", normalX + 14f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 15f);
		SkijaRenderer.text("Master Mode", masterX + 14f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, 15f);
		curY += 28f;

		String[] names = {"Entrance", "Bonzo", "Scarf", "Professor", "Thorn", "Livid", "Sadan", "Necron"};
		for (int floor = 0; floor < names.length; floor++) {
			RenderHelper.drawModernCard(startX, curY, floorW, 94f, 10f, false);
			SkijaRenderer.text(floor == 0 ? "Entrance" : "Floor " + floor, startX + 14f, curY + 19f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 17f);
			if (floor > 0) SkijaRenderer.text(names[floor], startX + 14f, curY + 49f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 15f);
			renderFloorStats(d.normalFloors.getOrDefault("F" + floor, new DungeonData.FloorStats()), normalX, curY, modeW, 0xFF38BDF8);
			if (floor > 0) {
				renderFloorStats(d.masterFloors.getOrDefault("M" + floor, new DungeonData.FloorStats()), masterX, curY, modeW, 0xFFFF7777);
			} else {
				RenderHelper.drawModernCard(masterX, curY, modeW, 94f, 10f, false);
				SkijaRenderer.text("Not available", masterX + 14f, curY + 38f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 15f);
			}
			curY += 104f;
		}
		return curY - y0;
	}

	private static void renderFloorStats(DungeonData.FloorStats floor, float x, float y, float width, int color) {
		RenderHelper.drawModernCard(x, y, width, 94f, 10f, false);
		SkijaRenderer.text(RenderHelper.formatNumber(floor.completions) + " clears", x + 14f, y + 12f, Fonts.PRETENDARD_SEMIBOLD, color, 18f);
		String timeLabel = floor.fastestTimeSplusMs > 0 ? "Fastest S+" : "Fastest";
		String time = floor.fastestTimeSplusMs > 0 ? floor.getFastestSPlusFormatted() : floor.fastestTimeMs > 0 ? floor.getFastestTimeFormatted() : "—";
		RenderHelper.drawStatRow(timeLabel, time, x + 14f, y + 43f, width - 28f, 14.5f, RenderHelper.FONT_SECONDARY);
		RenderHelper.drawStatRow("Best score", floor.bestScore > 0 ? String.valueOf(floor.bestScore) : "—", x + 14f, y + 67f, width - 28f, 14.5f, RenderHelper.FONT_SECONDARY);
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
		SkijaRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFF5555, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE3E7", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFF5555, 21f);
		float tx = ix + iconBoxSize + 12f;
		SkijaRenderer.text("SELECTED FACTION", tx, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(d.crimsonFaction, tx, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF5555, 19f);

		// Mage Rep Card
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		SkijaRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE87D", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);
		float tx2 = ix2 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.dungeons.mage_rep"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(RenderHelper.formatNumber(d.mageReputation), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);

		// Barb Rep Card
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		SkijaRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFF97316, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE52F", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFF97316, 21f);
		float tx3 = ix3 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.dungeons.barb_rep"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(RenderHelper.formatNumber(d.barbarianReputation), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF97316, 19f);

		curY += cardH + 24f;

		// Kuudra Tier Completions
		RenderHelper.alignedIcon("\uE3AF", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF7777, 18f, 16f);
		SkijaRenderer.text(L10n.translate("pv.dungeons.kuudra_tiers"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		int kColumns = Math.min(5, Math.max(1, (int) ((width + 10f) / 160f)));
		float kColW = (width - (kColumns - 1) * 10f) / kColumns;
		float kH = 76f;
		String[] tiers = {"none", "hot", "burning", "fiery", "infernal"};
		String[] tierNames = {"Basic", "Hot", "Burning", "Fiery", "Infernal"};

		for (int i = 0; i < tiers.length; i++) {
			float kx = startX + i % kColumns * (kColW + 10f);
			if (i > 0 && i % kColumns == 0) curY += kH + 10f;
			RenderHelper.drawModernCard(kx, curY, kColW, kH, 10f, false);
			SkijaRenderer.text(tierNames[i], kx + 12f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			int comps = d.kuudraCompletions.getOrDefault(tiers[i], 0);
			SkijaRenderer.text(comps + " clears", kx + 12f, curY + 34f, Fonts.PRETENDARD_SEMIBOLD, comps > 0 ? 0xFFFF7777 : RenderHelper.FONT_DISABLED, 18f);
		}

		curY += kH + 14f;
		long collection = 0;
		for (int i = 0; i < tiers.length; i++) collection += (long) d.kuudraCompletions.getOrDefault(tiers[i], 0) * (i + 1);
		int collectionTier = 0;
		for (var threshold : ProfileJson.array(DungeonData.CRIMSON_CATALOG, "kuudra", "collection")) if (collection >= threshold.getAsLong()) collectionTier++;
		RenderHelper.drawStatRow("Kuudra Collection", RenderHelper.formatNumber(collection) + " · Tier " + collectionTier + "/" + ProfileJson.array(DungeonData.CRIMSON_CATALOG, "kuudra", "collection").size(), startX + 12f, curY, width - 24f, 15f, RenderHelper.FONT_SECONDARY);
		curY += 36f;

		// Dojo Scores
		if (!d.dojoScores.isEmpty()) {
			RenderHelper.alignedIcon("\uE838", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 18f, 16f);
			SkijaRenderer.text("DOJO SCORES", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 24f;

			int dojoColumns = Math.min(3, Math.max(1, (int) ((width + 10f) / 220f)));
			float dColW = (width - (dojoColumns - 1) * 10f) / dojoColumns;
			float dH = 70f;
			int idx = 0;

			for (Map.Entry<String, Integer> entry : d.dojoScores.entrySet()) {
				float dx = startX + (idx % dojoColumns) * (dColW + 10f);
				float dy = curY + (idx / dojoColumns) * (dH + 8f);

				RenderHelper.drawModernCard(dx, dy, dColW, dH, 8f, false);
				String djName = ProfileJson.string(DungeonData.CRIMSON_CATALOG, "dojo", "name_map", entry.getKey());
				SkijaRenderer.text(djName, dx + 12f, dy + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
				SkijaRenderer.text((entry.getValue() < 0 ? "Not played" : entry.getValue() + " pts · " + DungeonData.dojoGrade(entry.getValue())), dx + 12f, dy + 39f, Fonts.PRETENDARD_MEDIUM, 0xFF38BDF8, 16f);

				idx++;
			}
			curY += ((idx + dojoColumns - 1) / dojoColumns) * (dH + 8f) + 12f;
			boolean played = d.dojoScores.values().stream().anyMatch(points -> points >= 0);
			int totalPoints = d.dojoScores.values().stream().mapToInt(points -> Math.max(0, points)).sum();
			RenderHelper.drawStatRow("Total Dojo Points", played ? RenderHelper.formatNumber(totalPoints) : "Not played", startX + 12f, curY, width - 24f, 16f, 0xFFFBBF24);
			curY += 34f;
		}

		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		if (activeSubTab == CombatSubTab.BESTIARY && BestiaryTabRenderer.mouseClicked(mx, my)) return true;
		float subTabH = 32f;
		float subTabX = startX;
		for (CombatSubTab st : CombatSubTab.values()) {
			float stW = SkijaRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}

	private static ItemStack getCatacombsItemStack() {
		ItemStack stack = ItemRepo.getItemStack("MASTER_SKULL_TIER_7");
		if (stack == null || stack.isEmpty()) {
			stack = ItemRepo.getItemStack("MASTER_SKULL_TIER_5");
		}
		if (stack == null || stack.isEmpty()) {
			stack = ItemRepo.getItemStack("MASTER_SKULL_TIER_1");
		}
		if (stack == null || stack.isEmpty()) {
			stack = new ItemStack(Items.WITHER_SKELETON_SKULL);
		}
		return stack;
	}

	private static ItemStack getClassItemStack(String className) {
		if (className == null) return ItemStack.EMPTY;
		return switch (className.toLowerCase(Locale.ROOT)) {
			case "healer" -> PotionContents.createItemStack(Items.POTION, Potions.HEALING);
			case "mage" -> new ItemStack(Items.BLAZE_ROD);
			case "berserk" -> new ItemStack(Items.IRON_SWORD);
			case "archer" -> new ItemStack(Items.BOW);
			case "tank" -> new ItemStack(Items.LEATHER_CHESTPLATE);
			default -> ItemStack.EMPTY;
		};
	}

	private static int getClassColor(String className) {
		if (className == null) return 0xFF818CF8;
		return switch (className.toLowerCase(Locale.ROOT)) {
			case "healer" -> 0xFFF472B6;
			case "mage" -> 0xFF38BDF8;
			case "berserk" -> 0xFFEF4444;
			case "archer" -> 0xFFF59E0B;
			case "tank" -> 0xFF10B981;
			default -> 0xFF818CF8;
		};
	}
}
