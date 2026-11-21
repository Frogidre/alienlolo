package com.alienskin.mixin;

import com.alienskin.AlienModelAnimator;
import com.alienskin.AlienModelBuilder;
import com.alienskin.AlienSkin;
import com.alienskin.AlienTalker;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin {

    @Unique private boolean alienskin$wasOn;

    // Adds the extra alien body parts when the player model is created.
    @Inject(
            method = "getTexturedModelData(Lnet/minecraft/client/model/Dilation;Z)Lnet/minecraft/client/model/ModelData;",
            at = @At("RETURN")
    )
    private static void alienskin$build(Dilation dilation, boolean slim, CallbackInfoReturnable<ModelData> cir) {
        AlienModelBuilder.build(cir.getReturnValue());
    }

    // Poses the alien parts every frame (after vanilla has set its own pose).
    @Inject(
            method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V",
            at = @At("TAIL")
    )
    private void alienskin$animate(PlayerEntityRenderState state, CallbackInfo ci) {
        boolean on = AlienSkin.enabled;
        if (on || alienskin$wasOn) {
            float mouth = ((AlienTalker) (Object) state).alienskin$getMouth();
            AlienModelAnimator.apply((PlayerEntityModel) (Object) this, mouth, on);
        }
        alienskin$wasOn = on;
    }
}
