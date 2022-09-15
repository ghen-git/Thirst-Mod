package dev.ghen.thirst;

import dev.ghen.thirst.client.gui.ThirstBarRenderer;
import dev.ghen.thirst.client.gui.appleskin.HUDOverlayHandler;
import dev.ghen.thirst.client.gui.appleskin.TooltipOverlayHandler;
import dev.ghen.thirst.common.capability.IThirstCap;
import com.mojang.logging.LogUtils;
import dev.ghen.thirst.common.event.WaterPurity;
import dev.ghen.thirst.config.ItemSettingsConfig;
import dev.ghen.thirst.init.ItemInit;
import dev.ghen.thirst.network.ThirstModPacketHandler;
import net.minecraft.core.BlockSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

@Mod(Thirst.ID)
public class Thirst
{
    public static final String ID = "thirst";
    public static final String NAME = "Thirst";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Thirst()
    {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        IEventBus modBus = FMLJavaModLoadingContext.get()
                .getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerCapabilities);

        ItemInit.ITEMS.register(modBus);

        //configs
        ItemSettingsConfig.setup();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        WaterPurity.init();
        ThirstModPacketHandler.init();
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        if(ModList.get().isLoaded("appleskin"))
        {
            HUDOverlayHandler.init();
            TooltipOverlayHandler.init();
        }

        ThirstBarRenderer.register();
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(IThirstCap.class);
    }

    public static ResourceLocation asResource(String path)
    {
        return new ResourceLocation(ID, path);
    }
}
