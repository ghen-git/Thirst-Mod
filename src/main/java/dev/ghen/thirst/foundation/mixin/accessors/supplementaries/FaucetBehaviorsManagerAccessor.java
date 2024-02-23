package dev.ghen.thirst.foundation.mixin.accessors.supplementaries;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FaucetBehaviorsManager.class)
public interface FaucetBehaviorsManagerAccessor
{
    @Invoker(remap = false)
    static void invokePrepareToTransferBottle(SoftFluidTank tempFluidHolder, Holder<SoftFluid> softFluid, @Nullable CompoundTag tag){
        throw new AssertionError();
    }
}
