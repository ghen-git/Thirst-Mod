package dev.ghen.thirst.content.thirst;

import de.teamlapen.vampirism.util.Helper;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.damagesource.ModDamageSource;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.foundation.network.message.PlayerThirstSyncMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class PlayerThirst implements IThirst
{
    public static boolean checkTombstoneEffects = false;
    public static boolean checkFDEffects = false;
    public static boolean checkLetsDoBakeryEffects = false;
    public static boolean checkVampirismEffects = false;

    int thirst = 20;
    int quenched = 5;
    float exhaustion = 0;
    int damageTimer = 0;
    int syncTimer = 0;
    float prevTickExhaustion = 0.0F;
    Vec3 lastPos = Vec3.ZERO;
    boolean justHealed = false;

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
        this.quenched = Math.min(this.quenched + quenched, this.thirst);
    }

    /**
    * Method adapted from minecraft's Food Data class equivalent for hunger.
    */
    public void tick(Player player)
    {
        Difficulty difficulty = player.level().getDifficulty();

        if(player.getAbilities().invulnerable)
            return;

        if(checkTombstoneEffects && player.hasEffect(ovh.corail.tombstone.registry.ModEffects.ghostly_shape))
            return;

        if(checkVampirismEffects && Helper.isVampire(player))
            return;

        boolean isNourished = checkFDEffects && player.hasEffect(ModEffects.NOURISHMENT.get());
        boolean isStuffed = checkLetsDoBakeryEffects &&
                player.getActiveEffects().stream().anyMatch(e -> e.getDescriptionId().contains("stuffed"));

        if (!isNourished && !isStuffed)
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
            else if (difficulty != Difficulty.PEACEFUL)
            {
                thirst = Math.max(thirst - 1, 0);
            }
        }

        ++syncTimer;
        if(syncTimer > 10 && !player.level().isClientSide())
        {
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
        float normalizedHungerExhaustion = hungerExhaustion < this.prevTickExhaustion ? hungerExhaustion + 4.0F : hungerExhaustion;
        float deltaExhaustion = normalizedHungerExhaustion - this.prevTickExhaustion;
        this.addExhaustion(player, deltaExhaustion);
        this.prevTickExhaustion = hungerExhaustion;
    }

    public void updateThirstData(Player player)
    {
        ThirstModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new PlayerThirstSyncMessage(thirst, quenched, exhaustion));
    }

    @Override
    public void setJustHealed()
    {
        justHealed = true;
    }

    @Override
    public void copy(IThirst cap)
    {
        thirst = cap.getThirst();
        quenched = cap.getQuenched();
        exhaustion = cap.getExhaustion();
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

        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt)
    {
        thirst = nbt.getInt("thirst");
        quenched = nbt.getInt("quenched");
        exhaustion = nbt.getFloat("exhaustion");

    }
}
