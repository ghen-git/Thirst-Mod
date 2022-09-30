package dev.ghen.thirst.foundation.mixin.farmersrespite;

import com.farmersrespite.common.block.entity.KettleBlockEntity;
import com.farmersrespite.common.crafting.KettleRecipe;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.mixin.accessors.farmersrespite.KettleBlockEntityAccessor;
import dev.ghen.thirst.foundation.mixin.accessors.farmersdelight.SyncedBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.farmersrespite.common.block.entity.KettleBlockEntity.animationTick;

@Mixin(KettleBlockEntity.class)
public abstract class MixinKettleBlockEntity
{
    @Inject(method = "brewingTick", at = @At("HEAD"), remap = false, cancellable = true)
    private static void brewingTickWithPurity(Level level, BlockPos pos, BlockState state, KettleBlockEntity kettle, CallbackInfo ci)
    {
        boolean isHeated = kettle.isHeated(level, pos);
        boolean didInventoryChange = false;
        KettleBlockEntityAccessor kettleAcc = (KettleBlockEntityAccessor) kettle;

        if (isHeated && kettleAcc.invokeHasInput()) {
            Optional<KettleRecipe> recipe = kettleAcc.invokeGetMatchingRecipe(new RecipeWrapper(kettle.getInventory()));
            if (recipe.isPresent() && kettleAcc.invokeCanBrew((KettleRecipe)recipe.get()) && WaterPurity.isWaterFilledContainer(recipe.get().getResultItem()))
            {
                didInventoryChange = kettleAcc.invokeProcessBrewing((KettleRecipe)recipe.get());
                if(didInventoryChange)
                {
                    int purity = Math.min(WaterPurity.getBlockPurity(kettle.getBlockState()) + 1, WaterPurity.MAX_PURITY);
                    WaterPurity.addPurity(kettle.getInventory().getStackInSlot(2), purity);
                }
            } else
                return;
        } else
            return;

        ItemStack mealStack = kettle.getMeal();
        if (!mealStack.isEmpty())
        {
            animationTick(level, pos, state, kettle);
            if (!kettleAcc.invokeDoesMealHaveContainer(mealStack))
            {
                kettleAcc.invokeMoveMealToOutput();
                didInventoryChange = true;
            } else if (!kettle.getInventory().getStackInSlot(3).isEmpty())
            {
                kettleAcc.invokeUseStoredContainersOnMeal();
                didInventoryChange = true;
            }
        }

        if (didInventoryChange)
        {
            ((SyncedBlockEntityAccessor) kettle).invokeInventoryChanged();
        }

        ci.cancel();
    }
}
