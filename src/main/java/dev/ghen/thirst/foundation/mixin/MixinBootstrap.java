package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(Bootstrap.class)
public class MixinBootstrap
{
    /**
     * Funny cauldron makes me wanna kill myself also this is the only way
     * to modify a cauldron interaction with a mixin because spongepowered doesn't support
     * injections in interfaces.
     * */
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/synchronization/ArgumentTypes;bootStrap()V"), remap = true)
    private static void modifyCauldronInteractions(CallbackInfo ci)
    {
        CauldronInteraction.WATER.remove(Items.GLASS_BOTTLE);

        CauldronInteraction.WATER.put(Items.GLASS_BOTTLE, (blockState, level, pos, player, hand, itemStack) ->
        {
            Thirst.LOGGER.info("cock");

            if (!level.isClientSide)
            {
                Item item = itemStack.getItem();

                ItemStack result = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                WaterPurity.addPurity(result, pos, level);

                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
                level.playSound((Player)null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, pos);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        CauldronInteraction.WATER.remove(Items.BUCKET);

        CauldronInteraction.WATER.put(Items.BUCKET, (blockState, level, pos, player, hand, item) ->
                fillBucket(blockState, level, pos, player, hand, item, WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), pos, level), (p_175660_) ->
                        p_175660_.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
    }

    private static InteractionResult fillBucket(BlockState p_175636_, Level p_175637_, BlockPos p_175638_, Player p_175639_, InteractionHand p_175640_, ItemStack p_175641_, ItemStack p_175642_, Predicate<BlockState> p_175643_, SoundEvent p_175644_) {
        if (!p_175643_.test(p_175636_)) {
            Thirst.LOGGER.info("cock2");
            return InteractionResult.PASS;
        } else {
            Thirst.LOGGER.info("cock3");
            if (!p_175637_.isClientSide) {
                Item item = p_175641_.getItem();
                p_175639_.setItemInHand(p_175640_, ItemUtils.createFilledResult(p_175641_, p_175639_, p_175642_));
                p_175639_.awardStat(Stats.USE_CAULDRON);
                p_175639_.awardStat(Stats.ITEM_USED.get(item));
                p_175637_.setBlockAndUpdate(p_175638_, Blocks.CAULDRON.defaultBlockState());
                p_175637_.playSound((Player)null, p_175638_, p_175644_, SoundSource.BLOCKS, 1.0F, 1.0F);
                p_175637_.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, p_175638_);
            }

            return InteractionResult.sidedSuccess(p_175637_.isClientSide);
        }
    }
}
