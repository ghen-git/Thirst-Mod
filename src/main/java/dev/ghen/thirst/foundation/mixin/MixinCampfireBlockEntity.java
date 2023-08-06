package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CampfireBlockEntity.class})
public class MixinCampfireBlockEntity
{
    public MixinCampfireBlockEntity() { }

    @Inject(
            method = {"particleTick"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private static void waterVapour(Level level, BlockPos pos, BlockState blockState, CampfireBlockEntity campfire, CallbackInfo ci) {
        RandomSource random = level.getRandom();
        int l = blockState.getValue(CampfireBlock.FACING).get2DDataValue();
        boolean cancel = false;

        for(int i = 0; i < campfire.getItems().size(); ++i) {
            ItemStack itemstack = campfire.getItems().get(i);
            if (WaterPurity.isWaterFilledContainer(itemstack)) {
                cancel = true;
                if (random.nextFloat() < 0.2F) {
                    Direction direction = Direction.from2DDataValue(Math.floorMod(i + l, 4));
                    final float f = 0.3125F;
                    double d0 = (double)pos.getX() + 0.5 - (double)((float)direction.getStepX() * f) + (double)((float)direction.getClockWise().getStepX() * f);
                    double d1 = (double)pos.getY() + 0.6;
                    double d2 = (double)pos.getZ() + 0.5 - (double)((float)direction.getStepZ() * f) + (double)((float)direction.getClockWise().getStepZ() * f);
                    level.addParticle(ParticleTypes.EFFECT, d0, d1, d2, 0.0, 0.001, 0.0);
                }
            }
        }

        if (cancel) {
            if (random.nextFloat() < 0.11F) {
                for(int i = 0; i < random.nextInt(2) + 2; ++i) {
                    CampfireBlock.makeParticles(level, pos, blockState.getValue(CampfireBlock.SIGNAL_FIRE), false);
                }
            }

            ci.cancel();
        }

    }
}
