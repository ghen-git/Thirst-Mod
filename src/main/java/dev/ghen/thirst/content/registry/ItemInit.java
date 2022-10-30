package dev.ghen.thirst.content.registry;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.AssemblyOperatorBlockItem;
import com.simibubi.create.content.contraptions.fluids.actors.SpoutBlock;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import dev.ghen.thirst.foundation.tab.ThirstTab;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.simibubi.create.AllTags.pickaxeOnly;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "thirst");

    public static final RegistryObject<Item>
            CLAY_BOWL = ITEMS.register("clay_bowl", () -> new Item(new Item.Properties().stacksTo(16).tab(ThirstTab.THIRST_TAB))),
            TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl", () -> new Item(new Item.Properties().stacksTo(16).tab(ThirstTab.THIRST_TAB))),
            TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl", () -> new DrinkableItem().setContainer(TERRACOTTA_BOWL.get()));
}
