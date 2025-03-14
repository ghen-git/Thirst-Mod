package dev.ghen.thirst.foundation.mixin.brewinandchewin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

@Mixin(value = KegFermentingRecipe.class,remap = false)
public class MixinKegFermentingRecipe {
    @Redirect(method = "matches(Lumpaz/brewinandchewin/common/utility/KegRecipeWrapper;Lnet/minecraft/world/level/Level;)Z",at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/FluidStack;isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z"))
    private boolean matches(FluidStack instance, FluidStack other){
        FluidStack stack = instance.copy();
        stack.removeChildTag("Purity");
        return WaterPurity.matchRecipe(stack,other);
    }
}
