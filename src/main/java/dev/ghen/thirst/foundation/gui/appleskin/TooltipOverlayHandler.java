package dev.ghen.thirst.foundation.gui.appleskin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.foundation.gui.ThirstBarRenderer;
import dev.ghen.thirst.api.ThirstHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.ModConfig;
import squeek.appleskin.helpers.KeyHelper;

@OnlyIn(Dist.CLIENT)
public class TooltipOverlayHandler
{
    /**
     * This is some of the most esoteric garbage code you'll ever see. Read at your own risk
     * also this is adapted from AppleSkin
     * */
    private static ResourceLocation modIcons = new ResourceLocation(Thirst.ID, "textures/gui/appleskin_icons.png");
    public static final int TOOLTIP_REAL_HEIGHT_OFFSET_BOTTOM = 3;
    public static final int TOOLTIP_REAL_HEIGHT_OFFSET_TOP = -3;
    public static final int TOOLTIP_REAL_WIDTH_OFFSET_RIGHT = 3;

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new TooltipOverlayHandler());
        MinecraftForgeClient.registerTooltipComponentFactory(FoodTooltip.class, FoodTooltipRenderer::new);
    }

    private static final TextureOffsets normalBarTextureOffsets = new TextureOffsets();

    static
    {
        normalBarTextureOffsets.containerNegativeHunger = 43;
        normalBarTextureOffsets.containerExtraHunger = 133;
        normalBarTextureOffsets.containerNormalHunger = 5;
        normalBarTextureOffsets.containerPartialHunger = 124;
        normalBarTextureOffsets.containerMissingHunger = 34;
        normalBarTextureOffsets.shankMissingFull = 70;
        normalBarTextureOffsets.shankMissingPartial = normalBarTextureOffsets.shankMissingFull + 9;
        normalBarTextureOffsets.shankFull = 52;
        normalBarTextureOffsets.shankPartial = normalBarTextureOffsets.shankFull + 9;
    }

    private static final TextureOffsets rottenBarTextureOffsets = new TextureOffsets();

    static
    {
        rottenBarTextureOffsets.containerNegativeHunger = normalBarTextureOffsets.containerNegativeHunger;
        rottenBarTextureOffsets.containerExtraHunger = normalBarTextureOffsets.containerExtraHunger;
        rottenBarTextureOffsets.containerNormalHunger = normalBarTextureOffsets.containerNormalHunger;
        rottenBarTextureOffsets.containerPartialHunger = normalBarTextureOffsets.containerPartialHunger;
        rottenBarTextureOffsets.containerMissingHunger = normalBarTextureOffsets.containerMissingHunger;
        rottenBarTextureOffsets.shankMissingFull = 106;
        rottenBarTextureOffsets.shankMissingPartial = rottenBarTextureOffsets.shankMissingFull + 9;
        rottenBarTextureOffsets.shankFull = 88;
        rottenBarTextureOffsets.shankPartial = rottenBarTextureOffsets.shankFull + 9;
    }

    static class TextureOffsets
    {
        int containerNegativeHunger;
        int containerExtraHunger;
        int containerNormalHunger;
        int containerPartialHunger;
        int containerMissingHunger;
        int shankMissingFull;
        int shankMissingPartial;
        int shankFull;
        int shankPartial;
    }

    static class FoodTooltipRenderer implements ClientTooltipComponent
    {
        private FoodTooltip foodTooltip;

        FoodTooltipRenderer(FoodTooltip foodTooltip)
        {
            this.foodTooltip = foodTooltip;
        }

        @Override
        public int getHeight()
        {
            // hunger + spacing + saturation + arbitrary spacing,
            // for some reason 3 extra looks best
            return 9 + 1 + 7 + 3;
        }

        @Override
        public int getWidth(Font font)
        {
            int hungerBarsWidth = foodTooltip.hungerBars * 9;
            if (foodTooltip.hungerBarsText != null)
                hungerBarsWidth += font.width(foodTooltip.hungerBarsText);

            int saturationBarsWidth = foodTooltip.saturationBars * 7;
            if (foodTooltip.saturationBarsText != null)
                saturationBarsWidth += font.width(foodTooltip.saturationBarsText);

            return Math.max(hungerBarsWidth, saturationBarsWidth) + 2; // right padding
        }

        @Override
        public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer_, int zIndex)
        {
            ItemStack itemStack = foodTooltip.itemStack;
            Minecraft mc = Minecraft.getInstance();
            if (!shouldShowTooltip(itemStack, mc.player))
                return;

            Screen gui = mc.screen;
            if (gui == null)
                return;

            ThirstValues thirstValues = foodTooltip.thirstValues;

            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            int offsetX = x;
            int offsetY = y;

            int thirst = thirstValues.thirst;

            // Render from right to left so that the icons 'face' the right way
            offsetX += (foodTooltip.hungerBars - 1) * 9;

            RenderSystem.setShaderTexture(0, ThirstBarRenderer.THIRST_ICONS);
            TextureOffsets offsets = normalBarTextureOffsets;
            for (int i = 0; i < foodTooltip.hungerBars * 2; i += 2)
            {
                if (thirst == i + 1)
                    GuiComponent.blit(poseStack, offsetX, offsetY, zIndex, 8, 0, 9, 9, 25, 9);
                else
                    GuiComponent.blit(poseStack, offsetX, offsetY, zIndex, 16, 0, 9, 9, 25, 9);

                offsetX -= 9;
            }
            if (foodTooltip.hungerBarsText != null)
            {
                offsetX += 18;
                poseStack.pushPose();
                poseStack.translate(offsetX, offsetY, zIndex);
                poseStack.scale(0.75f, 0.75f, 0.75f);
                font.drawShadow(poseStack, foodTooltip.hungerBarsText, 2, 2, 0xFFAAAAAA, false);
                poseStack.popPose();
            }

            offsetX = x;
            offsetY += 10;

            float modifiedSaturationIncrement = thirstValues.quenchedModifier;
            float absModifiedSaturationIncrement = Math.abs(modifiedSaturationIncrement);

            // Render from right to left so that the icons 'face' the right way
            offsetX += (foodTooltip.saturationBars - 1) * 7;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, modIcons);
            for (int i = 0; i < foodTooltip.saturationBars * 2; i += 2)
            {
                float effectiveSaturationOfBar = (absModifiedSaturationIncrement - i) / 2f;

                boolean shouldBeFaded = absModifiedSaturationIncrement <= i;
                if (shouldBeFaded)
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, .5F);

                GuiComponent.blit(poseStack, offsetX, offsetY, zIndex, effectiveSaturationOfBar >= 1 ? 21 : effectiveSaturationOfBar > 0.5 ? 14 : effectiveSaturationOfBar > 0.25 ? 7 : effectiveSaturationOfBar > 0 ? 0 : 28, modifiedSaturationIncrement >= 0 ? 27 : 34, 7, 7, 256, 256);

                if (shouldBeFaded)
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                offsetX -= 7;
            }
            if (foodTooltip.saturationBarsText != null)
            {
                offsetX += 14;
                poseStack.pushPose();
                poseStack.translate(offsetX, offsetY, zIndex);
                poseStack.scale(0.75f, 0.75f, 0.75f);
                font.drawShadow(poseStack, foodTooltip.saturationBarsText, 2, 1, 0xFFAAAAAA, false);
                poseStack.popPose();
            }

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // reset to drawHoveringText state
            RenderSystem.disableDepthTest();
        }
    }

    static class FoodTooltip implements TooltipComponent
    {
        private ThirstValues thirstValues;

        private int hungerBars;
        private String hungerBarsText;

        private int saturationBars;
        private String saturationBarsText;

        private ItemStack itemStack;

        FoodTooltip(ItemStack itemStack, ThirstValues thirstValues, Player player)
        {
            this.itemStack = itemStack;
            this.thirstValues = thirstValues;

            hungerBars = (int) Math.ceil(Math.abs(thirstValues.thirst) / 2f);
            if (hungerBars > 10)
            {
                hungerBarsText = "x" + ((thirstValues.thirst < 0 ? -1 : 1) * hungerBars);
                hungerBars = 1;
            }

            saturationBars = (int) Math.ceil(Math.abs(thirstValues.quenchedModifier) / 2f);
            if (saturationBars > 10 || saturationBars == 0)
            {
                saturationBarsText = "x" + ((thirstValues.quenchedModifier < 0 ? -1 : 1) * saturationBars);
                saturationBars = 1;
            }
        }

        boolean shouldRenderHungerBars()
        {
            return hungerBars > 0;
        }
    }

    @SubscribeEvent
    public void gatherTooltips(RenderTooltipEvent.GatherComponents event)
    {
        if (event.isCanceled())
            return;

        ItemStack hoveredStack = event.getItemStack();
        Minecraft mc = Minecraft.getInstance();
        if (!shouldShowTooltip(hoveredStack, mc.player))
            return;

        ThirstValues thirstValues = new ThirstValues(ThirstHelper.getThirst(hoveredStack), ThirstHelper.getQuenched(hoveredStack));

        FoodTooltip foodTooltip = new FoodTooltip(hoveredStack, thirstValues, mc.player);
        if (foodTooltip.shouldRenderHungerBars())
            event.getTooltipElements().add(Either.right(foodTooltip));
    }

    private static boolean shouldShowTooltip(ItemStack hoveredStack, Player player)
    {
        if (hoveredStack.isEmpty())
            return false;

        boolean shouldShowTooltip = (ModConfig.SHOW_FOOD_VALUES_IN_TOOLTIP.get() && KeyHelper.isShiftKeyDown()) || ModConfig.ALWAYS_SHOW_FOOD_VALUES_TOOLTIP.get();
        if (!shouldShowTooltip)
            return false;

        return ThirstHelper.itemRestoresThirst(hoveredStack);
    }
}
