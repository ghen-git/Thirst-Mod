package dev.ghen.thirst.foundation.mixin.brewinandchewin;

import com.brewinandchewin.common.block.entity.KegBlockEntity;
import com.brewinandchewin.common.crafting.KegRecipe;
import com.farmersrespite.common.block.entity.KettleBlockEntity;
import com.farmersrespite.common.crafting.KettleRecipe;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.mixin.accessors.brewinandchewin.KegBlockEntityAccessor;
import dev.ghen.thirst.foundation.mixin.accessors.farmersdelight.SyncedBlockEntityAccessor;
import dev.ghen.thirst.foundation.mixin.accessors.farmersrespite.KettleBlockEntityAccessor;
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

import static com.brewinandchewin.common.block.entity.KegBlockEntity.animationTick;

@Mixin(KegBlockEntity.class)
public class MixinKegBlockEntity
{
    @Inject(method = "fermentingTick", at = @At("HEAD"), remap = false, cancellable = true)
    private static void brewingTickWithPurity(Level level, BlockPos pos, BlockState state, KegBlockEntity keg, CallbackInfo ci)
    {
        boolean didInventoryChange = false;
        KegBlockEntityAccessor kegAcc = (KegBlockEntityAccessor) keg;

        if (kegAcc.invokeHasInput()) {
            Optional<KegRecipe> recipe = kegAcc.invokeGetMatchingRecipe(new RecipeWrapper(keg.getInventory()));
            if (recipe.isPresent() && kegAcc.invokeCanFerment((KegRecipe) recipe.get()) && WaterPurity.isWaterFilledContainer(recipe.get().getResultItem()))
            {
                didInventoryChange = kegAcc.invokeProcessFermenting((KegRecipe) recipe.get());
                if(didInventoryChange)
                {
                    int purity = WaterPurity.getPurity(keg.getInventory().getStackInSlot(4));

                    purity = purity < WaterPurity.MAX_PURITY ? Math.max(purity - 1, WaterPurity.MIN_PURITY) : purity;

                    WaterPurity.addPurity(keg.getInventory().getStackInSlot(5), purity);
                }
            } else
                return;
        } else
            return;

        ItemStack mealStack = keg.getMeal();
        if (!mealStack.isEmpty())
        {
            animationTick(level, pos, state, keg);
            if (!kegAcc.invokeDoesMealHaveContainer(mealStack))
            {
                kegAcc.invokeMoveMealToOutput();
                didInventoryChange = true;
            } else if (!keg.getInventory().getStackInSlot(3).isEmpty())
            {
                kegAcc.invokeUseStoredContainersOnMeal();
                didInventoryChange = true;
            }
        }

        if (didInventoryChange)
        {
            ((SyncedBlockEntityAccessor) keg).invokeInventoryChanged();
        }

        ci.cancel();
    }
}
