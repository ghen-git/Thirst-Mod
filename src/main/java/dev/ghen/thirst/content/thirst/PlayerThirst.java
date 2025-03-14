package dev.ghen.thirst.content.thirst;

import de.teamlapen.vampirism.util.Helper;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.common.damagesource.ModDamageSource;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.foundation.network.message.PlayerThirstSyncMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class PlayerThirst implements IThirst
{
    public static boolean checkTombstoneEffects = false;
    public static boolean checkFDEffects = false;
    public static boolean checkLetsDoBakeryEffects = false;
    public static boolean checkLetsDoBreweryEffects = false;
    public static boolean checkVampirismEffects = false;

    int thirst = 20;
    int quenched = 5;
    float exhaustion = 0;
    int damageTimer = 0;
    int syncTimer = 0;
    float prevTickExhaustion = 0.0F;
    boolean justHealed = false;
    boolean shouldTickThirst = true;
    boolean exhaustionRecalculate = false;
    boolean init = true;

    /**
     * Attempts to give hydration to player if item restores thirst.
     * @param item
     * @param player
     */
    public static void drink(ItemStack item, Player player)
    {
        if(ThirstHelper.itemRestoresThirst(item))
        {
            player.getCapability(ModCapabilities.PLAYER_THIRST,null).ifPresent(cap ->
            {
                if(WaterPurity.givePurityEffects(player, item))
                    cap.drink(player, ThirstHelper.getThirst(item), ThirstHelper.getQuenched(item));
            });
        }
    }

    public int getThirst()
    {
        return thirst;
    }

    public void setThirst(int value)
    {
        thirst = value;
    }

    public int getQuenched()
    {
        return quenched;
    }

    public void setQuenched(int value)
    {
        quenched = value;
    }

    public float getExhaustion()
    {
        return exhaustion;
    }

    public void setExhaustion(float value)
    {
        exhaustion = value;
    }

    @Override
    public void setShouldTickThirst(boolean value){shouldTickThirst = value;}
    @Override
    public boolean getShouldTickThirst(){return shouldTickThirst;}

    public void drink(Player player, int thirst, int quenched)
    {
        int extra_quenched = Math.max(this.thirst + thirst - 20, 0);
        if(!CommonConfig.EXTRA_HYDRATION_CONVERT_TO_QUENCHED.get())
            extra_quenched = 0;
        this.thirst = Math.min(this.thirst + thirst, 20);
        this.quenched = Math.min(this.quenched + quenched + extra_quenched, this.thirst);
    }

    /**
    * Method adapted from minecraft's Food Data class equivalent for hunger.
    */
    public void tick(Player player)
    {
        Difficulty difficulty = player.level().getDifficulty();

        if(player.getAbilities().invulnerable)
            return;

        if(!shouldTickThirst) {
            if (init) {
                init = false;
                updateThirstData(player);
            }
            return;
        }

        if(checkTombstoneEffects && player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("ghostly_shape")))
            return;

        if(checkVampirismEffects && Helper.isVampire(player))
            return;

        boolean isNourished = checkFDEffects && player.hasEffect(ModEffects.NOURISHMENT.get());
        boolean isHunger = player.hasEffect(MobEffects.HUNGER);
        boolean isStuffed = checkLetsDoBakeryEffects &&
                player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("stuffed"));
        boolean isSaturated = checkLetsDoBreweryEffects &&
                player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("saturated"));
        boolean isSitting = player.isPassenger();

        if(CommonConfig.DEPLETES_WHEN_NAUSEA.get() && player.getActiveEffects().stream().anyMatch(e->e.getEffect().equals(MobEffects.CONFUSION))){
            addExhaustion(player,0.06F);
        }

        if(isHunger){
            exhaustion -= 0.005F * (float)(player.getEffect(MobEffects.HUNGER).getAmplifier() + 1) *
                    ThirstHelper.getExhaustionBiomeModifier(player) *
                    ThirstHelper.getExhaustionFireProtModifier(player)*
                    ThirstHelper.getExhaustionFireResistanceModifier(player);
        }

        if (!isSitting && !isNourished && !isStuffed && !isSaturated)
        {
            updateExhaustion(player);
        }

        if (exhaustion > 4)
        {
            exhaustion -= 4;
            if (quenched > 0)
            {
                quenched--;
            }
            else if (difficulty != Difficulty.PEACEFUL || CommonConfig.THIRST_DEPLETION_IN_PEACEFUL.get())
            {
                thirst = Math.max(thirst - 1, 0);
            }
        }

        ++syncTimer;
        if(syncTimer > 10 && !player.level().isClientSide())
        {
            if(difficulty == Difficulty.PEACEFUL && !CommonConfig.THIRST_DEPLETION_IN_PEACEFUL.get()){
                thirst = Math.min(thirst + 1,20);
            }

            final float angle = Mth.wrapDegrees(player.getXRot());
            if (angle <= -80  && player.level().isRainingAt(player.blockPosition().above()) && CommonConfig.CAN_DRINK_RAIN_WATETR.get())
            {
                thirst = Math.min(thirst + 1,20);
                quenched = Math.min(quenched +1,20);
            }

            updateThirstData(player);
            syncTimer = 0;
        }

        if (thirst <= 0)
        {
            ++damageTimer;
            if (damageTimer >= 40)
            {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 0 && difficulty == Difficulty.NORMAL)
                {
                    player.hurt(ModDamageSource.getDamageSource(player.level(),ModDamageSource.DIE_OF_THIRST_KEY), 1.0F);
                }

                damageTimer = 0;
            }
        }
    }

    void updateExhaustion(Player player)
    {
        float hungerExhaustion = player.getFoodData().getExhaustionLevel();
        float normalizedHungerExhaustion = hungerExhaustion < this.prevTickExhaustion ? (exhaustionRecalculate ? hungerExhaustion + 4.0F : hungerExhaustion) : hungerExhaustion;
        if(exhaustionRecalculate){
            exhaustionRecalculate = false;
        }
        float deltaExhaustion = normalizedHungerExhaustion - this.prevTickExhaustion;
        this.addExhaustion(player, deltaExhaustion);
        this.prevTickExhaustion = hungerExhaustion;
    }

    public void updateThirstData(Player player)
    {
        ThirstModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new PlayerThirstSyncMessage(thirst, quenched, exhaustion,shouldTickThirst));
    }

    @Override
    public void setJustHealed()
    {
        justHealed = true;
    }

    @Override
    public void ExhaustionRecalculate(){exhaustionRecalculate = true;}

    @Override
    public void copy(IThirst cap)
    {
        thirst = cap.getThirst();
        quenched = cap.getQuenched();
        exhaustion = cap.getExhaustion();
        shouldTickThirst = cap.getShouldTickThirst();
    }

    public void addExhaustion(Player player, float amount)
    {
        if(!CommonConfig.HEALTH_REGEN_DEPLETES_HYDRATION.get() && justHealed)
            amount = 0;

        if(!CommonConfig.HEALTH_REGEN_DEHYDRATION_IS_BIOME_DEPENDENT.get() && justHealed)
            exhaustion += amount;
        else
            exhaustion += (amount *
                    ThirstHelper.getExhaustionBiomeModifier(player) *
                    ThirstHelper.getExhaustionFireProtModifier(player)*
                    ThirstHelper.getExhaustionFireResistanceModifier(player)
            );

        if(justHealed)
            justHealed = false;

        updateThirstData(player);
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("thirst", thirst);
        nbt.putInt("quenched", quenched);
        nbt.putFloat("exhaustion", exhaustion);
        nbt.putBoolean("enable",shouldTickThirst);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt)
    {
        thirst = nbt.getInt("thirst");
        quenched = nbt.getInt("quenched");
        exhaustion = nbt.getFloat("exhaustion");
        shouldTickThirst = !nbt.contains("enable") || nbt.getBoolean("enable");
    }
}
