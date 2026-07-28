package com.lucasmellof.bookimages.mixin;

import com.lucasmellof.bookimages.client.ClientStuff;
import com.lucasmellof.bookimages.client.widget.BookImageWidget;
import com.lucasmellof.bookimages.duck.IScreen;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import com.lucasmellof.bookimages.packet.ServerboundBookImagesSavePacket;
import com.lucasmellof.bookimages.platform.Services;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@Mixin(BookEditScreen.class)
public class MixinBookEditScreen extends Screen implements IScreen {
    @Shadow
    private int currentPage;

    @Final
    @Shadow
    private ItemStack book;

    @Final
    @Shadow
    private InteractionHand hand;

    @Unique
    private boolean bookImages$restored;

    @Unique
    private List<BookImageWidget> bookImages$widgets;

    protected MixinBookEditScreen(Component title) {
        super(title);
        throw new RuntimeException("MixinBookEditScreen constructor should not be called");
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void bookImages$keyPressed(KeyEvent ev, CallbackInfoReturnable<Boolean> cir) {
        if (!ev.isPaste()) return;
        try {
            var image = ClientStuff.pasteImage();
			if (image == null) return;
            ClientImageUploadManager.INSTANCE.sendImage(image);
        	cir.setReturnValue(true);
		} catch (Exception ignored) {
        }
    }

	@Override
	public void bookImages$addComponent(AbstractWidget widget) {
		if (widget instanceof BookImageWidget bookImageWidget) {
			this.bookImages$getWidgets().add(bookImageWidget);
		}
		this.addRenderableWidget(widget);
	}

	@Override
	public void bookImages$removeComponent(AbstractWidget widget) {
		if (widget instanceof BookImageWidget bookImageWidget) {
			this.bookImages$getWidgets().remove(bookImageWidget);
		}
		this.removeWidget(widget);
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
		return true;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void bookImages$init(CallbackInfo ci) {
		if (!this.bookImages$restored) {
			this.bookImages$restored = true;

			var images = this.book.get(Services.PLATFORM.getBookImagesTag());
			if (images != null) {
				for (int i = 0; i < images.size(); i++) {
					var widget = BookImageWidget.fromTag(images.getCompoundOrEmpty(i), this);
					if (widget != null) this.bookImages$getWidgets().add(widget);
				}
			}
		}

		for (var widget : this.bookImages$getWidgets()) {
			this.addRenderableWidget(widget);
		}
	}

	@Inject(method = "saveChanges", at = @At("HEAD"))
	private void bookImages$saveChanges(CallbackInfo ci) {
		var images = new ListTag();
		for (var widget : this.bookImages$getWidgets()) {
			images.add(widget.toTag());
		}

		this.book.set(Services.PLATFORM.getBookImagesTag(), images.copy());
		Services.PLATFORM.sendPacketToServer(new ServerboundBookImagesSavePacket(this.hand == InteractionHand.MAIN_HAND, images));
	}

	@Override
	public int bookImages$getBookLeft() {
		return (this.width / 2) - (bookImages$getBookHeight() / 2) + 2;
	}

	@Override
	public int bookImages$getBookTop() {
		return 30;
	}

	@Override
	public int bookImages$getBookWidth() {
		return 116;
	}

	@Override
	public int bookImages$getBookHeight() {
		return 130;
	}
}
