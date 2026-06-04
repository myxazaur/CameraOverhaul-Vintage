package ua.myxazaur.cameraoverhaul.client;

import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import ua.myxazaur.cameraoverhaul.Tags;
import ua.myxazaur.cameraoverhaul.camera.CameraContext;
import ua.myxazaur.cameraoverhaul.camera.ScreenShakes;
import ua.myxazaur.cameraoverhaul.camera.TimeSystem;
import ua.myxazaur.cameraoverhaul.config.CameraConfig;

import java.lang.reflect.Field;

import static ua.myxazaur.cameraoverhaul.CameraOverhaul.*;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientHandler
{
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final Field explosionSize = ObfuscationReflectionHelper.findField(Explosion.class, "field_77280_f");

    private static boolean isEntityBlacklisted(Entity entity) {
        if (entity == null) return true;

        if (CameraConfig.general.onlyLivingEntities && !(entity instanceof EntityLivingBase)) {
            return true;
        }

        String className = entity.getClass().getName();
        for (String blacklisted : CameraConfig.general.entityBlacklist) {
            if (blacklisted != null && !blacklisted.isEmpty() && className.contains(blacklisted)) {
                return true;
            }
        }

        return false;
    }

    /// Main camera handler
    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Entity entity = mc.getRenderViewEntity();

        if (isEntityBlacklisted(entity)) return;
        if (!CameraConfig.general.enabled) return;

        Entity vehicle = entity.getRidingEntity();
        Entity controlled = vehicle != null ? vehicle : entity;

        context.isRiding = vehicle != null;
        context.isRidingMount = vehicle instanceof EntityAnimal;
        context.isRidingVehicle = context.isRiding && !(vehicle instanceof EntityLivingBase);

        context.velocity.set(controlled.motionX, controlled.motionY, controlled.motionZ);

        context.transform.position.set(
                entity.prevPosX + (entity.posX - entity.prevPosX) * event.getRenderPartialTicks(),
                entity.prevPosY + (entity.posY - entity.prevPosY) * event.getRenderPartialTicks(),
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * event.getRenderPartialTicks()
        );

        context.transform.eulerRot.set(
                event.getPitch(),
                event.getYaw(),
                0
        );

        context.perspective =
                mc.gameSettings.thirdPersonView == 0
                        ? CameraContext.Perspective.FIRST_PERSON
                        : CameraContext.Perspective.THIRD_PERSON;

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            context.isFlying = living.isElytraFlying();
            context.isSprinting = living.isSprinting();
            if (entity instanceof EntityPlayerSP && aquaAcrobatics) {
                EntityPlayerSP player = (EntityPlayerSP) entity;
                context.isSwimming = ((IPlayerResizeable) player).isSwimming();
            } else {
                context.isSwimming = living.isInWater() && living.isSprinting();
            }
        }

        if (!mc.isGamePaused()) {
            TimeSystem.update();
            camera.onCameraUpdate(context, TimeSystem.getDeltaTime());
        }

        camera.modifyCameraTransform(context.transform);

        event.setPitch((float) context.transform.eulerRot.x);
        event.setYaw((float) context.transform.eulerRot.y);
        event.setRoll((float) -context.transform.eulerRot.z);
    }

    /// Explosion handler
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Vec3d pos = explosion.getPosition();

        try {
            float strength = explosionSize.getFloat(explosion);
            if (mc.player == null) return;

            ScreenShakes.Slot shake = ScreenShakes.createDirect();
            shake.position.set(pos.x, pos.y, pos.z);
            shake.radius = 32f;

            if (CameraConfig.general.scaleExplosionByStrength) {
                // Scale by explosion strength (strength / 2.0 as multiplier)
                shake.trauma = (float) (CameraConfig.general.explosionTrauma * (strength / 2.0));
            } else {
                shake.trauma = (float) CameraConfig.general.explosionTrauma;
            }

            shake.lengthInSeconds = 2f;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /// Lightning strike handler
    @SubscribeEvent
    public static void onLightningStrike(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof EntityLightningBolt) {
            Vec3d pos = entity.getPositionVector();

            ScreenShakes.Slot explosion = ScreenShakes.createDirect();
            explosion.position.set(pos.x, pos.y, pos.z);
            explosion.radius = 16f;
            explosion.trauma = (float) CameraConfig.general.explosionTrauma;
            explosion.lengthInSeconds = 3f;

            ScreenShakes.Slot thunder = ScreenShakes.createDirect();
            thunder.position.set(pos.x, pos.y, pos.z);
            thunder.radius = 192f;
            thunder.trauma = (float) CameraConfig.general.thunderTrauma;
            thunder.frequency = 0.5f;
            thunder.lengthInSeconds = 7f;
        }
    }

    private static long shakeHandle;

    /// Hand swing handler
    @SubscribeEvent
    public static void onPlayerSwing(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != mc.player) return; // Handle only our player

        if (event.player.isSwingInProgress && event.player.swingProgressInt == 0) {
            shakeHandle = ScreenShakes.recreate(shakeHandle);

            ScreenShakes.Slot shake = ScreenShakes.get(shakeHandle);
            shake.trauma = (float) CameraConfig.general.handSwingTrauma;
            shake.frequency = 0.5f;
            shake.lengthInSeconds = 0.5f;

            camera.notifyOfPlayerAction();
        }
    }
}