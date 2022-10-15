package dev.ghen.thirst.foundation.network.message;

import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerThirstSyncMessage
{
    public int thirst;
    public int quenched;
    public float exhaustion;

    public PlayerThirstSyncMessage(int thirst, int quenched, float exhaustion)
    {
        this.thirst = thirst;
        this.quenched = quenched;
        this.exhaustion = exhaustion;
    }

    public static void encode(PlayerThirstSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.thirst);
        buffer.writeInt(message.quenched);
        buffer.writeFloat(message.exhaustion);
    }

    public static PlayerThirstSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerThirstSyncMessage(buffer.readInt(), buffer.readInt(), buffer.readFloat());
    }

    public static void handle(PlayerThirstSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientThirstSyncMessage.handlePacket(message, contextSupplier)));
        }

        context.setPacketHandled(true);
    }
}

@OnlyIn(Dist.CLIENT)
class ClientThirstSyncMessage
{
    public static void handlePacket(PlayerThirstSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        Player player = Minecraft.getInstance().player;

        if (player != null)
        {
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap ->
            {
                cap.setThirst(message.thirst);
                cap.setQuenched(message.quenched);
                cap.setExhaustion(message.exhaustion);
            });
        }
    }
}