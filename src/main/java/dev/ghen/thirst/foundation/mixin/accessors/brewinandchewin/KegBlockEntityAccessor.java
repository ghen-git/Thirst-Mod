package dev.ghen.thirst.foundation.mixin.accessors.brewinandchewin;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegRecipe;

import java.util.Optional;

@Mixin(value = KegBlockEntity.class,remap = false)
public interface KegBlockEntityAccessor
{
    @Invoker
    boolean invokeHasInput();
    @Invoker
    Optional<KegRecipe> invokeGetMatchingRecipe(RecipeWrapper inventoryWrapper);
    @Invoker
    boolean invokeCanFerment(KegRecipe recipe);
    @Invoker
    boolean invokeDoesDrinkHaveContainer(ItemStack meal);
    @Invoker
    void invokeMoveDrinkToOutput();
    @Invoker
    void invokeUseStoredContainersOnMeal();

    @Invoker
    boolean invokeProcessFermenting(KegRecipe recipe, KegBlockEntity keg);
}
