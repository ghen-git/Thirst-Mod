package dev.ghen.thirst.api;

import com.momosoftworks.coldsweat.api.util.Temperature;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.config.ItemSettingsConfig;
import dev.ghen.thirst.foundation.config.KeyWordConfig;
import dev.ghen.thirst.foundation.util.ConfigHelper;
import dev.ghen.thirst.foundation.util.LoadedValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.ghen.thirst.content.purity.WaterPurity.hasPurity;

public class ThirstHelper
{
    private static boolean useColdSweatCaps = false;
    private static final float MODIFIER_HARSHNESS = 0.5f;
    public static Map<Item, Number[]> VALID_DRINKS = LoadedValue.of(() -> ConfigHelper
            .getItemsWithValues(ItemSettingsConfig.DRINKS.get()))
            .get();
    public static Map<Item, Number[]> VALID_FOODS = LoadedValue.of(() -> ConfigHelper
            .getItemsWithValues(ItemSettingsConfig.FOODS.get()))
            .get();

    public static String keywordBlackList = KeyWordConfig.KEYWORD_BLACKLIST.get();
    public static String keywordDrink = KeyWordConfig.KEYWORD_DRINK.get();
    public static String keywordSoup = KeyWordConfig.KEYWORD_SOUP.get();
    public static String keywordFruit = KeyWordConfig.KEYWORD_FRUIT.get();

    public static boolean itemRestoresThirst(ItemStack itemStack)
    {
        return isDrink(itemStack) ||
                isFood(itemStack) || checkKeywords(itemStack);
    }

    public static boolean isDrink(ItemStack itemStack)
    {
        return !ItemSettingsConfig.ITEMS_BLACKLIST.get().contains(itemStack.getItem().toString()) &&
                VALID_DRINKS.containsKey(itemStack.getItem());
    }


    public static boolean isFood(ItemStack itemStack)
    {
        return !ItemSettingsConfig.ITEMS_BLACKLIST.get().contains(itemStack.getItem().toString()) &&
                VALID_FOODS.containsKey(itemStack.getItem());
    }

    /**
     * Adds a hydration and "quenchness" value to an item via code, and treats it as food.
     * Can be overwritten by the player in the config.
     * */
    @SuppressWarnings("unused")
    public static void addFood(Item item, int thirst, int quenched)
    {
        VALID_FOODS.put(item, new Number[]{thirst, quenched});
    }

    /**
     * Adds a hydration and "quenchness" value to an item via code, and treats it as a drink.
     * Can be overwritten by the player in the config.
     * */
    @SuppressWarnings("unused")
    public static void addDrink(Item item, int thirst, int quenched)
    {
        VALID_DRINKS.put(item, new Number[]{thirst, quenched});
    }

    public static int getThirst(ItemStack itemStack)
    {
        Item item = itemStack.getItem();

        if(VALID_DRINKS.containsKey(item)) {
            if (!CommonConfig.ENABLE_DRINKS_NUTRITION.get()){
                if (item.getFoodProperties() != null) {
                    Objects.requireNonNull(item.getFoodProperties()).nutrition = 0;
                }
            }
            return VALID_DRINKS.get(item)[0].intValue();
        }
        else
            return VALID_FOODS.get(item)[0].intValue();
    }

    public static int getQuenched(ItemStack itemStack)
    {
        Item item = itemStack.getItem();

        if(VALID_DRINKS.containsKey(item))
            return VALID_DRINKS.get(item)[1].intValue();
        else
            return VALID_FOODS.get(item)[1].intValue();
    }

    public static int getPurity(ItemStack item)
    {
        if(!hasPurity(item))
            return -1;
        else {
            assert item.getTag() != null;
            return item.getTag().getInt("Purity");
        }
    }

    public static void shouldUseColdSweatCaps(boolean should)
    {
        useColdSweatCaps = should;
    }
    public static float getExhaustionFireProtModifier(Player player)
    {
        final float perLevelMultiplier = 0.0625f;
        int totalLevels = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), player.damageSources().onFire()) / 2;

        return 1.0f - ((totalLevels * perLevelMultiplier) * 0.75f);
    }

    public static float getExhaustionFireResistanceModifier(Player player){
        if(player.hasEffect(MobEffects.FIRE_RESISTANCE)){
            return CommonConfig.FIRE_RESISTANCE_DEHYDRATION.get();
        }else return 1.0f;
    }

    /**
     * Calculates the thirst depletion speed modifier based on the player's
     * temperature and humidity. If the mod "Cold Sweat" is present, the temperature used is
     * the one calculated from the mod, otherwise both parameters are entirely
     * dependent on the biome the player is standing in.
     */
    public static float getExhaustionBiomeModifier(Player player)
    {
        BlockPos pos = player.getOnPos();
        Level level = player.level();

        if(level.dimensionType().ultraWarm())
            return 3.0f;
        else
        {
            Biome biome = level.getBiome(pos).value();

            //humidity range: 0 - 0.8 == 0.8 midpoint: 0.4
            float humidity = biome.getModifiedClimateSettings().downfall() + 0.6f;
            if(humidity <= 0.6)
                humidity += 0.5;

            //temperature range: -0.8 - 2 == 2.8 midpoint: 0.8
            float temp = biome.getBaseTemperature() + 0.2f;

            if(useColdSweatCaps)
                {
                    temp = (float) (Temperature.get(player, Temperature.Type.BODY) / 100f);
                }
            else
            {
                if(temp <= 0)
                    temp = (float) Math.exp(temp);
                else if(temp > 1)
                    temp /= 2;
            }

            float thirstModifier = CommonConfig.THIRST_DEPLETION_MODIFIER.get().floatValue() * (temp  / humidity);

            if(thirstModifier < 1)
            {
                float modifierOffset = 1 - thirstModifier;
                modifierOffset *= MODIFIER_HARSHNESS;
                thirstModifier = 1 - modifierOffset;
            }

            return thirstModifier;
        }
    }

    /**
     * Function from Thirst Was Remade, handles water items added from the
     * keyword config file
     * @param itemStack item to be checked
     * @return if the item contains water
     */

    private static boolean checkKeywords(ItemStack itemStack)
    {
        if(!KeyWordConfig.ENABLE_KEYWORD_CONFIG.get())
            return false;

        if(!itemStack.isEdible())
            return false;

        String pattern= keywordBlackList;
        Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(itemStack.getDescriptionId());

        if(matcher.find())
            return false;

        pattern = keywordDrink;
        matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(itemStack.getDescriptionId());

        boolean hasWater=matcher.find();
        if(hasWater)
        {
            VALID_DRINKS.put(itemStack.getItem(), new Number[]{
                    KeyWordConfig.getDrinkHydration(),
                    KeyWordConfig.getDrinkQuenchness()
            });
            return true;
        }

        pattern=keywordSoup;
        matcher= Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(itemStack.getDescriptionId());

        hasWater=matcher.find();
        if(hasWater)
        {
            VALID_FOODS.put(itemStack.getItem(), new Number[]{
                    KeyWordConfig.getSoupHydration(),
                    KeyWordConfig.getSoupQuenchness()
            });
            return true;
        }

        pattern=keywordFruit;
        matcher= Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                .matcher(itemStack.getDescriptionId());

        hasWater=matcher.find();
        if(hasWater)
            VALID_FOODS.put(itemStack.getItem(), new Number[]{
                    KeyWordConfig.getFruitHydration(),
                    KeyWordConfig.getFruitQuenchness()
            });

        return hasWater;
    }
}
