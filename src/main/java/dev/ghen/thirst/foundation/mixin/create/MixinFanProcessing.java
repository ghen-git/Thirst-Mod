package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.fan.FanProcessing;
import com.simibubi.create.content.kinetics.fan.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.SplashingRecipe;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.world.item.ItemStack;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Mixin(value = FanProcessing.class,remap = false)
public class MixinFanProcessing {

    @Final
    @Shadow
    private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));
    @Final
    @Shadow
    private static final FanProcessing.SplashingWrapper SPLASHING_WRAPPER = new FanProcessing.SplashingWrapper();

    @Final
    @Shadow
    private static final FanProcessing.HauntingWrapper HAUNTING_WRAPPER = new FanProcessing.HauntingWrapper();


    @Inject(method = "process",at= @At("HEAD"), cancellable = true)
    private static void process(ItemStack stack, FanProcessing.Type type, Level world, CallbackInfoReturnable<List<ItemStack>> cir) {
        if (type == FanProcessing.Type.SPLASHING) {
            SPLASHING_WRAPPER.setItem(0, stack);
            Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(SPLASHING_WRAPPER, world);
            if (recipe.isPresent())
                return;
            return;
        }
        if (type == FanProcessing.Type.HAUNTING) {
            HAUNTING_WRAPPER.setItem(0, stack);
            Optional<HauntingRecipe> recipe = AllRecipeTypes.HAUNTING.find(HAUNTING_WRAPPER, world);
            if (recipe.isPresent())
                return;
            return;
        }

        RECIPE_WRAPPER.setItem(0, stack);
        Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, world);

        if (type == FanProcessing.Type.BLASTING) {
            if(Objects.requireNonNull(stack.serializeNBT().get("id")).getAsString().equals("minecraft:potion")){
                WaterPurity.addPurity(stack, Math.min(WaterPurity.getPurity(stack) + CommonConfig.SAND_FILTER_FILTRATION_AMOUNT.get().intValue(), WaterPurity.MAX_PURITY));
                cir.setReturnValue(Collections.singletonList(stack));
                return;
            }

            RECIPE_WRAPPER.setItem(0, stack);
            Optional<? extends AbstractCookingRecipe> smeltingRecipe = world.getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, RECIPE_WRAPPER, world);
            if (smeltingRecipe.isEmpty()) {
                RECIPE_WRAPPER.setItem(0, stack);
                smeltingRecipe = world.getRecipeManager()
                        .getRecipeFor(RecipeType.BLASTING, RECIPE_WRAPPER, world);
                if(smokingRecipe.isEmpty() && WaterPurity.hasPurity(stack)){
                    WaterPurity.addPurity(stack, Math.min(WaterPurity.getPurity(stack) + CommonConfig.SAND_FILTER_FILTRATION_AMOUNT.get().intValue(), WaterPurity.MAX_PURITY));
                    cir.setReturnValue(Collections.singletonList(stack));
                }
            }

            if (smeltingRecipe.isPresent()) {
                if (smokingRecipe.isEmpty() || !ItemStack.isSame(smokingRecipe.get()
                                .getResultItem(),
                        smeltingRecipe.get()
                                .getResultItem())) {
                    return;
                }
            }

            return;
        }

        if (type == FanProcessing.Type.SMOKING && smokingRecipe.isPresent())
            return;

        return;
    }
}
