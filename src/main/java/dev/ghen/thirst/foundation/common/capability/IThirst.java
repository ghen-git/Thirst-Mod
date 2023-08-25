package dev.ghen.thirst.foundation.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IThirst
{
    int getThirst();
    void setThirst(int value);
    int getQuenched();
    void setQuenched(int value);
    float getExhaustion();
    void setExhaustion(float value);
    void addExhaustion(Player player, float amount);
    void tick(Player player);
    void drink(Player player, int thirst, int quenched);
    void updateThirstData(Player player);
    void setJustHealed();
    void copy(IThirst cap);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
