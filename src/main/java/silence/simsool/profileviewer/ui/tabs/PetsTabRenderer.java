package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.lucent.ui.widget.components.TextBox;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.PetData;
import silence.simsool.profileviewer.ui.RenderHelper;

public class PetsTabRenderer {

	public static class PetSlotInfo {
		public float x, y, size;
		public PetData.PetItem pet;
		public boolean isDetailSlot = false;

		public PetSlotInfo(float x, float y, float size, PetData.PetItem pet) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.pet = pet;
		}

		public PetSlotInfo(float x, float y, float size, PetData.PetItem pet, boolean isDetailSlot) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.pet = pet;
			this.isDetailSlot = isDetailSlot;
		}
	}

	public static final List<PetSlotInfo> visiblePetSlots = new ArrayList<>();
	public static TextBox searchBox = new TextBox(0, 0, 180, 26, "");
	public static PetData.PetItem selectedPet = null;
	public static PetData.PetItem hoveredPet = null;

	private static final List<String> RARITY_ORDER = List.of("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE");

	public static float render(MemberData data, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		float curY = startY;
		visiblePetSlots.clear();
		hoveredPet = null;

		if (data == null || data.pets == null) return 50f;

		List<PetData.PetItem> allPets = data.pets.pets;

		// Calculate Pet Score & Magic Find Bonus
		int petScore = 0;
		for (PetData.PetItem p : allPets) {
			int rarityScore = switch (p.rarity.toUpperCase()) {
				case "COMMON" -> 1;
				case "UNCOMMON" -> 2;
				case "RARE" -> 3;
				case "EPIC" -> 4;
				case "LEGENDARY" -> 5;
				case "MYTHIC", "DIVINE" -> 6;
				default -> 1;
			};
			petScore += rarityScore;
		}
		int magicFindBonus = petScore / 10;

		// Default selected pet
		if (selectedPet == null && !allPets.isEmpty()) {
			selectedPet = data.pets.activePet != null ? data.pets.activePet : allPets.get(0);
		}

		// Top Row Header: Title, Pet Score, Search Box
		String titleStr = L10n.translate("pv.pets.collection") + " (" + allPets.size() + ")";
		NVGRenderer.text(titleStr, startX + 4, curY + 4, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_BUTTON);

		float titleW = NVGRenderer.textWidth(titleStr, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_BUTTON);
		String scoreStr = L10n.translate("pv.pets.score") + ": §b" + petScore + " §7(§b+" + magicFindBonus + " " + L10n.translate("pv.pets.magic_find") + "§7)";
		RenderHelper.drawColoredText(scoreStr, startX + titleW + 16f, curY + 5, RenderHelper.FS_CAPTION, RenderHelper.FONT_SECONDARY);

		float searchW = 180f;
		float searchX = startX + width - searchW;
		searchBox.setPosition((int) searchX, (int) curY);
		searchBox.render(null, (int) mouseX, (int) mouseY, delta);

		curY += 36;

		// Sort Pets: Rarity desc -> Level desc -> Exp desc
		List<PetData.PetItem> sortedPets = new ArrayList<>(allPets);
		sortedPets.sort((a, b) -> {
			int rA = RARITY_ORDER.indexOf(a.rarity.toUpperCase());
			int rB = RARITY_ORDER.indexOf(b.rarity.toUpperCase());
			if (rA != rB) return Integer.compare(rB, rA);
			if (a.level != b.level) return Integer.compare(b.level, a.level);
			return Double.compare(b.exp, a.exp);
		});

		// Filter
		String q = searchBox.getValue().trim().toLowerCase();
		List<PetData.PetItem> filtered = new ArrayList<>();
		for (PetData.PetItem p : sortedPets) {
			if (q.isEmpty() || p.type.toLowerCase().contains(q) || p.rarity.toLowerCase().contains(q) || String.valueOf(p.level).contains(q) || p.heldItem.toLowerCase().contains(q)) {
				filtered.add(p);
			}
		}

		// Two-column Layout: Left 62% Grid, Right 38% Details Panel
		float gap = 14f;
		float leftW = (width - gap) * 0.62f;
		float rightW = width - gap - leftW;
		float leftX = startX;
		float rightX = startX + leftW + gap;

		// Left Column: Pet Grid
		float slotSize = 38f;
		float slotGap = 6f;
		int cols = Math.max(1, (int) ((leftW - 24f + slotGap) / (slotSize + slotGap)));
		int rows = (int) Math.ceil((double) filtered.size() / cols);
		float gridH = Math.max(340f, rows * (slotSize + slotGap) + 20f);

		RenderHelper.drawModernCard(leftX, curY, leftW, gridH, 10f, false);

		if (filtered.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.pets.no_pets"), leftX + leftW / 2f - 60f, curY + 40f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
		} else {
			for (int i = 0; i < filtered.size(); i++) {
				PetData.PetItem pet = filtered.get(i);
				int col = i % cols;
				int row = i / cols;
				float sx = leftX + 12f + col * (slotSize + slotGap);
				float sy = curY + 12f + row * (slotSize + slotGap);

				boolean isSelected = (pet == selectedPet);
				boolean hov = mouseX >= sx && mouseX <= sx + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
				if (hov) hoveredPet = pet;

				// Slot Background & Outline
				int bgCol = isSelected ? 0xFF2A364F : (hov ? 0xFF282A38 : 0x551B1C26);
				int borderCol = isSelected ? UIColors.ACCENT_BLUE : (hov ? pet.getRarityColor() : UIColors.withAlpha(pet.getRarityColor(), 140));
				NVGRenderer.rect(sx, sy, slotSize, slotSize, bgCol, 6f);
				NVGRenderer.outlineRect(sx, sy, slotSize, slotSize, isSelected ? 1.8f : 1.0f, borderCol, 6f);

				// Active Badge (Small green dot)
				if (pet.active) {
					NVGRenderer.rect(sx + 3f, sy + 3f, 6f, 6f, 0xFF55FF55, 3f);
				}

				// Level Badge at bottom-right of slot
				String lvlStr = String.valueOf(pet.level);
				float lvlW = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 10f);
				NVGRenderer.rect(sx + slotSize - lvlW - 5f, sy + slotSize - 13f, lvlW + 4f, 11f, 0xCC111218, 3f);
				NVGRenderer.text(lvlStr, sx + slotSize - lvlW - 3f, sy + slotSize - 12f, Fonts.PRETENDARD_SEMIBOLD, pet.getRarityColor(), 10f);

				visiblePetSlots.add(new PetSlotInfo(sx, sy, slotSize, pet));
			}
		}

		// Right Column: Selected Pet Details Card
		renderSelectedPetDetails(rightX, curY, rightW, gridH);

		curY += Math.max(gridH, 300f) + 16f;
		return curY - startY;
	}

	private static void renderSelectedPetDetails(float x, float y, float w, float h) {
		RenderHelper.drawModernCard(x, y, w, h, 10f, false);

		if (selectedPet == null) {
			NVGRenderer.text(L10n.translate("pv.pets.no_pets"), x + w / 2f - 55f, y + 40f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_SECONDARY, RenderHelper.FS_BUTTON);
			return;
		}

		PetData.PetItem pet = selectedPet;
		float px = x + 14f;
		float py = y + 14f;

		// Large Pet Icon Slot (52x52)
		float bigSlotSize = 52f;
		NVGRenderer.rect(px, py, bigSlotSize, bigSlotSize, 0xFF222433, 8f);
		NVGRenderer.outlineRect(px, py, bigSlotSize, bigSlotSize, 1.5f, pet.getRarityColor(), 8f);
		visiblePetSlots.add(new PetSlotInfo(px, py, bigSlotSize, pet, true));

		// Name & Rarity & Level
		float nameX = px + bigSlotSize + 12f;
		String cleanName = pet.type.replace("_", " ");
		NVGRenderer.text(cleanName, nameX, py + 2f, Fonts.PRETENDARD_SEMIBOLD, pet.getRarityColor(), RenderHelper.FS_BODY);

		String tag = pet.rarity + " PET";
		RenderHelper.drawBadge(tag, nameX, py + 22f, 0x33000000, pet.getRarityColor());

		if (pet.active) {
			RenderHelper.drawBadge(L10n.translate("pv.gear.active"), nameX + NVGRenderer.textWidth(tag, Fonts.PRETENDARD_SEMIBOLD, 10f) + 18f, py + 22f, 0xFF1B3D24, 0xFF55FF55);
		}

		py += bigSlotSize + 16f;

		// Level & XP Progress
		String lvlText = L10n.translate("pv.ui.level") + " " + pet.level + " / " + pet.maxLevel;
		NVGRenderer.text(lvlText, px, py, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_CAPTION);

		String pctText = pet.level >= pet.maxLevel ? L10n.translate("pv.ui.max") : String.format("%.1f%%", pet.progressToNextLevel * 100);
		NVGRenderer.text(pctText, px + w - 28f - NVGRenderer.textWidth(pctText, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_CAPTION), py, Fonts.PRETENDARD_SEMIBOLD, pet.level >= pet.maxLevel ? 0xFF55FFFF : UIColors.ACCENT_BLUE, RenderHelper.FS_CAPTION);

		py += 16f;
		RenderHelper.drawProgressBar(px, py, w - 28f, 7f, pet.progressToNextLevel, UIColors.ACCENT_BLUE);
		py += 14f;

		// Total XP Info
		RenderHelper.drawStatRow(L10n.translate("pv.pets.exp"), String.format("%,.0f XP", pet.exp), px, py, w - 28f, RenderHelper.FS_CAPTION, 0xFFFFDD55);
		py += 22f;

		if (pet.level < pet.maxLevel) {
			RenderHelper.drawStatRow("Next Level", String.format("%,.0f / %,.0f", pet.currentLevelExp, pet.nextLevelExp), px, py, w - 28f, RenderHelper.FS_CAPTION, RenderHelper.FONT_PRIMARY);
			py += 22f;
		}

		// Candy Used
		String candyVal = pet.candyUsed > 0 ? (pet.candyUsed + " / 10") : "0 / 10";
		RenderHelper.drawStatRow("Candy", candyVal, px, py, w - 28f, RenderHelper.FS_CAPTION, pet.candyUsed > 0 ? 0xFFFF77DD : RenderHelper.FONT_SECONDARY);
		py += 22f;

		// Held Item
		String heldVal = pet.heldItem.isEmpty() ? L10n.translate("pv.ui.none") : pet.heldItem.replace("PET_ITEM_", "").replace("_", " ");
		RenderHelper.drawStatRow(L10n.translate("pv.pets.held_item"), heldVal, px, py, w - 28f, RenderHelper.FS_CAPTION, !pet.heldItem.isEmpty() ? 0xFF55FF55 : RenderHelper.FONT_SECONDARY);
		py += 22f;

		// Skin
		if (!pet.skin.isEmpty()) {
			String skinVal = pet.skin.replace("PET_SKIN_", "").replace("_", " ");
			RenderHelper.drawStatRow(L10n.translate("pv.pets.skin"), skinVal, px, py, w - 28f, RenderHelper.FS_CAPTION, 0xFFFFAA00);
			py += 22f;
		}
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		if (searchBox.mouseClicked(mx, my, 0)) {
			return true;
		}

		for (PetSlotInfo slot : visiblePetSlots) {
			if (!slot.isDetailSlot && mx >= slot.x && mx <= slot.x + slot.size && my >= slot.y && my <= slot.y + slot.size) {
				selectedPet = slot.pet;
				return true;
			}
		}

		return false;
	}

	public static boolean charTyped(char chr, int modifiers) {
		return searchBox.charTyped(chr, modifiers);
	}

	public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return searchBox.keyPressed(keyCode, scanCode, modifiers);
	}
}