package dev.ghen.thirst.foundation.gui.appleskin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.ClientConfig;
import dev.ghen.thirst.foundation.gui.ThirstBarRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import squeek.appleskin.ModConfig;
import squeek.appleskin.util.IntPoint;

import java.util.Random;
import java.util.Vector;

@OnlyIn(Dist.CLIENT)
public class HUDOverlayHandler {
    private static float unclampedFlashAlpha = 0.0F;
    private static float flashAlpha = 0.0F;
    private static byte alphaDir = 1;
    protected static int foodIconsOffset;
    public static final Vector<squeek.appleskin.util.IntPoint> foodBarOffsets = new Vector<>();
    private static final Random random = new Random();
    private static final ResourceLocation modIcons;
    static ResourceLocation THIRST_LEVEL_ELEMENT;

    public HUDOverlayHandler() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new HUDOverlayHandler());
    }

    @SubscribeEvent
    public void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == GuiOverlayManager.findOverlay(THIRST_LEVEL_ELEMENT)) {
            Minecraft mc = Minecraft.getInstance();
            ForgeGui gui = (ForgeGui)mc.gui;
            boolean isMounted = mc.player.getVehicle() instanceof LivingEntity;
            boolean isAlive = mc.player.isAlive();
            //stop getExhaustion when player is dead to prevent error log spam
            if (isAlive && ModConfig.SHOW_FOOD_EXHAUSTION_UNDERLAY.get() && !isMounted && !mc.options.hideGui && gui.shouldDrawSurvivalElements() && !ThirstBarRenderer.cancelRender) {
                renderExhaustion(gui, event.getGuiGraphics());
            }
        }

    }

    @SubscribeEvent
    public void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == GuiOverlayManager.findOverlay(THIRST_LEVEL_ELEMENT)) {
            Minecraft mc = Minecraft.getInstance();
            ForgeGui gui = (ForgeGui)mc.gui;
            boolean isMounted = mc.player.getVehicle() instanceof LivingEntity;
            boolean isAlive = mc.player.isAlive();

            if (isAlive && ModConfig.SHOW_SATURATION_OVERLAY.get() && !isMounted && !mc.options.hideGui && gui.shouldDrawSurvivalElements() && !ThirstBarRenderer.cancelRender) {
                renderThirstOverlay(event.getGuiGraphics());
            }
        }

    }

    public static void renderExhaustion(ForgeGui gui, GuiGraphics mStack)
    {
        foodIconsOffset = gui.rightHeight;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;

        int right = mc.getWindow().getGuiScaledWidth() / 2 + 91 + ClientConfig.THIRST_BAR_X_OFFSET.get();
        int top = mc.getWindow().getGuiScaledHeight() - foodIconsOffset + ClientConfig.THIRST_BAR_Y_OFFSET.get();
        float exhaustion = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null).getExhaustion();

        drawExhaustionOverlay(exhaustion, mStack, right, top);
    }

    public static void renderThirstOverlay(GuiGraphics guiGraphics)
    {
        if (!shouldRenderAnyOverlays())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;
        IThirst thirstData = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);

        int top = mc.getWindow().getGuiScaledHeight() - foodIconsOffset + ClientConfig.THIRST_BAR_Y_OFFSET.get();
        int right = mc.getWindow().getGuiScaledWidth() / 2 + 91 + ClientConfig.THIRST_BAR_X_OFFSET.get(); // right of food bar

        generateHungerBarOffsets(top, right, mc.gui.getGuiTicks(), player);

        drawSaturationOverlay(0, thirstData.getQuenched(), guiGraphics , right, top, 1f);

        // try to get the item stack in the player hand
        ItemStack heldItem = player.getMainHandItem();
        if (ModConfig.SHOW_FOOD_VALUES_OVERLAY_WHEN_OFFHAND.get() && !ThirstHelper.itemRestoresThirst(heldItem))
            heldItem = player.getOffhandItem();

        boolean shouldRenderHeldItemValues = !heldItem.isEmpty() && ThirstHelper.itemRestoresThirst(heldItem);
        if (!shouldRenderHeldItemValues)
        {
            resetFlash();
            return;
        }

        ThirstValues thirstValues = new ThirstValues(ThirstHelper.getThirst(heldItem), ThirstHelper.getQuenched(heldItem));
        //FoodValuesEvent foodValuesEvent = new FoodValuesEvent(player, heldItem, FoodHelper.getDefaultFoodValues(heldItem, player), modifiedFoodValues);

        // notify everyone that we should render hunger hud overlay
        /*HUDOverlayEvent.HungerRestored renderRenderEvent = new HUDOverlayEvent.HungerRestored(stats.getFoodLevel(), heldItem, modifiedFoodValues, right, top, poseStack);
        MinecraftForge.EVENT_BUS.post(renderRenderEvent);
        if (renderRenderEvent.isCanceled())
            return;*/

        // calculate the final hunger and saturation
        int drinkThirst = thirstValues.thirst;
        float thirstQuenchedIncrement = thirstValues.getQuenchedIncrement();

        // restored hunger/saturation overlay while holding food
        if(thirstData.getThirst() < 20)
            drawHungerOverlay(drinkThirst, thirstData.getThirst(), guiGraphics, right, top, flashAlpha);
        // Redraw saturation overlay for gained
        if(!ThirstHelper.isFood(heldItem) || player.getFoodData().getFoodLevel() < 20)
            drawSaturationOverlay(thirstValues.quenchedModifier, thirstData.getQuenched(),guiGraphics, right, top, flashAlpha);
    }

    public static void drawSaturationOverlay(float saturationGained, float saturationLevel, GuiGraphics guiGraphics, int right, int top, float alpha)
    {
        if (saturationLevel + saturationGained < 0)
            return;

        enableAlpha(alpha);
        RenderSystem.setShaderTexture(0, modIcons);

        float modifiedSaturation = Math.max(0, Math.min(saturationLevel + saturationGained, 20));

        int startSaturationBar = 0;
        int endSaturationBar = (int) Math.ceil(modifiedSaturation / 2.0F);

        // when require rendering the gained saturation, start should relocation to current saturation tail.
        if (saturationGained != 0)
            startSaturationBar = (int) Math.max(saturationLevel / 2.0F, 0);

        int iconSize = 9;

        for (int i = startSaturationBar; i < endSaturationBar; ++i)
        {
            // gets the offset that needs to be rendered of icon
            IntPoint offset = foodBarOffsets.get(i);
            if (offset == null)
                continue;

            int x = right + offset.x;
            int y = top + offset.y;

            int v = 0;
            int u = 0;

            float effectiveSaturationOfBar = (modifiedSaturation / 2.0F) - i;

            if (effectiveSaturationOfBar >= 1)
                u = 3 * iconSize;
            else if (effectiveSaturationOfBar > .5)
                u = 2 * iconSize;
            else if (effectiveSaturationOfBar > .25)
                u = iconSize;

            guiGraphics.blit(modIcons, x, y, u, v, iconSize, iconSize);
        }

        // rebind default icons
        RenderSystem.setShaderTexture(0, ThirstBarRenderer.MC_ICONS);
        disableAlpha();
    }

    public static void drawHungerOverlay(int hungerRestored, int foodLevel, GuiGraphics guiGraphics, int right, int top, float alpha)
    {
        if (hungerRestored <= 0)
            return;

        enableAlpha(alpha);
        RenderSystem.setShaderTexture(0, ThirstBarRenderer.THIRST_ICONS);

        int modifiedFood = Math.max(0, Math.min(20, foodLevel + hungerRestored));

        int startFoodBars = Math.max(0, foodLevel / 2);
        int endFoodBars = (int) Math.ceil(modifiedFood / 2.0F);

        int iconStartOffset = 8 -3;
        int iconSize = 9;

        for (int i = startFoodBars; i < endFoodBars; ++i)
        {
            // gets the offset that needs to be rendered of icon
            IntPoint offset = foodBarOffsets.get(i);
            if (offset == null)
                continue;

            int x = right + offset.x;
            int y = top + offset.y;

            // location to normal food by default
            int v = 3 * iconSize;
            int u = iconStartOffset + 4 * iconSize;

            // relocation to half food
            if (i * 2 + 1 == modifiedFood)
                u -= iconSize -1;

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

            guiGraphics.blit(ThirstBarRenderer.THIRST_ICONS, x, y, u, v, iconSize, iconSize, 25, 9);
        }

        disableAlpha();
    }

    public static void drawExhaustionOverlay(float exhaustion, GuiGraphics guiGraphics, int right, int top)
    {
        RenderSystem.setShaderTexture(0, modIcons);

        float maxExhaustion = 4.0f;
        // clamp between 0 and 1
        float ratio = Math.min(1, Math.max(0, exhaustion / maxExhaustion));
        int width = (int) (ratio * 81);
        int height = 9;

        enableAlpha(.75f);
        guiGraphics.blit(modIcons, right - width, top, 81 - width, 18, width, height);
        disableAlpha();

        // rebind default icons
        RenderSystem.setShaderTexture(0, ThirstBarRenderer.MC_ICONS);
    }

    public static void enableAlpha(float alpha)
    {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableAlpha()
    {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
            return;

        unclampedFlashAlpha += alphaDir * 0.125f;
        if (unclampedFlashAlpha >= 1.5f)
        {
            alphaDir = -1;
        }
        else if (unclampedFlashAlpha <= -0.5f)
        {
            alphaDir = 1;
        }
        flashAlpha = Math.max(0F, Math.min(1F, unclampedFlashAlpha)) * 0.65f;
    }

    public static void resetFlash()
    {
        unclampedFlashAlpha = flashAlpha = 0f;
        alphaDir = 1;
    }

    private static boolean shouldRenderAnyOverlays()
    {
        return true;
    }

    private static void generateHungerBarOffsets(int top, int right, int ticks, Player player)
    {
        final int preferFoodBars = 10;

        boolean shouldAnimatedFood;

        IThirst thirstData = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);

        // in vanilla saturation level is zero will show hunger animation
        float quenched = thirstData.getQuenched();
        int thirst = thirstData.getThirst();
        shouldAnimatedFood = quenched <= 0.0F && ticks % (thirst * 3 + 1) == 0;

        if (foodBarOffsets.size() != preferFoodBars)
            foodBarOffsets.setSize(preferFoodBars);

        // right alignment, single row
        for (int i = 0; i < preferFoodBars; ++i)
        {
            int x = right - i * 8 - 9;
            int y = top;

            // apply the animated offset
            if (shouldAnimatedFood)
                y += random.nextInt(3) - 1;

            // reuse the point object to reduce memory usage
            IntPoint point = foodBarOffsets.get(i);
            if (point == null)
            {
                point = new IntPoint();
                foodBarOffsets.set(i, point);
            }

            point.x = x - right;
            point.y = y - top;
        }
    }

    static {
        modIcons = Thirst.asResource("textures/gui/appleskin_icons.png");
        THIRST_LEVEL_ELEMENT = Thirst.asResource("thirst_level");
    }
}
