package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.content.kinetics.fan.FanProcessing;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Mixin(value = FanProcessing.class,remap = false)
public class MixinFanProcessing {

    @Final
    @Shadow
    private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));


    @Inject(method = "process",at= @At("HEAD"), cancellable = true)
    private static void process(ItemStack stack, FanProcessing.Type type, Level world, CallbackInfoReturnable<List<ItemStack>> cir) {

        RECIPE_WRAPPER.setItem(0, stack);
        Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, world);

        if (type == FanProcessing.Type.BLASTING) {
            if(stack.getItem() == Items.POTION){
                WaterPurity.addPurity(stack, Math.min(WaterPurity.getPurity(stack) + 1, WaterPurity.MAX_PURITY));
                List<ItemStack> mutableList = new ArrayList<>();
                mutableList.add(stack);
                cir.setReturnValue(mutableList);
                return;
            }

            RECIPE_WRAPPER.setItem(0, stack);
            Optional<? extends AbstractCookingRecipe> smeltingRecipe = world.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, RECIPE_WRAPPER, world);
            if (smeltingRecipe.isEmpty()) {
                RECIPE_WRAPPER.setItem(0, stack);
                if(smokingRecipe.isEmpty() && WaterPurity.hasPurity(stack)){
                    WaterPurity.addPurity(stack, Math.min(WaterPurity.getPurity(stack) + 1, WaterPurity.MAX_PURITY));
                    List<ItemStack> mutableList = new ArrayList<>();
                    mutableList.add(stack);
                    cir.setReturnValue(mutableList);
                }
            }

        }

    }
}
