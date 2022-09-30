package dev.ghen.thirst.foundation.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
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

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DRINKS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> FOODS;

    static final ItemSettingsConfig INSTANCE = new ItemSettingsConfig();

    static
    {
        BUILDER.push("Drinks")
                .comment("Defines items that will recover thirst when drunk",
                        "Format: [[\"item-id-1\", hydration-amount, quenching-amount], [\"item-id-2\", hydration-amount, quenching-amount], ...etc]");
        DRINKS = BUILDER
                .defineList("drinks", Arrays.asList
                                (
                                        Arrays.asList("minecraft:potion", 6, 8),
                                        Arrays.asList("thirst:terracotta_water_bowl", 4, 5),
                                        Arrays.asList("farmersrespite:green_tea", 10, 14),
                                        Arrays.asList("farmersrespite:yellow_tea", 10, 14),
                                        Arrays.asList("farmersrespite:black_tea", 10, 14),
                                        Arrays.asList("farmersrespite:rose_hip_tea", 12, 22),
                                        Arrays.asList("farmersrespite:dandelion_tea", 12, 22),
                                        Arrays.asList("create:builders_tea", 12, 22),
                                        Arrays.asList("farmersdelight:apple_cider", 8, 13),
                                        Arrays.asList("farmersdelight:melon_juice", 8, 13),
                                        Arrays.asList("brewinandchewin:beer", 10, 14),
                                        Arrays.asList("brewinandchewin:vodka", 10, 14),
                                        Arrays.asList("brewinandchewin:rice_wine", 10, 14),
                                        Arrays.asList("brewinandchewin:mead", 10, 14),
                                        Arrays.asList("brewinandchewin:egg_nog", 10, 14),
                                        Arrays.asList("brewinandchewin:glittering_grenadine", 10, 14),
                                        Arrays.asList("brewinandchewin:bloody_mary", 12, 22),
                                        Arrays.asList("brewinandchewin:salty_folly", 12, 22),
                                        Arrays.asList("brewinandchewin:pale_jane", 12, 22),
                                        Arrays.asList("brewinandchewin:saccharine_rum", 12, 22),
                                        Arrays.asList("brewinandchewin:strongroot_ale", 12, 22),
                                        Arrays.asList("brewinandchewin:dread_nog", 12, 22),
                                        Arrays.asList("brewinandchewin:kombucha", 14, 22),
                                        Arrays.asList("brewinandchewin:red_rum", 14, 22),
                                        Arrays.asList("brewinandchewin:steel_toe_stout", 14, 22)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        BUILDER.pop();

        BUILDER.push("Foods")
                .comment("Defines items that will recover thirst when eaten",
                        "Format: [[\"item-id-1\", hydration-amount, quenching-amount], [\"item-id-2\", hydration-amount, quenching-amount], ...etc]");
        FOODS = BUILDER
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
                                        Arrays.asList("minecraft:glow_berries", 1, 2),
                                        Arrays.asList("farmersdelight:pumpkin_slice", 2, 1),
                                        Arrays.asList("farmersdelight:cabbage_leaf", 1, 2),
                                        Arrays.asList("farmersdelight:melon_popsicle", 7, 9),
                                        Arrays.asList("farmersdelight:fruit_salad", 6, 8),
                                        Arrays.asList("farmersdelight:tomato_sauce", 4, 5),
                                        Arrays.asList("farmersdelight:mixed_salad", 4, 5),
                                        Arrays.asList("farmersdelight:beef_stew", 4, 5),
                                        Arrays.asList("farmersdelight:chicken_soup", 4, 5),
                                        Arrays.asList("farmersdelight:vegetable_soup", 4, 5),
                                        Arrays.asList("farmersdelight:fish_stew", 4, 5),
                                        Arrays.asList("farmersdelight:pumpkin_soup", 4, 5),
                                        Arrays.asList("farmersdelight:baked_cod_stew", 4, 5),
                                        Arrays.asList("farmersdelight:noodle_soup", 4, 5)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "thirst/item_settings.toml");
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
        return DRINKS.get();
    }

    public List<? extends List<?>> getFoods()
    {
        return FOODS.get();
    }

    public void setDrinks(List<? extends List<?>> itemMap)
    {
        DRINKS.set(itemMap);
    }

    public void setFoods(List<? extends List<?>> itemMap)
    {
        FOODS.set(itemMap);
    }
}
