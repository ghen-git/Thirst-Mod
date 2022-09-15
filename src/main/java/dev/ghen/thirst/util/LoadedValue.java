package dev.ghen.thirst.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

public class LoadedValue<T>
{
    T value;
    Supplier<T> valueCreator;

    public LoadedValue(Supplier<T> valueCreator)
    {
        this.valueCreator = valueCreator;
        this.value = valueCreator.get();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static <V> LoadedValue<V> of(Supplier<V> valueCreator)
    {
        return new LoadedValue<>(valueCreator);
    }

    @SubscribeEvent
    public void onLoaded(ServerStartedEvent event)
    {
        this.value = valueCreator.get();
    }

    public T get()
    {
        return value;
    }
}
