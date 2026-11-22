package com.lucasmellof.bookimages.platform;

import com.lucasmellof.bookimages.ModRegistry;
import com.lucasmellof.bookimages.platform.services.IPlatformHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public DataComponentType<ListTag> getBookImagesTag() {
        return ModRegistry.BOOK_IMAGES_TAG.get();
    }

    @Override
    public void sendPacketToServer(CustomPacketPayload object) {
        PacketDistributor.sendToServer(object);
    }

    @Override
    public void sendPacketToClient(ServerPlayer player, CustomPacketPayload pkt) {
        PacketDistributor.sendToPlayer(player, pkt);
    }
}
