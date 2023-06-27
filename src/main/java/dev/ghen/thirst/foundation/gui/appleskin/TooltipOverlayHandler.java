package dev.ghen.thirst.foundation.gui.appleskin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.gui.ThirstBarRenderer;
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
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.ModConfig;
import squeek.appleskin.api.food.FoodValues;
import squeek.appleskin.helpers.KeyHelper;

@OnlyIn(Dist.CLIENT)
public class TooltipOverlayHandler {
    private static ResourceLocation modIcons;
    public static final int TOOLTIP_REAL_HEIGHT_OFFSET_BOTTOM = 3;
    public static final int TOOLTIP_REAL_HEIGHT_OFFSET_TOP = -3;
    public static final int TOOLTIP_REAL_WIDTH_OFFSET_RIGHT = 3;
    private static final TextureOffsets normalBarTextureOffsets;
    private static final TextureOffsets rottenBarTextureOffsets;

    public TooltipOverlayHandler() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new TooltipOverlayHandler());
    }

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(FoodTooltip.class, FoodTooltipRenderer::new);
    }

    @SubscribeEvent
    public void gatherTooltips(RenderTooltipEvent.GatherComponents event) {
        if (!event.isCanceled()) {
            ItemStack hoveredStack = event.getItemStack();
            Minecraft mc = Minecraft.getInstance();
            if (shouldShowTooltip(hoveredStack, mc.player)) {

                FoodTooltip foodTooltip = new FoodTooltip(hoveredStack, mc.player);
                if (foodTooltip.shouldRenderHungerBars()) {
                    event.getTooltipElements().add(Either.right(foodTooltip));
                }
            }
        }
    }

    private static boolean shouldShowTooltip(ItemStack hoveredStack, Player player) {
        if (hoveredStack.isEmpty()) {
            return false;
        } else {
            boolean shouldShowTooltip = (Boolean)ModConfig.SHOW_FOOD_VALUES_IN_TOOLTIP.get() && KeyHelper.isShiftKeyDown() || (Boolean)ModConfig.ALWAYS_SHOW_FOOD_VALUES_TOOLTIP.get();
            if (!shouldShowTooltip) {
                return false;
            } else {
                return ThirstHelper.itemRestoresThirst(hoveredStack);
            }
        }
    }

    static {
        modIcons = Thirst.asResource("textures/gui/appleskin_icons.png");
        normalBarTextureOffsets = new TextureOffsets();
        normalBarTextureOffsets.containerNegativeHunger = 43;
        normalBarTextureOffsets.containerExtraHunger = 133;
        normalBarTextureOffsets.containerNormalHunger = 16;
        normalBarTextureOffsets.containerPartialHunger = 124;
        normalBarTextureOffsets.containerMissingHunger = 34;
        normalBarTextureOffsets.shankMissingFull = 70;
        normalBarTextureOffsets.shankMissingPartial = normalBarTextureOffsets.shankMissingFull + 9;
        normalBarTextureOffsets.shankFull = 52;
        normalBarTextureOffsets.shankPartial = normalBarTextureOffsets.shankFull + 9;
        rottenBarTextureOffsets = new TextureOffsets();
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

    static class FoodTooltip implements TooltipComponent {
        private FoodValues defaultFood;
        private FoodValues modifiedFood;
        private int biggestHunger;
        private float biggestSaturationIncrement;
        private int hungerBars;
        private String hungerBarsText;
        private int saturationBars;
        private String saturationBarsText;
        private ItemStack itemStack;

        FoodTooltip(ItemStack itemStack, Player player) {
            this.itemStack = itemStack;
            this.biggestHunger = ThirstHelper.getThirst(itemStack);
            this.biggestSaturationIncrement = ThirstHelper.getQuenched(itemStack);
            this.hungerBars = (int)Math.ceil((float)Math.abs(this.biggestHunger) / 2.0F);
            if (this.hungerBars > 10) {
                this.hungerBarsText = "x" + (this.biggestHunger < 0 ? -1 : 1) * this.hungerBars;
                this.hungerBars = 1;
            }

            this.saturationBars = (int)Math.ceil(Math.abs(this.biggestSaturationIncrement) / 2.0F);
            if (this.saturationBars > 10 || this.saturationBars == 0) {
                this.saturationBarsText = "x" + (this.biggestSaturationIncrement < 0.0F ? -1 : 1) * this.saturationBars;
                this.saturationBars = 1;
            }

        }

        boolean shouldRenderHungerBars() {
            return this.hungerBars > 0;
        }
    }

    static class TextureOffsets {
        int containerNegativeHunger;
        int containerExtraHunger;
        int containerNormalHunger;
        int containerPartialHunger;
        int containerMissingHunger;
        int shankMissingFull;
        int shankMissingPartial;
        int shankFull;
        int shankPartial;

        TextureOffsets() {
        }
    }

    static class FoodTooltipRenderer implements ClientTooltipComponent {
        private FoodTooltip foodTooltip;

        FoodTooltipRenderer(FoodTooltip foodTooltip) {
            this.foodTooltip = foodTooltip;
        }

        public int getHeight() {
            return 20;
        }

        public int getWidth(Font font) {
            int hungerBarsWidth = this.foodTooltip.hungerBars * 9;
            if (this.foodTooltip.hungerBarsText != null) {
                hungerBarsWidth += font.width(this.foodTooltip.hungerBarsText);
            }

            int saturationBarsWidth = this.foodTooltip.saturationBars * 7;
            if (this.foodTooltip.saturationBarsText != null) {
                saturationBarsWidth += font.width(this.foodTooltip.saturationBarsText);
            }

            return Math.max(hungerBarsWidth, saturationBarsWidth) + 2;
        }

        public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer_, int zIndex) {
            ItemStack itemStack = foodTooltip.itemStack;
            Minecraft mc = Minecraft.getInstance();
            if (!shouldShowTooltip(itemStack, mc.player))
                return;

            Screen gui = mc.screen;
            if (gui == null)
                return;

            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            int offsetX = x;
            int offsetY = y;

            int thirst = ThirstHelper.getThirst(itemStack);

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

            float modifiedSaturationIncrement = ThirstHelper.getQuenched(itemStack);
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
}
