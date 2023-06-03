package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.foundation.utility.Pair;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(GenericItemEmptying.class)
public class MixinEmptyingByBasin
{
    //@Shadow
    private static RecipeWrapper wrapper;

    @Inject(method = "emptyItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void emptyItem(Level world, ItemStack stack, boolean simulate, CallbackInfoReturnable<Pair<FluidStack, ItemStack>> cir)
    {
        FluidStack resultingFluid = FluidStack.EMPTY;
        ItemStack resultingItem = ItemStack.EMPTY;

        if (!WaterPurity.hasPurity(stack))
            return;

        wrapper.setItem(0, stack);
        Optional<Recipe<RecipeWrapper>> recipe = AllRecipeTypes.EMPTYING.find(wrapper, world);

        if (recipe.isPresent())
        {
            EmptyingRecipe emptyingRecipe = (EmptyingRecipe)recipe.get();
            List<ItemStack> results = emptyingRecipe.rollResults();
            if (!simulate) {
                stack.shrink(1);
            }

            resultingItem = results.isEmpty() ? ItemStack.EMPTY : results.get(0);
            resultingFluid = emptyingRecipe.getResultingFluid();

            if(!resultingFluid.isEmpty())
            {
                CompoundTag tag = resultingFluid.getOrCreateTag();
                tag.putInt("Purity", WaterPurity.getPurity(stack));
                resultingFluid.setTag(tag);
            }

            cir.setReturnValue(Pair.of(resultingFluid, resultingItem));
            return;
        }

        ItemStack split = stack.copy();
        split.setCount(1);
        LazyOptional<IFluidHandlerItem> capability =
                split.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem tank = capability.orElse(null);

        if (tank == null) {
            if(!resultingFluid.isEmpty())
            {
                CompoundTag tag = resultingFluid.getOrCreateTag();
                tag.putInt("Purity", WaterPurity.getPurity(stack));
                resultingFluid.setTag(tag);
            }

            cir.setReturnValue(Pair.of(resultingFluid, resultingItem));
            return;
        }

        resultingFluid = tank.drain(1000, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        resultingItem = tank.getContainer().copy();
        if (!simulate) {
            stack.shrink(1);
        }

        CompoundTag tag = resultingFluid.getOrCreateTag();
        tag.putInt("Purity", WaterPurity.getPurity(stack));
        resultingFluid.setTag(tag);

        cir.setReturnValue(Pair.of(resultingFluid, resultingItem));
    }
}
