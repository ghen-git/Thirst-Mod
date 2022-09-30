package dev.ghen.thirst.foundation.mixin.accessors.farmersrespite;

import com.farmersrespite.common.block.entity.KettleBlockEntity;
import com.farmersrespite.common.crafting.KettleRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(KettleBlockEntity.class)
public interface KettleBlockEntityAccessor
{
    @Invoker(remap = false)
    boolean invokeHasInput();
    @Invoker(remap = false)
    Optional<KettleRecipe> invokeGetMatchingRecipe(RecipeWrapper inventoryWrapper);
    @Invoker(remap = false)
    boolean invokeCanBrew(KettleRecipe recipe);
    @Invoker(remap = false)
    boolean invokeDoesMealHaveContainer(ItemStack meal);
    @Invoker(remap = false)
    void invokeMoveMealToOutput();
    @Invoker(remap = false)
    void invokeUseStoredContainersOnMeal();

    @Invoker(remap = false)
    boolean invokeProcessBrewing(KettleRecipe recipe);
}
