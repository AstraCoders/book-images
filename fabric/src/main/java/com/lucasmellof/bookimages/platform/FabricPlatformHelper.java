package com.lucasmellof.bookimages.platform;

import com.lucasmellof.bookimages.ModRegistry;
import com.lucasmellof.bookimages.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public DataComponentType<ListTag> getBookImagesTag() {
        return ModRegistry.BOOK_IMAGES_TAG;
    }

    @Override
    public void sendPacketToServer(CustomPacketPayload object) {
        ClientPlayNetworking.send(object);
    }

    @Override
    public void sendPacketToClient(ServerPlayer player, CustomPacketPayload pkt) {
        ServerPlayNetworking.send(player, pkt);
    }
}
