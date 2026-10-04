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
	private static FishingData cachedFishing;
	private static long cachedRepoRevision = -1;
	private static List<List<ItemStack>> trophyStacks = List.of();

	private static List<List<ItemStack>> createTrophyStacks(FishingData data) {
		List<List<ItemStack>> result = new ArrayList<>();
		String[] tiers = {"BRONZE", "SILVER", "GOLD", "DIAMOND"};
		for (String type : FishingData.TROPHY_FISH_TYPES) {
			int[] counts = data.trophyFishCounts.getOrDefault(type, new int[4]);
			List<ItemStack> row = new ArrayList<>();
			for (int tier = 0; tier < 5; tier++) {
				int count = tier < 4 ? counts[tier] : data.trophyFishTotals.getOrDefault(type, 0);
				int iconTier = tier;
				if (tier == 4) {
					iconTier = 0;
					for (int candidate = 0; candidate < 4; candidate++) if (counts[candidate] > 0) iconTier = candidate;
				}
				ItemStack stack = count > 0 ? ItemRepo.getItemStack(FishingData.trophyId(type) + "_" + tiers[iconTier]) : new ItemStack(Items.DYE.gray());
				if (stack.isEmpty()) stack = new ItemStack(Items.PAPER);
				stack.set(DataComponents.CUSTOM_NAME, Component.literal("§e" + type + " " + (tier < 4 ? tiers[tier] : "TOTAL")));
				List<Component> lore = new ArrayList<>();
				for (int t = 0; t < 4; t++) lore.add(Component.literal("§7" + tiers[t] + ": §f" + counts[t]));
				lore.add(Component.literal("§aTotal: §f" + data.trophyFishTotals.getOrDefault(type, 0)));
				stack.set(DataComponents.LORE, new ItemLore(lore));
				row.add(stack);
			}
			result.add(row);
		}
		return result;
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

		RenderHelper.drawStatRow("Trophy Fish", RenderHelper.formatNumber(f.totalCatches), c1X + 12f, infoY, infoW - 24f, 13f, 0xFFFBBF24);
		infoY += rowH;
		RenderHelper.drawStatRow("Trophy Rank", new String[]{"None", "Bronze", "Silver", "Gold", "Diamond"}[f.trophyRank], c1X + 12f, infoY, infoW - 24f, 13f, RenderHelper.FONT_MUTED);
		infoY += rowH;
		RenderHelper.drawStatRow("Drake Piper", String.valueOf(f.drakePiper), c1X + 12f, infoY, infoW - 24f, 13f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Midas Lure", String.valueOf(f.midasLure), c1X + 12f, infoY, infoW - 24f, 13f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Radiant Fisher", String.valueOf(f.radiantFisher), c1X + 12f, infoY, infoW - 24f, 13f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Dolphin Pet", f.dolphinRarity(), c1X + 12f, infoY, infoW - 24f, 13f, 0xFF55FF55);

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
		RenderHelper.drawStatRow("Total Catches", RenderHelper.formatNumber(f.itemsFishedTotal), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		RenderHelper.drawStatRow("Normal Catches", RenderHelper.formatNumber(f.normalCatches), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		RenderHelper.drawStatRow("Treasures / Large", RenderHelper.formatNumber(f.treasuresFished) + " / " + RenderHelper.formatNumber(f.largeTreasuresCaught), c2X + 12f, statsY, statsW - 24f, 13f, RenderHelper.FONT_PRIMARY);
		statsY += rowH;
		int trophyTotal = f.totalCatches;
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

		if (cachedFishing != f || cachedRepoRevision != ItemRepo.getRevision()) {
			cachedFishing = f;
			cachedRepoRevision = ItemRepo.getRevision();
			trophyStacks = createTrophyStacks(f);
		}
		SkijaRenderer.text("Trophy Fish", startX + 4f, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 16f);
		curY += 28f;
		int columns = Math.max(1, Math.min(3, (int) (width / 260f)));
		float cardW = (width - (columns - 1) * 12f) / columns;
		float cardH = 116f;
		String[] tiers = {"Bronze", "Silver", "Gold", "Diamond", "Total"};
		int[] colors = {0xFFCD7F32, 0xFFD1D5DB, 0xFFFBBF24, 0xFF67E8F9, 0xFF9CA3AF};
		for (int index = 0; index < FishingData.TROPHY_FISH_TYPES.length; index++) {
			String type = FishingData.TROPHY_FISH_TYPES[index];
			float x = startX + index % columns * (cardW + 12f);
			float y = curY + index / columns * (cardH + 12f);
			RenderHelper.drawModernCard(x, y, cardW, cardH, 10f, false);
			SkijaRenderer.text(type, x + 12f, y + 10f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);
			int[] counts = f.trophyFishCounts.getOrDefault(type, new int[4]);
			float cellW = (cardW - 20f) / 5f;
			for (int tier = 0; tier < 5; tier++) {
				float sx = x + 10f + tier * cellW + (cellW - 28f) / 2f;
				float sy = y + 46f;
				int count = tier < 4 ? counts[tier] : f.trophyFishTotals.getOrDefault(type, 0);
				float labelW = SkijaRenderer.textWidth(tiers[tier], Fonts.PRETENDARD_MEDIUM, 10f);
				SkijaRenderer.text(tiers[tier], sx + (28f - labelW) / 2f, y + 30f, Fonts.PRETENDARD_MEDIUM, colors[tier], 10f);
				boolean hovered = mouseX >= sx && mouseX <= sx + 28f && mouseY >= sy && mouseY <= sy + 28f;
				RenderHelper.drawItemSlotBg(sx, sy, 28f, hovered, count > 0 ? 0x44FFFFFF : 0x224B5563, 0x5514151E, 4f);
				RenderHelper.registerItemSlot(sx, sy, 28f, trophyStacks.get(index).get(tier));
				String amount = RenderHelper.formatNumber(count);
				float amountW = SkijaRenderer.textWidth(amount, Fonts.PRETENDARD_MEDIUM, 14f);
				SkijaRenderer.text(amount, sx + (28f - amountW) / 2f, y + 84f, Fonts.PRETENDARD_MEDIUM, count > 0 ? colors[tier] : RenderHelper.FONT_DISABLED, 14f);
			}
		}
		curY += ((FishingData.TROPHY_FISH_TYPES.length + columns - 1) / columns) * (cardH + 12f);
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
