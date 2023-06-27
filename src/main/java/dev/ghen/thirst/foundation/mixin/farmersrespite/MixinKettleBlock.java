package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.farmersrespite.common.block.KettleBlock;

@Mixin(KettleBlock.class)
public class MixinKettleBlock
{
    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void addPurityBlockState(StateDefinition.Builder<Block, BlockState> p_153549_, CallbackInfo ci)
    {
        p_153549_.add(WaterPurity.BLOCK_PURITY);
    }

    // @Redirect(method = "emptyContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    // private static void use(Player instance, InteractionHand interactionHand, ItemStack itemStack)
    // {
    //     if(itemStack.is(Items.BUCKET))
    //     {
    //         instance.setItemInHand(interactionHand, new ItemStack(Items.BUCKET));
    //     }
    //     else
    //     {
    //         ItemStack heldStack = instance.getItemInHand(interactionHand);
    //         if(heldStack.getCount() > 1)
    //             heldStack.shrink(1);
    //         else
    //             instance.setItemInHand(interactionHand, new ItemStack(Items.GLASS_BOTTLE));
    //     }
    // }
}
