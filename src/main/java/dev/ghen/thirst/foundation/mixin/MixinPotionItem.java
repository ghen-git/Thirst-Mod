package dev.ghen.thirst.foundation.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionItem.class)
public class MixinPotionItem {

    @Redirect(method = "finishUsingItem",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean finishUsingItem(Inventory instance, ItemStack stack){
        ItemEntity itemEntity = new ItemEntity(instance.player.level, instance.player.getX(), instance.player.getY(), instance.player.getZ(), stack);
        instance.player.level.addFreshEntity(itemEntity);
        return true;
    }
}
