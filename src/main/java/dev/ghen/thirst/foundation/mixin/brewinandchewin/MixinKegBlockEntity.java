package dev.ghen.thirst.foundation.mixin.brewinandchewin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.mixin.accessors.brewinandchewin.KegBlockEntityAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import vectorwing.farmersdelight.common.mixin.accessor.RecipeManagerAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Mixin(value = KegBlockEntity.class,remap = false)
public abstract class MixinKegBlockEntity
{

    @Inject(method = "processFermenting", at = @At(value = "INVOKE", target = "Lumpaz/brewinandchewin/common/crafting/KegFermentingRecipe;getResultFluid()Lnet/minecraft/world/level/material/Fluid;",ordinal = 0), cancellable = true)
    private void processFermentingWithPurity(KegFermentingRecipe recipe, KegBlockEntity keg, CallbackInfoReturnable<Boolean> cir) {
        if (recipe.getResultFluid() != null) {

            int purity = WaterPurity.getPurity(keg.getFluidTank().getFluid());
            purity = purity < CommonConfig.FERMENTATION_MOLDING_THRESHOLD.get().intValue() ?
                    Math.max(purity - CommonConfig.FERMENTATION_MOLDING_HARSHNESS.get().intValue(), WaterPurity.MIN_PURITY) : purity;

            keg.getFluidTank().setFluid(WaterPurity.addPurity(
                    new FluidStack(recipe.getResultFluid(), keg.getFluidTank().getFluidAmount()), purity));
            if (keg.getLevel().isClientSide()) {
                keg.getLevel().playLocalSound(keg.getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F, true);
            }
        }

        int purity_output = 0;

        if (recipe.getResultItem() != null) {
            if (recipe.getFluidIngredient() != null) {
                purity_output = WaterPurity.getPurity(keg.getFluidTank().getFluid());
                keg.getFluidTank().drain(recipe.getFluidIngredient().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            }

            ItemStack output = new ItemStack(recipe.getResultItem(), recipe.getAmount());

            if(WaterPurity.isWaterFilledContainer(output)){
                WaterPurity.addPurity(output, purity_output);
            }

            keg.getInventory().insertItem(5,output,false);
        }

        for(int i = 0; i < 5; ++i) {
            ItemStack slotStack = keg.getInventory().getStackInSlot(i);
            if (slotStack.hasCraftingRemainingItem()) {
                ((KegBlockEntityAccessor) keg).invokeEjectIngredientRemainder(slotStack.getCraftingRemainingItem());
            }

            if (!slotStack.isEmpty()) {
                slotStack.shrink(1);
            }
        }

        cir.setReturnValue(true);
    }

    @Shadow private ResourceLocation lastRecipeID;
    @Shadow private boolean checkNewRecipe;
    @Shadow private int fermentTime;


    /**
     * @author mlus
     * @reason match recipe
     */
    @Overwrite
    private Optional<KegFermentingRecipe> getMatchingRecipe(KegRecipeWrapper inventoryWrapper) {
        if (((KegBlockEntity) (Object) this).getLevel() == null) {
            return Optional.empty();
        } else {
            if (lastRecipeID != null) {
                Recipe<KegRecipeWrapper> recipe = (Recipe)((RecipeManagerAccessor) ((KegBlockEntity) (Object) this).getLevel().getRecipeManager()).getRecipeMap((RecipeType)BnCRecipeTypes.FERMENTING.get()).get(this.lastRecipeID);
                if (recipe instanceof KegFermentingRecipe && recipe.matches(inventoryWrapper, ((KegBlockEntity) (Object) this).getLevel())) {
                    return Optional.of((KegFermentingRecipe)recipe);
                }
            }

            if (checkNewRecipe) {
                FluidStack stack = ((KegBlockEntity) (Object) this).getFluidTank().getFluid().copy();
                stack.removeChildTag("Purity");
                Optional<KegFermentingRecipe> recipe = ((KegBlockEntity) (Object) this).getLevel().getRecipeManager().getAllRecipesFor(BnCRecipeTypes.FERMENTING.get()).stream().filter((a) -> a.matches(inventoryWrapper, ((KegBlockEntity) (Object) this).getLevel()) && (a.getFluidIngredient() == null || WaterPurity.matchRecipe(a.getFluidIngredient() ,stack))).findFirst();
                if (recipe.isPresent()) {
                    ResourceLocation newRecipeID = recipe.get().getId();
                    if (this.lastRecipeID != null && !this.lastRecipeID.equals(newRecipeID)) {
                        this.fermentTime = 0;
                    }

                    this.lastRecipeID = newRecipeID;
                    return recipe;
                }
            }

            this.checkNewRecipe = false;
            return Optional.empty();
        }
    }

    @Shadow @Final private KegRecipeWrapper recipeWrapper;

    /**
     * @author mlus
     * @reason add purity tag
     */
    @Overwrite
    private List<ItemStack> fluidExtract(ItemStack slotIn, int maxTakeAmount, boolean inGui, boolean isCreative) {
        KegBlockEntity keg = (KegBlockEntity) (Object) this;
        if (slotIn.isEmpty()) {
            return List.of();
        } else {
            Optional<KegPouringRecipe> recipe = keg.getPouringRecipe(slotIn);
            boolean changed = false;
            List<ItemStack> outputs = new ArrayList<>();
            int amountToDrain;
            if (recipe.isPresent() && (keg.getFluidTank().isEmpty() || keg.getFluidTank().getFluid().getFluid() == recipe.get().getRawFluid())) {
                ItemStack resultItem = recipe.get().assemble(recipeWrapper, keg.getLevel().registryAccess());
                int containerAmount;
                if (!ItemStack.isSameItem(slotIn, recipe.get().getContainer()) || recipe.get().getAmount() > keg.getFluidTank().getFluidAmount() || inGui && !keg.getInventory().getStackInSlot(5).isEmpty() && !ItemStack.isSameItemSameTags(resultItem, keg.getInventory().getStackInSlot(5))) {
                    if (recipe.filter(KegPouringRecipe::canFill).isPresent() && (recipe.get().isStrict() && ItemStack.isSameItemSameTags(resultItem, slotIn) || !recipe.get().isStrict() && ItemStack.isSameItem(slotIn, resultItem)) && (keg.getFluidTank().isEmpty() || keg.getFluidTank().getFluidAmount() < keg.getFluidTank().getCapacity()) && (!inGui || keg.getInventory().getStackInSlot(5).isEmpty() || ItemStack.isSameItemSameTags(recipe.get().getContainer(), keg.getInventory().getStackInSlot(5)))) {
                        containerAmount = Mth.clamp(Math.min(slotIn.getCount(), keg.getFluidTank().getCapacity() / recipe.get().getAmount()), 1, maxTakeAmount);
                        keg.getFluidTank().fill( WaterPurity.addPurity(new FluidStack(recipe.get().getFluid(slotIn), recipe.get().getAmount() * containerAmount)
                                ,WaterPurity.getPurity(slotIn)), IFluidHandler.FluidAction.EXECUTE);
                        if (!isCreative) {
                            ItemStack recipeItem = recipe.get().getContainer(slotIn);
                            amountToDrain = containerAmount;

                            while(amountToDrain > 0 && !slotIn.isEmpty()) {
                                ItemStack newResult = recipeItem.copyWithCount(Math.min(recipeItem.getMaxStackSize(), amountToDrain));
                                outputs.add(newResult);
                                amountToDrain -= newResult.getCount();
                                slotIn.shrink(newResult.getCount());
                            }

                            if (!slotIn.isEmpty()) {
                                outputs.add(slotIn);
                            }
                        } else {
                            outputs.add(slotIn);
                        }

                        changed = true;
                    }
                } else {
                    int purity = keg.getFluidTank().isEmpty()? CommonConfig.DEFAULT_PURITY.get():
                            WaterPurity.getPurity(keg.getFluidTank().getFluid());

                    containerAmount = Mth.clamp(Math.min(slotIn.getCount(), maxTakeAmount), 1, keg.getFluidTank().getCapacity() / recipe.get().getAmount());
                    keg.getFluidTank().drain(new FluidStack(keg.getFluidTank().getFluid(), recipe.get().getAmount() * containerAmount), IFluidHandler.FluidAction.EXECUTE);
                    if (!isCreative) {
                        int overflow = containerAmount;

                        if(WaterPurity.isWaterFilledContainer(resultItem))
                            WaterPurity.addPurity(resultItem,purity);

                        while(overflow > 0 && !slotIn.isEmpty()) {
                            ItemStack newResult = resultItem.copyWithCount(Math.min(resultItem.getMaxStackSize(), overflow));
                            outputs.add(newResult);
                            overflow -= newResult.getCount();
                            slotIn.shrink(newResult.getCount());
                        }

                        if (!slotIn.isEmpty()) {
                            outputs.add(slotIn);
                        }
                    } else {
                        outputs.add(slotIn);
                    }

                    changed = true;
                }

                if (changed) {
                    keg.setChanged();
                }
            }

            if (outputs.isEmpty() && recipe.isEmpty()) {
                LazyOptional<IFluidHandlerItem> fluidHandler = isCreative ? slotIn.copy().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM) : slotIn.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                IFluidHandlerItem iFluidItemHandler = fluidHandler.orElse(null);
                if (fluidHandler.isPresent() && !slotIn.isEmpty()) {
                    int amount;
                    IFluidHandlerItem finalIFluidItemHandler = iFluidItemHandler;
                    if (keg.getFluidTank().getFluid().isFluidEqual(iFluidItemHandler.getFluidInTank(0)) || keg.getFluidTank().getFluid().isEmpty() && (!inGui || keg.getInventory().getStackInSlot(5).isEmpty() || keg.getInventory().getStackInSlot(5).is(iFluidItemHandler.getContainer().getItem())) && keg.getLevel().getRecipeManager().getAllRecipesFor((RecipeType)BnCRecipeTypes.KEG_POURING.get()).stream().anyMatch((pouringRecipe) -> ((KegPouringRecipe)pouringRecipe).getFluid(slotIn).isFluidEqual(finalIFluidItemHandler.getFluidInTank(0)))) {
                        amountToDrain = keg.getFluidTank().getCapacity() - keg.getFluidTank().getFluidAmount();
                        amount = keg.getFluidTank().fill(iFluidItemHandler.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.SIMULATE);
                        if (amount <= amountToDrain && amount > 0) {
                            keg.getFluidTank().fill(iFluidItemHandler.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                            if (!isCreative) {
                                ItemStack recipeItem = slotIn.getCraftingRemainingItem().isEmpty() ? iFluidItemHandler.getContainer() : slotIn.getCraftingRemainingItem();
                                int overflow = amount / keg.getFluidTank().getCapacity();

                                while(overflow > 0 && !slotIn.isEmpty()) {
                                    ItemStack newResult = recipeItem.copyWithCount(Math.min(recipeItem.getMaxStackSize(), overflow));
                                    outputs.add(newResult);
                                    overflow -= newResult.getCount();
                                    slotIn.shrink(newResult.getCount());
                                }
                            } else {
                                outputs.add(slotIn);
                            }

                            keg.setChanged();
                        }
                    } else if (!keg.getFluidTank().getFluid().isEmpty() && iFluidItemHandler.isFluidValid(0, keg.getFluidTank().getFluid()) && (!inGui || keg.getInventory().getStackInSlot(5).isEmpty() || keg.getInventory().getStackInSlot(5).is(iFluidItemHandler.getContainer().getItem()))) {
                        amountToDrain = iFluidItemHandler.getTankCapacity(0);
                        iFluidItemHandler = slotIn.copyWithCount(amountToDrain / iFluidItemHandler.getTankCapacity(0)).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
                        amount = iFluidItemHandler.fill(keg.getFluidTank().drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.SIMULATE);
                        if (amount > 0) {
                            iFluidItemHandler.fill(keg.getFluidTank().drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                            if (amount <= amountToDrain) {
                                outputs.add(slotIn);
                                keg.setChanged();
                            }
                        }
                    }
                }

                return outputs;
            } else {
                return outputs;
            }
        }
    }
}