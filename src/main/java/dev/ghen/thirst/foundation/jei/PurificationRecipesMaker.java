package dev.ghen.thirst.foundation.jei;

import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

public class PurificationRecipesMaker
{
    private PurificationRecipesMaker() {}

    public static List<CampfireCookingRecipe> getCampfireRecipes()
    {
        List<CampfireCookingRecipe> recipes = new ArrayList<>();

        for(int i = 0; i < WaterPurity.MAX_PURITY; i++)
        {
            recipes.add(new CampfireCookingRecipe(
                    Thirst.asResource("bottle_purification"),
                    "",
                    Ingredient.of(WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), i)),
                    WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), Math.min(i + CommonConfig.CAMPFIRE_PURIFICATION_LEVELS.get().intValue(), WaterPurity.MAX_PURITY)),
                    0.35f,
                    300
            ));
            recipes.add(new CampfireCookingRecipe(
                    Thirst.asResource("bucket_purification"),
                    "",
                    Ingredient.of(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), i)),
                    WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), Math.min(i + CommonConfig.CAMPFIRE_PURIFICATION_LEVELS.get().intValue(), WaterPurity.MAX_PURITY)),
                    0.35f,
                    300
            ));
        }

        return recipes;
    }

    public static List<SmeltingRecipe> getFurnaceRecipes()
    {
        List<SmeltingRecipe> recipes = new ArrayList<>();

        for(int i = 0; i < WaterPurity.MAX_PURITY; i++)
        {
            recipes.add(new SmeltingRecipe(
                    Thirst.asResource("bottle_purification"),
                    "",
                    Ingredient.of(WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), i)),
                    WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), Math.min(i + CommonConfig.FURNACE_PURIFICATION_LEVELS.get().intValue(), WaterPurity.MAX_PURITY)),
                    0.35f,
                    200
            ));
            recipes.add(new SmeltingRecipe(
                    Thirst.asResource("bucket_purification"),
                    "",
                    Ingredient.of(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), i)),
                    WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), Math.min(i + CommonConfig.FURNACE_PURIFICATION_LEVELS.get().intValue(), WaterPurity.MAX_PURITY)),
                    0.35f,
                    200
            ));
        }

        return recipes;
    }
}
