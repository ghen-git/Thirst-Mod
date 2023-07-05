package dev.ghen.thirst.api;

import com.mojang.logging.LogUtils;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.config.ItemSettingsConfig;
import dev.ghen.thirst.foundation.util.ConfigHelper;
import dev.ghen.thirst.foundation.util.LoadedValue;
import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

import java.util.Map;

import static dev.ghen.thirst.content.purity.WaterPurity.hasPurity;

public class ThirstHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean useColdSweatCaps = false;
    private static final float MODIFIER_HARSHNESS = 0.5f;
    public static LoadedValue<Map<Item, Number[]>> VALID_DRINKS = LoadedValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.DRINKS.get()));
    public static LoadedValue<Map<Item, Number[]>> VALID_FOODS = LoadedValue.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.FOODS.get()));

    public static boolean itemRestoresThirst(ItemStack itemStack) {
        return isDrink(itemStack) ||
                isFood(itemStack);
    }

    public static boolean isDrink(ItemStack itemStack) {
        return !ItemSettingsConfig.ITEMS_BLACKLIST.get().contains(itemStack.getItem().toString()) &&
                VALID_DRINKS.get().containsKey(itemStack.getItem());
    }


    public static boolean isFood(ItemStack itemStack) {
        return !ItemSettingsConfig.ITEMS_BLACKLIST.get().contains(itemStack.getItem().toString()) &&
                VALID_FOODS.get().containsKey(itemStack.getItem());
    }

    /**
     * Adds a hydration and "quenchness" value to an item via code, and treats it as food.
     * Can be overwritten by the player in the config.
     */
    public static void addFood(Item item, int thirst, int quenched) {
        VALID_FOODS.get().put(item, new Number[]{thirst, quenched});
    }

    /**
     * Adds a hydration and "quenchness" value to an item via code, and treats it as a drink.
     * Can be overwritten by the player in the config.
     */
    public static void addDrink(Item item, int thirst, int quenched) {
        VALID_DRINKS.get().put(item, new Number[]{thirst, quenched});
    }

    public static int getThirst(ItemStack itemStack) {
        Item item = itemStack.getItem();

        if (VALID_DRINKS.get().containsKey(item))
            return VALID_DRINKS.get().get(item)[0].intValue();
        else
            return VALID_FOODS.get().get(item)[0].intValue();
    }

    public static int getQuenched(ItemStack itemStack) {
        Item item = itemStack.getItem();

        if (VALID_DRINKS.get().containsKey(item))
            return VALID_DRINKS.get().get(item)[1].intValue();
        else
            return VALID_FOODS.get().get(item)[1].intValue();
    }

    public static int getPurity(ItemStack item) {
        if (!hasPurity(item))
            return -1;
        else
            return item.getTag().getInt("Purity");
    }

    public static void shouldUseColdSweatCaps(boolean should) {
        useColdSweatCaps = should;
    }

    /**
     * Calculates the thirst depletion speed modifier based on the player's
     * temperature and humidity. If the mod "Cold Sweat" is present, the temperature used is
     * the one calculated from the mod, otherwise both parameters are entirely
     * dependent on the biome the player is standing in.
     */
    public static float getExhaustionBiomeModifier(Player player) {
        BlockPos pos = player.getOnPos();
        Level level = player.getLevel();

        if (level.dimensionType().ultraWarm())
            return 3.0f;
        else {
            Biome biome = level.getBiome(pos).value();

            //humidity range: 0 - 0.8 == 0.8 midpoint: 0.4
            float humidity = biome.getDownfall() + 0.6f;
            if (humidity <= 0.6)
                humidity += 0.5;

            //temperature range: -0.8 - 2 == 2.8 midpoint: 0.8
            float temp = biome.getBaseTemperature() + 0.2f;

            if (useColdSweatCaps) {
                temp = (float) (Temperature.get(player, Temperature.Type.BODY) / 100f);
            } else {
                if (temp <= 0)
                    temp = (float) Math.exp(temp);
                else if (temp > 1)
                    temp /= 2;
            }

            float thirstModifier = CommonConfig.THIRST_DEPLETION_MODIFIER.get().floatValue() * (temp / humidity);

            if (thirstModifier < 1) {
                float modifierOffset = 1 - thirstModifier;
                modifierOffset *= MODIFIER_HARSHNESS;
                thirstModifier = 1 - modifierOffset;
            }

            return thirstModifier;
        }
    }
}
