package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = FluidUtil.class,remap = false)
public class MixinFluidUtil {
    /**
     * @author mlus
     * @reason add purity to the result bucket
     */
    @Overwrite
    public static @NotNull ItemStack getFilledBucket(@NotNull FluidStack fluidStack) {
        Fluid fluid = fluidStack.getFluid();
        if (!fluidStack.hasTag() || fluidStack.getTag().isEmpty()) {
            if (fluid == Fluids.WATER) {
                return new ItemStack(Items.WATER_BUCKET);
            }

            if (fluid == Fluids.LAVA) {
                return new ItemStack(Items.LAVA_BUCKET);
            }
        }else if(WaterPurity.hasPurity(fluidStack)){
            return WaterPurity.addPurity(new ItemStack(fluidStack.getFluid().getBucket()),WaterPurity.getPurity(fluidStack));
        }

        return new ItemStack(fluidStack.getFluid().getBucket());
    }
}
