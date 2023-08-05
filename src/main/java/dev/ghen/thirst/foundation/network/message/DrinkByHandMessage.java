package dev.ghen.thirst.foundation.network.message;

import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DrinkByHandMessage
{
    public BlockPos pos;

    public DrinkByHandMessage(BlockPos pos)
    {
        this.pos = pos;
    }

    public static void encode(DrinkByHandMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.pos);
    }

    public static DrinkByHandMessage decode(FriendlyByteBuf buffer)
    {
        return new DrinkByHandMessage(buffer.readBlockPos());
    }

    public static void handle(DrinkByHandMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isServer())
        {
            context.enqueueWork(() ->
            {
                Player player = context.getSender();
                Level level = player.level();

                player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
                {
                    int purity = WaterPurity.getBlockPurity(level, message.pos);
                    level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    if(WaterPurity.givePurityEffects(player, purity))
                        cap.drink(player, CommonConfig.HAND_DRINKING_HYDRATION.get().intValue(), CommonConfig.HAND_DRINKING_QUENCHED.get().intValue());
                });
            });
        }

        context.setPacketHandled(true);
    }
}
