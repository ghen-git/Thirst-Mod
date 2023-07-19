package dev.ghen.thirst.foundation.util;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class TickHelper
{
    /**
     * Util for running actions on the server delayed by n ticks
     * may not be the best implementation, i'm a dumb idiot.
     * */
    private static final Map<Integer, List<Runnable>> tickTasks = new HashMap<>();
    private static int tickTimerFsr = 0;

    public static void addTask(int tick, Runnable task)
    {
        if(!tickTasks.containsKey(tick))
            tickTasks.put(tick, new ArrayList<>());

        tickTasks.get(tick).add(task);
    }

    public static void nextTick(Level level, Runnable task)
    {
        addTask(Objects.requireNonNull(level.getServer()).getTickCount() + 1, task);
    }

    public static void TickLater(Level level, int tickNumber,Runnable task)
    {
        addTask(Objects.requireNonNull(level.getServer()).getTickCount() + tickNumber, task);
    }

    @SubscribeEvent
    static void runTasks(TickEvent.LevelTickEvent event)
    {
        if(event.level instanceof ServerLevel && tickTimerFsr == 0 && tickTasks.containsKey(event.level.getServer().getTickCount()))
        {
            tickTasks.get(event.level.getServer().getTickCount()).forEach(Runnable::run);
            tickTasks.remove(event.level.getServer().getTickCount());

            tickTimerFsr += 3;
        }
        else if(tickTimerFsr > 0)
            tickTimerFsr--;
    }
}
