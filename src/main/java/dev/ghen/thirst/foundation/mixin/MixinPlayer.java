package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.thirst.PlayerThirst;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer
{


    @Shadow public abstract <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing);

    @Inject(method = "eat", at = @At("HEAD"))
    public void onEatDrink(Level level, ItemStack item, CallbackInfoReturnable<ItemStack> cir)
    {
        Player player = (Player) ((Object) this);
        PlayerThirst.drink(item, player);
    }

}
