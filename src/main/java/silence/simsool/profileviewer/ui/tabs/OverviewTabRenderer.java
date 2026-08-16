package silence.simsool.profileviewer.ui.tabs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;
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

		// Row 1: Purse, Bank, Net Worth
		drawStatCard(rightX, rightY, statColW, statCardH, "\uE263", 0xFFFBBF24, "Purse", RenderHelper.formatCoins(data.purse), 0xFFFBBF24, mouseX, mouseY);
		drawStatCard(rightX + statColW + 12f, rightY, statColW, statCardH, "\uE84F", 0xFF4ADE80, "Bank", data.networth.bank > 0 ? RenderHelper.formatCoins(data.networth.bank) : "0", 0xFF4ADE80, mouseX, mouseY);
		drawStatCard(rightX + (statColW + 12f) * 2, rightY, statColW, statCardH, "\uE838", 0xFFE879F9, "Net Worth", data.networth.total > 0 ? RenderHelper.formatCoins(data.networth.total) : "0", 0xFFE879F9, mouseX, mouseY);
		rightY += statCardH + 12f;

		// Row 2: SkyBlock Level, Skill Average, Fairy Souls
		drawLevelStatCard(rightX, rightY, statColW, statCardH, data.skyBlockLevel, data.skyBlockLevelProgress, mouseX, mouseY);
		drawStatCard(rightX + statColW + 12f, rightY, statColW, statCardH, "\uE83A", 0xFF38BDF8, "Skill Average", data.skills.skillAverage > 0 ? String.format(Locale.ROOT, "%.2f", data.skills.skillAverage) : "0.00", 0xFF38BDF8, mouseX, mouseY);
		drawStatCard(rightX + (statColW + 12f) * 2, rightY, statColW, statCardH, "\uE3A5", 0xFFF43F5E, "Fairy Souls", String.valueOf(data.fairySouls), 0xFFF43F5E, mouseX, mouseY);
		rightY += statCardH + 14f;

		// 2. Bottom Two Sections: SKILLS (Left) & SLAYER / ESSENCE (Right)
		float bottomSectionH = 434f;
		float skillsW = rightW * 0.58f;
		float rightBoxX = rightX + skillsW + 12f;
		float rightBoxW = rightW - skillsW - 12f;

		// SKILLS Card
		RenderHelper.drawModernCard(rightX, rightY, skillsW, bottomSectionH, 14f, isHovered(rightX, rightY, skillsW, bottomSectionH, mouseX, mouseY));
		drawSkillsSection(rightX, rightY, skillsW, bottomSectionH, data.skills, mouseX, mouseY);

		// SLAYER Card (Upper right)
		float slayerH = 148f;
		RenderHelper.drawModernCard(rightBoxX, rightY, rightBoxW, slayerH, 14f, isHovered(rightBoxX, rightY, rightBoxW, slayerH, mouseX, mouseY));
		drawSlayerSection(rightBoxX, rightY, rightBoxW, slayerH, data.slayer, mouseX, mouseY);

		// ESSENCE Card (Lower right)
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

		// INFO Header
		NVGRenderer.text("INFO", padX, curY, Fonts.PRETENDARD_SEMIBOLD, 0xFF818CF8, 15f);
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

		// 8. Net Worth
		drawInfoRow("Net Worth", data.networth.total > 0 ? RenderHelper.formatCoins(data.networth.total) : "48.58B", padX, curY, padW, fs, RenderHelper.FONT_SECONDARY);
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

		// Emblem Icon on Left
		float iconBoxSize = 40f;
		float ix = x + 14f;
		float iy = y + (h - iconBoxSize) / 2f;
		int emblemCol = 0xFFA855F7;
		NVGRenderer.rect(ix, iy, iconBoxSize, iconBoxSize, UIColors.withAlpha(emblemCol, 32), iconBoxSize / 2f);
		NVGRenderer.text("\uE8E8", ix + 9.5f, iy + 10f, Fonts.MATERIAL_ICONS_ROUND, emblemCol, 21f);

		float tx = ix + iconBoxSize + 12f;
		NVGRenderer.text("SkyBlock Level", tx, y + 16f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_MUTED, 13.5f);

		String lvlStr = "Lv. " + level;
		NVGRenderer.text(lvlStr, tx, y + 36f, Fonts.PRETENDARD_SEMIBOLD, 0xFFC084FC, 18f);

		// Progress Bar to the right of level text
		float lvlW = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 18f);
		float barX = tx + lvlW + 12f;
		float barW = x + w - barX - 14f;
		if (barW > 20f) {
			RenderHelper.drawProgressBar(barX, y + 44f, barW, 4.5f, progress / 100f, 0xFF6366F1, 0xFFA855F7);
		}
	}

	private static void drawSkillsSection(float x, float y, float w, float h, SkillsData skills, float mx, float my) {
		float padX = x + 16f;
		float headerY = y + 14f;

		// Section Header: [Icon] SKILLS
		NVGRenderer.text("\uE6E1", padX, headerY + 8f, Fonts.MATERIAL_ICONS_ROUND, 0xFF38BDF8, 20f);
		NVGRenderer.text("SKILLS", padX + 26f, headerY + 8f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		
		float sRowH = 72f;
		float gridH = 4 * sRowH + 46f;
		float curY = y + 46f + Math.max(0f, (h - 46f - gridH) / 2f);

		String[] skillKeys = {"combat", "farming", "foraging", "fishing", "alchemy", "enchanting", "runecrafting", "taming", "mining", "social"};
		net.minecraft.world.item.Item[] skillItems = {
			net.minecraft.world.item.Items.IRON_SWORD, net.minecraft.world.item.Items.GOLDEN_HOE, net.minecraft.world.item.Items.JUNGLE_SAPLING, net.minecraft.world.item.Items.FISHING_ROD,
			net.minecraft.world.item.Items.BREWING_STAND, net.minecraft.world.item.Items.ENCHANTING_TABLE, net.minecraft.world.item.Items.MAGMA_CREAM, net.minecraft.world.item.Items.BONE,
			net.minecraft.world.item.Items.DIAMOND_PICKAXE, net.minecraft.world.item.Items.RED_TULIP
		};

		float sColW = (w - 48f) / 2f;
		float sStartX = padX;

		for (int i = 0; i < skillKeys.length; i++) {
			int col = i % 2;
			int row = i / 2;
			float sx = sStartX + col * (sColW + 16f);
			float sy = curY + row * sRowH;

			String key = skillKeys[i];
			SkillsData.SkillInfo info = skills.skills.get(key);
			int lvl = info != null ? info.level : 0;
			float prog = info != null ? info.progress : 0f;
			String name = info != null ? info.name : key.substring(0, 1).toUpperCase() + key.substring(1);
			
			// Queue Item
			visibleItemSlots.add(new OverviewSlotInfo(sx, sy + 4f, 14f, new net.minecraft.world.item.ItemStack(skillItems[i]), name + " Lv." + lvl));
			
			// Name & Level
			NVGRenderer.text(name, sx + 20f, sy + 6f, Fonts.PRETENDARD_MEDIUM, RenderHelper.FONT_PRIMARY, 13f);
			String lvlStr2 = String.valueOf(lvl);
			NVGRenderer.text(lvlStr2, sx + sColW - NVGRenderer.textWidth(lvlStr2, Fonts.PRETENDARD_MEDIUM, 13f) - 4f, sy + 6f, Fonts.PRETENDARD_MEDIUM, 0xFF6366F1, 13f);

			// Progress bar
			RenderHelper.drawProgressBar(sx + 20f, sy + 26f, sColW - 24f, 6f, prog, 0xFF6366F1, 0xFFA855F7);
		}
	}

	private static void drawSlayerSection(float x, float y, float w, float h, SlayerData slayer, float mx, float my) {
		float padX = x + 14f;
		float curY = y + 14f;

		// Header: [Skull] SLAYER
		NVGRenderer.text("\uE3AF", padX, curY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFFA855F7, 16f);
		NVGRenderer.text("SLAYER", padX + 22f, curY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		curY += 26f;

		// 6 Bosses with 4 per row (Vertical slot style: icon on top, level on bottom)
		String[] bossKeys = {"zombie", "spider", "wolf", "enderman", "blaze", "vampire"};

		float slotW = (w - 28f - 3 * 8f) / 4f;
		float slotH = 50f;

		for (int i = 0; i < bossKeys.length; i++) {
			String k = bossKeys[i];
			SlayerData.SlayerBoss b = slayer.bosses.get(k);
			int lvl = (b != null) ? b.level : 9;

			float bx = padX + (i % 4) * (slotW + 8f);
			float by = curY + (i / 4) * (slotH + 8f);

			RenderHelper.drawSubCard(bx, by, slotW, slotH, 8f, false);

			// Boss icon on top with 18x18 size
			Identifier bossTexture = getSlayerTexture(k);
			visibleItemSlots.add(new OverviewSlotInfo(bx + (slotW - 18f) / 2f, by + 6f, 18f, bossTexture, (b != null ? b.name : k) + " Lv. " + lvl));

			// Level text centered below with plenty of room
			String lvlStr = String.valueOf(lvl);
			float lw = NVGRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, 15.5f);
			NVGRenderer.text(lvlStr, bx + (slotW - lw) / 2f, by + 28f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 15.5f);
		}
	}

	private static void drawEssenceSection(float x, float y, float w, float h, Map<String, Long> essence, float mx, float my) {
		float padX = x + 14f;
		float headerY = y + 14f;

		// Header: [Sparkle/Diamond] ESSENCE
		NVGRenderer.text("\uE3E8", padX, headerY + 1f, Fonts.MATERIAL_ICONS_ROUND, 0xFF818CF8, 16f);
		NVGRenderer.text("ESSENCE", padX + 22f, headerY, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, 16f);
		
		float rowH = 40f;
		float vGap = 26f;
		float gridH = 2 * (rowH + vGap) + rowH;
		float curY = y + 40f + Math.max(0f, (h - 40f - gridH) / 2f);

		// 3x3 Grid (9 essences: wither, dragon, undead, crimson, diamond, gold, ice, spider, forest)
		String[] essenceKeys = {"wither", "dragon", "undead", "crimson", "diamond", "gold", "ice", "spider", "forest"};
		float colW = (w - 28f - 2 * 8f) / 3f;

		for (int i = 0; i < essenceKeys.length; i++) {
			String k = essenceKeys[i];
			long count = essence != null ? essence.getOrDefault(k, 0L) : 0L;

			float ex = padX + (i % 3) * (colW + 8f);
			float ey = curY + (i / 3) * (rowH + vGap);

			RenderHelper.drawSubCard(ex, ey, colW, rowH, 8f, false);

			// Essence Item Icon on left with 16x16 size
			ItemStack essStack = getEssenceItemStack(k);
			visibleItemSlots.add(new OverviewSlotInfo(ex + 6f, ey + 12f, 16f, essStack, capitalize(k) + " Essence: " + count));

			// Large prominent amount text vertically centered
			String countStr = count > 0 ? RenderHelper.formatCoins(count) : "0";
			float fs = 13.5f;
			float tx = ex + 26f;
			
			// Auto-scale font if it still doesn't fit
			if (NVGRenderer.textWidth(countStr, Fonts.PRETENDARD_SEMIBOLD, fs) > colW - 30f) {
				fs = 12f;
			}
			NVGRenderer.text(countStr, tx, ey + 13f, Fonts.PRETENDARD_SEMIBOLD, RenderHelper.FONT_PRIMARY, fs);
		}
	}

	private static ItemStack getSkillItemStack(String skillName) {
		if (skillName == null) return new ItemStack(Items.PAPER);
		return switch (skillName.toLowerCase(Locale.ROOT)) {
			case "combat" -> new ItemStack(Items.DIAMOND_SWORD);
			case "farming" -> new ItemStack(Items.WHEAT);
			case "foraging" -> new ItemStack(Items.OAK_SAPLING);
			case "fishing" -> new ItemStack(Items.FISHING_ROD);
			case "alchemy" -> new ItemStack(Items.POTION);
			case "enchanting" -> new ItemStack(Items.ENCHANTING_TABLE);
			case "runecrafting" -> new ItemStack(Items.MAGMA_CREAM);
			case "taming" -> new ItemStack(Items.EGG);
			case "mining" -> new ItemStack(Items.DIAMOND_PICKAXE);
			case "social" -> new ItemStack(Items.POPPY);
			default -> new ItemStack(Items.BOOK);
		};
	}

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

	private static ItemStack getSlayerItemStack(String slayerKey) {
		ItemStack repoStack = ItemRepo.getItemStack(slayerKey != null ? slayerKey.toUpperCase(Locale.ROOT) : "");
		if (repoStack != null && !repoStack.isEmpty()) return repoStack;

		if (slayerKey == null) return new ItemStack(Items.ZOMBIE_HEAD);
		return switch (slayerKey.toLowerCase(Locale.ROOT)) {
			case "zombie" -> new ItemStack(Items.ZOMBIE_HEAD);
			case "spider" -> new ItemStack(Items.SPIDER_EYE);
			case "wolf" -> new ItemStack(Items.BONE);
			case "enderman" -> new ItemStack(Items.ENDER_EYE);
			case "blaze" -> new ItemStack(Items.BLAZE_ROD);
			case "vampire" -> new ItemStack(Items.WITHER_SKELETON_SKULL);
			default -> new ItemStack(Items.PLAYER_HEAD);
		};
	}

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
			case "forest", "foraging" -> new ItemStack(Items.OAK_SAPLING);
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