package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import umpaz.farmersrespite.client.gui.KettleScreen;
import umpaz.farmersrespite.common.block.KettleBlock;

import java.util.Objects;

@Mixin(KettleScreen.class)
public abstract class MixinKettleScreen
{

    @Redirect(method = "renderWaterBarIndicatorTooltip", at = @At(value = "INVOKE", target = "Lumpaz/farmersrespite/common/utility/FRTextUtils;getTranslation(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"), remap = false)
    private MutableComponent addPurityTooltip(String key, Object[] oldArgs)
    {
        BlockState state = Objects.requireNonNull(((KettleScreen) (Object) this).getMenu().tileEntity.getLevel()).getBlockState(((KettleScreen)(Object)this).getMenu().tileEntity.getBlockPos());

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
        return Component.translatable("thirst." + key, args);
    }
}