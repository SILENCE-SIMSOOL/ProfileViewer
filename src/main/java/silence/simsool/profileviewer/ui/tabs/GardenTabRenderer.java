package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import silence.simsool.profileviewer.api.data.ProfileJson;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.data.GardenData;
import silence.simsool.profileviewer.api.data.GearFinder;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.PetData;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.RenderHelper;

public class GardenTabRenderer {

	public enum GardenSubTab {
		OVERVIEW("pv.garden.subtab.overview", "Overview", "\uEA40"),
		CROPS("pv.garden.subtab.crops", "Crops", "\uE56C"),
		COMPOSTER("pv.garden.subtab.composter", "Composter", "\uE263"),
		VISITORS("pv.garden.subtab.visitors", "Visitors", "\uE7FD");

		public final String translationKey;
		public final String defaultName;
		public final String icon;

		GardenSubTab(String translationKey, String defaultName, String icon) {
			this.translationKey = translationKey;
			this.defaultName = defaultName;
			this.icon = icon;
		}

		public String getTitle() {
			String trans = L10n.translate(translationKey);
			return (trans != null && !trans.equals(translationKey)) ? trans : defaultName;
		}
	}

	public static GardenSubTab activeSubTab = GardenSubTab.OVERVIEW;

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		RenderHelper.clearGlobalSlots();
		float curY = startY;

		// 1. Sub-tab Navigation Bar
		curY += renderSubTabs(startX, curY, width, mouseX, mouseY);

		// 2. Render Active Sub-tab View
		switch (activeSubTab) {
			case OVERVIEW -> curY += renderMainOverview(data, startX, curY, width, mouseX, mouseY);
			case CROPS -> curY += renderCropsView(data, startX, curY, width, mouseX, mouseY);
			case COMPOSTER -> curY += renderComposterView(data, startX, curY, width, mouseX, mouseY);
			case VISITORS -> curY += renderVisitorsView(data, startX, curY, width, mouseX, mouseY);
		}

		return curY - startY;
	}

	private static float renderSubTabs(float startX, float curY, float width, float mx, float my) {
		float subTabH = 30f;
		float subTabX = startX;

		for (GardenSubTab st : GardenSubTab.values()) {
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
	// 1. MAIN OVERVIEW (Gear, Contests, Chips, Information)
	// =========================================================================
	private static float renderMainOverview(MemberData data, float startX, float curY, float width, float mx, float my) {
		float y0 = curY;
		GardenData g = (data != null && data.garden != null) ? data.garden : new GardenData();

		float colGap = 12f;
		float totalW = width;

		// 4 Cards Layout: Gear (220f), Contests (120f), Chips (115f), Information (Remaining)
		float gearW = 210f;
		float contestsW = 152f;
		float chipsW = 115f;
		float infoW = totalW - gearW - contestsW - chipsW - 3 * colGap;
		float cardH = 270f;

		// ---------------------------------------------------------------------
		// Card 1: Gear (4 Armor + 4 Equipment + 4 Pets + Vacuum/Watering Can)
		// ---------------------------------------------------------------------
		float c1X = startX;
		RenderHelper.drawModernCard(c1X, curY, gearW, cardH, 10f, false);
		SkijaRenderer.text("Gear", c1X + (gearW - SkijaRenderer.textWidth("Gear", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		float slotSize = 40f;
		float slotGap = 4f;
		float gTopY = curY + 36f;
		float col1X = c1X + 10f;
		float col2X = col1X + slotSize + slotGap;
		float col3X = col2X + slotSize + slotGap;
		float col4X = col3X + slotSize + slotGap + 6f;

		// 1. Farming Armor (Helmet down to Boots)
		List<ItemStack> farmingArmor = List.of(GearFinder.findFarmingItem(data, GearFinder.FARMING_HELMETS), GearFinder.findFarmingItem(data, GearFinder.FARMING_CHESTPLATES), GearFinder.findFarmingItem(data, GearFinder.FARMING_LEGGINGS), GearFinder.findFarmingItem(data, GearFinder.FARMING_BOOTS));
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= col1X && mx <= col1X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(col1X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = farmingArmor.get(r);
			if (!st.isEmpty()) RenderHelper.registerItemSlot(col1X, sy, slotSize, st);
		}

		// 2. Farming Equipment (Necklace, Cloak, Belt, Gloves)
		List<ItemStack> farmingEq = GearFinder.findEquipmentSet(data, GearFinder.FARMING_EQUIPMENT, true);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= col2X && mx <= col2X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(col2X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);
			ItemStack st = farmingEq.get(r);
			if (!st.isEmpty()) RenderHelper.registerItemSlot(col2X, sy, slotSize, st);
		}

		// 3. Farming Pets (Elephant, Mooshroom Cow, Rabbit, Bee, etc.)
		List<PetData.PetItem> farmingPets = getFarmingPets(data);
		for (int r = 0; r < 4; r++) {
			float sy = gTopY + r * (slotSize + slotGap);
			boolean hov = mx >= col3X && mx <= col3X + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(col3X, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);

			if (r < farmingPets.size()) {
				PetData.PetItem p = farmingPets.get(r);
				RenderHelper.registerItemSlot(col3X, sy, slotSize, p.itemStack, String.valueOf(p.level), p.getRarityColor());
			}
		}

		// 4. Right Tools: Vacuum & Watering Can
		float vacY = gTopY + slotSize + slotGap;
		float compY = vacY + slotSize + slotGap;

		boolean hovVac = mx >= col4X && mx <= col4X + slotSize && my >= vacY && my <= vacY + slotSize;
		RenderHelper.drawItemSlotBg(col4X, vacY, slotSize, hovVac, 0x33FFFFFF, 0x5514151E, 4f);
		ItemStack vacStack = getFarmingVacuumStack(data);
		if (!vacStack.isEmpty()) RenderHelper.registerItemSlot(col4X, vacY, slotSize, vacStack);

		boolean hovComp = mx >= col4X && mx <= col4X + slotSize && my >= compY && my <= compY + slotSize;
		RenderHelper.drawItemSlotBg(col4X, compY, slotSize, hovComp, 0x33FFFFFF, 0x5514151E, 4f);
		ItemStack compStack = getWateringCanStack(data);
		if (!compStack.isEmpty()) RenderHelper.registerItemSlot(col4X, compY, slotSize, compStack);

		// ---------------------------------------------------------------------
		// Card 2: Contests (10 Crops with Brackets & Medals Tooltip)
		// ---------------------------------------------------------------------
		float c2X = c1X + gearW + colGap;
		RenderHelper.drawModernCard(c2X, curY, contestsW, cardH, 10f, false);
		SkijaRenderer.text("Contests", c2X + (contestsW - SkijaRenderer.textWidth("Contests", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		String[] cropIds = {"wheat", "carrot", "potato", "pumpkin", "melon", "sugar_cane", "cactus", "cocoa_beans", "mushroom", "nether_wart"};
		ItemStack[] cropStacks = {
			new ItemStack(Items.WHEAT), new ItemStack(Items.CARROT), new ItemStack(Items.POTATO),
			new ItemStack(Items.CARVED_PUMPKIN), new ItemStack(Items.MELON_SLICE), new ItemStack(Items.SUGAR_CANE),
			new ItemStack(Items.CACTUS), new ItemStack(Items.COCOA_BEANS), new ItemStack(Items.RED_MUSHROOM), new ItemStack(Items.NETHER_WART)
		};

		int cCols = 3;
		float cPadX = c2X + (contestsW - (3 * slotSize + 2 * slotGap)) / 2f;
		for (int i = 0; i < 10; i++) {
			int cr = i / cCols;
			int cc = i % cCols;
			float sx = cPadX + cc * (slotSize + slotGap);
			float sy = gTopY + cr * (slotSize + slotGap);

			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);

			ItemStack cSt = cropStacks[i];
			long pb = g.personalBests.getOrDefault(cropIds[i], 0L);
			cSt.set(DataComponents.CUSTOM_NAME, Component.literal("§a" + formatCropName(cropIds[i])));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.literal("§7Personal Best: §e" + RenderHelper.formatNumber(pb)));

			cSt.set(DataComponents.LORE, new ItemLore(lore));

			RenderHelper.registerItemSlot(sx, sy, slotSize, cSt);
		}

		// ---------------------------------------------------------------------
		// Card 3: Chips (10 Head Icons with Levels & Sowdust Tooltip)
		// ---------------------------------------------------------------------
		float c3X = c2X + contestsW + colGap;
		RenderHelper.drawModernCard(c3X, curY, chipsW, cardH, 10f, false);
		SkijaRenderer.text("Chips", c3X + (chipsW - SkijaRenderer.textWidth("Chips", Fonts.PRETENDARD_SEMIBOLD, 14f)) / 2f, curY + 12f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		float chPadX = c3X + (chipsW - (2 * slotSize + slotGap)) / 2f;
		String[] chipSbIds = {
			"CROPSHOT_CHIP", "EVERGREEN_CHIP", "HYPERCHARGE_CHIP", "MECHAMIND_CHIP", "OVERDRIVE_CHIP",
			"QUICKDRAW_CHIP", "RAREFINDER_CHIP", "SOWLEDGE_CHIP", "SYNTHESIS_CHIP", "VERMIN_VAPORIZER_CHIP"
		};


		for (int i = 0; i < 10; i++) {
			int chr = i / 2;
			int chc = i % 2;
			float sx = chPadX + chc * (slotSize + slotGap);
			float sy = gTopY + chr * (slotSize + slotGap);

			boolean hov = mx >= sx && mx <= sx + slotSize && my >= sy && my <= sy + slotSize;
			RenderHelper.drawItemSlotBg(sx, sy, slotSize, hov, 0x33FFFFFF, 0x5514151E, 4f);

			ItemStack chipStack = ItemRepo.getItemStack(chipSbIds[i]);
			if (chipStack.isEmpty()) chipStack = new ItemStack(Items.COMPARATOR);

			int lvl = g.chipLevels.getOrDefault(chipSbIds[i].replace("_CHIP", "").toLowerCase(Locale.ROOT), 0);
			int badgeCol = (lvl == 0) ? 0xFFEF4444 : (lvl <= 10 ? 0xFF38BDF8 : (lvl <= 15 ? 0xFFA855F7 : 0xFFF97316));

			chipStack.set(DataComponents.CUSTOM_NAME, Component.literal("§d" + formatCropName(chipSbIds[i])));
			List<Component> lore = new ArrayList<>();
			lore.add(Component.literal("§7Chip Level: §e" + lvl));
			lore.add(Component.literal("§7Sowdust: §a" + RenderHelper.formatNumber(GardenData.chipSowdust(lvl))));
			chipStack.set(DataComponents.LORE, new ItemLore(lore));

			RenderHelper.registerItemSlot(sx, sy, slotSize, chipStack, String.valueOf(lvl), badgeCol);
		}

		// ---------------------------------------------------------------------
		// Card 4: Information
		// ---------------------------------------------------------------------
		float c4X = c3X + chipsW + colGap;
		RenderHelper.drawModernCard(c4X, curY, infoW, cardH, 10f, false);
		RenderHelper.alignedIcon("\uE88F", c4X + 14f, curY + 11f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 16f, 14f);
		SkijaRenderer.text("Information", c4X + 34f, curY + 11f, Fonts.PRETENDARD_SEMIBOLD, 0xFFE879F9, 14f);

		float infoY = curY + 36f;
		float rowH = 24f;

		RenderHelper.drawStatRow("Copper", RenderHelper.formatNumber(g.copper), c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFFEF4444);
		infoY += rowH;
		RenderHelper.drawStatRow("Garden Level", String.valueOf(g.gardenLevel), c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFF10B981);
		infoY += rowH;
		int totalContests = g.contestsParticipated;
		RenderHelper.drawStatRow("Contests", String.valueOf(totalContests), c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFFFBBF24);
		infoY += rowH;
		String medalsDisplay = g.goldMedals + "G / " + g.silverMedals + "S / " + g.bronzeMedals + "B";
		RenderHelper.drawStatRow("Medals", medalsDisplay, c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFFFBBF24);
		infoY += rowH;
		RenderHelper.drawStatRow("Larva Consumed", g.larvaConsumed + "/" + ProfileJson.number(GardenData.CATALOG, "misc", "max_larva_consumed"), c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFF10B981);
		infoY += rowH;
		RenderHelper.drawStatRow("Farming Level Cap", String.valueOf(50 + g.farmingLevelCap), c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFFEF4444);
		infoY += rowH;
		RenderHelper.drawStatRow("Double Drops", String.valueOf(g.doubleDrops), c4X + 14f, infoY, infoW - 28f, 13.5f, 0xFFEF4444);

		curY += cardH + 16f;
		return curY - y0;
	}

	private static float renderCropsView(MemberData data, float startX, float curY, float width, float mx, float my) {
		float cardW = (width - 12f) / 2f;
		int index = 0;
		for (var entry : ProfileJson.object(GardenData.CATALOG, "crop_milestones").entrySet()) {
			String id = entry.getKey();
			long amount = data.garden.cropMilestones.getOrDefault(GardenData.cropKey(id), 0L);
			int level = GardenData.milestoneLevel(id, amount);
			long previous = 0;
			long next = 0;
			for (int i = 0; i <= level && i < entry.getValue().getAsJsonArray().size(); i++) {
				previous = next;
				next += entry.getValue().getAsJsonArray().get(i).getAsLong();
			}
			boolean maxed = level == entry.getValue().getAsJsonArray().size();
			float x = startX + index % 2 * (cardW + 12f);
			float y = curY + index / 2 * 100f;
			RenderHelper.drawModernCard(x, y, cardW, 88f, 10f, false);
			RenderHelper.drawStatRow(formatCropName(GardenData.cropKey(id)), "Milestone " + level, x + 14f, y + 12f, cardW - 28f, 14f, 0xFF34D399);
			String progress = maxed ? "MAX · " + RenderHelper.formatNumber(amount) : RenderHelper.formatNumber(amount) + " / " + RenderHelper.formatNumber(next);
			SkijaRenderer.text(progress, x + 14f, y + 36f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 13f);
			RenderHelper.drawProgressBar(x + 14f, y + 65f, cardW - 28f, 5f, maxed ? 1f : (float) (amount - previous) / Math.max(1, next - previous), 0xFF10B981);
			index++;
		}
		return ((index + 1) / 2) * 100f;
	}

	private static float renderComposterView(MemberData data, float startX, float curY, float width, float mx, float my) {
		GardenData g = data.garden;
		RenderHelper.drawModernCard(startX, curY, width, 122f, 10f, false);
		RenderHelper.drawStatRow("Organic Matter", RenderHelper.formatNumber((long) g.composterOrganicMatter), startX + 16f, curY + 16f, width - 32f, 14f, 0xFF34D399);
		RenderHelper.drawStatRow("Fuel", RenderHelper.formatNumber((long) g.composterFuel), startX + 16f, curY + 48f, width - 32f, 14f, 0xFFFBBF24);
		RenderHelper.drawStatRow("Compost Ready", RenderHelper.formatNumber(g.composterItems), startX + 16f, curY + 80f, width - 32f, 14f, 0xFF38BDF8);
		String[] ids = {"SPEED", "MULTI_DROP", "FUEL_CAP", "ORGANIC_MATTER_CAP", "COST_REDUCTION"};
		int[] levels = {g.composterSpeedUpgrade, g.composterMultiDropUpgrade, g.composterFuelCapUpgrade, g.composterOrganicCapUpgrade, g.composterCostReductionUpgrade};
		for (int i = 0; i < ids.length; i++) {
			JsonObject definition = ProfileJson.object(GardenData.CATALOG, "composter_data", ids[i]);
			int maximum = ProfileJson.array(definition, "upgrades").size();
			float y = curY + 138f + i * 68f;
			RenderHelper.drawModernCard(startX, y, width, 56f, 8f, false);
			RenderHelper.drawStatRow(ProfileJson.string(definition, "name"), levels[i] + " / " + maximum, startX + 16f, y + 12f, width - 32f, 14f, 0xFF34D399);
			RenderHelper.drawProgressBar(startX + 16f, y + 39f, width - 32f, 4f, (float) levels[i] / Math.max(1, maximum), 0xFF10B981);
		}
		return 138f + ids.length * 68f;
	}

	private static float renderVisitorsView(MemberData data, float startX, float curY, float width, float mx, float my) {
		GardenData g = data.garden;
		RenderHelper.drawModernCard(startX, curY, width, 60f, 10f, false);
		RenderHelper.drawStatRow("Visitors Served", g.uniqueVisitors + " unique / " + g.completedVisitors + " offers accepted", startX + 16f, curY + 20f, width - 32f, 14f, 0xFF34D399);
		Map<String, String> visitors = new LinkedHashMap<>();
		for (var element : ProfileJson.array(GardenData.CATALOG, "visitors")) {
			JsonObject visitor = element.getAsJsonObject();
			visitors.put(ProfileJson.string(visitor, "id"), ProfileJson.string(visitor, "name"));
		}
		for (String id : g.visitorVisits.keySet()) visitors.putIfAbsent(id, formatCropName(id));
		for (String id : g.visitorCompletions.keySet()) visitors.putIfAbsent(id, formatCropName(id));
		int index = 0;
		float cardW = (width - 12f) / 2f;
		for (var visitor : visitors.entrySet()) {
			float x = startX + index % 2 * (cardW + 12f);
			float y = curY + 76f + index / 2 * 64f;
			RenderHelper.drawModernCard(x, y, cardW, 52f, 8f, false);
			SkijaRenderer.text(visitor.getValue(), x + 12f, y + 8f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 13f);
			String counts = g.visitorCompletions.getOrDefault(visitor.getKey(), 0) + " accepted / " + g.visitorVisits.getOrDefault(visitor.getKey(), 0) + " visits";
			SkijaRenderer.text(counts, x + 12f, y + 29f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, 12f);
			index++;
		}
		return 76f + ((index + 1) / 2) * 64f;
	}
	private static List<PetData.PetItem> getFarmingPets(MemberData data) {
		List<PetData.PetItem> res = new ArrayList<>();
		if (data != null && data.pets != null && data.pets.pets != null) {
			java.util.Set<String> seen = new java.util.HashSet<>();
			for (PetData.PetItem pet : data.pets.pets.stream()
				.filter(p -> GearFinder.FARMING_PETS.contains(p.type.toUpperCase()))
				.sorted(java.util.Comparator.comparingInt((PetData.PetItem p) -> petRarity(p.rarity)).reversed().thenComparing(java.util.Comparator.comparingDouble((PetData.PetItem p) -> p.exp).reversed()))
				.toList()) {
				if (seen.add(pet.type) && res.size() < 4) res.add(pet);
			}
		}
		return res;
	}

	private static int petRarity(String rarity) {
		return switch (rarity.toUpperCase()) {
			case "UNCOMMON" -> 1;
			case "RARE" -> 2;
			case "EPIC" -> 3;
			case "LEGENDARY" -> 4;
			case "MYTHIC" -> 5;
			case "DIVINE" -> 6;
			default -> 0;
		};
	}

	private static ItemStack getFarmingVacuumStack(MemberData data) {
		return GearFinder.findRarestItem(data, GearFinder.FARMING_VACUUMS);
	}

	private static ItemStack getWateringCanStack(MemberData data) {
		return GearFinder.findRarestItem(data, GearFinder.FARMING_WATERING_CANS);
	}

	private static String formatCropName(String raw) {
		String clean = raw.replace("_", " ").toLowerCase();
		return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		float subTabH = 30f;
		float subTabX = startX;

		for (GardenSubTab st : GardenSubTab.values()) {
			float stW = SkijaRenderer.textWidth(st.getTitle(), Fonts.PRETENDARD_SEMIBOLD, 14f) + 38f;
			if (mx >= subTabX && mx <= subTabX + stW && my >= startY && my <= startY + subTabH) {
				activeSubTab = st;
				return true;
			}
			subTabX += stW + 8f;
		}
		return false;
	}
}
