package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.client.widget.BookImageWidget;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import com.lucasmellof.bookimages.packet.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

public class BookImagesClient implements ClientModInitializer {
    private static BookImageWidget activeWidget;

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

        ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
            ScreenMouseEvents.allowMouseClick(screen).register((currentScreen, event) -> {
                for (int i = currentScreen.children().size() - 1; i >= 0; i--) {
                    if (!(currentScreen.children().get(i) instanceof BookImageWidget widget)) {
                        continue;
                    }
                    if (widget.isInsideWidget(event.x(), event.y()) && widget.mouseClicked(event, false)) {
                        activeWidget = widget;
                        return false;
                    }
                }
                activeWidget = null;
                return true;
            });

            ScreenMouseEvents.allowMouseRelease(screen).register((currentScreen, event) ->
                    activeWidget == null || !activeWidget.mouseReleased(event));

            ScreenMouseEvents.allowMouseDrag(screen).register((currentScreen, event, dragX, dragY) ->
                    activeWidget == null || !activeWidget.mouseDragged(event, dragX, dragY));
        });
    }
}
