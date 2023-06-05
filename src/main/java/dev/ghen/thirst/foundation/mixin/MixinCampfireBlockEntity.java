package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.mixin.accessors.CampfireBlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public class MixinCampfireBlockEntity
{
    @Shadow @Final private NonNullList<ItemStack> items;

    @Inject(method = "cookTick", at = @At("HEAD"), cancellable = true)
    private static void cookPurityContainers(Level level, BlockPos pos, BlockState blockState, CampfireBlockEntity campfire, CallbackInfo ci)
    {
        boolean flag = false;

        for(int i = 0; i < campfire.getItems().size(); ++i)
        {
            ItemStack itemstack = campfire.getItems().get(i);
            if (!itemstack.isEmpty())
            {
                if (((CampfireBlockEntityAccessor) campfire).getCookingProgress()[i] + 1 >= ((CampfireBlockEntityAccessor) campfire).getCookingTime()[i])
                {
                    if(WaterPurity.isWaterFilledContainer(itemstack))
                    {
                        ItemStack itemstack1 = WaterPurity.getFilledContainer(itemstack, true);
                        int purity = WaterPurity.getPurity(itemstack);
                        WaterPurity.addPurity(itemstack1, Math.min(purity + CommonConfig.CAMPFIRE_PURIFICATION_LEVELS.get().intValue(), 3));

                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemstack1);
                        campfire.getItems().set(i, ItemStack.EMPTY);
                        level.sendBlockUpdated(pos, blockState, blockState, 3);

                        ci.cancel();

                        campfire.setChanged();
                    }
                }
            }
        }
    }

    @Inject(method = "placeFood", at = @At("HEAD"))
    private void blockPotions(Entity p_238285_, ItemStack item, int p_238287_, CallbackInfoReturnable<Boolean> cir)
    {
        if(WaterPurity.isWaterFilledContainer(item))
        {
            if(WaterPurity.getPurity(item) == WaterPurity.MAX_PURITY)
                cir.cancel();
        }
        else if(item.is(Items.POTION))
            cir.cancel();
    }

    @Inject(method = "particleTick", at = @At("HEAD"), cancellable = true)
    private static void waterVapour(Level level, BlockPos pos, BlockState blockState, CampfireBlockEntity campfire, CallbackInfo ci)
    {
        RandomSource random = level.random;
        int l = blockState.getValue(CampfireBlock.FACING).get2DDataValue();
        boolean cancel = false;

        for(int j = 0; j < campfire.getItems().size(); ++j)
        {
            ItemStack itemstack = campfire.getItems().get(j);
            if (WaterPurity.isWaterFilledContainer(itemstack))
            {
                cancel = true;
                if(random.nextFloat() < 0.2F)
                {
                    Direction direction = Direction.from2DDataValue(Math.floorMod(j + l, 4));
                    float f = 0.3125F;
                    double d0 = (double)pos.getX() + 0.5D - (double)((float)direction.getStepX() * 0.3125F) + (double)((float)direction.getClockWise().getStepX() * 0.3125F);
                    double d1 = (double)pos.getY() + 0.6D;
                    double d2 = (double)pos.getZ() + 0.5D - (double)((float)direction.getStepZ() * 0.3125F) + (double)((float)direction.getClockWise().getStepZ() * 0.3125F);

                    level.addParticle(ParticleTypes.EFFECT, d0, d1, d2, 0.0D, 0.001d, 0.0D);
                }
            }
        }

        if(cancel)
        {
            if (random.nextFloat() < 0.11F)
            {
                for(int i = 0; i < random.nextInt(2) + 2; ++i)
                {
                    CampfireBlock.makeParticles(level, pos, blockState.getValue(CampfireBlock.SIGNAL_FIRE), false);
                }
            }

            ci.cancel();
        }
    }
}
