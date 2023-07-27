package dev.ghen.thirst.foundation.common.damagesource;

import dev.ghen.thirst.Thirst;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDamageSource
{

    public static final ResourceKey<DamageType> DIE_OF_THIRST_KEY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("thirst", "dehydrate".toLowerCase(Locale.ROOT)));

    public static DamageSource getDamageSource(Level level, ResourceKey<DamageType> type) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), null, null);
    }

}
