package dev.ghen.thirst.content.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import net.minecraft.world.item.Item;

import static dev.ghen.thirst.Thirst.REGISTRATE;


public class ThirstItem
{
    public static void register(){}

    public static final ItemEntry<Item>
            CLAY_BOWL= REGISTRATE.get().item("clay_bowl", Item::new)
                    .properties(p -> p.stacksTo(16))
                    .register(),
            TERRACOTTA_BOWL=REGISTRATE.get().item("terracotta_bowl", Item::new)
                    .properties(p -> p.stacksTo(16))
                    .register();
    public static final ItemEntry<DrinkableItem>
            TERRACOTTA_WATER_BOWL=REGISTRATE.get().item("terracotta_water_bowl", p->new DrinkableItem().setContainer(TERRACOTTA_BOWL.get()))
                    .register();
}
