package dev.ghen.thirst;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ghen.thirst.compat.create.CreateRegistry;
import dev.ghen.thirst.foundation.config.ClientConfig;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.gui.ThirstBarRenderer;
import dev.ghen.thirst.foundation.common.capability.IThirstCap;
import com.mojang.logging.LogUtils;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.ItemSettingsConfig;
import dev.ghen.thirst.content.registry.ItemInit;
import dev.ghen.thirst.foundation.gui.appleskin.HUDOverlayHandler;
import dev.ghen.thirst.foundation.gui.appleskin.TooltipOverlayHandler;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.api.ThirstHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(Thirst.ID)
public class Thirst
{
    public static final String ID = "thirst";
    public static final String NAME = "Thirst";
    public static final String VERSION = "1.0.7";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final NonNullSupplier<Registrate> REGISTRATE=NonNullSupplier.lazy(() ->Registrate.create(Thirst.ID));

    public Thirst()
    {

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(ThirstBarRenderer::registerThirstOverlay);

        ItemInit.ITEMS.register(modBus);

        if(ModList.get().isLoaded("create"))
        {
            CreateRegistry.register();
        }
        if(ModList.get().isLoaded("appleskin") && FMLEnvironment.dist.isClient())
        {
            HUDOverlayHandler.init();
            TooltipOverlayHandler.init();
            modBus.addListener(this::onRegisterClientTooltipComponentFactories);
        }

        //configs
        ItemSettingsConfig.setup();
        CommonConfig.setup();
        ClientConfig.setup();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        WaterPurity.init();
        ThirstModPacketHandler.init();

        if(ModList.get().isLoaded("coldsweat"))
            ThirstHelper.shouldUseColdSweatCaps(true);
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(IThirstCap.class);
    }

    //this is from Create but it looked very cool
    public static ResourceLocation asResource(String path)
    {
        return new ResourceLocation(ID, path);
    }
    private void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        TooltipOverlayHandler.register(event);
    }
}
