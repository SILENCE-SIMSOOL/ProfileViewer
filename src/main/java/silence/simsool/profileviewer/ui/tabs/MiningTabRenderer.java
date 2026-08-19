package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.GearFinder;
import silence.simsool.profileviewer.api.data.HotmTreeData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MiningData;
import silence.simsool.profileviewer.api.data.PetData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
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
	public static final List<TreeSlotInfo> visibleTreeSlots = new ArrayList<>();
	public static TreeSlotInfo hoveredTreeSlot = null;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		float curY = startY;
		MiningData m = (data != null) ? data.mining : new MiningData();
		visibleTreeSlots.clear();
		hoveredTreeSlot = null;

		// Sub-tabs bar (Overview, HOTM Tree, Glacite)
		float subTabH = 32f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
			String title = st.getTitle();
			float stW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			int bgCol = active ? 0xFF0284C7 : (hov ? 0x330284C7 : 0x1AFFFFFF);
			int textCol = active ? 0xFFFFFFFF : (hov ? 0xFF7DD3FC : RenderHelper.FONT_MUTED);

			NVGRenderer.rect(subTabX, curY, stW, subTabH, bgCol, 7f);
			if (active) {
				NVGRenderer.outlineRect(subTabX, curY, stW, subTabH, 1.2f, 0xFF38BDF8, 7f);
			}

			NVGRenderer.text(st.icon, subTabX + 10f, curY + 8f, Fonts.MATERIAL_ICONS_ROUND, textCol, 16f);
			NVGRenderer.text(title, subTabX + 30f, curY + 8.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 14f);

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
		float topH = 200f;

		// 1. HOTM Information
		RenderHelper.drawModernCard(startX, curY, c1W, topH, 10f, false);
		NVGRenderer.text("HOTM Information", startX + (c1W - NVGRenderer.textWidth("HOTM Information", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

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
		NVGRenderer.text("Powders", c2X + (c2W - NVGRenderer.textWidth("Powders", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

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
		NVGRenderer.text("Mining Gear", c3X + (c3W - NVGRenderer.textWidth("Mining Gear", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 14f);

		float slotSize = 30f;
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

		// Col 2: Mining Tools / Drills
		List<ItemStack> miningTools = getMiningDrills(data);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= gCol2X && mx <= gCol2X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol2X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = (r < miningTools.size()) ? miningTools.get(r) : ItemStack.EMPTY;
			RenderHelper.registerItemSlot(gCol2X, sy, slotSize, st);
		}

		// Col 3: Mining Equipment
		List<ItemStack> miningEq = GearFinder.findEquipmentSet(data, GearFinder.MINING_EQUIPMENT);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= gCol3X && mx <= gCol3X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol3X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = miningEq.get(r);
			RenderHelper.registerItemSlot(gCol3X, sy, slotSize, st);
		}

		// Col 4: Mining Pets
		List<PetData.PetItem> miningPets = getMiningPets(data);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= gCol4X && mx <= gCol4X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol4X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			if (r < miningPets.size()) {
				PetData.PetItem p = miningPets.get(r);
				RenderHelper.registerItemSlot(gCol4X, sy, slotSize, p.itemStack, String.valueOf(p.level), p.getRarityColor());
			} else {
				RenderHelper.registerItemSlot(gCol4X, sy, slotSize, ItemStack.EMPTY);
			}
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
		float treeCardH = treeH + 60f;

		RenderHelper.drawModernCard(startX, curY, width, treeCardH, 12f, false);
		NVGRenderer.text("\uE8EF", startX + 16f, curY + 16f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 18f);
		NVGRenderer.text("Heart of the Mountain (HOTM)", startX + 40f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		float startTreeX = startX + (width - treeW) / 2f;
		float startTreeY = curY + 45f;

		for (HotmTreeData.HotmNode node : HotmTreeData.ALL_NODES) {
			if (node.type == HotmTreeData.NodeType.TIER || node.type == HotmTreeData.NodeType.SPACER) continue;

			int r = 9 - node.y;
			int c = node.x;
			if (r < 0 || r >= rows || c < 0 || c >= cols) continue;

			float sx = startTreeX + c * (slotSize + slotGap);
			float sy = startTreeY + r * (slotSize + slotGap);

			int level = node.getNodeLevel(m.nodes);
			boolean isSelAb = (node.type == HotmTreeData.NodeType.ABILITY && node.matchesAbility(m.selectedAbility));
			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;

			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, isSelAb ? 0xFF34D399 : 0x33FFFFFF, isSelAb ? 0xFF1E382B : 0x5514151E, 5f);

			ItemStack stack = node.createParsedItem(level, !node.matchesAbility(m.selectedAbility) && node.type == HotmTreeData.NodeType.ABILITY, m.selectedAbility, m.hotmLevel).toItemStack();
			boolean isMaxed = (level >= node.maxLevel);

			String customText = (level > 0 && !isMaxed && node.type != HotmTreeData.NodeType.ABILITY && node.type != HotmTreeData.NodeType.UNLEVELABLE) ? String.valueOf(level) : null;
			int textColor = isSelAb ? 0xFF34D399 : 0xFFFFFFFF;

			RenderHelper.registerItemSlot(sx, sy, slotSize, stack, customText, textColor);
			visibleTreeSlots.add(new TreeSlotInfo(sx, sy, slotSize, node, level, stack));
		}

		curY += treeCardH + 16f;
		return curY - y0;
	}

	private static float renderGlaciteView(MiningData m, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		RenderHelper.drawModernCard(startX, curY, width, 200f, 12f, false);
		NVGRenderer.text("Glacite Mineshafts", startX + 16f, curY + 16f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 216f;
		return curY - y0;
	}

	private static List<ItemStack> getMiningDrills(MemberData data) {
		List<ItemStack> res = new ArrayList<>();
		List<ParsedItem> all = GearFinder.getAllPlayerItems(data);
		for (ParsedItem pi : all) {
			if (pi != null && !pi.isEmpty()) {
				String id = pi.skyblockId.toUpperCase();
				if (id.contains("DRILL") || id.contains("PICKAXE") || id.contains("STONK") || id.contains("PICKONIMBUS")) {
					res.add(pi.itemStack);
					if (res.size() >= 4) break;
				}
			}
		}
		while (res.size() < 4) {
			res.add(ItemStack.EMPTY);
		}

		return res;
	}

	private static List<PetData.PetItem> getMiningPets(MemberData data) {
		List<PetData.PetItem> res = new ArrayList<>();
		if (data != null && data.pets != null && data.pets.pets != null) {
			for (PetData.PetItem p : data.pets.pets) {
				String type = p.type.toUpperCase();
				if (type.contains("SCATHA") || type.contains("BAL") || type.contains("ARMADILLO") || type.contains("SILVERFISH") || type.contains("MITHRIL") || type.contains("GLACITE")) {
					res.add(p);
					if (res.size() >= 4) break;
				}
			}
			if (res.isEmpty() && !data.pets.pets.isEmpty()) {
				for (int i = 0; i < Math.min(4, data.pets.pets.size()); i++) {
					res.add(data.pets.pets.get(i));
				}
			}
		}
		return res;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 32f;
		float subTabX = startX;
		for (MiningSubTab st : MiningSubTab.values()) {
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}