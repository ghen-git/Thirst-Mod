package dev.ghen.thirst.foundation.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{

    @Override
    public @NotNull ResourceLocation getPluginUid()
    {
        return new ResourceLocation("thirst", "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(RecipeTypes.CAMPFIRE_COOKING, PurificationRecipesMaker.getCampfireRecipes());
        registration.addRecipes(RecipeTypes.SMELTING, PurificationRecipesMaker.getFurnaceRecipes());
    }
}
