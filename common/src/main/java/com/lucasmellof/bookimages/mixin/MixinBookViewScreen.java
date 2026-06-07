package com.lucasmellof.bookimages.mixin;

import com.lucasmellof.bookimages.client.widget.BookImageWidget;
import com.lucasmellof.bookimages.duck.IBookAccess;
import com.lucasmellof.bookimages.duck.IScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@Mixin(BookViewScreen.class)
public class MixinBookViewScreen extends Screen implements IScreen {
    @Shadow
    private int currentPage;

    @Shadow
    private BookViewScreen.BookAccess bookAccess;

    @Unique
    private boolean bookImages$restored;

    @Unique
    private List<BookImageWidget> bookImages$widgets;

    protected MixinBookViewScreen(Component title) {
        super(title);
        throw new RuntimeException("MixinBookViewScreen constructor should not be called");
    }

	@Override
	public void bookImages$addComponent(AbstractWidget widget) {
		if (widget instanceof BookImageWidget bookImageWidget) {
			this.bookImages$getWidgets().add(bookImageWidget);
		}
		this.addRenderableOnly(widget);
	}

	@Unique
	private List<BookImageWidget> bookImages$getWidgets() {
		if (this.bookImages$widgets == null) {
			this.bookImages$widgets = new ArrayList<>();
		}
		return this.bookImages$widgets;
	}

	@Override
	public int bookImages$getCurrentPage() {
		return this.currentPage;
	}

	@Override
	public boolean bookImages$isEditable() {
		return false;
	}

	@Override
	public int bookImages$getBookLeft() {
		return (this.width - 192) / 2 + 36;
	}

	@Override
	public int bookImages$getBookTop() {
		return 32;
	}

	@Override
	public int bookImages$getBookWidth() {
		return 116;
	}

	@Override
	public int bookImages$getBookHeight() {
		return 130;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void bookImages$init(CallbackInfo ci) {
		if (!this.bookImages$restored) {
			this.bookImages$restored = true;

			var images = IBookAccess.of(this.bookAccess).bookImages$getImages();
			if (images != null) {
				for (int i = 0; i < images.size(); i++) {
					var widget = BookImageWidget.fromTag(images.getCompoundOrEmpty(i), this);
					if (widget != null) this.bookImages$getWidgets().add(widget);
				}
			}
		}

		for (var widget : this.bookImages$getWidgets()) {
			this.addRenderableOnly(widget);
		}
	}

}
