package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
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
		MAIN("pv.mining.subtab.main", "\uE52F"),
		HOTM_TREE("pv.mining.subtab.hotm_tree", "\uE8EF"),
		GEAR("pv.mining.subtab.gear", "\uE8C9"),
		GLACITE("pv.mining.subtab.glacite", "\uE3E8");

		public final String translationKey;
		public final String icon;
		MiningSubTab(String translationKey, String icon) {
			this.translationKey = translationKey;
			this.icon = icon;
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

		// Sub-tabs bar (Modern Pills)
		float subTabH = 32f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
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
			case MAIN -> curY += renderMainView(m, startX, curY, width, mouseX, mouseY);
			case HOTM_TREE -> curY += renderHotmTreeView(m, startX, curY, width, mouseX, mouseY);
			case GEAR -> curY += renderGearView(data, startX, curY, width, mouseX, mouseY);
			case GLACITE -> curY += renderGlaciteView(m, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderMainView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Card (HOTM Level)
		float cardH = 80f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 12f, false);

		float iconBoxSize = 40f;
		float ix = startX + 16f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		int hotmCol = 0xFF10B981;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(hotmCol, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE52F", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, hotmCol, 21f);

		float tx = ix + iconBoxSize + 14f;
		NVGRenderer.text(L10n.translate("pv.mining.hotm"), tx, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(L10n.translate("pv.ui.tier") + " " + m.hotmLevel + " / 10", tx, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, hotmCol, 19f);

		String potmStr = L10n.translate("pv.mining.potm") + ": " + m.peakOfTheMountain + "  •  " + L10n.translate("pv.mining.nucleus_runs") + ": " + m.nucleusRuns;
		NVGRenderer.text(potmStr, startX + width - 280f, curY + 32f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 14f);

		curY += cardH + 20f;

		// 3 Powders Row
		NVGRenderer.text("\uE3E8", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
		NVGRenderer.text(L10n.translate("pv.mining.powders"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float colW = (width - 24f) / 3f;
		float pH = 76f;

		// Mithril Powder
		RenderHelper.drawModernCard(startX, curY, colW, pH, 12f, false);
		float ixP = startX + 14f;
		float iyP = curY + (pH - iconBoxSize) / 2f;
		NVGRenderer.rect(ixP, iyP, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF10B981, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE52F", ixP + 9.5f, iyP + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 21f);
		float txP = ixP + iconBoxSize + 12f;
		NVGRenderer.text("Mithril Powder", txP, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(RenderHelper.formatNumber(m.mithrilPowder), txP, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 18f);

		// Gemstone Powder
		float gX = startX + colW + 12f;
		RenderHelper.drawModernCard(gX, curY, colW, pH, 12f, false);
		float ixG = gX + 14f;
		NVGRenderer.rect(ixG, iyP, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFF55FF, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE3E8", ixG + 9.5f, iyP + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFF55FF, 21f);
		float txG = ixG + iconBoxSize + 12f;
		NVGRenderer.text("Gemstone Powder", txG, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(RenderHelper.formatNumber(m.gemstonePowder), txG, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF55FF, 18f);

		// Glacite Powder
		float glX = gX + colW + 12f;
		RenderHelper.drawModernCard(glX, curY, colW, pH, 12f, false);
		float ixGl = glX + 14f;
		NVGRenderer.rect(ixGl, iyP, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", ixGl + 9.5f, iyP + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);
		float txGl = ixGl + iconBoxSize + 12f;
		NVGRenderer.text("Glacite Powder", txGl, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
		NVGRenderer.text(RenderHelper.formatNumber(m.glacitePowder), txGl, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 18f);

		curY += pH + 24f;

		// Crystal Nucleus Crystals (5 Gems)
		NVGRenderer.text("\uE838", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 18f);
		NVGRenderer.text("CRYSTAL NUCLEUS (5 GEMS)", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float crW = (width - 4 * 10f) / 5f;
		float crH = 58f;
		String[] crystals = {"jade_crystal", "amethyst_crystal", "topaz_crystal", "sapphire_crystal", "amber_crystal"};
		String[] cNames = {"Jade", "Amethyst", "Topaz", "Sapphire", "Amber"};
		int[] cColors = {0xFF10B981, 0xFFA855F7, 0xFFF59E0B, 0xFF38BDF8, 0xFFFBBF24};

		for (int i = 0; i < crystals.length; i++) {
			float cx = startX + i * (crW + 10f);
			boolean found = m.crystals.getOrDefault(crystals[i], false);
			RenderHelper.drawModernCard(cx, curY, crW, crH, 10f, false);
			NVGRenderer.text(cNames[i], cx + 14f, curY + 10f, Fonts.PRETENDARD_SEMIBOLD, cColors[i], 14f);
			String foundStr = found ? L10n.translate("pv.ui.unlocked") : L10n.translate("pv.ui.locked");
			int statCol = found ? 0xFF10B981 : RenderHelper.FONT_DISABLED;
			NVGRenderer.text(foundStr, cx + 14f, curY + 32f, Fonts.PRETENDARD_MEDIUM, statCol, 13f);
		}

		curY += crH + 20f;
		return curY - y0;
	}

	private static float renderHotmTreeView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Loadout Selector Buttons (Slot 1 .. 5)
		float loadoutBtnW = 70f;
		float loadoutBtnH = 30f;
		float btnStartX = startX + (width - (5 * (loadoutBtnW + 8f) - 8f)) / 2f;

		for (int slot = 1; slot <= 5; slot++) {
			float bx = btnStartX + (slot - 1) * (loadoutBtnW + 8f);
			boolean isSel = (slot == activeLoadoutSlot);
			boolean hov = mx >= bx && mx <= bx + loadoutBtnW && my >= curY && my <= curY + loadoutBtnH;

			NVGRenderer.rect(bx, curY, loadoutBtnW, loadoutBtnH, isSel ? 0xBF4F46E5 : (hov ? 0x1AFFFFFF : 0x44141624), 8f);
			NVGRenderer.outlineRect(bx, curY, loadoutBtnW, loadoutBtnH, 1f, isSel ? 0xFF818CF8 : 0x22FFFFFF, 8f);
			String slotTxt = "Slot " + slot;
			float stW = NVGRenderer.textWidth(slotTxt, Fonts.PRETENDARD_SEMIBOLD, 13f);
			NVGRenderer.text(slotTxt, bx + (loadoutBtnW - stW) / 2f, curY + 8f, Fonts.PRETENDARD_SEMIBOLD, isSel ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_SECONDARY, 13f);
		}

		curY += loadoutBtnH + 18f;

		// 7x9 HOTM Skill Tree Card Layout
		float treeCardW = width;
		float treeCardH = 440f;
		RenderHelper.drawModernCard(startX, curY, treeCardW, treeCardH, 14f, false);

		// Grid Dimensions
		float slotSize = 36f;
		float slotGap = 6f;
		float tierColW = 36f;
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

				NVGRenderer.rect(tx, ty, slotSize, slotSize, m.hotmLevel >= t ? 0xFF1E382B : (hov ? 0xFF2A2C3A : 0x55111218), 8f);
				NVGRenderer.outlineRect(tx, ty, slotSize, slotSize, 1.2f, m.hotmLevel >= t ? 0xFF10B981 : (hov ? 0xFFFFAA00 : 0xFF374151), 8f);

				TreeSlotInfo info = new TreeSlotInfo(tx, ty, slotSize, tierNode, m.hotmLevel >= t ? 1 : 0, icon);
				visibleTreeSlots.add(info);
				if (hov) hoveredTreeSlot = info;
			}
		}

		// 2. 7-column Perk and Ability nodes (Tier 10 down to Tier 1)
		float nodeGridStartX = gridStartX + tierColW + 16f;

		for (HotmTreeData.HotmNode node : HotmTreeData.ALL_NODES) {
			if (node.type == HotmTreeData.NodeType.TIER) continue;

			int row = 9 - node.y;
			int col = node.x;

			float nx = nodeGridStartX + col * (slotSize + slotGap);
			float ny = gridStartY + row * (slotSize + slotGap);

			int lvl = m.nodes.getOrDefault(node.id, 0);
			boolean isSelAb = node.id.equalsIgnoreCase(m.selectedAbility);
			ItemStack icon = node.getItemIcon(lvl, isSelAb);

			boolean hov = mx >= nx && mx <= nx + slotSize && my >= ny && my <= ny + slotSize;

			int bgCol = isSelAb ? 0xFF1E382B : (lvl > 0 ? 0xFF1C2A3A : 0x5514151E);
			int borderCol = isSelAb ? 0xFF10B981 : (lvl >= node.maxLevel ? 0xFF38BDF8 : (lvl > 0 ? 0xFF6366F1 : 0x334B5563));

			NVGRenderer.rect(nx, ny, slotSize, slotSize, bgCol, 8f);
			NVGRenderer.outlineRect(nx, ny, slotSize, slotSize, 1.2f, borderCol, 8f);

			// Level badge at bottom-right of slot if > 1
			if (lvl > 1 && node.maxLevel > 1) {
				String lvlStr = String.valueOf(lvl);
				float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 9.5f);
				NVGRenderer.rect(nx + slotSize - lw - 5f, ny + slotSize - 12f, lw + 4f, 11f, 0xDD111218, 3f);
				NVGRenderer.text(lvlStr, nx + slotSize - lw - 3f, ny + slotSize - 11.5f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 9.5f);
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
		float cardH = 100f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 12f, false);
		NVGRenderer.text(L10n.translate("pv.mining.subtab.gear"), startX + 16f, curY + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(L10n.translate("pv.mining.drill"), startX + 16f, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 17f);
		NVGRenderer.text(L10n.translate("pv.mining.commissions"), startX + 16f, curY + 64f, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, 14f);

		curY += cardH + 16f;
		return curY - y0;
	}

	private static float renderGlaciteView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float cardH = 90f;
		RenderHelper.drawModernCard(startX, curY, width, cardH, 12f, false);
		NVGRenderer.text(L10n.translate("pv.mining.subtab.glacite"), startX + 16f, curY + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text("Glacite Powder: " + RenderHelper.formatNumber(m.glacitePowder), startX + 16f, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 17f);
		NVGRenderer.text(L10n.translate("pv.mining.glacite_corpses"), startX + 16f, curY + 62f, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, 14f);

		curY += cardH + 16f;
		return curY - y0;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 32f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}

		if (activeSubTab == MiningSubTab.HOTM_TREE) {
			float loadoutBtnW = 70f;
			float loadoutBtnH = 30f;
			float btnStartX = startX + (width - (5 * (loadoutBtnW + 8f) - 8f)) / 2f;
			float btnY = startY + subTabH + 16f;

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