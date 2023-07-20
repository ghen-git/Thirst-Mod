package dev.ghen.thirst.foundation.mixin.toughasnails;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import toughasnails.config.ServerConfig;

@Mixin(value = ServerConfig.class,remap = false)
public abstract class MixinServerConfig {

    @ModifyArg(method = "<clinit>",index = 1, at = @At(value = "INVOKE",target = "Lnet/minecraftforge/common/ForgeConfigSpec$Builder;define(Ljava/lang/String;Z)Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;",ordinal = 0))
    private static boolean onInit(boolean defaultValue) {
        return false;
    }

}
