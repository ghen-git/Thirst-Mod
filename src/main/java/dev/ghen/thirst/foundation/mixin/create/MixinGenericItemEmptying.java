package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GenericItemEmptying.class,remap = false)
public class MixinGenericItemEmptying
{

    @Inject(method = "emptyItem", at = @At("RETURN"), cancellable = true, remap = false)
    private static void emptyItem(Level world, ItemStack stack, boolean simulate, CallbackInfoReturnable<Pair<FluidStack, ItemStack>> cir)
    {
        Pair<FluidStack,ItemStack> output= cir.getReturnValue();
        if(WaterPurity.hasPurity(stack)){
            FluidStack fluidStack=output.getFirst();
            if(fluidStack.isEmpty()) return;
            WaterPurity.addPurity(fluidStack,WaterPurity.getPurity(stack));
            cir.setReturnValue(Pair.of(fluidStack,output.getSecond()));
        }
    }
}
