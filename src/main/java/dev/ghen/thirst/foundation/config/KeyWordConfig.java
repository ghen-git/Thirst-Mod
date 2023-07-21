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

    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_DRINK;
    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_SOUP;

    public static final ForgeConfigSpec.ConfigValue<String> KEYWORD_FRUIT;


    static {
        BUILDER.push("BlackList Keyword")
                .comment("Defines items will be ignored if it contains the keyword",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_BLACKLIST = BUILDER.define("keyword_blacklist", "(?:\\b|[^a-zA-Z])(dried|candied|leaf|leaves|gummy|crate|jam|sauce|bucket|seed|cookie|pie|bush|sapling|bean|curry|cake|candy)(?:\\b|[^a-zA-Z])");
        BUILDER.pop();

        BUILDER.push("Drink KeyWord")
                .comment("Defines items will be considered as drink if it contains the keyword",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_DRINK = BUILDER.define("keyword_drink", "(juice|yogurt|milkshade|smoothie|(?:\\b|[^a-zA-Z])(drink|tea|soda|coffee|wine|beer|cider|)(?:\\b|[^a-zA-Z]))");
        BUILDER.pop();

        BUILDER.push("Soup KeyWord")
                .comment("Defines items will be considered as soup if it contains the keyword",
                        "Format: [(keyword1|keyword2|keyword3)]");
        KEYWORD_SOUP = BUILDER.define("keyword_soup", "(?:\\b|[^a-zA-Z])(soup|stew|porridge)(?:\\b|[^a-zA-Z])");
        BUILDER.pop();

        BUILDER.push("Fruit KeyWord")
                .comment("Defines items will be considered as fruit if it contains the keyword",
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
}
