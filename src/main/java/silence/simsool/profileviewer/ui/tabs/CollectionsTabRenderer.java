package silence.simsool.profileviewer.ui.tabs;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.CollectionData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class CollectionsTabRenderer {

	public enum ColCategory {
		FARMING("pv.col.subtab.farming", "\uE56C"),
		MINING("pv.col.subtab.mining", "\uE52F"),
		COMBAT("pv.col.subtab.combat", "\uE834"),
		FORAGING("pv.col.subtab.foraging", "\uE5D2"),
		FISHING("pv.col.subtab.fishing", "\uEA40"),
		MINIONS("pv.col.subtab.minions", "\uE88A");

		public final String translationKey;
		public final String icon;
		ColCategory(String translationKey, String icon) {
			this.translationKey = translationKey;
			this.icon = icon;
		}

		public String getTitle() {
			return L10n.translate(translationKey);
		}
	}

	public static ColCategory activeCategory = ColCategory.FARMING;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		CollectionData col = data.collections;

		// Sub-tabs bar (Modern Pills)
		float subTabH = 32f;
		float subTabX = startX;
		for (ColCategory cat : ColCategory.values()) {
			String title = cat.getTitle();
			float catW = SkijaRenderer.textWidth(title, Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			boolean active = (cat == activeCategory);
			boolean hov = mouseX >= subTabX && mouseX <= subTabX + catW && mouseY >= curY && mouseY <= curY + subTabH;

			if (active) {
				SkijaRenderer.rect(subTabX, curY, catW, subTabH, 0xBF4F46E5, 8f);
			} else if (hov) {
				SkijaRenderer.rect(subTabX, curY, catW, subTabH, 0x1AFFFFFF, 8f);
			}

			int textColor = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);
			RenderHelper.alignedIcon(cat.icon, subTabX + 10f, curY + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, 16f, 14f);
			SkijaRenderer.text(title, subTabX + 30f, curY + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, 14f);

			subTabX += catW + 8f;
		}

		curY += subTabH + 16f;

		if (activeCategory == ColCategory.MINIONS) {
			curY += renderMinionsView(col, startX, curY, width, mouseX, mouseY);
		} else {
			if (!col.available) {
				SkijaRenderer.text("Collection API is disabled or unavailable.", startX + 14f, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
				return curY - startY + 50f;
			}
			List<CollectionData.CollectionItem> items = switch (activeCategory) {
				case FARMING -> col.farmingCollections;
				case MINING -> col.miningCollections;
				case COMBAT -> col.combatCollections;
				case FORAGING -> col.foragingCollections;
				case FISHING -> col.fishingCollections;
				default -> col.farmingCollections;
			};
			curY += renderCollectionItemsGrid(items, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderCollectionItemsGrid(List<CollectionData.CollectionItem> items, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		if (items == null || items.isEmpty()) {
			SkijaRenderer.text(L10n.translate("pv.col.no_data"), startX + 14f, curY + 20f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return 50f;
		}

		float cardW = (width - 16f) / 2f;
		float cardH = 70f;
		int idx = 0;

		for (CollectionData.CollectionItem item : items) {
			float cx = startX + (idx % 2) * (cardW + 16f);
			float cy = curY + (idx / 2) * (cardH + 12f);

			boolean hov = mx >= cx && mx <= cx + cardW && my >= cy && my <= cy + cardH;
			RenderHelper.drawModernCard(cx, cy, cardW, cardH, 10f, hov);

			// Item Slot on Left
			float slotS = 36f;
			float sx = cx + 12f;
			float sy = cy + 17f;
			boolean hovS = mx >= sx && mx <= sx + slotS && my >= sy && my <= sy + slotS;
			RenderHelper.drawItemSlotBg(sx, sy, slotS, hovS, 0x33FFFFFF, 0x5511131E, 6f);

			ItemStack colStack = createCollectionStack(item);
			RenderHelper.registerItemSlot(sx, sy, slotS, colStack);

			float tx = sx + slotS + 12f;
			SkijaRenderer.text(item.name, tx, cy + 12f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14.5f);

			String status = item.isMax ? L10n.translate("pv.ui.max") : String.format(L10n.translate("pv.ui.tier") + " %d/%d (%.1f%%)", item.tier, item.maxTier, item.progress * 100f);
			int statCol = item.isMax ? 0xFFFFAA00 : 0xFF38BDF8;
			float sw = SkijaRenderer.textWidth(status, Fonts.PRETENDARD_SEMIBOLD, 13f);
			SkijaRenderer.text(status, cx + cardW - 14f - sw, cy + 12f, Fonts.PRETENDARD_SEMIBOLD, statCol, 13f);

			String amtStr = RenderHelper.formatNumber(item.amount) + " " + L10n.translate("pv.col.collected");
			SkijaRenderer.text(amtStr, tx, cy + 30f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

			if (item.isMax) {
				RenderHelper.drawRainbowProgressBar(tx, cy + 50f, cx + cardW - 14f - tx, 5f, 1.0f);
			} else {
				RenderHelper.drawProgressBar(tx, cy + 50f, cx + cardW - 14f - tx, 5f, item.progress, 0xFF4F46E5, 0xFF818CF8);
			}

			idx++;
		}

		curY += ((idx + 1) / 2) * (cardH + 12f) + 12f;
		return curY - y0;
	}

	private static ItemStack createCollectionStack(CollectionData.CollectionItem item) {
		String cleanId = item.id.toUpperCase();
		ItemStack stack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack(cleanId);
		if (stack.isEmpty()) {
			stack = silence.simsool.profileviewer.api.nbt.NbtItemParser.resolveItemStack(0, "", cleanId, 0, 1);
		}
		if (stack.isEmpty()) {
			stack = new ItemStack(net.minecraft.world.item.Items.PAPER);
		}
		stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("§a" + item.name));
		List<net.minecraft.network.chat.Component> lore = new java.util.ArrayList<>();
		lore.add(net.minecraft.network.chat.Component.literal("§7Tier: §e" + item.tier + " / " + item.maxTier));
		lore.add(net.minecraft.network.chat.Component.literal("§7Collected: §b" + RenderHelper.formatNumber(item.amount)));
		stack.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));
		return stack;
	}

	private static float renderMinionsView(CollectionData col, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;

		// Summary Row (3 Cards)
		float colW = (width - 24f) / 3f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Unlocked Minions
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float ix1 = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		SkijaRenderer.rect(ix1, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE88A", ix1 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx1 = ix1 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.col.unlocked_minions"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(col.unlockedMinions + " " + L10n.translate("pv.ui.unique"), tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);

		// Minion Slots
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		SkijaRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF10B981, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE838", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF10B981, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.col.minion_slots"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(col.minionSlots + " Slots", tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF10B981, 19f);

		// Crafted Minions
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		SkijaRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFBBF24, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE8C9", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.col.crafted_generators"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(col.craftedMinions.size() + " Crafted", tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 19f);

		curY += cardH + 24f;

		if (!col.craftedMinions.isEmpty()) {
			RenderHelper.alignedIcon("\uE8C9", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 18f, 16f);
			SkijaRenderer.text(L10n.translate("pv.col.crafted_minions_list") + " (" + col.craftedMinions.size() + ")", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 24f;

			float mColW = (width - 3 * 10f) / 4f;
			float mH = 50f;
			int idx = 0;

			for (String minion : col.craftedMinions) {
				float mx_ = startX + (idx % 4) * (mColW + 10f);
				float my_ = curY + (idx / 4) * (mH + 8f);

				boolean hov = mx >= mx_ && mx <= mx_ + mColW && my >= my_ && my <= my_ + mH;
				RenderHelper.drawModernCard(mx_, my_, mColW, mH, 8f, hov);

				float msS = 32f;
				float msX = mx_ + 8f;
				float msY = my_ + 9f;
				boolean hovS = mx >= msX && mx <= msX + msS && my >= msY && my <= msY + msS;
				RenderHelper.drawItemSlotBg(msX, msY, msS, hovS, 0x33FFFFFF, 0x5511131E, 4f);

				ItemStack minionStack = createMinionStack(minion);
				RenderHelper.registerItemSlot(msX, msY, msS, minionStack);

				String mName = minion.replace("_GENERATOR_", " ").replace("_", " ").toLowerCase();
				mName = mName.substring(0, 1).toUpperCase() + mName.substring(1);
				SkijaRenderer.text(mName, msX + msS + 8f, my_ + 17f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, 13f);

				idx++;
			}
			curY += ((idx + 3) / 4) * (mH + 8f) + 12f;
		}

		return curY - y0;
	}

	private static ItemStack createMinionStack(String minionKey) {
		String cleanId = minionKey.toUpperCase();
		if (!cleanId.contains("_GENERATOR_")) cleanId = cleanId.replaceFirst("_([0-9]+)$", "_GENERATOR_$1");
		ItemStack stack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack(cleanId);
		if (stack.isEmpty()) {
			stack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack(cleanId + "_1");
		}
		if (stack.isEmpty()) {
			stack = new ItemStack(net.minecraft.world.item.Items.BARRIER);
		}
		String mName = minionKey.replace("_GENERATOR_", " ").replace("_", " ").toLowerCase();
		mName = mName.substring(0, 1).toUpperCase() + mName.substring(1);
		stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("§a" + mName + " Minion"));
		return stack;
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 32f;
		float subTabX = startX;
		for (ColCategory cat : ColCategory.values()) {
			float catW = SkijaRenderer.textWidth(cat.getTitle(), Fonts.PRETENDARD_MEDIUM, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + catW && my >= startY && my <= startY + subTabH) {
				activeCategory = cat;
				return true;
			}
			subTabX += catW + 8f;
		}
		return false;
	}
}