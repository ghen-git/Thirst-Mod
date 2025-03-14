package dev.ghen.thirst.foundation.mixin.accessors.brewinandchewin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

@Mixin(value = KegBlockEntity.class,remap = false)
public interface KegBlockEntityAccessor
{
    @Invoker
    void invokeEjectIngredientRemainder(ItemStack remainderStack);
}
