package dev.ghen.thirst.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ItemSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> drinks;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> foods;

    static final ItemSettingsConfig INSTANCE = new ItemSettingsConfig();

    static
    {
        BUILDER.push("Drinks")
                .comment("Defines items that will recover thirst when drunk",
                        "Format: [[\"item-id-1\", hydration-amount, quenching-amount], [\"item-id-2\", hydration-amount, quenching-amount], ...etc]");
        drinks = BUILDER
                .defineList("drinks", Arrays.asList
                                (
                                        Arrays.asList("minecraft:potion", 5, 6),
                                        Arrays.asList("thirst:water_bowl", 3, 4)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        BUILDER.pop();

        BUILDER.push("Foods")
                .comment("Defines items that will recover thirst when eaten",
                        "Format: [[\"item-id-1\", hydration-amount, quenching-amount], [\"item-id-2\", hydration-amount, quenching-amount], ...etc]");
        foods = BUILDER
                .defineList("foods", Arrays.asList
                                (
                                        Arrays.asList("minecraft:apple", 2, 3),
                                        Arrays.asList("minecraft:golden_apple", 2, 3),
                                        Arrays.asList("minecraft:enchanted_golden_apple", 2, 3),
                                        Arrays.asList("minecraft:melon_slice", 4, 5),
                                        Arrays.asList("minecraft:carrot", 1, 2),
                                        Arrays.asList("minecraft:mushroom_stew", 2, 3),
                                        Arrays.asList("minecraft:rabbit_stew", 2, 3),
                                        Arrays.asList("minecraft:beetroot_soup", 5, 7),
                                        Arrays.asList("minecraft:beetroot", 1, 2),
                                        Arrays.asList("minecraft:sweet_berries", 1, 2),
                                        Arrays.asList("minecraft:glow_berries", 1, 2)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "thirst");

        // Create the config folder
        try
        {
            Files.createDirectory(csConfigPath);
        }
        catch (Exception ignored) {}

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "thirst/item_settings.toml");
    }

    public void copyValues(ItemSettingsConfig config)
    {
        setDrinks(config.getDrinks());
        setFoods(config.getFoods());
    }

    public static ItemSettingsConfig getInstance()
    {
        return INSTANCE;
    }

    public List<? extends List<?>> getDrinks()
    {
        return drinks.get();
    }

    public List<? extends List<?>> getFoods()
    {
        return foods.get();
    }

    public void setDrinks(List<? extends List<?>> itemMap)
    {
        drinks.set(itemMap);
    }

    public void setFoods(List<? extends List<?>> itemMap)
    {
        foods.set(itemMap);
    }
}
