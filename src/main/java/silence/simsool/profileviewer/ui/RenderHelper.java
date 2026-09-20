package silence.simsool.profileviewer.ui;

import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.skija.Shader;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import silence.simsool.lucent.skija.compositor.SkijaCompositor;
import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.skija.Fonts;
import silence.simsool.lucent.ui.utils.skija.SkijaRenderer;

public class RenderHelper {

	// Font Color Guide (Dark background contrast >= 4.5:1, 5 levels)
	public static final int FONT_PRIMARY   = 0xFFFFFFFF; // High emphasis / Headings / White
	public static final int FONT_SECONDARY = 0xFFD1D5DB; // Body text / Sub-headings (~11:1)
	public static final int FONT_MUTED     = 0xFF9CA3AF; // Secondary labels / Captions (~6.5:1)
	public static final int FONT_HINT      = 0xFF999999; // Subtle hints / Special hierarchy
	public static final int FONT_DISABLED  = 0xFF6B7280; // Disabled state

	// Font Size Guide (User Requested)
	// Headlines (Titles): 36–44
	// Subheadings (Headers): 28–36
	// Body Text: 24–30
	// Quotes & Callouts: 28+
	// Data Labels (on charts): 18–24
	// Footnotes & Captions: 18–20
	public static final float FS_TITLE   = 36f; // Headlines (Titles)
	public static final float FS_H1      = 36f;
	public static final float FS_H2      = 28f; // Subheadings (Headers)
	public static final float FS_H3      = 24f;
	public static final float FS_BODY    = 24f; // Body Text: 24-30px
	public static final float FS_LABEL   = 20f; // Data Labels (18-24px)
	public static final float FS_BUTTON  = 20f;
	public static final float FS_CAPTION = 18f; // Footnotes & Captions: 18-20px

	public static String formatCoins(double coins) {
		if (coins >= 1_000_000_000) return String.format("%.2fB", coins / 1_000_000_000.0);
		if (coins >= 1_000_000) return String.format("%.2fM", coins / 1_000_000.0);
		if (coins >= 1_000) return String.format("%.1fK", coins / 1_000.0);
		return String.format("%,.0f", coins);
	}

	public static int getSkyBlockLevelColor(int level) {
		if (level < 40) return 0xFFAAAAAA;
		if (level < 80) return 0xFFFFFFFF;
		if (level < 120) return 0xFFFFFF55;
		if (level < 160) return 0xFF55FF55;
		if (level < 200) return 0xFF00AA00;
		if (level < 240) return 0xFF55FFFF;
		if (level < 280) return 0xFF00AAAA;
		if (level < 320) return 0xFF5555FF;
		if (level < 360) return 0xFFFF55FF;
		if (level < 400) return 0xFFAA00AA;
		if (level < 440) return 0xFFFFAA00;
		if (level < 480) return 0xFFFF5555;
		return 0xFFAA0000;
	}

	public static void drawNameplate(String username, int level, float x, float y, float w, float h) {
		SkijaRenderer.rect(x, y, w, h, 0xCC181A22, 6f);
		SkijaRenderer.outlineRect(x, y, w, h, 1f, 0x33FFFFFF, 6f);

		String prefix = "[";
		String lvlStr = String.valueOf(level);
		String suffix = "] ";
		String name = (username != null && !username.isEmpty()) ? username : "Player";

		float fs = FS_BODY;
		float pW = SkijaRenderer.textWidth(prefix, Fonts.PRETENDARD_SEMIBOLD, fs);
		float lW = SkijaRenderer.textWidth(lvlStr, Fonts.PRETENDARD_SEMIBOLD, fs);
		float sW = SkijaRenderer.textWidth(suffix, Fonts.PRETENDARD_SEMIBOLD, fs);
		float nW = SkijaRenderer.textWidth(name, Fonts.PRETENDARD_SEMIBOLD, fs);
		float totalW = pW + lW + sW + nW;

		float startX = x + (w - totalW) / 2f;
		float textY = y + (h - fs) / 2f + 1f;

		int lvlCol = getSkyBlockLevelColor(level);
		SkijaRenderer.text(prefix, startX, textY, Fonts.PRETENDARD_SEMIBOLD, 0xFF6B7280, fs);
		SkijaRenderer.text(lvlStr, startX + pW, textY, Fonts.PRETENDARD_SEMIBOLD, lvlCol, fs);
		SkijaRenderer.text(suffix, startX + pW + lW, textY, Fonts.PRETENDARD_SEMIBOLD, 0xFF6B7280, fs);
		SkijaRenderer.text(name, startX + pW + lW + sW, textY, Fonts.PRETENDARD_SEMIBOLD, FONT_PRIMARY, fs);
	}

	public static String formatNumber(long num) {
		if (num >= 1_000_000_000) return String.format("%.2fB", num / 1_000_000_000.0);
		if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0);
		if (num >= 1_000) return String.format("%.1fK", num / 1_000.0);
		return String.format("%,d", num);
	}

	public static void drawModernCard(float x, float y, float w, float h, float radius, boolean hovered) {
		SkijaRenderer.rect(x, y, w, h, 0xC80D0F18, radius);
		SkijaRenderer.outlineRect(x, y, w, h, 1f, 0x18FFFFFF, radius);
	}

	public static void drawSubCard(float x, float y, float w, float h, float radius, boolean hovered) {
		SkijaRenderer.rect(x, y, w, h, 0xD0121422, radius);
		SkijaRenderer.outlineRect(x, y, w, h, 1f, 0x12FFFFFF, radius);
	}

	public static void drawAmbientGlow(float cx, float cy, float radius, int innerColor, int outerColor) {
		SkijaCompositor.INSTANCE.enqueue(canvas -> {
			try (Shader shader = Shader.makeRadialGradient(cx, cy, radius, new int[]{innerColor, outerColor});
				 Paint paint = new Paint()) {
				paint.setShader(shader);
				paint.setAntiAlias(true);
				canvas.drawCircle(cx, cy, radius, paint);
			}
		});
	}

	public static void drawPlayerShadow(float cx, float cy, float rx, float ry) {
		SkijaCompositor.INSTANCE.enqueue(canvas -> {
			try (Shader shader = Shader.makeRadialGradient(cx, cy, rx, new int[]{0xA0000000, 0x00000000});
				 Paint paint = new Paint()) {
				paint.setShader(shader);
				paint.setAntiAlias(true);
				canvas.drawOval(Rect.makeXYWH(cx - rx, cy - ry, rx * 2f, ry * 2f), paint);
			}
		});
	}

	public static void drawProgressBar(float x, float y, float w, float h, float progress, int fillColor) {
		drawProgressBar(x, y, w, h, progress, fillColor, fillColor);
	}

	public static void drawProgressBar(float x, float y, float w, float h, float progress, int startCol, int endCol) {
		SkijaRenderer.rect(x, y, w, h, 0x44000000, h / 2f);
		float fillW = Math.max(0, Math.min(w, w * progress));
		if (fillW > 0) {
			SkijaRenderer.rect(x, y, fillW, h, startCol, h / 2f);
		}
	}

	public static void drawRainbowProgressBar(float x, float y, float w, float h, float progress) {
		SkijaRenderer.rect(x, y, w, h, 0x44000000, h / 2f);
		float fillW = Math.max(0, Math.min(w, w * progress));
		if (fillW <= 0) return;

		int[] colors = {
			0xFFF472B6, // Soft Rose
			0xFFFBBF24, // Soft Amber
			0xFF34D399, // Soft Mint
			0xFF38BDF8, // Soft Sky Blue
			0xFFA78BFA, // Soft Lavender
			0xFFF472B6  // Soft Rose loop
		};
		int numSegs = colors.length - 1;
		float segW = fillW / numSegs;

		for (int i = 0; i < numSegs; i++) {
			float sx = x + i * segW;
			float curSegW = (i == numSegs - 1) ? (x + fillW - sx) : (segW + 0.5f);
			float r1 = (i == 0) ? h / 2f : 0f;              // Top-Left (tl)
			float r2 = (i == numSegs - 1) ? h / 2f : 0f;    // Top-Right (tr)
			float r3 = (i == numSegs - 1) ? h / 2f : 0f;    // Bottom-Right (br)
			float r4 = (i == 0) ? h / 2f : 0f;              // Bottom-Left (bl)
			SkijaRenderer.gradientRect(sx, y, curSegW, h, colors[i], colors[i + 1], silence.simsool.lucent.general.enums.GradientType.LEFT_TO_RIGHT, r1, r2, r3, r4);
		}
	}

	public static void drawRainbowBorder(float x, float y, float w, float h, float radius, float thickness) {
		int col1 = 0xE0F472B6;
		int col2 = 0xE038BDF8;
		SkijaCompositor.INSTANCE.enqueue(canvas -> {
			try (Shader shader = Shader.makeLinearGradient(x, y, x + w, y + h, new int[]{col1, col2});
				 Paint paint = new Paint()) {
				paint.setShader(shader);
				paint.setMode(PaintMode.STROKE);
				paint.setStrokeWidth(thickness);
				paint.setAntiAlias(true);
				if (radius <= 0) canvas.drawRect(Rect.makeXYWH(x, y, w, h), paint);
				else canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint);
			}
		});
	}

	public static void drawStatRow(String label, String value, float x, float y, float w, float fontSize, int valColor) {
		SkijaRenderer.text(label, x, y, Fonts.PRETENDARD_MEDIUM, UIColors.TEXT_SECONDARY, fontSize);
		float valW = SkijaRenderer.textWidth(value, Fonts.PRETENDARD_SEMIBOLD, fontSize);
		SkijaRenderer.text(value, x + w - valW, y, Fonts.PRETENDARD_SEMIBOLD, valColor, fontSize);
	}

	public static void drawBadge(String text, float x, float y, int bgColor, int textColor) {
		float fs = 11f;
		float tw = SkijaRenderer.textWidth(text, Fonts.PRETENDARD_SEMIBOLD, fs);
		float bw = tw + 12f;
		float bh = 18f;
		SkijaRenderer.rect(x, y, bw, bh, bgColor, 6f);
		SkijaRenderer.text(text, x + 6f, y + 3.5f, Fonts.PRETENDARD_SEMIBOLD, textColor, fs);
	}

	public static int getMinecraftColor(char code, int defaultColor) {
		return switch (code) {
			case '0' -> 0xFF000000;
			case '1' -> 0xFF0000AA;
			case '2' -> 0xFF00AA00;
			case '3' -> 0xFF00AAAA;
			case '4' -> 0xFFAA0000;
			case '5' -> 0xFFAA00AA;
			case '6' -> 0xFFFFAA00;
			case '7' -> 0xFFAAAAAA;
			case '8' -> 0xFF555555;
			case '9' -> 0xFF5555FF;
			case 'a' -> 0xFF55FF55;
			case 'b' -> 0xFF55FFFF;
			case 'c' -> 0xFFFF5555;
			case 'd' -> 0xFFFF55FF;
			case 'e' -> 0xFFFFFF55;
			case 'f' -> 0xFFFFFFFF;
			case 'r' -> defaultColor;
			default -> defaultColor;
		};
	}

	public static float drawColoredText(String text, float x, float y, float fontSize, int defaultColor) {
		if (text == null || text.isEmpty()) return 0;

		float curX = x;
		int currentColor = defaultColor;
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '§' && i + 1 < text.length()) {
				if (buffer.length() > 0) {
					String segment = buffer.toString();
					SkijaRenderer.text(segment, curX, y, Fonts.PRETENDARD, currentColor, fontSize);
					curX += SkijaRenderer.textWidth(segment, Fonts.PRETENDARD, fontSize);
					buffer.setLength(0);
				}
				char colorCode = Character.toLowerCase(text.charAt(i + 1));
				currentColor = getMinecraftColor(colorCode, defaultColor);
				i++; // Skip color code char
			} else {
				buffer.append(c);
			}
		}

		if (buffer.length() > 0) {
			String segment = buffer.toString();
			SkijaRenderer.text(segment, curX, y, Fonts.PRETENDARD, currentColor, fontSize);
			curX += SkijaRenderer.textWidth(segment, Fonts.PRETENDARD, fontSize);
		}

		return curX - x;
	}

	public static float getColoredTextWidth(String text, float fontSize) {
		if (text == null || text.isEmpty()) return 0;
		String clean = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
		return SkijaRenderer.textWidth(clean, Fonts.PRETENDARD, fontSize);
	}

	// =========================================================================
	// Unified Item Slot System
	// =========================================================================
	public static class ItemSlotInfo {
		public float x, y, size;
		public net.minecraft.world.item.ItemStack stack;
		public String customText = "";
		public int customTextColor = 0xFFFFFFFF;
		public boolean hasGlint = false;
		public boolean showTooltip = true;

		public ItemSlotInfo(float x, float y, float size, net.minecraft.world.item.ItemStack stack) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.stack = stack;
		}

		public ItemSlotInfo(float x, float y, float size, net.minecraft.world.item.ItemStack stack, boolean showTooltip) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.stack = stack;
			this.showTooltip = showTooltip;
		}

		public ItemSlotInfo(float x, float y, float size, net.minecraft.world.item.ItemStack stack, String customText, int customTextColor) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.stack = stack;
			this.customText = customText;
			this.customTextColor = customTextColor;
		}
	}

	public static final java.util.List<ItemSlotInfo> globalItemSlots = new java.util.ArrayList<>();

	public static void clearGlobalSlots() {
		globalItemSlots.clear();
	}

	public static void registerItemSlot(float x, float y, float size, net.minecraft.world.item.ItemStack stack) {
		if (stack != null && !stack.isEmpty()) {
			globalItemSlots.add(new ItemSlotInfo(x, y, size, stack));
		}
	}

	public static void registerItemSlot(float x, float y, float size, net.minecraft.world.item.ItemStack stack, boolean showTooltip) {
		if (stack != null && !stack.isEmpty()) {
			globalItemSlots.add(new ItemSlotInfo(x, y, size, stack, showTooltip));
		}
	}

	public static void registerItemSlot(float x, float y, float size, net.minecraft.world.item.ItemStack stack, String customText, int customTextColor) {
		if (stack != null && !stack.isEmpty()) {
			globalItemSlots.add(new ItemSlotInfo(x, y, size, stack, customText, customTextColor));
		}
	}


	public static void drawItemSlotBg(float x, float y, float size, boolean hovered, int borderColor, int bgColor, float radius) {
		SkijaRenderer.rect(x, y, size, size, bgColor, radius);
		SkijaRenderer.outlineRect(x, y, size, size, hovered ? 1.4f : 1.0f, borderColor, radius);
	}
}