package dev.ghen.thirst.content.purity;

import com.mojang.logging.LogUtils;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.util.TickHelper;
import dev.ghen.thirst.content.registry.ItemInit;
import dev.ghen.thirst.foundation.util.MathHelper;
import dev.ghen.thirst.foundation.util.ReflectionUtil;
import dev.ghen.thirst.api.ThirstHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import umpaz.brewinandchewin.common.registry.BCItems;
import umpaz.farmersrespite.common.registry.FRBlocks;
import umpaz.farmersrespite.common.registry.FRItems;

import java.lang.reflect.Method;
import java.util.*;


@Mod.EventBusSubscriber
public class WaterPurity
{
    private static List<ContainerWithPurity> waterContainers = new ArrayList<>();
    private static List<Block> fillablesWithPurity = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_PURITY = 0;
    public static final int MAX_PURITY = 3;

    /**
     * Specifies the purity of a block filled with water. Has to be incremented by one
     * number because while using Mixins, generally every block that
     * implements water purity has a mixin-able "createBlockStateDefinition" function,
     * but doesn't have an as-accessible "setDefaultState" function. Thus i am forced to
     * use 0 as the "null" value for the block purity.
     * <br><br>
     * On the bright side, there is a function in this class which takes in a BlockState and
     * returns the already-modified purity
     * */
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 4);

    public static void init()
    {
        registerDispenserBehaviours();
        registerContainers();
        registerFillables();

        if(ModList.get().isLoaded("farmersrespite"))
        {
            registerFarmersRespiteContainers();
            fillablesWithPurity.add(FRBlocks.KETTLE.get());
        }
        if(ModList.get().isLoaded("brewinandchewin"))
            registerBrewinAndChewinContainers();
    }

    private static void registerContainers()
    {
        waterContainers.add(new ContainerWithPurity(new ItemStack(Items.GLASS_BOTTLE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)).setEqualsFilled(itemStack ->
                itemStack.is(Items.POTION) && PotionUtils.getPotion(itemStack) == Potions.WATER));
        waterContainers.add(new ContainerWithPurity(new ItemStack(ItemInit.TERRACOTTA_BOWL.get()),
                new ItemStack(ItemInit.TERRACOTTA_WATER_BOWL.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(Items.BUCKET),
                new ItemStack(Items.WATER_BUCKET), false).canHarvestRunningWater(false));
    }

    private static void registerFarmersRespiteContainers()
    {
        waterContainers.add(new ContainerWithPurity(new ItemStack(FRItems.GREEN_TEA.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(FRItems.YELLOW_TEA.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(FRItems.BLACK_TEA.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(FRItems.ROSE_HIP_TEA.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(FRItems.DANDELION_TEA.get())));
    }

    private static void registerBrewinAndChewinContainers()
    {
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.BEER.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.VODKA.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.MEAD.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.RICE_WINE.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.EGG_GROG.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.STRONGROOT_ALE.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.SACCHARINE_RUM.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.PALE_JANE.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.DREAD_NOG.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.SALTY_FOLLY.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.STEEL_TOE_STOUT.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.GLITTERING_GRENADINE.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.BLOODY_MARY.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.RED_RUM.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.WITHERING_DROSS.get())));
        waterContainers.add(new ContainerWithPurity(new ItemStack(BCItems.KOMBUCHA.get())));
    }

    private static void registerFillables()
    {
        fillablesWithPurity.add(Blocks.CAULDRON);
        fillablesWithPurity.add(Blocks.WATER_CAULDRON);
    }

    /**
     * Registers new custom water container
     */
    public static void addContainer(ContainerWithPurity container)
    {
        waterContainers.add(container);
    }

    /**
     * Sets the purity of the water in a water fillable block (such as the cauldron)
     * after the player adds water to it. If the water purity in the block is greater
     * than that of the water in the item, the second one prevails.
     */
    @SubscribeEvent
    static void fillablesHandler(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getEntity() instanceof ServerPlayer)
        {
            if(!hasPurity(event.getItemStack())) return;

            if(isWaterFilledContainer(event.getItemStack()))
            {
                //Due to Crash happened after click cauldron with water bowl
                if(Objects.requireNonNull(event.getItemStack().serializeNBT().get("id")).getAsString().equals("thirst:terracotta_water_bowl")) return;
                Player player = event.getEntity();
                Level level = player.getLevel();
                BlockPos pos = event.getHitVec().getBlockPos();
                BlockState blockState = level.getBlockState(pos);

                if(isFillableBlock(blockState))
                {


                    int purity = getPurity(event.getItemStack());
                    int blockPurity = !blockState.hasProperty(BLOCK_PURITY) ? MAX_PURITY :
                            (blockState.getValue(BLOCK_PURITY) - 1 < 0 ? MAX_PURITY : blockState.getValue(BLOCK_PURITY) - 1);

                    TickHelper.nextTick(level, () ->
                    {
                        BlockState blockState1 = level.getBlockState(event.getHitVec().getBlockPos());
                        level.setBlock(event.getHitVec().getBlockPos(), blockState1.setValue(BLOCK_PURITY, Math.min(purity, blockPurity) + 1), 0);
                    });
                }
            }
        }
    }

    /**
     * Returns the filled equivalent of the water container given in input.
     * The second parameter specifies if the container inputted is the empty or
     * filled version
     */
    public static ItemStack getFilledContainer(ItemStack container, boolean fromFilled)
    {
        for (ContainerWithPurity waterContainer : waterContainers)
            if ((!fromFilled && waterContainer.equalsEmpty(container)) || (fromFilled && waterContainer.equalsFilled(container)))
                return waterContainer.getFilledItem().copy();

        return ItemStack.EMPTY.copy();
    }

    /**
     * Gives the ability to certain water containers to pick up water from
     * non-source blocks
     */
    @SubscribeEvent
    static void harvestRunningWater(PlayerInteractEvent.RightClickItem event)
    {
        if(event.getEntity() != null)
        {
            ItemStack item = event.getItemStack();
            if(canHarvestRunningWater(item))
            {
                Player player = event.getEntity();
                Level level = player.getLevel();
                BlockPos blockPos = MathHelper.getPlayerPOVHitResult(player.getLevel(), player, ClipContext.Fluid.ANY).getBlockPos();

                if(level.getFluidState(blockPos).is(FluidTags.WATER))
                {
                    SoundEvent sound;
                    ItemStack filledItem;

                    if(item.getItem() == Items.GLASS_BOTTLE &&  !level.getFluidState(blockPos).isSource())
                    {
                        sound = SoundEvents.BOTTLE_FILL;
                        filledItem = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                    }
                    else if(item.getItem() == ItemInit.TERRACOTTA_BOWL.get())
                    {
                        sound = SoundEvents.BUCKET_FILL;
                        filledItem = new ItemStack(ItemInit.TERRACOTTA_WATER_BOWL.get());
                    }
                    else
                        return;

                    level.playSound(player, player.getX(), player.getY(), player.getZ(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);

                    CompoundTag tag = filledItem.getOrCreateTag();
                    tag.putInt("Purity", getBlockPurity(level, blockPos));

                    ItemStack result = ItemUtils.createFilledResult(item, player, filledItem);

                    player.setItemInHand(event.getHand(), result);
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Renders the client-side tooltip for items that have a water
     * purity tag
     */
    @SubscribeEvent
    static void renderPurityTooltip(ItemTooltipEvent event)
    {
        if(isWaterFilledContainer(event.getItemStack()))
        {
            int purity = getPurity(event.getItemStack());
            if(purity >= MIN_PURITY && purity <= MAX_PURITY)
            {
                String purityText = getPurityText(purity);

                int purityColor = getPurityColor(purity);

                event.getToolTip()
                        .add(MutableComponent
                                .create(new LiteralContents(purityText))
                                .setStyle(Style.EMPTY.withColor(purityColor)));
            }
        }
    }

    static boolean isFillableBlock(Block block)
    {
        for (Block fillable : fillablesWithPurity)
            if (fillable == block)
                return true;

        return false;
    }

    static boolean isFillableBlock(BlockState blockState)
    {
        for (Block fillable : fillablesWithPurity)
            if (blockState.is(fillable))
                return true;

        return false;
    }

    public static boolean isWaterFilledContainer(ItemStack item)
    {
        for (ContainerWithPurity waterContainer : waterContainers)
            if (waterContainer.equalsFilled(item))
                return true;

        return false;
    }

    public static boolean isEmptyWaterContainer(ItemStack item)
    {
        for (ContainerWithPurity waterContainer : waterContainers)
            if (waterContainer.equalsEmpty(item))
                return true;

        return false;
    }

    static boolean canHarvestRunningWater(ItemStack item)
    {
        for (ContainerWithPurity waterContainer : waterContainers)
            if (waterContainer.equalsEmpty(item) && waterContainer.canHarvestRunningWater())
                return true;

        return false;
    }

    /**
     * Reads the purity from an item
     */
    public static int getPurity(ItemStack item)
    {
        if(!item.getOrCreateTag().contains("Purity"))
            item.getOrCreateTag().putInt("Purity", -1);

        return item.getTag().getInt("Purity");
    }

    /**
     * Reads the purity from a fluid
     */
    public static int getPurity(FluidStack fluid)
    {
        if(!fluid.getOrCreateTag().contains("Purity"))
            fluid.getOrCreateTag().putInt("Purity", -1);

        return fluid.getTag().getInt("Purity");
    }

    /**
     * Returns the purity string in the language selected by the player
     */
    public static String getPurityText(int purity)
    {
        String purityText = purity == 0 ? "dirty" :
                purity == 1 ? "slightly_dirty" :
                        purity == 2 ? "acceptable" : "purified";

        return MutableComponent.create(new TranslatableContents("thirst.purity." + purityText)).getString();
    }

    /**
     * Returns the purity color in decimal format
     */
    public static int getPurityColor(int purity)
    {
        return purity == 0 ? 11028517 :
                purity == 1 ? 7957617 :
                purity == 2 ? 6128285 : 2208255;
    }

    /**
     * Returns the already-adjusted water purity level of a
     * block with the BLOCK_PURITY tag
     */
    public static int getBlockPurity(BlockState blockState)
    {
        return blockState.hasProperty(BLOCK_PURITY) ? blockState.getValue(BLOCK_PURITY) - 1 : -1;
    }

    public static boolean hasPurity(ItemStack item)
    {
        if(!item.hasTag())
            return false;
        else
            return item.getTag().contains("Purity");
    }

    public static boolean hasPurity(FluidStack fluid)
    {
        if(!fluid.hasTag())
            return false;
        else
            return fluid.getTag().contains("Purity");
    }

    /**
     * Shorthand for adding purity to an item if in a context where the block
     * the player is pointing at is accessible
     */
    public static ItemStack addPurity(ItemStack item, BlockPos pos, Level level)
    {
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt("Purity", getBlockPurity(level, pos));

        return  item;
    }


    /**
     * Adds the "Purity" tag to an item
     */
    public static ItemStack addPurity(ItemStack item, int purity)
    {
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt("Purity", purity);

        return item;
    }

    /**
     * Adds the "Purity" tag to a fluid
     */
    public static FluidStack addPurity(FluidStack fluid, int purity)
    {
        CompoundTag tag = fluid.getOrCreateTag();
        tag.putInt("Purity", purity);

        return fluid;
    }


    /**
     * Calculates the water purity of a specific block in the level
     */
    public static int getBlockPurity(Level level, BlockPos pos)
    {
        int purity = (pos.getY() > CommonConfig.MOUNTAINS_Y.get().intValue() || pos.getY() < CommonConfig.CAVES_Y.get().intValue())
                && pos.getY() < CommonConfig.MOUNTAINS_Y.get().intValue() - 32 ? 1 : 0;

        if(level.getFluidState(pos).is(FluidTags.WATER))
        {
            if(!level.getFluidState(pos).isSource())
                purity = Math.min(purity + CommonConfig.RUNNING_WATER_PURIFICATION_AMOUNT.get().intValue(), MAX_PURITY);

            return purity;
        }
        else if(level.getBlockState(pos).is(Blocks.WATER_CAULDRON))
        {
            return level.getBlockState(pos).getValue(BLOCK_PURITY) - 1;
        }
        else
            return -1;
    }

    /**
     * Gives the player effects based on the purity of the water just drunk
     * and returns whether thirst and quenched should be added or not
     */
    public static boolean givePurityEffects(Player player, ItemStack item)
    {
        if(hasPurity(item))
            return givePurityEffects(player, ThirstHelper.getPurity(item));
        else
            return true;
    }

    /**
     * Calculates purity-derived effects
     */
    public static boolean givePurityEffects(Player player, int purity)
    {
        boolean shouldRegenerate = true;
        Random random = new Random();
        float chance = random.nextFloat();

        switch (purity) {
            case 0 -> {
                if (chance < CommonConfig.DIRTY_NAUSEA_PERCENTAGE.get().intValue()) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));

                }

                if (chance <= CommonConfig.DIRTY_POISON_PERCENTAGE.get().intValue()) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));
                    shouldRegenerate = false;
                }

            }
            case 1 -> {
                if (chance < CommonConfig.SLIGHTLY_DIRTY_NAUSEA_PERCENTAGE.get().intValue() / 100.0f) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));

                }

                if (chance <= CommonConfig.SLIGHTLY_DIRTY_POISON_PERCENTAGE.get().intValue() / 100.0f) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));
                    shouldRegenerate = false;
                }

            }
            case 2 -> {
                if (chance < CommonConfig.ACCEPTABLE_NAUSEA_PERCENTAGE.get().intValue() / 100.0f) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));

                }

                if (chance <= CommonConfig.ACCEPTABLE_POISON_PERCENTAGE.get().intValue() / 100.0f) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));
                    shouldRegenerate = false;
                }

            }
            case 3 -> {
                if (chance < CommonConfig.PURIFIED_NAUSEA_PERCENTAGE.get().intValue() / 100.0f) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));

                }

                if (chance <= CommonConfig.PURIFIED_POISON_PERCENTAGE.get().intValue() / 100.0f) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));
                    shouldRegenerate = false;
                }

            }
        }

        return shouldRegenerate || CommonConfig.QUENCH_THIRST_WHEN_DEBUFFED.get();
    }

    static void registerDispenserBehaviours()
    {
        //fuck you mappings (the default is getDispenseMethod)
        Method getDispenseMethod = ObfuscationReflectionHelper.findMethod(DispenserBlock.class, "m_7216_", ItemStack.class);

        DefaultDispenseItemBehavior bucketDefaultBehaviour = (DefaultDispenseItemBehavior) ReflectionUtil.fuckYouReflections(getDispenseMethod, Blocks.DISPENSER, new ItemStack(Items.BUCKET));
        OptionalDispenseItemBehavior bottleDefaultBehaviour = (OptionalDispenseItemBehavior) ReflectionUtil.fuckYouReflections(getDispenseMethod, Blocks.DISPENSER, new ItemStack(Items.GLASS_BOTTLE.asItem()));

        //fuck you mappings (part 2) (the default is execute)
        Method execute = ObfuscationReflectionHelper.findMethod(DefaultDispenseItemBehavior.class, "m_7498_", BlockSource.class, ItemStack.class);

        DispenserBlock.registerBehavior(Items.BUCKET, (BlockSource block, ItemStack item) ->
        {
            Level level = block.getLevel();
            BlockPos blockpos = block.getPos().relative(block.getBlockState().getValue(DispenserBlock.FACING));

            if(level.getBlockState(blockpos).is(Blocks.WATER) && level.getBlockState(blockpos).getFluidState().isSource())
            {
                ((BucketPickup)level.getBlockState(blockpos).getBlock()).pickupBlock(level, blockpos, level.getBlockState(blockpos));
                ItemStack result = new ItemStack(Items.WATER_BUCKET);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, blockpos);
                addPurity(result, blockpos, level);

                item.shrink(1);
                if (item.isEmpty()) {
                    return result;
                } else
                {
                    if (block.<DispenserBlockEntity>getEntity().addItem(result) < 0)
                    {
                        new DefaultDispenseItemBehavior().dispense(block, result);
                    }

                    return item;
                }
            }
            else
                return (ItemStack) ReflectionUtil.fuckYouReflections(execute, bucketDefaultBehaviour, block, item);

        });

        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE, (BlockSource block, ItemStack item) ->
        {
            Level level = block.getLevel();
            BlockPos blockpos = block.getPos().relative(block.getBlockState().getValue(DispenserBlock.FACING));

            if(level.getFluidState(blockpos).is(FluidTags.WATER))
            {
                ItemStack result = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, blockpos);
                addPurity(result, blockpos, level);

                item.shrink(1);
                if (item.isEmpty()) {
                    return result;
                } else
                {
                    if (block.<DispenserBlockEntity>getEntity().addItem(result) < 0)
                    {
                        new DefaultDispenseItemBehavior().dispense(block, result);
                    }

                    return item;
                }
            }
            else
                return (ItemStack) ReflectionUtil.fuckYouReflections(execute, bottleDefaultBehaviour, block, item);
        });
    }
}
