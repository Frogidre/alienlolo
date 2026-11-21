package com.alienskin;

import net.minecraft.util.Identifier;

public final class AlienSkin {
    public static final String MOD_ID = "alienskin";

    public static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/alien.png");

    /** Toggled with the keybind. */
    public static volatile boolean enabled = true;

    private AlienSkin() {}
}
