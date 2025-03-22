package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.thirst.PlayerThirst;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PotionItem.class)
public class MixinPotionItem {

    @Redirect(method = "finishUsingItem",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean finishUsingItem(Inventory instance, ItemStack stack){
        if (!instance.add(stack)) {
            instance.player.drop(stack, false);
        }
        return true;
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onFinishUsingItem(ItemStack item, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir)
    {
        Player player = livingEntity instanceof Player ? (Player)livingEntity : null;
        if(player != null)
        {
            PlayerThirst.drink(item, player);
        }
    }
}
