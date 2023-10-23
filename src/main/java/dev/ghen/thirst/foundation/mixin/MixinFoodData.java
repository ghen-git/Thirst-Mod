package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public abstract class MixinFoodData
{
    @Shadow
    public abstract void addExhaustion(float p_38704_);
    @Unique
    private int dehydratedHealTimer = 0;


    @Redirect(
            method = {"tick"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 0)
    )
    private void healWithSaturation(Player player, float amount)
    {
        if(!player.getCapability(ModCapabilities.PLAYER_THIRST).isPresent())
            return;
        FoodData foodData = player.getFoodData();
        IThirst thirstData =  player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);

        float f = Math.min(foodData.getSaturationLevel(), 6.0F);

        boolean shouldHeal = !CommonConfig.DEHYDRATION_HALTS_HEALTH_REGEN.get() || thirstData.getThirst() >= 20;

        if(shouldHeal)
        {
            player.heal(f / 6.0F);
            thirstData.setJustHealed();
            return;
        }

        dehydratedHealTimer++;
        if(dehydratedHealTimer >= 8 && thirstData.getThirst() > 18)
        {
            player.heal(f / 6.0F);
            thirstData.setJustHealed();
            dehydratedHealTimer = 0;
            return;
        }

        this.addExhaustion(-f);
    }

    @Redirect(
            method = {"tick"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 1)
    )
    private void healWithHunger(Player player, float amount)
    {
        if(!player.getCapability(ModCapabilities.PLAYER_THIRST).isPresent())
            return;
        IThirst thirstData =  player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        boolean shouldHeal = !CommonConfig.DEHYDRATION_HALTS_HEALTH_REGEN.get() || thirstData.getThirst() > 18;

        if(shouldHeal)
        {
            player.heal(1.0F);
            thirstData.setJustHealed();
        }
        else
            this.addExhaustion(-6.0F);
    }
}
