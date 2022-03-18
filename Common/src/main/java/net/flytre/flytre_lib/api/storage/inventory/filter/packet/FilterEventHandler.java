package net.flytre.flytre_lib.api.storage.inventory.filter.packet;

public interface FilterEventHandler {


    /**
     * Executes when a BlockFilterMode, BlockModMatch, or BlockNbtMatch packet is received
     */
    default void onPacketReceived() {

    }
}
