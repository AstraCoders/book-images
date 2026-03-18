package com.lucasmellof.bookimages.duck;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.ListTag;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
public interface IBookAccess {
	ListTag bookImages$getImages();

	void bookImages$setImages(ListTag tag);

	static IBookAccess of(BookViewScreen.BookAccess access) {
		Object acc = access;
		return ((IBookAccess) acc);
	}
}
