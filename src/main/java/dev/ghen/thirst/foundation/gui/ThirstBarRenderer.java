package dev.ghen.thirst.foundation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.util.Helper;
import dev.ghen.thirst.foundation.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.ModList;

public class ThirstBarRenderer
{
    public static IThirst PLAYER_THIRST = null;
    public static ResourceLocation THIRST_ICONS = Thirst.asResource("textures/gui/thirst_icons.png");

    public static final ResourceLocation MC_ICONS = new ResourceLocation("textures/gui/icons.png");
    public static Boolean cancelRender = false;
    public static Boolean checkIfPlayerIsVampire = false;
    static Minecraft minecraft = Minecraft.getInstance();
    protected final static RandomSource random = RandomSource.create();
    public static IGuiOverlay THIRST_OVERLAY = (gui, poseStack, partialTicks, screenWidth, screenHeight) ->
    {
        boolean isMounted = gui.getMinecraft().player.getVehicle() instanceof LivingEntity;
        cancelRender =false;
        if (!isMounted && !gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements())
        {
            if(checkIfPlayerIsVampire)
            {
                if(Helper.isVampire(gui.getMinecraft().player))
                {
                    cancelRender =true;
                    return;
                }
            }
            gui.setupOverlayRenderState(true, false);
            render(gui, screenWidth, screenHeight, poseStack);
        }
    };

    public static void registerThirstOverlay(RegisterGuiOverlaysEvent event)
    {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "thirst_level", THIRST_OVERLAY);
    }
    public static void render(ForgeGui gui, int width, int height, GuiGraphics guiGraphics)
    {
        minecraft.getProfiler().push("thirst");
        if (PLAYER_THIRST == null || minecraft.player.tickCount % 40 == 0)
        {
            PLAYER_THIRST = minecraft.player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        }

        Player player = (Player) gui.getMinecraft().getCameraEntity();
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, THIRST_ICONS);
        int left = width / 2 + 91 + ClientConfig.THIRST_BAR_X_OFFSET.get();
        int top = height - gui.rightHeight + ClientConfig.THIRST_BAR_Y_OFFSET.get();
        gui.rightHeight += 10;
        boolean unused = false;// Unused flag in vanilla, seems to be part of a 'fade out' mechanic

        int level = PLAYER_THIRST.getThirst();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            if (PLAYER_THIRST.getQuenched() <= 0.0F && gui.getGuiTicks() % (level * 3 + 1) == 0)
            {
                y = top + (random.nextInt(3) - 1);
            }

            guiGraphics.blit(THIRST_ICONS, x, y, 0, 0, 9, 9, 25, 9);

            if (idx < level)
                guiGraphics.blit(THIRST_ICONS, x, y, 16, 0, 9, 9, 25, 9);
            else if (idx == level)
                guiGraphics.blit(THIRST_ICONS, x, y, 8, 0, 9, 9, 25, 9);
        }
        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, MC_ICONS);

        minecraft.getProfiler().pop();
    }
}