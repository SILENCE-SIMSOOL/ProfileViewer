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
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.CfData;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.ProfileJson;
import silence.simsool.profileviewer.api.nbt.NbtItemParser;
import com.google.gson.JsonObject;
import silence.simsool.profileviewer.ui.RenderHelper;

public class ChocolateFactoryTabRenderer {
	private static final JsonObject EMPLOYEE_TEXTURES = ProfileJson.catalog("chocolate_textures");
	private static final JsonObject SKULL_TEXTURES = ProfileJson.catalog("skull_textures");

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		CfData cf = data.cf;

		if (!cf.available) {
			RenderHelper.drawModernCard(startX, curY, width, 80f, 12f, false);
			RenderHelper.alignedIcon("\uE5D2", startX + 24f, curY + 22f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, 24f, 16f);
			SkijaRenderer.text(L10n.translate("pv.cf.no_data_title"), startX + 60f, curY + 22f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			SkijaRenderer.text(L10n.translate("pv.cf.no_data_desc"), startX + 60f, curY + 44f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13f);
			return 90f;
		}

		// Row 1: 3 Summary Cards
		float colW = (width - 24f) / 3f;
		float cardH = 80f;
		float iconBoxSize = 40f;

		// Card 1: Current Chocolate
		RenderHelper.drawModernCard(startX, curY, colW, cardH, 12f, false);
		float ix1 = startX + 14f;
		float iy = curY + (cardH - iconBoxSize) / 2f;
		SkijaRenderer.rect(ix1, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFD2691E, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE5D2", ix1 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFD2691E, 21f);

		float tx1 = ix1 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.cf.current_chocolate"), tx1, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(RenderHelper.formatNumber(cf.chocolate), tx1, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFD2691E, 19f);
		SkijaRenderer.text("Lifetime: " + RenderHelper.formatNumber(cf.totalChocolate), tx1, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 2: Prestige & Barn
		float c2X = startX + colW + 12f;
		RenderHelper.drawModernCard(c2X, curY, colW, cardH, 12f, false);
		float ix2 = c2X + 14f;
		SkijaRenderer.rect(ix2, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFF38BDF8, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE838", ix2 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 21f);

		float tx2 = ix2 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.cf.factory_prestige"), tx2, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text("Prestige " + cf.prestigeLevel, tx2, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFF38BDF8, 19f);
		SkijaRenderer.text(L10n.translate("pv.cf.barn_capacity") + ": " + (18 + cf.barnCapacityLevel * 2), tx2, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		// Card 3: Rabbits Collected
		float c3X = c2X + colW + 12f;
		RenderHelper.drawModernCard(c3X, curY, colW, cardH, 12f, false);
		float ix3 = c3X + 14f;
		SkijaRenderer.rect(ix3, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(0xFFFBBF24, 32), iconBoxSize / 2f);
		SkijaRenderer.text("\uE91D", ix3 + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, 0xFFFBBF24, 21f);

		float tx3 = ix3 + iconBoxSize + 12f;
		SkijaRenderer.text(L10n.translate("pv.cf.rabbits_collected"), tx3, curY + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		SkijaRenderer.text(cf.rabbits.size() + " " + L10n.translate("pv.cf.unique_rabbits"), tx3, curY + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 19f);
		SkijaRenderer.text(L10n.translate("pv.cf.since_prestige") + ": " + RenderHelper.formatNumber(cf.chocolateSincePrestige), tx3, curY + 58f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12.5f);

		curY += cardH + 20f;

		// Section: Factory Upgrades (4 Cards with Actual Item Slots)
		RenderHelper.alignedIcon("\uE5D5", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 18f, 16f);
		SkijaRenderer.text(L10n.translate("pv.cf.factory_upgrades"), startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 24f;

		float uColW = (width - 3 * 10f) / 4f;
		float uCardH = 74f;
		float uSlotS = 36f;

		// Upgrade 1: Cookie (Click Upgrade)
		renderUpgradeCard(startX, curY, uColW, uCardH, uSlotS, createCookieUpgradeStack(cf.clickUpgrades + 1), "Click Upgrade", "Lv. " + (cf.clickUpgrades + 1), 0xFF38BDF8, mouseX, mouseY);

		// Upgrade 2: Clock (Time Tower)
		renderUpgradeCard(startX + uColW + 10f, curY, uColW, uCardH, uSlotS, createClockUpgradeStack(cf), "Time Tower", "Lv. " + cf.timeTowerLevel, 0xFF10B981, mouseX, mouseY);

		// Upgrade 3: Rabbit Foot (Rabbit Shrine)
		renderUpgradeCard(startX + 2 * (uColW + 10f), curY, uColW, uCardH, uSlotS, createShrineUpgradeStack(cf.rabbitRarityUpgrades), "Rabbit Shrine", "Lv. " + cf.rabbitRarityUpgrades, 0xFFF472B6, mouseX, mouseY);

		// Upgrade 4: Coach Jackrabbit
		renderUpgradeCard(startX + 3 * (uColW + 10f), curY, uColW, uCardH, uSlotS, createJackrabbitUpgradeStack(cf.chocolateMultiplierUpgrades), "Jackrabbit", "Lv. " + cf.chocolateMultiplierUpgrades, 0xFFFBBF24, mouseX, mouseY);


		curY += uCardH + 24f;

		// Section: Employees
		if (!cf.employees.isEmpty()) {
			RenderHelper.alignedIcon("\uE91D", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFFFBBF24, 18f, 16f);
			SkijaRenderer.text(L10n.translate("pv.cf.employees") + " (" + cf.employees.size() + ")", startX + 26f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 24f;

			float eColW = (width - 16f) / 2f;
			float eH = 56f;
			int idx = 0;

			for (CfData.RabbitEmployee emp : cf.employees) {
				float ex = startX + (idx % 2) * (eColW + 16f);
				float ey = curY + (idx / 2) * (eH + 10f);

				boolean hov = mouseX >= ex && mouseX <= ex + eColW && mouseY >= ey && mouseY <= ey + eH;
				RenderHelper.drawModernCard(ex, ey, eColW, eH, 10f, hov);

				float esS = 36f;
				float esX = ex + 10f;
				float esY = ey + 10f;
				boolean hovS = mouseX >= esX && mouseX <= esX + esS && mouseY >= esY && mouseY <= esY + esS;
				RenderHelper.drawItemSlotBg(esX, esY, esS, hovS, 0x33FFFFFF, 0x5511131E, 6f);

				ItemStack empStack = createEmployeeStack(emp);
				RenderHelper.registerItemSlot(esX, esY, esS, empStack);

				String empName = CfData.employeeName(emp.id);
				SkijaRenderer.text(empName, esX + esS + 12f, ey + 18f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14.5f);
				String lvlStr = emp.level > 0 ? "Lv. " + emp.level : "Not hired";
				float lw = SkijaRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 14f);
				SkijaRenderer.text(lvlStr, ex + eColW - 14f - lw, ey + 18f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFFAA00, 14f);

				idx++;
			}
			curY += ((idx + 1) / 2) * (eH + 10f) + 20f;
		}

		RenderHelper.drawModernCard(startX, curY, width, 104f, 10f, false);
		RenderHelper.drawStatRow("Time Tower Charges", String.valueOf(cf.timeTowerCharges), startX + 16f, curY + 16f, width - 32f, 14f, 0xFF34D399);
		RenderHelper.drawStatRow("Hitman Slots / Uncollected Eggs", cf.hitmanSlots + " / " + cf.uncollectedEggs, startX + 16f, curY + 44f, width - 32f, 14f, 0xFFFBBF24);
		RenderHelper.drawStatRow("Faction", cf.faction.isEmpty() ? "None" : cf.faction + " · Lv. " + cf.factionLevel, startX + 16f, curY + 72f, width - 32f, 14f, 0xFF38BDF8);
		curY += 120f;
		if (!cf.rabbits.isEmpty()) {
			SkijaRenderer.text("Rabbit Collection", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
			curY += 28f;
			float rabbitW = (width - 24f) / 3f;
			int index = 0;
			for (var rabbit : cf.rabbits.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
				float x = startX + index % 3 * (rabbitW + 12f);
				float y = curY + index / 3 * 64f;
				RenderHelper.drawModernCard(x, y, rabbitW, 52f, 8f, false);
				SkijaRenderer.text(formatRabbitName(rabbit.getKey()), x + 12f, y + 8f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13f);
				SkijaRenderer.text(CfData.rabbitRarity(rabbit.getKey()) + " · " + rabbit.getValue(), x + 12f, y + 29f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12f);
				index++;
			}
			curY += ((index + 2) / 3) * 64f;
		}
		return curY - startY;
	}

	private static void renderUpgradeCard(float x, float y, float w, float h, float slotS, ItemStack stack, String name, String levelStr, int lvlCol, float mx, float my) {
		boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;
		RenderHelper.drawModernCard(x, y, w, h, 10f, hov);

		float sx = x + 10f;
		float sy = y + (h - slotS) / 2f;
		boolean hovS = mx >= sx && mx <= sx + slotS && my >= sy && my <= sy + slotS;
		RenderHelper.drawItemSlotBg(sx, sy, slotS, hovS, 0x33FFFFFF, 0x5511131E, 6f);
		RenderHelper.registerItemSlot(sx, sy, slotS, stack);

		float tx = sx + slotS + 10f;
		SkijaRenderer.text(name, tx, y + 14f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13.5f);
		SkijaRenderer.text(levelStr, tx, y + 38f, Fonts.PRETENDARD_SEMIBOLD, lvlCol, 15f);
	}

	private static ItemStack createCookieUpgradeStack(int level) {
		ItemStack stack = new ItemStack(Items.COOKIE);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("§6Click Upgrade " + level));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal("§7Increases chocolate per click."));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}

	private static ItemStack createClockUpgradeStack(CfData cf) {
		ItemStack stack = new ItemStack(Items.CLOCK);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("§aTime Tower"));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal("§7Level: §a" + cf.timeTowerLevel));
		lore.add(Component.literal("§7Charges: §a" + cf.timeTowerCharges));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}

	private static ItemStack createShrineUpgradeStack(int level) {
		ItemStack stack = new ItemStack(Items.RABBIT_FOOT);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("§dRabbit Shrine " + level));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal("§7Increases chance of rare rabbits in Hoppity's Hunt."));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}

	private static ItemStack createJackrabbitUpgradeStack(int level) {
		ItemStack stack = NbtItemParser.createSkull(ProfileJson.string(SKULL_TEXTURES, "coach_jackrabbit"), 1);
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("§eCoach Jackrabbit " + level));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal("§7Increases chocolate per second."));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}

	private static ItemStack createEmployeeStack(CfData.RabbitEmployee emp) {
		ItemStack stack = emp.level > 0 ? NbtItemParser.createSkull(ProfileJson.string(EMPLOYEE_TEXTURES, emp.id), 1) : new ItemStack(Items.DYE.gray());
		stack.set(DataComponents.CUSTOM_NAME, Component.literal("§a" + CfData.employeeName(emp.id) + " (Lv. " + emp.level + ")"));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.literal("§7Employee Level: §e" + emp.level));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		return stack;
	}


	private static String formatRabbitName(String id) {
		String cleaned = id.replace("rabbit_", "").replace("_", " ");
		String[] words = cleaned.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			if (!w.isEmpty()) {
				sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
			}
		}
		return sb.toString().trim();
	}
}