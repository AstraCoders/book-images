package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.packet.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class BookImages implements ModInitializer {
    @Override
    public void onInitialize() {
        ModRegistry.register();
        registerPayloads();
        CommonClass.init();
    }

    private static void registerPayloads() {
        var c2s = PayloadTypeRegistry.playC2S();
        c2s.register(ServerboundImageUploadStartPacket.TYPE, ServerboundImageUploadStartPacket.CODEC);
        c2s.register(ServerboundImageUploadChunkPacket.TYPE, ServerboundImageUploadChunkPacket.CODEC);
        c2s.register(ServerboundImageUploadFinishPacket.TYPE, ServerboundImageUploadFinishPacket.CODEC);
        c2s.register(ServerboundImageRequestPacket.TYPE, ServerboundImageRequestPacket.CODEC);
        c2s.register(ServerboundBookImagesSavePacket.TYPE, ServerboundBookImagesSavePacket.CODEC);

        var s2c = PayloadTypeRegistry.playS2C();
        s2c.register(ClientboundImageResponseStartPacket.TYPE, ClientboundImageResponseStartPacket.CODEC);
        s2c.register(ClientboundImageResponseChunkPacket.TYPE, ClientboundImageResponseChunkPacket.CODEC);
        s2c.register(ClientboundImageResponseFinishPacket.TYPE, ClientboundImageResponseFinishPacket.CODEC);
        s2c.register(ClientboundImageUploadConfirmPacket.TYPE, ClientboundImageUploadConfirmPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundImageUploadStartPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundImageUploadChunkPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundImageUploadFinishPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundImageRequestPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(
                ServerboundBookImagesSavePacket.TYPE, (packet, context) -> packet.handle(context.player()));
    }
}
