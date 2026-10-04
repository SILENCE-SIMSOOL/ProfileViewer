package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.GearFinder;
import silence.simsool.profileviewer.api.data.HotmTreeData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MiningData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.RenderHelper;

public class MiningTabRenderer {

	public enum MiningSubTab {
		MAIN("pv.mining.subtab.main", "Overview", "\uE52F"),
		HOTM_TREE("pv.mining.subtab.hotm_tree", "HOTM Tree", "\uE8EF"),
		GLACITE("pv.mining.subtab.glacite", "Glacite", "\uE3E8");

		public final String translationKey;
		public final String defaultName;
		public final String icon;

		MiningSubTab(String translationKey, String defaultName, String icon) {
			this.translationKey = translationKey;
			this.defaultName = defaultName;
			this.icon = icon;
		}

		public String getTitle() {
			String trans = L10n.translate(translationKey);
			return (trans != null && !trans.equals(translationKey)) ? trans : defaultName;
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
	private static MiningData lastMiningData;
	public static final List<TreeSlotInfo> visibleTreeSlots = new ArrayList<>();
	public static TreeSlotInfo hoveredTreeSlot = null;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		float curY = startY;
		MiningData m = (data != null) ? data.mining : new MiningData();
		if (lastMiningData != m) {
			activeLoadoutSlot = Math.max(1, Math.min(5, m.selectedMiningPreset));
			lastMiningData = m;
		}
		visibleTreeSlots.clear();
		hoveredTreeSlot = null;

		// Sub-tabs bar (Overview, HOTM Tree, Glacite)
		float subTabH = 32f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
			String title = st.getTitle();
			float stW = SkijaRenderer.textWidth(title, Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			int bgCol = active ? 0xFF0284C7 : (hov ? 0x330284C7 : 0x1AFFFFFF);
			int textCol = active ? 0xFFFFFFFF : (hov ? 0xFF7DD3FC : RenderHelper.FONT_MUTED);

			SkijaRenderer.rect(subTabX, curY, stW, subTabH, bgCol, 7f);
			if (active) {
				SkijaRenderer.outlineRect(subTabX, curY, stW, subTabH, 1.2f, 0xFF38BDF8, 7f);
			}

			RenderHelper.alignedIcon(st.icon, subTabX + 10f, curY + 8.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 16f, 14f);
			SkijaRenderer.text(title, subTabX + 30f, curY + 8.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 14f);

			subTabX += stW + 8f;
		}

		curY += subTabH + 16f;

		switch (activeSubTab) {
			case MAIN -> curY += renderMainOverview(data, m, startX, curY, width, mouseX, mouseY);
			case HOTM_TREE -> curY += renderHotmTreeView(m, startX, curY, width, mouseX, mouseY);
			case GLACITE -> curY += renderGlaciteView(m, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	// =========================================================================
	// 1. MAIN OVERVIEW (Information, Powder, Mining Gear 통합)
	// =========================================================================
	private static float renderMainOverview(MemberData data, MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float gap = 14f;

		// Top Row: HOTM Info (38%) + Powder Card (30%) + Mining Gear Card (32%)
		float c1W = (width - gap * 2) * 0.35f;
		float c2W = (width - gap * 2) * 0.31f;
		float c3W = width - gap * 2 - c1W - c2W;
		float topH = 224f;

		// 1. HOTM Information
		RenderHelper.drawModernCard(startX, curY, c1W, topH, 10f, false);
		SkijaRenderer.text("HOTM Information", startX + (c1W - SkijaRenderer.textWidth("HOTM Information", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

		float infoY = curY + 36f;
		float rowH = 24f;
		RenderHelper.drawStatRow("HOTM Level", "Tier " + m.hotmLevel + " / 10", startX + 14f, infoY, c1W - 28f, 13.5f, 0xFF38BDF8);
		infoY += rowH;
		RenderHelper.drawStatRow("Selected Ability", m.selectedAbility.isEmpty() ? "None" : m.selectedAbility.replace("_", " "), startX + 14f, infoY, c1W - 28f, 13.5f, 0xFFF59E0B);
		infoY += rowH;
		RenderHelper.drawStatRow("Mithril Powder", RenderHelper.formatNumber(m.mithrilPowder), startX + 14f, infoY, c1W - 28f, 13.5f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Gemstone Powder", RenderHelper.formatNumber(m.gemstonePowder), startX + 14f, infoY, c1W - 28f, 13.5f, 0xFFEC4899);
		infoY += rowH;
		RenderHelper.drawStatRow("Glacite Powder", RenderHelper.formatNumber(m.glacitePowder), startX + 14f, infoY, c1W - 28f, 13.5f, 0xFF06B6D4);

		// 2. Powder Details
		float c2X = startX + c1W + gap;
		RenderHelper.drawModernCard(c2X, curY, c2W, topH, 10f, false);
		SkijaRenderer.text("Powders", c2X + (c2W - SkijaRenderer.textWidth("Powders", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

		float pY = curY + 36f;
		RenderHelper.drawStatRow("Mithril Available", RenderHelper.formatNumber(m.mithrilPowder), c2X + 14f, pY, c2W - 28f, 13.5f, 0xFF10B981);
		pY += rowH;
		RenderHelper.drawStatRow("Gemstone Available", RenderHelper.formatNumber(m.gemstonePowder), c2X + 14f, pY, c2W - 28f, 13.5f, 0xFFEC4899);
		pY += rowH;
		RenderHelper.drawStatRow("Glacite Available", RenderHelper.formatNumber(m.glacitePowder), c2X + 14f, pY, c2W - 28f, 13.5f, 0xFF06B6D4);
		pY += rowH;
		RenderHelper.drawStatRow("Peak of the Mountain", "Level " + m.peakOfTheMountain, c2X + 14f, pY, c2W - 28f, 13.5f, 0xFFFBBF24);


		// 3. Mining Gear Card (Armor + Drill + Equipment + Pets)
		float c3X = c2X + c2W + gap;
		RenderHelper.drawModernCard(c3X, curY, c3W, topH, 10f, false);
		SkijaRenderer.text("Mining Gear", c3X + (c3W - SkijaRenderer.textWidth("Mining Gear", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

		float slotSize = 40f;
		float slotGap = 4f;
		float gTopY = curY + 34f;
		float gCol1X = c3X + (c3W - (4 * slotSize + 3 * slotGap)) / 2f;
		float gCol2X = gCol1X + slotSize + slotGap;
		float gCol3X = gCol2X + slotSize + slotGap;
		float gCol4X = gCol3X + slotSize + slotGap;

		// Col 1: Mining Armor
		List<ItemStack> miningArmor = GearFinder.findArmorSet(data, GearFinder.MINING_HELMETS, GearFinder.MINING_CHESTPLATES, GearFinder.MINING_LEGGINGS, GearFinder.MINING_BOOTS);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= gCol1X && mx <= gCol1X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol1X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = miningArmor.get(r);
			RenderHelper.registerItemSlot(gCol1X, sy, slotSize, st);
		}

		// Col 3: Mining Tools / Drills
		List<ItemStack> miningTools = getMiningDrills(data);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= gCol3X && mx <= gCol3X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol3X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = (r < miningTools.size()) ? miningTools.get(r) : ItemStack.EMPTY;
			RenderHelper.registerItemSlot(gCol3X, sy, slotSize, st);
		}

		// Col 2: Mining Equipment
		List<ItemStack> miningEq = GearFinder.findEquipmentSet(data, GearFinder.MINING_EQUIPMENT);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= gCol2X && mx <= gCol2X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol2X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = miningEq.get(r);
			RenderHelper.registerItemSlot(gCol2X, sy, slotSize, st);
		}

		// Col 4: Chisel and Suspicious Scrap, matching the original mining gear screen
		ItemStack chisel = GearFinder.findBestItem(data, GearFinder.MINING_CHISELS);
		ItemStack scrap = ItemRepo.getItemStack("SUSPICIOUS_SCRAP");
		long scrapCount = GearFinder.countItems(data, GearFinder.MINING_SCRAP);
		for (int r = 0; r < 2; r++) {
			float sy = gTopY + (r + 1) * (slotSize + slotGap);
			boolean hov = mx >= gCol4X && mx <= gCol4X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol4X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			if (r == 0) RenderHelper.registerItemSlot(gCol4X, sy, slotSize, chisel);
			else if (r == 1 && !scrap.isEmpty()) RenderHelper.registerItemSlot(gCol4X, sy, slotSize, scrap, RenderHelper.formatNumber(scrapCount), 0xFFFFFFFF);
		}

		curY += topH + 16f;
		return curY - y0;
	}

	// =========================================================================
	// 2. HOTM TREE VIEW
	// =========================================================================
	private static float renderHotmTreeView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float slotSize = 34f;
		float slotGap = 6f;
		int cols = 7;
		int rows = 10;

		float treeW = cols * slotSize + (cols - 1) * slotGap;
		float treeH = rows * slotSize + (rows - 1) * slotGap;
		float treeCardH = treeH + 116f;

		RenderHelper.drawModernCard(startX, curY, width, treeCardH, 12f, false);
		RenderHelper.alignedIcon("\uE8EF", startX + 16f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 18f, 16f);
		SkijaRenderer.text("Heart of the Mountain (HOTM)", startX + 40f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		renderLoadoutSlots(m, startX, curY + 42f, width, mx, my);
		float startTreeX = startX + (width - treeW) / 2f;
		float startTreeY = curY + 101f;
		Map<String, Integer> activeNodes = m.presetNodes.getOrDefault(activeLoadoutSlot, Map.of());
		String activeAbility = m.presetAbilities.getOrDefault(activeLoadoutSlot, "");

		for (HotmTreeData.HotmNode node : HotmTreeData.ALL_NODES) {
			if (node.type == HotmTreeData.NodeType.TIER || node.type == HotmTreeData.NodeType.SPACER) continue;

			int r = 9 - node.y;
			int c = node.x;
			if (r < 0 || r >= rows || c < 0 || c >= cols) continue;

			float sx = startTreeX + c * (slotSize + slotGap);
			float sy = startTreeY + r * (slotSize + slotGap);

			int level = node.getNodeLevel(activeNodes);
			boolean isSelAb = (node.type == HotmTreeData.NodeType.ABILITY && node.matchesAbility(activeAbility));
			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;

			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, isSelAb ? 0xFF34D399 : 0x33FFFFFF, isSelAb ? 0xFF1E382B : 0x5514151E, 5f);

			ItemStack stack = node.createParsedItem(level, !node.matchesAbility(activeAbility) && node.type == HotmTreeData.NodeType.ABILITY, activeAbility, m.hotmLevel).toItemStack();
			boolean isMaxed = (level >= node.maxLevel);

			String customText = (level > 0 && !isMaxed && node.type != HotmTreeData.NodeType.ABILITY && node.type != HotmTreeData.NodeType.UNLEVELABLE) ? String.valueOf(level) : null;
			int textColor = isSelAb ? 0xFF34D399 : 0xFFFFFFFF;

			RenderHelper.registerItemSlot(sx, sy, slotSize, stack, customText, textColor);
		}

		curY += treeCardH + 16f;
		return curY - y0;
	}

	private static void renderLoadoutSlots(MiningData m, float startX, float y, float width, float mx, float my) {
		float size = 40f;
		float gap = 8f;
		float x = startX + (width - (size * 5f + gap * 4f)) / 2f;
		for (int slot = 1; slot <= 5; slot++) {
			float sx = x + (slot - 1) * (size + gap);
			boolean selected = slot == activeLoadoutSlot;
			boolean hover = mx >= sx && mx <= sx + size && my >= y && my <= y + size;
			RenderHelper.drawItemSlotBg(sx, y, size, hover, selected ? 0xFF38BDF8 : 0x33FFFFFF, selected ? 0xFF123047 : 0x5514151E, 6f);
			ItemStack icon = ItemRepo.getItemStack("HEART_OF_THE_MOUNTAIN");
			if (icon.isEmpty()) icon = new ItemStack(net.minecraft.world.item.Items.EMERALD);
			RenderHelper.registerItemSlot(sx, y, size, icon, String.valueOf(slot), selected ? 0xFF7DD3FC : 0xFFFFFFFF);
		}
	}

	private static float renderGlaciteView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float gap = 14f;
		float cardW = (width - gap) / 2f;
		float cardH = 220f;

		MiningData.GlaciteData g = (m != null && m.glacite != null) ? m.glacite : new MiningData.GlaciteData();

		// 1. Left Card: Mineshafts & Corpses Looted
		RenderHelper.drawModernCard(startX, curY, cardW, cardH, 12f, false);
		RenderHelper.alignedIcon("\uE3E8", startX + 14f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 16f, 13.5f);
		SkijaRenderer.text("Glacite Mineshafts & Corpses", startX + 34f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);

		float rowY = curY + 38f;
		float rowH = 24f;
		float padW = cardW - 28f;
		RenderHelper.drawStatRow("Mineshafts Entered", RenderHelper.formatNumber(g.mineshaftsEntered), startX + 14f, rowY, padW, 13.5f, 0xFF38BDF8);
		rowY += rowH;
		RenderHelper.drawStatRow("Total Corpses Looted", RenderHelper.formatNumber(g.totalCorpses), startX + 14f, rowY, padW, 13.5f, 0xFFFFFFFF);
		rowY += rowH;
		RenderHelper.drawStatRow("Lapis Corpses", RenderHelper.formatNumber(g.corpsesLooted.getOrDefault("lapis", 0)), startX + 14f, rowY, padW, 13.5f, 0xFF60A5FA);
		rowY += rowH;
		RenderHelper.drawStatRow("Tungsten Corpses", RenderHelper.formatNumber(g.corpsesLooted.getOrDefault("tungsten", 0)), startX + 14f, rowY, padW, 13.5f, 0xFF9CA3AF);
		rowY += rowH;
		RenderHelper.drawStatRow("Umber Corpses", RenderHelper.formatNumber(g.corpsesLooted.getOrDefault("umber", 0)), startX + 14f, rowY, padW, 13.5f, 0xFFFBBF24);
		rowY += rowH;
		RenderHelper.drawStatRow("Vanguard Corpses", RenderHelper.formatNumber(g.corpsesLooted.getOrDefault("vanguard", 0)), startX + 14f, rowY, padW, 13.5f, 0xFF2DD4BF);

		// 2. Right Card: Fossils Donated (8 known fossils)
		float rightX = startX + cardW + gap;
		RenderHelper.drawModernCard(rightX, curY, cardW, cardH, 12f, false);
		RenderHelper.alignedIcon("\uE88F", rightX + 14f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF59E0B, 16f, 13.5f);
		SkijaRenderer.text("Fossils Donated (" + g.fossilsDonated.size() + " / 8)", rightX + 34f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);

		String[][] fossils = {
			{"clubbed", "Clubbed Fossil"},
			{"spined", "Spined Fossil"},
			{"webbed", "Webbed Fossil"},
			{"clawed", "Clawed Fossil"},
			{"tusked", "Tusked Fossil"},
			{"horned", "Horned Fossil"},
			{"helix", "Helix Fossil"},
			{"footprint", "Footprint Fossil"}
		};

		float fColW = (cardW - 36f) / 2f;
		float fRowH = 34f;
		float fStartY = curY + 40f;

		for (int i = 0; i < fossils.length; i++) {
			int col = i % 2;
			int row = i / 2;
			float fx = rightX + 14f + col * (fColW + 8f);
			float fy = fStartY + row * (fRowH + 6f);

			String fKey = fossils[i][0];
			String fName = fossils[i][1];

			boolean donated = false;
			for (String don : g.fossilsDonated) {
				if (don.contains(fKey)) {
					donated = true;
					break;
				}
			}

			int bgCol = donated ? 0x2210B981 : 0x14FFFFFF;
			int borderCol = donated ? 0x6610B981 : 0x1AFFFFFF;
			int textCol = donated ? 0xFFFFFFFF : RenderHelper.FONT_MUTED;

			SkijaRenderer.rect(fx, fy, fColW, fRowH, bgCol, 6f);
			SkijaRenderer.outlineRect(fx, fy, fColW, fRowH, 1f, borderCol, 6f);

			RenderHelper.alignedIcon(donated ? "\uE86C" : "\uE5C9", fx + 8f, fy + 9.5f, Fonts.PRETENDARD_MEDIUM, donated ? 0xFF10B981 : 0xFFEF4444, 15f, 12.5f);
			SkijaRenderer.text(fName, fx + 28f, fy + 9.5f, Fonts.PRETENDARD_MEDIUM, textCol, 12.5f);
		}

		curY += cardH + 16f;
		return curY - y0;
	}

	private static List<ItemStack> getMiningDrills(MemberData data) {
		List<ItemStack> res = new ArrayList<>();
		for (ParsedItem item : GearFinder.findBestItems(data, GearFinder.MINING_PICKAXES, 4)) res.add(item.itemStack);
		while (res.size() < 4) {
			res.add(ItemStack.EMPTY);
		}

		return res;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 32f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
			float stW = SkijaRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		if (activeSubTab == MiningSubTab.HOTM_TREE) {
			float size = 40f;
			float gap = 8f;
			float y = startY + subTabH + 16f + 42f;
			float x = startX + (width - (size * 5f + gap * 4f)) / 2f;
			for (int slot = 1; slot <= 5; slot++) {
				float sx = x + (slot - 1) * (size + gap);
				if (mx >= sx && mx <= sx + size && my >= y && my <= y + size) {
					activeLoadoutSlot = slot;
					return true;
				}
			}
		}
		return false;
	}
}