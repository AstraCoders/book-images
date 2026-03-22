package com.lucasmellof.bookimages.image;

import lombok.Getter;

import java.io.InputStream;
import java.util.UUID;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@Getter
public class OutgoingImageUpload implements AutoCloseable {
	private final UUID transferId;
	private final String imageName;
	private final long size;
	private final int totalChunks;
	private int nextChunkIdx;

	private final InputStream inputStream;

	public OutgoingImageUpload(UUID transferId, String imageName, InputStream inputStream, long size, int totalChunks) {
		this.transferId = transferId;
		this.imageName = imageName;
		this.inputStream = inputStream;
		this.size = size;
		this.totalChunks = totalChunks;
		this.nextChunkIdx = 0;
	}

	@Override
	public void close() throws Exception {
		inputStream.close();
	}

	public void incrementNextChunkIdx() {
		nextChunkIdx++;
	}
}
