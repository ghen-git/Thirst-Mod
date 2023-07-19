package dev.ghen.thirst.foundation.tab;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.content.registry.ThirstItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

public class ThirstTab extends CreativeModeTab
{
    public static final ThirstTab THIRST_TAB = new ThirstTab("thirst");

    public ThirstTab(String label)
    {
        super(label);
    }

    @Override
    public @NotNull ItemStack makeIcon()
    {
        return new ItemStack(ThirstItem.TERRACOTTA_WATER_BOWL.get());
    }

    @Override
    public void fillItemList(@NotNull NonNullList<ItemStack> list)
    {
        super.fillItemList(list);

        list.add(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), 0));
        list.add(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), 1));
        list.add(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), 2));
        list.add(WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), 3));
        list.add(WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 0));
        list.add(WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 1));
        list.add(WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 2));
        list.add(WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 3));
    }
}
