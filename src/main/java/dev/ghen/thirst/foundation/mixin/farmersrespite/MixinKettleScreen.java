package dev.ghen.thirst.foundation.mixin.farmersrespite;

import com.farmersrespite.client.gui.KettleScreen;
import com.farmersrespite.common.block.KettleBlock;
import com.farmersrespite.common.block.entity.KettleBlockEntity;
import com.farmersrespite.common.block.entity.container.KettleContainer;
import com.farmersrespite.core.utility.FRTextUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

@Mixin(KettleScreen.class)
public abstract class MixinKettleScreen
{

    @Redirect(method = "renderWaterBarIndicatorTooltip", at = @At(value = "INVOKE", target = "Lcom/farmersrespite/core/utility/FRTextUtils;getTranslation(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"), remap = false)
    private MutableComponent addPurityTooltip(String key, Object[] oldArgs)
    {
        BlockState state = ((KettleScreen)(Object)this).getMenu().tileEntity.getLevel().getBlockState(((KettleScreen)(Object)this).getMenu().tileEntity.getBlockPos());

        int waterLevel = state.getValue(KettleBlock.WATER_LEVEL);
        int purity = WaterPurity.getBlockPurity(state);
        String purityString = purity == 0 ? "dirty" :
                purity == 1 ? "slightly dirty" :
                purity == 2 ? "acceptable" : "purified";

        Object[] args;
        if(purity == -1 || waterLevel == 0)
        {
            args = new Object[]{};
        }
        else if(waterLevel == 1)
        {
            args = new Object[]{purityString};
        }
        else
        {
            args = new Object[]{waterLevel, purityString};
        }

        return new TranslatableComponent("thirst." + key, args);
    }
}
