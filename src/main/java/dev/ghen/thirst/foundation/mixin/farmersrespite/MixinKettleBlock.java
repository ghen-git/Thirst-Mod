package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.KettleBlock;

@Mixin(KettleBlock.class)
public abstract class MixinKettleBlock {
    @Final
    @Shadow(remap = false)
    public static IntegerProperty WATER_LEVEL;

    private BlockState blockState;


    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void addPurityBlockState(StateDefinition.Builder<Block, BlockState> p_153549_, CallbackInfo ci) {
        p_153549_.add(WaterPurity.BLOCK_PURITY);
    }

    @Inject(method = "use", at = @At("HEAD"))
    private void addPurityOnUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int water_level = state.getValue(WATER_LEVEL);
            if(water_level<3) {
                int purity = WaterPurity.getPurity(heldStack);
                int blockPurity = !state.hasProperty(WaterPurity.BLOCK_PURITY) ? WaterPurity.MAX_PURITY : (state.getValue(WaterPurity.BLOCK_PURITY) - 1 < 0 ? WaterPurity.MAX_PURITY : state.getValue(WaterPurity.BLOCK_PURITY) - 1);
                if (heldStack.getItem() == Items.WATER_BUCKET) {
                    blockState = state.setValue(WaterPurity.BLOCK_PURITY, Math.min(purity, blockPurity) + 1).setValue(WATER_LEVEL,3);
                }
                else if(heldStack.getItem() == Items.POTION && PotionUtils.getPotion(heldStack) == Potions.WATER){
                    blockState = state.setValue(WaterPurity.BLOCK_PURITY, Math.min(purity, blockPurity) + 1).setValue(WATER_LEVEL,water_level+1);
                }
            }
        }
    }

    @ModifyArg(method = "use",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",ordinal = 2),index = 1)
    private BlockState modifyPurity_bucket(BlockState p_46599_) {
        return blockState;
    }
    @ModifyArg(method = "use",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",ordinal = 3),index = 1)
    private BlockState modifyPurity_bottle(BlockState p_46599_) {
        return blockState;
    }

}