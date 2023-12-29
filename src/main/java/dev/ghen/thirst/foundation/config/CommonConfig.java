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


    public static final ForgeConfigSpec.ConfigValue<Number> THIRST_DEPLETION_MODIFIER;
    public static final ForgeConfigSpec.IntValue FIRE_RESISTANCE_DEHYDRATION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MOVE_SLOW_WHEN_THIRSTY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_DRINKS_NUTRITION;
    public static final ForgeConfigSpec.ConfigValue<Integer> WATER_BOTTLE_STACKSIZE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DEHYDRATION_HALTS_HEALTH_REGEN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HEALTH_REGEN_DEHYDRATION_IS_BIOME_DEPENDENT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HEALTH_REGEN_DEPLETES_HYDRATION;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CAN_DRINK_BY_HAND;
    public static final ForgeConfigSpec.ConfigValue<Number> HAND_DRINKING_HYDRATION;
    public static final ForgeConfigSpec.ConfigValue<Number> HAND_DRINKING_QUENCHED;

    public static final ForgeConfigSpec.ConfigValue<Number> MOUNTAINS_Y;
    public static final ForgeConfigSpec.ConfigValue<Number> CAVES_Y;
    public static final ForgeConfigSpec.ConfigValue<Number> RUNNING_WATER_PURIFICATION_AMOUNT;

    public static final ForgeConfigSpec.ConfigValue<Integer> DEFAULT_PURITY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> QUENCH_THIRST_WHEN_DEBUFFED;
    public static final ForgeConfigSpec.ConfigValue<Number> DIRTY_POISON_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> DIRTY_NAUSEA_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> SLIGHTLY_DIRTY_POISON_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> SLIGHTLY_DIRTY_NAUSEA_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> ACCEPTABLE_POISON_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> ACCEPTABLE_NAUSEA_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> PURIFIED_POISON_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> PURIFIED_NAUSEA_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Number> KETTLE_PURIFICATION_LEVELS;

    public static final ForgeConfigSpec.ConfigValue<Number> FERMENTATION_MOLDING_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Number> FERMENTATION_MOLDING_HARSHNESS;

    public static final ForgeConfigSpec.ConfigValue<Number> SAND_FILTER_FILTRATION_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Number> SAND_FILTER_MB_PER_TICK;

    static
    {
        BUILDER.push("General");
        THIRST_DEPLETION_MODIFIER = BUILDER.comment("How much faster is hydration depletion relative to hunger (1 means they will deplete at the same speed)").define("thirstDepletionModifier", 1.2);
        FIRE_RESISTANCE_DEHYDRATION = BUILDER.comment("How much faster is hydration depletion when players with fire resistance(Range 0 to 100, 0 means not to depletion)").defineInRange("fireResistanceDehydration",0,0,100);
        MOVE_SLOW_WHEN_THIRSTY=BUILDER.comment("Whether players won't be able to sprint if their thirst bar is 3 droplets or less").define("moveSlowWhenThirsty",true);
        ENABLE_DRINKS_NUTRITION=BUILDER.comment("Whether foods labeled as drinks will restore hunger").define("enableDrinksNutrition",true);
        BUILDER.pop();

        BUILDER.push("Drinking Mechanics");
        WATER_BOTTLE_STACKSIZE = BUILDER.comment("Stack size for water bottles").define("waterBottleStacksize", 64);
        DEHYDRATION_HALTS_HEALTH_REGEN = BUILDER.comment("Whether the player can't regenerate as fast when hydration isn't full (like hunger)").define("dehydrationHaltsHealthRegen", true);
        HEALTH_REGEN_DEPLETES_HYDRATION = BUILDER.comment("Whether hydration depletes when the player's health is regenerating (like hunger)").define("healthRegenDepletesHydration", true);
        HEALTH_REGEN_DEHYDRATION_IS_BIOME_DEPENDENT = BUILDER.comment("Whether dehydration from regenerating health (if enabled above) should take into account temperature and humidity").define("healthRegenDehydrationIsBiomeDependent", true);
        CAN_DRINK_BY_HAND = BUILDER.comment("Whether players can drink by shift-right-clicking water with an empty hand").define("canDrinkByHand", false);
        HAND_DRINKING_HYDRATION = BUILDER.comment("How much the player is hydrated when drinking by hand").define("handDrinkingHydration", 3);
        HAND_DRINKING_QUENCHED = BUILDER.comment("How much the player thirst is quenched when drinking by hand").define("handDrinkingQuenched", 2);
        BUILDER.pop();

        BUILDER.push("World");
        MOUNTAINS_Y = BUILDER.comment("Y level above which water has 1 more level of purification by default (i.e Mountains)").define("mountainsY", 100);
        CAVES_Y = BUILDER.comment("Y level below which water has 1 more level of purification by default (i.e Caves) (for aquatic biomes, this number will be decreased by 32)").define("cavesY", 48);
        RUNNING_WATER_PURIFICATION_AMOUNT = BUILDER.comment("How many levels of purification does running water have compared to still water").define("runningWaterPurificationAmount", 1);
        BUILDER.pop();

        BUILDER.push("Purity-related Effects");
        DEFAULT_PURITY =  BUILDER.comment("Purity for drinks that normally have purity but for whatever reason don't have a value set").define("defaultPurity", 3);
        QUENCH_THIRST_WHEN_DEBUFFED =  BUILDER.comment("Whether player should gain hydration even if they recieved a purity-related debuff").define("quenchThirstWhenDebuffed", true);
        DIRTY_POISON_PERCENTAGE =  BUILDER.comment("% of getting poisoned after drinking dirty water").define("dirtyPoisonPercentage", 30);
        DIRTY_NAUSEA_PERCENTAGE =  BUILDER.comment("% of getting sick (hunger and nausea) after drinking dirty water").define("dirtyNauseaPercentage", 100);
        SLIGHTLY_DIRTY_POISON_PERCENTAGE =  BUILDER.comment("% of getting poisoned after drinking slightly dirty water").define("slightlyDirtyPoisonPercentage", 10);
        SLIGHTLY_DIRTY_NAUSEA_PERCENTAGE =  BUILDER.comment("% of getting sick (hunger and nausea) after drinking slightly dirty water").define("slightlyDirtyNauseaPercentage", 50);
        ACCEPTABLE_POISON_PERCENTAGE =  BUILDER.comment("% of getting poisoned after drinking acceptable water").define("acceptablePoisonPercentage", 0);
        ACCEPTABLE_NAUSEA_PERCENTAGE =  BUILDER.comment("% of getting sick (hunger and nausea) after drinking acceptable water").define("acceptableNauseaPercentage", 5);
        PURIFIED_POISON_PERCENTAGE =  BUILDER.comment("% of getting poisoned after drinking purified water").define("purifiedPoisonPercentage", 0);
        PURIFIED_NAUSEA_PERCENTAGE =  BUILDER.comment("% of getting sick (hunger and nausea) after drinking purified water").define("purifiedNauseaPercentage", 0);
        BUILDER.pop();

        BUILDER.push("Purification levels");
        KETTLE_PURIFICATION_LEVELS = BUILDER.comment("How many levels of purification are added after boiling in a kettle").define("kettlePurificationLevels", 2);
        BUILDER.pop();

        BUILDER.push("Fermentation levels");
        FERMENTATION_MOLDING_THRESHOLD = BUILDER.comment("Purification level below which fermented liquids will grow bacteria and get less purified").define("fermentationMoldingThreshold", 3);
        FERMENTATION_MOLDING_HARSHNESS = BUILDER.comment("Decrement of purification levels if water isn't purified enough when fermenting").define("fermentationMoldingHarshness", 2);
        BUILDER.pop();

        BUILDER.push("Create compatibility");
        SAND_FILTER_FILTRATION_AMOUNT = BUILDER.comment("Purification levels gained by filtering water through a Sand Filter").define("sandFilterFiltrationAmount", 1);
        SAND_FILTER_MB_PER_TICK = BUILDER.comment("Millibuckets of water filtered per game tick with a Sand Filter").define("sandFilterMbPerTick", 10);
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
}
