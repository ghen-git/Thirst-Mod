package dev.ghen.thirst.foundation.mixin.accessors;

import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CampfireBlockEntity.class)
public interface CampfireBlockEntityAccessor
{
    @Accessor
    int[] getCookingProgress();

    @Accessor
    int[] getCookingTime();
}
