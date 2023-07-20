package dev.ghen.thirst.compat.create.ponder.scene;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SandFilterScene {
    public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sand_filter", "Purifying the water with sand filter");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.world.showSection(util.select.fromTo(0, 0, 0,5,5,5), Direction.UP);

        scene.idle(5);

        BlockPos filter = util.grid.at(2, 3, 2);
        BlockPos tankAbove = util.grid.at(2, 5, 2);
        BlockPos tankBelow= util.grid.at(2, 1, 2);

        scene.overlay.showText(60)
                .text("Connect the input and output above and below the filter")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector.blockSurface(filter,Direction.WEST));

        scene.idle(50);

        FluidStack content = new FluidStack(Fluids.WATER,4000);
        scene.world.modifyBlockEntity(tankAbove, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content, IFluidHandler.FluidAction.EXECUTE));

        scene.idle(30);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("The filter will purify the water input from above it")
                .placeNearTarget()
                .pointAt(util.vector.blockSurface(tankAbove,Direction.WEST));

        scene.idle(50);

        scene.world.modifyBlockEntity(tankAbove, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .drain(4000, IFluidHandler.FluidAction.EXECUTE));

        scene.overlay.showText(60)
                .attachKeyFrame()
                .text("The purified water will be output from below the filter")
                .placeNearTarget()
                .pointAt(util.vector.blockSurface(tankBelow,Direction.WEST));

        scene.world.modifyBlockEntity(tankBelow, FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content, IFluidHandler.FluidAction.EXECUTE));

        scene.idle(50);

    }
}
