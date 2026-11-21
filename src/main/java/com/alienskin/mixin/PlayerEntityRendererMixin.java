package com.alienskin.mixin;

import com.alienskin.AlienSkin;
import com.alienskin.AlienTalker;
import com.alienskin.voice.VoiceTracker;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    // Copy "how open is this player's mouth" from the voice tracker into the render state.
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void alienskin$talking(PlayerLikeEntity entity, PlayerEntityRenderState state, float tickProgress, CallbackInfo ci) {
        ((AlienTalker) (Object) state).alienskin$setMouth(VoiceTracker.mouth(entity.getUuid()));
    }

    // Use the alien texture.
    @Inject(
            method = "getTexture(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)Lnet/minecraft/util/Identifier;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void alienskin$texture(PlayerEntityRenderState state, CallbackInfoReturnable<Identifier> cir) {
        if (AlienSkin.enabled) cir.setReturnValue(AlienSkin.TEXTURE);
    }
}
