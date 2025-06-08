package ua.myxazaur.vintagecam.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.myxazaur.vintagecam.CameraOverhaul;
import ua.myxazaur.vintagecam.camera.CameraSystem;
import ua.myxazaur.vintagecam.config.COConfig;

@Mixin(NetHandlerPlayClient.class)
public abstract class ExplosionMixin {

    @Inject(
        method = "handleExplosion",
        at = @At("HEAD")
    )
    private void onExplosion(SPacketExplosion packet, CallbackInfo ci) {
        if (!Thread.currentThread().getName().equals("Client thread")) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || CameraOverhaul.instance == null) return;

        Vec3d explosionPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
        Vec3d playerPos = mc.player.getPositionVector();

        double distance = explosionPos.distanceTo(playerPos);
        double maxDistance = 32.0;

        if (distance > maxDistance) return;

        double trauma = (1.0 - (distance / maxDistance)) *
                COConfig.cameraConfig.explosionTraumaBase *
                (packet.getStrength() / 4.0);

        CameraSystem cameraSystem = CameraOverhaul.instance.getCameraSystem();
        if (cameraSystem == null || cameraSystem.getShakeSystem() == null) return;

        cameraSystem.getShakeSystem().addTrauma(trauma);
    }
}