package dev.ghen.thirst.foundation.mixin.accessors.farmersdelight;

import com.farmersrespite.common.block.entity.KettleBlockEntity;
import com.farmersrespite.common.crafting.KettleRecipe;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

import java.util.Optional;

@Mixin(SyncedBlockEntity.class)
public interface SyncedBlockEntityAccessor
{
    @Invoker(remap = false)
    void invokeInventoryChanged();
}
