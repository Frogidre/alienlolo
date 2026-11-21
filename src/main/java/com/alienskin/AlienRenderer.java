package com.alienskin;

import com.alienskin.mesh.AlienMeshes;
import com.alienskin.mesh.Mesh;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;

/** Draws the smooth alien meshes, following the vanilla bone transforms. */
public final class AlienRenderer {
    private AlienRenderer() {}

    public static void render(PlayerEntityModel m, MatrixStack matrices, VertexConsumer vc,
                              int light, int overlay, int color) {
        if (!AlienSkin.enabled) return;
        if (m.getClass() != PlayerEntityModel.class) return;     // not cape / ears models
        BipedEntityModel<?> b = m;
        if (!b.head.hasChild("alien_jaw")) return;

        matrices.push();
        m.getRootPart().applyTransform(matrices);

        // head + jaw
        matrices.push();
        b.head.applyTransform(matrices);
        draw(AlienMeshes.SKULL, matrices, vc, light, overlay, color);
        matrices.push();
        b.head.getChild("alien_jaw").applyTransform(matrices);
        draw(AlienMeshes.JAW, matrices, vc, light, overlay, color);
        matrices.pop();
        matrices.pop();

        // torso
        matrices.push();
        b.body.applyTransform(matrices);
        draw(AlienMeshes.TORSO, matrices, vc, light, overlay, color);
        matrices.pop();

        arm(b.leftArm, matrices, vc, light, overlay, color);
        arm(b.rightArm, matrices, vc, light, overlay, color);
        leg(b.leftLeg, matrices, vc, light, overlay, color);
        leg(b.rightLeg, matrices, vc, light, overlay, color);

        matrices.pop();
    }

    private static void arm(ModelPart arm, MatrixStack matrices, VertexConsumer vc, int light, int overlay, int color) {
        matrices.push();
        arm.applyTransform(matrices);
        draw(AlienMeshes.ARM_UPPER, matrices, vc, light, overlay, color);
        matrices.push();
        arm.getChild("alien_fore").applyTransform(matrices);
        draw(AlienMeshes.ARM_FORE, matrices, vc, light, overlay, color);
        matrices.pop();
        matrices.pop();
    }

    private static void leg(ModelPart leg, MatrixStack matrices, VertexConsumer vc, int light, int overlay, int color) {
        matrices.push();
        leg.applyTransform(matrices);
        draw(AlienMeshes.LEG_THIGH, matrices, vc, light, overlay, color);
        ModelPart shin = leg.getChild("alien_shin");
        matrices.push();
        shin.applyTransform(matrices);
        draw(AlienMeshes.LEG_SHIN, matrices, vc, light, overlay, color);
        matrices.push();
        shin.getChild("alien_foot").applyTransform(matrices);
        draw(AlienMeshes.LEG_FOOT, matrices, vc, light, overlay, color);
        matrices.pop();
        matrices.pop();
        matrices.pop();
    }

    /** Emits every quad twice (front and reversed) so it is visible whatever the culling setting. */
    private static void draw(Mesh mesh, MatrixStack matrices, VertexConsumer vc, int light, int overlay, int color) {
        MatrixStack.Entry e = matrices.peek();
        int[] q = mesh.quads;
        for (int i = 0; i < q.length; i += 4) {
            for (int k = 0; k < 4; k++) vertex(mesh, q[i + k], e, vc, light, overlay, color);
            for (int k = 3; k >= 0; k--) vertex(mesh, q[i + k], e, vc, light, overlay, color);
        }
    }

    private static void vertex(Mesh m, int i, MatrixStack.Entry e, VertexConsumer vc, int light, int overlay, int color) {
        vc.vertex(e, m.pos[i * 3], m.pos[i * 3 + 1], m.pos[i * 3 + 2])
                .color(color)
                .texture(m.uv[i * 2], m.uv[i * 2 + 1])
                .overlay(overlay)
                .light(light)
                .normal(e, m.nor[i * 3], m.nor[i * 3 + 1], m.nor[i * 3 + 2]);
    }
}
