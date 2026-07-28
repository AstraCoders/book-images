package com.lucasmellof.bookimages.client.widget;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.duck.IScreen;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
public class BookImageWidget extends AbstractWidget {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	private static final int MIN_WIDTH = 32;
	private static final int MIN_HEIGHT = 32;
	private static final int RESIZE_HANDLE_SIZE = 8;
	private static final int REMOVE_BUTTON_SIZE = 8;

	private static final int TEXTURE_WIDTH = 128;
	private static final int TEXTURE_HEIGHT = 128;

	private boolean dragging = false;
	private boolean resizing = false;

	private int dragOffsetX;
	private int dragOffsetY;

	private int startMouseX;
	private int startMouseY;
	private int startWidth;
	private int startHeight;

	private final UUID img;
	private final IScreen parent;
	private final int page;
	private final float aspectRatio;

	private final int originalWidth;
	private final int originalHeight;

	public BookImageWidget(int x, int y, int width, int height, int originalWidth, int originalHeight, IScreen parent, UUID image, int page) {
		super(x, y, width /2, height/2, Component.empty());
		this.img = image;
		this.parent = parent;
		this.page = page;

		this.originalWidth = originalWidth;
		this.originalHeight = originalHeight;

		// Prefer original dimensions to preserve the true aspect ratio; fall back to provided width/height.
		if (this.originalWidth > 0 && this.originalHeight > 0) {
			this.aspectRatio = (float) this.originalWidth / (float) this.originalHeight;
		} else {
			this.aspectRatio = height <= 0 ? 1.0F : (float) width / (float) height;
		}

		this.setWidth(Math.min(this.getWidth(), parent.bookImages$getBookWidth()));
		this.setHeight(Math.min(this.getHeight(), parent.bookImages$getBookHeight()));

		this.setX(Math.clamp(this.getX(), 0, parent.bookImages$getBookWidth() - this.getWidth()));
		this.setY(Math.clamp(this.getY(), 0, parent.bookImages$getBookHeight() - this.getHeight()));

	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.page != this.parent.bookImages$getCurrentPage()) return;

		Identifier id = ClientImageUploadManager.INSTANCE.requestImage(this.img);
		if (id == null) {
			return;
		}

		int renderX = this.parent.bookImages$getBookLeft() + this.getX();
		int renderY = this.parent.bookImages$getBookTop() + this.getY();

		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				id,
				renderX,
				renderY,
				0,
				0,
				this.width,
				this.height,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		if (!this.parent.bookImages$isEditable()) return;

		var inside = this.isInsideWidget(mouseX, mouseY);
		if (!inside) return;

		guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
		// remove button
		int removeButtonRight = renderX + this.width -1;
		int removeButtonBottom = renderY + REMOVE_BUTTON_SIZE + 1;
		int removeButtonLeft = removeButtonRight - REMOVE_BUTTON_SIZE;

		var y = renderY + 1;
		guiGraphics.fill(
				removeButtonLeft,
				y,
				removeButtonRight,
				removeButtonBottom,
				isMouseOverRemoveButton(mouseX, mouseY) ? 0xffff5555 : 0xffaa0000
		);
		guiGraphics.fill(removeButtonLeft + 2, y + 2, removeButtonLeft + 3, y + 3, 0xffffffff);
		guiGraphics.fill(removeButtonLeft + 5, y + 2, removeButtonLeft + 6, y + 3, 0xffffffff);
		guiGraphics.fill(removeButtonLeft + 3, y + 3, removeButtonLeft + 5, y + 5, 0xffffffff);
		guiGraphics.fill(removeButtonLeft + 2, y + 5, removeButtonLeft + 3, y + 6, 0xffffffff);
		guiGraphics.fill(removeButtonLeft + 5, y + 5, removeButtonLeft + 6, y + 6, 0xffffffff);

		// resize button
		int handleLeft = renderX + this.width - RESIZE_HANDLE_SIZE;
		int handleTop = renderY + this.height - RESIZE_HANDLE_SIZE;

		int handleColor = isMouseOverResizeHandle(mouseX, mouseY) ? 0xc059168b : 0xff59168b;
		//

		guiGraphics.fill(
				handleLeft,
				handleTop,
				renderX + this.width,
				renderY + this.height,
				handleColor
		);

		guiGraphics.fill(renderX, renderY, renderX + this.getWidth(), renderY + 1, 0xff59168b);
		guiGraphics.fill(renderX, renderY + this.getHeight() - 1, renderX + this.getWidth(), renderY + this.getHeight(), 0xff59168b);
		guiGraphics.fill(renderX, renderY, renderX + 1, renderY + this.getHeight(), 0xff59168b);
		guiGraphics.fill(renderX + this.getWidth() - 1, renderY, renderX + this.getWidth(), renderY + this.getHeight(), 0xff59168b);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent ev, boolean unknown) {
		if (this.page != this.parent.bookImages$getCurrentPage()) return false;
		if (!this.parent.bookImages$isEditable()) return false;

		if (ev.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}

		int mouseX = (int) ev.x();
		int mouseY = (int) ev.y();

		if (isMouseOverRemoveButton(mouseX, mouseY)) {
			this.parent.bookImages$removeComponent(this);
			return true;
		}

		if (isMouseOverResizeHandle(mouseX, mouseY)) {
			this.resizing = true;
			this.dragging = false;

			this.startMouseX = mouseX - this.parent.bookImages$getBookLeft();
			this.startMouseY = mouseY - this.parent.bookImages$getBookTop();
			this.startWidth = this.width;
			this.startHeight = this.height;

			return true;
		}

		if (isInsideWidget(mouseX, mouseY)) {
			this.dragging = true;
			this.resizing = false;

			this.dragOffsetX = (mouseX - this.parent.bookImages$getBookLeft()) - this.getX();
			this.dragOffsetY = (mouseY - this.parent.bookImages$getBookTop()) - this.getY();

			return true;
		}

		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent ev, double dragX, double dragY) {
		if (this.page != this.parent.bookImages$getCurrentPage()) return false;
		if (!this.parent.bookImages$isEditable()) return false;

		int mouseX = (int) ev.x();
		int mouseY = (int) ev.y();

		int relativeMouseX = mouseX - this.parent.bookImages$getBookLeft();
		int relativeMouseY = mouseY - this.parent.bookImages$getBookTop();

		if (this.dragging) {
			int newX = relativeMouseX - this.dragOffsetX;
			int newY = relativeMouseY - this.dragOffsetY;

			newX = Math.clamp(newX, 0, this.parent.bookImages$getBookWidth() - this.width);
			newY = Math.clamp(newY, 0, this.parent.bookImages$getBookHeight() - this.height);

			this.setX(newX);
			this.setY(newY);
			return true;
		}

		if (this.resizing) {
			boolean free = ev.hasAltDown();

			int maxWidth = this.parent.bookImages$getBookWidth() - this.getX();
			int maxHeight = this.parent.bookImages$getBookHeight() - this.getY();

			int deltaX = relativeMouseX - this.startMouseX;
			int deltaY = relativeMouseY - this.startMouseY;

			int newWidth;
			int newHeight;

			if (free) {
				newWidth = this.startWidth + deltaX;
				newHeight = this.startHeight + deltaY;

				newWidth = Math.max(MIN_WIDTH, newWidth);
				newHeight = Math.max(MIN_HEIGHT, newHeight);

				newWidth = Math.min(newWidth, maxWidth);
				newHeight = Math.min(newHeight, maxHeight);
			} else {
				if (Math.abs(deltaX) >= Math.abs(deltaY)) {
					newWidth = this.startWidth + deltaX;
					newWidth = Math.max(MIN_WIDTH, newWidth);
					newHeight = Math.max(MIN_HEIGHT, Math.round(newWidth / this.aspectRatio));
				} else {
					newHeight = this.startHeight + deltaY;
					newHeight = Math.max(MIN_HEIGHT, newHeight);
					newWidth = Math.max(MIN_WIDTH, Math.round(newHeight * this.aspectRatio));
				}

				if (newWidth > maxWidth) {
					newWidth = maxWidth;
					newHeight = Math.max(MIN_HEIGHT, Math.round(newWidth / this.aspectRatio));
				}

				if (newHeight > maxHeight) {
					newHeight = maxHeight;
					newWidth = Math.max(MIN_WIDTH, Math.round(newHeight * this.aspectRatio));
				}

				newWidth = Math.max(MIN_WIDTH, newWidth);
				newHeight = Math.max(MIN_HEIGHT, newHeight);

				if (newWidth > maxWidth) {
					newWidth = maxWidth;
					newHeight = Math.max(MIN_HEIGHT, Math.round(newWidth / this.aspectRatio));
				}
				if (newHeight > maxHeight) {
					newHeight = maxHeight;
					newWidth = Math.max(MIN_WIDTH, Math.round(newHeight * this.aspectRatio));
				}
			}

			this.setWidth(newWidth);
			this.setHeight(newHeight);
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent ev) {
		if (ev.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			boolean handled = this.dragging || this.resizing;
			this.dragging = false;
			this.resizing = false;
			return handled;
		}

		return false;
	}


	public boolean isInsideWidget(double mouseX, double mouseY) {
		if (this.page != this.parent.bookImages$getCurrentPage()) return false;

		int renderX = this.parent.bookImages$getBookLeft() + this.getX();
		int renderY = this.parent.bookImages$getBookTop() + this.getY();

		return mouseX >= renderX
					   && mouseX < renderX + this.width
					   && mouseY >= renderY
					   && mouseY < renderY + this.height;
	}

	public boolean isMouseOverResizeHandle(double mouseX, double mouseY) {
		if (this.page != this.parent.bookImages$getCurrentPage()) return false;

		int renderX = this.parent.bookImages$getBookLeft() + this.getX();
		int renderY = this.parent.bookImages$getBookTop() + this.getY();

		int left = renderX + this.width - RESIZE_HANDLE_SIZE;
		int top = renderY + this.height - RESIZE_HANDLE_SIZE;
		int right = renderX + this.width;
		int bottom = renderY + this.height;

		return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
	}

	public boolean isMouseOverRemoveButton(double mouseX, double mouseY) {
		if (this.page != this.parent.bookImages$getCurrentPage()) return false;

		int right = this.parent.bookImages$getBookLeft() + this.getX() + this.width;
		int top = this.parent.bookImages$getBookTop() + this.getY();
		int left = right - REMOVE_BUTTON_SIZE;
		int bottom = top + REMOVE_BUTTON_SIZE;

		return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
	}

	public CompoundTag toTag() {
		var tag = new CompoundTag();
		tag.putString("id", this.img.toString());
		tag.putInt("page", this.page);
		tag.putInt("x", this.getX());
		tag.putInt("y", this.getY());
		tag.putInt("width", this.getWidth());
		tag.putInt("height", this.getHeight());
		tag.putInt("original_width", this.originalWidth);
		tag.putInt("original_height", this.originalHeight);
		return tag;
	}

	public static BookImageWidget fromTag(CompoundTag tag, IScreen parent) {
		try {
			return new BookImageWidget(
					tag.getIntOr("x", 0),
					tag.getIntOr("y", 0),
					tag.getIntOr("width", MIN_WIDTH) * 2,
					tag.getIntOr("height", MIN_HEIGHT) * 2,
					tag.getIntOr("original_width", MIN_WIDTH) * 2,
					tag.getIntOr("original_height", MIN_HEIGHT) * 2,
					parent,
					UUID.fromString(tag.getStringOr("id", "")),
					tag.getIntOr("page", 0));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
