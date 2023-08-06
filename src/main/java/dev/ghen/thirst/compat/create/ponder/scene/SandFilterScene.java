package dev.ghen.thirst.compat.create.ponder.scene;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SandFilterScene {
    public static void filtering(SceneBuilder scene, SceneBuildingUtil util)
    {
        scene.title("sand_filter", "Purifying Water with a Sand Filter");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);

        Selection filter = util.select.position(2, 3, 2);
        BlockPos filterPos = util.grid.at(2, 3, 2);

        scene.world.showSection(filter, Direction.DOWN);
        scene.idle(5);

        Vec3 filterSide = util.vector.blockSurface(filterPos, Direction.WEST);
        scene.overlay.showText(80)
                .pointAt(filterSide)
                .placeNearTarget()
                .attachKeyFrame()
                .text("A Sand Filter purifies by one step water pumped through it");

        scene.idle(70);

        Selection gears = util.select.fromTo(3, 1, 2, 3, 5, 5);
        Selection bottomGear = util.select.position(3, 0, 5);
        Selection topTank = util.select.fromTo(2, 4, 2, 2, 5, 2);
        BlockPos topTankPos = util.grid.at(2, 5, 2);

        scene.world.showSection(topTank, Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(gears, Direction.WEST);
        scene.world.showSection(bottomGear, Direction.WEST);
        scene.idle(5);

        FluidStack content = new FluidStack(Fluids.WATER, 4000);
        scene.world.modifyBlockEntity(topTankPos, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content, IFluidHandler.FluidAction.EXECUTE));

        Vec3 topTankSide = util.vector.blockSurface(topTankPos, Direction.EAST);
        scene.overlay.showText(60)
                .pointAt(topTankSide)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Dirty Water needs to be pumped in from the top...");

        scene.idle(30);

        Selection bottomTank = util.select.fromTo(2, 1, 2, 2, 2, 2);
        BlockPos bottomTankPos = util.grid.at(2, 1, 2);

        scene.world.showSection(bottomTank, Direction.DOWN);
        scene.idle(30);

        Vec3 bottomTankSide = util.vector.blockSurface(bottomTankPos, Direction.WEST);
        scene.overlay.showText(60)
                .pointAt(bottomTankSide)
                .placeNearTarget()
                .attachKeyFrame()
                .text("and Purified Water can be pumped out from the bottom");

        scene.idle(10);

        scene.world.modifyBlockEntity(topTankPos, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .drain(4000, IFluidHandler.FluidAction.EXECUTE));

        scene.world.modifyBlockEntity(bottomTankPos, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content, IFluidHandler.FluidAction.EXECUTE));

        scene.idle(50);

    }
}
