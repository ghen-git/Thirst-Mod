package dev.ghen.thirst.foundation.mixin.accessors.create;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidDrainingBehaviour.class)
public interface IFluidDrainingBehaviourAccessor
{
    @Accessor
    Fluid getFluid();
}
