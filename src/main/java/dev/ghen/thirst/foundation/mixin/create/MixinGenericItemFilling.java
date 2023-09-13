package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value= GenericItemFilling.class,remap = false)
public class MixinGenericItemFilling {

    @Inject(method = "fillItem",at= @At("RETURN"), cancellable = true)
    private static void fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output=cir.getReturnValue();
        if(WaterPurity.hasPurity(availableFluid) && WaterPurity.isWaterFilledContainer(output)){
             WaterPurity.addPurity(output, WaterPurity.getPurity(availableFluid));
             cir.setReturnValue(output);
        }
    }

}
