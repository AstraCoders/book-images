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

    @Shadow
    private boolean isSigning;

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

    @Unique
    private BookImageWidget bookImages$activeWidget;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void bookImages$mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        bookImages$activeWidget = null;
        var widgets = bookImages$getWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            var widget = widgets.get(i);
            if (widget.mouseClicked(x, y, button)) {
                bookImages$activeWidget = widget;
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void bookImages$mouseDragged(double x, double y, int button, double dragX, double dragY,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (bookImages$activeWidget != null && bookImages$activeWidget.mouseDragged(x, y, button, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (bookImages$activeWidget != null && bookImages$activeWidget.mouseReleased(x, y, button)) {
            bookImages$activeWidget = null;
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    protected MixinBookEditScreen(Component title) {
        super(title);
        throw new RuntimeException("MixinBookEditScreen constructor should not be called");
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void bookImages$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!bookImages$isEditable() || !Screen.isPaste(keyCode)) return;
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
			bookImageWidget.visible = !this.isSigning;
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
		return !this.isSigning;
	}

	@Inject(method = "updateButtonVisibility", at = @At("TAIL"))
	private void bookImages$updateWidgetVisibility(CallbackInfo ci) {
		for (var widget : this.bookImages$getWidgets()) {
			widget.visible = !this.isSigning;
		}
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void bookImages$init(CallbackInfo ci) {
		if (!this.bookImages$restored) {
			this.bookImages$restored = true;

			var images = this.book.get(Services.PLATFORM.getBookImagesTag());
			if (images != null) {
				for (int i = 0; i < images.size(); i++) {
					var widget = BookImageWidget.fromTag(images.getCompound(i), this);
					if (widget != null) this.bookImages$getWidgets().add(widget);
				}
			}
		}

		for (var widget : this.bookImages$getWidgets()) {
			widget.visible = !this.isSigning;
			this.addRenderableWidget(widget);
		}
	}

	@Inject(method = "saveChanges", at = @At("HEAD"))
	private void bookImages$saveChanges(boolean sign, CallbackInfo ci) {
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
