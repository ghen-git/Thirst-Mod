package dev.ghen.thirst.foundation.mixin.jade;

import dev.ghen.thirst.content.purity.WaterPurity;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.VanillaPlugin;
import snownee.jade.addon.forge.ForgeCapabilityProvider;

@Mixin(value = ForgeCapabilityProvider.class,remap = false)
public class MixinForgeCapabilityProvider {

    @Inject(method ="appendTank", at = @At("HEAD"), cancellable = true)
    private static void appendTank(ITooltip tooltip, FluidStack fluidStack, int capacity, CallbackInfo ci){
        if (capacity > 0) {
            IElementHelper helper = tooltip.getElementHelper();
            Component text;
            if (fluidStack.isEmpty()) {
                text = new TranslatableComponent("jade.fluid.empty");
            } else {
                String amountText = VanillaPlugin.getDisplayHelper().humanReadableNumber(fluidStack.getAmount(), "B", true);
                if(WaterPurity.hasPurity(fluidStack) && WaterPurity.getPurity(fluidStack) != -1){
                    text = new TextComponent(WaterPurity.getPurityText(WaterPurity.getPurity(fluidStack))+" "+
                            new TranslatableComponent("jade.fluid",fluidStack.getDisplayName(), amountText).getString()
                            );
                }else {
                    text = new TranslatableComponent("jade.fluid", fluidStack.getDisplayName(), amountText);
                }

            }

            IProgressStyle progressStyle = helper.progressStyle().overlay(helper.fluid(fluidStack));
            tooltip.add(helper.progress((float)fluidStack.getAmount() / (float)capacity, text, progressStyle, helper.borderStyle()).tag(VanillaPlugin.FORGE_FLUID));
            ci.cancel();
        }
    }
}
