package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public class MixinBottleItem
{
    private boolean shouldModify;
    private int purity;

    @Inject(method = "turnBottleIntoItem", at = @At("HEAD"))
    public void setPurity(ItemStack source, Player player, ItemStack result, CallbackInfoReturnable<ItemStack> cir)
    {
        Level level = player.level();
        BlockPos fluidPos = MathHelper.getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.SOURCE_ONLY).getBlockPos();

        shouldModify = level.getFluidState(fluidPos).is(FluidTags.WATER) && level.getFluidState(fluidPos).isSource();
        if(shouldModify)
            purity = WaterPurity.getBlockPurity(level, fluidPos);
    }

    @ModifyArg(method = "turnBottleIntoItem", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack addPurity(ItemStack result)
    {
        if(shouldModify)
            WaterPurity.addPurity(result, purity);

        return result;
    }
}
