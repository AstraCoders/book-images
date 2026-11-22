package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import com.lucasmellof.bookimages.packet.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BookImagesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientImageUploadManager.INSTANCE.tick());

        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundImageResponseStartPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundImageResponseChunkPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundImageResponseFinishPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundImageUploadConfirmPacket.TYPE, (packet, context) -> packet.handle(context.player()));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientImageUploadManager.INSTANCE.cleanup());
    }
}
