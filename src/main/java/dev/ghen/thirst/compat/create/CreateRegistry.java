package dev.ghen.thirst.compat.create;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ghen.thirst.foundation.tab.ThirstTab;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static dev.ghen.thirst.Thirst.REGISTRATE;

public class CreateRegistry
{
    public CreateRegistry(){}
    public static void register(){}



    static
    {
        REGISTRATE.get().creativeModeTab(() -> ThirstTab.THIRST_TAB);
    }

    public static final BlockEntry<SandFilterBlock> SAND_FILTER_BLOCK= REGISTRATE.get()
            .block("sand_filter", SandFilterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            //.addLayer(()->RenderType::cutoutMipped)
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();
    public static final BlockEntityEntry<SandFilterTileEntity> SAND_FILTER_TE= REGISTRATE.get()
            .blockEntity("sand_filter",SandFilterTileEntity::new)
            .validBlocks(SAND_FILTER_BLOCK)
            .register();

}
