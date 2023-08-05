package dev.ghen.thirst.foundation.common.loot;


import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS;
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_LOOT_TABLE;

    public ModLootModifiers() {
    }

    static {
        LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "thirst");
        ADD_LOOT_TABLE = LOOT_MODIFIERS.register("add_loot_table", AddLootTableModifier.CODEC);
    }
}
