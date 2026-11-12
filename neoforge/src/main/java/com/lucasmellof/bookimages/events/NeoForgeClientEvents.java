package com.lucasmellof.bookimages.events;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.client.widget.BookImageWidget;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import net.minecraft.client.gui.components.Renderable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void register(ClientTickEvent.Pre event) {
		ClientImageUploadManager.INSTANCE.tick();
    }

	private static BookImageWidget widget;

	@SubscribeEvent
	public static void onRender(ScreenEvent.MouseButtonPressed.Pre e) {
		for (int i = e.getScreen().renderables.size() - 1; i >= 0; i--) {
			Renderable widget = e.getScreen().renderables.get(i);
			if (!(widget instanceof BookImageWidget w)) continue;

			if (w.isInsideWidget(e.getMouseX(), e.getMouseY())) {
				if (w.mouseClicked(e.getMouseButtonEvent(), false)) {
					NeoForgeClientEvents.widget = w;
					e.setCanceled(true);
					return;
				}
			}
		}

		NeoForgeClientEvents.widget = null;
	}

	@SubscribeEvent
	public static void onRender(ScreenEvent.MouseButtonReleased.Pre e) {
		if (NeoForgeClientEvents.widget != null) {
			if (NeoForgeClientEvents.widget.mouseReleased(e.getMouseButtonEvent())) {
				e.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onRender(ScreenEvent.MouseDragged.Pre e) {
		if (NeoForgeClientEvents.widget != null) {
			if (NeoForgeClientEvents.widget.mouseDragged(e.getMouseButtonEvent(), e.getDragX(), e.getDragY())) {
				e.setCanceled(true);
			}
		}

	}

	@SubscribeEvent
	public static void onQuit(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientImageUploadManager.INSTANCE.cleanup();
	}
}
