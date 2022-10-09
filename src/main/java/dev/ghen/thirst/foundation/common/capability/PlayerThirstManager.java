package dev.ghen.thirst.foundation.common.capability;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.content.thirst.ThirstHelper;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.foundation.network.message.DrinkByHandMessage;
import dev.ghen.thirst.foundation.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class PlayerThirstManager
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            IThirstCap playerThirstCap = new PlayerThirstCap();
            LazyOptional<IThirstCap> capOptional = LazyOptional.of(() -> playerThirstCap);
            Capability<IThirstCap> capability = ModCapabilities.PLAYER_THIRST;

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
        if(CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level.isClientSide)
        {
            Minecraft mc = Minecraft.getInstance();

            Player player = mc.player;
            Level level = mc.level;
            BlockPos blockPos = MathHelper.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY).getBlockPos();

            if ((player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) &&
                    level.getFluidState(blockPos).is(FluidTags.WATER) && player.isCrouching()) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                ThirstModPacketHandler.INSTANCE.sendToServer(new DrinkByHandMessage(blockPos));
            }
        }
    }

    @SubscribeEvent
    public static void drinkByHand(PlayerInteractEvent.RightClickEmpty event)
    {
        if(CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().level.isClientSide)
        {
            Minecraft mc = Minecraft.getInstance();

            Player player = mc.player;
            Level level = mc.level;
            BlockPos blockPos = MathHelper.getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY).getBlockPos();

            if ((player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) &&
                    level.getFluidState(blockPos).is(FluidTags.WATER) && player.isCrouching()) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);
                ThirstModPacketHandler.INSTANCE.sendToServer(new DrinkByHandMessage(blockPos));
            }
        }
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
    public static void onPlayerJump(LivingEvent.LivingJumpEvent  event)
    {
        if(event.getEntity() instanceof ServerPlayer)
        {
            event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
            {
                Player player = (Player) event.getEntity();
                cap.addExhaustion(player, 0.05f + (player.isSprinting() ? 0.175f : 0f));
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && event.player instanceof ServerPlayer)
        {
            event.player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap -> cap.tick(event.player));
        }
    }

    @SubscribeEvent
    public static void onPlayerBreak(LivingDestroyBlockEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer)
        {
            event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
            {
                Player player = (Player) event.getEntity();
                cap.addExhaustion(player, 0.005f);
            });
        }
    }

    /**
     * Adds the thirst capability to the player if they returned from the end
     * without dying.
     */
    @SubscribeEvent
    public static void endFix(PlayerEvent.Clone event)
    {
        if (!event.isWasDeath() && !event.getPlayer().level.isClientSide)
        {
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();

            event.getPlayer().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
            {
                oldPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap::copy);
            });

            oldPlayer.invalidateCaps();
        }
    }
}
