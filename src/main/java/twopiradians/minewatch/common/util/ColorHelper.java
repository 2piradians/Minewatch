package twopiradians.minewatch.common.util;

import net.minecraft.util.text.TextFormatting;

public class ColorHelper {

	public static int getForegroundColor(TextFormatting format) {
		switch (format) {
		case AQUA:
			return 0x55FFFF;
		case BLACK:
			return 0x000000;
		case BLUE:
			return 0x5555FF;
		case DARK_AQUA:
			return 0x00AAAA;
		case DARK_BLUE:
			return 0x0000AA;
		case DARK_GRAY:
			return 0x555555;
		case DARK_GREEN:
			return 0x00AA00;
		case DARK_PURPLE:
			return 0xAA00AA;
		case DARK_RED:
			return 0xAA0000;
		case GOLD:
			return 0xFFAA00;
		case GRAY:
			return 0xAAAAAA;
		case GREEN:
			return 0x55FF55;
		case LIGHT_PURPLE:
			return 0xFF55FF;
		case RED:
			return 0xFF5555;
		case WHITE:
			return 0xFFFFFF;
		case YELLOW:
			return 0xFFFF55;
		default:
			return -1;
		}
	}

	public static int getBackgroundColor(TextFormatting format) {
		switch (format) {
		case AQUA:
			return 0x153F3F;
		case BLACK:
			return 0x000000;
		case BLUE:
			return 0x15153F;
		case DARK_AQUA:
			return 0x002A2A;
		case DARK_BLUE:
			return 0x00002A;
		case DARK_GRAY:
			return 0x151515;
		case DARK_GREEN:
			return 0x002A00;
		case DARK_PURPLE:
			return 0x2A002A;
		case DARK_RED:
			return 0x2A0000;
		case GOLD:
			return 0x2A2A00;
		case GRAY:
			return 0x2A2A2A;
		case GREEN:
			return 0x153F15;
		case LIGHT_PURPLE:
			return 0x3F153F;
		case RED:
			return 0x3F1515;
		case WHITE:
			return 0x3F3F3F;
		case YELLOW:
			return 0x3F3F15;
		default:
			return -1;
		}
	}

}
