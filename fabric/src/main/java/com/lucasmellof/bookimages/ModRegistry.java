package com.lucasmellof.bookimages;

import com.lucasmellof.bookimages.utils.BookImagesCodecs;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;

public final class ModRegistry {
    public static final DataComponentType<ListTag> BOOK_IMAGES_TAG =
            DataComponentType.<ListTag>builder()
                    .networkSynchronized(BookImagesCodecs.LIST_TAG_STREAM_CODEC)
                    .persistent(BookImagesCodecs.LIST_TAG_CODEC)
                    .build();

    private ModRegistry() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Const.of("book_images"), BOOK_IMAGES_TAG);
    }
}
