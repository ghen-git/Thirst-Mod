package dev.ghen.thirst.content;

import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "thirst");

    public static final RegistryObject<Item> WOODEN_WATER_BOWL = ITEMS.register("wooden_water_bowl", () -> new DrinkableItem().setContainer(Items.BOWL)),
            TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl", () -> new Item(new Item.Properties().stacksTo(16))),
            CLAY_BOWL = ITEMS.register("clay_bowl", () -> new Item(new Item.Properties().stacksTo(16))),
            TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl", () -> new DrinkableItem().setContainer(TERRACOTTA_BOWL.get()));
}
