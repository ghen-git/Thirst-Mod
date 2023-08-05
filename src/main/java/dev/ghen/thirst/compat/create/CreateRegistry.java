package dev.ghen.thirst.compat.create;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ghen.thirst.Thirst;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class CreateRegistry
{
    public static final NonNullSupplier<Registrate> REGISTRATE=NonNullSupplier.lazy(() ->Registrate.create(Thirst.ID));

    public static void register(){}


    public static final BlockEntry<SandFilterBlock> SAND_FILTER_BLOCK= REGISTRATE.get()
            .block("sand_filter", SandFilterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();
    public static final BlockEntityEntry<SandFilterTileEntity> SAND_FILTER_TE= REGISTRATE.get()
            .blockEntity("sand_filter",SandFilterTileEntity::new)
            .validBlocks(SAND_FILTER_BLOCK)
            .register();

}
