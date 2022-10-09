package dev.ghen.thirst.foundation.common.item;

import dev.ghen.thirst.foundation.tab.ThirstTab;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class DrinkableItem extends Item
{
    private int drinkDuration = 32;
    private Item container;

    public DrinkableItem()
    {
        super(new Properties().stacksTo(64).tab(ThirstTab.THIRST_TAB));
    }

    public DrinkableItem(Properties p_41383_)
    {
        super(p_41383_);
    }

    public DrinkableItem setContainer(Item item)
    {
        this.container = item;
        return this;
    }

    public DrinkableItem setDrinkDuration(int duration)
    {
        this.drinkDuration = duration;
        return this;
    }

    public ItemStack finishUsingItem(ItemStack item, Level level, LivingEntity entity)
    {
        Player player = entity instanceof Player ? (Player)entity : null;

        if (player instanceof ServerPlayer)
        {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, item);
        }

        if (player != null)
        {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild)
            {
                item.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild)
        {
            if (item.isEmpty())
            {
                return new ItemStack(container);
            }

            if (player != null)
            {
                player.getInventory().add(new ItemStack(container));
            }
        }

        level.gameEvent(entity, GameEvent.DRINKING_FINISH, entity.eyeBlockPosition());
        return item;
    }

    public int getUseDuration(ItemStack p_43001_) {
        return drinkDuration;
    }

    public UseAnim getUseAnimation(ItemStack p_42997_) {
        return UseAnim.DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level p_42993_, Player p_42994_, InteractionHand p_42995_)
    {
        return ItemUtils.startUsingInstantly(p_42993_, p_42994_, p_42995_);
    }
}
