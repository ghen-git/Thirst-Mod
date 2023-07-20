package dev.ghen.thirst.content.purity;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class ContainerWithPurity
{
    private ItemStack filledItem;
    private ItemStack emptyItem;
    private final boolean isDrinkable;
    private final boolean isStatic;
    private Predicate<ItemStack> equalsFilled;
    private Predicate<ItemStack> equalsEmpty;
    private boolean canHarvestRunningWater;

    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem)
    {
        this.emptyItem = emptyItem;
        this.filledItem = filledItem;
        this.isDrinkable = true;
        this.isStatic = false;
        this.canHarvestRunningWater = true;

        fillPredicates();
    }

    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem, boolean isDrinkable)
    {
        this.emptyItem = emptyItem;
        this.filledItem = filledItem;
        this.isDrinkable = isDrinkable;
        this.isStatic = false;
        this.canHarvestRunningWater = true;

        fillPredicates();
    }

    /**
     * Creates a Static container (has purity but can only be
     * drunk, not used to fill anything)
     */
    public ContainerWithPurity(ItemStack filledItem)
    {
        this.emptyItem = null;
        this.filledItem = filledItem;
        this.isDrinkable = true;
        this.isStatic = true;
        this.canHarvestRunningWater = false;

        fillPredicates();
    }

    public ContainerWithPurity canHarvestRunningWater(boolean canHarvestRunningWater)
    {
        this.canHarvestRunningWater = canHarvestRunningWater;
        return this;
    }

    public boolean canHarvestRunningWater()
    {
        return canHarvestRunningWater;
    }

    void fillPredicates()
    {
        equalsFilled = itemStack -> itemStack.getItem() == filledItem.getItem();
        equalsEmpty = itemStack -> itemStack.getItem() == emptyItem.getItem();
    }

    public ContainerWithPurity setEqualsEmpty(Predicate<ItemStack> predicate)
    {
        this.equalsEmpty = predicate;
        return this;
    }

    public ContainerWithPurity setEqualsFilled(Predicate<ItemStack> predicate)
    {
        this.equalsFilled = predicate;
        return this;
    }

    public boolean equalsEmpty(ItemStack item)
    {
        return !isStatic && equalsEmpty.test(item);
    }

    public boolean equalsFilled(ItemStack item)
    {
        return equalsFilled.test(item);
    }

    public boolean isDrinkable()
    {
        return isDrinkable;
    }

    public ItemStack getFilledItem()
    {
        return filledItem;
    }

    public void setFilledItem(ItemStack filledItem)
    {
        this.filledItem = filledItem;
    }

    public ItemStack getEmptyItem()
    {
        return emptyItem;
    }

    public void setEmptyItem(ItemStack emptyItem)
    {
        this.emptyItem = emptyItem;
    }
}
