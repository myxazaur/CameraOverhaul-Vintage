package ua.myxazaur.vintagecam.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.myxazaur.vintagecam.camera.CameraSystem;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    private final CameraSystem cameraSystem = new CameraSystem();

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void addCameraTilt(float partialTicks, CallbackInfo ci) {
        cameraSystem.updateCamera(partialTicks);
    }
}