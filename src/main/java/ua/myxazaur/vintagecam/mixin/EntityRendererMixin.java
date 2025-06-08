package ua.myxazaur.vintagecam.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.myxazaur.vintagecam.CameraOverhaul;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void addCameraTilt(float partialTicks, CallbackInfo ci) {
        if (CameraOverhaul.instance != null) {
            CameraOverhaul.instance.getCameraSystem().updateCamera(partialTicks);
        }
    }
}