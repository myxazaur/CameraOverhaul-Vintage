// Copyright 2020-2025 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

package ua.myxazaur.cameraoverhaul.utils;

public final class MathUtils
{
	// Clamp

	public static float clamp(float value, float min, float max) {
		return value < min ? min : (value > max ? max : value);
	}
	public static double clamp(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}
	public static float clamp01(float value) {
		return value < 0f ? 0f : (value > 1f ? 1f : value);
	}
	public static double clamp01(double value) {
		return value < 0d ? 0d : (value > 1d ? 1d : value);
	}

	// Lerp

	public static float lerp(float a, float b, float time) {
		return a + (b - a) * clamp01(time);
	}
	public static double lerp(double a, double b, double time) {
		return a + (b - a) * clamp01(time);
	}

	// Framerate-agnostic smoothing.
	// https://www.rorydriscoll.com/2016/03/07/frame-rate-independent-damping-using-lerp

	public static float damp(float source, float destination, float smoothing, float dt) {
		return lerp(source, destination, 1f - (float)Math.pow(smoothing * smoothing, dt));
	}
	public static double damp(double source, double destination, double smoothing, double dt) {
		return lerp(source, destination, 1d - Math.pow(smoothing * smoothing, dt));
	}

	// Step towards

	public static double stepTowards(double current, double target, double step) {
		if (current < target) {
			return Math.min(current + step, target);
		} else if (current > target) {
			return Math.max(current - step, target);
		}

		return current;
	}
}