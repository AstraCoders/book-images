package com.lucasmellof.bookimages.client;

import com.lucasmellof.bookimages.functional.ThrowableConsumer;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@NoArgsConstructor
public class ClientStuff {

    public static void pasteImage(ThrowableConsumer<BufferedImage, RuntimeException> callback) {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard systemClipboard = toolkit.getSystemClipboard();
            Transferable transferable = systemClipboard.getContents(null);
            if (!transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                return;
            }

            BufferedImage img = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
            callback.accept(img);
        } catch (Exception ignored) {
        }
    }
}
