package dev.ghen.thirst.foundation.mixin.toughasnails;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import toughasnails.config.ThirstConfig;

@Mixin(value = ThirstConfig.class,remap = false)
public class MixinServerConfig {
    @ModifyArg(method ="load", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;valueOf(Z)Ljava/lang/Boolean;", ordinal = 0),index = 0)
    private boolean modifyBoolean(boolean defaultValue) {
        return false;
    }
}
