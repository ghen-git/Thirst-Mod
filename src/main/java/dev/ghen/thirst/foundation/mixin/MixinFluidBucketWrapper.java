package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidBucketWrapper.class,remap = false)
public class MixinFluidBucketWrapper {
    @Shadow @NotNull protected ItemStack container;

    /**
     * @author mlus
     * @reason add purity to fluid stack in bucket
     */
    @Overwrite
    public @NotNull FluidStack getFluid() {
        Item item = container.getItem();
        if (item instanceof BucketItem) {
            FluidStack stack = new FluidStack(((BucketItem) item).getFluid(), 1000);
            if(WaterPurity.hasPurity(container)){
               WaterPurity.addPurity(stack,WaterPurity.getPurity(container));
            }
            return stack;
        } else {
            return item instanceof MilkBucketItem && ForgeMod.MILK.isPresent() ? new FluidStack(ForgeMod.MILK.get(), 1000) : FluidStack.EMPTY;
        }
    }
}
