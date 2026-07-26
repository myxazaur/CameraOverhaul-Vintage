package ua.myxazaur.cameraoverhaul.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.myxazaur.cameraoverhaul.camera.ScreenShakes;
import ua.myxazaur.cameraoverhaul.config.CameraConfig;

import static ua.myxazaur.cameraoverhaul.CameraOverhaul.camera;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin
{
    @Unique
    private static long co$shakeHandle;

    @Inject(method = "swingArm",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/EntityLivingBase;swingProgressInt:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void co$onSwingArm(EnumHand hand, CallbackInfo ci) {
        EntityLivingBase target = (EntityLivingBase) (Object) this;

        if (target == Minecraft.getMinecraft().player) {
            co$shakeHandle = ScreenShakes.recreate(co$shakeHandle);

            ScreenShakes.Slot shake = ScreenShakes.get(co$shakeHandle);
            shake.trauma = (float) CameraConfig.general.handSwingTrauma;
            shake.frequency = 0.5f;
            shake.lengthInSeconds = 0.5f;

            camera.notifyOfPlayerAction();
        }
    }
}
