package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OpenEndedPipe.class,remap = false)
public class MixinOpenEndedPipe
{

    @Inject(method = "removeFluidFromSpace", at = @At("HEAD"), cancellable = true, remap = false)
    private void removeFluidFromSpace(boolean simulate, CallbackInfoReturnable<FluidStack> cir)
    {
        OpenEndedPipe pipe = ((OpenEndedPipe)(Object)this);

        if(pipe.getWorld() != null && pipe.getWorld().isLoaded(pipe.getOutputPos()))
        {
            BlockState state = pipe.getWorld().getBlockState(pipe.getOutputPos());
            FluidState fluidState = state.getFluidState();
            boolean waterlog = state.hasProperty(BlockStateProperties.WATERLOGGED);

            if ((!fluidState.isEmpty() && fluidState.isSource())&&(waterlog || state.canBeReplaced()))
            {
                FluidStack stack = new FluidStack(fluidState.getType(), 1000);
                if(FluidHelper.isWater(stack.getFluid()))
                {
                    CompoundTag tag = stack.getOrCreateTag();
                    tag.putInt("Purity", WaterPurity.getBlockPurity(pipe.getWorld(), pipe.getOutputPos()));
                    stack.setTag(tag);

                    if (simulate)
                    {
                        cir.setReturnValue(stack);
                    }
                    else
                    {
                        AdvancementBehaviour.tryAward(pipe.getWorld(), pipe.getPos(), AllAdvancements.WATER_SUPPLY);

                        if (waterlog)
                        {
                            pipe.getWorld().setBlock(pipe.getOutputPos(), state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
                            pipe.getWorld().scheduleTick(pipe.getOutputPos(), Fluids.WATER, 1);
                            cir.setReturnValue(stack);
                        } else {
                            pipe.getWorld().setBlock(pipe.getOutputPos(), fluidState.createLegacyBlock().setValue(LiquidBlock.LEVEL, 14), 3);
                            cir.setReturnValue(stack);
                        }
                    }
                }
            }
        }
    }
}
