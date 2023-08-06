package dev.ghen.thirst;

import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.compat.create.CreateRegistry;
import dev.ghen.thirst.compat.create.ponder.ThirstPonders;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.content.registry.ItemInit;
import dev.ghen.thirst.foundation.common.capability.IThirstCap;
import dev.ghen.thirst.foundation.common.loot.ModLootModifiers;
import dev.ghen.thirst.foundation.config.ClientConfig;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.config.ItemSettingsConfig;
import dev.ghen.thirst.foundation.config.KeyWordConfig;
import dev.ghen.thirst.foundation.gui.ThirstBarRenderer;
import dev.ghen.thirst.foundation.gui.appleskin.HUDOverlayHandler;
import dev.ghen.thirst.foundation.gui.appleskin.TooltipOverlayHandler;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
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

@Mod(Thirst.ID)
public class Thirst
{
    public static final String ID = "thirst";

    public Thirst()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(ThirstBarRenderer::registerThirstOverlay);

        ItemInit.ITEMS.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);

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
        KeyWordConfig.setup();
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
        if(ModList.get().isLoaded("create")){
            event.enqueueWork(ThirstPonders::register);
        }
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
