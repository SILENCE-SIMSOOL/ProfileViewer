package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.HotmTreeData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MiningData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class MiningTabRenderer {

	public enum MiningSubTab {
		MAIN("pv.mining.subtab.main"),
		HOTM_TREE("pv.mining.subtab.hotm_tree"),
		GEAR("pv.mining.subtab.gear"),
		GLACITE("pv.mining.subtab.glacite");

		public final String translationKey;
		MiningSubTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static class TreeSlotInfo {
		public float x, y, size;
		public HotmTreeData.HotmNode node;
		public int level;
		public ItemStack stack;

		public TreeSlotInfo(float x, float y, float size, HotmTreeData.HotmNode node, int level, ItemStack stack) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.node = node;
			this.level = level;
			this.stack = stack;
		}
	}

	public static MiningSubTab activeSubTab = MiningSubTab.MAIN;
	public static int activeLoadoutSlot = 1;
	public static final List<TreeSlotInfo> visibleTreeSlots = new ArrayList<>();
	public static TreeSlotInfo hoveredTreeSlot = null;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		MiningData m = data.mining;
		visibleTreeSlots.clear();
		hoveredTreeSlot = null;

		// Sub-tabs bar
		float subTabH = 28f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
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
			case MAIN -> curY += renderMainView(m, startX, curY, width, mouseX, mouseY);
			case HOTM_TREE -> curY += renderHotmTreeView(m, startX, curY, width, mouseX, mouseY);
			case GEAR -> curY += renderGearView(data, startX, curY, width, mouseX, mouseY);
			case GLACITE -> curY += renderGlaciteView(m, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderMainView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Card
		float cardH = 84f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 12f, false);
		NVGRenderer.text(L10n.translate("pv.mining.hotm"), startX + 18, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(L10n.translate("pv.ui.tier") + " " + m.hotmLevel + " / 10", startX + 18, curY + 34, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FF55, RenderHelper.FS_H2);
		NVGRenderer.text(L10n.translate("pv.mining.potm") + ": " + m.peakOfTheMountain + "  |  " + L10n.translate("pv.mining.nucleus_runs") + ": " + m.nucleusRuns, startX + width - 280, curY + 38, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);

		curY += cardH + 24;

		// 3 Powders Row
		NVGRenderer.text(L10n.translate("pv.mining.powders"), startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float colW = (width - 24) / 3f;
		float pH = 74f;

		RenderHelper.drawModernCard(startX, curY, colW, pH, 10f, false);
		NVGRenderer.text("Mithril Powder", startX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, 0xFF00AA00, RenderHelper.FS_BUTTON);
		NVGRenderer.text(RenderHelper.formatNumber(m.mithrilPowder), startX + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		float gX = startX + colW + 12;
		RenderHelper.drawModernCard(gX, curY, colW, pH, 10f, false);
		NVGRenderer.text("Gemstone Powder", gX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF55FF, RenderHelper.FS_BUTTON);
		NVGRenderer.text(RenderHelper.formatNumber(m.gemstonePowder), gX + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		float glX = gX + colW + 12;
		RenderHelper.drawModernCard(glX, curY, colW, pH, 10f, false);
		NVGRenderer.text("Glacite Powder", glX + 14, curY + 14, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_BUTTON);
		NVGRenderer.text(RenderHelper.formatNumber(m.glacitePowder), glX + 14, curY + 36, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);

		curY += pH + 24;

		// Crystal Nucleus Crystals (5 Gems)
		NVGRenderer.text("CRYSTAL NUCLEUS (5 GEMS)", startX + 4, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
		curY += 22;

		float crW = (width - 4 * 10) / 5f;
		float crH = 54f;
		String[] crystals = {"jade_crystal", "amethyst_crystal", "topaz_crystal", "sapphire_crystal", "amber_crystal"};
		String[] cNames = {"Jade", "Amethyst", "Topaz", "Sapphire", "Amber"};
		int[] cColors = {0xFF55FF55, 0xFFAA00AA, 0xFFFFAA00, 0xFF5555FF, 0xFFFFD700};

		for (int i = 0; i < crystals.length; i++) {
			float cx = startX + i * (crW + 10);
			boolean found = m.crystals.getOrDefault(crystals[i], false);
			RenderHelper.drawModernCard(cx, curY, crW, crH, 8f, false);
			NVGRenderer.text(cNames[i], cx + 12, curY + 10, Fonts.PRETENDARD_SEMIBOLD, cColors[i], RenderHelper.FS_BUTTON);
			String foundStr = found ? "§a" + L10n.translate("pv.ui.unlocked") : "§7" + L10n.translate("pv.ui.locked");
			RenderHelper.drawColoredText(foundStr, cx + 12, curY + 28, RenderHelper.FS_CAPTION, RenderHelper.FONT_MUTED);
		}

		curY += crH + 24;
		return curY - y0;
	}

	private static float renderHotmTreeView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Loadout Selector Buttons (Slot 1 .. 5)
		float loadoutBtnW = 60f;
		float loadoutBtnH = 26f;
		float btnStartX = startX + (width - (5 * (loadoutBtnW + 8f) - 8f)) / 2f;

		for (int slot = 1; slot <= 5; slot++) {
			float bx = btnStartX + (slot - 1) * (loadoutBtnW + 8f);
			boolean isSel = (slot == activeLoadoutSlot);
			boolean hov = mx >= bx && mx <= bx + loadoutBtnW && my >= curY && my <= curY + loadoutBtnH;

			NVGRenderer.rect(bx, curY, loadoutBtnW, loadoutBtnH, isSel ? UIColors.ACCENT_BLUE : (hov ? 0x33FFFFFF : 0x221B1C26), 6f);
			NVGRenderer.outlineRect(bx, curY, loadoutBtnW, loadoutBtnH, 1f, isSel ? 0xFF55FFFF : 0x44FFFFFF, 6f);
			String slotTxt = "Slot " + slot;
			float stW = NVGRenderer.textWidth(slotTxt, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION);
			NVGRenderer.text(slotTxt, bx + (loadoutBtnW - stW) / 2f, curY + 6f, Fonts.PRETENDARD_SEMIBOLD, isSel ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
		}

		curY += loadoutBtnH + 18f;

		// 7x9 HOTM Skill Tree Card Layout
		float treeCardW = width;
		float treeCardH = 430f;
		RenderHelper.drawModernCard(startX, curY, treeCardW, treeCardH, 12f, false);

		// Grid Dimensions
		float slotSize = 34f;
		float slotGap = 6f;
		float tierColW = 34f;
		float gridStartX = startX + (treeCardW - (tierColW + 16f + 7 * (slotSize + slotGap) - slotGap)) / 2f;
		float gridStartY = curY + 20f;

		// 1. Tier nodes column (Tier 10 down to Tier 1 from top to bottom)
		for (int t = 10; t >= 1; t--) {
			int row = 10 - t;
			float tx = gridStartX;
			float ty = gridStartY + row * (slotSize + slotGap);

			HotmTreeData.HotmNode tierNode = HotmTreeData.NODES_BY_ID.get("tier_" + t);
			if (tierNode != null) {
				ItemStack icon = tierNode.getItemIcon(m.hotmLevel, false);
				boolean hov = mx >= tx && mx <= tx + slotSize && my >= ty && my <= ty + slotSize;

				NVGRenderer.rect(tx, ty, slotSize, slotSize, m.hotmLevel >= t ? 0xFF1E382B : (hov ? 0xFF2A2C3A : 0x55111218), 6f);
				NVGRenderer.outlineRect(tx, ty, slotSize, slotSize, 1.2f, m.hotmLevel >= t ? 0xFF55FF55 : (hov ? 0xFFFFFF55 : 0xFF555555), 6f);

				TreeSlotInfo info = new TreeSlotInfo(tx, ty, slotSize, tierNode, m.hotmLevel >= t ? 1 : 0, icon);
				visibleTreeSlots.add(info);
				if (hov) hoveredTreeSlot = info;
			}
		}

		// 2. 7-column Perk and Ability nodes (Tier 10 down to Tier 1)
		float nodeGridStartX = gridStartX + tierColW + 16f;

		for (HotmTreeData.HotmNode node : HotmTreeData.ALL_NODES) {
			if (node.type == HotmTreeData.NodeType.TIER) continue;

			int row = 9 - node.y; // invert y so Tier 10 is at top
			int col = node.x;

			float nx = nodeGridStartX + col * (slotSize + slotGap);
			float ny = gridStartY + row * (slotSize + slotGap);

			int lvl = m.nodes.getOrDefault(node.id, 0);
			boolean isSelAb = node.id.equalsIgnoreCase(m.selectedAbility);
			ItemStack icon = node.getItemIcon(lvl, isSelAb);

			boolean hov = mx >= nx && mx <= nx + slotSize && my >= ny && my <= ny + slotSize;

			int bgCol = isSelAb ? 0xFF1E382B : (lvl > 0 ? 0xFF1C2A3A : 0x5514151E);
			int borderCol = isSelAb ? 0xFF55FF55 : (lvl >= node.maxLevel ? 0xFF55FFFF : (lvl > 0 ? 0xFF3B82F6 : 0x44555566));

			NVGRenderer.rect(nx, ny, slotSize, slotSize, bgCol, 6f);
			NVGRenderer.outlineRect(nx, ny, slotSize, slotSize, 1.2f, borderCol, 6f);

			// Level badge at bottom-right of slot if > 1
			if (lvl > 1 && node.maxLevel > 1) {
				String lvlStr = String.valueOf(lvl);
				float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 9f);
				NVGRenderer.rect(nx + slotSize - lw - 4f, ny + slotSize - 11f, lw + 3f, 10f, 0xDD111218, 2f);
				NVGRenderer.text(lvlStr, nx + slotSize - lw - 2.5f, ny + slotSize - 10.5f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFFFFF, 9f);
			}

			TreeSlotInfo info = new TreeSlotInfo(nx, ny, slotSize, node, lvl, icon);
			visibleTreeSlots.add(info);
			if (hov) hoveredTreeSlot = info;
		}

		curY += treeCardH + 16f;
		return curY - y0;
	}

	private static float renderGearView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float cardH = 120f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.mining.subtab.gear"), startX + 16, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text(L10n.translate("pv.mining.drill"), startX + 16, curY + 36, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H3);
		NVGRenderer.text(L10n.translate("pv.mining.commissions"), startX + 16, curY + 68, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);

		curY += cardH + 16f;
		return curY - y0;
	}

	private static float renderGlaciteView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float cardH = 90f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 10f, false);
		NVGRenderer.text(L10n.translate("pv.mining.subtab.glacite"), startX + 16, curY + 14, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, RenderHelper.FS_CAPTION);
		NVGRenderer.text("Glacite Powder: " + RenderHelper.formatNumber(m.glacitePowder), startX + 16, curY + 36, Fonts.PRETENDARD_SEMIBOLD, 0xFF55FFFF, RenderHelper.FS_H3);
		NVGRenderer.text(L10n.translate("pv.mining.glacite_corpses"), startX + 16, curY + 66, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);

		curY += cardH + 16f;
		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 28f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BUTTON) + 20f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}

		if (activeSubTab == MiningSubTab.HOTM_TREE) {
			float loadoutBtnW = 60f;
			float loadoutBtnH = 26f;
			float btnStartX = startX + (width - (5 * (loadoutBtnW + 8f) - 8f)) / 2f;
			float btnY = startY + subTabH + 18f;

			for (int slot = 1; slot <= 5; slot++) {
				float bx = btnStartX + (slot - 1) * (loadoutBtnW + 8f);
				if (mx >= bx && mx <= bx + loadoutBtnW && my >= btnY && my <= btnY + loadoutBtnH) {
					activeLoadoutSlot = slot;
					return true;
				}
			}
		}

		return false;
	}
}