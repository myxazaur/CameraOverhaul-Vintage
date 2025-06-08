package ua.myxazaur.vintagecam.camera;

import ua.myxazaur.vintagecam.utils.SimplexNoise;
import ua.myxazaur.vintagecam.utils.Vector3d;
import ua.myxazaur.vintagecam.utils.MathUtils;

import static ua.myxazaur.vintagecam.config.COConfig.cameraConfig;

public class ShakeSystem {
    private final Vector3d shakeOffset = new Vector3d();
    private final Vector3d targetShake = new Vector3d();
    private double trauma = 0.0;
    
    public ShakeSystem() {}
    
    public void update(float deltaTime) {
        if (trauma > 0) {
            if (deltaTime <= 0 || Float.isNaN(deltaTime)) {
                return;
            }
            
            double recoverySpeed = cameraConfig.explosionRecoverySpeed;
            double maxAngle = cameraConfig.explosionMaxAngle;
            double frequency = cameraConfig.explosionFrequency;
            
            trauma = Math.max(0.0, trauma - recoverySpeed * deltaTime);
            
            if (Double.isNaN(trauma) || Double.isInfinite(trauma)) {
                trauma = 0.0;
                return;
            }
            
            double shake = trauma * trauma;
            double time = (System.nanoTime() / 1_000_000_000.0) % 1000.0;
            
            double noiseX = SimplexNoise.noise2(1337L, time * frequency, 0.0);
            double noiseY = SimplexNoise.noise2(2345L, time * frequency, 0.0);
            double noiseZ = SimplexNoise.noise2(3456L, time * frequency, 0.0);
            
            if (!Double.isNaN(noiseX) && !Double.isNaN(noiseY) && !Double.isNaN(noiseZ)) {
                targetShake.x = maxAngle * shake * noiseX;
                targetShake.y = maxAngle * shake * noiseY;
                targetShake.z = maxAngle * shake * noiseZ;
                
                targetShake.x = MathUtils.clamp(targetShake.x, -maxAngle, maxAngle);
                targetShake.y = MathUtils.clamp(targetShake.y, -maxAngle, maxAngle);
                targetShake.z = MathUtils.clamp(targetShake.z, -maxAngle, maxAngle);
                
                shakeOffset.x = MathUtils.damp(shakeOffset.x, targetShake.x, 0.5, deltaTime);
                shakeOffset.y = MathUtils.damp(shakeOffset.y, targetShake.y, 0.5, deltaTime);
                shakeOffset.z = MathUtils.damp(shakeOffset.z, targetShake.z, 0.5, deltaTime);
            }
        }
    }
    
    public void addTrauma(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return;
        }
        trauma = MathUtils.clamp01(trauma + amount);
    }

    public double getTrauma() {
        return trauma;
    }
    
    public Vector3d getShakeOffset() {
        return shakeOffset;
    }
    
    public boolean isShaking() {
        return trauma > 0.0;
    }
}