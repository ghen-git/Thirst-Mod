package dev.ghen.thirst.foundation.mixin.toughasnails;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import toughasnails.thirst.ThirstHandler;

@Mixin(value = ThirstHandler.class,remap = false)
public class MixinThirstHandler {
    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"), cancellable = true)
    private void onPlayerInteractItem(PlayerInteractEvent.RightClickItem event, CallbackInfo ci){
        ci.cancel();
    }
}
