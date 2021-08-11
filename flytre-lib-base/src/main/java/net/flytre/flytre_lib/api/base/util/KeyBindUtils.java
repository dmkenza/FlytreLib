package net.flytre.flytre_lib.api.base.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.flytre.flytre_lib.impl.base.KeyBindUtilsImpl;
import net.minecraft.client.option.KeyBinding;

/**
 * Used to register key binds on the client
 */
@Environment(EnvType.CLIENT)
public class KeyBindUtils {

    public static KeyBinding register(KeyBinding keyBinding) {
        return KeyBindUtilsImpl.register(keyBinding);
    }

    private KeyBindUtils() {
        throw new AssertionError();
    }
}
