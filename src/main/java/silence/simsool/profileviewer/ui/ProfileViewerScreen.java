package silence.simsool.profileviewer.ui;

import static silence.simsool.lucent.Lucent.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import silence.simsool.lucent.general.utils.L10n;
import silence.simsool.lucent.general.utils.useful.UDisplay;
import silence.simsool.lucent.general.utils.useful.UMouse;
import silence.simsool.lucent.general.utils.useful.UScreen;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGPIPRenderer;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.PvApi;
import silence.simsool.profileviewer.api.data.SkyBlockProfileData;
import silence.simsool.profileviewer.api.nbt.ParsedItem;
import silence.simsool.profileviewer.ui.tabs.*;

public class ProfileViewerScreen extends Screen {

	private final String username;
	private final UUID uuid;

	private List<SkyBlockProfileData> profiles = new ArrayList<>();
	private SkyBlockProfileData currentProfile = null;
	private PVTab currentTab = PVTab.OVERVIEW;

	private boolean loading = true;
	private String errorMessage = "";

	private static final int WIN_W = 1000;
	private static final int WIN_H = 630;
	private static final int SIDEBAR_W = 200;
	private static final int TOPBAR_H = 68;

	private float winX, winY;
	private float contentX, contentY, contentW, contentH;
	private float uiScale = 1.0f;

	private double scrollOffset = 0;
	private double maxScroll = 0;
	private boolean profileDropdownOpen = false;
	private boolean isDraggingScrollbar = false;

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
		loadData(false);
	}

	private void updateLayout() {
		float gs = NVGRenderer.getStandardGuiScale();
		int sw = (int) (UDisplay.getWidth() / gs);
		int sh = (int) (UDisplay.getHeight() / gs);

		winX = (sw - WIN_W) / 2f;
		winY = (sh - WIN_H) / 2f;

		contentX = winX + SIDEBAR_W + 16f;
		contentY = winY + TOPBAR_H + 12f;
		contentW = WIN_W - SIDEBAR_W - 32f;
		contentH = WIN_H - TOPBAR_H - 24f;
	}

	private void loadData(boolean forceRefresh) {
		loading = true;
		errorMessage = "";
		PvApi.fetchPlayerStatusAsync(uuid).thenAccept(st -> this.playerStatus = st);
		PvApi.fetchProfilesAsync(uuid, forceRefresh).thenAccept(list -> {
			loading = false;
			if (list == null || list.isEmpty()) {
				errorMessage = L10n.translate("pv.ui.not_found");
				return;
			}
			this.profiles = list;
			if (this.currentProfile == null || forceRefresh) {
				this.currentProfile = list.get(0);
			}
		}).exceptionally(e -> {
			loading = false;
			errorMessage = String.format(L10n.translate("pv.ui.request_failed"), e.getMessage());
			return null;
		});
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
		graphics.fill(0, 0, width, height, 0x88000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		updateLayout();

		float gs = NVGRenderer.getStandardGuiScale();

		// 1. Draw NanoVG Base UI (Background, Cards, Sidebars, Tabs)
		NVGPIPRenderer.draw(graphics, 0, 0, width, height, () -> {
			float smx = UMouse.getNvgScaledX(uiScale);
			float smy = UMouse.getNvgScaledY(uiScale);

			NVGRenderer.push();
			NVGRenderer.scale(gs * uiScale, gs * uiScale);

			// Main Window Frame (Dark Glassmorphism)
			NVGRenderer.rect(winX, winY, WIN_W, WIN_H, 0xF0181920, 14f);
			NVGRenderer.outlineRect(winX, winY, WIN_W, WIN_H, 1.5f, UIColors.withAlpha(UIColors.ITEM_BORDER, 180), 14f);

			// Sidebar Background
			NVGRenderer.rect(winX, winY, SIDEBAR_W, WIN_H, 0xF513141A, 14f, 0, 0, 14f);
			NVGRenderer.rect(winX + SIDEBAR_W, winY, 1f, WIN_H, UIColors.withAlpha(UIColors.ITEM_BORDER, 100));

			// Topbar Separator
			NVGRenderer.rect(winX + SIDEBAR_W, winY + TOPBAR_H, WIN_W - SIDEBAR_W, 1f, UIColors.withAlpha(UIColors.ITEM_BORDER, 100));

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

			NVGRenderer.pop();
		});

		// 2. Draw 3D/2D Minecraft Item Textures in exact NanoVG Coordinate Space
		float scaleRatio = (float) NVGRenderer.getStandardGuiScale() / (float) mc.getWindow().getGuiScale();
		float totalScale = scaleRatio * uiScale;
		float itemScale = 2.0f;

		ItemStack hoveredStack = ItemStack.EMPTY;

		graphics.pose().pushMatrix();
		graphics.pose().scale(totalScale * itemScale, totalScale * itemScale);

		if (currentTab == PVTab.GEAR) {
			for (GearTabRenderer.SlotRenderInfo slot : GearTabRenderer.visibleSlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				if (slot.item != null && !slot.item.isEmpty() && slot.item.itemStack != null && !slot.item.itemStack.isEmpty()) {
					int itemX = (int) ((slot.x + (slot.size - 32f) / 2f) / itemScale);
					int itemY = (int) ((slot.y + (slot.size - 32f) / 2f) / itemScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.item.itemStack, itemX, itemY);
					graphics.itemDecorations(this.font, slot.item.itemStack, itemX, itemY);

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
					int itemX = (int) ((slot.x + (slot.size - 32f) / 2f) / itemScale);
					int itemY = (int) ((slot.y + (slot.size - 32f) / 2f) / itemScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.pet.itemStack, itemX, itemY);
					graphics.itemDecorations(this.font, slot.pet.itemStack, itemX, itemY);

					float slotScreenX = slot.x * totalScale;
					float slotScreenY = slot.y * totalScale;
					float slotScreenSize = slot.size * totalScale;
					if (mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
						hoveredStack = slot.pet.itemStack;
					}
				}
			}
		} else if (currentTab == PVTab.MINING) {
			for (MiningTabRenderer.TreeSlotInfo slot : MiningTabRenderer.visibleTreeSlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				if (slot.stack != null && !slot.stack.isEmpty()) {
					int itemX = (int) ((slot.x + (slot.size - 32f) / 2f) / itemScale);
					int itemY = (int) ((slot.y + (slot.size - 32f) / 2f) / itemScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.stack, itemX, itemY);
					graphics.itemDecorations(this.font, slot.stack, itemX, itemY);

					float slotScreenX = slot.x * totalScale;
					float slotScreenY = slot.y * totalScale;
					float slotScreenSize = slot.size * totalScale;
					if (mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
						hoveredStack = slot.stack;
					}
				}
			}
		} else if (currentTab == PVTab.FISHING) {
			for (FishingTabRenderer.TrophySlotInfo slot : FishingTabRenderer.visibleTrophySlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				if (slot.stack != null && !slot.stack.isEmpty()) {
					int itemX = (int) ((slot.x + (slot.size - 32f) / 2f) / itemScale);
					int itemY = (int) ((slot.y + (slot.size - 32f) / 2f) / itemScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.stack, itemX, itemY);
					graphics.itemDecorations(this.font, slot.stack, itemX, itemY);

					float slotScreenX = slot.x * totalScale;
					float slotScreenY = slot.y * totalScale;
					float slotScreenSize = slot.size * totalScale;
					if (mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
						hoveredStack = slot.stack;
					}
				}
			}
		} else if (currentTab == PVTab.MUSEUM) {
			for (MuseumTabRenderer.MuseumSlotInfo slot : MuseumTabRenderer.visibleMuseumSlots) {
				if (slot.y < contentY - 5f || slot.y + slot.size > contentY + contentH + 5f) continue;
				if (slot.stack != null && !slot.stack.isEmpty()) {
					int itemX = (int) ((slot.x + (slot.size - 32f) / 2f) / itemScale);
					int itemY = (int) ((slot.y + (slot.size - 32f) / 2f) / itemScale);
					silence.simsool.lucent.general.utils.render.ItemRenderer.drawItemStack(graphics, slot.stack, itemX, itemY);
					graphics.itemDecorations(this.font, slot.stack, itemX, itemY);

					float slotScreenX = slot.x * totalScale;
					float slotScreenY = slot.y * totalScale;
					float slotScreenSize = slot.size * totalScale;
					if (mouseX >= slotScreenX && mouseX < slotScreenX + slotScreenSize && mouseY >= slotScreenY && mouseY < slotScreenY + slotScreenSize) {
						hoveredStack = slot.stack;
					}
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
				int scale = (int) (105f * totalScale);
				float yOffset = 0.06f;

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
		float hy = winY + 22f;

		// H2 (24px) for Username
		NVGRenderer.text(username, hx, hy, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, RenderHelper.FS_H2);

		if (currentProfile != null) {
			float userW = NVGRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FS_H2);
			float pX = hx + userW + 16f;
			float pW = 120f;
			float pH = 28f;
			boolean hovP = mx >= pX && mx <= pX + pW && my >= hy - 2f && my <= hy - 2f + pH;
			NVGRenderer.rect(pX, hy - 2f, pW, pH, hovP ? 0xFF333748 : 0xFF232532, 6f);
			NVGRenderer.outlineRect(pX, hy - 2f, pW, pH, 1f, hovP ? UIColors.ACCENT_BLUE : 0x44FFFFFF, 6f);

			NVGRenderer.text(currentProfile.cuteName, pX + 12f, hy + 5f, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_BUTTON);
			NVGRenderer.text("\uE5C5", pX + pW - 22f, hy + 5f, Fonts.MATERIAL_ICONS_ROUND, UIColors.ACCENT_BLUE, 16f);

			if (!"normal".equalsIgnoreCase(currentProfile.gameMode)) {
				RenderHelper.drawBadge(currentProfile.gameMode.toUpperCase(), pX + pW + 10f, hy - 1f, 0xFF4A3B18, 0xFFFFAA00);
			}
		}

		float btnSize = 28f;
		float closeX = winX + WIN_W - 42f;
		float refX = closeX - 36f;

		boolean hovRef = mx >= refX && mx <= refX + btnSize && my >= hy - 2f && my <= hy - 2f + btnSize;
		NVGRenderer.rect(refX, hy - 2f, btnSize, btnSize, hovRef ? 0x44FFFFFF : 0x22FFFFFF, 6f);
		NVGRenderer.text("\uE5D5", refX + 6f, hy + 4f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_PRIMARY, 16f);

		boolean hovClose = mx >= closeX && mx <= closeX + btnSize && my >= hy - 2f && my <= hy - 2f + btnSize;
		NVGRenderer.rect(closeX, hy - 2f, btnSize, btnSize, hovClose ? 0x44FF4444 : 0x22FFFFFF, 6f);
		NVGRenderer.text("\uE5CD", closeX + 6f, hy + 4f, Fonts.MATERIAL_ICONS_ROUND, RenderHelper.FONT_PRIMARY, 16f);
	}

	private void renderSidebar(float mx, float my) {
		float sx = winX + 14f;
		float sy = winY + 24f;

		// H3 (20px) Sidebar Title
		NVGRenderer.text(L10n.translate("pv.ui.title"), sx + 6, sy, Fonts.PRETENDARD_SEMIBOLD, UIColors.ACCENT_BLUE, RenderHelper.FS_H3);
		sy += 36f;

		float tabW = SIDEBAR_W - 28f;
		float tabH = 34f;

		for (PVTab tab : PVTab.values()) {
			boolean active = (tab == currentTab);
			boolean hov = mx >= sx && mx <= sx + tabW && my >= sy && my <= sy + tabH;

			if (active) {
				NVGRenderer.rect(sx, sy, tabW, tabH, UIColors.ACCENT_BLUE, 8f);
			} else if (hov) {
				NVGRenderer.rect(sx, sy, tabW, tabH, 0x22FFFFFF, 8f);
			}

			int textColor = active ? RenderHelper.FONT_PRIMARY : (hov ? RenderHelper.FONT_PRIMARY : RenderHelper.FONT_MUTED);
			NVGRenderer.text(tab.icon, sx + 10f, sy + 8f, Fonts.MATERIAL_ICONS_ROUND, textColor, 16f);
			NVGRenderer.text(tab.getTitle(), sx + 34f, sy + 8.5f, Fonts.PRETENDARD_MEDIUM, textColor, RenderHelper.FS_BUTTON);

			sy += tabH + 4f;
		}
	}


	private void renderContent(float mx, float my, float delta) {
		if (loading) {
			String loadTxt = L10n.translate("pv.ui.loading");
			float loadW = NVGRenderer.textWidth(loadTxt, Fonts.PRETENDARD_MEDIUM, RenderHelper.FS_BODY);
			NVGRenderer.text(loadTxt, contentX + (contentW - loadW) / 2f, contentY + contentH / 2f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, RenderHelper.FS_BODY);
			return;
		}

		if (!errorMessage.isEmpty()) {
			NVGRenderer.text(errorMessage, contentX + 20f, contentY + 40f, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF5555, RenderHelper.FS_BODY);
			return;
		}

		if (currentProfile == null) return;

		NVGRenderer.pushScissor(contentX, contentY, contentW, contentH);

		float renderedH = 0;
		float startY = (float) (contentY - scrollOffset);

		OverviewTabRenderer.playerBounds.visible = false;

		switch (currentTab) {
			case OVERVIEW -> renderedH = OverviewTabRenderer.render(username, currentProfile.member, playerStatus, contentX, startY, contentW, mx, my, delta);
			case GEAR -> renderedH = GearTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case PETS -> renderedH = PetsTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case DUNGEONS -> renderedH = DungeonsTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case SLAYER -> renderedH = SlayerTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case MINING -> renderedH = MiningTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case FARMING -> renderedH = FarmingTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case FISHING -> renderedH = FishingTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case MUSEUM -> renderedH = MuseumTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case RIFT -> renderedH = RiftTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case COLLECTIONS -> renderedH = CollectionsTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
			case CHOCOLATE_FACTORY -> renderedH = ChocolateFactoryTabRenderer.render(currentProfile.member, contentX, startY, contentW, mx, my, delta);
		}

		maxScroll = Math.max(0, renderedH - contentH);

		NVGRenderer.popScissor();

		if (maxScroll > 0) {
			float trackX = contentX + contentW + 4f;
			float trackY = contentY;
			float trackH = contentH;
			float thumbH = Math.max(25f, (float) (contentH / (renderedH)) * trackH);
			float thumbY = trackY + (float) (scrollOffset / maxScroll) * (trackH - thumbH);
			NVGRenderer.rect(trackX, thumbY, 4f, thumbH, 0x55FFFFFF, 2f);
		}
	}

	private void renderProfileDropdown(float mx, float my) {
		float hx = winX + SIDEBAR_W + 20f;
		float pX = hx + NVGRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20f) + 16f;
		float pY = winY + 22f + 24f;
		float pW = 140f;
		float itemH = 28f;
		float totalH = profiles.size() * itemH + 8f;

		NVGRenderer.rect(pX, pY, pW, totalH, 0xFA20222D, 8f);
		NVGRenderer.outlineRect(pX, pY, pW, totalH, 1f, UIColors.withAlpha(UIColors.ITEM_BORDER, 200), 8f);

		for (int i = 0; i < profiles.size(); i++) {
			SkyBlockProfileData p = profiles.get(i);
			float iy = pY + 4f + i * itemH;
			boolean hov = mx >= pX && mx <= pX + pW && my >= iy && my <= iy + itemH;
			if (hov) NVGRenderer.rect(pX + 4f, iy, pW - 8f, itemH, 0x33FFFFFF, 6f);
			NVGRenderer.text(p.cuteName, pX + 12f, iy + 7f, Fonts.PRETENDARD_MEDIUM, (p == currentProfile) ? UIColors.ACCENT_BLUE : UIColors.TEXT_PRIMARY, 13f);
		}
	}

	private void renderItemTooltip(float mx, float my) {
		ParsedItem item = GearTabRenderer.hoveredItem;
		if (item == null || item.isEmpty()) return;

		float tx = GearTabRenderer.hoveredItemX;
		float ty = GearTabRenderer.hoveredItemY;

		float maxLoreW = RenderHelper.getColoredTextWidth(item.displayName, 14f);
		for (String l : item.lore) {
			float lw = RenderHelper.getColoredTextWidth(l, 11f);
			if (lw > maxLoreW) maxLoreW = lw;
		}

		float tipW = Math.min(380f, maxLoreW + 24f);
		float tipH = 26f + (item.lore.size() * 15f) + 12f;

		float gs = NVGRenderer.getStandardGuiScale();
		float maxW = UDisplay.getWidth() / gs;
		float maxH = UDisplay.getHeight() / gs;

		if (tx + tipW > maxW - 10) tx = maxW - tipW - 10;
		if (ty + tipH > maxH - 10) ty = maxH - tipH - 10;

		NVGRenderer.rect(tx, ty, tipW, tipH, 0xF812131A, 8f);
		NVGRenderer.outlineRect(tx, ty, tipW, tipH, 1.2f, item.rarityColor, 8f);

		RenderHelper.drawColoredText(item.displayName, tx + 10f, ty + 8f, 14f, item.rarityColor);

		float ly = ty + 26f;
		for (String line : item.lore) {
			RenderHelper.drawColoredText(line, tx + 10f, ly, 11f, UIColors.TEXT_SECONDARY);
			ly += 15f;
		}
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
		float mx = UMouse.getNvgScaledX(uiScale);
		float my = UMouse.getNvgScaledY(uiScale);
		int btn = event.button();

		if (btn == 0) {
			float startY = (float) (contentY - scrollOffset);

			if (currentTab == PVTab.GEAR) {
				if (GearTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			} else if (currentTab == PVTab.COLLECTIONS) {
				if (CollectionsTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			} else if (currentTab == PVTab.DUNGEONS) {
				if (DungeonsTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			} else if (currentTab == PVTab.PETS) {
				if (PetsTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			} else if (currentTab == PVTab.MINING) {
				if (MiningTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			} else if (currentTab == PVTab.FISHING) {
				if (FishingTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			} else if (currentTab == PVTab.MUSEUM) {
				if (MuseumTabRenderer.mouseClicked(mx, my, contentX, startY, contentW)) {
					return true;
				}
			}

			if (profileDropdownOpen) {
				float hx = winX + SIDEBAR_W + 20f;
				float pX = hx + NVGRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20f) + 16f;
				float pY = winY + 22f + 24f;
				float pW = 140f;
				float itemH = 28f;

				for (int i = 0; i < profiles.size(); i++) {
					float iy = pY + 4f + i * itemH;
					if (mx >= pX && mx <= pX + pW && my >= iy && my <= iy + itemH) {
						currentProfile = profiles.get(i);
						profileDropdownOpen = false;
						scrollOffset = 0;
						return true;
					}
				}
				profileDropdownOpen = false;
				return true;
			}

			float hx = winX + SIDEBAR_W + 20f;
			float pX = hx + NVGRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, 20f) + 16f;
			float pY = winY + 22f - 4f;
			if (mx >= pX && mx <= pX + 120f && my >= pY && my <= pY + 26f) {
				profileDropdownOpen = !profileDropdownOpen;
				return true;
			}

			float closeX = winX + WIN_W - 40f;
			if (mx >= closeX && mx <= closeX + 28f && my >= winY + 16f && my <= winY + 44f) {
				UScreen.setScreen(null);
				return true;
			}

			float refX = closeX - 36f;
			if (mx >= refX && mx <= refX + 28f && my >= winY + 16f && my <= winY + 44f) {
				loadData(true);
				return true;
			}

			float sx = winX + 14f;
			float sy = winY + 60f;
			float tabW = SIDEBAR_W - 28f;
			float tabH = 34f;
			for (PVTab tab : PVTab.values()) {
				if (mx >= sx && mx <= sx + tabW && my >= sy && my <= sy + tabH) {
					currentTab = tab;
					scrollOffset = 0;
					return true;
				}
				sy += tabH + 4f;
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
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double mouseX, double mouseY) {
		if (isDraggingScrollbar && maxScroll > 0) {
			float my = UMouse.getNvgScaledY(uiScale);
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
		if (maxScroll > 0) {
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - vAmount * 30.0));
			return true;
		}
		return super.mouseScrolled(mx, my, hAmount, vAmount);
	}

	@Override
	public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
		if (currentTab == PVTab.PETS) {
			if (PetsTabRenderer.charTyped((char) event.codepoint(), 0)) {
				return true;
			}
		}
		return super.charTyped(event);
	}


	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (currentTab == PVTab.PETS) {
			if (PetsTabRenderer.keyPressed(event.key(), event.scancode(), event.modifiers())) {
				return true;
			}
		}
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			UScreen.setScreen(null);
			return true;
		}
		return super.keyPressed(event);
	}


	@Override
	public boolean isPauseScreen() {
		return false;
	}
}