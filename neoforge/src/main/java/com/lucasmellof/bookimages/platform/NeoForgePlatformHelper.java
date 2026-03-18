package com.lucasmellof.bookimages.platform;

import com.lucasmellof.bookimages.ModRegistry;
import com.lucasmellof.bookimages.platform.services.IPlatformHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.ListTag;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

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
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public DataComponentType<ListTag> getBookImagesTag() {
        return ModRegistry.BOOK_IMAGES_TAG.get();
    }
}
