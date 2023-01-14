package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.fluids.actors.FillingBySpout;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(FillingBySpout.class)
public class MixinFillingBySpout
{
    @Shadow
    static RecipeWrapper wrapper;

    @Inject(method = "fillItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir)
    {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);
        wrapper.setItem(0, stack);

        if(availableFluid.hasTag() && availableFluid.getTag().contains("Purity"))
        {
            int purity = availableFluid.getTag().getInt("Purity");

            FillingRecipe fillingRecipe = (FillingRecipe) SequencedAssemblyRecipe.getRecipe(world, wrapper, AllRecipeTypes.FILLING.getType(), FillingRecipe.class).filter((fr) ->
                    fr.getRequiredFluid().test(toFill)).orElseGet(() ->
            {
                Iterator var2 = world.getRecipeManager().getRecipesFor(AllRecipeTypes.FILLING.getType(), wrapper, world).iterator();

                FillingRecipe fr;
                FluidIngredient requiredFluid;
                do {
                    if (!var2.hasNext()) {
                        return null;
                    }

                    Recipe<RecipeWrapper> recipe = (Recipe)var2.next();
                    fr = (FillingRecipe)recipe;
                    requiredFluid = fr.getRequiredFluid();
                } while(!requiredFluid.test(toFill));

                return fr;
            });
            if (fillingRecipe != null) {
                List<ItemStack> results = fillingRecipe.rollResults();
                availableFluid.shrink(requiredAmount);
                stack.shrink(1);
                cir.setReturnValue(results.isEmpty() ? ItemStack.EMPTY : WaterPurity.addPurity((ItemStack)results.get(0), purity));
            } else {
                ItemStack output = GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
                WaterPurity.addPurity(output, purity);
                cir.setReturnValue(output);
            }
        }
    }
}
