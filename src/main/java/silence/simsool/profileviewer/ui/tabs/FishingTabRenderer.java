package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.FishingData;
import silence.simsool.profileviewer.api.data.GearFinder;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.RenderHelper;

public class FishingTabRenderer {
	private static String getHighestTrophyTier(FishingData data) {
		if (data.diamondTrophy > 0) return "Diamond";
		if (data.goldTrophy > 0) return "Gold";
		if (data.silverTrophy > 0) return "Silver";
		if (data.bronzeTrophy > 0) return "Bronze";
		return "None";
	}

	public static class TrophySlotInfo {
		public float x, y, size;
		public ItemStack stack;
		public TrophySlotInfo(float x, float y, float size, ItemStack stack) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.stack = stack;
		}
	}

	public static final List<TrophySlotInfo> visibleTrophySlots = new ArrayList<>();

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		visibleTrophySlots.clear();
		float curY = startY;

		FishingData f = (data != null && data.fishing != null) ? data.fishing : new FishingData();

		float colGap = 12f;
		float totalW = width;

		// 3 Cards Layout: Information (28%), Stats (32%), Gear (40%)
		float infoW = (totalW - 2 * colGap) * 0.28f;
		float statsW = (totalW - 2 * colGap) * 0.32f;
		float gearW = totalW - infoW - statsW - 2 * colGap;
		float topCardH = 200f;

		// ---------------------------------------------------------------------
		// Card 1: Information
		// ---------------------------------------------------------------------
		float c1X = startX;
		RenderHelper.drawModernCard(c1X, curY, infoW, topCardH, 10f, false);
		SkijaRenderer.text("Information", c1X + (infoW - SkijaRenderer.textWidth("Information", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		float infoY = curY + 34f;
		float rowH = 22f;

		RenderHelper.drawStatRow("Trophy Fish", RenderHelper.formatNumber(f.bronzeTrophy + f.silverTrophy + f.goldTrophy + f.diamondTrophy), c1X + 12f, infoY, infoW - 24f, 13f, 0xFFFBBF24);
		infoY += rowH;
		RenderHelper.drawStatRow("Highest Tier", getHighestTrophyTier(f), c1X + 12f, infoY, infoW - 24f, 13f, RenderHelper.FONT_MUTED);
		infoY += rowH;
		RenderHelper.drawStatRow("Drake Piper", "1/1", c1X + 12f, infoY, infoW - 24f, 13f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Midas Lure", "10/10", c1X + 12f, infoY, infoW - 24f, 13f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Radiant Fisher", "10/10", c1X + 12f, infoY, infoW - 24f, 13f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Dolphin Pet", "Uncommon", c1X + 12f, infoY, infoW - 24f, 13f, 0xFF55FF55);

		// ---------------------------------------------------------------------
		// Card 2: Stats
		// ---------------------------------------------------------------------
		float c2X = c1X + infoW + colGap;
		RenderHelper.drawModernCard(c2X, curY, statsW, topCardH, 10f, false);
		SkijaRenderer.text("Stats", c2X + (statsW - SkijaRenderer.textWidth("Stats", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		float statsY = curY + 34f;
		RenderHelper.drawStatRow("Treasures caught", RenderHelper.formatNumber(f.treasuresCaught), c2X + 12f, statsY, statsW - 24f, 13f, 0xFFEF4444);
		statsY += rowH;
		RenderHelper.drawStatRow("Sea creatures killed", RenderHelper.formatNumber(f.seaCreaturesKilled), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		RenderHelper.drawStatRow("Total Catches", RenderHelper.formatNumber(f.totalCatches), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		RenderHelper.drawStatRow("Normal Catches", RenderHelper.formatNumber(f.itemsFishedTotal), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		RenderHelper.drawStatRow("Treasures Found", RenderHelper.formatNumber(f.treasuresCaught), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		int trophyTotal = f.bronzeTrophy + f.silverTrophy + f.goldTrophy + f.diamondTrophy;
		RenderHelper.drawStatRow("Trophy Fishes Caught", RenderHelper.formatNumber(trophyTotal), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);

		// ---------------------------------------------------------------------
		// Card 3: Gear (4 Armor + 4 Equipment + 4 Rods + 4 Pets)
		// ---------------------------------------------------------------------
		float c3X = c2X + statsW + colGap;
		RenderHelper.drawModernCard(c3X, curY, gearW, topCardH, 10f, false);
		SkijaRenderer.text("Gear", c3X + (gearW - SkijaRenderer.textWidth("Gear", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		float slotSize = 30f;
		float slotGap = 4f;
		float gTopY = curY + 34f;
		float gCol1X = c3X + (gearW - (4 * slotSize + 3 * slotGap)) / 2f;
		float gCol2X = gCol1X + slotSize + slotGap;
		float gCol3X = gCol2X + slotSize + slotGap;
		float gCol4X = gCol3X + slotSize + slotGap;

		// 1. Fishing Armor (Helmet down to Boots)
		List<ItemStack> fishingArmor = GearFinder.findArmorSet(data, GearFinder.FISHING_HELMETS, GearFinder.FISHING_CHESTPLATES, GearFinder.FISHING_LEGGINGS, GearFinder.FISHING_BOOTS);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mouseX >= gCol1X && mouseX <= gCol1X + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol1X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = fishingArmor.get(r);
			RenderHelper.registerItemSlot(gCol1X, sy, slotSize, st);
		}

		// 2. Fishing Equipment (Necklace, Cloak, Belt, Gloves)
		List<ItemStack> fishingEq = GearFinder.findEquipmentSet(data, GearFinder.FISHING_EQUIPMENT);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mouseX >= gCol2X && mouseX <= gCol2X + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol2X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = fishingEq.get(r);
			RenderHelper.registerItemSlot(gCol2X, sy, slotSize, st);
		}

		// 3. Fishing Rods
		List<ItemStack> fishingRods = getFishingRodStacks(data);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mouseX >= gCol3X && mouseX <= gCol3X + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol3X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = (r < fishingRods.size()) ? fishingRods.get(r) : ItemStack.EMPTY;
			RenderHelper.registerItemSlot(gCol3X, sy, slotSize, st);
		}

		// 4. Trophy Armor
		List<ItemStack> trophyArmor = GearFinder.findArmorSet(data,
			filterPart(GearFinder.FISHING_TROPHY_ARMOR, "HELMET"),
			filterPart(GearFinder.FISHING_TROPHY_ARMOR, "CHESTPLATE"),
			filterPart(GearFinder.FISHING_TROPHY_ARMOR, "LEGGINGS"),
			filterPart(GearFinder.FISHING_TROPHY_ARMOR, "BOOTS"));
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mouseX >= gCol4X && mouseX <= gCol4X + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
			RenderHelper.drawItemSlotBg(gCol4X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			RenderHelper.registerItemSlot(gCol4X, sy, slotSize, trophyArmor.get(r));
		}

		curY += topCardH + 16f;

		// ---------------------------------------------------------------------
		// Card 4: Trophy Fish (18 Columns x 5 Rows Grid with Skull Heads)
		// ---------------------------------------------------------------------
		float trophyCardH = 220f;
		RenderHelper.drawModernCard(startX, curY, totalW, trophyCardH, 10f, false);
		SkijaRenderer.text("Trophy Fish", startX + (totalW - SkijaRenderer.textWidth("Trophy Fish", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		int numFish = FishingData.TROPHY_FISH_TYPES.length; // 18
		float tfSlotSize = 28f;
		float tfSlotGap = 4f;
		float tfGridW = numFish * tfSlotSize + (numFish - 1) * tfSlotGap;
		float tfStartX = startX + (totalW - tfGridW) / 2f;
		float tfStartY = curY + 34f;

		// 5 Rows: Diamond, Gold, Silver, Bronze, None/Total
		String[] tierNames = {"DIAMOND", "GOLD", "SILVER", "BRONZE", "TOTAL"};

		for (int r = 0; r < 5; r++) {
			for (int c = 0; c < numFish; c++) {
				String fishName = FishingData.TROPHY_FISH_TYPES[c];
				float sx = tfStartX + c * (tfSlotSize + tfSlotGap);
				float sy = tfStartY + r * (tfSlotSize + tfSlotGap);

				int[] counts = f.trophyFishCounts.getOrDefault(fishName, new int[4]);
				int bronze = counts[0], silver = counts[1], gold = counts[2], diamond = counts[3];
				int total = bronze + silver + gold + diamond;

				int rowCount = (r == 0) ? diamond : ((r == 1) ? gold : ((r == 2) ? silver : ((r == 3) ? bronze : total)));
				boolean hasFish = rowCount > 0;

				boolean hov = mouseX >= sx && mouseX <= sx + tfSlotSize && mouseY >= sy && mouseY <= sy + tfSlotSize;
				RenderHelper.drawItemSlotBg(sx, sy, tfSlotSize, hov, hasFish ? 0x33FFFFFF : 0x224B5563, hasFish ? 0x5514151E : 0x33000000, 3f);

				ItemStack stack;
				if (hasFish) {
					String sbId = fishName.toUpperCase().replace(" ", "_");
					stack = ItemRepo.getItemStack(sbId);
					if (stack.isEmpty()) stack = ItemRepo.getItemStack(sbId + "_" + tierNames[r]);
					if (stack.isEmpty()) stack = new ItemStack(Items.COD);
				} else {
					stack = ItemRepo.getItemStack("GRAY_DYE");
					if (stack.isEmpty()) stack = new ItemStack(Items.GUNPOWDER);
				}


				stack.set(DataComponents.CUSTOM_NAME, Component.literal("§e" + fishName + " §6" + tierNames[r]));
				List<Component> lore = new ArrayList<>();
				lore.add(Component.literal("§7Found swimming around in the lava."));
				lore.add(Component.literal(""));
				lore.add(Component.literal("§bDiamond: §f" + diamond));
				lore.add(Component.literal("§6Gold: §f" + gold));
				lore.add(Component.literal("§7Silver: §f" + silver));
				lore.add(Component.literal("§cBronze: §f" + bronze));
				lore.add(Component.literal("§aTotal: §f" + total));
				stack.set(DataComponents.LORE, new ItemLore(lore));

				String badge = (hasFish && rowCount > 0) ? String.valueOf(rowCount) : null;
				RenderHelper.registerItemSlot(sx, sy, tfSlotSize, stack, badge, 0xFFFFFFFF);
			}
		}

		curY += trophyCardH + 16f;
		return curY - startY;
	}

	private static List<ItemStack> getFishingRodStacks(MemberData data) {
		List<ItemStack> res = new ArrayList<>();
		for (ParsedItem item : GearFinder.findBestItems(data, GearFinder.FISHING_RODS, 4)) res.add(item.itemStack);
		while (res.size() < 4) {
			res.add(ItemStack.EMPTY);
		}

		return res;
	}

	private static java.util.Set<String> filterPart(java.util.Set<String> ids, String suffix) {
		return ids.stream().filter(id -> id.endsWith(suffix)).collect(java.util.stream.Collectors.toSet());
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		return false;
	}
}
