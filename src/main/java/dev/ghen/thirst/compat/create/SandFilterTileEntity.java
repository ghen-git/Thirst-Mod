package dev.ghen.thirst.compat.create;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SandFilterTileEntity extends SmartBlockEntity implements IHaveGoggleInformation
{
    public static final int TANK_SIZE = 1000;
    SmartFluidTankBehaviour dirtyTank;
    SmartFluidTankBehaviour purifiedTank;

    public SandFilterTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        dirtyTank = SmartFluidTankBehaviour.single(this, TANK_SIZE);
        behaviours.add(dirtyTank);
        purifiedTank = SmartFluidTankBehaviour.single(this, TANK_SIZE);
        behaviours.add(purifiedTank);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }


    private boolean trackFoods() {
        return getBehaviour(AdvancementBehaviour.TYPE).isOwnerPresent();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && side != null && side.getAxis() == Direction.Axis.Y)
        {
            if(side == Direction.DOWN)
                return purifiedTank.getCapability()
                        .cast();
            else
                return dirtyTank.getCapability()
                        .cast();
        }
        return super.getCapability(cap, side);
    }

    public void tick()
    {
        super.tick();

        if(!level.isClientSide() && dirtyTank.getPrimaryHandler().getFluidAmount() >= CommonConfig.SAND_FILTER_MB_PER_TICK.get().intValue() &&
                purifiedTank.getPrimaryHandler().getFluidAmount() < TANK_SIZE)
        {
            FluidStack water = dirtyTank.getPrimaryHandler().drain(CommonConfig.SAND_FILTER_MB_PER_TICK.get().intValue(), IFluidHandler.FluidAction.EXECUTE);

            if(WaterPurity.hasPurity(water))
                WaterPurity.addPurity(water, Math.min(WaterPurity.getPurity(water) + CommonConfig.SAND_FILTER_FILTRATION_AMOUNT.get().intValue(), WaterPurity.MAX_PURITY));

            purifiedTank.getPrimaryHandler().fill(water, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking)
    {
            LangBuilder mb = Lang.translate("generic.unit.millibuckets");
            Lang.translate("gui.goggles.fluid_container")
                    .forGoggles(tooltip);

            int dirtyWaterAmount = dirtyTank.getPrimaryHandler().getFluidAmount();
            int purifiedWaterAmount = purifiedTank.getPrimaryHandler().getFluidAmount();

        buildTooltip(tooltip, mb, dirtyWaterAmount, dirtyTank);

        buildTooltip(tooltip, mb, purifiedWaterAmount, purifiedTank);

        if(dirtyTank.isEmpty() && purifiedTank.isEmpty()){
            Lang.translate("gui.goggles.fluid_container.capacity")
                    .add(Lang.number(dirtyTank.getPrimaryHandler().getTankCapacity(0))
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }

        return !dirtyTank.isEmpty() || !purifiedTank.isEmpty();
    }

    private void buildTooltip(List<Component> tooltip, LangBuilder mb, int purifiedWaterAmount, SmartFluidTankBehaviour purifiedTank) {
        if(!purifiedTank.isEmpty())
        {
            Lang.builder()
                    .text(WaterPurity.getPurityText(WaterPurity.getPurity(purifiedTank.getPrimaryHandler().getFluid())))
                    .add(Lang.text(" "))
                    .add(Lang.fluidName(purifiedTank.getPrimaryHandler().getFluid()))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            Lang.builder()
                    .add(Lang.number(purifiedWaterAmount)
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(Lang.number(purifiedTank.getPrimaryHandler().getCapacity())
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }
    }
}