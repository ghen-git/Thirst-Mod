package dev.ghen.thirst.init;

import dev.ghen.thirst.common.item.DrinkableItem;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "thirst");

    public static final RegistryObject<Item> WATER_BOWL = ITEMS.register("water_bowl", () -> new DrinkableItem().setContainer(Items.BOWL));
}
