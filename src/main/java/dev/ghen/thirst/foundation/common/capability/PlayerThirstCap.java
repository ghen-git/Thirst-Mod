package dev.ghen.thirst.foundation.common.capability;

import dev.ghen.thirst.foundation.common.damagesource.ModDamageSource;
import dev.ghen.thirst.foundation.network.message.PlayerThirstSyncMessage;
import vectorwing.farmersdelight.common.registry.ModEffects;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;

public class PlayerThirstCap implements IThirstCap
{

    int thirst = 20;
    int quenched = 5;
    float exhaustion = 0;
    int damageTimer = 0;
    int syncTimer = 0;
    Vec3 lastPos = Vec3.ZERO;

    public Vec3 getLastPos()
    {
        return lastPos;
    }

    private static final float exhaustionMultiplier = 0.175f;

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

    public void drink(Player player, int thirst, int quenched)
    {
        this.thirst = Math.min(this.thirst + thirst, 20);
        this.quenched = Math.min(this.quenched + quenched, 20);
    }

    /**
    * Method adapted from minecraft's Food Data class equivalent for hunger.
    */
    public void tick(Player player)
    {
        if (player.isCreative()||player.isSpectator()) return;

        Difficulty difficulty = player.level().getDifficulty();
        if (!ModList.get().isLoaded("farmersdelight") || !player.hasEffect(ModEffects.NOURISHMENT.get())) {
                updateExhaustion(player);
        }

        if (exhaustion > 4)
        {
            exhaustion -= 4;
            if (quenched > 0)
            {
                quenched--;
            }
            else if (difficulty != Difficulty.PEACEFUL)
            {
                thirst = Math.max(thirst - 1, 0);
            }
        }

        boolean flag = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);

        ++syncTimer;
        if(syncTimer > 10 && !player.level().isClientSide())
        {
            updateThirstData(player);
            syncTimer = 0;
        }

        FoodData foodData = player.getFoodData();
        if (flag && quenched > 0.0F && player.isHurt() && thirst >= 20 && foodData.getSaturationLevel() > 0.0F && foodData.getFoodLevel() >= 20)
        {
            ++damageTimer;
            if (damageTimer >= 10)
            {
                float f = Math.min(quenched, 6.0F);
                addExhaustion(player, f);
                damageTimer = 0;
            }
        }
        else if (flag && thirst >= 18 && player.isHurt() && foodData.getFoodLevel() >= 18)
        {
            ++damageTimer;
            if (damageTimer >= 80)
            {
                addExhaustion(player, 6.0f);
                damageTimer = 0;
            }
        }
        else if (thirst <= 0)
        {
            ++damageTimer;
            if (damageTimer >= 40)
            {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 0 && difficulty == Difficulty.NORMAL)
                {
                    player.hurt(ModDamageSource.DEHYDRATE, 1.0F);
                }

                damageTimer = 0;
            }
        }
    }

    void updateExhaustion(Player player)
    {
        if (!player.isPassenger() && !player.position().equals(lastPos)&&!player.isFallFlying())
        {
            if(player.isSwimming())
            {
                double dist = (Math.abs(player.position().x - lastPos.x)
                        + Math.abs(player.position().y - lastPos.y)
                        + Math.abs(player.position().z - lastPos.z)) / 3;
                addExhaustion(player, (float) dist * exhaustionMultiplier);
            }
            else if (player.onGround() && player.isSprinting())
            {
                double dist = (Math.abs(player.position().x - lastPos.x) + Math.abs(player.position().z - lastPos.z)) / 2;
                if(dist>20) return;
                if(player.isSprinting()){
                    addExhaustion(player, (float) dist * exhaustionMultiplier);
                }
                else {
                    if(CommonConfig.WALKING_CONSUME_WATER.get())
                        addExhaustion(player, (float) dist * exhaustionMultiplier / 5);
                }

            }
        }

        /*
         * If the player is in a hot biome, they will lose thirst even standing still.
         */
        if(player.level().dimensionType().ultraWarm()||player.level().getBiome(player.getOnPos()).value().getBaseTemperature()>=1.9){
            addExhaustion(player, (float)CommonConfig.STAND_STILL_CONSUME_WATER.get());
        }
        lastPos = player.position();
    }

    public void updateThirstData(Player player)
    {
        ThirstModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new PlayerThirstSyncMessage(thirst, quenched, exhaustion));
    }

    @Override
    public void copy(IThirstCap cap)
    {
        thirst = cap.getThirst();
        quenched = cap.getQuenched();
        exhaustion = cap.getExhaustion();
    }

    public void addExhaustion(Player player, float amount)
    {
        if (!player.isCreative()&&!player.isSpectator())
        {
            exhaustion += (amount * ThirstHelper.getExhaustionBiomeModifier(player));
            updateThirstData(player);
        }
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();

        nbt.putInt("thirst", thirst);
        nbt.putInt("quenched", quenched);
        nbt.putFloat("exhaustion", exhaustion);

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt)
    {
        thirst = nbt.getInt("thirst");
        quenched = nbt.getInt("quenched");
        exhaustion = nbt.getFloat("exhaustion");

    }
}
