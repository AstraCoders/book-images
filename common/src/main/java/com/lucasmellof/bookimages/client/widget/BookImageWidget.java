package com.lucasmellof.bookimages.client.widget;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
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

	public BookImageWidget(int x, int y, int width, int height, UUID image) {
		super(x, y, width /2, height/2, Component.empty());
		this.img = image;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Identifier id = ClientImageUploadManager.INSTANCE.requestImage(this.img);
		if (id == null) {
			return;
		}

		guiGraphics.blit(
				RenderPipelines.GUI_TEXTURED,
				id,
				this.getX(),
				this.getY(),
				0,
				0,
				this.width,
				this.height,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT,
				TEXTURE_WIDTH,
				TEXTURE_HEIGHT
		);

		int handleLeft = this.getX() + this.width - RESIZE_HANDLE_SIZE;
		int handleTop = this.getY() + this.height - RESIZE_HANDLE_SIZE;

		int handleColor = isMouseOverResizeHandle(mouseX, mouseY) ? 0xC0FFFFFF : 0x80FFFFFF;

		guiGraphics.fill(
				handleLeft,
				handleTop,
				this.getX() + this.width,
				this.getY() + this.height,
				handleColor
		);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent ev, boolean unknown) {
		if (ev.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}

		int mouseX = (int) ev.x();
		int mouseY = (int) ev.y();

		if (isMouseOverResizeHandle(mouseX, mouseY)) {
			this.resizing = true;
			this.dragging = false;

			this.startMouseX = mouseX;
			this.startMouseY = mouseY;
			this.startWidth = this.width;
			this.startHeight = this.height;

			return true;
		}

		if (isInsideWidget(mouseX, mouseY)) {
			this.dragging = true;
			this.resizing = false;

			this.dragOffsetX = mouseX - this.getX();
			this.dragOffsetY = mouseY - this.getY();

			return true;
		}

		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent ev, double dragX, double dragY) {
		int mouseX = (int) ev.x();
		int mouseY = (int) ev.y();

		int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

		if (this.dragging) {
			int newX = mouseX - this.dragOffsetX;
			int newY = mouseY - this.dragOffsetY;

			newX = Math.max(0, Math.min(newX, screenWidth - this.width));
			newY = Math.max(0, Math.min(newY, screenHeight - this.height));

			this.setX(newX);
			this.setY(newY);
			return true;
		}

		if (this.resizing) {
			int newWidth = this.startWidth + (mouseX - this.startMouseX);
			int newHeight = this.startHeight + (mouseY - this.startMouseY);

			newWidth = Math.max(MIN_WIDTH, newWidth);
			newHeight = Math.max(MIN_HEIGHT, newHeight);

			newWidth = Math.min(newWidth, screenWidth - this.getX());
			newHeight = Math.min(newHeight, screenHeight - this.getY());

			this.width = newWidth;
			this.height = newHeight;
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


	private boolean isInsideWidget(double mouseX, double mouseY) {
		return mouseX >= this.getX()
					   && mouseX < this.getX() + this.width
					   && mouseY >= this.getY()
					   && mouseY < this.getY() + this.height;
	}

	private boolean isMouseOverResizeHandle(double mouseX, double mouseY) {
		int left = this.getX() + this.width - RESIZE_HANDLE_SIZE;
		int top = this.getY() + this.height - RESIZE_HANDLE_SIZE;
		int right = this.getX() + this.width;
		int bottom = this.getY() + this.height;

		return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
	}
}
