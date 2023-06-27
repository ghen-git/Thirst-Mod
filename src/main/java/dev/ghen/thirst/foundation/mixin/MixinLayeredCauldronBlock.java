package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayeredCauldronBlock.class)
public abstract class MixinLayeredCauldronBlock extends AbstractCauldronBlock
{

    public MixinLayeredCauldronBlock(Properties pProperties, Map<Item, CauldronInteraction> pInteractions) {
        super(pProperties, pInteractions);
    }
    
    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void addPurityBlockState(StateDefinition.Builder<Block, BlockState> p_153549_, CallbackInfo ci)
    {
        p_153549_.add(WaterPurity.BLOCK_PURITY);
    }

     @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState, @NotNull Level level, @NotNull BlockPos p_151971_, @NotNull Player player, @NotNull InteractionHand p_151973_, @NotNull BlockHitResult p_151974_) {
        ItemStack itemstack = player.getItemInHand(p_151973_);
        if(!level.isClientSide) {
            Fluid fluid = blockState.getFluidState().getType();
            if (fluid == Fluids.WATER || fluid == Fluids.EMPTY) {
                if (WaterPurity.isWaterFilledContainer(itemstack) && WaterPurity.hasPurity(itemstack) && !blockState.getFluidState().isEmpty()) {
                    int purity = WaterPurity.getPurity(itemstack);
                    int blockPurity = !blockState.hasProperty(WaterPurity.BLOCK_PURITY) ? WaterPurity.MAX_PURITY : (blockState.getValue(WaterPurity.BLOCK_PURITY) - 1 < 0 ? WaterPurity.MAX_PURITY : blockState.getValue(WaterPurity.BLOCK_PURITY) - 1);
                    blockState.setValue(WaterPurity.BLOCK_PURITY, Math.min(purity, blockPurity) + 1);
                }
                if (WaterPurity.isEmptyWaterContainer(itemstack) && !blockState.is(Blocks.CAULDRON)) {
                    int blockPurity = !blockState.hasProperty(WaterPurity.BLOCK_PURITY) ? WaterPurity.MAX_PURITY : (blockState.getValue(WaterPurity.BLOCK_PURITY) - 1 < 0 ? WaterPurity.MAX_PURITY : blockState.getValue(WaterPurity.BLOCK_PURITY) - 1);
                    ItemStack Filled;
                    if (itemstack.is(Items.BUCKET)) {
                        Filled = new ItemStack(Items.WATER_BUCKET);
                    } else {
                        Filled = new ItemStack(Items.POTION);
                        PotionUtils.setPotion(Filled, Potions.WATER);
                    }
                    WaterPurity.addPurity(Filled, blockPurity);
                    Iterator<ItemStack> iterator = player.getInventory().items.iterator();
                    TickHelper.nextTick(level, () -> {
                        while (iterator.hasNext()) {
                            ItemStack item = iterator.next();
                            if (item.is(Filled.getItem())) {
                                if(WaterPurity.getPurity(item)==-1){
                                    if(item.getCount()>1)
                                        item.shrink(1);
                                    else
                                        player.getInventory().removeItem(item);
                                    if (!player.getInventory().add(Filled)) {
                                        player.drop(Filled, false);
                                    }
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        }
        return super.use(blockState, level, p_151971_, player, p_151973_, p_151974_);
    }
}
