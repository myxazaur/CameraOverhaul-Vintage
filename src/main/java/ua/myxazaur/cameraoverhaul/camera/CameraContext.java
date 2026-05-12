// Copyright 2020-2025 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

package ua.myxazaur.cameraoverhaul.camera;

import org.joml.*;
import ua.myxazaur.cameraoverhaul.utils.*;

public class CameraContext
{
	public enum Perspective {
		FIRST_PERSON,
		THIRD_PERSON,
		THIRD_PERSON_REVERSE,
	}

	public boolean isSwimming;
	public boolean isFlying;
	public boolean isSprinting;
	public boolean isRiding;
	public boolean isRidingMount;
	public boolean isRidingVehicle;
	public Vector3d velocity = new Vector3d();
	public Perspective perspective;
	public Transform transform = new Transform();

	private final Vector2d tempRotate = new Vector2d();
	private final Vector3d tempForwardVel = new Vector3d();

	public Vector3d getForwardRelativeVelocity() {
		tempRotate.set(velocity.x, velocity.z);
		VectorUtils.rotate(tempRotate, 360d - transform.eulerRot.y, tempRotate);
		return tempForwardVel.set(tempRotate.x, velocity.y, tempRotate.y);
	}
}
