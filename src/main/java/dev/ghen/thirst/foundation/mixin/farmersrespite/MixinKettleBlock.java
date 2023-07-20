package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.farmersrespite.common.block.KettleBlock;

@Mixin(KettleBlock.class)
public class MixinKettleBlock {
    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void addPurityBlockState(StateDefinition.Builder<Block, BlockState> p_153549_, CallbackInfo ci) {
        p_153549_.add(WaterPurity.BLOCK_PURITY);
    }

}