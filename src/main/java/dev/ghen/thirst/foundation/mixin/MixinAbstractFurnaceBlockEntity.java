package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity
{
    @Shadow
    protected abstract boolean canBurn(@Nullable Recipe<?> p_155006_, NonNullList<ItemStack> p_155007_, int p_155008_);

    @Inject(method = "canBurn", at = @At("HEAD"), cancellable = true)
    private void blockPotions(Recipe<?> p_155006_, NonNullList<ItemStack> item, int p_155008_, CallbackInfoReturnable<Boolean> cir)
    {
        if(WaterPurity.isWaterFilledContainer(item.get(0)))
        {
            if(WaterPurity.getPurity(item.get(0)) == WaterPurity.MAX_PURITY)
                cir.cancel();
        }
        else if(item.get(0).is(Items.POTION))
            cir.cancel();
    }

    @Inject(method = "burn", at = @At("HEAD"), cancellable = true)
    private void burnPurityContainers(Recipe<?> p_155027_, NonNullList<ItemStack> p_155028_, int p_155029_, CallbackInfoReturnable<Boolean> cir)
    {
        if (p_155027_ != null && this.canBurn(p_155027_, p_155028_, p_155029_))
        {
            ItemStack itemstack = p_155028_.get(0);
            ItemStack itemstack2 = p_155028_.get(2);

            if(WaterPurity.isWaterFilledContainer(itemstack))
            {
                ItemStack itemstack1 = WaterPurity.getFilledContainer(itemstack, true);
                int purity = WaterPurity.getPurity(itemstack);
                WaterPurity.addPurity(itemstack1, Math.min(purity + 2, 3));

                if (itemstack2.isEmpty())
                {
                    p_155028_.set(2, itemstack1.copy());
                }
                else if (itemstack2.is(itemstack1.getItem()))
                {
                    itemstack2.grow(itemstack1.getCount());
                }

                itemstack.shrink(1);

                cir.setReturnValue(true);
            }
        }
    }
}
