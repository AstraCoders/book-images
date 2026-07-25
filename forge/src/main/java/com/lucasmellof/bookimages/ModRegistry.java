package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.utils.BookImagesCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModRegistry {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Const.MOD_ID);

    public static final RegistryObject<DataComponentType<ListTag>> BOOK_IMAGES_TAG =
            DATA_COMPONENTS.register("book_images", () ->
                    DataComponentType.<ListTag>builder()
                            .networkSynchronized(BookImagesCodecs.LIST_TAG_STREAM_CODEC)
                            .persistent(BookImagesCodecs.LIST_TAG_CODEC)
                            .build());

    private ModRegistry() {
    }

    public static void register(BusGroup modBusGroup) {
        DATA_COMPONENTS.register(modBusGroup);
    }
}
