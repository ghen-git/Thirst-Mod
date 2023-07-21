package dev.ghen.thirst.foundation.mixin.toughasnails;

import net.minecraftforge.client.event.RenderTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toughasnails.client.handler.TooltipHandler;

@Mixin(value = TooltipHandler.class,remap = false)
public class MixinTooltipHandler {
    @Inject(method = "onRenderTooltip",at = @At("HEAD"),remap = false, cancellable = true)
    private static void onRenderTooltip(RenderTooltipEvent.GatherComponents event, CallbackInfo ci) {
        ci.cancel();
    }
}
