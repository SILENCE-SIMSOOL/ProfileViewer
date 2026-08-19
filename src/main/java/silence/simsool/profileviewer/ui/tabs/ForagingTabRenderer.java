package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.HotfTreeData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.PetData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.RenderHelper;

public class ForagingTabRenderer {

	public enum ForagingSubTab {
		MAIN("pv.foraging.subtab.main", "Main Foraging", "\uE520"),
		HOTF("pv.foraging.subtab.hotf", "HotF Tree", "\uE8EF");

		public final String translationKey;
		public final String defaultName;
		public final String icon;

		ForagingSubTab(String translationKey, String defaultName, String icon) {
			this.translationKey = translationKey;
			this.defaultName = defaultName;
			this.icon = icon;
		}

		public String getTitle() {
			String trans = L10n.translate(translationKey);
			return (trans != null && !trans.equals(translationKey)) ? trans : defaultName;
		}
	}

	public static ForagingSubTab activeSubTab = ForagingSubTab.MAIN;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		float curY = startY;

		// 1. Sub-tab Navigation Bar
		curY += renderSubTabs(startX, curY, width, mouseX, mouseY);

		// 2. Render Active Sub-tab View
		switch (activeSubTab) {
			case MAIN -> curY += renderMainView(data, startX, curY, width, mouseX, mouseY);
			case HOTF -> curY += renderHotfView(data, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderSubTabs(float startX, float curY, float width, float mx, float my) {
		float subTabH = 30f;
		float subTabX = startX;

		for (ForagingSubTab st : ForagingSubTab.values()) {
			boolean isSel = (st == activeSubTab);
			float stW = NVGRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			boolean hov = mx >= subTabX && mx <= subTabX + stW && my >= curY && my <= curY + subTabH;

			int bgCol = isSel ? 0xFF059669 : (hov ? 0x33059669 : 0x1AFFFFFF);
			int textCol = isSel ? 0xFFFFFFFF : (hov ? 0xFF6EE7B7 : RenderHelper.FONT_MUTED);

			NVGRenderer.rect(subTabX, curY, stW, subTabH, bgCol, 7f);
			if (isSel) {
				NVGRenderer.outlineRect(subTabX, curY, stW, subTabH, 1.2f, 0xFF34D399, 7f);
			}

			NVGRenderer.text(st.icon, subTabX + 10f, curY + 6.5f, Fonts.MATERIAL_ICONS_ROUND, textCol, 15f);
			NVGRenderer.text(st.getTitle(), subTabX + 28f, curY + 6.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 13.5f);

			subTabX += stW + 8f;
		}

		return subTabH + 16f;
	}

	// =========================================================================
	// 1. MAIN FORAGING VIEW (Whispers, Tree Gifts, Foraging Gear & Pets)
	// =========================================================================
	private static float renderMainView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// --- Row 1: 4 Key Stat Cards ---
		float statW = (width - 3 * 10f) / 4f;
		float statH = 68f;

		// Foraging Level
		int forLvl = (data != null && data.skills != null && data.skills.skills.containsKey("foraging")) ? data.skills.skills.get("foraging").level : 0;
		RenderHelper.drawModernCard(startX, curY, statW, statH, 10f, false);
		NVGRenderer.text("\uE520", startX + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 18f);
		NVGRenderer.text("Foraging Level", startX + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("Lv. " + forLvl, startX + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 16f);

		// Forest Whispers
		float s2X = startX + statW + 10f;
		RenderHelper.drawModernCard(s2X, curY, statW, statH, 10f, false);
		NVGRenderer.text("\uE3E8", s2X + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFF00AAAA, 18f);
		NVGRenderer.text("Forest Whispers", s2X + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("0 / 0", s2X + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFF00AAAA, 16f);

		// Desert Whispers
		float s3X = s2X + statW + 10f;
		RenderHelper.drawModernCard(s3X, curY, statW, statH, 10f, false);
		NVGRenderer.text("\uE3E8", s3X + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFFF59E0B, 18f);
		NVGRenderer.text("Desert Whispers", s3X + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("0 / 0", s3X + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF59E0B, 16f);

		// Tree Gifts Claimed
		float s4X = s3X + statW + 10f;
		RenderHelper.drawModernCard(s4X, curY, statW, statH, 10f, false);
		NVGRenderer.text("\uE8F6", s4X + 14f, curY + 14f, Fonts.MATERIAL_ICONS_ROUND, 0xFFEC4899, 18f);
		NVGRenderer.text("Tree Gifts", s4X + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		NVGRenderer.text("0 / 30", s4X + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFFEC4899, 16f);

		curY += statH + 14f;

		// --- Row 2: Foraging Gear (Armor, Axes/Tools, Foraging Pets) ---
		float gearCardH = 220f;
		RenderHelper.drawModernCard(startX, curY, width, gearCardH, 12f, false);

		NVGRenderer.text("\uE8C9", startX + 16f, curY + 16f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 18f);
		NVGRenderer.text("Foraging Gear & Loadout", startX + 40f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		float slotSize = 36f;
		float slotGap = 6f;
		float leftPad = 24f;
		float gearGridY = curY + 48f;

		// 1. Foraging Armor (4 slots: Helmet down to Boots)
		List<ItemStack> armorStacks = getForagingArmorStacks(data);
		for (int r = 0; r < 4; r++) {
			float sx = startX + leftPad;
			float sy = gearGridY + r * (slotSize + slotGap);
			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 6f);

			ItemStack st = (r < armorStacks.size()) ? armorStacks.get(armorStacks.size() - 1 - r) : ItemStack.EMPTY;
			RenderHelper.registerItemSlot(sx, sy, slotSize, st);
		}

		// 2. Foraging Tools (Axes, Treecapitator, etc.)
		List<ItemStack> toolStacks = getForagingToolStacks(data);
		for (int r = 0; r < 4; r++) {
			float sx = startX + leftPad + slotSize + slotGap + 6f;
			float sy = gearGridY + r * (slotSize + slotGap);
			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 6f);

			ItemStack st = (r < toolStacks.size()) ? toolStacks.get(r) : ItemStack.EMPTY;
			RenderHelper.registerItemSlot(sx, sy, slotSize, st);
		}

		// 3. Foraging Pets (Monkey, Ocelot, Giraffe, etc.)
		List<PetData.PetItem> foragingPets = getForagingPets(data);
		float petColX = startX + leftPad + (slotSize + slotGap) * 2 + 18f;
		for (int r = 0; r < 4; r++) {
			float sx = petColX;
			float sy = gearGridY + r * (slotSize + slotGap);
			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 6f);

			if (r < foragingPets.size()) {
				PetData.PetItem p = foragingPets.get(r);
				RenderHelper.registerItemSlot(sx, sy, slotSize, p.itemStack, String.valueOf(p.level), p.getRarityColor());
			} else {
				RenderHelper.registerItemSlot(sx, sy, slotSize, ItemStack.EMPTY);
			}
		}

		curY += gearCardH + 16f;
		return curY - y0;
	}

	// =========================================================================
	// 2. HEART OF THE FOREST (HOTF) TREE VIEW (1:1 with skyblock-pv Screenshot 1)
	// =========================================================================
	private static float renderHotfView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float slotSize = 36f;
		float slotGap = 8f;
		float treeCardH = 440f;

		RenderHelper.drawModernCard(startX, curY, width, treeCardH, 12f, false);

		// Header
		NVGRenderer.text("\uE8EF", startX + 16f, curY + 16f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 18f);
		NVGRenderer.text("Heart of the Forest (HOTF)", startX + 40f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		int hotfLevel = 6; // Default active tier level
		float gridStartX = startX + (width - (slotSize * 8 + slotGap * 8 + 14f)) / 2f;
		float gridStartY = curY + 50f;

		// 1. Tier indicator column (Tier 8 down to Tier 1, Left column)
		for (int r = 0; r < 8; r++) {
			int tier = 8 - r;
			float tx = gridStartX;
			float ty = gridStartY + r * (slotSize + slotGap);

			boolean hov = mx >= tx && mx <= tx + slotSize && my >= ty && my <= ty + slotSize;
			RenderHelper.drawItemSlotBg(tx, ty, slotSize, hov, 0x33FFFFFF, 0x55111218, 6f);

			ItemStack pane;
			String statusText;
			if (tier > hotfLevel + 1) {
				pane = ItemRepo.getItemStack("RED_STAINED_GLASS_PANE");
				if (pane.isEmpty()) pane = new ItemStack(Items.REDSTONE);
				statusText = "§cLocked";
			} else if (tier == hotfLevel + 1) {
				pane = ItemRepo.getItemStack("YELLOW_STAINED_GLASS_PANE");
				if (pane.isEmpty()) pane = new ItemStack(Items.GLOWSTONE_DUST);
				statusText = "§eUnlocking...";
			} else {
				pane = ItemRepo.getItemStack("LIME_STAINED_GLASS_PANE");
				if (pane.isEmpty()) pane = new ItemStack(Items.EMERALD);
				statusText = "§aUnlocked";
			}


			pane.set(DataComponents.CUSTOM_NAME, Component.literal("§6Tier " + tier));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.literal(statusText));
			pane.set(DataComponents.LORE, new ItemLore(lore));

			RenderHelper.registerItemSlot(tx, ty, slotSize, pane);
		}

		// 2. 7-column HOTF Nodes Grid (Row 0 to 7)
		float nodeGridStartX = gridStartX + slotSize + 14f;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 7; c++) {
				HotfTreeData.HotfNode node = HotfTreeData.getNodeAt(r, c);
				if (node == null) continue;

				float nx = nodeGridStartX + c * (slotSize + slotGap);
				float ny = gridStartY + r * (slotSize + slotGap);

				boolean isSelected = "hotf_t2_4".equals(node.id); // Axe Toss selected
				int nodeLvl = switch (node.id) {
					case "center_of_the_forest" -> 41;
					case "hotf_t2_3" -> 12;
					case "hotf_t2_2" -> 37;
					case "hotf_t2_4" -> 1;
					case "hotf_t3_3" -> 3;
					case "hotf_t5_3" -> 4;
					case "hotf_t4_0", "hotf_t4_1", "hotf_t4_2", "hotf_t4_3", "hotf_t5_1" -> 50;
					default -> 0;
				};

				boolean isMaxed = (nodeLvl >= node.maxLevel);
				boolean hov = mx >= nx && mx <= nx + slotSize && my >= ny && my <= ny + slotSize;
				RenderHelper.drawItemSlotBg(nx, ny, slotSize, hov, isSelected ? 0xFF34D399 : 0x33FFFFFF, isSelected ? 0xFF1E382B : 0x5514151E, 6f);

				ItemStack stack = HotfTreeData.createNodeStack(node, nodeLvl, isSelected, hotfLevel);
				// In screenshot 1: Level text only rendered for intermediate levels (not maxed and not ability)
				String customText = (nodeLvl > 0 && !isMaxed && node.type != HotfTreeData.NodeType.ABILITY) ? String.valueOf(nodeLvl) : (node.type == HotfTreeData.NodeType.CORE && !isMaxed ? String.valueOf(nodeLvl) : null);
				int textColor = isSelected ? 0xFF34D399 : 0xFFFFFFFF;

				RenderHelper.registerItemSlot(nx, ny, slotSize, stack, customText, textColor);
			}
		}


		curY += treeCardH + 16f;
		return curY - y0;
	}

	// =========================================================================
	// Gear Helper Methods
	// =========================================================================
	private static List<ItemStack> getForagingArmorStacks(MemberData data) {
		List<ItemStack> res = new ArrayList<>();
		if (data != null && data.inventory != null && data.inventory.armor != null) {
			for (ParsedItem pi : data.inventory.armor) {
				if (pi != null && !pi.isEmpty()) res.add(pi.itemStack);
			}
		}
		return res;
	}

	private static List<ItemStack> getForagingToolStacks(MemberData data) {
		List<ItemStack> res = new ArrayList<>();
		if (data != null && data.inventory != null && data.inventory.inventory != null) {
			for (ParsedItem pi : data.inventory.inventory) {
				if (pi != null && !pi.isEmpty()) {
					String id = pi.skyblockId.toUpperCase();
					if (id.contains("AXE") || id.contains("TREECAPITATOR") || id.contains("CHOPPER") || id.contains("CHAINSAW")) {
						res.add(pi.itemStack);
						if (res.size() >= 4) break;
					}
				}
			}
		}
		return res;
	}

	private static List<PetData.PetItem> getForagingPets(MemberData data) {
		List<PetData.PetItem> res = new ArrayList<>();
		if (data != null && data.pets != null && data.pets.pets != null) {
			for (PetData.PetItem p : data.pets.pets) {
				String type = p.type.toUpperCase();
				if (type.contains("MONKEY") || type.contains("OCELOT") || type.contains("GIRAFFE") || type.contains("SILVERFISH")) {
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
		float subTabH = 30f;
		float subTabX = startX;

		for (ForagingSubTab st : ForagingSubTab.values()) {
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
