package dev.ghen.thirst.foundation.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<Number> THIRST_DEPLETION_MODIFIER;

    static final CommonConfig INSTANCE = new CommonConfig();

    static
    {
        BUILDER.push("Thirst depletion harshness")
                .comment("How much faster is hydration depletion relative to hunger (1 means they will deplete at the same speed)");
        THIRST_DEPLETION_MODIFIER = BUILDER.define("thirstDepletionModifier", 1.2);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path configFolder = Paths.get(configPath.toAbsolutePath().toString(), "thirst");

        try
        {
            Files.createDirectory(configFolder);
        }
        catch (Exception ignored) {}

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "thirst/common.toml");
    }

    public void copyValues(CommonConfig config)
    {
        setThirstDepletionModifier(config.getThirstDepletionModifier());
    }

    public static CommonConfig getInstance()
    {
        return INSTANCE;
    }

    public float getThirstDepletionModifier()
    {
        return THIRST_DEPLETION_MODIFIER.get().floatValue();
    }

    public void setThirstDepletionModifier(float value)
    {
        THIRST_DEPLETION_MODIFIER.set(value);
    }
}
