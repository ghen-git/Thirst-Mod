package dev.ghen.thirst.foundation.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Boolean> ONLY_SHOW_PURITY_WHEN_SHIFTING;
    public static final ForgeConfigSpec.ConfigValue<Integer> THIRST_BAR_Y_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Integer> THIRST_BAR_X_OFFSET;

    static
    {
        BUILDER.push("Purity tooltip");
        ONLY_SHOW_PURITY_WHEN_SHIFTING = BUILDER.comment("If the purity tooltip should be shown only when the player is pressing the shift key").define("onlyShowPurityWhenShifting", false);
        BUILDER.pop();

        BUILDER.push("Thirst Bar");
        THIRST_BAR_Y_OFFSET = BUILDER.comment("How many pixels should the thirst bar be shifted vertically from its original position").define("thirstBarYOffset", 0);
        THIRST_BAR_X_OFFSET = BUILDER.comment("How many pixels should the thirst bar be shifted horizontally from its original position").define("thirstBarXOffset", 0);
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "thirst/client.toml");
    }
}
