package net.flytre.flytre_lib.loader;

import net.flytre.flytre_lib.api.config.ConfigHandler;
import net.flytre.flytre_lib.impl.config.init.FlytreLibConfig;

import java.nio.file.Path;

/**
 * Basic loader properties, like config directory
 */
public final class LoaderProperties {

    public static ConfigHandler<FlytreLibConfig> HANDLER = null;

    private static Delegate DELEGATE;

    static void setDelegate(Delegate delegate) {
        LoaderProperties.DELEGATE = delegate;
    }

    private LoaderProperties() {
        throw new AssertionError();
    }

    public static Path getModConfigDirectory() {
        return DELEGATE.getModConfigDirectory();
    }

    public static String getModName(String modId) {
        return DELEGATE.getModName(modId);
    }

    public static boolean isDevEnvironment() {
        return DELEGATE.isDevEnvironment();
    }

    interface Delegate {
        Path getModConfigDirectory();

        String getModName(String modId);

        boolean isDevEnvironment();
    }

}
