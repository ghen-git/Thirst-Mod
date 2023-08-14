package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
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
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.crafting.KettleRecipe;

import java.util.Optional;

@Mixin(KettleBlockEntity.class)
public abstract class MixinKettleBlockEntity {

    @Inject(method = "brewingTick", at = @At("HEAD"), remap = false, cancellable = true)
    private static void brewingTickWithPurity(Level level, BlockPos pos, BlockState state, KettleBlockEntity kettle, CallbackInfo ci) {
        boolean isHeated = kettle.isHeated(level, pos);
        boolean didInventoryChange;
        KettleBlockEntityAccessor kettleAcc = (KettleBlockEntityAccessor)kettle;
        if (isHeated && kettleAcc.invokeHasInput()) {
            Optional<KettleRecipe> recipe = kettleAcc.invokeGetMatchingRecipe(new RecipeWrapper(kettle.getInventory()));
            if (recipe.isPresent() && kettleAcc.invokeCanBrew(recipe.get()) && WaterPurity.isWaterFilledContainer(recipe.get().getResultItem())) {
                didInventoryChange = kettleAcc.invokeProcessBrewing(recipe.get(), kettle);
                if(didInventoryChange) {
                    int purity = Math.min(WaterPurity.getBlockPurity(kettle.getBlockState()) + CommonConfig.KETTLE_PURIFICATION_LEVELS.get().intValue(), WaterPurity.MAX_PURITY);
                    kettle.getInventory().setStackInSlot(2, WaterPurity.addPurity(kettle.getInventory().getStackInSlot(2), purity));
                }


            ItemStack mealStack = kettle.getMeal();
            if (!mealStack.isEmpty()) {
                if (!kettleAcc.invokeDoesMealHaveContainer(mealStack)) {
                    kettleAcc.invokeMoveMealToOutput();
                    didInventoryChange = true;
                } else if (!kettle.getInventory().getStackInSlot(3).isEmpty()) {
                    kettleAcc.invokeUseStoredContainersOnMeal();
                    didInventoryChange = true;
                }
             }

        if (didInventoryChange) {
            ((SyncedBlockEntityAccessor)kettle).invokeInventoryChanged();
        }

            ci.cancel();
            }
        }
    }
}
