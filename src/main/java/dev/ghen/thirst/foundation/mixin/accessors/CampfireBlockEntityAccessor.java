package dev.ghen.thirst.foundation.mixin.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CampfireBlockEntity.class)
public interface CampfireBlockEntityAccessor
{
    @Accessor
    int[] getCookingProgress();

    @Accessor
    int[] getCookingTime();
}
