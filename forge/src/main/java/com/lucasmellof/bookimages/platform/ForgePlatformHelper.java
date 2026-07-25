package com.lucasmellof.bookimages.platform;

import com.lucasmellof.bookimages.ForgeNetwork;
import com.lucasmellof.bookimages.ModRegistry;
import com.lucasmellof.bookimages.platform.services.IPlatformHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
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
        ForgeNetwork.CHANNEL.send(object, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendPacketToClient(ServerPlayer player, CustomPacketPayload pkt) {
        ForgeNetwork.CHANNEL.send(pkt, PacketDistributor.PLAYER.with(player));
    }
}
