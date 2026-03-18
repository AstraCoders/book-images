package com.lucasmellof.bookimages.utils;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
public class RenderUtils {
	public static int getArgb(int a, int red, int green, int blue) {
		return a << 24 | red << 16 | green << 8 | blue;
	}

	public static int getAbgr(int alpha, int red, int green, int blue) {
		return alpha << 24 | blue << 16 | green << 8 | red;
	}

	public static int getAlpha(int argb) {
		return (argb >> 24) & 0xFF;
	}

	public static int getRed(int argb) {
		return (argb >> 16) & 0xFF;
	}

	public static int getGreen(int argb) {
		return (argb >> 8) & 0xFF;
	}

	public static int getBlue(int argb) {
		return argb & 0xFF;
	}

	public static float getAlphaFloat(int argb) {
		return (float) getAlpha(argb) / 255F;
	}

	public static float getRedFloat(int argb) {
		return (float) getRed(argb) / 255F;
	}

	public static float getGreenFloat(int argb) {
		return (float) getGreen(argb) / 255F;
	}

	public static float getBlueFloat(int argb) {
		return (float) getBlue(argb) / 255F;
	}

}
