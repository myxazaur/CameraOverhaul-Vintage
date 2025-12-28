package ua.myxazaur.cameraoverhaul.config;

import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import ua.myxazaur.cameraoverhaul.Tags;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Objects;

public final class ConfigDefaultsUtil
{
    public static @Nullable String getDefault(String categoryName, String optionName) {
        return getForgeConfiguration(Tags.MOD_ID, null).getCategory("general").getChildren().stream()
                .filter(cat -> cat.getName().equalsIgnoreCase(categoryName))
                .map(cat -> cat.get(optionName))
                .filter(Objects::nonNull)
                .map(Property::getDefault)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static double getDefaultDouble(String categoryName, String optionName) {
        Object o = getDefault(categoryName, optionName);
        if (o == null) return 0.0;
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0.0; }
    }

    public static boolean getDefaultBoolean(String categoryName, String optionName) {
        Object o = getDefault(categoryName, optionName);
        if (o == null) return false;
        return Boolean.parseBoolean(o.toString());
    }

    private static Configuration getForgeConfiguration(String modid, String name) {
        try {
            Method m = ConfigManager.class.getDeclaredMethod("getConfiguration", String.class, String.class);
            m.setAccessible(true);
            return (Configuration) m.invoke(null, modid, name);
        } catch (Throwable t) {
            return null;
        }
    }
}