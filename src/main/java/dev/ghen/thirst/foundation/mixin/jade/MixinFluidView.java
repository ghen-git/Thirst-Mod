package dev.ghen.thirst.foundation.mixin.jade;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import snownee.jade.api.view.FluidView;

import java.util.Objects;

@Mixin(value = FluidView.class,remap = false)
public class MixinFluidView {

    @Redirect(method ="read", at = @At(value = "INVOKE",target = "Lnet/minecraftforge/fluids/FluidStack;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private static Component read(FluidStack instance){
        if(WaterPurity.hasPurity(instance)){
            return Component.literal(Objects.requireNonNull(
                    WaterPurity.getPurityText(WaterPurity.getPurity(instance))))
                    .append(" ")
                    .append(instance.getDisplayName());

        }

        return instance.getDisplayName();
    }
}
