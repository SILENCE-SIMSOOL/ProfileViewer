package silence.simsool.profileviewer.ui;

import static silence.simsool.lucent.Lucent.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.general.utils.useful.UDisplay;
import silence.simsool.lucent.general.utils.useful.UMouse;
import silence.simsool.lucent.general.utils.useful.UScreen;
import silence.simsool.lucent.ui.utils.URender;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;
import silence.simsool.profileviewer.api.PvApi;
import silence.simsool.profileviewer.api.PlayerDbApi;
import silence.simsool.profileviewer.api.data.SkyBlockProfileData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.ui.tabs.ChocolateFactoryTabRenderer;
import silence.simsool.profileviewer.ui.tabs.CollectionsTabRenderer;
import silence.simsool.profileviewer.ui.tabs.DungeonsTabRenderer;
import silence.simsool.profileviewer.ui.tabs.FishingTabRenderer;
import silence.simsool.profileviewer.ui.tabs.ForagingTabRenderer;
import silence.simsool.profileviewer.ui.tabs.GardenTabRenderer;
import silence.simsool.profileviewer.ui.tabs.GearTabRenderer;
import silence.simsool.profileviewer.ui.tabs.MiningTabRenderer;
import silence.simsool.profileviewer.ui.tabs.MuseumTabRenderer;
import silence.simsool.profileviewer.ui.tabs.OverviewTabRenderer;
import silence.simsool.profileviewer.ui.tabs.PetsTabRenderer;
import silence.simsool.profileviewer.ui.tabs.RiftTabRenderer;
import silence.simsool.profileviewer.ui.tabs.SlayerTabRenderer;

public class ProfileViewerScreen extends Screen {

	private String username;
	private UUID uuid;

	private List<SkyBlockProfileData> profiles = new ArrayList<>();
	private SkyBlockProfileData currentProfile = null;
	private PVTab currentTab = PVTab.OVERVIEW;

	private boolean loading = true;
	private boolean initialized;
	private long requestGeneration;
	private String errorMessage = "";

	private static final int WIN_W = 1200;
	private static final int WIN_H = 750;
	private static final int SIDEBAR_W = 220;
	private static final int TOPBAR_H = 66;

	private float winX, winY;
	private float contentX, contentY, contentW, contentH;
	private float uiScale = 1.0f;

	private double scrollOffset = 0;
	private double maxScroll = 0;
	private boolean profileDropdownOpen = false;
	private boolean isDraggingScrollbar = false;

	private String searchInput = "";
	private boolean searchFocused = false;

	private net.minecraft.client.entity.ClientMannequin mannequin = null;
	private silence.simsool.profileviewer.api.data.PlayerStatus playerStatus = null;
	private int lastArmorHash = 0;
	private UUID lastMannequinUuid = null;
	private static final java.util.concurrent.atomic.AtomicInteger NEXT_ENTITY_ID = new java.util.concurrent.atomic.AtomicInteger(100000);

	public ProfileViewerScreen(String username, UUID uuid) {
		super(Component.literal("Profile Viewer"));
		this.username = username;
		this.uuid = uuid;
	}

	@Override
	protected void init() {
		super.init();
		updateLayout();
		if (!initialized) {
			initialized = true;
			loadData(false);
		}
	}

	private void updateLayout() {
		float gs = SkijaRenderer.getStandardGuiScale();
		float sw = (float) UDisplay.getWidth() / gs;
		float sh = (float) UDisplay.getHeight() / gs;

		float margin = 12f;
		float availW = sw - margin * 2f;
		float availH = sh - margin * 2f;

		float targetScale = 1.0f;
		if (availW < WIN_W || availH < WIN_H) {
			targetScale = Math.min(availW / WIN_W, availH / WIN_H);
		}
		this.uiScale = Math.max(0.2f, targetScale);

		winX = (sw - WIN_W * uiScale) / 2f / uiScale;
		winY = (sh - WIN_H * uiScale) / 2f / uiScale;

		contentX = winX + SIDEBAR_W + 18f;
		contentY = winY + TOPBAR_H + 10f;
		contentW = WIN_W - SIDEBAR_W - 36f;
		contentH = WIN_H - TOPBAR_H - 20f;
	}

	private void clearContentState() {
		scrollOffset = 0;
		maxScroll = 0;
		isDraggingScrollbar = false;
		profileDropdownOpen = false;
		OverviewTabRenderer.visibleItemSlots.clear();
		OverviewTabRenderer.playerBounds.visible = false;
		GearTabRenderer.visibleSlots.clear();
		PetsTabRenderer.visiblePetSlots.clear();
		RenderHelper.clearGlobalSlots();
	}

	private void loadData(boolean forceRefresh) {
		long generation = ++requestGeneration;
		String selectedId = currentProfile == null ? "" : currentProfile.profileId;
		loading = true;
		errorMessage = "";
		clearContentState();
		PvApi.fetchPlayerStatusAsync(uuid, forceRefresh).thenAccept(status -> mc.execute(() -> {
			if (generation == requestGeneration) playerStatus = status;
		}));
		PvApi.fetchProfilesAsync(uuid, forceRefresh).whenComplete((list, error) -> mc.execute(() -> {
			if (generation != requestGeneration) return;
			loading = false;
			if (error != null) {
				errorMessage = String.format(L10n.translate("pv.ui.request_failed"), failureMessage(error));
				return;
			}
			profiles = list;
			currentProfile = list.stream().filter(profile -> profile.profileId.equals(selectedId)).findFirst().orElse(list.isEmpty() ? null : list.get(0));
			if (currentProfile == null) errorMessage = L10n.translate("pv.ui.not_found");
		}));
	}

	private static String failureMessage(Throwable error) {
		while (error.getCause() != null) error = error.getCause();
		return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
	}

	private void performSearch(String target) {
		long generation = ++requestGeneration;
		loading = true;
		errorMessage = "";
		clearContentState();
		PlayerDbApi.resolveGameProfile(target).whenComplete((profile, error) -> mc.execute(() -> {
			if (generation != requestGeneration) return;
			if (error != null || profile == null) {
				loading = false;
				errorMessage = error == null ? L10n.translate("pv.ui.not_found") : String.format(L10n.translate("pv.ui.request_failed"), failureMessage(error));
				return;
			}
			username = profile.name();
			uuid = profile.id();
			currentProfile = null;
			profiles = new ArrayList<>();
			playerStatus = null;
			mannequin = null;
			loadData(false);
		}));
	}

	private void updateMannequin() {
		if (mc.level == null) return;
		if (currentProfile == null || currentProfile.member == null) return;

		List<ParsedItem> armor = currentProfile.member.inventory.armor;
		int armorHash = (armor != null ? armor.hashCode() : 0) ^ (uuid != null ? uuid.hashCode() : 0);

		if (mannequin == null || !uuid.equals(lastMannequinUuid)) {
			mannequin = new net.minecraft.client.entity.ClientMannequin(mc.level, mc.playerSkinRenderCache());
			mannequin.setId(NEXT_ENTITY_ID.getAndIncrement());
			com.mojang.authlib.GameProfile gp = new com.mojang.authlib.GameProfile(uuid, username);
			net.minecraft.world.item.component.ResolvableProfile resolvableProfile = net.minecraft.world.item.component.ResolvableProfile.createResolved(gp);
			resolvableProfile.resolveProfile(mc.services().profileResolver());
			lastMannequinUuid = uuid;
			lastArmorHash = 0;
		}

		if (armorHash != lastArmorHash) {
			lastArmorHash = armorHash;
			if (armor != null && !armor.isEmpty()) {
				if (armor.size() > 0 && armor.get(0).itemStack != null) mannequin.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, armor.get(0).itemStack);
				if (armor.size() > 1 && armor.get(1).itemStack != null) mannequin.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, armor.get(1).itemStack);
				if (armor.size() > 2 && armor.get(2).itemStack != null) mannequin.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, armor.get(2).itemStack);
				if (armor.size() > 3 && armor.get(3).itemStack != null) mannequin.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, armor.get(3).itemStack);
			}
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0x99000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		updateLayout();

		float gs = SkijaRenderer.getStandardGuiScale();

		// 1. Draw Skija Base UI (Background, Cards, Sidebars, Tabs)
		SkijaRenderer.draw(graphics, 0, 0, width, height, () -> {
			float smx = UMouse.getSkijaScaledX(uiScale);
			float smy = UMouse.getSkijaScaledY(uiScale);

			SkijaRenderer.push();
			SkijaRenderer.scale(gs * uiScale, gs * uiScale);

			// Main Window Frame with Subtle Nebula Fog Background (Photo 3 Effect across entire window, 90% opacity = 0xE6)
			SkijaRenderer.pushScissor(winX, winY, WIN_W, WIN_H);
			SkijaRenderer.rect(winX, winY, WIN_W, WIN_H, 0xE60A0C13, 14f);

			// Gentle, subtle atmospheric fog clouds across sidebar and content
			RenderHelper.drawAmbientGlow(winX + 160f, winY + 120f, 420f, 0x105850EC, 0x00000000); // Top-left indigo fog
			RenderHelper.drawAmbientGlow(winX + 820f, winY + 140f, 480f, 0x0C2563EB, 0x00000000); // Top-right deep blue fog
			RenderHelper.drawAmbientGlow(winX + 980f, winY + 580f, 520f, 0x0E7C3AED, 0x00000000); // Bottom-right purple fog
			RenderHelper.drawAmbientGlow(winX + 220f, winY + 620f, 400f, 0x0A0284C7, 0x00000000); // Bottom-left cyan fog
			RenderHelper.drawAmbientGlow(winX + 600f, winY + 380f, 360f, 0x086366F1, 0x00000000); // Subtle center glow
			SkijaRenderer.popScissor();

			SkijaRenderer.outlineRect(winX, winY, WIN_W, WIN_H, 1.2f, 0x22FFFFFF, 14f);

			SkijaRenderer.rect(winX, winY, SIDEBAR_W, WIN_H, 0x55080910, 14f, 0, 0, 14f);
			SkijaRenderer.rect(winX + SIDEBAR_W, winY, 1f, WIN_H, 0x14FFFFFF);

			// Topbar Separator
			SkijaRenderer.rect(winX + SIDEBAR_W, winY + TOPBAR_H, WIN_W - SIDEBAR_W, 1f, 0x14FFFFFF);

			// Topbar
			renderTopBar(smx, smy);

			// Sidebar Tabs
			renderSidebar(smx, smy);

			// Content
			renderContent(smx, smy, delta);

			// Dropdown
			if (profileDropdownOpen) {
				renderProfileDropdown(smx, smy);
			}

			SkijaRenderer.pop();
		});

		// 2. Draw 3D/2D Minecraft Item Textures in exact Skija Coordinate Space
		float scaleRatio = (float) SkijaRenderer.getStandardGuiScale() / (float) mc.getWindow().getGuiScale();
		float totalScale = scaleRatio * uiScale;
		float itemScale = 2.0f;

		ItemStack hoveredStack = ItemStack.EMPTY;

		graphics.pose().pushMatrix();
		graphics.pose().scale(totalScale * itemScale, totalScale * itemScale);

		if (!loading && (currentTab == PVTab.OVERVIEW || currentTab == PVTab.SLAYER)) {
			for (OverviewTabRenderer.OverviewSlotInfo slot : OverviewTabRenderer.visibleItemSlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				float customScale = slot.size / 32f;
				if (slot.texture != null) {
					graphics.pose().pushMatrix();
					graphics.pose().translate(slot.x / itemScale, slot.y / itemScale);
					graphics.pose().scale(customScale, customScale);
					URender.drawImage(graphics, slot.texture, 0, 0, 16, 16);
					graphics.pose().popMatrix();
				} else if (slot.stack != null && !slot.stack.isEmpty()) {
					graphics.pose().pushMatrix();
					graphics.pose().translate(slot.x / itemScale, slot.y / itemScale);
					graphics.pose().scale(customScale, customScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.stack, 0, 0);
					graphics.pose().popMatrix();
				}
				// Overview tab uses items only as icons - no tooltip
			}
		} else if (currentTab == PVTab.GEAR || currentTab == PVTab.RIFT) {
			for (GearTabRenderer.SlotRenderInfo slot : GearTabRenderer.visibleSlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				if (slot.item != null && !slot.item.isEmpty() && slot.item.itemStack != null && !slot.item.itemStack.isEmpty()) {
					float slotSize = slot.size;
					float itemVisualSize = 32f;
					float customScale = Math.min(1.0f, (slotSize - 4f) / itemVisualSize);
					float offX = (slotSize - itemVisualSize * customScale) / 2f;
					float offY = (slotSize - itemVisualSize * customScale) / 2f;

					graphics.pose().pushMatrix();
					graphics.pose().translate((slot.x + offX) / itemScale, (slot.y + offY) / itemScale);
					graphics.pose().scale(customScale, customScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.item.itemStack, 0, 0);
					if (slot.item.count > 1) {
						graphics.itemDecorations(this.font, slot.item.itemStack, 0, 0);
					}
					graphics.pose().popMatrix();

					float slotScreenX = slot.x * totalScale;
					float slotScreenY = slot.y * totalScale;
					float slotScreenSize = slot.size * totalScale;
					if (mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
						hoveredStack = slot.item.itemStack;
					}
				}
			}
		} else if (currentTab == PVTab.PETS) {
			for (PetsTabRenderer.PetSlotInfo slot : PetsTabRenderer.visiblePetSlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				if (slot.pet != null && slot.pet.itemStack != null && !slot.pet.itemStack.isEmpty()) {
					float slotSize = slot.size;
					float itemVisualSize = 32f;
					float customScale = slot.isDetailSlot ? 1.35f : Math.min(1.0f, (slotSize - 4f) / itemVisualSize);
					float offX = (slotSize - itemVisualSize * customScale) / 2f;
					float offY = (slotSize - itemVisualSize * customScale) / 2f;

					graphics.pose().pushMatrix();
					graphics.pose().translate((slot.x + offX) / itemScale, (slot.y + offY) / itemScale);
					graphics.pose().scale(customScale, customScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.pet.itemStack, 0, 0);
					graphics.itemDecorations(this.font, slot.pet.itemStack, 0, 0);

					graphics.pose().popMatrix();


					float slotScreenX = slot.x * totalScale;
					float slotScreenY = slot.y * totalScale;
					float slotScreenSize = slot.size * totalScale;
					if (mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
						hoveredStack = slot.pet.itemStack;
					}
				}

			}
		}


		// Render Global Item Slots (Unified for all tabs: Farming, ChocolateFactory, Collections, Rift, Slayer, etc.)
		float dropX1 = winX + SIDEBAR_W + 24f + SkijaRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20.5f) + 16f;
		float dropX2 = dropX1 + 145f;
		float dropY1 = winY + 27f - 4f;
		float dropY2 = dropY1 + 28f + (profileDropdownOpen ? (profiles.size() * 28f + 6f) : 0f);

		for (RenderHelper.ItemSlotInfo slot : RenderHelper.globalItemSlots) {
			if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
			if (profileDropdownOpen && slot.x + slot.size >= dropX1 && slot.x <= dropX2 && slot.y + slot.size >= dropY1 && slot.y <= dropY2) continue;

			if (slot.stack != null && !slot.stack.isEmpty()) {
				float slotSize = slot.size;
				float itemVisualSize = 32f;
				float customScale = Math.min(1.0f, (slotSize - 4f) / itemVisualSize);
				float offX = (slotSize - itemVisualSize * customScale) / 2f;
				float offY = (slotSize - itemVisualSize * customScale) / 2f;

				graphics.pose().pushMatrix();
				graphics.pose().translate((slot.x + offX) / itemScale, (slot.y + offY) / itemScale);
				graphics.pose().scale(customScale, customScale);
				silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.stack, 0, 0);
				graphics.itemDecorations(this.font, slot.stack, 0, 0);

				if (slot.customText != null && !slot.customText.isEmpty()) {
					int strW = this.font.width(slot.customText);
					int tx = (int) (16 - strW);
					int ty = (int) (16 - 7);
					silence.simsool.lucent.general.utils.render.DrawContextUtils.text(graphics, slot.customText, tx, ty, slot.customTextColor, true);
				}



				graphics.pose().popMatrix();

				float slotScreenX = slot.x * totalScale;
				float slotScreenY = slot.y * totalScale;
				float slotScreenSize = slot.size * totalScale;
				if (slot.showTooltip && mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
					hoveredStack = slot.stack;
				}

			}
		}

		graphics.pose().popMatrix();


		// Draw 3D Player Mannequin in Overview Tab
		if (currentTab == PVTab.OVERVIEW && OverviewTabRenderer.playerBounds.visible) {
			updateMannequin();
			if (mannequin != null) {
				int x1 = (int) (OverviewTabRenderer.playerBounds.x * totalScale);
				int y1 = (int) (OverviewTabRenderer.playerBounds.y * totalScale);
				int x2 = (int) ((OverviewTabRenderer.playerBounds.x + OverviewTabRenderer.playerBounds.w) * totalScale);
				int y2 = (int) ((OverviewTabRenderer.playerBounds.y + OverviewTabRenderer.playerBounds.h) * totalScale);
				int scale = (int) (112f * totalScale);
				float yOffset = 0.05f;

				net.minecraft.client.gui.screens.inventory.InventoryScreen.extractEntityInInventoryFollowsMouse(
					graphics, x1, y1, x2, y2, scale, yOffset, mouseX, mouseY, mannequin
				);
			}
		}

		// Fixed GUI Scale 2 Tooltip
		if (hoveredStack != null && !hoveredStack.isEmpty()) {
			float adaptiveScale = computeAdaptiveScale();
			int tipMouseX = (int) (mouseX / adaptiveScale);
			int tipMouseY = (int) (mouseY / adaptiveScale);
			graphics.pose().pushMatrix();
			graphics.pose().scale(adaptiveScale, adaptiveScale);
			graphics.setTooltipForNextFrame(this.font, hoveredStack, tipMouseX, tipMouseY);
			graphics.pose().popMatrix();
		}

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	public float computeAdaptiveScale() {
		float currentScale = (float) UDisplay.getGuiScale();
		if (currentScale <= 0) currentScale = 2.0f;
		return 2.0f / currentScale;
	}


	private void renderTopBar(float mx, float my) {
		float hx = winX + SIDEBAR_W + 24f;
		float hy = winY + 27f;

		// Username (20.5px - slightly smaller and placed lower with more breathing room)
		SkijaRenderer.text(username, hx, hy - 1f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 20.5f);

		// Profile Selector Button (Wider, seamless dropdown connection)
		if (currentProfile != null) {
			float userW = SkijaRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20.5f);
			float pX = hx + userW + 16f;
			float pW = 145f;
			float pH = 28f;
			float pY = hy - 4f;
			boolean hovP = mx >= pX && mx <= pX + pW && my >= pY && my <= pY + pH;

			if (profileDropdownOpen) {
				SkijaRenderer.rect(pX, pY, pW, pH, 0xF8141624, 7f, 7f, 0f, 0f);
				SkijaRenderer.outlineRect(pX, pY, pW, pH, 1f, 0x33FFFFFF, 7f, 7f, 0f, 0f);
			} else {
				SkijaRenderer.rect(pX, pY, pW, pH, hovP ? 0xE0222636 : 0xD0141624, 7f);
				SkijaRenderer.outlineRect(pX, pY, pW, pH, 1f, hovP ? 0x33FFFFFF : 0x1AFFFFFF, 7f);
			}

			// Raspberry / Fruit Icon
			SkijaRenderer.text("\uE541", pX + 9f, hy + 2.5f, Fonts.MATERIAL_ICONS_ROUND, 0xFFF43F5E, 16f);
			SkijaRenderer.text(currentProfile.cuteName, pX + 30f, hy + 2.5f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, 14f);

			// Arrow Icon: keyboard_arrow_up \uE316 or keyboard_arrow_down \uE313
			String arrowIcon = profileDropdownOpen ? "\uE316" : "\uE313";
			SkijaRenderer.text(arrowIcon, pX + pW - 20f, hy + 3f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_MUTED, 16f);

			if (!"normal".equalsIgnoreCase(currentProfile.gameMode)) {
				RenderHelper.drawBadge(currentProfile.gameMode.toUpperCase(), pX + pW + 10f, hy - 2f, 0xFF4A3B18, 0xFFFFAA00);
			}
		}

		float btnSize = 28f;
		float closeX = winX + WIN_W - 42f;
		float refX = closeX - 36f;
		float setX = refX - 36f;
		float discX = setX - 36f;

		// Search Bar (with \uE8B6 icon) - Clean look, no blue outline / hover color, text slightly lower
		float searchW = 160f;
		float searchX = discX - searchW - 12f;
		float searchY = hy - 4f;

		SkijaRenderer.rect(searchX, searchY, searchW, btnSize, 0xD0141624, 7f);
		SkijaRenderer.outlineRect(searchX, searchY, searchW, btnSize, 1f, 0x1AFFFFFF, 7f);
		SkijaRenderer.text("\uE8B6", searchX + 8.5f, hy + 3.5f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_MUTED, 15f);

		if (searchInput.isEmpty() && !searchFocused) {
			SkijaRenderer.text("Search player...", searchX + 28f, hy + 4f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_DISABLED, 13f);
		} else {
			String displayTxt = searchInput + (searchFocused && (System.currentTimeMillis() % 1000 < 500) ? "|" : "");
			SkijaRenderer.text(displayTxt, searchX + 28f, hy + 4f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, 13.5f);
		}

		// Discord Button
		if (silence.simsool.lucent.ui.manager.LucentResourceManager.iconDiscord == null) {
			silence.simsool.lucent.ui.manager.LucentResourceManager.loadLucentIcons();
		}
		boolean hovDisc = mx >= discX && mx <= discX + btnSize && my >= hy - 4f && my <= hy - 4f + btnSize;
		SkijaRenderer.rect(discX, hy - 4f, btnSize, btnSize, hovDisc ? 0x33FFFFFF : 0x1AFFFFFF, 6f);
		if (silence.simsool.lucent.ui.manager.LucentResourceManager.iconDiscord != null) {
			SkijaRenderer.image(silence.simsool.lucent.ui.manager.LucentResourceManager.iconDiscord, discX + 5.5f, hy - 4f + 5.5f, 17f, 17f);
		}

		// Settings Button
		boolean hovSet = mx >= setX && mx <= setX + btnSize && my >= hy - 4f && my <= hy - 4f + btnSize;
		SkijaRenderer.rect(setX, hy - 4f, btnSize, btnSize, hovSet ? 0x33FFFFFF : 0x1AFFFFFF, 6f);
		SkijaRenderer.text("\uE8B8", setX + 5.5f, hy + 2f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_PRIMARY, 17f);

		// Refresh Button
		boolean hovRef = mx >= refX && mx <= refX + btnSize && my >= hy - 4f && my <= hy - 4f + btnSize;
		SkijaRenderer.rect(refX, hy - 4f, btnSize, btnSize, hovRef ? 0x33FFFFFF : 0x1AFFFFFF, 6f);
		SkijaRenderer.text("\uE5D5", refX + 5.5f, hy + 2f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_PRIMARY, 17f);

		// Close Button
		boolean hovClose = mx >= closeX && mx <= closeX + btnSize && my >= hy - 4f && my <= hy - 4f + btnSize;
		SkijaRenderer.rect(closeX, hy - 4f, btnSize, btnSize, hovClose ? 0x44FF4444 : 0x1AFFFFFF, 6f);
		SkijaRenderer.text("\uE5CD", closeX + 5.5f, hy + 2f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_PRIMARY, 17f);
	}

	private void renderSidebar(float mx, float my) {
		float sx = winX + 14f;
		float sy = winY + 28f;

		// Sidebar Title (18px) with more top margin
		SkijaRenderer.text("PROFILE ", sx + 4, sy, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 18f);
		float pW = SkijaRenderer.textWidth("PROFILE ", Fonts.PRETENDARD_SEMIBOLD, 18f);
		SkijaRenderer.text("VIEWER", sx + 4 + pW, sy, Fonts.PRETENDARD_SEMIBOLD, 0xFF6366F1, 18f);
		sy += 42f;

		float tabW = SIDEBAR_W - 28f;
		float tabH = 36f;

		for (PVTab tab : PVTab.values()) {
			boolean active = (tab == currentTab);
			boolean hov = mx >= sx && mx <= sx + tabW && my >= sy && my <= sy + tabH;

			if (active) {
				SkijaRenderer.rect(sx, sy, tabW, tabH, 0xBF4F46E5, 8f);
			} else if (hov) {
				SkijaRenderer.rect(sx, sy, tabW, tabH, 0x1AFFFFFF, 8f);
			}

			int textColor = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);
			RenderHelper.alignedIcon(tab.icon, sx + 12f, sy + 11f, Fonts.PRETENDARD_MEDIUM, textColor, 18f, 14.5f);
			SkijaRenderer.text(tab.getTitle(), sx + 36f, sy + 11f, Fonts.PRETENDARD_MEDIUM, textColor, 14.5f);

			sy += tabH + 6f;
		}
	}


	private void renderContent(float mx, float my, float delta) {
		OverviewTabRenderer.visibleItemSlots.clear();
		OverviewTabRenderer.playerBounds.visible = false;
		GearTabRenderer.visibleSlots.clear();
		PetsTabRenderer.visiblePetSlots.clear();
		RenderHelper.clearGlobalSlots();
		if (loading) {
			String loadTxt = "Loading Profile Data...";
			float fs = 24f;
			float loadW = SkijaRenderer.textWidth(loadTxt, Fonts.PRETENDARD_SEMIBOLD, fs);
			float loadX = contentX + (contentW - loadW) / 2f;
			float loadY = contentY + (contentH / 2f) - fs;
			SkijaRenderer.text(loadTxt, loadX, loadY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, fs);
			return;
		}

		if (!errorMessage.isEmpty()) {
			SkijaRenderer.text(errorMessage, contentX + 20f, contentY + 40f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF5555, RenderHelper.FS_BODY);
			return;
		}

		if (currentProfile == null) return;
		String section = switch (currentTab) {
			case OVERVIEW -> "skills";
			case GEAR -> "inventory";
			case CHOCOLATE_FACTORY -> "cf";
			default -> currentTab.name().toLowerCase(Locale.ROOT);
		};
		String sectionError = currentProfile.member.sectionErrors.get(section);
		if (sectionError != null) {
			maxScroll = 0;
			SkijaRenderer.text(sectionError, contentX + 20f, contentY + 40f, Fonts.PRETENDARD_MEDIUM, 0xFFFFAA55, RenderHelper.FS_BODY);
			return;
		}
		String extraError = currentTab == PVTab.GARDEN ? currentProfile.gardenError : currentTab == PVTab.MUSEUM ? currentProfile.museumError : "";
		if (!extraError.isEmpty()) {
			SkijaRenderer.text(extraError, contentX + 20f, contentY + 40f, Fonts.PRETENDARD_MEDIUM, 0xFFFFAA55, RenderHelper.FS_BODY);
			return;
		}

		SkijaRenderer.pushScissor(contentX, contentY, contentW, contentH);

		float renderedH = 0;
		float startY = (float) (contentY - scrollOffset);

		OverviewTabRenderer.visibleItemSlots.clear();
		OverviewTabRenderer.playerBounds.visible = false;
		GearTabRenderer.visibleSlots.clear();
		PetsTabRenderer.visiblePetSlots.clear();
		RenderHelper.clearGlobalSlots();


		switch (currentTab) {
			case OVERVIEW -> renderedH = OverviewTabRenderer.render(username, currentProfile.member, playerStatus, contentX, startY, contentW, mx, my, delta);
			case GEAR -> renderedH = GearTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case PETS -> renderedH = PetsTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case DUNGEONS -> renderedH = DungeonsTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case SLAYER -> renderedH = SlayerTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case MINING -> renderedH = MiningTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case FORAGING -> renderedH = ForagingTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case GARDEN -> renderedH = GardenTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case FISHING -> renderedH = FishingTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case MUSEUM -> renderedH = MuseumTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case RIFT -> renderedH = RiftTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case COLLECTIONS -> renderedH = CollectionsTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case CHOCOLATE_FACTORY -> renderedH = ChocolateFactoryTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
		}


		maxScroll = Math.max(0, renderedH - contentH);
		scrollOffset = Math.min(scrollOffset, maxScroll);

		SkijaRenderer.popScissor();

		if (maxScroll > 0) {
			float trackX = contentX + contentW + 4f;
			float trackY = contentY;
			float trackH = contentH;
			float thumbH = Math.max(25f, (float) (contentH / (renderedH)) * trackH);
			float thumbY = trackY + (float) (scrollOffset / maxScroll) * (trackH - thumbH);
			SkijaRenderer.rect(trackX, thumbY, 4f, thumbH, 0x55FFFFFF, 2f);
		}
	}

	private void renderProfileDropdown(float mx, float my) {
		float hx = winX + SIDEBAR_W + 24f;
		float userW = SkijaRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20.5f);
		float pX = hx + userW + 16f;
		float pW = 145f;
		float pH = 28f;
		float pY = winY + 27f - 4f;

		float itemH = 28f;
		float totalH = profiles.size() * itemH + 6f;

		// Seamless dropdown body attached underneath
		SkijaRenderer.rect(pX, pY + pH, pW, totalH, 0xF8141624, 0f, 0f, 7f, 7f);
		SkijaRenderer.outlineRect(pX, pY + pH, pW, totalH, 1f, 0x33FFFFFF, 0f, 0f, 7f, 7f);

		for (int i = 0; i < profiles.size(); i++) {
			SkyBlockProfileData p = profiles.get(i);
			float iy = pY + pH + 3f + i * itemH;
			boolean hov = mx >= pX && mx <= pX + pW && my >= iy && my <= iy + itemH;
			if (hov) SkijaRenderer.rect(pX + 4f, iy, pW - 8f, itemH, 0x22FFFFFF, 5f);

			int col = (p == currentProfile) ? 0xFFFFFFFF : RenderHelper.FONT_MUTED;
			SkijaRenderer.text(p.cuteName, pX + 12f, iy + 7.5f, Fonts.PRETENDARD_MEDIUM, col, 13.5f);
		}
	}

//	private void renderItemTooltip(float mx, float my) {
//		ParsedItem item = GearTabRenderer.hoveredItem;
//		if (item == null || item.isEmpty()) return;
//
//		float tx = GearTabRenderer.hoveredItemX;
//		float ty = GearTabRenderer.hoveredItemY;
//
//		float maxLoreW = RenderHelper.getColoredTextWidth(item.displayName, 14f);
//		for (String l : item.lore) {
//			float lw = RenderHelper.getColoredTextWidth(l, 11f);
//			if (lw > maxLoreW) maxLoreW = lw;
//		}
//
//		float tipW = Math.min(380f, maxLoreW + 24f);
//		float tipH = 26f + (item.lore.size() * 15f) + 12f;
//
//		float gs = SkijaRenderer.getStandardGuiScale();
//		float maxW = UDisplay.getWidth() / gs;
//		float maxH = UDisplay.getHeight() / gs;
//
//		if (tx + tipW > maxW - 10) tx = maxW - tipW - 10;
//		if (ty + tipH > maxH - 10) ty = maxH - tipH - 10;
//
//		SkijaRenderer.rect(tx, ty, tipW, tipH, 0xF812131A, 8f);
//		SkijaRenderer.outlineRect(tx, ty, tipW, tipH, 1.2f, item.rarityColor, 8f);
//
//		RenderHelper.drawColoredText(item.displayName, tx + 10f, ty + 8f, 14f, item.rarityColor);
//
//		float ly = ty + 26f;
//		for (String line : item.lore) {
//			RenderHelper.drawColoredText(line, tx + 10f, ly, 11f, UIColors.TEXT_SECONDARY);
//			ly += 15f;
//		}
//	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		float mx = UMouse.getSkijaScaledX(uiScale);
		float my = UMouse.getSkijaScaledY(uiScale);
		int btn = UMouse.getButton(event);

		if (btn == 0) {
			float userW = SkijaRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20.5f);
			float pX = winX + SIDEBAR_W + 24f + userW + 16f;
			float pW = 145f;
			float pH = 28f;
			float pY = winY + 27f - 4f;

			if (profileDropdownOpen) {
				float itemH = 28f;
				for (int i = 0; i < profiles.size(); i++) {
					float iy = pY + pH + 3f + i * itemH;
					if (mx >= pX && mx <= pX + pW && my >= iy && my <= iy + itemH) {
						currentProfile = profiles.get(i);
						clearContentState();
						return true;
					}
				}
				profileDropdownOpen = false;
				return true;
			}

			if (mx >= pX && mx <= pX + pW && my >= pY && my <= pY + pH) {
				profileDropdownOpen = !loading && !profiles.isEmpty() && !profileDropdownOpen;
				return true;
			}

			float btnSize = 28f;
			float closeX = winX + WIN_W - 42f;
			float refX = closeX - 36f;
			float setX = refX - 36f;
			float discX = setX - 36f;

			// Search Bar Click
			float searchW = 160f;
			float searchX = discX - searchW - 12f;
			float searchY = winY + 27f - 4f;
			boolean inSearch = mx >= searchX && mx <= searchX + searchW && my >= searchY && my <= searchY + btnSize;
			searchFocused = inSearch;
			if (inSearch) {
				return true;
			}

			if (mx >= closeX && mx <= closeX + btnSize && my >= winY + 23f && my <= winY + 23f + btnSize) {
				UScreen.setScreen(null);
				return true;
			}

			if (mx >= refX && mx <= refX + btnSize && my >= winY + 23f && my <= winY + 23f + btnSize) {
				if (!loading) loadData(true);
				return true;
			}

			if (mx >= setX && mx <= setX + btnSize && my >= winY + 23f && my <= winY + 23f + btnSize) {
				silence.simsool.lucent.ui.screens.ConfigScreen cs = new silence.simsool.lucent.ui.screens.ConfigScreen(silence.simsool.lucent.Lucent.config);
				try {
					java.lang.reflect.Field f = silence.simsool.lucent.ui.screens.ConfigScreen.class.getDeclaredField("currentSidebarPage");
					f.setAccessible(true);
					f.set(cs, "Preferences");
				} catch (Exception ignored) {}
				UScreen.setScreen(cs);
				return true;
			}

			if (mx >= discX && mx <= discX + btnSize && my >= winY + 23f && my <= winY + 23f + btnSize) {
				silence.simsool.lucent.general.utils.useful.UDesktop.openBrowse(silence.simsool.lucent.config.LucentConfig.DISCORD_LINK);
				return true;
			}

			float sx = winX + 14f;
			float sy = winY + 70f;
			float tabW = SIDEBAR_W - 28f;
			float tabH = 36f;
			for (PVTab tab : PVTab.values()) {
				if (mx >= sx && mx <= sx + tabW && my >= sy && my <= sy + tabH) {
					currentTab = tab;
					clearContentState();
					OverviewTabRenderer.visibleItemSlots.clear();
					OverviewTabRenderer.playerBounds.visible = false;
					GearTabRenderer.visibleSlots.clear();
					return true;
				}
				sy += tabH + 6f;
			}

			// Scrollbar Track / Thumb Click
			if (maxScroll > 0) {
				float trackX = contentX + contentW + 2f;
				float trackY = contentY;
				float trackH = contentH;
				if (mx >= trackX - 6f && mx <= trackX + 16f && my >= trackY && my <= trackY + trackH) {
					isDraggingScrollbar = true;
					float thumbH = Math.max(25f, (float) (contentH / (maxScroll + contentH)) * trackH);
					float progress = (my - trackY - thumbH / 2f) / (trackH - thumbH);
					scrollOffset = Math.max(0, Math.min(maxScroll, progress * maxScroll));
					return true;

				}
			}

			if (loading || !errorMessage.isEmpty() || mx < contentX || mx > contentX + contentW || my < contentY || my > contentY + contentH) return true;

			// Subtab Click Delegations
			float subStartY = contentY - (float) scrollOffset;
			if (currentTab == PVTab.GEAR && GearTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.PETS && PetsTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.DUNGEONS && DungeonsTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.MINING && MiningTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.FORAGING && ForagingTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.GARDEN && GardenTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.FISHING && FishingTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.MUSEUM && MuseumTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;
			if (currentTab == PVTab.COLLECTIONS && CollectionsTabRenderer.mouseClicked(mx, my, contentX, subStartY, contentW)) return true;

		}


		return super.mouseClicked(event, doubleClick);

	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double mouseX, double mouseY) {
		if (isDraggingScrollbar && maxScroll > 0) {
			float my = UMouse.getSkijaScaledY(uiScale);
			float trackY = contentY;
			float trackH = contentH;
			float thumbH = Math.max(25f, (float) (contentH / (maxScroll + contentH)) * trackH);
			float progress = (my - trackY - thumbH / 2f) / (trackH - thumbH);
			scrollOffset = Math.max(0, Math.min(maxScroll, progress * maxScroll));
			return true;
		}
		return super.mouseDragged(event, mouseX, mouseY);
	}

	@Override
	public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
		isDraggingScrollbar = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
		if (!loading && errorMessage.isEmpty() && !profileDropdownOpen && maxScroll > 0
			&& UMouse.getSkijaScaledX(uiScale) >= contentX && UMouse.getSkijaScaledX(uiScale) <= contentX + contentW
			&& UMouse.getSkijaScaledY(uiScale) >= contentY && UMouse.getSkijaScaledY(uiScale) <= contentY + contentH) {
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - vAmount * 30.0));
			return true;
		}
		return super.mouseScrolled(mx, my, hAmount, vAmount);
	}

	@Override
	public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
		if (searchFocused) {
			char c = (char) event.codepoint();
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
				if (searchInput.length() < 16) {
					searchInput += c;
				}
				return true;
			}
			return true;
		}
		if (loading || !errorMessage.isEmpty()) return super.charTyped(event);
		if (currentTab == PVTab.PETS) {
			if (PetsTabRenderer.charTyped((char) event.codepoint(), 0)) {
				return true;
			}
		} else if (currentTab == PVTab.GEAR) {
			if (GearTabRenderer.charTyped((char) event.codepoint(), 0)) {
				return true;
			}
		} else if (currentTab == PVTab.MUSEUM) {
			if (MuseumTabRenderer.charTyped((char) event.codepoint(), 0)) {
				return true;
			}
		}
		return super.charTyped(event);
	}


	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {

		if (searchFocused) {

			if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
				String target = searchInput.trim();
				if (!target.isEmpty()) {
					performSearch(target);
					searchInput = "";
					searchFocused = false;
				}
				return true;
			}
			if (event.key() == InputConstants.KEY_BACKSPACE) {
				if (!searchInput.isEmpty()) {
					searchInput = searchInput.substring(0, searchInput.length() - 1);
				}
				return true;
			}
			if (event.key() == InputConstants.KEY_ESCAPE) {
				searchFocused = false;
				return true;
			}
			// Ctrl+V (Paste)
			if (event.key() == InputConstants.KEY_V && (event.modifiers() & InputConstants.MOD_CONTROL) != 0) {
				String clip = mc.keyboardHandler.getClipboard();
				if (clip != null && !clip.isEmpty()) {
					for (char ch : clip.toCharArray()) {
						if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') && searchInput.length() < 16) {
							searchInput += ch;
						}
					}
				}
				return true;
			}
			// Ctrl+C (Copy)
			if (event.key() == InputConstants.KEY_C && (event.modifiers() & InputConstants.MOD_CONTROL) != 0) {
				if (!searchInput.isEmpty()) {
					mc.keyboardHandler.setClipboard(searchInput);
				}
				return true;
			}
			// Ctrl+A (Clear)
			if (event.key() == InputConstants.KEY_A && (event.modifiers() & InputConstants.MOD_CONTROL) != 0) {
				searchInput = "";
				return true;
			}
			return true;
		}
		if (!loading && errorMessage.isEmpty()) {
			if (currentTab == PVTab.PETS && PetsTabRenderer.keyPressed(event.key(), event.keycode(), event.modifiers())) {
				return true;
			}
			if (currentTab == PVTab.GEAR && GearTabRenderer.keyPressed(event.key(), event.keycode(), event.modifiers())) {
				return true;
			}
			if (currentTab == PVTab.MUSEUM && MuseumTabRenderer.keyPressed(event.key(), event.keycode(), event.modifiers())) {
				return true;
			}
		}

		if (event.key() == InputConstants.KEY_ESCAPE) {
			if (profileDropdownOpen) {
				profileDropdownOpen = false;
				return true;
			}
			UScreen.setScreen(null);
			return true;
		}
		return super.keyPressed(event);
	}


	@Override
	public void removed() {
		requestGeneration++;
		initialized = false;
		clearContentState();
		super.removed();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
