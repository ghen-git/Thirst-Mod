package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.ghen.thirst.content.purity.WaterPurity;

@Mixin(value = FluidDrainingBehaviour.class,remap = false)
public abstract class MixinFluidDrainingBehaviour
{

    @Inject(method = "getDrainableFluid", at = @At("RETURN"), remap = false, cancellable = true)
    public void getDrainableFluid(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir){
        FluidDrainingBehaviour behaviour = ((FluidDrainingBehaviour)(Object) this);
        FluidStack output=cir.getReturnValue();
        if (FluidHelper.isWater(output.getFluid())){
            CompoundTag tag = output.getOrCreateTag();
                tag.putInt("Purity", WaterPurity.getBlockPurity(behaviour.getWorld(), rootPos));
                output.setTag(tag);
                cir.setReturnValue(output);
        }
    }
}
