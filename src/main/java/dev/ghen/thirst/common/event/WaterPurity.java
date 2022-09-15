package dev.ghen.thirst.common.event;

import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.INameMappingService;
import dev.ghen.thirst.init.ItemInit;
import dev.ghen.thirst.util.MathHelper;
import dev.ghen.thirst.util.ReflectionUtil;
import dev.ghen.thirst.util.ThirstHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.core.jmx.Server;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Random;

@Mod.EventBusSubscriber
public class WaterPurity
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 3);

    public static void init()
    {
        registerDispenserBehaviours();
    }

    @SubscribeEvent
    static void pourWater(PlayerInteractEvent.RightClickBlock event)
    {
        if(event.getEntity() instanceof ServerPlayer && isWaterContainer(event.getItemStack()))
        {
            Player player = (Player) event.getEntity();
            ServerLevel level = (ServerLevel) player.getLevel();

            if(level.getBlockState(event.getHitVec().getBlockPos()).is(Blocks.WATER_CAULDRON))
            {
                int purity = event.getItemStack().getTag().getInt("Purity");

                LOGGER.info(level.getServer().getTickCount() + "s");
                level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1000, () -> {
                    LOGGER.info(level.getServer().getTickCount() + "");
                    BlockState cauldron = level.getBlockState(event.getHitVec().getBlockPos());
                    int waterLevel = cauldron.getValue(LayeredCauldronBlock.LEVEL);

                    if(waterLevel < 3)
                        cauldron.setValue(BLOCK_PURITY, purity);
                }));
            }
        }
    }

    @SubscribeEvent
    static void harvestRunningWater(PlayerInteractEvent.RightClickItem event)
    {
        if(event.getEntity() instanceof Player)
        {
            ItemStack item = event.getItemStack();
            if(item.getItem() == Items.GLASS_BOTTLE || item.getItem() == Items.BOWL)
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
                    else if(item.getItem() == Items.BOWL)
                    {
                        sound = SoundEvents.BUCKET_FILL;
                        filledItem = new ItemStack(ItemInit.WATER_BOWL.get());
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

    @SubscribeEvent
    static void renderPurityTooltip(ItemTooltipEvent event)
    {
        if(isWaterContainer(event.getItemStack()))
        {
            if(!event.getItemStack().getTag().contains("Purity"))
                event.getItemStack().getTag().putInt("Purity", 3);

            int purity = event.getItemStack().getTag().getInt("Purity");

            String purityText = purity == 0 ? "Dirty" :
                    purity == 1 ? "Slightly Dirty" :
                            purity == 2 ? "Acceptable" : "Purified";

            int purityColor = purity == 0 ? 11028517 :
                    purity == 1 ? 7957617:
                            purity == 2 ? 6128285 : 2208255;

            event.getToolTip().add(new TextComponent(purityText).setStyle(Style.EMPTY.withColor(purityColor)));
        }
    }

    /**
     * Gives the player effects based on the purity of the water just drank
     * and returns whether thirst and quenched should be added or not
     */
    public static boolean givePurityEffects(Player player, ItemStack item)
    {
        if(ThirstHelper.hasPurity(item))
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

    static boolean isWaterContainer(ItemStack item)
    {
        return ((item.getItem() instanceof PotionItem && PotionUtils.getPotion(item) == Potions.WATER) ||
                item.getItem() == Items.WATER_BUCKET||
                item.getItem() == ItemInit.WATER_BOWL.get()) && item.getTag() != null;
    }

    static void registerDispenserBehaviours()
    {
        //fuck you mappings (the default is m_7216_)
        Method getDispenseMethod = ObfuscationReflectionHelper.findMethod(DispenserBlock.class, "getDispenseMethod", ItemStack.class);

        DefaultDispenseItemBehavior bucketDefaultBehaviour = (DefaultDispenseItemBehavior) ReflectionUtil.fuckYouReflections(getDispenseMethod, Blocks.DISPENSER, new ItemStack(Items.BUCKET));
        OptionalDispenseItemBehavior bottleDefaultBehaviour = (OptionalDispenseItemBehavior) ReflectionUtil.fuckYouReflections(getDispenseMethod, Blocks.DISPENSER, new ItemStack(Items.GLASS_BOTTLE.asItem()));

        //fuck you mappings (part 2) (the default is m_7498_)
        Method execute = ObfuscationReflectionHelper.findMethod(DefaultDispenseItemBehavior.class, "execute", BlockSource.class, ItemStack.class);

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

    public static ItemStack addPurity(ItemStack item, BlockPos pos, Level level)
    {
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt("Purity", getWaterPurity(level, pos));

        return  item;
    }

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
        else
            return -1;
    }

    public static boolean isBiomeWaterSalty(Biome biome)
    {
        return biome.getRegistryName().toString().contains("ocean");
    }
}
