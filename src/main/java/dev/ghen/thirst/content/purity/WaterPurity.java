package dev.ghen.thirst.content.purity;

import com.mojang.logging.LogUtils;
import dev.ghen.thirst.foundation.util.TickHelper;
import dev.ghen.thirst.content.ItemInit;
import dev.ghen.thirst.foundation.util.MathHelper;
import dev.ghen.thirst.foundation.util.ReflectionUtil;
import dev.ghen.thirst.content.thirst.ThirstHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

@Mod.EventBusSubscriber
public class WaterPurity
{
    private static List<ContainerWithPurity> waterContainers = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_PURITY = 0;
    public static final int MAX_PURITY = 3;
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 3);

    public static void init()
    {
        registerDispenserBehaviours();
        registerContainers();
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

    /**
     * Registers new custom water container
     */
    public static void addContainer(ContainerWithPurity container)
    {
        waterContainers.add(container);
    }

    /**
     * Sets the purity of the water in a cauldron after the player adds water
     * to it. If the water purity in the cauldron is greater than that of the water
     * in the item, the second one prevails.
     */
    @SubscribeEvent
    static void cauldronHandler(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getEntity() instanceof ServerPlayer)
        {
            if(isWaterFilledContainer(event.getItemStack()))
            {
                Player player = (Player) event.getEntity();
                Level level = player.getLevel();
                BlockPos pos = event.getHitVec().getBlockPos();
                BlockState blockState = level.getBlockState(pos);

                if(blockState.is(Blocks.CAULDRON) || blockState.is(Blocks.WATER_CAULDRON))
                {
                    int purity = getPurity(event.getItemStack());
                    int cauldronPurity = blockState.is(Blocks.WATER_CAULDRON) ? blockState.getValue(BLOCK_PURITY) : 0;

                    TickHelper.nextTick(level, () ->
                    {
                        BlockState cauldron = level.getBlockState(event.getHitVec().getBlockPos());

                        if(blockState.is(Blocks.CAULDRON) || purity < cauldronPurity)
                            level.setBlock(event.getHitVec().getBlockPos(), cauldron.setValue(BLOCK_PURITY, purity), 0);
                        else
                            level.setBlock(event.getHitVec().getBlockPos(), cauldron.setValue(BLOCK_PURITY, cauldronPurity), 0);
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
        if(event.getEntity() instanceof Player)
        {
            ItemStack item = event.getItemStack();
            if(canHarvestRunningWater(item))
            {
                Player player = (Player) event.getEntity();
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
                    tag.putInt("Purity", getWaterPurity(level, blockPos));

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
                String purityText = purity == 0 ? "Dirty" :
                        purity == 1 ? "Slightly Dirty" :
                                purity == 2 ? "Acceptable" : "Purified";

                int purityColor = purity == 0 ? 11028517 :
                        purity == 1 ? 7957617:
                                purity == 2 ? 6128285 : 2208255;

                event.getToolTip().add(new TextComponent(purityText).setStyle(Style.EMPTY.withColor(purityColor)));
            }
        }
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

    public static boolean hasPurity(ItemStack item)
    {
        if(!item.hasTag())
            return false;
        else
            return item.getTag().contains("Purity");
    }

    /**
     * Shorthand for adding purity to an item if in a context where the block
     * the player is pointing at is accessible
     */
    public static ItemStack addPurity(ItemStack item, BlockPos pos, Level level)
    {
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt("Purity", getWaterPurity(level, pos));

        return  item;
    }


    /**
     * Adds the "Purity" tag to an item
     */
    public static void addPurity(ItemStack item, int purity)
    {
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt("Purity", purity);
    }


    /**
     * Calculates the water purity of a specific block in the level
     */
    public static int getWaterPurity(Level level, BlockPos pos)
    {
        int purity = (pos.getY() > 100 || pos.getY() < 48)
                && (!isBiomeWaterSalty(level.getBiome(pos).value()) || pos.getY() < 16) ? 1 : 0;

        if(level.getFluidState(pos).is(FluidTags.WATER))
        {
            if(!level.getFluidState(pos).isSource())
                purity += 1;

            return purity;
        }
        else if(level.getBlockState(pos).is(Blocks.WATER_CAULDRON))
        {
            return level.getBlockState(pos).getValue(BLOCK_PURITY);
        }
        else
            return -1;
    }

    public static boolean isBiomeWaterSalty(Biome biome)
    {
        return biome.getRegistryName().toString().contains("ocean");
    }

    /**
     * Gives the player effects based on the purity of the water just drank
     * and returns whether thirst and quenched should be added or not
     */
    public static boolean givePurityEffects(Player player, ItemStack item)
    {
        if(hasPurity(item))
        {
            boolean shouldRegenerate = true;
            Random random = new Random();

            switch(ThirstHelper.getPurity(item))
            {
                case 0:
                {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 10, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));

                    if(random.nextFloat() <= 0.3)
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));

                    shouldRegenerate = false;

                    break;
                }
                case 1:
                {
                    float chance = random.nextFloat();

                    if(chance <= 0.5)
                    {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));

                        if(chance <= 0.1)
                            player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));

                        shouldRegenerate = false;
                    }
                    break;
                }
                case 2:
                {
                    if(random.nextFloat() <= 0.1)
                    {
                        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));
                        shouldRegenerate = false;
                    }
                    break;
                }
            }

            return shouldRegenerate;
        }
        else
            return true;
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
                level.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blockpos);
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
                level.gameEvent((Entity)null, GameEvent.FLUID_PICKUP, blockpos);
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
