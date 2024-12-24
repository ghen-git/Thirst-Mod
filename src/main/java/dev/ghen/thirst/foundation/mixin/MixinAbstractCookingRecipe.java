package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractCookingRecipe.class)
public class MixinAbstractCookingRecipe {
    @ModifyArg(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z"))
    public ItemStack matches(ItemStack itemStack){
        if(WaterPurity.isWaterFilledContainer(itemStack) && !itemStack.getTag().contains("Purity")){
            ItemStack matched = itemStack.copy();
            CompoundTag tag = matched.getOrCreateTag();
            tag.putInt("Purity", CommonConfig.DEFAULT_PURITY.get());
            return matched;
        }
        return itemStack;
    }
}
