package com.alienskin;

import com.alienskin.mesh.AlienMeshes;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

/** Hides the vanilla boxes and poses the alien bones every frame. */
public final class AlienModelAnimator {
    private AlienModelAnimator() {}

    public static void apply(PlayerEntityModel m, float mouth, boolean on) {
        BipedEntityModel<?> b = m;
        if (!b.head.hasChild("alien_jaw")) return;     // not the real player model

        // hide the vanilla boxes (children are not drawn anyway) and the clothing layers
        ModelPart[] limbs = {b.head, b.body, b.leftArm, b.rightArm, b.leftLeg, b.rightLeg};
        for (ModelPart p : limbs) p.hidden = on;
        if (on) {
            b.hat.visible = false;
            m.jacket.visible = false;
            m.leftSleeve.visible = false;
            m.rightSleeve.visible = false;
            m.leftPants.visible = false;
            m.rightPants.visible = false;
        }
        if (!on) return;

        // jaw: hinge behind the chin, front drops when talking
        double[] h = AlienMeshes.JAW_HINGE;
        ModelPart jaw = b.head.getChild("alien_jaw");
        jaw.setOrigin((float) h[0], (float) h[1], (float) h[2]);
        jaw.pitch = 0.55f * mouth;

        // narrow shoulders, slightly bent elbows
        b.leftArm.originX = 4.4f;
        b.rightArm.originX = -4.4f;
        poseArm(b.leftArm);
        poseArm(b.rightArm);

        // bent knees, flat feet
        poseLeg(b.leftLeg);
        poseLeg(b.rightLeg);
    }

    private static void poseArm(ModelPart arm) {
        ModelPart fore = arm.getChild("alien_fore");
        fore.setOrigin(0.0f, 6.8f, 0.2f);
        fore.pitch = -0.2f;
    }

    private static void poseLeg(ModelPart leg) {
        ModelPart shin = leg.getChild("alien_shin");
        shin.setOrigin(0.0f, 6.4f, 0.3f);
        shin.pitch = 0.32f;
        ModelPart foot = shin.getChild("alien_foot");
        foot.setOrigin(0.0f, 5.7f, 0.0f);
        foot.pitch = -0.32f;
    }
}
