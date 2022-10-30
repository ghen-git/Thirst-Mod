package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.content.contraptions.fluids.actors.FluidDrainingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.mixin.accessors.create.IFluidDrainingBehaviourAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidDrainingBehaviour.class)
public abstract class MixinFluidDrainingBehaviour
{
    @Shadow protected abstract boolean isSearching();

    @Shadow public abstract boolean pullNext(BlockPos root, boolean simulate);

    @Inject(method = "getDrainableFluid", at = @At("HEAD"), cancellable = true, remap = false)
    public void getDrainableFluid(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir)
    {
        FluidDrainingBehaviour behaviour = ((FluidDrainingBehaviour)(Object) this);
        if(((IFluidDrainingBehaviourAccessor)behaviour).getFluid() != null && !this.isSearching() && this.pullNext(rootPos, true))
        {
            FluidStack output = new FluidStack(((IFluidDrainingBehaviourAccessor)behaviour).getFluid(), 1000);
            if(FluidHelper.isWater(output.getFluid()))
            {
                CompoundTag tag = output.getOrCreateTag();
                tag.putInt("Purity", WaterPurity.getBlockPurity(behaviour.getWorld(), rootPos));
                output.setTag(tag);
                cir.setReturnValue(output);
            }
        }
    }
}
