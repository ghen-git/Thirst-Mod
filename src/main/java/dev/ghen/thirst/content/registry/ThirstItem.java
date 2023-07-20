package dev.ghen.thirst.content.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import net.minecraft.world.item.Item;
import dev.ghen.thirst.foundation.tab.ThirstTab;


public class ThirstItem
{

    static
    {
        Thirst.REGISTRATE.get().creativeModeTab(() -> ThirstTab.THIRST_TAB);
    }

    public static void register(){}

    public static final ItemEntry<Item>
            CLAY_BOWL= Thirst.REGISTRATE.get().item("clay_bowl", Item::new)
                    .properties(p -> p.stacksTo(16))
                    .register(),
            TERRACOTTA_BOWL= Thirst.REGISTRATE.get().item("terracotta_bowl", Item::new)
                    .properties(p -> p.stacksTo(16))
                    .register();
    public static final ItemEntry<DrinkableItem>
            TERRACOTTA_WATER_BOWL= Thirst.REGISTRATE.get().item("terracotta_water_bowl", p->new DrinkableItem().setContainer(TERRACOTTA_BOWL.get()))
                    .register();
}
