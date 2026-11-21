package com.alienskin.mixin;

import com.alienskin.AlienTalker;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements AlienTalker {
    private float alienskin$mouth;

    @Override
    public float alienskin$getMouth() {
        return alienskin$mouth;
    }

    @Override
    public void alienskin$setMouth(float mouth) {
        this.alienskin$mouth = mouth;
    }
}
