package dev.ghen.thirst.foundation.common.loot;


import dev.ghen.thirst.Thirst;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS;
    public static final RegistryObject<GlobalLootModifierSerializer<?>> ADD_LOOT_TABLE;

    public ModLootModifiers() {
    }

    static {
        LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, Thirst.ID);
        ADD_LOOT_TABLE = LOOT_MODIFIERS.register("add_loot_table", AddLootTableModifier.Serializer::new);
    }
}
