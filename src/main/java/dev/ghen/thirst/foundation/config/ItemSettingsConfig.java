package dev.ghen.thirst.foundation.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DRINKS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> FOODS;
    public static final ForgeConfigSpec.ConfigValue<List<String>> ITEMS_BLACKLIST;

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
                                        Arrays.asList("farmersrespite:coffee",6,11),
                                        Arrays.asList("create:builders_tea", 12, 22),
                                        Arrays.asList("farmersdelight:apple_cider", 8, 13),
                                        Arrays.asList("farmersdelight:melon_juice", 8, 13),
                                        Arrays.asList("brewinandchewin:beer", 10, 14),
                                        Arrays.asList("brewinandchewin:vodka", 10, 14),
                                        Arrays.asList("brewinandchewin:rice_wine", 10, 14),
                                        Arrays.asList("brewinandchewin:mead", 10, 14),
                                        Arrays.asList("brewinandchewin:egg_grog", 10, 14),
                                        Arrays.asList("brewinandchewin:glittering_grenadine", 10, 14),
                                        Arrays.asList("brewinandchewin:bloody_mary", 12, 22),
                                        Arrays.asList("brewinandchewin:salty_folly", 12, 22),
                                        Arrays.asList("brewinandchewin:pale_jane", 12, 22),
                                        Arrays.asList("brewinandchewin:saccharine_rum", 12, 22),
                                        Arrays.asList("brewinandchewin:strongroot_ale", 12, 22),
                                        Arrays.asList("brewinandchewin:dread_nog", 12, 22),
                                        Arrays.asList("brewinandchewin:kombucha", 14, 22),
                                        Arrays.asList("brewinandchewin:red_rum", 14, 22),
                                        Arrays.asList("brewinandchewin:steel_toe_stout", 14, 22),
                                        Arrays.asList("collectorsreap:pink_limeade",8,10),
                                        Arrays.asList("collectorsreap:berry_limeade",8,13),
                                        Arrays.asList("collectorsreap:limeade",8,13),
                                        Arrays.asList("collectorsreap:pink_limeade",8,13),
                                        Arrays.asList("collectorsreap:pomegranate_black_tea",10,14),
                                        Arrays.asList("collectorsreap:lime_green_tea",10,14),

                                        Arrays.asList("toughasnails:dirty_water_bottle", 6, 8),
                                        Arrays.asList("toughasnails:purified_water_bottle", 8, 10),
                                        Arrays.asList("toughasnails:dirty_water_canteen", 8, 10),
                                        Arrays.asList("toughasnails:water_canteen", 9, 11),
                                        Arrays.asList("toughasnails:purified_water_canteen", 10, 12),
                                        Arrays.asList("toughasnails:melon_juice", 8, 13),
                                        Arrays.asList("toughasnails:apple_juice", 8, 13),
                                        Arrays.asList("toughasnails:cactus_juice", 8, 13),
                                        Arrays.asList("toughasnails:carrot_juice", 8, 13),
                                        Arrays.asList("toughasnails:glow_berry_juice", 8, 13),
                                        Arrays.asList("toughasnails:chorus_fruit_juice", 8, 13),
                                        Arrays.asList("toughasnails:suspicious_water_cup", 8, 13),
                                        Arrays.asList("toughasnails:pumpkin_juice", 8, 13),
                                        Arrays.asList("toughasnails:sweet_berry_juice", 8, 13)

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
                                        Arrays.asList("minecraft:golden_carrot", 1, 2),
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
                                        Arrays.asList("farmersdelight:noodle_soup", 4, 5),
                                        Arrays.asList("collectorsreap:lime_slice",1,2),
                                        Arrays.asList("collectorsreap:lime",2,3),
                                        Arrays.asList("collectorsreap:portobello_rice_soup",6,8),
                                        Arrays.asList("collectorsreap:lime_popsicle",7,9)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        BUILDER.pop();

        BUILDER.push("Blacklist");

        ITEMS_BLACKLIST = BUILDER.comment("A mod may have added thirst compatibility to an item via code. If you want to edit the thirst values",
                "of that item, add an entry in one of the first two lists. If instead you want to remove thirst support for that item, add an entry in this list",
                "Format: [\"examplemod:example_item_1\", \"examplemod:example_item_2\"]")
                .define("itemsBlacklist", new ArrayList<>());

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
}
