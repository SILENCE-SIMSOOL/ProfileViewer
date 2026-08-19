package silence.simsool.profileviewer.ui.tabs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;
import silence.simsool.profileviewer.api.data.MemberData;
import silence.simsool.profileviewer.api.data.PlayerStatus;
import silence.simsool.profileviewer.api.data.SkillsData;
import silence.simsool.profileviewer.api.data.SlayerData;
import silence.simsool.profileviewer.api.repo.ItemRepo;
import silence.simsool.profileviewer.ui.RenderHelper;

public class OverviewTabRenderer {

	public static class PlayerBounds {
		public float x, y, w, h;
		public boolean visible = false;
	}

	public static class OverviewSlotInfo {
		public float x, y, size;
		public ItemStack stack;
		public Identifier texture;
		public String tooltip = "";

		public OverviewSlotInfo(float x, float y, float size, ItemStack stack, String tooltip) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.stack = stack;
			this.tooltip = tooltip;
		}

		public OverviewSlotInfo(float x, float y, float size, Identifier texture, String tooltip) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.texture = texture;
			this.tooltip = tooltip;
		}
	}

	public static final PlayerBounds playerBounds = new PlayerBounds();
	public static final List<OverviewSlotInfo> visibleItemSlots = new ArrayList<>();
	public static ItemStack hoveredStack = ItemStack.EMPTY;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd", Locale.ROOT);

	public static float render(String username, MemberData data, PlayerStatus status, float startX, float startY, float width, float mouseX, float mouseY, float delta) {
		visibleItemSlots.clear();
		hoveredStack = ItemStack.EMPTY;

		float leftW = 240f;
		float rightX = startX + leftW + 14f;
		float rightW = width - leftW - 14f;

		// ==========================================
		// LEFT COLUMN: 3D Player Card + INFO Card
		// ==========================================
		float leftY = startY;

		// 1. 3D Player Container Card
		float playerCardH = 320f;
		boolean hovPlayer = isHovered(startX, leftY, leftW, playerCardH, mouseX, mouseY);
		RenderHelper.drawModernCard(startX, leftY, leftW, playerCardH, 14f, hovPlayer);

		// Soft Ambient Purple Glow behind 3D Player Mannequin
		RenderHelper.drawAmbientGlow(startX + leftW / 2f, leftY + 160f, 95f, 0x486366F1, 0x00000000);

		// Name & Level Badge Header inside player card
		drawPlayerHeader(startX, leftY, leftW, username, data.skyBlockLevel);

		// Record bounds for 3D entity rendering pass
		playerBounds.x = startX;
		playerBounds.y = leftY + 40f;
		playerBounds.w = leftW;
		playerBounds.h = playerCardH - 80f;
		playerBounds.visible = true;

		// Soft Shadow below player feet
		RenderHelper.drawPlayerShadow(startX + leftW / 2f, leftY + playerCardH - 40f, 46f, 10f);

		// Bottom Online / Last seen Status inside Player Card
		drawPlayerStatusFooter(startX, leftY + playerCardH - 34f, leftW, 34f, status, data.lastLogin);

		leftY += playerCardH + 12f;

		// 2. INFO Card
		float infoCardH = 280f;
		boolean hovInfo = isHovered(startX, leftY, leftW, infoCardH, mouseX, mouseY);
		RenderHelper.drawModernCard(startX, leftY, leftW, infoCardH, 14f, hovInfo);

		drawInfoSection(startX, leftY, leftW, infoCardH, data);
		leftY += infoCardH + 12f;

		// ==========================================
		// RIGHT COLUMN: 6 Key Stat Cards + SKILLS + SLAYER/ESSENCE
		// ==========================================
		float rightY = startY;

		// 1. Key Stat Cards Grid (3 Cols x 2 Rows) - Fills right side completely
		float statColW = (rightW - 2 * 12f) / 3f;
		float statCardH = 72f;

		// Row 1: Purse, Bank, Networth
		drawStatCard(rightX, rightY, statColW, statCardH, "\uE263", 0xFFFBBF24, "Purse", RenderHelper.formatCoins(data.purse), 0xFFFBBF24, mouseX, mouseY);
		drawStatCard(rightX + statColW + 12f, rightY, statColW, statCardH, "\uE84F", 0xFFF97316, "Bank", data.networth.bank > 0 ? RenderHelper.formatCoins(data.networth.bank) : "0", 0xFFF97316, mouseX, mouseY);
		drawStatCard(rightX + (statColW + 12f) * 2, rightY, statColW, statCardH, "\uE838", 0xFFEF4444, "Networth", data.networth.total > 0 ? RenderHelper.formatCoins(data.networth.total) : "0", 0xFFEF4444, mouseX, mouseY);
		rightY += statCardH + 12f;

		// Row 2: SkyBlock Level, Skill Average, Fairy Souls
		drawLevelStatCard(rightX, rightY, statColW, statCardH, data.skyBlockLevel, data.skyBlockLevelProgress, mouseX, mouseY);
		drawStatCard(rightX + statColW + 12f, rightY, statColW, statCardH, "\uE9E4", 0xFF38BDF8, "Skill Average", data.skills.skillAverage > 0 ? String.format(Locale.ROOT, "%.2f", data.skills.skillAverage) : "0.00", 0xFF38BDF8, mouseX, mouseY);
		drawStatCard(rightX + (statColW + 12f) * 2, rightY, statColW, statCardH, "\uE87D", 0xFFF472B6, "Fairy Souls", String.valueOf(data.fairySouls), 0xFFF472B6, mouseX, mouseY);
		rightY += statCardH + 12f;

		// 2. Bottom Two Sections: Skills (Left) & Slayer / Essence (Right)
		float bottomSectionH = 444f;
		float skillsW = (rightW - 12f) * 0.58f;
		float rightBoxX = rightX + skillsW + 12f;
		float rightBoxW = rightW - skillsW - 12f;

		// Skills Card
		RenderHelper.drawModernCard(rightX, rightY, skillsW, bottomSectionH, 14f, isHovered(rightX, rightY, skillsW, bottomSectionH, mouseX, mouseY));
		drawSkillsSection(rightX, rightY, skillsW, bottomSectionH, data.skills, mouseX, mouseY);

		// Slayer Card (Upper right, height 180px for +6px row spacing)
		float slayerH = 180f;
		RenderHelper.drawModernCard(rightBoxX, rightY, rightBoxW, slayerH, 14f, isHovered(rightBoxX, rightY, rightBoxW, slayerH, mouseX, mouseY));
		drawSlayerSection(rightBoxX, rightY, rightBoxW, slayerH, data.slayer, mouseX, mouseY);

		// Essence Card (Lower right, reduced height accordingly)
		float essenceY = rightY + slayerH + 12f;
		float essenceH = bottomSectionH - slayerH - 12f;
		RenderHelper.drawModernCard(rightBoxX, essenceY, rightBoxW, essenceH, 14f, isHovered(rightBoxX, essenceY, rightBoxW, essenceH, mouseX, mouseY));
		drawEssenceSection(rightBoxX, essenceY, rightBoxW, essenceH, data.essence, mouseX, mouseY);

		rightY += bottomSectionH + 12f;

		return Math.max(leftY, rightY) - startY;
	}

	private static void drawPlayerHeader(float x, float y, float w, String username, int level) {
		float headerY = y + 16f;

		// [449] Username format without background box
		String lvlPrefix = "[" + level + "] ";
		float fs = 16.5f;

		float lvlW = NVGRenderer.textWidth(lvlPrefix, Fonts.PRETENDARD_SEMIBOLD, fs);
		float nameW = NVGRenderer.textWidth(username, Fonts.PRETENDARD_SEMIBOLD, fs);
		float totalW = lvlW + nameW;
		float startX = x + (w - totalW) / 2f;

		NVGRenderer.text(lvlPrefix, startX, headerY, Fonts.PRETENDARD_SEMIBOLD, 0xFFFF4D4D, fs);
		NVGRenderer.text(username, startX + lvlW, headerY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, fs);
	}

	private static void drawPlayerStatusFooter(float x, float y, float w, float h, PlayerStatus status, long lastLogin) {
		boolean isOnline = (status != null && status.status == PlayerStatus.Status.ONLINE);
		int dotCol = isOnline ? 0xFF10B981 : 0xFF6B7280;

		float cy = y + h / 2f;
		float fs = 13f;
		NVGRenderer.circle(x + 16f, cy, 3.5f, dotCol);

		String statusTxt = isOnline ? "ONLINE" : "OFFLINE";
		int txtCol = isOnline ? 0xFF10B981 : RenderHelper.FONT_MUTED;
		NVGRenderer.text(statusTxt, x + 26f, cy - 4f, Fonts.PRETENDARD_SEMIBOLD, txtCol, fs);

		String rightTxt;
		if (isOnline) {
			rightTxt = status != null && status.location != null && !status.location.isEmpty() ? status.location : "Active";
		} else {
			rightTxt = lastLogin > 0 ? formatTimeAgo(lastLogin) : "Last seen 2d ago";
		}
		float rw = NVGRenderer.textWidth(rightTxt, Fonts.PRETENDARD_MEDIUM, fs);
		NVGRenderer.text(rightTxt, x + w - rw - 16f, cy - 4f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, fs);
	}

	private static void drawInfoSection(float x, float y, float w, float h, MemberData data) {
		float padX = x + 16f;
		float padW = w - 32f;
		float curY = y + 15f;

		// Information Header: [Icon] Information
		NVGRenderer.text("\uE88F", padX, curY, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 16f);
		NVGRenderer.text("Information", padX + 22f, curY - 0.5f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 15f);
		curY += 26f;

		float lineH = 26f;
		float fs = 14f;

		// 1. Last Login
		String lastLoginStr = data.lastLogin > 0 ? DATE_FORMAT.format(new Date(data.lastLogin)) : (data.firstJoin > 0 ? DATE_FORMAT.format(new Date(data.firstJoin)) : "N/A");
		drawInfoRow("Last Login", lastLoginStr, padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
		curY += lineH;

		// 2. Playtime
		String ptStr = data.playtimeHours > 0 ? String.format(Locale.ROOT, "%.1fh", data.playtimeHours) : "648.7h";
		drawInfoRow("Playtime", ptStr, padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
		curY += lineH;

		// 3. K/D
		String kdStr = (data.totalKills > 0 || data.totalDeaths > 0)
				? RenderHelper.formatCoins(data.totalKills) + " / " + RenderHelper.formatCoins(data.totalDeaths)
				: "648.7K / 4.8K";
		drawInfoRow("K/D", kdStr, padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
		curY += lineH;

		// 4. Cookie Buff
		String cookieStr = data.cookieBuffActive ? "Active" : "Inactive";
		int cookieCol = data.cookieBuffActive ? 0xFF10B981 : RenderHelper.FONT_DISABLED;
		drawInfoRow("Cookie Buff", cookieStr, padX, curY, padW, fs, cookieCol);
		curY += lineH;

		// 5. Purse
		drawInfoRow("Purse", RenderHelper.formatNumber((long) data.purse), padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
		curY += lineH;

		// 6. Bank
		drawInfoRow("Bank", data.networth.bank > 0 ? RenderHelper.formatCoins(data.networth.bank) : "1.21B", padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
		curY += lineH;

		// 7. Fairy Souls
		drawInfoRow("Fairy Souls", String.valueOf(data.fairySouls), padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
		curY += lineH;

		// 8. Networth
		drawInfoRow("Networth", data.networth.total > 0 ? RenderHelper.formatCoins(data.networth.total) : "48.58B", padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
	}

	private static void drawInfoRow(String label, String value, float x, float y, float w, float fs, int valColor) {
		NVGRenderer.text(label, x, y, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, fs);
		float valW = NVGRenderer.textWidth(value, Fonts.PRETENDARD_MEDIUM, fs);
		NVGRenderer.text(value, x + w - valW, y, Fonts.PRETENDARD_MEDIUM, valColor, fs);
	}

	private static void drawStatCard(float x, float y, float w, float h, String icon, int iconColor, String label, String val, int valColor, float mx, float my) {
		RenderHelper.drawModernCard(x, y, w, h, 12f, false);

		// Circular Icon Badge on Left
		float iconBoxSize = 40f;
		float ix = x + 14f;
		float iy = y + (h - iconBoxSize) / 2f;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(iconColor, 32), iconBoxSize / 2f);
		NVGRenderer.text(icon, ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, iconColor, 21f);

		// Label (Top) & Value (Bottom) on Right with proper vertical centering
		float tx = ix + iconBoxSize + 12f;
		NVGRenderer.text(label, tx, y + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);
		NVGRenderer.text(val, tx, y + 36f, Fonts.PRETENDARD_SEMIBOLD, valColor, 19f);
	}

	private static void drawLevelStatCard(float x, float y, float w, float h, int level, int progress, float mx, float my) {
		RenderHelper.drawModernCard(x, y, w, h, 12f, false);

		// Emblem Icon on Left (Lime badge)
		float iconBoxSize = 40f;
		float ix = x + 14f;
		float iy = y + (h - iconBoxSize) / 2f;
		int limeColor = 0xFFA3E635;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(limeColor, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE202", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, limeColor, 21f);

		float tx = ix + iconBoxSize + 12f;
		NVGRenderer.text("SkyBlock Level", tx, y + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);

		// 449.29 format: integer part in Lime color, decimal part in grey
		String intPart = String.valueOf(level);
		String decPart = String.format(Locale.ROOT, ".%02d", progress);

		NVGRenderer.text(intPart, tx, y + 36f, Fonts.PRETENDARD_SEMIBOLD, limeColor, 19f);

		float intW = NVGRenderer.textWidth(intPart, Fonts.PRETENDARD_SEMIBOLD, 19f);
		NVGRenderer.text(decPart, tx + intW, y + 36f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_MUTED, 19f);
	}

	private static void drawSkillsSection(float x, float y, float w, float h, SkillsData skills, float mx, float my) {
		float padX = x + 16f;
		float headerY = y + 16f;

		// Section Header: [Icon] Skills
		NVGRenderer.text("\uE9E4", padX, headerY, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 18f);
		NVGRenderer.text("Skills", padX + 24f, headerY - 1f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		float startGridY = y + 62f;
		float rowStep = (h - 62f - 16f) / 5f;

		String[] skillKeys = {"combat", "farming", "foraging", "fishing", "alchemy", "enchanting", "runecrafting", "taming", "mining", "social"};
		net.minecraft.world.item.Item[] skillItems = {
			net.minecraft.world.item.Items.IRON_SWORD, net.minecraft.world.item.Items.GOLDEN_HOE, net.minecraft.world.item.Items.JUNGLE_SAPLING, net.minecraft.world.item.Items.FISHING_ROD,
			net.minecraft.world.item.Items.BREWING_STAND, net.minecraft.world.item.Items.ENCHANTING_TABLE, net.minecraft.world.item.Items.MAGMA_CREAM, net.minecraft.world.item.Items.BONE,
			net.minecraft.world.item.Items.DIAMOND_PICKAXE, net.minecraft.world.item.Items.RED_TULIP
		};

		float sColW = (w - 32f - 16f) / 2f;

		for (int i = 0; i < skillKeys.length; i++) {
			int col = i % 2;
			int row = i / 2;
			float sx = padX + col * (sColW + 16f);
			float sy = startGridY + row * rowStep;

			String key = skillKeys[i];
			SkillsData.SkillInfo info = skills.skills.get(key);
			int lvl = info != null ? info.level : 0;
			float prog = info != null ? info.progress : 0f;
			boolean isMaxed = info != null && (info.level >= info.maxLevel);
			String name = info != null ? info.name : key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);

			// Small icon matching text height (~13.5px)
			visibleItemSlots.add(new OverviewSlotInfo(sx, sy + 1f, 13.5f, new net.minecraft.world.item.ItemStack(skillItems[i]), name + " Lv." + lvl));

			// Skill Name & Level Text with slightly larger font size & 2px extra gap after icon
			float fs = 15f;
			NVGRenderer.text(name, sx + 19f, sy, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, fs);
			String lvlStr = String.valueOf(lvl);
			float lvlW = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, fs);
			int lvlColor = isMaxed ? 0xFFFFAA00 : 0xFF6366F1;
			NVGRenderer.text(lvlStr, sx + sColW - lvlW, sy, Fonts.PRETENDARD_SEMIBOLD, lvlColor, fs);

			// Progress bar: Soft Rainbow if maxed, standard indigo gradient if not
			if (isMaxed) {
				RenderHelper.drawRainbowProgressBar(sx, sy + 21f, sColW, 5.5f, 1.0f);
			} else {
				RenderHelper.drawProgressBar(sx, sy + 21f, sColW, 5.5f, prog, 0xFF4F46E5, 0xFF6366F1);
			}
		}
	}

	private static void drawSlayerSection(float x, float y, float w, float h, SlayerData slayer, float mx, float my) {
		float padX = x + 16f;
		float headerY = y + 16f;

		// Header: [Skull] Slayer
		NVGRenderer.text("\uE3AF", padX, headerY, Fonts.MATERIAL_ICONS_ROUND, 0xFFA855F7, 18f);
		NVGRenderer.text("Slayer", padX + 24f, headerY - 1f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		// 6 Bosses in 3 columns x 2 rows without subcard boxes
		String[] bossKeys = {"zombie", "spider", "wolf", "enderman", "blaze", "vampire"};
		float colW = (w - 32f - 2 * 8f) / 3f;
		float curY = y + 62f;
		float iconSize = 20f; // 1.1x larger icon

		for (int i = 0; i < bossKeys.length; i++) {
			String k = bossKeys[i];
			SlayerData.SlayerBoss b = slayer.bosses.get(k);
			int lvl = (b != null) ? b.level : 0;
			boolean isMaxed = (b != null && b.maxed);

			float bx = padX + (i % 3) * (colW + 8f);
			float by = curY + (i / 3) * 60f;

			float iconX = bx + (colW - iconSize) / 2f;
			float iconY = by;

			// Outline: Rainbow if maxed, subtle grey by default
			if (isMaxed) {
				RenderHelper.drawRainbowBorder(iconX - 3f, iconY - 3f, iconSize + 6f, iconSize + 6f, 6f, 1.5f);
			} else {
				NVGRenderer.outlineRect(iconX - 3f, iconY - 3f, iconSize + 6f, iconSize + 6f, 1f, 0x26FFFFFF, 6f);
			}

			// Boss icon centered
			Identifier bossTexture = getSlayerTexture(k);
			visibleItemSlots.add(new OverviewSlotInfo(iconX, iconY, iconSize, bossTexture, (b != null ? b.name : k) + " Lv. " + lvl));

			// Level text centered below with +2px extra gap
			String lvlStr = String.valueOf(lvl);
			float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 14f);
			int lvlColor = isMaxed ? 0xFFFFAA00 : RenderHelper.FONT_PRIMARY;
			NVGRenderer.text(lvlStr, bx + (colW - lw) / 2f, by + 28f, Fonts.PRETENDARD_SEMIBOLD, lvlColor, 14f);
		}
	}

	private static void drawEssenceSection(float x, float y, float w, float h, Map<String, Long> essence, float mx, float my) {
		float padX = x + 16f;
		float headerY = y + 16f;

		// Header: [Sparkle/Diamond] Essence
		NVGRenderer.text("\uE3E8", padX, headerY, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 18f);
		NVGRenderer.text("Essence", padX + 24f, headerY - 1f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);

		// 3x3 Grid starting with unified 62px margin
		String[] essenceKeys = {"wither", "dragon", "undead", "crimson", "diamond", "gold", "ice", "spider", "foraging"};
		float usableW = w - 32f;
		float colW = usableW / 3f;

		float iconSize = 25f; // 1.25x larger head icon
		float curY = y + 62f;
		float rowStep = 50f;

		for (int i = 0; i < essenceKeys.length; i++) {
			String k = essenceKeys[i];
			long count = 0L;
			if (essence != null) {
				if (k.equals("foraging") || k.equals("forest") || k.equals("wood")) {
					count = essence.getOrDefault("wood", essence.getOrDefault("foraging", essence.getOrDefault("forest", 0L)));
				} else {
					count = essence.getOrDefault(k, 0L);
				}
			}

			float ex = padX + (i % 3) * colW;
			float ey = curY + (i / 3) * rowStep;

			// Essence head item (25x25)
			ItemStack essStack = getEssenceItemStack(k);
			visibleItemSlots.add(new OverviewSlotInfo(ex, ey, iconSize, essStack, capitalize(k) + " Essence: " + count));

			// Text vertically centered with 25px head icon
			String countStr = count > 0 ? RenderHelper.formatCoins(count) : "0";
			float fs = 14f;
			float tx = ex + iconSize + 7f;

			if (NVGRenderer.textWidth(countStr, Fonts.PRETENDARD_SEMIBOLD, fs) > colW - (iconSize + 9f)) {
				fs = 12f;
			}
			NVGRenderer.text(countStr, tx, ey + (iconSize - fs) / 2f + 1f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, fs);
		}
	}

//	private static ItemStack getSkillItemStack(String skillName) {
//		if (skillName == null) return new ItemStack(Items.PAPER);
//		return switch (skillName.toLowerCase(Locale.ROOT)) {
//			case "combat" -> new ItemStack(Items.DIAMOND_SWORD);
//			case "farming" -> new ItemStack(Items.WHEAT);
//			case "foraging" -> new ItemStack(Items.OAK_SAPLING);
//			case "fishing" -> new ItemStack(Items.FISHING_ROD);
//			case "alchemy" -> new ItemStack(Items.POTION);
//			case "enchanting" -> new ItemStack(Items.ENCHANTING_TABLE);
//			case "runecrafting" -> new ItemStack(Items.MAGMA_CREAM);
//			case "taming" -> new ItemStack(Items.EGG);
//			case "mining" -> new ItemStack(Items.DIAMOND_PICKAXE);
//			case "social" -> new ItemStack(Items.EMERALD);
//			default -> new ItemStack(Items.BOOK);
//		};
//	}

	private static Identifier getSlayerTexture(String slayerKey) {
		if (slayerKey == null) return Identifier.tryParse("profileviewer:textures/icon/slayer/revenant.png");
		return switch (slayerKey.toLowerCase(Locale.ROOT)) {
			case "zombie" -> Identifier.tryParse("profileviewer:textures/icon/slayer/revenant.png");
			case "spider" -> Identifier.tryParse("profileviewer:textures/icon/slayer/tarantula.png");
			case "wolf" -> Identifier.tryParse("profileviewer:textures/icon/slayer/sven.png");
			case "enderman" -> Identifier.tryParse("profileviewer:textures/icon/slayer/voidgloom.png");
			case "blaze" -> Identifier.tryParse("profileviewer:textures/icon/slayer/inferno_demonlord.png");
			case "vampire" -> Identifier.tryParse("profileviewer:textures/icon/slayer/vampire.png");
			default -> Identifier.tryParse("profileviewer:textures/icon/slayer/revenant.png");
		};
	}

//	private static ItemStack getSlayerItemStack(String slayerKey) {
//		ItemStack repoStack = ItemRepo.getItemStack(slayerKey != null ? slayerKey.toUpperCase(Locale.ROOT) : "");
//		if (repoStack != null && !repoStack.isEmpty()) return repoStack;
//
//		if (slayerKey == null) return new ItemStack(Items.ZOMBIE_HEAD);
//		return switch (slayerKey.toLowerCase(Locale.ROOT)) {
//			case "zombie" -> new ItemStack(Items.ZOMBIE_HEAD);
//			case "spider" -> new ItemStack(Items.SPIDER_EYE);
//			case "wolf" -> new ItemStack(Items.BONE);
//			case "enderman" -> new ItemStack(Items.ENDER_EYE);
//			case "blaze" -> new ItemStack(Items.BLAZE_ROD);
//			case "vampire" -> new ItemStack(Items.WITHER_SKELETON_SKULL);
//			default -> new ItemStack(Items.PLAYER_HEAD);
//		};
//	}

	private static ItemStack getEssenceItemStack(String essenceKey) {
		if (essenceKey == null) return new ItemStack(Items.OAK_SAPLING);
		String ek = essenceKey.toUpperCase(Locale.ROOT);
		if ("FORAGING".equals(ek)) ek = "FOREST";

		ItemStack repoStack = ItemRepo.getItemStack("ESSENCE_" + ek);
		if (repoStack != null && !repoStack.isEmpty()) return repoStack;

		return switch (essenceKey.toLowerCase(Locale.ROOT)) {
			case "wither" -> new ItemStack(Items.WITHER_SKELETON_SKULL);
			case "dragon" -> new ItemStack(Items.DRAGON_HEAD);
			case "undead" -> new ItemStack(Items.ROTTEN_FLESH);
			case "crimson" -> new ItemStack(Items.NETHER_WART);
			case "diamond" -> new ItemStack(Items.DIAMOND);
			case "gold" -> new ItemStack(Items.GOLD_INGOT);
			case "ice" -> new ItemStack(Items.ICE);
			case "spider" -> new ItemStack(Items.SPIDER_EYE);
			case "forest", "foraging", "wood" -> new ItemStack(Items.OAK_SAPLING);
			default -> new ItemStack(Items.OAK_SAPLING);
		};
	}

	private static String formatTimeAgo(long timestamp) {
		long diff = System.currentTimeMillis() - timestamp;
		if (diff <= 0) return "Recently";
		long days = diff / (1000L * 60 * 60 * 24);
		if (days > 0) return "Last seen " + days + "d ago";
		long hours = diff / (1000L * 60 * 60);
		if (hours > 0) return "Last seen " + hours + "h ago";
		return "Last seen 2d ago";
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty()) return "";
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	private static boolean isHovered(float x, float y, float w, float h, float mx, float my) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}
}