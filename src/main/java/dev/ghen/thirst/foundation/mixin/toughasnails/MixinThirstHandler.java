package dev.ghen.thirst.foundation.mixin.toughasnails;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toughasnails.thirst.ThirstHandler;

@Mixin(value = ThirstHandler.class,remap = false)
public class MixinThirstHandler {
    @Inject(method = "onPlayerUseItem", at = @At("HEAD"), cancellable = true)
    private static void onPlayerInteractItem(glitchcore.event.player.PlayerInteractEvent.UseItem event, CallbackInfo ci){
        ci.cancel();
    }
}
