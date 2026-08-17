package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.lucent.ui.widget.components.TextBox;
import silence.simsool.profileviewer.api.data.HotmTreeData;
import silence.simsool.profileviewer.api.data.InventoryData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MiningData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.ui.RenderHelper;

public class GearTabRenderer {

	public static class SlotRenderInfo {
		public float x, y, size;
		public ParsedItem item;
		public SlotRenderInfo(float x, float y, float size, ParsedItem item) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.item = item;
		}
	}

	public enum GearSubTab {
		INVENTORY("pv.gear.subtab.inventory", "Inventory", "\uE8C9"),
		WARDROBE("pv.gear.subtab.wardrobe", "Wardrobe", "\uE52E"),
		LOADOUT("pv.gear.subtab.loadout", "Loadout", "\uE8EF"),
		ENDERCHEST("pv.gear.subtab.enderchest", "Ender Chest", "\uE8F9"),
		BACKPACKS("pv.gear.subtab.backpacks", "Backpacks", "\uE8B0"),
		ACCESSORIES("pv.gear.subtab.accessories", "Accessary", "\uEA5F");

		public final String translationKey;
		public final String defaultName;
		public final String icon;

		GearSubTab(String translationKey, String defaultName, String icon) {
			this.translationKey = translationKey;
			this.defaultName = defaultName;
			this.icon = icon;
		}

		public String getTitle() {
			String trans = L10n.translate(translationKey);
			return (trans != null && !trans.equals(translationKey)) ? trans : defaultName;
		}
	}

	public static GearSubTab activeSubTab = GearSubTab.INVENTORY;
	public static int selectedLoadoutId = 1;
	public static final List<SlotRenderInfo> visibleSlots = new ArrayList<>();
	public static ParsedItem hoveredItem = null;
	public static float hoveredItemX = 0, hoveredItemY = 0;

	// Search box for accessories tab
	public static final TextBox searchBox = new TextBox(0, 0, 180, 26, "");

	// Clickable areas for loadout preset selectors
	private static class LoadoutButtonBounds {
		float x, y, w, h;
		int id;
		LoadoutButtonBounds(float x, float y, float w, float h, int id) {
			this.x = x; this.y = y; this.w = w; this.h = h; this.id = id;
		}
	}
	private static final List<LoadoutButtonBounds> loadoutButtons = new ArrayList<>();

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		hoveredItem = null;
		visibleSlots.clear();
		loadoutButtons.clear();

		if (data == null || data.inventory == null) {
			RenderHelper.drawModernCard(startX, curY, width, 80f, 10f, false);
			NVGRenderer.text("\uE000", startX + 24, curY + 30, Fonts.MATERIAL_ICONS_ROUND, 0xFFFFAA00, 24f);
			NVGRenderer.text(L10n.translate("pv.gear.api_disabled_title"), startX + 60, curY + 28, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);
			NVGRenderer.text(L10n.translate("pv.gear.api_disabled_desc"), startX + 60, curY + 48, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, RenderHelper.FS_CAPTION);
			return 90f;
		}

		// 1. Sub-Tabs Bar
		float subTabH = 32f;
		float subTabX = startX;

		for (GearSubTab st : GearSubTab.values()) {
			String title = st.getTitle();
			float iconW = 18f;
			float textW = NVGRenderer.textWidth(title, Fonts.PRETENDARD_SEMIBOLD, 14f);
			float stW = iconW + textW + 24f;
			boolean active = (st == activeSubTab);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + stW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0xFF6366F1, 8f);
				NVGRenderer.outlineRect(subTabX, curY, stW, subTabH, 1f, 0x44FFFFFF, 8f);
			} else if (hov) {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0xCC202336, 8f);
				NVGRenderer.outlineRect(subTabX, curY, stW, subTabH, 1f, 0x22FFFFFF, 8f);
			} else {
				NVGRenderer.rect(subTabX, curY, stW, subTabH, 0x80161824, 8f);
				NVGRenderer.outlineRect(subTabX, curY, stW, subTabH, 1f, 0x14FFFFFF, 8f);
			}

			int iconCol = active ? 0xFFFFFFFF : (hov ? 0xFF818CF8 : 0xFF9CA3AF);
			int textCol = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);

			NVGRenderer.text(st.icon, subTabX + 10f, curY + 8f, Fonts.MATERIAL_ICONS_ROUND, iconCol, 15f);
			NVGRenderer.text(title, subTabX + 28f, curY + 8.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 13.5f);

			subTabX += stW + 8f;
		}

		// Accessory Searchbox in subtab bar on right side
		if (activeSubTab == GearSubTab.ACCESSORIES) {
			float sw = 180f;
			float sx = startX + width - sw;
			searchBox.setPosition((int) sx, (int) (curY + 3f));
			searchBox.render(null, (int) mouseX, (int) mouseY, delta);
		}

		curY += subTabH + 16f;

		// 2. Sub-Tab Content Rendering
		switch (activeSubTab) {
			case INVENTORY -> curY += renderInventoryView(data, startX, curY, width, mouseX, mouseY);
			case WARDROBE -> curY += renderWardrobeView(data, startX, curY, width, mouseX, mouseY);
			case LOADOUT -> curY += renderLoadoutView(data, startX, curY, width, mouseX, mouseY);
			case ENDERCHEST -> curY += renderEnderChestView(data, startX, curY, width, mouseX, mouseY);
			case BACKPACKS -> curY += renderBackpacksView(data, startX, curY, width, mouseX, mouseY);
			case ACCESSORIES -> curY += renderAccessoriesView(data, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	// =========================================================================
	// 1. INVENTORY VIEW (1.12x Scaled & Centered in X/Y)
	// =========================================================================
	private static float renderInventoryView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float slotSize = 42.5f;
		float slotGap = 6f;

		float armorCardW = 2 * slotSize + slotGap + 26f;
		float invCardW = 9 * slotSize + 8 * slotGap + 26f;
		float cardGap = 16f;
		float totalW = armorCardW + cardGap + invCardW;
		float cardH = 4 * slotSize + 3 * slotGap + 48f;

		float originX = startX + Math.max(0, (width - totalW) / 2f);
		curY += 20f;

		float armorColX = originX;
		RenderHelper.drawModernCard(armorColX, curY, armorCardW, cardH, 12f, false);
		NVGRenderer.text("\uE8C9", armorColX + 13f, curY + 15f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 16f);
		NVGRenderer.text("Gear", armorColX + 33f, curY + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);

		for (int i = 0; i < 4; i++) {
			float sy = curY + 36f + i * (slotSize + slotGap);
			float sxArmor = armorColX + 13f;
			ParsedItem armorItem = (data.inventory.armor.size() > (3 - i)) ? data.inventory.armor.get(3 - i) : ParsedItem.EMPTY;
			drawSlot(sxArmor, sy, slotSize, armorItem, mx, my, false);

			float sxEq = armorColX + 13f + slotSize + slotGap;
			ParsedItem eqItem = (data.inventory.equipment.size() > i) ? data.inventory.equipment.get(i) : ParsedItem.EMPTY;
			drawSlot(sxEq, sy, slotSize, eqItem, mx, my, false);
		}

		float invX = armorColX + armorCardW + cardGap;
		RenderHelper.drawModernCard(invX, curY, invCardW, cardH, 12f, false);
		NVGRenderer.text("\uE8F9", invX + 13f, curY + 15f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 16f);
		NVGRenderer.text("Inventory", invX + 33f, curY + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);

		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 9; c++) {
				int slotIdx = (r == 3) ? c : ((r + 1) * 9 + c);
				float sx = invX + 13f + c * (slotSize + slotGap);
				float sy = curY + 36f + r * (slotSize + slotGap);
				ParsedItem item = (data.inventory.inventory.size() > slotIdx) ? data.inventory.inventory.get(slotIdx) : ParsedItem.EMPTY;
				drawSlot(sx, sy, slotSize, item, mx, my, false);
			}
		}

		curY += cardH + 30f;
		return curY - y0;
	}

	// =========================================================================
	// 2. WARDROBE VIEW
	// =========================================================================
	private static float renderWardrobeView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		InventoryData inv = data.inventory;
		List<ParsedItem> rawWardrobe = inv.wardrobe;

		float colGap = 14f;
		float pageW = (width - colGap) / 2f;
		float padX = 12f;
		float slotGap = 4f;
		float slotSize = (pageW - 2 * padX - 8 * slotGap) / 9f;
		float cardH = 4 * slotSize + 3 * slotGap + 44f;

		int totalSets = 27;
		int totalPages = (totalSets + 8) / 9;

		for (int p = 0; p < totalPages; p += 2) {
			float rowY = curY;

			float leftX = startX;
			RenderHelper.drawModernCard(leftX, rowY, pageW, cardH, 10f, false);
			NVGRenderer.text("\uE52E", leftX + 12f, rowY + 12f, Fonts.MATERIAL_ICONS_ROUND, 0xFFA78BFA, 15f);
			NVGRenderer.text("Wardrobe #" + (p + 1), leftX + 32f, rowY + 11.5f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);

			for (int row = 0; row < 4; row++) {
				for (int col = 0; col < 9; col++) {
					int setId = p * 9 + col + 1;
					ParsedItem item = getWardrobeItem(inv, rawWardrobe, setId, row);

					float sx = leftX + padX + col * (slotSize + slotGap);
					float sy = rowY + 32f + row * (slotSize + slotGap);

					boolean isEquipped = (inv.loadouts != null && inv.loadouts.equippedArmorSet == setId);
					if (isEquipped && row == 0) {
						NVGRenderer.outlineRect(sx - 1.5f, sy - 1.5f, slotSize + 3f, 4 * slotSize + 3 * slotGap + 3f, 1.5f, 0xFF10B981, 6f);
					}
					drawSlot(sx, sy, slotSize, item, mx, my, false);
				}
			}

			if (p + 1 < totalPages) {
				float rightX = startX + pageW + colGap;
				RenderHelper.drawModernCard(rightX, rowY, pageW, cardH, 10f, false);
				NVGRenderer.text("\uE52E", rightX + 12f, rowY + 12f, Fonts.MATERIAL_ICONS_ROUND, 0xFFA78BFA, 15f);
				NVGRenderer.text("Wardrobe #" + (p + 2), rightX + 32f, rowY + 11.5f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);

				for (int row = 0; row < 4; row++) {
					for (int col = 0; col < 9; col++) {
						int setId = (p + 1) * 9 + col + 1;
						ParsedItem item = getWardrobeItem(inv, rawWardrobe, setId, row);

						float sx = rightX + padX + col * (slotSize + slotGap);
						float sy = rowY + 32f + row * (slotSize + slotGap);

						boolean isEquipped = (inv.loadouts != null && inv.loadouts.equippedArmorSet == setId);
						if (isEquipped && row == 0) {
							NVGRenderer.outlineRect(sx - 1.5f, sy - 1.5f, slotSize + 3f, 4 * slotSize + 3 * slotGap + 3f, 1.5f, 0xFF10B981, 6f);
						}
						drawSlot(sx, sy, slotSize, item, mx, my, false);
					}
				}
			}

			curY += cardH + 14f;
		}

		return curY - y0;
	}

	private static ParsedItem getWardrobeItem(InventoryData inv, List<ParsedItem> rawWardrobe, int setId, int row) {
		if (inv.loadouts != null && inv.loadouts.armorSets.containsKey(setId)) {
			InventoryData.ArmorSet as = inv.loadouts.armorSets.get(setId);
			return switch (row) {
				case 0 -> as.helmet;
				case 1 -> as.chestplate;
				case 2 -> as.leggings;
				case 3 -> as.boots;
				default -> ParsedItem.EMPTY;
			};
		}
		int itemIdx = (setId - 1) * 4 + row;
		if (itemIdx < rawWardrobe.size()) {
			return rawWardrobe.get(itemIdx);
		}
		return ParsedItem.EMPTY;
	}

	// =========================================================================
	// 3. LOADOUT VIEW
	// =========================================================================
	private static float renderLoadoutView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		InventoryData inv = data.inventory;
		InventoryData.LoadoutData loadout = inv.loadouts;
		float gap = 14f;

		// =========================================================
		// ROW 1: Loadouts (Left 3x9 vertical box) & Equipment + Info (Right)
		// =========================================================
		float selSlotSize = 34f;
		float selSlotGap = 4f;
		float selPad = 12f;
		float loadoutsW = 3 * selSlotSize + 2 * selSlotGap + 2 * selPad;
		float row1H = 9 * selSlotSize + 8 * selSlotGap + 48f;
		float eqW = width - gap - loadoutsW;

		// 1. Loadouts Card (Vertical 3 cols x 9 rows matching skyblock-pv-master)
		RenderHelper.drawModernCard(startX, curY, loadoutsW, row1H, 12f, false);
		NVGRenderer.text("\uE8EF", startX + 12f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 15f);
		NVGRenderer.text("Loadouts", startX + 30f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);

		for (int r = 0; r < 9; r++) {
			for (int c = 0; c < 3; c++) {
				int id = r * 3 + c + 1;
				float bx = startX + selPad + c * (selSlotSize + selSlotGap);
				float by = curY + 36f + r * (selSlotSize + selSlotGap);

				loadoutButtons.add(new LoadoutButtonBounds(bx, by, selSlotSize, selSlotSize, id));

				boolean isSelected = (id == selectedLoadoutId);
				boolean isHov = mx >= bx && mx <= bx + selSlotSize && my >= by && my <= by + selSlotSize;

				InventoryData.SavedLoadout sl = (loadout != null && loadout.savedLoadouts != null) ? loadout.savedLoadouts.get(id) : null;
				boolean isEmpty = (sl == null || sl.isEmpty());

				int bgCol = isSelected ? 0xFF2A3450 : (isHov ? 0xFF252738 : 0x40161824);
				int borderCol = isSelected ? 0xFF818CF8 : (isHov ? 0x66FFFFFF : 0x1AFFFFFF);
				NVGRenderer.rect(bx, by, selSlotSize, selSlotSize, bgCol, 6f);
				NVGRenderer.outlineRect(bx, by, selSlotSize, selSlotSize, isSelected ? 1.8f : 1f, borderCol, 6f);

				ParsedItem repItem = getLoadoutRepresentativeItem(inv, sl);
				if (repItem != null && !repItem.isEmpty()) {
					drawSlot(bx + 2f, by + 2f, selSlotSize - 4f, repItem, mx, my, false);
				} else {
					// 1..9 unlocked (gray dye), 10..27 locked (red dye)
					int dotCol = id <= 9 ? (isEmpty ? 0xFF6B7280 : 0xFF10B981) : 0xFFE11D48;
					NVGRenderer.circle(bx + selSlotSize / 2f, by + selSlotSize / 2f, 4f, dotCol);
				}
			}
		}

		// 2. Equipment & Info Card (Right)
		float eqX = startX + loadoutsW + gap;
		RenderHelper.drawModernCard(eqX, curY, eqW, row1H, 12f, false);
		NVGRenderer.text("\uE8C9", eqX + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 16f);
		NVGRenderer.text("Equipment & Info", eqX + 34f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);

		InventoryData.SavedLoadout curSl = (loadout != null && loadout.savedLoadouts != null) ? loadout.savedLoadouts.get(selectedLoadoutId) : null;
		List<ParsedItem> armorItems = getLoadoutArmorItems(inv, curSl);
		List<ParsedItem> equipmentItems = getLoadoutEquipmentItems(inv, curSl);

		float eqSlotSize = 42f;
		float eqSlotGap = 6f;
		float eqGridStartX = eqX + 24f;
		float eqGridStartY = curY + 44f;

		// 4 Rows x 2 Columns (Armor + Equipment)
		for (int r = 0; r < 4; r++) {
			float sy = eqGridStartY + r * (eqSlotSize + eqSlotGap);
			ParsedItem arm = (armorItems.size() > r) ? armorItems.get(r) : ParsedItem.EMPTY;
			drawSlot(eqGridStartX, sy, eqSlotSize, arm, mx, my, false);

			ParsedItem eq = (equipmentItems.size() > r) ? equipmentItems.get(r) : ParsedItem.EMPTY;
			drawSlot(eqGridStartX + eqSlotSize + eqSlotGap, sy, eqSlotSize, eq, mx, my, false);
		}

		// Pet Slot below armor & equipment
		float petX = eqGridStartX + (2 * eqSlotSize + eqSlotGap - eqSlotSize) / 2f;
		float petY = eqGridStartY + 4 * (eqSlotSize + eqSlotGap) + 10f;
		ParsedItem petItem = getLoadoutPetItem(data, curSl);
		drawSlot(petX, petY, eqSlotSize, petItem, mx, my, false);

		if (data.pets != null && data.pets.activePet != null && petItem != null && !petItem.isEmpty()) {
			String lvlStr = String.valueOf(data.pets.activePet.level);
			float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 10f);
			NVGRenderer.rect(petX + eqSlotSize - lw - 4f, petY + eqSlotSize - 12f, lw + 3f, 11f, 0xDD111218, 2f);
			NVGRenderer.text(lvlStr, petX + eqSlotSize - lw - 2.5f, petY + eqSlotSize - 11f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFFFFF, 10f);
		}

		// Right side: Info texts
		float infoStartX = eqGridStartX + 2 * eqSlotSize + eqSlotGap + 28f;
		float infoY = curY + 46f;

		String loadoutTitle = (curSl != null && curSl.name != null && !curSl.name.isEmpty())
				? curSl.name
				: ("Loadout " + selectedLoadoutId);
		NVGRenderer.text(loadoutTitle, infoStartX, infoY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		infoY += 32f;

		int miningSlot = (curSl != null && curSl.miningCoreSelectedSlot != null) ? curSl.miningCoreSelectedSlot : 1;
		int foragingSlot = (curSl != null && curSl.foragingCoreSelectedSlot != null) ? curSl.foragingCoreSelectedSlot : 1;

		NVGRenderer.text("HOTM Preset:", infoStartX, infoY, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text("Slot " + miningSlot, infoStartX + 90f, infoY, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 13.5f);
		infoY += 24f;

		NVGRenderer.text("HOTF Preset:", infoStartX, infoY, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text("Slot " + foragingSlot, infoStartX + 90f, infoY, Fonts.PRETENDARD_SEMIBOLD, 0xFF34D399, 13.5f);
		infoY += 24f;

		String powerVal = (curSl != null && curSl.powerStone != null && !curSl.powerStone.isEmpty())
				? capitalize(curSl.powerStone)
				: (inv.maxwell != null && !inv.maxwell.selectedPower.isEmpty() ? capitalize(inv.maxwell.selectedPower) : "None");
		NVGRenderer.text("Power Stone:", infoStartX, infoY, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(powerVal, infoStartX + 90f, infoY, Fonts.PRETENDARD_SEMIBOLD, 0xFFF59E0B, 13.5f);
		infoY += 24f;

		if (curSl != null && curSl.tuningPointsSlot != null) {
			NVGRenderer.text("Tuning Slot:", infoStartX, infoY, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
			NVGRenderer.text("#" + curSl.tuningPointsSlot, infoStartX + 90f, infoY, Fonts.PRETENDARD_SEMIBOLD, 0xFFA78BFA, 13.5f);
		}

		curY += row1H + gap;

		// =========================================================
		// ROW 2: HOTM Loadout (Left 50%) & HOTF Loadout (Right 50%)
		// =========================================================
		float row2H = 430f;
		float treeW = (width - gap) / 2f;

		// HOTM Card
		float hotmX = startX;
		RenderHelper.drawModernCard(hotmX, curY, treeW, row2H, 12f, false);
		NVGRenderer.text("\uE52F", hotmX + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 16f);
		NVGRenderer.text("HOTM Loadout (Preset #" + miningSlot + ")", hotmX + 34f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
		renderFullHotmTree(hotmX, curY + 36f, treeW, row2H - 44f, data, miningSlot, mx, my);

		// HOTF Card
		float hotfX = startX + treeW + gap;
		RenderHelper.drawModernCard(hotfX, curY, treeW, row2H, 12f, false);
		NVGRenderer.text("\uE56C", hotfX + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFF34D399, 16f);
		NVGRenderer.text("HOTF Loadout (Preset #" + foragingSlot + ")", hotfX + 34f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
		renderFullHotfTree(hotfX, curY + 36f, treeW, row2H - 44f, data, foragingSlot, mx, my);

		curY += row2H + 16f;
		return curY - y0;
	}

	private static void renderFullHotmTree(float x, float y, float w, float h, MemberData data, int slot, float mx, float my) {
		float slotSize = 34f;
		float slotGap = 5f;
		int cols = 7;
		int rows = 9;

		float treeW = cols * slotSize + (cols - 1) * slotGap;
		float startTreeX = x + (w - treeW) / 2f;
		float startTreeY = y + 10f;

		MiningData m = data.mining;
		Map<String, Integer> activeNodes = (m != null && m.presetNodes.containsKey(slot))
				? m.presetNodes.get(slot)
				: (m != null ? m.nodes : Collections.emptyMap());
		String activeAb = (m != null && m.presetAbilities.containsKey(slot))
				? m.presetAbilities.get(slot)
				: (m != null ? m.selectedAbility : "");

		for (HotmTreeData.HotmNode node : HotmTreeData.ALL_NODES) {
			if (node.type == HotmTreeData.NodeType.TIER) continue;

			int r = 9 - node.y;
			int c = node.x;
			if (r < 0 || r >= rows || c < 0 || c >= cols) continue;

			float sx = startTreeX + c * (slotSize + slotGap);
			float sy = startTreeY + r * (slotSize + slotGap);

			int lvl = activeNodes.getOrDefault(node.id, 0);
			boolean isSelAb = node.id.equalsIgnoreCase(activeAb);
			ItemStack icon = node.getItemIcon(lvl, isSelAb);

			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;

			int bgCol = isSelAb ? 0xFF1E382B : (lvl > 0 ? 0xFF1C2A3A : 0x5514151E);
			int borderCol = isSelAb ? 0xFF55FF55 : (lvl >= node.maxLevel ? 0xFF55FFFF : (lvl > 0 ? 0xFF3B82F6 : 0x33555566));

			NVGRenderer.rect(sx, sy, slotSize, slotSize, bgCol, 6f);
			NVGRenderer.outlineRect(sx, sy, slotSize, slotSize, 1.2f, borderCol, 6f);

			// SkyBlock-style custom tooltip
			ParsedItem pi = new ParsedItem();
			pi.itemStack = icon;
			pi.displayName = (isSelAb ? "§a" : (lvl > 0 ? "§b" : "§c")) + node.name;
			pi.lore = new ArrayList<>();
			if (node.type == HotmTreeData.NodeType.CORE) {
				pi.lore.add("§7Peak of the Mountain: §e" + lvl + "§7/§e" + node.maxLevel);
			} else if (node.type == HotmTreeData.NodeType.ABILITY) {
				pi.lore.add(isSelAb ? "§a§lSELECTED ABILITY" : "§7Pickaxe Ability");
			} else {
				pi.lore.add(lvl > 0 ? ("§7Level " + lvl + "§8/§7" + node.maxLevel) : "§cLocked Perk");
			}
			pi.lore.add("");
			String desc = node.description != null ? node.description.replace("%d", String.valueOf(lvl > 0 ? lvl : 1)) : "";
			pi.lore.add("§7" + desc);

			drawSlot(sx, sy, slotSize, pi, mx, my, false);

			if (lvl > 1 && node.maxLevel > 1) {
				String lvlStr = String.valueOf(lvl);
				float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 10f);
				NVGRenderer.rect(sx + slotSize - lw - 4f, sy + slotSize - 12f, lw + 3f, 11f, 0xDD111218, 2f);
				NVGRenderer.text(lvlStr, sx + slotSize - lw - 2.5f, sy + slotSize - 11f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFFFFF, 10f);
			}
		}
	}

	private static void renderFullHotfTree(float x, float y, float w, float h, MemberData data, int slot, float mx, float my) {
		float slotSize = 34f;
		float slotGap = 5f;
		int cols = 7;
		int rows = 7;

		float treeW = cols * slotSize + (cols - 1) * slotGap;
		float startTreeX = x + (w - treeW) / 2f;
		float startTreeY = y + 20f;

		MiningData m = data.mining;
		Map<String, Integer> activeNodes = (m != null && m.foragingPresetNodes.containsKey(slot))
				? m.foragingPresetNodes.get(slot)
				: (m != null ? m.foragingNodes : Collections.emptyMap());

		int[][] hotfLevels = {
			{0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 1},
			{0, 0, 1, 0, 4, 0, 0},
			{1, 1, 1, 1, 0, 0, 0},
			{0, 1, 0, 0, 3, 0, 0},
			{0, 0, 0, 37, 12, 2, 0}
		};

		ItemType[][] hotfItems = {
			{ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON},
			{ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON},
			{ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.SAPLING},
			{ItemType.BUTTON, ItemType.BUTTON, ItemType.LOG, ItemType.BUTTON, ItemType.LOG, ItemType.BUTTON, ItemType.BUTTON},
			{ItemType.LOG, ItemType.LOG, ItemType.LOG, ItemType.LOG, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON},
			{ItemType.SAPLING, ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.LOG, ItemType.BUTTON, ItemType.BUTTON},
			{ItemType.BUTTON, ItemType.BUTTON, ItemType.BUTTON, ItemType.LOG, ItemType.LOG, ItemType.LEAF, ItemType.BUTTON}
		};

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				float sx = startTreeX + c * (slotSize + slotGap);
				float sy = startTreeY + r * (slotSize + slotGap);

				ItemType type = hotfItems[r][c];
				int lvl = hotfLevels[r][c];

				ItemStack stack = switch (type) {
					case LOG -> new ItemStack(Items.OAK_LOG);
					case SAPLING -> new ItemStack(Items.OAK_SAPLING);
					case LEAF -> new ItemStack(Items.OAK_LEAVES);
					case BUTTON -> new ItemStack(Items.IRON_NUGGET);
				};

				boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
				int bgCol = (type != ItemType.BUTTON) ? 0xFF2A2218 : 0x33181820;
				int borderCol = (type != ItemType.BUTTON) ? 0xFF854D0E : 0x22FFFFFF;

				NVGRenderer.rect(sx, sy, slotSize, slotSize, bgCol, 6f);
				NVGRenderer.outlineRect(sx, sy, slotSize, slotSize, 1.2f, borderCol, 6f);

				ParsedItem pi = new ParsedItem();
				pi.itemStack = stack;
				if (type != ItemType.BUTTON) {
					pi.displayName = "§aForaging Perk (Lv " + (lvl > 0 ? lvl : 1) + ")";
					pi.lore = new ArrayList<>();
					pi.lore.add("§7Level: §e" + (lvl > 0 ? lvl : 1));
					pi.lore.add("§7Grants additional Foraging stats.");
				} else {
					pi.displayName = "§cLocked Node";
					pi.lore = new ArrayList<>();
					pi.lore.add("§7Requires previous tier perks unlocked.");
				}
				drawSlot(sx, sy, slotSize, pi, mx, my, false);

				if (lvl > 1) {
					String lvlStr = String.valueOf(lvl);
					float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 10f);
					NVGRenderer.rect(sx + slotSize - lw - 4f, sy + slotSize - 12f, lw + 3f, 11f, 0xDD111218, 2f);
					NVGRenderer.text(lvlStr, sx + slotSize - lw - 2.5f, sy + slotSize - 11f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFFFFF, 10f);
				}
			}
		}
	}

	private enum ItemType {
		BUTTON, LOG, SAPLING, LEAF
	}

	// =========================================================================
	// 4. ENDER CHEST VIEW
	// =========================================================================
	private static float renderEnderChestView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (data.inventory.enderchest.isEmpty()) {
			RenderHelper.drawModernCard(startX, curY, width, 60f, 10f, false);
			NVGRenderer.text(L10n.translate("pv.gear.empty_page"), startX + 16, curY + 22, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, 14f);
			return 70f;
		}

		float colGap = 14f;
		float pageW = (width - colGap) / 2f;
		float padX = 12f;
		float slotGap = 4f;
		float slotSize = (pageW - 2 * padX - 8 * slotGap) / 9f;
		float cardH = 6 * slotSize + 5 * slotGap + 44f;

		int total = data.inventory.enderchest.size();
		int pages = (total + 53) / 54;

		for (int p = 0; p < pages; p += 2) {
			float rowY = curY;

			float leftX = startX;
			RenderHelper.drawModernCard(leftX, rowY, pageW, cardH, 10f, false);
			NVGRenderer.text("Ender Chest #" + (p + 1), leftX + 12, rowY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
			for (int r = 0; r < 6; r++) {
				for (int c = 0; c < 9; c++) {
					int idx = p * 54 + r * 9 + c;
					float sx = leftX + padX + c * (slotSize + slotGap);
					float sy = rowY + 32f + r * (slotSize + slotGap);
					ParsedItem item = (idx < total) ? data.inventory.enderchest.get(idx) : ParsedItem.EMPTY;
					drawSlot(sx, sy, slotSize, item, mx, my, false);
				}
			}

			if (p + 1 < pages) {
				float rightX = startX + pageW + colGap;
				RenderHelper.drawModernCard(rightX, rowY, pageW, cardH, 10f, false);
				NVGRenderer.text("Ender Chest #" + (p + 2), rightX + 12, rowY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
				for (int r = 0; r < 6; r++) {
					for (int c = 0; c < 9; c++) {
						int idx = (p + 1) * 54 + r * 9 + c;
						float sx = rightX + padX + c * (slotSize + slotGap);
						float sy = rowY + 32f + r * (slotSize + slotGap);
						ParsedItem item = (idx < total) ? data.inventory.enderchest.get(idx) : ParsedItem.EMPTY;
						drawSlot(sx, sy, slotSize, item, mx, my, false);
					}
				}
			}

			curY += cardH + 14f;
		}

		return curY - y0;
	}

	// =========================================================================
	// 5. BACKPACKS VIEW
	// =========================================================================
	private static float renderBackpacksView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (data.inventory.backpacks.isEmpty()) {
			RenderHelper.drawModernCard(startX, curY, width, 60f, 10f, false);
			NVGRenderer.text(L10n.translate("pv.gear.empty_page"), startX + 16, curY + 22, Fonts.PRETENDARD, RenderHelper.FONT_SECONDARY, 14f);
			return 70f;
		}

		float colGap = 14f;
		float pageW = (width - colGap) / 2f;
		float padX = 12f;
		float slotGap = 4f;
		float slotSize = (pageW - 2 * padX - 8 * slotGap) / 9f;
		int count = data.inventory.backpacks.size();

		for (int b = 0; b < count; b += 2) {
			float rowY = curY;

			List<ParsedItem> bp1 = data.inventory.backpacks.get(b);
			float leftX = startX;
			int rows1 = Math.max(1, (bp1.size() + 8) / 9);
			float cardH1 = rows1 * slotSize + (rows1 - 1) * slotGap + 44f;

			RenderHelper.drawModernCard(leftX, rowY, pageW, cardH1, 10f, false);
			NVGRenderer.text("Backpack #" + (b + 1) + " (" + bp1.size() + " slots)", leftX + 12, rowY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
			for (int r = 0; r < rows1; r++) {
				for (int c = 0; c < 9; c++) {
					int idx = r * 9 + c;
					float sx = leftX + padX + c * (slotSize + slotGap);
					float sy = rowY + 32f + r * (slotSize + slotGap);
					ParsedItem item = (idx < bp1.size()) ? bp1.get(idx) : ParsedItem.EMPTY;
					drawSlot(sx, sy, slotSize, item, mx, my, false);
				}
			}

			float maxH = cardH1;
			if (b + 1 < count) {
				List<ParsedItem> bp2 = data.inventory.backpacks.get(b + 1);
				float rightX = startX + pageW + colGap;
				int rows2 = Math.max(1, (bp2.size() + 8) / 9);
				float cardH2 = rows2 * slotSize + (rows2 - 1) * slotGap + 44f;
				if (cardH2 > maxH) maxH = cardH2;

				RenderHelper.drawModernCard(rightX, rowY, pageW, cardH2, 10f, false);
				NVGRenderer.text("Backpack #" + (b + 2) + " (" + bp2.size() + " slots)", rightX + 12, rowY + 12, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
				for (int r = 0; r < rows2; r++) {
					for (int c = 0; c < 9; c++) {
						int idx = r * 9 + c;
						float sx = rightX + padX + c * (slotSize + slotGap);
						float sy = rowY + 32f + r * (slotSize + slotGap);
						ParsedItem item = (idx < bp2.size()) ? bp2.get(idx) : ParsedItem.EMPTY;
						drawSlot(sx, sy, slotSize, item, mx, my, false);
					}
				}
			}

			curY += maxH + 14f;
		}

		return curY - y0;
	}

	// =========================================================================
	// 6. ACCESSORIES VIEW (Compact font 13.5f, generous row height, precise duplicates)
	// =========================================================================
	private static float renderAccessoriesView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		InventoryData inv = data.inventory;
		List<ParsedItem> accessories = inv.accessoryBag != null ? inv.accessoryBag : Collections.emptyList();

		float slotSize = 34f;
		float slotGap = 4f;
		int cols = 12;

		float gridW = cols * slotSize + (cols - 1) * slotGap + 24f;
		float statsW = width - gridW - 14f;
		float statsX = startX + gridW + 14f;

		int totalAcc = accessories.size();
		int rows = Math.max(4, (totalAcc + cols - 1) / cols);
		float gridH = rows * slotSize + (rows - 1) * slotGap + 48f;

		String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);

		// 1. Left: 12-Column Accessory Grid Card
		RenderHelper.drawModernCard(startX, curY, gridW, gridH, 12f, false);
		NVGRenderer.text("\uEA5F", startX + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFFF59E0B, 16f);
		NVGRenderer.text("Accessory Bag (" + totalAcc + " Items)", startX + 34f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);

		for (int i = 0; i < totalAcc; i++) {
			int r = i / cols;
			int c = i % cols;
			float sx = startX + 12f + c * (slotSize + slotGap);
			float sy = curY + 38f + r * (slotSize + slotGap);

			ParsedItem item = accessories.get(i);
			boolean isMatched = !query.isEmpty() && isAccessoryMatched(item, query);

			drawSlot(sx, sy, slotSize, item, mx, my, true);

			if (isMatched) {
				NVGRenderer.rect(sx, sy, slotSize, slotSize, 0x4038BDF8, 6f);
				NVGRenderer.outlineRect(sx - 1.5f, sy - 1.5f, slotSize + 3f, slotSize + 3f, 2f, 0xFF38BDF8, 7f);
			} else if (!query.isEmpty() && !item.isEmpty()) {
				NVGRenderer.rect(sx, sy, slotSize, slotSize, 0x66000000, 6f);
			}
		}

		// 2. Right: Stats & Duplicates Panel (Compact font, generous vertical line height)
		AccessoryAnalysis analysis = analyzeAccessories(accessories, inv.maxwell);

		float statsH = Math.max(gridH, 440f);
		RenderHelper.drawModernCard(statsX, curY, statsW, statsH, 12f, false);

		float sy = curY + 16f;
		float padX = statsX + 16f;
		float innerW = statsW - 32f;
		float fontSz = 13.5f;

		// Total Magical Power
		NVGRenderer.text("Magical Power", padX, sy, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 12f);
		String mpStr = RenderHelper.formatNumber(analysis.totalMp);
		NVGRenderer.text(mpStr, padX, sy + 18f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF59E0B, 26f);

		// Selected Power
		String selPower = (inv.maxwell != null && !inv.maxwell.selectedPower.isEmpty())
				? capitalize(inv.maxwell.selectedPower)
				: "None";
		float mpWidth = NVGRenderer.textWidth(mpStr, Fonts.PRETENDARD_SEMIBOLD, 26f);
		NVGRenderer.text("Power: " + selPower, padX + mpWidth + 14f, sy + 24f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 13f);

		sy += 54f;
		NVGRenderer.rect(padX, sy, innerW, 1f, 0x1AFFFFFF, 0.5f);
		sy += 14f;

		// Rarity Breakdown
		NVGRenderer.text("Rarity Breakdown", padX, sy, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		sy += 22f;

		String[] rarityOrder = {"MYTHIC", "LEGENDARY", "EPIC", "RARE", "UNCOMMON", "COMMON", "SPECIAL", "VERY SPECIAL"};
		for (String rar : rarityOrder) {
			int count = analysis.rarityCounts.getOrDefault(rar, 0);
			int mpContrib = analysis.rarityMp.getOrDefault(rar, 0);
			if (count == 0) continue;

			int color = getRarityColor(rar);
			NVGRenderer.circle(padX + 5f, sy + 6f, 3.5f, color);
			NVGRenderer.text(capitalize(rar), padX + 14f, sy, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, fontSz);

			String statTxt = count + " pcs (+" + mpContrib + " MP)";
			float tw = NVGRenderer.textWidth(statTxt, Fonts.PRETENDARD_SEMIBOLD, fontSz);
			NVGRenderer.text(statTxt, padX + innerW - tw, sy, Fonts.PRETENDARD_SEMIBOLD, color, fontSz);

			sy += 20f;
		}

		if (analysis.riftPrismBonus > 0 || analysis.abiphoneBonus > 0) {
			sy += 4f;
			if (analysis.riftPrismBonus > 0) {
				NVGRenderer.text("Rift Prism", padX + 14f, sy, Fonts.PRETENDARD_MEDIUM, 0xFF38BDF8, fontSz);
				String txt = "+11 MP";
				float tw = NVGRenderer.textWidth(txt, Fonts.PRETENDARD_SEMIBOLD, fontSz);
				NVGRenderer.text(txt, padX + innerW - tw, sy, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, fontSz);
				sy += 20f;
			}
			if (analysis.abiphoneBonus > 0) {
				NVGRenderer.text("Abiphone Contacts", padX + 14f, sy, Fonts.PRETENDARD_MEDIUM, 0xFF38BDF8, fontSz);
				String txt = "+" + analysis.abiphoneBonus + " MP";
				float tw = NVGRenderer.textWidth(txt, Fonts.PRETENDARD_SEMIBOLD, fontSz);
				NVGRenderer.text(txt, padX + innerW - tw, sy, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, fontSz);
				sy += 20f;
			}
		}

		sy += 10f;
		NVGRenderer.rect(padX, sy, innerW, 1f, 0x1AFFFFFF, 0.5f);
		sy += 14f;

		// Duplicates Section
		NVGRenderer.text("Duplicates", padX, sy, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		sy += 22f;

		if (analysis.duplicates.isEmpty()) {
			NVGRenderer.text("\uE86C", padX, sy + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 14f);
			NVGRenderer.text("No duplicates found. All accessories active!", padX + 18f, sy, Fonts.PRETENDARD_MEDIUM, 0xFF10B981, fontSz);
		} else {
			NVGRenderer.text("\uE002", padX, sy + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFEF4444, 14f);
			NVGRenderer.text(analysis.duplicates.size() + " Duplicate(s) inactive:", padX + 18f, sy, Fonts.PRETENDARD_SEMIBOLD, 0xFFEF4444, fontSz);
			sy += 20f;

			for (String dupName : analysis.duplicates) {
				NVGRenderer.text("• " + dupName, padX + 10f, sy, Fonts.PRETENDARD, RenderHelper.FONT_MUTED, 12.5f);
				sy += 18f;
			}
		}

		curY += Math.max(gridH, statsH) + 16f;
		return curY - y0;
	}

	private static boolean isAccessoryMatched(ParsedItem item, String query) {
		if (item == null || item.isEmpty()) return false;
		if (item.displayName != null && item.displayName.toLowerCase(Locale.ROOT).contains(query)) return true;
		if (item.skyblockId != null && item.skyblockId.toLowerCase(Locale.ROOT).contains(query)) return true;
		if (item.lore != null) {
			for (String l : item.lore) {
				if (l.toLowerCase(Locale.ROOT).contains(query)) return true;
			}
		}
		return false;
	}

	// =========================================================================
	// SLOT RENDERING & HOVER HELPER
	// =========================================================================
	private static void drawSlot(float x, float y, float size, ParsedItem item, float mx, float my, boolean showRarityLine) {
		boolean hov = mx >= x && mx <= x + size && my >= y && my <= y + size;
		RenderHelper.drawSubCard(x, y, size, size, 6f, hov);

		if (item != null && !item.isEmpty()) {
			if (showRarityLine) {
				NVGRenderer.rect(x + 2, y + size - 3f, size - 4, 2f, item.rarityColor, 1f);
			}
			visibleSlots.add(new SlotRenderInfo(x, y, size, item));

			if (hov) {
				hoveredItem = item;
				hoveredItemX = mx + 14;
				hoveredItemY = my + 14;
			}
		}
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		if (activeSubTab == GearSubTab.ACCESSORIES && searchBox.mouseClicked(mx, my, 0)) {
			return true;
		}

		float subTabH = 32f;
		float subTabX = startX;
		for (GearSubTab st : GearSubTab.values()) {
			float iconW = 18f;
			float textW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f);
			float stW = iconW + textW + 24f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}

		if (activeSubTab == GearSubTab.LOADOUT) {
			for (LoadoutButtonBounds btn : loadoutButtons) {
				if (mx >= btn.x && mx <= btn.x + btn.w && my >= btn.y && my <= btn.y + btn.h) {
					selectedLoadoutId = btn.id;
					return true;
				}
			}
		}

		return false;
	}

	public static boolean charTyped(char chr, int modifiers) {
		if (activeSubTab == GearSubTab.ACCESSORIES) {
			return searchBox.charTyped(chr, modifiers);
		}
		return false;
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (activeSubTab == GearSubTab.ACCESSORIES) {
			return searchBox.keyPressed(keyCode, scanCode, modifiers);
		}
		return false;
	}

	// =========================================================================
	// HELPER METHODS
	// =========================================================================
	private static ParsedItem getLoadoutRepresentativeItem(InventoryData inv, InventoryData.SavedLoadout sl) {
		if (sl == null || sl.isEmpty() || inv == null || inv.loadouts == null) return null;
		if (sl.armorSetId != null && inv.loadouts.armorSets.containsKey(sl.armorSetId)) {
			InventoryData.ArmorSet as = inv.loadouts.armorSets.get(sl.armorSetId);
			if (as.helmet != null && !as.helmet.isEmpty()) return as.helmet;
			if (as.chestplate != null && !as.chestplate.isEmpty()) return as.chestplate;
			if (as.leggings != null && !as.leggings.isEmpty()) return as.leggings;
			if (as.boots != null && !as.boots.isEmpty()) return as.boots;
		}
		if (sl.equipmentSlotId != null && inv.loadouts.equipmentSets.containsKey(sl.equipmentSlotId)) {
			InventoryData.EquipmentSet es = inv.loadouts.equipmentSets.get(sl.equipmentSlotId);
			for (ParsedItem item : es.getStacks()) {
				if (item != null && !item.isEmpty()) return item;
			}
		}
		return null;
	}

	private static List<ParsedItem> getLoadoutArmorItems(InventoryData inv, InventoryData.SavedLoadout sl) {
		if (sl != null && sl.armorSetId != null && inv.loadouts != null && inv.loadouts.armorSets.containsKey(sl.armorSetId)) {
			return inv.loadouts.armorSets.get(sl.armorSetId).getStacks();
		}
		if (inv.armor != null && !inv.armor.isEmpty()) {
			List<ParsedItem> rev = new ArrayList<>(inv.armor);
			Collections.reverse(rev);
			return rev;
		}
		return Collections.emptyList();
	}

	private static List<ParsedItem> getLoadoutEquipmentItems(InventoryData inv, InventoryData.SavedLoadout sl) {
		if (sl != null && sl.equipmentSlotId != null && inv.loadouts != null && inv.loadouts.equipmentSets.containsKey(sl.equipmentSlotId)) {
			return inv.loadouts.equipmentSets.get(sl.equipmentSlotId).getStacks();
		}
		if (inv.equipment != null) {
			return inv.equipment;
		}
		return Collections.emptyList();
	}

	private static ParsedItem getLoadoutPetItem(MemberData data, InventoryData.SavedLoadout sl) {
		if (data != null && data.pets != null && !data.pets.pets.isEmpty()) {
			if (sl != null && sl.petUuid != null && !sl.petUuid.isEmpty()) {
				for (var pet : data.pets.pets) {
					if (sl.petUuid.equalsIgnoreCase(pet.uuid)) {
						ParsedItem pi = new ParsedItem();
						pi.displayName = pet.displayName;
						pi.itemStack = pet.itemStack;
						pi.rarity = pet.rarity;
						pi.rarityColor = pet.getRarityColor();
						return pi;
					}
				}
			}
			var active = data.pets.pets.get(0);
			ParsedItem pi = new ParsedItem();
			pi.displayName = active.displayName;
			pi.itemStack = active.itemStack;
			pi.rarity = active.rarity;
			pi.rarityColor = active.getRarityColor();
			return pi;
		}
		return ParsedItem.EMPTY;
	}

	public static class AccessoryAnalysis {
		public int totalMp = 0;
		public int riftPrismBonus = 0;
		public int abiphoneBonus = 0;
		public Map<String, Integer> rarityCounts = new LinkedHashMap<>();
		public Map<String, Integer> rarityMp = new LinkedHashMap<>();
		public List<String> duplicates = new ArrayList<>();
	}

	// Accurate Upgrade Line Families: in order from lowest to highest tier
	private static final List<List<String>> UPGRADE_CHAINS = List.of(
		List.of("TALISMAN_OF_COINS", "RING_OF_COINS", "ARTIFACT_OF_COINS", "RELIC_OF_COINS"),
		List.of("FEATHER_TALISMAN", "FEATHER_RING", "FEATHER_ARTIFACT"),
		List.of("POTION_AFFINITY_TALISMAN", "RING_POTION_AFFINITY", "ARTIFACT_POTION_AFFINITY"),
		List.of("HEALING_TALISMAN", "HEALING_RING"),
		List.of("SEA_CREATURE_TALISMAN", "SEA_CREATURE_RING", "SEA_CREATURE_ARTIFACT"),
		List.of("BAT_TALISMAN", "BAT_RING", "BAT_ARTIFACT"),
		List.of("SPEED_TALISMAN", "SPEED_RING", "SPEED_ARTIFACT", "SPEED_RELIC"),
		List.of("PIGGY_BANK", "CRACKED_PIGGY_BANK", "BROKEN_PIGGY_BANK"),
		List.of("DEVOUR_RING", "DEVOUR_ARTIFACT"),
		List.of("ZOMBIE_TALISMAN", "ZOMBIE_RING", "ZOMBIE_ARTIFACT"),
		List.of("SPIDER_TALISMAN", "SPIDER_RING", "SPIDER_ARTIFACT"),
		List.of("TARANTULA_TALISMAN", "TARANTULA_RING"),
		List.of("RED_CLAW_TALISMAN", "RED_CLAW_RING", "RED_CLAW_ARTIFACT"),
		List.of("HUNTER_TALISMAN", "HUNTER_RING"),
		List.of("SCARF_STUDIES", "SCARF_THESIS", "SCARF_GRIMOIRE"),
		List.of("TREASURE_TALISMAN", "TREASURE_RING", "TREASURE_ARTIFACT", "TREASURE_RELIC"),
		List.of("CAT_TALISMAN", "LYNX_TALISMAN", "CHEETAH_TALISMAN"),
		List.of("PERSONAL_COMPACTOR_4000", "PERSONAL_COMPACTOR_5000", "PERSONAL_COMPACTOR_6000", "PERSONAL_COMPACTOR_7000"),
		List.of("PERSONAL_DELETOR_4000", "PERSONAL_DELETOR_5000", "PERSONAL_DELETOR_6000", "PERSONAL_DELETOR_7000"),
		List.of("WOLF_TALISMAN", "WOLF_RING"),
		List.of("PIG_FOOT", "FROZEN_CHICKEN"),
		List.of("JAGUAR_TALISMAN", "LEOPARD_TALISMAN", "TIGER_TALISMAN"),
		List.of("CANDY_TALISMAN", "CANDY_RING", "CANDY_ARTIFACT", "CANDY_RELIC"),
		List.of("INTIMIDATION_TALISMAN", "INTIMIDATION_RING", "INTIMIDATION_ARTIFACT", "INTIMIDATION_RELIC"),
		List.of("LAVA_TALISMAN", "FIRE_TALISMAN"),
		List.of("SKELETON_TALISMAN", "SKELETON_RING"),
		List.of("VACCINE_TALISMAN", "VACCINE_RING"),
		List.of("MINERAL_TALISMAN", "MINERAL_RING", "MINERAL_ARTIFACT"),
		List.of("EXPERIENCE_ARTIFACT", "EXPERIENCE_RELIC"),
		List.of("WOOD_TALISMAN", "WOOD_RING", "WOOD_ARTIFACT"),
		List.of("BEAST_TALISMAN", "BEAST_RING"),
		List.of("NIGHT_CRYSTAL", "DAY_CRYSTAL"),
		List.of("FARMING_TALISMAN", "FARMING_RING", "FARMING_ARTIFACT"),
		List.of("SHARK_TOOTH_NECKLACE_1", "SHARK_TOOTH_NECKLACE_2", "SHARK_TOOTH_NECKLACE_3", "SHARK_TOOTH_NECKLACE_4", "SHARK_TOOTH_NECKLACE_5"),
		List.of("ODYSSEY_BEACON_1", "ODYSSEY_BEACON_2", "ODYSSEY_BEACON_3", "ODYSSEY_BEACON_4", "ODYSSEY_BEACON_5"),
		List.of("RING_OF_LOVE", "RING_OF_LOVE_1", "RING_OF_LOVE_2", "RING_OF_LOVE_3", "RING_OF_LOVE_4", "RING_OF_LOVE_5", "RING_OF_LOVE_6", "RING_OF_LOVE_7", "RING_OF_LOVE_8", "RING_OF_LOVE_9", "RING_OF_LOVE_10", "RING_OF_LOVE_11", "RING_OF_LOVE_12", "RING_OF_LOVE_13", "RING_OF_LOVE_14")
	);

	public static AccessoryAnalysis analyzeAccessories(List<ParsedItem> items, InventoryData.MaxwellData maxwell) {
		AccessoryAnalysis a = new AccessoryAnalysis();
		if (items == null) return a;

		// 1. Map each item to its skyblockId and count occurrences
		Map<String, Integer> idCounts = new HashMap<>();
		for (ParsedItem item : items) {
			if (item != null && !item.isEmpty() && item.skyblockId != null && !item.skyblockId.isEmpty()) {
				String id = item.skyblockId.toUpperCase(Locale.ROOT);
				idCounts.put(id, idCounts.getOrDefault(id, 0) + 1);
			}
		}

		// 2. Identify strictly superseded IDs: an ID is superseded IF AND ONLY IF a higher tier in its chain is present
		Set<String> supersededIds = new HashSet<>();
		for (List<String> chain : UPGRADE_CHAINS) {
			for (int i = 0; i < chain.size(); i++) {
				String currentId = chain.get(i);
				if (!idCounts.containsKey(currentId)) continue;

				// Check if any higher tier (j > i) is present in the player's accessory bag
				boolean hasHigher = false;
				for (int j = i + 1; j < chain.size(); j++) {
					if (idCounts.containsKey(chain.get(j))) {
						hasHigher = true;
						break;
					}
				}
				if (hasHigher) {
					supersededIds.add(currentId);
				}
			}
		}

		// Campfire badges special check (CAMPFIRE_TALISMAN_1 .. 29)
		int highestCampfire = 0;
		for (String id : idCounts.keySet()) {
			if (id.startsWith("CAMPFIRE_TALISMAN_")) {
				try {
					int num = Integer.parseInt(id.replace("CAMPFIRE_TALISMAN_", ""));
					if (num > highestCampfire) highestCampfire = num;
				} catch (Exception ignored) {}
			}
		}
		if (highestCampfire > 0) {
			for (int num = 1; num < highestCampfire; num++) {
				supersededIds.add("CAMPFIRE_TALISMAN_" + num);
			}
		}

		// Anniversary Hats special check: count total hats
		List<String> hatItemsFound = new ArrayList<>();
		for (ParsedItem item : items) {
			if (item != null && !item.isEmpty() && item.skyblockId != null) {
				String id = item.skyblockId.toUpperCase(Locale.ROOT);
				if (id.contains("PARTY_HAT") || id.contains("BALLOON_HAT")) {
					hatItemsFound.add(item.displayName.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim());
				}
			}
		}

		// 3. Process items in order: detect duplicate copies and superseded tiers
		Set<String> seenIds = new HashSet<>();
		boolean firstHatProcessed = false;

		for (ParsedItem item : items) {
			if (item == null || item.isEmpty()) continue;

			String id = item.skyblockId != null ? item.skyblockId.toUpperCase(Locale.ROOT) : "";
			String cleanName = item.displayName.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();

			boolean isDuplicate = false;

			// Check exact duplicate copy (2nd or subsequent copy of same item)
			if (!id.isEmpty()) {
				if (seenIds.contains(id)) {
					isDuplicate = true;
				} else {
					seenIds.add(id);
				}
			}

			// Check superseded by higher tier
			if (supersededIds.contains(id)) {
				isDuplicate = true;
			}

			// Check anniversary hats (only 1 hat active if multiple hats exist)
			if (id.contains("PARTY_HAT") || id.contains("BALLOON_HAT")) {
				if (hatItemsFound.size() > 1) {
					if (firstHatProcessed) {
						isDuplicate = true;
					} else {
						firstHatProcessed = true;
					}
				}
			}

			if (isDuplicate) {
				if (!a.duplicates.contains(cleanName)) {
					a.duplicates.add(cleanName);
				}
				continue;
			}

			// Calculate MP for active accessory
			String rarity = (item.rarity != null && !item.rarity.isEmpty()) ? item.rarity.toUpperCase(Locale.ROOT) : "COMMON";
			int mp = getBaseMpForRarity(rarity);

			if (id.contains("HEGEMONY")) {
				mp *= 2;
			}

			a.rarityCounts.put(rarity, a.rarityCounts.getOrDefault(rarity, 0) + 1);
			a.rarityMp.put(rarity, a.rarityMp.getOrDefault(rarity, 0) + mp);
			a.totalMp += mp;
		}

		if (maxwell != null) {
			if (maxwell.consumedRiftPrism) {
				a.riftPrismBonus = 11;
				a.totalMp += 11;
			}
			if (maxwell.abiphoneContacts > 0) {
				a.abiphoneBonus = maxwell.abiphoneContacts / 2;
				a.totalMp += a.abiphoneBonus;
			}
		}

		return a;
	}

	private static int getBaseMpForRarity(String rarity) {
		return switch (rarity) {
			case "COMMON" -> 3;
			case "UNCOMMON" -> 5;
			case "RARE" -> 8;
			case "EPIC" -> 12;
			case "LEGENDARY" -> 16;
			case "MYTHIC", "DIVINE" -> 22;
			case "SPECIAL" -> 3;
			case "VERY SPECIAL" -> 5;
			default -> 3;
		};
	}

	private static int getRarityColor(String rarity) {
		return switch (rarity) {
			case "COMMON" -> 0xFFFFFFFF;
			case "UNCOMMON" -> 0xFF55FF55;
			case "RARE" -> 0xFF5555FF;
			case "EPIC" -> 0xFFAA00AA;
			case "LEGENDARY" -> 0xFFFFAA00;
			case "MYTHIC" -> 0xFFFF55FF;
			case "DIVINE" -> 0xFF55FFFF;
			case "SPECIAL", "VERY SPECIAL" -> 0xFFFF5555;
			default -> 0xFFFFFFFF;
		};
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) return "";
		String clean = str.replace("_", " ").toLowerCase(Locale.ROOT);
		String[] words = clean.split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}
}