package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public class MixinBucketItem
{
    private boolean shouldModify;
    private int purity;

    @Inject(method = "use", at = @At("HEAD"))
    public void setPurity(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir)
    {
        BlockPos blockPos = MathHelper.getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.SOURCE_ONLY).getBlockPos();

        shouldModify = (level.getFluidState(blockPos).is(FluidTags.WATER) && level.getFluidState(blockPos).isSource());

        if(shouldModify)
            purity = WaterPurity.getBlockPurity(level, blockPos);
    }

    @ModifyArg(method = "use", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack addPurity(ItemStack result)
    {
        if(shouldModify)
        {
            CompoundTag tag = result.getOrCreateTag();
            tag.putInt("Purity", purity);
        }

        return result;
    }
}
