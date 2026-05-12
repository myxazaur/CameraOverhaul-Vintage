// Copyright 2020-2025 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

package ua.myxazaur.cameraoverhaul.utils;

import org.joml.Vector2d;

public final class VectorUtils
{
	public static double length(Vector2d vec) {
		return length(vec.x, vec.y);
	}
	public static double length(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	public static Vector2d rotate(Vector2d vec, double degrees) {
		double radians = Math.toRadians(degrees);
		double sin = Math.sin(radians);
		double cos = Math.cos(radians);

		return new Vector2d((cos * vec.x) - (sin * vec.y), (sin * vec.x) + (cos * vec.y));
	}

	public static void rotate(Vector2d vec, double degrees, Vector2d out) {
		double radians = Math.toRadians(degrees);
		double sin = Math.sin(radians);
		double cos = Math.cos(radians);
		double x = vec.x, y = vec.y;
		out.set((cos * x) - (sin * y), (sin * x) + (cos * y));
	}

	public static Vector2d lerp(Vector2d src, Vector2d dst, double step) {
		return lerp(src.x, src.y, dst.x, dst.y, step);
	}
	public static Vector2d lerp(double xSrc, double ySrc, double xDst, double yDst, double step) {
		return new Vector2d(
			xSrc + (xDst - xSrc) * step,
			ySrc + (yDst - ySrc) * step
		);
	}

	public static Vector2d multiply(Vector2d vec, double value) {
		return new Vector2d(vec.x * value, vec.y * value);
	}
	public static Vector2d multiply(Vector2d vec, Vector2d value) {
		return new Vector2d(vec.x * value.x, vec.y * value.y);
	}
}
