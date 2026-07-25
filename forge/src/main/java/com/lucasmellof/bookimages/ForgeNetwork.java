package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.packet.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;

public final class ForgeNetwork {
    public static Channel<CustomPacketPayload> CHANNEL;

    private ForgeNetwork() {
    }

    public static void register() {
        var connection = ChannelBuilder.named(Const.of("main"))
                .networkProtocolVersion(1)
                .payloadChannel();
        var play = connection.play();

        var serverbound = play.serverbound();
        serverbound.addMain(ServerboundImageUploadStartPacket.TYPE, ServerboundImageUploadStartPacket.CODEC.cast(),
                (packet, context) -> packet.handle(context.getSender()));
        serverbound.addMain(ServerboundImageUploadChunkPacket.TYPE, ServerboundImageUploadChunkPacket.CODEC.cast(),
                (packet, context) -> packet.handle(context.getSender()));
        serverbound.addMain(ServerboundImageUploadFinishPacket.TYPE, ServerboundImageUploadFinishPacket.CODEC.cast(),
                (packet, context) -> packet.handle(context.getSender()));
        serverbound.addMain(ServerboundImageRequestPacket.TYPE, ServerboundImageRequestPacket.CODEC.cast(),
                (packet, context) -> packet.handle(context.getSender()));
        serverbound.addMain(ServerboundBookImagesSavePacket.TYPE, ServerboundBookImagesSavePacket.CODEC.cast(),
                (packet, context) -> packet.handle(context.getSender()));

        var clientbound = play.clientbound();
        clientbound.addMain(ClientboundImageResponseStartPacket.TYPE, ClientboundImageResponseStartPacket.CODEC.cast(),
                (packet, context) -> packet.handle(null));
        clientbound.addMain(ClientboundImageResponseChunkPacket.TYPE, ClientboundImageResponseChunkPacket.CODEC.cast(),
                (packet, context) -> packet.handle(null));
        clientbound.addMain(ClientboundImageResponseFinishPacket.TYPE, ClientboundImageResponseFinishPacket.CODEC.cast(),
                (packet, context) -> packet.handle(null));
        clientbound.addMain(ClientboundImageUploadConfirmPacket.TYPE, ClientboundImageUploadConfirmPacket.CODEC.cast(),
                (packet, context) -> packet.handle(null));

        CHANNEL = clientbound.build();
    }
}
