package dev.ghen.thirst.foundation.mixin.brewinandchewin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.mixin.accessors.brewinandchewin.KegBlockEntityAccessor;
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
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegRecipe;

import java.util.Optional;


@Mixin(KegBlockEntity.class)
public class MixinKegBlockEntity
{
    @Inject(method = "fermentingTick", at = @At("HEAD"), remap = false, cancellable = true)
    private static void brewingTickWithPurity(Level level, BlockPos pos, BlockState state, KegBlockEntity keg, CallbackInfo ci)
    {
        boolean didInventoryChange = false;
        KegBlockEntityAccessor kegAcc = (KegBlockEntityAccessor) keg;
        keg.updateTemperature();

        if (kegAcc.invokeHasInput()) {
            Optional<KegRecipe> recipe = kegAcc.invokeGetMatchingRecipe(new RecipeWrapper(keg.getInventory()));
            if (recipe.isPresent() && kegAcc.invokeCanFerment(recipe.get()) &&
                    WaterPurity.isWaterFilledContainer(recipe.get().getResultItem()))
            {
                didInventoryChange = kegAcc.invokeProcessFermenting(recipe.get(), keg);
                int purity = WaterPurity.getPurity(keg.getInventory().getStackInSlot(4));
                if(didInventoryChange)
                {


                    purity = purity < CommonConfig.FERMENTATION_MOLDING_THRESHOLD.get().intValue() ?
                            Math.max(purity - CommonConfig.FERMENTATION_MOLDING_HARSHNESS.get().intValue(), WaterPurity.MIN_PURITY) : purity;

                    keg.getInventory().setStackInSlot(5, WaterPurity.addPurity(keg.getInventory().getStackInSlot(5), purity));
                }
            } else
                return;
        } else
            return;

        ItemStack mealStack = keg.getDrink();
        if (!mealStack.isEmpty())
        {
            if (!kegAcc.invokeDoesDrinkHaveContainer(mealStack))
            {
                kegAcc.invokeMoveDrinkToOutput();
                didInventoryChange = true;
            } else if (!keg.getInventory().getStackInSlot(6).isEmpty())
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
