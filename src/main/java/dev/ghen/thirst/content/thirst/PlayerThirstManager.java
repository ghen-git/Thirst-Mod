package dev.ghen.thirst.content.thirst;

import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.content.purity.WaterPurity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class PlayerThirstManager
{

    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            IThirst playerThirstCap = new PlayerThirst();
            LazyOptional<IThirst> capOptional = LazyOptional.of(() -> playerThirstCap);
            Capability<IThirst> capability = ModCapabilities.PLAYER_THIRST;

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                {
                    if (cap == capability)
                    {
                        return capOptional.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {
                    return playerThirstCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {
                    playerThirstCap.deserializeNBT(nbt);
                }
            };

            event.addCapability(Thirst.asResource("thirst"), provider);
        }
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickBlock event)
    {
        if(CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level().isClientSide)
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DrinkByHandClient::drinkByHand);
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickEmpty event)
    {
        if(CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level().isClientSide)
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DrinkByHandClient::drinkByHand);
    }

    @SubscribeEvent
    public static void drink(LivingEntityUseItemEvent.Finish event)
    {
        if(event.getEntity() instanceof Player && ThirstHelper.itemRestoresThirst(event.getItem()))
        {
            event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
            {
                ItemStack item = event.getItem();
                if(WaterPurity.givePurityEffects((Player) event.getEntity(), item))
                    cap.drink((Player) event.getEntity(), ThirstHelper.getThirst(item), ThirstHelper.getQuenched(item));
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayer serverPlayer)
        {
            serverPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap -> cap.tick(serverPlayer));
        }
    }

    /**
     * Adds the thirst capability to the player if they returned from the end
     * without dying.
     */
    @SubscribeEvent
    public static void endFix(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath() && !event.getEntity().level().isClientSide)
        {
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
                    oldPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap::copy));

            oldPlayer.invalidateCaps();
        }
    }
}