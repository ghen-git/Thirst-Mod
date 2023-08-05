package dev.ghen.thirst.content.registry;

import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<Item> CLAY_BOWL;
    public static final RegistryObject<Item> TERRACOTTA_BOWL;
    public static final RegistryObject<Item> TERRACOTTA_WATER_BOWL;

    public ItemInit() {
    }

    static {
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "thirst");
        CLAY_BOWL = ITEMS.register("clay_bowl", () -> new Item((new Item.Properties())
                .stacksTo(64)
        ));
        TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl", () -> new Item((new Item.Properties())
                .stacksTo(64)
        ));
        TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl", () -> (new DrinkableItem())
                .setContainer(TERRACOTTA_BOWL.get())
        );
    }
}