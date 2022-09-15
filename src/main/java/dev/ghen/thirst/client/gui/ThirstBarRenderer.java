package dev.ghen.thirst.client.gui;

import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.common.capability.IThirstCap;
import dev.ghen.thirst.common.capability.ModCapabilities;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.ModList;

@OnlyIn(Dist.CLIENT)
public class ThirstBarRenderer
{
    public static IThirstCap PLAYER_THIRST = null;
    public static ResourceLocation THIRST_ICONS = Thirst.asResource("textures/gui/thirst_icons.png");
    static Minecraft minecraft = Minecraft.getInstance();

    public static final IIngameOverlay THIRST_OVERLAY = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FOOD_LEVEL_ELEMENT, "Thirst Level", (gui, poseStack, partialTicks, screenWidth, screenHeight) ->
    {
        boolean isMounted = minecraft.player.getVehicle() instanceof LivingEntity;
        if (!isMounted && !minecraft.options.hideGui && gui.shouldDrawSurvivalElements())
        {
            gui.setupOverlayRenderState(true, false);
            render(gui, screenWidth, screenHeight, poseStack);
        }
    });

    public static void register()
    {

    }

    public static void render(ForgeIngameGui gui, int screenWidth, int screenHeight, PoseStack poseStack)
    {
        minecraft.getProfiler().push("thirst");
        if (PLAYER_THIRST == null || minecraft.player.tickCount % 40 == 0)
        {
            PLAYER_THIRST = minecraft.player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, THIRST_ICONS);

        int left = screenWidth / 2 + 91;
        int top = screenHeight - gui.right_height;
        gui.right_height += 10;

        int level = PLAYER_THIRST.getThirst();

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            gui.blit(poseStack, x, y, 0, 0, 9, 9, 25, 9);

            if (idx < level)
                gui.blit(poseStack, x, y, 16, 0, 9, 9, 25, 9);
            else if (idx == level)
                gui.blit(poseStack, x, y, 8, 0, 9, 9, 25, 9);
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

        minecraft.getProfiler().pop();
    }
}
