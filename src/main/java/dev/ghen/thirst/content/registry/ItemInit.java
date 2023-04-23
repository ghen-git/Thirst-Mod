package dev.ghen.thirst.content.registry;

import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import dev.ghen.thirst.foundation.tab.ThirstTab;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "thirst");

    public static final RegistryObject<Item>
            CLAY_BOWL = ITEMS.register("clay_bowl", () -> new Item(new Item.Properties().stacksTo(16).tab(ThirstTab.THIRST_TAB))),
            TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl", () -> new Item(new Item.Properties().stacksTo(16).tab(ThirstTab.THIRST_TAB))),
            TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl", () -> new DrinkableItem().setContainer(TERRACOTTA_BOWL.get()));
}
