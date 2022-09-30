package dev.ghen.thirst.foundation.mixin.accessors.brewinandchewin;

import com.brewinandchewin.common.block.entity.KegBlockEntity;
import com.brewinandchewin.common.crafting.KegRecipe;
import com.farmersrespite.common.block.entity.KettleBlockEntity;
import com.farmersrespite.common.crafting.KettleRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(KegBlockEntity.class)
public interface KegBlockEntityAccessor
{
    @Invoker(remap = false)
    boolean invokeHasInput();
    @Invoker(remap = false)
    Optional<KegRecipe> invokeGetMatchingRecipe(RecipeWrapper inventoryWrapper);
    @Invoker(remap = false)
    boolean invokeCanFerment(KegRecipe recipe);
    @Invoker(remap = false)
    boolean invokeDoesMealHaveContainer(ItemStack meal);
    @Invoker(remap = false)
    void invokeMoveMealToOutput();
    @Invoker(remap = false)
    void invokeUseStoredContainersOnMeal();

    @Invoker(remap = false)
    boolean invokeProcessFermenting(KegRecipe recipe);
}
