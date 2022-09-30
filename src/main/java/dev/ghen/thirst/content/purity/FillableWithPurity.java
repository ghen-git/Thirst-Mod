package dev.ghen.thirst.content.purity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FillableWithPurity
{
    private Block block;

    public FillableWithPurity(Block block)
    {
        this.block = block;
    }

    public int getPurity(BlockState blockState)
    {
        if(!blockState.hasProperty(WaterPurity.BLOCK_PURITY))
            return WaterPurity.MAX_PURITY;
        else
            return blockState.getValue(WaterPurity.BLOCK_PURITY);
    }

    public Block getBlock()
    {
        return block;
    }

    public void setBlock(Block block)
    {
        this.block = block;
    }
}
