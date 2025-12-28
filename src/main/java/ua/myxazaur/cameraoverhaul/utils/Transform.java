// Copyright 2020-2025 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

package ua.myxazaur.cameraoverhaul.utils;

import org.joml.*;

public class Transform
{
	public Vector3d position;
	public Vector3d eulerRot;

	public Transform() {
		this.position = new Vector3d(0, 0, 0);
		this.eulerRot = new Vector3d(0, 0, 0);
	}

	public Transform(Vector3d position, Vector3d eulerRot) {
		this.position = position;
		this.eulerRot = eulerRot;
	}
}