package dev.ghen.thirst.foundation.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KeyWordConfig {
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_KEYWORD_CONFIG;

    public static final ForgeConfigSpec.ConfigValue<Number> DEFAULT_DRINK_HYDRATION;
    public static final ForgeConfigSpec.ConfigValue<Number> DEFAULT_DRINK_QUENCHNESS;
    public static final ForgeConfigSpec.ConfigValue<Number> DEFAULT_SOUP_HYDRATION;
    public static final ForgeConfigSpec.ConfigValue<Number> DEFAULT_SOUP_QUENCHNESS;
    public static final ForgeConfigSpec.ConfigValue<Number> DEFAULT_FRUIT_HYDRATION;
    public static final ForgeConfigSpec.ConfigValue<Number> DEFAULT_FRUIT_QUENCHNESS;
    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_DRINK;
    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_SOUP;

    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_FRUIT;


    static {
        BUILDER.push("Keyword config")
                .comment("This config allows for faster implementation of new thirst quenching items. It works by defining",
                        "lists of regular expressions to select items by their id name. It divides the items into categories",
                        "that have preset values for hydration and quenching");
        ENABLE_KEYWORD_CONFIG = BUILDER.comment("If the keyword config should be taken into consideration").define("enableKeywordConfig", false);

        BUILDER.push("Default Hydration values");
        DEFAULT_DRINK_HYDRATION = BUILDER.comment("Default hydration for drinks selected with keywords [0-20]")
                .define("defaultDrinkHydration", 10);
        DEFAULT_DRINK_QUENCHNESS = BUILDER.comment("Default quenchness for drinks selected with keywords [0-20]")
                .define("defaultDrinkQuenchness", 14);
        DEFAULT_SOUP_HYDRATION = BUILDER.comment("Default hydration for soups selected with keywords [0-20]")
                .define("defaultSoupHydration", 4);
        DEFAULT_SOUP_QUENCHNESS = BUILDER.comment("Default quenchness for soups selected with keywords [0-20]")
                .define("defaultSoupQuenchness", 5);
        DEFAULT_FRUIT_HYDRATION = BUILDER.comment("Default hydration for fruits selected with keywords [0-20]")
                .define("defaultFruitHydration", 2);
        DEFAULT_FRUIT_QUENCHNESS = BUILDER.comment("Default quenchness for fruits selected with keywords [0-20]")
                .define("defaultFruitQuenchness", 3);

        BUILDER.push("Blacklisted Keywords")
                .comment("The list of items to be ignored if they get selected by mistake by other keywords",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_BLACKLIST = BUILDER.define("keyword_blacklist", "(?:\\b|[^a-zA-Z])(dried|candied|leaf|leaves|gummy|crate|jam|sauce|bucket|seed|cookie|pie|bush|sapling|bean|curry|cake|candy)(?:\\b|[^a-zA-Z])");
        BUILDER.pop();

        BUILDER.push("Drink Keywords")
                .comment("List of keywords for drinks",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_DRINK = BUILDER.define("keyword_drink", "(?:\\b|[^a-zA-Z])(drink|juice|tea|soda|coffee|wine|beer|cider|yogurt|milkshake|smoothie)(?:\\b|[^a-zA-Z])");
        BUILDER.pop();

        BUILDER.push("Soup Keywords")
                .comment("List of keywords for soups",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_SOUP = BUILDER.define("keyword_soup", "(?:\\b|[^a-zA-Z])(soup|stew|porridge)(?:\\b|[^a-zA-Z])");
        BUILDER.pop();

        BUILDER.push("Fruit Keywords")
                .comment("List of keywords for fruits",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_FRUIT = BUILDER.define("keyword_fruit", "(?:\\b|[^a-zA-Z])(fruit|berry|berries|grape|orange|peach|pear|coconut|lemon|melon|cherry|apple)(?:\\b|[^a-zA-Z])");
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "thirst/keyword.toml");
    }

    public static int getDrinkHydration()
    {
        return DEFAULT_DRINK_HYDRATION.get().intValue();
    }

    public static int getDrinkQuenchness()
    {
        return DEFAULT_DRINK_QUENCHNESS.get().intValue();
    }

    public static int getSoupHydration()
    {
        return DEFAULT_SOUP_HYDRATION.get().intValue();
    }

    public static int getSoupQuenchness()
    {
        return DEFAULT_SOUP_QUENCHNESS.get().intValue();
    }

    public static int getFruitHydration()
    {
        return DEFAULT_FRUIT_HYDRATION.get().intValue();
    }

    public static int getFruitQuenchness()
    {
        return DEFAULT_FRUIT_QUENCHNESS.get().intValue();
    }
}
