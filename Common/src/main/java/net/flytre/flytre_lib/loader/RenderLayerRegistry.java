package net.flytre.flytre_lib.loader;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;

/**
 * Used to set how to render a block or fluid
 */
public final class RenderLayerRegistry {

    private RenderLayerRegistry() {
        throw new AssertionError();
    }

    private static Delegate DELEGATE;

    static void setDelegate(Delegate delegate) {
        RenderLayerRegistry.DELEGATE = delegate;
    }


    public static void register(RenderLayer type, Block... blocks) {
        DELEGATE.register(type, blocks);
    }

    public static void register(RenderLayer type, Fluid... fluids) {
        DELEGATE.register(type, fluids);
    }

    interface Delegate {
        void register(RenderLayer type, Block... blocks);

        void register(RenderLayer type, Fluid... fluids);
    }
}
