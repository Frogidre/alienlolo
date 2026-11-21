package com.alienskin;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;

/**
 * Adds empty "bone" parts under the vanilla player parts. They have no boxes of their own;
 * they only carry the transforms (position + rotation) that the smooth alien meshes follow.
 */
public final class AlienModelBuilder {
    private AlienModelBuilder() {}

    public static void build(ModelData data) {
        ModelPartData root = data.getRoot();

        root.getChild("head").addChild("alien_jaw", ModelPartBuilder.create(), ModelTransform.NONE);

        for (String arm : new String[]{"left_arm", "right_arm"}) {
            root.getChild(arm).addChild("alien_fore", ModelPartBuilder.create(), ModelTransform.NONE);
        }
        for (String leg : new String[]{"left_leg", "right_leg"}) {
            ModelPartData shin = root.getChild(leg)
                    .addChild("alien_shin", ModelPartBuilder.create(), ModelTransform.NONE);
            shin.addChild("alien_foot", ModelPartBuilder.create(), ModelTransform.NONE);
        }
    }
}
