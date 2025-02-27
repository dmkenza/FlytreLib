package net.flytre.flytre_lib.mixin.base;


import io.netty.buffer.Unpooled;
import net.flytre.flytre_lib.impl.base.PacketUtilsImpl;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


/**
 * Converts custom registered packets into Minecraft supported custom payload packets.
 * Directly registering custom packets is a BAD idea because if say a mod registers
 * packets but is only installed on either the client or the server, the number based system will desync
 * (the same packet could be given a different ID on the client and server)
 * and cause Minecraft to interpret packets as different packets than they really are
 */
@Mixin(ClientConnection.class)
class ClientConnectionMixin {


    @ModifyVariable(method = "handlePacket", at = @At("HEAD"), argsOnly = true)
    private static <T extends PacketListener> Packet<T> flytre_lib$repackPacket(Packet<T> packet) {
        if (packet instanceof CustomPayloadC2SPacket && PacketUtilsImpl.REGISTERED_IDS.containsKey(((CustomPayloadC2SPacket) packet).getChannel())) {
            PacketByteBuf buf = ((CustomPayloadC2SPacket) packet).getData();
            Identifier channel = ((CustomPayloadC2SPacket) packet).getChannel();
            int index = PacketUtilsImpl.REGISTERED_IDS.get(channel).index();
            var triad = PacketUtilsImpl.PLAY_C2S_PACKET.get(index);
            //noinspection unchecked
            return (Packet<T>) triad.creator().apply(buf);
        }

        if (packet instanceof CustomPayloadS2CPacket && PacketUtilsImpl.REGISTERED_IDS.containsKey(((CustomPayloadS2CPacket) packet).getChannel())) {
            PacketByteBuf buf = ((CustomPayloadS2CPacket) packet).getData();
            Identifier channel = ((CustomPayloadS2CPacket) packet).getChannel();
            int index = PacketUtilsImpl.REGISTERED_IDS.get(channel).index();
            var triad = PacketUtilsImpl.PLAY_S2C_PACKET.get(index);

            //noinspection unchecked
            return (Packet<T>) triad.creator().apply(buf);
        }

        return packet;

    }

    @ModifyVariable(method = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), argsOnly = true)
    public Packet<?> flytre_lib$modifySentPacket(Packet<?> packet) {
        if (PacketUtilsImpl.REGISTERED_TYPES.containsKey(packet.getClass())) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            packet.write(buf);

            PacketUtilsImpl.PacketData data = PacketUtilsImpl.REGISTERED_TYPES.get(packet.getClass());

            if (data.clientbound()) {
                Identifier id = PacketUtilsImpl.PLAY_S2C_PACKET.get(data.index()).channel();
                return new CustomPayloadS2CPacket(id, buf);
            } else {
                Identifier id = PacketUtilsImpl.PLAY_C2S_PACKET.get(data.index()).channel();
                return new CustomPayloadC2SPacket(id, buf);
            }
        }

        return packet;
    }
}
