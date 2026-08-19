package silence.simsool.profileviewer.ui.tabs;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.lucent.ui.widget.components.TextBox;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.PetData;
import silence.simsool.profileviewer.api.repo.ItemRepo;
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
	public static TextBox searchBox = new TextBox(0, 0, 160, 26, "");
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

		// Top Row Header: [Icon] Pet Collection (Score Badge) & Search Box with Icon
		NVGRenderer.text("\uE91D", startX + 4f, curY + 2f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 20f);
		String titleStr = L10n.translate("pv.pets.collection") + " (" + allPets.size() + ")";
		NVGRenderer.text(titleStr, startX + 30f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 17f);

		float titleW = NVGRenderer.textWidth(titleStr, Fonts.PRETENDARD_SEMIBOLD, 17f);
		String scoreStr = L10n.translate("pv.pets.score") + ": " + petScore + " (+" + magicFindBonus + " " + L10n.translate("pv.pets.magic_find") + ")";
		RenderHelper.drawBadge(scoreStr, startX + 38f + titleW, curY - 1f, 0x3338BDF8, 0xFF38BDF8);

		// Search Box with Search Icon (\uE8B6)
		float searchW = 170f;
		float searchX = startX + width - searchW;
		NVGRenderer.text("\uE8B6", searchX - 22f, curY + 4f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_MUTED, 18f);
		searchBox.setPosition((int) searchX, (int) curY - 2);
		searchBox.render(null, (int) mouseX, (int) mouseY, delta);

		curY += 36f;

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

		// Two-column Layout: Left 60% Grid, Right 40% Details Panel
		float gap = 14f;
		float leftW = (width - gap) * 0.60f;
		float rightW = width - gap - leftW;
		float leftX = startX;
		float rightX = startX + leftW + gap;

		// Left Column: Pet Grid
		float slotSize = 40f;
		float slotGap = 6f;
		int cols = Math.max(1, (int) ((leftW - 24f + slotGap) / (slotSize + slotGap)));
		int rows = (int) Math.ceil((double) filtered.size() / cols);
		float gridH = Math.max(380f, rows * (slotSize + slotGap) + 24f);

		RenderHelper.drawModernCard(leftX, curY, leftW, gridH, 14f, false);

		if (filtered.isEmpty()) {
			NVGRenderer.text(L10n.translate("pv.pets.no_pets"), leftX + 24f, curY + 36f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
		} else {
			for (int i = 0; i < filtered.size(); i++) {
				PetData.PetItem pet = filtered.get(i);
				int col = i % cols;
				int row = i / cols;
				float sx = leftX + 14f + col * (slotSize + slotGap);
				float sy = curY + 14f + row * (slotSize + slotGap);

				boolean isSelected = (pet == selectedPet);
				boolean hov = mouseX >= sx && mouseX <= sx + slotSize && mouseY >= sy && mouseY <= sy + slotSize;
				if (hov) hoveredPet = pet;

				// Slot Background & Outline
				int bgCol = isSelected ? 0xFF222638 : (hov ? 0xFF1C1E2C : 0x6611131E);
				int borderCol = isSelected ? 0xFF818CF8 : (hov ? pet.getRarityColor() : UIColors.withAlpha(pet.getRarityColor(), 100));
				NVGRenderer.rect(sx, sy, slotSize, slotSize, bgCol, 8f);
				NVGRenderer.outlineRect(sx, sy, slotSize, slotSize, isSelected ? 1.5f : 1.0f, borderCol, 8f);

				// Active Badge (Green dot)
				if (pet.active) {
					NVGRenderer.circle(sx + 6f, sy + 6f, 3f, 0xFF10B981);
				}

				visiblePetSlots.add(new PetSlotInfo(sx, sy, slotSize, pet));
			}
		}

		// Right Column: Selected Pet Details Card
		renderSelectedPetDetails(rightX, curY, rightW, gridH);

		curY += Math.max(gridH, 380f) + 16f;
		return curY - startY;
	}

	private static void renderSelectedPetDetails(float x, float y, float w, float h) {
		RenderHelper.drawModernCard(x, y, w, h, 14f, false);

		if (selectedPet == null) {
			NVGRenderer.text(L10n.translate("pv.pets.no_pets"), x + 24f, y + 36f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 14f);
			return;
		}

		PetData.PetItem pet = selectedPet;
		float px = x + 16f;
		float py = y + 16f;

		// Large Pet Icon Slot (52x52 box, enlarged item rendering texture)
		float bigSlotSize = 52f;
		boolean isMaxed = pet.level >= pet.maxLevel;

		if (isMaxed) {
			RenderHelper.drawRainbowBorder(px - 2f, py - 2f, bigSlotSize + 4f, bigSlotSize + 4f, 10f, 1.5f);
		} else {
			NVGRenderer.rect(px - 2f, py - 2f, bigSlotSize + 4f, bigSlotSize + 4f, 0xFF181A26, 10f);
			NVGRenderer.outlineRect(px - 2f, py - 2f, bigSlotSize + 4f, bigSlotSize + 4f, 1.2f, pet.getRarityColor(), 10f);
		}
		visiblePetSlots.add(new PetSlotInfo(px, py, bigSlotSize, pet, true));

		// Name & Rarity & Level
		float nameX = px + bigSlotSize + 14f;
		String cleanName = pet.type.replace("_", " ");
		NVGRenderer.text(cleanName, nameX, py + 2f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 17f);

		String tag = pet.rarity + " PET";
		RenderHelper.drawBadge(tag, nameX, py + 24f, UIColors.withAlpha(pet.getRarityColor(), 32), pet.getRarityColor());

		if (pet.active) {
			float tagW = NVGRenderer.textWidth(tag, Fonts.PRETENDARD_SEMIBOLD, 11f);
			RenderHelper.drawBadge(L10n.translate("pv.gear.active"), nameX + tagW + 18f, py + 24f, 0x3310B981, 0xFF10B981);
		}

		py += bigSlotSize + 20f;

		// Level & XP Progress
		String lvlText = L10n.translate("pv.ui.level") + " " + pet.level + " / " + pet.maxLevel;
		NVGRenderer.text(lvlText, px, py, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 14f);

		String pctText = isMaxed ? L10n.translate("pv.ui.max") : String.format("%.1f%%", pet.progressToNextLevel * 100f);
		float pctW = NVGRenderer.textWidth(pctText, Fonts.PRETENDARD_SEMIBOLD, 13f);
		NVGRenderer.text(pctText, px + w - 32f - pctW, py, Fonts.PRETENDARD_SEMIBOLD, isMaxed ? 0xFFFFAA00 : 0xFF38BDF8, 13f);

		py += 18f;
		if (isMaxed) {
			RenderHelper.drawRainbowProgressBar(px, py, w - 32f, 5.5f, 1.0f);
		} else {
			RenderHelper.drawProgressBar(px, py, w - 32f, 5.5f, pet.progressToNextLevel, 0xFF4F46E5, 0xFF818CF8);
		}
		py += 18f;

		// Total XP Info
		RenderHelper.drawStatRow(L10n.translate("pv.pets.exp"), String.format("%,.0f XP", pet.exp), px, py, w - 32f, 13.5f, 0xFFFBBF24);
		py += 24f;

		if (pet.level < pet.maxLevel) {
			RenderHelper.drawStatRow("Next Level", String.format("%,.0f / %,.0f", pet.currentLevelExp, pet.nextLevelExp), px, py, w - 32f, 13.5f, RenderHelper.FONT_PRIMARY);
			py += 24f;
		}

		// Candy Used
		String candyVal = pet.candyUsed > 0 ? (pet.candyUsed + " / 10") : "0 / 10";
		RenderHelper.drawStatRow("Candy", candyVal, px, py, w - 32f, 13.5f, pet.candyUsed > 0 ? 0xFFF472B6 : RenderHelper.FONT_SECONDARY);
		py += 24f;

		// Held Item (Render item icon without tooltip)
		if (!pet.heldItem.isEmpty()) {
			float itemSlotS = 24f;
			float itemSlotX = px + w - 32f - itemSlotS;
			float itemSlotY = py - 4f;

			RenderHelper.drawStatRow("Held Item", pet.heldItem.replace("_", " "), px, py, w - 32f - itemSlotS - 8f, 13.5f, 0xFF60A5FA);

			NVGRenderer.rect(itemSlotX, itemSlotY, itemSlotS, itemSlotS, 0xFF1E293B, 4f);
			NVGRenderer.outlineRect(itemSlotX, itemSlotY, itemSlotS, itemSlotS, 1f, 0xFF334155, 4f);

			ItemStack heldStack = ItemRepo.getItemStack(pet.heldItem);
			if (!heldStack.isEmpty()) {
				// Register as decorative slot without tooltip
				RenderHelper.registerItemSlot(itemSlotX, itemSlotY, itemSlotS, heldStack, false);

			}
		} else {
			RenderHelper.drawStatRow("Held Item", "None", px, py, w - 32f, 13.5f, RenderHelper.FONT_MUTED);
		}

	}

	public static void renderPetLevelBadges() {
		for (PetSlotInfo slot : visiblePetSlots) {
			if (slot.isDetailSlot || slot.pet == null || slot.pet.level <= 0) continue;
			String lvlStr = String.valueOf(slot.pet.level);
			float lvlW = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 10f);
			NVGRenderer.rect(slot.x + slot.size - lvlW - 6f, slot.y + slot.size - 13f, lvlW + 4f, 11f, 0xDD111218, 3f);
			NVGRenderer.text(lvlStr, slot.x + slot.size - lvlW - 4f, slot.y + slot.size - 12.5f, Fonts.PRETENDARD_SEMIBOLD, slot.pet.getRarityColor(), 10f);
		}
	}

	public static boolean mouseClicked(float mx, float my, float startX, float startY, float width) {
		if (searchBox.mouseClicked(mx, my, 0)) return true;

		for (PetSlotInfo slot : visiblePetSlots) {
			if (slot.isDetailSlot) continue;
			if (mx >= slot.x && mx <= slot.x + slot.size && my >= slot.y && my <= slot.y + slot.size) {
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