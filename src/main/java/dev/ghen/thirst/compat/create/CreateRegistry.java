package dev.ghen.thirst.compat.create;

import com.simibubi.create.Create;
import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class CreateRegistry
{
    public CreateRegistry(){}
    public static void register(){}
    public static final BlockEntry<SandFilterBlock> SAND_FILTER_BLOCK;
    public static final BlockEntityEntry<SandFilterTileEntity> SAND_FILTER_TE;


    static
    {
        SAND_FILTER_BLOCK = Create.REGISTRATE.block("sand_filter", SandFilterBlock::new)
                .initialProperties(SharedProperties::copperMetal)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
                //.addLayer(()->RenderType::cutoutMipped)
                .item(AssemblyOperatorBlockItem::new)
                .transform(customItemModel())
                .register();

        SAND_FILTER_TE = Create.REGISTRATE
                .blockEntity("sand_filter",SandFilterTileEntity::new)
                .validBlocks(SAND_FILTER_BLOCK)
                .register();
    }
}
