package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.HotfTreeData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.MiningData;
import silence.simsool.profileviewer.api.data.ForagingData;
import silence.simsool.profileviewer.api.data.ProfileJson;
import java.util.Locale;
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
	public static int activeLoadoutSlot = 1;
	private static MiningData lastMiningData;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		float curY = startY;
		if (data != null && data.mining != lastMiningData) {
			activeLoadoutSlot = Math.max(1, Math.min(5, data.mining.selectedForagingPreset));
			lastMiningData = data.mining;
		}

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
			float stW = SkijaRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			boolean hov = mx >= subTabX && mx <= subTabX + stW && my >= curY && my <= curY + subTabH;

			int bgCol = isSel ? 0xFF059669 : (hov ? 0x33059669 : 0x1AFFFFFF);
			int textCol = isSel ? 0xFFFFFFFF : (hov ? 0xFF6EE7B7 : RenderHelper.FONT_MUTED);

			SkijaRenderer.rect(subTabX, curY, stW, subTabH, bgCol, 7f);
			if (isSel) {
				SkijaRenderer.outlineRect(subTabX, curY, stW, subTabH, 1.2f, 0xFF34D399, 7f);
			}

			RenderHelper.alignedIcon(st.icon, subTabX + 10f, curY + 6.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 15f, 13.5f);
			SkijaRenderer.text(st.getTitle(), subTabX + 28f, curY + 6.5f, Fonts.PRETENDARD_SEMIBOLD, textCol, 13.5f);

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
		RenderHelper.alignedIcon("\uE520", startX + 14f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 18f, 13.5f);
		SkijaRenderer.text("Foraging Level", startX + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		SkijaRenderer.text("Lv. " + forLvl, startX + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 16f);

		// Heart of the Forest Level
		int hotfLvl = (data != null && data.mining != null) ? data.mining.hotfLevel : 0;
		float s2X = startX + statW + 10f;
		RenderHelper.drawModernCard(s2X, curY, statW, statH, 10f, false);
		RenderHelper.alignedIcon("\uE8EF", s2X + 14f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, 0xFF059669, 18f, 13.5f);
		SkijaRenderer.text("HotF Level", s2X + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		SkijaRenderer.text("Lv. " + hotfLvl, s2X + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFF059669, 16f);

		// HotF Experience
		double hotfExp = (data != null && data.mining != null) ? data.mining.hotfExperience : 0;
		float s3X = s2X + statW + 10f;
		RenderHelper.drawModernCard(s3X, curY, statW, statH, 10f, false);
		RenderHelper.alignedIcon("\uE838", s3X + 14f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF59E0B, 18f, 13.5f);
		SkijaRenderer.text("HotF Experience", s3X + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		SkijaRenderer.text(RenderHelper.formatNumber((long) hotfExp), s3X + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFFF59E0B, 16f);

		// Wood Essence
		long woodEssence = (data != null && data.essence != null) ? data.essence.getOrDefault("foraging", 0L) : 0L;
		float s4X = s3X + statW + 10f;
		RenderHelper.drawModernCard(s4X, curY, statW, statH, 10f, false);
		RenderHelper.alignedIcon("\uE520", s4X + 14f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 18f, 13.5f);
		SkijaRenderer.text("Wood Essence", s4X + 36f, curY + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		SkijaRenderer.text(RenderHelper.formatNumber(woodEssence), s4X + 14f, curY + 38f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 16f);

				curY += statH + 14f;
		ForagingData f = data.foraging;
		RenderHelper.drawModernCard(startX, curY, width, 102f, 10f, false);
		RenderHelper.drawStatRow("Forest Whispers · Current / Total", RenderHelper.formatNumber(f.forestWhispers) + " / " + RenderHelper.formatNumber(f.forestTotal), startX + 16f, curY + 16f, width - 32f, 14f, 0xFF34D399);
		RenderHelper.drawStatRow("Desert Whispers · Current / Total", RenderHelper.formatNumber(f.desertWhispers) + " / " + RenderHelper.formatNumber(f.desertTotal), startX + 16f, curY + 44f, width - 32f, 14f, 0xFFFBBF24);
		RenderHelper.drawStatRow("Daily Trees / Gifts", f.dailyTrees + " / " + f.dailyGifts, startX + 16f, curY + 72f, width - 32f, 14f, RenderHelper.FONT_SECONDARY);
		curY += 118f;
		String[] types = {"FIG", "MANGROVE", "HELIX"};
		for (String type : types) {
			String key = type.toLowerCase(Locale.ROOT);
			String npc = type.equals("HELIX") ? "miria" : "agatha";
			int fortune = f.perks.getOrDefault(npc + "_" + key + "_fortune", 0);
			boolean personalBest = f.perks.containsKey(npc + "_" + key + "_personal_best");
			RenderHelper.drawModernCard(startX, curY, width, 126f, 10f, false);
			SkijaRenderer.text(type.charAt(0) + key.substring(1), startX + 16f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFF34D399, 15f);
			RenderHelper.drawStatRow("Gifts · Tier / Total", f.giftTiers.getOrDefault(type, 0) + "/" + ProfileJson.array(ForagingData.CATALOG, "tree_gifts", key).size() + " · " + RenderHelper.formatNumber(f.treeGifts.getOrDefault(type, 0L)), startX + 16f, curY + 42f, width - 32f, 14f, RenderHelper.FONT_PRIMARY);
			RenderHelper.drawStatRow("Personal Best", personalBest ? RenderHelper.formatNumber(f.personalBests.getOrDefault(type + "_LOG", 0L)) : "Locked", startX + 16f, curY + 68f, width - 32f, 14f, RenderHelper.FONT_PRIMARY);
			RenderHelper.drawStatRow("Fortune", fortune + " / " + ProfileJson.number(ForagingData.CATALOG, "misc", key + "_fortune"), startX + 16f, curY + 94f, width - 32f, 14f, RenderHelper.FONT_PRIMARY);
			curY += 142f;
		}

		return curY - y0;
	}

	// =========================================================================
	// 2. HEART OF THE FOREST (HOTF) TREE VIEW (1:1 with skyblock-pv Screenshot 1)
	// =========================================================================
	private static float renderHotfView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		float slotSize = 36f;
		float slotGap = 8f;
		float treeCardH = 496f;

		RenderHelper.drawModernCard(startX, curY, width, treeCardH, 12f, false);

		// Header
		RenderHelper.alignedIcon("\uE8EF", startX + 16f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 18f, 16f);
		SkijaRenderer.text("Heart of the Forest (HOTF)", startX + 40f, curY + 15f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		int hotfLevel = data != null ? data.mining.hotfLevel : 0;
		Map<String, Integer> activeNodes = data != null ? data.mining.foragingPresetNodes.getOrDefault(activeLoadoutSlot, Map.of()) : Map.of();
		String activeAbility = data != null ? data.mining.foragingPresetAbilities.getOrDefault(activeLoadoutSlot, "") : "";
		renderLoadoutSlots(startX, curY + 42f, width, mx, my);
		float gridStartX = startX + (width - (slotSize * 8 + slotGap * 8 + 14f)) / 2f;
		float gridStartY = curY + 106f;

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

				boolean isSelected = node.id.equals(activeAbility) || activeAbility.endsWith(node.id);
				int nodeLvl = activeNodes.getOrDefault(node.id, -1);

				boolean isMaxed = (nodeLvl >= node.maxLevel);
				boolean hov = mx >= nx && mx <= nx + slotSize && my >= ny && my <= ny + slotSize;
				RenderHelper.drawItemSlotBg(nx, ny, slotSize, hov, isSelected ? 0xFF34D399 : 0x33FFFFFF, isSelected ? 0xFF1E382B : 0x5514151E, 6f);

				ItemStack stack = HotfTreeData.createNodeStack(node, nodeLvl, isSelected, hotfLevel, activeNodes.getOrDefault("center_of_the_forest", 0), data.mining.disabledForagingNodes.contains(node.id));
				// In screenshot 1: Level text only rendered for intermediate levels (not maxed and not ability)
				String customText = (nodeLvl > 0 && !isMaxed && node.type != HotfTreeData.NodeType.ABILITY) ? String.valueOf(nodeLvl) : (node.type == HotfTreeData.NodeType.CORE && nodeLvl >= 0 && !isMaxed ? String.valueOf(nodeLvl) : null);
				int textColor = isSelected ? 0xFF34D399 : 0xFFFFFFFF;

				RenderHelper.registerItemSlot(nx, ny, slotSize, stack, customText, textColor);
			}
		}


		curY += treeCardH + 16f;
		return curY - y0;
	}

	private static void renderLoadoutSlots(float startX, float y, float width, float mx, float my) {
		float size = 40f;
		float gap = 8f;
		float x = startX + (width - (size * 5f + gap * 4f)) / 2f;
		for (int slot = 1; slot <= 5; slot++) {
			float sx = x + (slot - 1) * (size + gap);
			boolean selected = slot == activeLoadoutSlot;
			boolean hover = mx >= sx && mx <= sx + size && my >= y && my <= y + size;
			RenderHelper.drawItemSlotBg(sx, y, size, hover, selected ? 0xFF34D399 : 0x33FFFFFF, selected ? 0xFF173D31 : 0x5514151E, 6f);
			ItemStack icon = ItemRepo.getItemStack("HEART_OF_THE_FOREST");
			if (icon.isEmpty()) icon = new ItemStack(Items.OAK_SAPLING);
			RenderHelper.registerItemSlot(sx, y, size, icon, String.valueOf(slot), selected ? 0xFF6EE7B7 : 0xFFFFFFFF);
		}
	}


	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 30f;
		float subTabX = startX;

		for (ForagingSubTab st : ForagingSubTab.values()) {
			float stW = SkijaRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		if (activeSubTab == ForagingSubTab.HOTF) {
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
