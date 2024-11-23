package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractCookingRecipe.class)
public class MixinAbstractCookingRecipe {

    @Mutable
    @Shadow
    @Final
    protected ItemStack result;

    @Shadow @Final protected RecipeType<?> type;

    @ModifyArg(method = "matches", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z"))
    public ItemStack matches(ItemStack itemStack){
        if(WaterPurity.isWaterFilledContainer(itemStack))
            return WaterPurity.addPurity(itemStack.copy(), CommonConfig.DEFAULT_PURITY.get());
        return itemStack;
    }
}
