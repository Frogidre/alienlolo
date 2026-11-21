package com.alienskin.mixin;

import com.alienskin.AlienRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** After the player model draws its normal parts, also draw the smooth alien meshes. */
@Mixin(Model.class)
public abstract class ModelRenderMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
            at = @At("TAIL")
    )
    private void alienskin$drawAlien(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color, CallbackInfo ci) {
        Object self = this;
        if (self instanceof PlayerEntityModel m) {
            AlienRenderer.render(m, matrices, vertices, light, overlay, color);
        }
    }
}
