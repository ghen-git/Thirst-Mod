package dev.ghen.thirst.foundation.mixin.supplementaries;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.mixin.accessors.supplementaries.FaucetBehaviorsManagerAccessor;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.WaterBlockInteraction")
public class MixinWaterBlockInteraction
{
    @Inject(method = "tryDrain", at = @At("HEAD"), cancellable = true, remap = false)
    protected void addPurityBlockState(Level level, SoftFluidTank faucetTank, BlockPos pos, FluidState fluidState, FaucetBlockTile.FillAction fillAction, CallbackInfoReturnable<InteractionResult> cir) {
        if (fluidState.getType() != Fluids.WATER)
            return;

        CompoundTag purityTag = new CompoundTag();
        purityTag.putInt("Purity", WaterPurity.getBlockPurity(level, pos));

        FaucetBehaviorsManagerAccessor.invokePrepareToTransferBottle(faucetTank, (SoftFluid) BuiltInSoftFluids.WATER.get(), purityTag);
        if (fillAction == null || fillAction.tryExecute()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
