// Copyright 2020-2025 Mirsario & Contributors.
// Released under the GNU General Public License 3.0.
// See LICENSE.md for details.

package ua.myxazaur.cameraoverhaul.camera;

import org.joml.*;
import ua.myxazaur.cameraoverhaul.config.CameraConfig;
import ua.myxazaur.cameraoverhaul.utils.*;

import java.lang.Math;

@SuppressWarnings("unused")
public final class CameraSystem
{
	private CameraConfig.Contextual ctxCfg;
	private final Vector3d prevCameraEulerRot = new Vector3d();
	private final Vector3d prevEntityVelocity = new Vector3d();
	private double lastActionTime;
	private CameraContext.Perspective prevCameraPerspective;
	private final Transform offsetTransform = new Transform();

	public void notifyOfPlayerAction() {
		lastActionTime = TimeSystem.getTime();
	}

	public void onCameraUpdate(CameraContext context, double deltaTime) {
		double time = TimeSystem.getTime();

		if (context.isRidingVehicle) ctxCfg = CameraConfig.vehicles;
		else if (context.isRidingMount) ctxCfg = CameraConfig.mounts;
		else if (context.isSwimming) ctxCfg = CameraConfig.swimming;
		else if (context.isFlying) ctxCfg = CameraConfig.flying;
		else ctxCfg = CameraConfig.walking;

		// Reset the offset transform
		offsetTransform.position = new Vector3d(0, 0, 0);
		offsetTransform.eulerRot = new Vector3d(0, 0, 0);

		if (!CameraConfig.general.enabled || (!CameraConfig.general.enableInThirdPerson && context.perspective != CameraContext.Perspective.FIRST_PERSON)) {
			return;
		}

		ScreenShakes.onCameraUpdate(context, deltaTime);

		if (!context.velocity.equals(prevEntityVelocity) || !context.transform.eulerRot.equals(prevCameraEulerRot))
			notifyOfPlayerAction();

		// X
		verticalVelocityPitchOffset(context, offsetTransform, deltaTime);
		forwardVelocityPitchOffset(context, offsetTransform, deltaTime);
		// Z
		turningRollOffset(context, offsetTransform, deltaTime);
		strafingRollOffset(context, offsetTransform, deltaTime);
		// XY
		noiseOffset(context, offsetTransform, deltaTime);

		prevEntityVelocity.set(context.velocity);
		prevCameraEulerRot.set(context.transform.eulerRot);
		prevCameraPerspective = context.perspective;
	}
	public void modifyCameraTransform(Transform transform) {
		transform.position.add(offsetTransform.position);
		transform.eulerRot.add(offsetTransform.eulerRot);

		ScreenShakes.modifyCameraTransform(transform);
	}

	private static final double BASE_VERTICAL_PITCH_SMOOTHING = 0.00004;
	private double prevVerticalVelocityPitchOffset;
	private void verticalVelocityPitchOffset(CameraContext context, Transform outputTransform, double deltaTime) {
		double multiplier = ctxCfg.verticalVelocityPitchFactor;
		double smoothing = BASE_VERTICAL_PITCH_SMOOTHING * ctxCfg.verticalVelocitySmoothingFactor;

		double targetOffset = context.velocity.y * multiplier;
		double currentOffset = MathUtils.damp(prevVerticalVelocityPitchOffset, targetOffset, smoothing, deltaTime);
		
		outputTransform.eulerRot.x += currentOffset;
		prevVerticalVelocityPitchOffset = currentOffset;
	}

	private static final double BASE_FORWARD_PITCH_SMOOTHING = 0.008;
	private double prevForwardVelocityPitchOffset;
	private void forwardVelocityPitchOffset(CameraContext context, Transform outputTransform, double deltaTime) {
		double multiplier = ctxCfg.forwardVelocityPitchFactor;
		double smoothing = BASE_FORWARD_PITCH_SMOOTHING * ctxCfg.horizontalVelocitySmoothingFactor;

		double targetOffset = context.getForwardRelativeVelocity().z * multiplier;
		double currentOffset = MathUtils.damp(prevForwardVelocityPitchOffset, targetOffset, smoothing, deltaTime);
		
		outputTransform.eulerRot.x += currentOffset;
		prevForwardVelocityPitchOffset = currentOffset;
	}

	private static final double BASE_TURNING_ROLL_ACCUMULATION = 0.0048;
	private static final double BASE_TURNING_ROLL_INTENSITY = 1.25;
	private static final double BASE_TURNING_ROLL_SMOOTHING = 0.0825;
	private double turningRollTargetOffset;
	private void turningRollOffset(CameraContext context, Transform outputTransform, double deltaTime) {
		double decaySmoothing = BASE_TURNING_ROLL_SMOOTHING * CameraConfig.general.turningRollSmoothing;
		double intensity = BASE_TURNING_ROLL_INTENSITY * CameraConfig.general.turningRollIntensity;
		double accumulation = BASE_TURNING_ROLL_ACCUMULATION * CameraConfig.general.turningRollAccumulation;
		double yawDelta = prevCameraEulerRot.y - context.transform.eulerRot.y;

		// Don't spazz out when switching perspectives.
		if (context.perspective != prevCameraPerspective) yawDelta = 0.0;

		// Decay
		turningRollTargetOffset = MathUtils.damp(turningRollTargetOffset, 0, decaySmoothing, deltaTime);
		// Accumulation
		turningRollTargetOffset = MathUtils.clamp(turningRollTargetOffset + (yawDelta * accumulation), -1.0, 1.0);
		// Apply
		double turningRollOffset = MathUtils.clamp01(turningEasing(Math.abs(turningRollTargetOffset))) * intensity * Math.signum(turningRollTargetOffset);
		outputTransform.eulerRot.z += turningRollOffset;
	}
	private static double turningEasing(double x) {
		// https://easings.net/#easeInOutCubic
		return x < 0.5 ? (4 * x * x * x) : (1 - Math.pow(-2 * x + 2, 3) / 2);
	}

	private static final double BASE_STRAFING_ROLL_SMOOTHING = 0.008;
	private double prevStrafingRollOffset;
	private void strafingRollOffset(CameraContext context, Transform outputTransform, double deltaTime) {
		double multiplier = ctxCfg.strafingRollFactor;
		double smoothing = BASE_STRAFING_ROLL_SMOOTHING * ctxCfg.horizontalVelocitySmoothingFactor;

		double target = -context.getForwardRelativeVelocity().x * multiplier;
		double offset = MathUtils.damp(prevStrafingRollOffset, target, smoothing, deltaTime);

		outputTransform.eulerRot.z += offset;
		prevStrafingRollOffset = offset;
	}

	private static final double CAMERASWAY_FADING_SMOOTHNESS = 3.0;
	private double cameraSwayFactor;
	private double cameraSwayFactorTarget;
	private void noiseOffset(CameraContext context, Transform outputTransform, double deltaTime) {
		double time = TimeSystem.getTime();
		float noiseX = (float)(time * CameraConfig.general.cameraSwayFrequency);

		// Fade out if the player turns, moves, or does an interaction.
		if ((time - lastActionTime) < CameraConfig.general.cameraSwayFadeInDelay) {
			cameraSwayFactorTarget = 0; // Fade-out
		}
		// Only start a fade-in after the last fade-out has ended.
		else if (cameraSwayFactor == cameraSwayFactorTarget) {
			cameraSwayFactorTarget = 1; // Fade-in
		}

		double cameraSwayFactorFadeLength = cameraSwayFactorTarget > 0 ? CameraConfig.general.cameraSwayFadeInLength : CameraConfig.general.cameraSwayFadeOutLength;
		double cameraSwayFactorFadeStep = cameraSwayFactorFadeLength > 0.0 ? deltaTime / cameraSwayFactorFadeLength : 1.0;
		cameraSwayFactor = MathUtils.stepTowards(cameraSwayFactor, cameraSwayFactorTarget, cameraSwayFactorFadeStep);

		double scaledIntensity = CameraConfig.general.cameraSwayIntensity * Math.pow(cameraSwayFactor, CAMERASWAY_FADING_SMOOTHNESS);
		Vector3d target = new Vector3d(scaledIntensity, scaledIntensity, 0.0);
		Vector3d noise = new Vector3d(
			SimplexNoise.noise(noiseX, 420),
			SimplexNoise.noise(noiseX, 1337),
			SimplexNoise.noise(noiseX, 6969)
		);

		outputTransform.eulerRot.add(noise.mul(target));
	}
}
