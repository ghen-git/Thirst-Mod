package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.crafting.KettlePouringRecipe;

import java.util.Optional;

@Mixin(value = KettleBlockEntity.class,remap = false)
public class MixinKettleBlockEntity {
    @Redirect(method = "processBrewing",at= @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;setFluid(Lnet/minecraftforge/fluids/FluidStack;)V"))
    private void ProcessBrewing(FluidTank instance, FluidStack stack){
        int purity = Math.min(WaterPurity.getPurity(instance.getFluid()) + CommonConfig.KETTLE_PURIFICATION_LEVELS.get().intValue(), WaterPurity.MAX_PURITY);
        instance.setFluid(WaterPurity.addPurity(stack,purity));
    }

    @Redirect(method = "canBrew",at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/FluidStack;areFluidStackTagsEqual(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/FluidStack;)Z"))
    private boolean canBrew(FluidStack stack1, FluidStack other){
        FluidStack stack = stack1.copy();
        stack.removeChildTag("Purity");
        return WaterPurity.matchRecipe(stack,other);
    }

    /**
     * @author mlus
     * @reason add purity to output
     */
    @Overwrite
    public ItemStack fluidExtract(KettleBlockEntity kettle, ItemStack slotIn, ItemStack slotOut) {
        Item container = slotIn.getItem();
        ItemStack output = ItemStack.EMPTY;
        Optional<KettlePouringRecipe> recipe = kettle.getPouringRecipe(container, kettle.getFluidTank().getFluid());
        boolean changed = false;
        if (recipe.isPresent() && (kettle.getFluidTank().isEmpty() || kettle.getFluidTank().getFluid().getFluid().isSame(recipe.get().getFluid()))) {
            if (container.equals(recipe.get().getContainer().getItem()) && recipe.get().getAmount() <= kettle.getFluidTank().getFluidAmount()) {

                int purity = WaterPurity.getPurity(kettle.getFluidTank().getFluid());

                for (; kettle.getFluidTank().getFluidAmount() >= recipe.get().getAmount() && (output.isEmpty() && slotOut.isEmpty() || ItemHandlerHelper.canItemStacksStack(slotOut,
                        WaterPurity.addPurity(recipe.get().getOutput().copyWithCount(output.getCount() + 1),purity))); changed = true) {
                    if (slotOut.getCount() + 1 > slotOut.getMaxStackSize() || slotIn.getCount() == 0 || slotIn.isEmpty() || slotIn.getCount() < recipe.get().getOutput().getCount()) {
                        break;
                    }
                    kettle.getFluidTank().drain(new FluidStack(kettle.getFluidTank().getFluid(), recipe.get().getAmount()), IFluidHandler.FluidAction.EXECUTE);
                    slotIn.shrink(recipe.get().getContainer().getCount());
                    if (output.isEmpty()) {
                        output = recipe.get().getOutput().copy();
                        WaterPurity.addPurity(output,purity);
                    } else {
                        output.grow(recipe.get().getOutput().getCount());
                    }
                }

                if (changed) {
                    if (kettle.getLevel().isClientSide()) {
                        kettle.getLevel().playLocalSound(kettle.getBlockPos(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    }

                    kettle.setChanged();
                }
            } else if (container.equals(recipe.get().getOutput().getItem()) && kettle.getFluidTank().getFluidAmount() + recipe.get().getAmount() <= kettle.getFluidTank().getCapacity()) {
                for (; kettle.getFluidTank().getFluidAmount() + recipe.get().getAmount() <= kettle.getFluidTank().getCapacity() && slotIn.getCount() != 0 && !slotIn.isEmpty() && slotIn.getCount() >= recipe.get().getContainer().getCount() && (output.isEmpty() && slotOut.isEmpty() || ItemHandlerHelper.canItemStacksStack(slotOut, recipe.get().getContainer().copyWithCount(output.getCount() + 1))); changed = true) {
                    if (slotOut.getCount() + 1 > slotOut.getMaxStackSize()) {
                        break;
                    }

                    kettle.getFluidTank().fill(new FluidStack(recipe.get().getFluid(), recipe.get().getAmount()), IFluidHandler.FluidAction.EXECUTE);
                    slotIn.shrink(recipe.get().getOutput().getCount());
                    if (output.isEmpty()) {
                        output = recipe.get().getContainer().copy();
                    } else {
                        output.grow(recipe.get().getContainer().getCount());
                    }
                }

                if (changed) {
                    if (kettle.getLevel().isClientSide()) {
                        kettle.getLevel().playLocalSound(kettle.getBlockPos(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                    }

                    kettle.setChanged();
                }
            }
        }

        LazyOptional<IFluidHandlerItem> fluidHandler = slotIn.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem iFluidItemHandler = fluidHandler.orElse( null);
        if (fluidHandler.isPresent() && !slotIn.isEmpty()) {
            int amountToDrain;
            int amount;
            if (!kettle.getFluidTank().getFluid().isFluidEqual(iFluidItemHandler.getFluidInTank(0)) && !kettle.getFluidTank().getFluid().isEmpty()) {
                if (!kettle.getFluidTank().getFluid().isEmpty() && iFluidItemHandler.isFluidValid(0, kettle.getFluidTank().getFluid())) {
                    amountToDrain = kettle.getFluidTank().getFluidAmount();
                    amount = iFluidItemHandler.fill(new FluidStack(kettle.getFluidTank().getFluid(), amountToDrain), IFluidHandler.FluidAction.SIMULATE);
                    if (amount > 0) {
                        iFluidItemHandler.fill(new FluidStack(kettle.getFluidTank().getFluid(), amountToDrain), IFluidHandler.FluidAction.EXECUTE);
                        kettle.getFluidTank().drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE);
                        if (amount <= amountToDrain) {
                            slotIn.setCount(0);
                            if (output.isEmpty()) {
                                output = iFluidItemHandler.getContainer().copy();
                            } else {
                                output.grow(iFluidItemHandler.getContainer().getCount());
                            }

                            kettle.setChanged();
                        }
                    }
                }
            } else {
                amountToDrain = kettle.getFluidTank().getCapacity() - kettle.getFluidTank().getFluidAmount();
                amount = iFluidItemHandler.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE).getAmount();
                if (amount > 0) {
                    kettle.getFluidTank().fill(iFluidItemHandler.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    if (amount <= amountToDrain) {
                        slotIn.setCount(0);
                        if (output.isEmpty()) {
                            output = iFluidItemHandler.getContainer().copy();
                        } else {
                            output.grow(iFluidItemHandler.getContainer().getCount());
                        }

                        kettle.setChanged();
                    }
                }
            }
        }

        return output;
    }
}
