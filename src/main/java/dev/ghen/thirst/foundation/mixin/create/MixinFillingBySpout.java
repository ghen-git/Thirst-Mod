package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(value = FillingBySpout.class,remap = false)
public class MixinFillingBySpout
{
    @Final
    @Shadow
    private static RecipeWrapper WRAPPER;

    @Inject(method = "fillItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir)
    {
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);
        WRAPPER.setItem(0, stack);

        if(WaterPurity.hasPurity(availableFluid))
        {
            int purity = WaterPurity.getPurity(availableFluid);

            FillingRecipe fillingRecipe = SequencedAssemblyRecipe.getRecipe(world, WRAPPER, AllRecipeTypes.FILLING.getType(), FillingRecipe.class).filter((fr) ->
                    fr.getRequiredFluid().test(toFill)).orElseGet(() ->
            {
                Iterator<Recipe<RecipeWrapper>> var2 = world.getRecipeManager().getRecipesFor(AllRecipeTypes.FILLING.getType(), WRAPPER, world).iterator();

                FillingRecipe fr;
                FluidIngredient requiredFluid;
                do {
                    if (!var2.hasNext()) {
                        return null;
                    }

                    Recipe<RecipeWrapper> recipe = var2.next();
                    fr = (FillingRecipe)recipe;
                    requiredFluid = fr.getRequiredFluid();
                } while(!requiredFluid.test(toFill));

                return fr;
            });
            if (fillingRecipe != null) {
                List<ItemStack> results = fillingRecipe.rollResults();
                availableFluid.shrink(requiredAmount);
                stack.shrink(1);
                cir.setReturnValue(results.isEmpty() ?
                        ItemStack.EMPTY : WaterPurity.isWaterFilledContainer(results.get(0))?
                        WaterPurity.addPurity(results.get(0),purity):results.get(0));
            } else {
                ItemStack output = GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
                cir.setReturnValue(output);
            }
        }
    }
}
