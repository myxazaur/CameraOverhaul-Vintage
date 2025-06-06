package ua.myxazaur.vintagecam.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.myxazaur.vintagecam.utils.*;

import static ua.myxazaur.vintagecam.config.COConfig.cameraConfig;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    private final CameraUtils.ConfigContext config = new CameraUtils.ConfigContext();
    private final CameraUtils.Transform transform = new CameraUtils.Transform();
    private final Vector3d prevVelocity = new Vector3d();
    private final Vector3d prevEulerRot = new Vector3d();
    private float lastActionTime = 0.0F;
    private boolean wasFirstPerson = true;
    private double prevVerticalPitchOffset = 0.0;
    private double prevForwardPitchOffset = 0.0;
    private double prevStrafingRollOffset = 0.0;
    private double turningRollTargetOffset = 0.0;
    private double cameraSwayFactor = 0.0;
    private double cameraSwayFactorTarget = 0.0;

    private static final double BASE_VERTICAL_PITCH_SMOOTHING = 0.00004;
    private static final double BASE_FORWARD_PITCH_SMOOTHING = 0.008;
    private static final double BASE_STRAFING_ROLL_SMOOTHING = 0.015;
    private static final double BASE_TURNING_ROLL_SMOOTHING = 0.15;
    private static final double CAMERASWAY_FADING_SMOOTHNESS = 3.0;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void addCameraTilt(float partialTicks, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null || !cameraConfig.enabled ||
                (!cameraConfig.enableInThirdPerson && mc.gameSettings.thirdPersonView != 0)) {
            return;
        }

        float time = (float) mc.world.getWorldTime() + partialTicks;
        boolean isFirstPerson = mc.gameSettings.thirdPersonView == 0;

        // Update configuration based on player state
        updateConfigContext(player);

        // Reset transform
        transform.eulerRot.set(0.0, 0.0, 0.0);

        // Detect player movement
        Vector3d velocity = new Vector3d(player.motionX, player.motionY, player.motionZ);
        Vector3d eulerRot = new Vector3d(player.rotationPitch, player.rotationYaw, 0.0);
        if (!velocity.equals(prevVelocity) || !eulerRot.equals(prevEulerRot)) {
            lastActionTime = time;
        }

        // Apply camera effects
        verticalVelocityPitchOffset(player, partialTicks);
        forwardVelocityPitchOffset(player, partialTicks);
        strafingRollOffset(player, partialTicks);
        turningRollOffset(player, isFirstPerson, eulerRot, partialTicks);
        noiseOffset(time, partialTicks);

        // Apply rotations
        GL11.glRotatef((float) transform.eulerRot.z, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef((float) transform.eulerRot.x, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float) transform.eulerRot.y, 0.0F, 1.0F, 0.0F);

        // Update previous state
        prevVelocity.set(velocity);
        prevEulerRot.set(eulerRot);
        wasFirstPerson = isFirstPerson;
    }

    private void updateConfigContext(EntityPlayerSP player) {
        config.reset();
        if (player.isRiding()) {
            config.applyRidingModifiers();
        } else if (player.isInWater()) {
            // maybe Aqua Acrobatics integration later?
            config.applySwimmingModifiers();
        } else if (!player.onGround) {
            config.applyFlyingModifiers();
        }
    }

    private void verticalVelocityPitchOffset(EntityPlayerSP player, float deltaTime) {
        double multiplier = config.verticalPitchFactor;
        double smoothing = BASE_VERTICAL_PITCH_SMOOTHING * config.verticalSmoothingFactor;

        double targetOffset = player.motionY * multiplier;
        double currentOffset = MathUtils.damp(prevVerticalPitchOffset, targetOffset, smoothing, deltaTime);

        transform.eulerRot.x += currentOffset;
        prevVerticalPitchOffset = currentOffset;
    }

    private void forwardVelocityPitchOffset(EntityPlayerSP player, float deltaTime) {
        double multiplier = config.forwardPitchFactor;
        double smoothing = BASE_FORWARD_PITCH_SMOOTHING * config.horizontalSmoothingFactor;

        double forwardVelocity = player.moveForward;
        double targetOffset = forwardVelocity * multiplier;
        double currentOffset = MathUtils.damp(prevForwardPitchOffset, targetOffset, smoothing, deltaTime);

        transform.eulerRot.x += currentOffset;
        prevForwardPitchOffset = currentOffset;
    }

    private void strafingRollOffset(EntityPlayerSP player, float deltaTime) {
        double multiplier = config.strafeRollFactor;
        double smoothing = BASE_STRAFING_ROLL_SMOOTHING * config.horizontalSmoothingFactor;

        double target = -player.moveStrafing * multiplier;
        double offset = MathUtils.damp(prevStrafingRollOffset, target, smoothing, deltaTime);

        transform.eulerRot.z += offset;
        prevStrafingRollOffset = offset;
    }

    private void turningRollOffset(EntityPlayerSP player, boolean isFirstPerson, Vector3d eulerRot, float deltaTime) {
        double decaySmoothing = BASE_TURNING_ROLL_SMOOTHING * config.turningRollSmoothing;
        double intensity = config.turningRollIntensity;
        double accumulation = config.turningRollAccumulation;

        double yawDelta = prevEulerRot.y - eulerRot.y;
        if (isFirstPerson != wasFirstPerson) {
            yawDelta = 0.0;
        }

        turningRollTargetOffset = MathUtils.damp(turningRollTargetOffset, 0, decaySmoothing, deltaTime);
        turningRollTargetOffset = MathUtils.clamp(turningRollTargetOffset + yawDelta * accumulation, -0.5, 0.5);
        double turningRollOffset = MathUtils.clamp01(turningEasing(Math.abs(turningRollTargetOffset))) *
                intensity * Math.signum(turningRollTargetOffset);

        transform.eulerRot.z += turningRollOffset;
    }

    private void noiseOffset(float time, float deltaTime) {
        float noiseX = time * cameraConfig.swayFrequency;

        if (time - lastActionTime < cameraConfig.swayFadeDelay) {
            cameraSwayFactorTarget = 0.0;
        } else if (cameraSwayFactor == cameraSwayFactorTarget) {
            cameraSwayFactorTarget = 1.0;
        }

        double fadeLength = cameraSwayFactorTarget > 0 ? cameraConfig.swayFadeInLength : cameraConfig.swayFadeOutLength;
        double fadeStep = fadeLength > 0.0 ? deltaTime / fadeLength : 1.0;
        cameraSwayFactor = MathUtils.stepTowards(cameraSwayFactor, cameraSwayFactorTarget, fadeStep);

        double scaledIntensity = cameraConfig.swayIntensity * Math.pow(cameraSwayFactor, CAMERASWAY_FADING_SMOOTHNESS);
        Vector3d target = new Vector3d(scaledIntensity, scaledIntensity, 0.0);
        Vector3d noise = new Vector3d(
                SimplexNoise.noise2(420L, noiseX, 0.0),
                SimplexNoise.noise2(1337L, noiseX, 0.0),
                SimplexNoise.noise2(6969L, noiseX, 0.0)
        );

        transform.eulerRot.add(noise.mul(target, new Vector3d()));
    }

    private static double turningEasing(double x) {
        return x < 0.5 ? (4 * x * x * x) : (1 - Math.pow(-2 * x + 2, 3) / 2);
    }
}