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
//import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        dirtyTank = SmartFluidTankBehaviour.single(this, TANK_SIZE);
        behaviours.add(dirtyTank);
        purifiedTank = SmartFluidTankBehaviour.single(this, TANK_SIZE);
        behaviours.add(purifiedTank);
    }

    private boolean trackFoods() {
        return getBehaviour(AdvancementBehaviour.TYPE).isOwnerPresent();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != null && side.getAxis() == Direction.Axis.Y)
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

        if(!level.isClientSide() && dirtyTank.getPrimaryHandler().getFluidAmount() > CommonConfig.SAND_FILTER_MB_PER_TICK.get().intValue() &&
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

            if(!dirtyTank.isEmpty())
            {
                Lang.builder()
                        .text(WaterPurity.getPurityText(WaterPurity.getPurity(dirtyTank.getPrimaryHandler().getFluid())))
                        .add(Lang.text(" "))
                        .add(Lang.fluidName(dirtyTank.getPrimaryHandler().getFluid()))
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip);

                Lang.builder()
                        .add(Lang.number(dirtyWaterAmount)
                                .add(mb)
                                .style(ChatFormatting.GOLD))
                        .text(ChatFormatting.GRAY, " / ")
                        .add(Lang.number(dirtyTank.getPrimaryHandler().getCapacity())
                                .add(mb)
                                .style(ChatFormatting.DARK_GRAY))
                        .forGoggles(tooltip, 1);
            }

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

            return !dirtyTank.isEmpty() || !purifiedTank.isEmpty();
    }
}