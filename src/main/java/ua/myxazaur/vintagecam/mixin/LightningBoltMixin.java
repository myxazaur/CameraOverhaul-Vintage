package ua.myxazaur.vintagecam.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.myxazaur.vintagecam.CameraOverhaul;
import ua.myxazaur.vintagecam.camera.CameraSystem;
import ua.myxazaur.vintagecam.config.COConfig;

@Mixin(EntityLightningBolt.class)
public abstract class LightningBoltMixin {
    
    //@Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/EntityLightningBolt;setDead()V"))
    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDZ)V", at = @At("TAIL"))
    private void onStrike(World worldIn, double x, double y, double z, boolean effectOnlyIn, CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null || CameraOverhaul.instance == null) return;

        Vec3d lightningPos = new Vec3d(x, y, z);
        Vec3d playerPos = mc.player.getPositionVector();

        double distance = lightningPos.distanceTo(playerPos);
        double maxDistance = 48.0;

        if (distance > maxDistance) return;

        double trauma = (1.0 - (distance / maxDistance)) *
                          COConfig.cameraConfig.explosionTraumaBase * 0.4;

        CameraSystem cameraSystem = CameraOverhaul.instance.getCameraSystem();
        if (cameraSystem == null || cameraSystem.getShakeSystem() == null) return;

        cameraSystem.getShakeSystem().addTrauma(trauma);
    }
}