package ua.myxazaur.cameraoverhaul.client;

import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
import ua.myxazaur.cameraoverhaul.Tags;
import ua.myxazaur.cameraoverhaul.camera.CameraContext;
import ua.myxazaur.cameraoverhaul.camera.ScreenShakes;
import ua.myxazaur.cameraoverhaul.camera.TimeSystem;
import ua.myxazaur.cameraoverhaul.config.CameraConfig;
import ua.myxazaur.cameraoverhaul.utils.Transform;
import org.joml.Vector3d;

import static ua.myxazaur.cameraoverhaul.CameraOverhaul.*;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ClientHandler
{
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static boolean isEntityBlacklisted(Entity entity) {
        if (entity == null) return true;

        // Check if we should only process living entities
        if (CameraConfig.general.onlyLivingEntities && !(entity instanceof EntityLivingBase)) {
            return true;
        }

        // Check against blacklist
        String className = entity.getClass().getName();
        for (String blacklisted : CameraConfig.general.entityBlacklist) {
            if (blacklisted != null && !blacklisted.isEmpty() && className.contains(blacklisted)) {
                return true;
            }
        }

        return false;
    }

    // Main camera handler
    @SubscribeEvent
    public static void onEntityViewRenderCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Entity entity = mc.getRenderViewEntity();

        // Skip if entity is blacklisted or camera effects are disabled
        if (isEntityBlacklisted(entity)) return;
        if (!CameraConfig.general.enabled) return;

        Entity vehicle = entity.getRidingEntity();
        Entity controlled = vehicle != null ? vehicle : entity;

        context.isRiding = vehicle != null;
        context.isRidingMount = vehicle instanceof EntityAnimal;
        context.isRidingVehicle =
                vehicle instanceof EntityBoat ||
                        vehicle instanceof EntityMinecart;

        context.velocity = new Vector3d(controlled.motionX, controlled.motionY, controlled.motionZ);

        context.transform = new Transform(
                new Vector3d(
                        entity.prevPosX + (entity.posX - entity.prevPosX) * event.getRenderPartialTicks(),
                        entity.prevPosY + (entity.posY - entity.prevPosY) * event.getRenderPartialTicks(),
                        entity.prevPosZ + (entity.posZ - entity.prevPosZ) * event.getRenderPartialTicks()
                ),
                new Vector3d(
                        entity.rotationPitch,
                        entity.rotationYaw,
                        0
                )
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

        TimeSystem.update();
        camera.onCameraUpdate(context, TimeSystem.getDeltaTime());
        camera.modifyCameraTransform(context.transform);

        GL11.glRotatef((float) context.transform.eulerRot.z, 0f, 0f, 1f);
        GL11.glRotatef((float) (context.transform.eulerRot.x - entity.rotationPitch), 1f, 0f, 0f);
        GL11.glRotatef((float) (context.transform.eulerRot.y - entity.rotationYaw), 0f, 1f, 0f);
    }

    // Explosion packet handler
    @SubscribeEvent
    public static void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        event.getManager().channel().pipeline().addBefore("packet_handler", "camera_overhaul_handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof SPacketExplosion) {
                    SPacketExplosion packet = (SPacketExplosion) msg;
                    handleExplosionCamera(packet);
                }
                super.channelRead(ctx, msg);
            }
        });
    }

    private static void handleExplosionCamera(SPacketExplosion packet) {
        mc.addScheduledTask(() -> {
            if (mc.player == null) return;

            ScreenShakes.Slot shake = ScreenShakes.createDirect();
            shake.position.set(packet.getX(), packet.getY(), packet.getZ());
            shake.radius = 32f;
            shake.trauma = (float) (CameraConfig.general.explosionTrauma * (packet.getStrength() / 2.0));
            shake.lengthInSeconds = 2f;
        });
    }

    // Lightning strike handler
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
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

    // Hand swing handler
    @SubscribeEvent
    public static void onTickPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player != mc.player) return;

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