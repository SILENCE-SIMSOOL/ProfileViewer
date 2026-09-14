package silence.simsool.profileviewer.ui.tabs;

import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.RiftData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class RiftTabRenderer {

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;

		if (data == null || data.rift == null) {
			NVGRenderer.text(L10n.translate("pv.rift.no_data"), startX + 14f, curY + 14f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return 40f;
		}

		RiftData r = data.rift;

		// Summary Row (3 Stat Cards)
		float colW = (width - 24f) / 3f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Card 1: Motes
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float ix1 = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		NVGRenderer.rect(ix1, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFDA70D6, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE3E8", ix1 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFDA70D6, 21f);

		float tx1 = ix1 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.rift.motes"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(RenderHelper.formatNumber(r.motes), tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFDA70D6, 19f);

		// Card 2: Enigma Souls
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		NVGRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF818CF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE87D", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.rift.enigma_souls"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(r.enigmaSouls + " / 42 " + L10n.translate("pv.rift.found"), tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 19f);

		// Card 3: Timecharms
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		NVGRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE838", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		NVGRenderer.text(L10n.translate("pv.rift.timecharms"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(r.timecharms + " / 8 " + L10n.translate("pv.ui.unlocked"), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);

		curY += cardH + 24f;

		// Timecharms Grid (8 Charms)
		NVGRenderer.text("\uE838", startX + 4f, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 18f);
		NVGRenderer.text(L10n.translate("pv.rift.timecharms_grid"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float tcW = (width - 3 * 10f) / 4f;
		float tcH = 62f;
		float slotS = 36f;

		for (int i = 0; i < RiftData.TIMECHARMS.length; i++) {
			String tc = RiftData.TIMECHARMS[i];
			boolean unlocked = r.unlockedTimecharms.contains(tc);

			float tcx = startX + (i % 4) * (tcW + 10f);
			float tcy = curY + (i / 4) * (tcH + 10f);

			boolean hov = mouseX >= tcx && mouseX <= tcx + tcW && mouseY >= tcy && mouseY <= tcy + tcH;
			RenderHelper.drawModernCard(tcx, tcy, tcW, tcH, 10f, hov);

			// Slot
			float sx = tcx + 10f;
			float sy = tcy + 13f;
			boolean hovS = mouseX >= sx && mouseX <= sx + slotS && mouseY >= sy && mouseY <= sy + slotS;
			RenderHelper.drawItemSlotBg(sx, sy, slotS, hovS, unlocked ? 0x4438BDF8 : 0x22FFFFFF, 0x5511131E, 6f);

			net.minecraft.world.item.ItemStack tcStack = createTimecharmStack(tc, unlocked);
			RenderHelper.registerItemSlot(sx, sy, slotS, tcStack);

			float tx = sx + slotS + 10f;
			NVGRenderer.text(tc.replace(" Timecharm", ""), tx, tcy + 14f, Fonts.PRETENDARD_SEMIBOLD, unlocked ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED, 13.5f);
			String statusStr = unlocked ? L10n.translate("pv.ui.unlocked") : L10n.translate("pv.ui.locked");
			int statCol = unlocked ? 0xFF10B981 : RenderHelper.FONT_DISABLED;
			NVGRenderer.text(statusStr, tx, tcy + 34f, Fonts.PRETENDARD_MEDIUM, statCol, 12.5f);
		}

		curY += 2 * (tcH + 10f) + 20f;
		if (r.inventory.available) {
			curY += GearTabRenderer.renderStorage("Rift Inventory", r.inventory.inventory, startX, curY, width, mouseX, mouseY);
			curY += GearTabRenderer.renderStorage("Rift Armor", r.inventory.armor, startX, curY, width, mouseX, mouseY);
			curY += GearTabRenderer.renderStorage("Rift Equipment", r.inventory.equipment, startX, curY, width, mouseX, mouseY);
			curY += GearTabRenderer.renderStorage("Rift Ender Chest", r.inventory.enderchest, startX, curY, width, mouseX, mouseY);
		}
		return curY - startY;
	}

	private static net.minecraft.world.item.ItemStack createTimecharmStack(String charmName, boolean unlocked) {
		String cleanId = charmName.toUpperCase().replace(" ", "_");
		net.minecraft.world.item.ItemStack stack = silence.simsool.profileviewer.api.repo.ItemRepo.getItemStack(cleanId);
		if (stack.isEmpty()) {
			stack = unlocked ? new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD) : new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GUNPOWDER);
		}
		stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal(unlocked ? "§b" + charmName : "§7" + charmName));
		java.util.List<net.minecraft.network.chat.Component> lore = new java.util.ArrayList<>();
		lore.add(net.minecraft.network.chat.Component.literal(unlocked ? "§aStatus: Unlocked" : "§cStatus: Locked"));
		stack.set(net.minecraft.core.component.DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(lore));
		return stack;
	}
}