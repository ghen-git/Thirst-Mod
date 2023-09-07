package dev.ghen.thirst.foundation.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class DrinkableItem extends Item
{
    private int drinkDuration = 32;
    private Item container;

    public DrinkableItem()
    {
        super(new Properties().stacksTo(64));
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

    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack item, @NotNull Level level, @NotNull LivingEntity entity)
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

        level.gameEvent(entity, GameEvent.ITEM_INTERACT_FINISH, entity.getEyePosition());
        return item;
    }

    public int getUseDuration(@NotNull ItemStack p_43001_) {
        return drinkDuration;
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack p_42997_) {
        return UseAnim.DRINK;
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level p_42993_, @NotNull Player p_42994_, @NotNull InteractionHand p_42995_)
    {
        return ItemUtils.startUsingInstantly(p_42993_, p_42994_, p_42995_);
    }
}
