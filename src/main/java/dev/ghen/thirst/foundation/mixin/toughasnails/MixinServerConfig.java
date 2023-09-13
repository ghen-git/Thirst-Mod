package dev.ghen.thirst.foundation.mixin.toughasnails;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import toughasnails.config.ServerConfig;

@Mixin(value = ServerConfig.class,remap = false)
public class MixinServerConfig {
    @ModifyArg(method ="<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeConfigSpec$Builder;define(Ljava/lang/String;Z)Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;", ordinal = 0),index = 1)
    private static boolean modifyBoolean(boolean defaultValue) {
        return false;
    }
}
