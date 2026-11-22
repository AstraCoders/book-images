package com.lucasmellof.bookimages.events;

import com.lucasmellof.bookimages.Const;
import com.lucasmellof.bookimages.image.ClientImageUploadManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@EventBusSubscriber(modid = Const.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void register(ClientTickEvent.Pre event) {
		ClientImageUploadManager.INSTANCE.tick();
    }

	@SubscribeEvent
	public static void onQuit(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientImageUploadManager.INSTANCE.cleanup();
	}
}
