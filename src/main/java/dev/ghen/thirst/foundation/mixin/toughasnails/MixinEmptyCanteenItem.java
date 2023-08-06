package dev.ghen.thirst.foundation.mixin.toughasnails;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toughasnails.api.block.TANBlocks;
import toughasnails.api.item.TANItems;
import toughasnails.block.RainCollectorBlock;
import toughasnails.item.EmptyCanteenItem;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.util.MathHelper;

@Mixin(EmptyCanteenItem.class)
public abstract class MixinEmptyCanteenItem {

    @Shadow(remap = false)
    protected abstract ItemStack replaceCanteen(ItemStack stack, Player player, ItemStack filledItem);
    @Inject(method = "use",at =@At("HEAD"), cancellable = true)
    private void use(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        ItemStack stack = player.getItemInHand(hand);
        Level level = player.level();

        BlockPos blockPos = MathHelper.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY).getBlockPos();
        BlockState state = world.getBlockState(blockPos);

        if (!world.mayInteract(player, blockPos)){
            cir.setReturnValue(InteractionResultHolder.pass(stack));
        }

        if(level.getFluidState(blockPos).is(FluidTags.WATER))
        {
            SoundEvent sound=SoundEvents.BOTTLE_FILL;
            ItemStack filledItem;

            level.playSound(player, player.getX(), player.getY(), player.getZ(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);

            int purity=WaterPurity.getBlockPurity(level, blockPos);
            if(purity==3){
                filledItem = TANItems.PURIFIED_WATER_CANTEEN.get().getDefaultInstance();
            } else if (purity==2) {
                filledItem = TANItems.WATER_CANTEEN.get().getDefaultInstance();
            }else {
                filledItem = TANItems.DIRTY_WATER_CANTEEN.get().getDefaultInstance();
            }

            ItemStack result = ItemUtils.createFilledResult(stack, player, filledItem);

            cir.setReturnValue(InteractionResultHolder.sidedSuccess(replaceCanteen(stack, player, result), world.isClientSide()));
        }
        else if (state.getBlock() instanceof RainCollectorBlock) {
            int waterLevel = state.getValue(RainCollectorBlock.LEVEL);
            if (waterLevel > 0 && !world.isClientSide()) {
                world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                ((RainCollectorBlock) TANBlocks.RAIN_COLLECTOR.get()).setWaterLevel(world, blockPos, state, waterLevel - 1);
                cir.setReturnValue(InteractionResultHolder.success(replaceCanteen(stack, player, new ItemStack(TANItems.PURIFIED_WATER_CANTEEN.get()))));
            }
        }
    }
}
