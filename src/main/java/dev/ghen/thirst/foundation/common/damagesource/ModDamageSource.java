package dev.ghen.thirst.foundation.common.damagesource;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageSource
{
    public static final DamageType DIE_OF_THIRST = new DamageType("dehydrate",0.0F);



    public static final DamageSource DEHYDRATE = (new DamageSource(new Holder.Direct<>(DIE_OF_THIRST)));

}
