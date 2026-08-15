package silence.simsool.profileviewer.ui;

import silence.simsool.lucent.ui.utils.UIColors;
import silence.simsool.lucent.ui.utils.nvg.Fonts;
import silence.simsool.lucent.ui.utils.nvg.NVGRenderer;

public class RenderHelper {

	// Font Color Guide (Dark background contrast >= 4.5:1, 5 levels)
	public static final int FONT_PRIMARY   = 0xFFFFFFFF; // High emphasis / Headings / White
	public static final int FONT_SECONDARY = 0xFFD1D5DB; // Body text / Sub-headings (~11:1)
	public static final int FONT_MUTED     = 0xFF9CA3AF; // Secondary labels / Captions (~6.5:1)
	public static final int FONT_HINT      = 0xFF999999; // Subtle hints / Special hierarchy
	public static final int FONT_DISABLED  = 0xFF6B7280; // Disabled state

	// Font Size Guide
	public static final float FS_H1      = 32f; // H1: 32-36px
	public static final float FS_H2      = 24f; // H2: 24-30px
	public static final float FS_H3      = 20f; // H3: 20-24px
	public static final float FS_BODY    = 16f; // Body Text: 16px base size
	public static final float FS_BUTTON  = 14f; // Buttons: 14-16px
	public static final float FS_CAPTION = 12f; // Captions / Hints: 12-14px

	public static String formatCoins(double coins) {
		if (coins >= 1_000_000_000) return String.format("%.2fB", coins / 1_000_000_000.0);
		if (coins >= 1_000_000) return String.format("%.2fM", coins / 1_000_000.0);
		if (coins >= 1_000) return String.format("%.1fK", coins / 1_000.0);
		return String.format("%,.0f", coins);
	}

	public static String formatNumber(long num) {
		if (num >= 1_000_000_000) return String.format("%.2fB", num / 1_000_000_000.0);
		if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0);
		if (num >= 1_000) return String.format("%.1fK", num / 1_000.0);
		return String.format("%,d", num);
	}

	public static void drawModernCard(float x, float y, float w, float h, float radius, boolean hovered) {
		int bg = hovered ? 0xCC252632 : 0xBB1E1F28;
		int border = hovered ? UIColors.ACCENT_BLUE : UIColors.withAlpha(UIColors.ITEM_BORDER, 160);
		NVGRenderer.rect(x, y, w, h, bg, radius);
		NVGRenderer.outlineRect(x, y, w, h, 1.2f, border, radius);
	}

	public static void drawProgressBar(float x, float y, float w, float h, float progress, int fillColor) {
		drawProgressBar(x, y, w, h, progress, fillColor, fillColor);
	}

	public static void drawProgressBar(float x, float y, float w, float h, float progress, int startCol, int endCol) {
		NVGRenderer.rect(x, y, w, h, 0x44000000, h / 2f);
		float fillW = Math.max(0, Math.min(w, w * progress));
		if (fillW > 0) {
			NVGRenderer.rect(x, y, fillW, h, startCol, h / 2f);
		}
	}

	public static void drawStatRow(String label, String value, float x, float y, float w, float fontSize, int valColor) {
		NVGRenderer.text(label, x, y, Fonts.PRETENDARD_MEDIUM, UIColors.TEXT_SECONDARY, fontSize);
		float valW = NVGRenderer.textWidth(value, Fonts.PRETENDARD_SEMIBOLD, fontSize);
		NVGRenderer.text(value, x + w - valW, y, Fonts.PRETENDARD_SEMIBOLD, valColor, fontSize);
	}

	public static void drawBadge(String text, float x, float y, int bgColor, int textColor) {
		float fs = 11f;
		float tw = NVGRenderer.textWidth(text, Fonts.PRETENDARD_SEMIBOLD, fs);
		float bw = tw + 12f;
		float bh = 18f;
		NVGRenderer.rect(x, y, bw, bh, bgColor, 6f);
		NVGRenderer.text(text, x + 6f, y + 3.5f, Fonts.PRETENDARD_SEMIBOLD, textColor, fs);
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
					NVGRenderer.text(segment, curX, y, Fonts.PRETENDARD, currentColor, fontSize);
					curX += NVGRenderer.textWidth(segment, Fonts.PRETENDARD, fontSize);
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
			NVGRenderer.text(segment, curX, y, Fonts.PRETENDARD, currentColor, fontSize);
			curX += NVGRenderer.textWidth(segment, Fonts.PRETENDARD, fontSize);
		}

		return curX - x;
	}

	public static float getColoredTextWidth(String text, float fontSize) {
		if (text == null || text.isEmpty()) return 0;
		String clean = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
		return NVGRenderer.textWidth(clean, Fonts.PRETENDARD, fontSize);
	}
}