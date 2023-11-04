package dev.ghen.thirst.foundation.mixin.jade;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.util.CommonProxy;

import java.util.Objects;

@Mixin(value = FluidView.class,remap = false)
public class MixinFluidView {
    @Redirect(method ="readDefault", at = @At(value = "INVOKE",target = "Lsnownee/jade/util/CommonProxy;getFluidName(Lsnownee/jade/api/fluid/JadeFluidObject;)Lnet/minecraft/network/chat/Component;"))
    private static Component read(JadeFluidObject fluid){
        FluidStack instance = CommonProxy.toFluidStack(fluid);
        if(instance.isEmpty()) return CommonProxy.getFluidName(fluid);

        if(WaterPurity.hasPurity(instance) && WaterPurity.getPurity(instance)!=-1){
            return Component.literal(Objects.requireNonNull(
                    WaterPurity.getPurityText(WaterPurity.getPurity(instance))))
                    .append(" ")
                    .append(instance.getDisplayName());

        }

        return CommonProxy.getFluidName(fluid);
    }
}
