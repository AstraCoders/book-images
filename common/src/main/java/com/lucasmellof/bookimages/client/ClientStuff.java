package com.lucasmellof.bookimages.client;

import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@NoArgsConstructor
public class ClientStuff {

    public static BufferedImage pasteImage() throws Exception {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard systemClipboard = toolkit.getSystemClipboard();
        Transferable transferable = systemClipboard.getContents(null);
        BufferedImage image = null;
        if (transferable == null) {
            return image;
        } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            image = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
        } else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            var fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            if (fileList.isEmpty()) return image;
            File file = fileList.getFirst();
            if (file == null) return image;
            // filter by extension
            if (!file.getName().endsWith(".png")) return image;
            image = ImageIO.read(file);
        }

		return image;
    }
}
