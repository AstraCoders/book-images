package com.lucasmellof.bookimages.events;

import com.lucasmellof.bookimages.client.widget.BookImageWidget;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class ForgeClientEvents {
    private static BookImageWidget activeWidget;

    private ForgeClientEvents() {
    }

    public static void register() {
        TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> ClientImageUploadManager.INSTANCE.tick());
        ScreenEvent.MouseButtonPressed.Pre.BUS.addListener(ForgeClientEvents::onMousePressed);
        ScreenEvent.MouseButtonReleased.Pre.BUS.addListener(ForgeClientEvents::onMouseReleased);
        ScreenEvent.MouseDragged.Pre.BUS.addListener(ForgeClientEvents::onMouseDragged);
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(ForgeClientEvents::onQuit);
    }

    private static void onQuit(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientImageUploadManager.INSTANCE.cleanup();
    }

    private static boolean onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        for (int i = event.getScreen().renderables.size() - 1; i >= 0; i--) {
            if (!(event.getScreen().renderables.get(i) instanceof BookImageWidget widget)) {
                continue;
            }
            if (widget.isInsideWidget(event.getMouseX(), event.getMouseY())
                    && widget.mouseClicked(event.getInfo(), false)) {
                activeWidget = widget;
                return true;
            }
        }
        activeWidget = null;
        return false;
    }

    private static boolean onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        return activeWidget != null && activeWidget.mouseReleased(mouseEvent(
                event.getMouseX(), event.getMouseY(), event.getButton()));
    }

    private static boolean onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        return activeWidget != null && activeWidget.mouseDragged(
                mouseEvent(event.getMouseX(), event.getMouseY(), event.getMouseButton()),
                event.getDragX(),
                event.getDragY());
    }

    private static MouseButtonEvent mouseEvent(double x, double y, int button) {
        long window = Minecraft.getInstance().getWindow().handle();
        boolean altDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
        int modifiers = altDown ? GLFW.GLFW_MOD_ALT : 0;
        return new MouseButtonEvent(x, y, new MouseButtonInfo(button, modifiers));
    }
}
