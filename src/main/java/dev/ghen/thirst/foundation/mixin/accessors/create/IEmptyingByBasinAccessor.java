package dev.ghen.thirst.foundation.mixin.accessors.create;

import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EmptyingByBasin.class)
public interface IEmptyingByBasinAccessor
{
    @Accessor
    RecipeWrapper getWrapper();
}
