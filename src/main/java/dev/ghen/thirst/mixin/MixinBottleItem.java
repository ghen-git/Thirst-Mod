package dev.ghen.thirst.mixin;

import com.mojang.logging.LogUtils;
import dev.ghen.thirst.common.event.WaterPurity;
import dev.ghen.thirst.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.codehaus.plexus.util.cli.Arg;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BottleItem.class)
public class MixinBottleItem
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean shouldModify;
    private int purity;

    @Inject(method = "turnBottleIntoItem", at = @At("HEAD"))
    public void setPurity(ItemStack source, Player player, ItemStack result, CallbackInfoReturnable<ItemStack> cir)
    {
        Level level = player.getLevel();
        BlockPos blockPos = MathHelper.getPlayerPOVHitResult(player.getLevel(), player, ClipContext.Fluid.SOURCE_ONLY).getBlockPos();

        shouldModify = level.getFluidState(blockPos).is(FluidTags.WATER) && level.getFluidState(blockPos).isSource();
        if(shouldModify)
            purity = WaterPurity.getWaterPurity(level, blockPos);
    }

    @ModifyArg(method = "turnBottleIntoItem", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
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
