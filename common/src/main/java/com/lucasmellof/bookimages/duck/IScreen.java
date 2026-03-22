package com.lucasmellof.bookimages.duck;

import net.minecraft.client.gui.components.AbstractWidget;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
public interface IScreen {
	void bookImages$addComponent(AbstractWidget widget);

	int bookImages$getBookLeft();

	int bookImages$getBookTop();

	int bookImages$getBookWidth();

	int bookImages$getBookHeight();
}
