package dev.ghen.thirst.foundation.mixin

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.forge.xplat.ForgeXplatImpl;
import vazkii.botania.xplat.XplatAbstractions;


@Mixin(ForgeXplatImpl.class)
public abstract class MixinBotania implements XplatAbstractions {
    @Inject(method = "extractFluidFromPlayerItem", at = @At("HEAD"), cancellable = true, remap = false)
    public void extractFluidFromPlayerItem(Player player, InteractionHand hand, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
        ItemStack a = player.getItemInHand(hand);
        ItemStack abc = new ItemStack(a.getItem());
        cir.setReturnValue(abc.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map((h) -> {
            FluidStack ex = h.drain(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE);
            boolean su = ex.getFluid() == fluid && ex.getAmount() == 1000;
            if (su && !player.getAbilities().instabuild) {
                h.drain(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                player.setItemInHand(hand, h.getContainer());
            }
            return su;
        }).orElse(false));
    }
}
