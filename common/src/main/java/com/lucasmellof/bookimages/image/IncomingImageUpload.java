package com.lucasmellof.bookimages.image;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 18/03/2026
 */
@Slf4j
@Getter
public class IncomingImageUpload {
	private final String imageName;
	private final long expectedSize;
	private final int totalChunks;
	private final byte[][] chunks;
	private final boolean[] receivedChunks;

	private int receivedCount = 0;
	private long receivedSize = 0;

	private boolean finished;

	public IncomingImageUpload(String imageName, long expectedSize, int totalChunks) {
		this.imageName = imageName;
		this.expectedSize = expectedSize;
		this.totalChunks = totalChunks;
		this.chunks = new byte[totalChunks][];
		this.receivedChunks = new boolean[totalChunks];
	}

	public boolean receiveChunk(int chunkIndex, byte[] data) {
		if (chunkIndex < 0 || chunkIndex >= totalChunks) {
			log.warn("Received chunk with invalid index {} for image {}", chunkIndex, imageName);
			return false;
		}
		if (receivedChunks[chunkIndex]) {
			log.warn("Received duplicate chunk {} for image {}", chunkIndex, imageName);
			return false;
		}

		chunks[chunkIndex] = data;
		receivedChunks[chunkIndex] = true;
		receivedCount++;
		receivedSize += data.length;
		return true;
	}

	public void finish() {
		if (receivedCount != totalChunks) {
			throw new IllegalStateException("Cannot finish upload: expected " + totalChunks + " chunks but received " + receivedCount);
		}
		if (receivedSize != expectedSize) {
			throw new IllegalStateException("Cannot finish upload: expected size " + expectedSize + " but received " + receivedSize);
		}

		this.finished = true;
	}

	public boolean isComplete() {
		return receivedCount == totalChunks && receivedSize == expectedSize;
	}

	public byte[] join() {
		if (!finished) {
			throw new IllegalStateException("Cannot join chunks: upload is not finished");
		}

		var baos = new ByteArrayOutputStream((int) this.expectedSize);
		for(int i = 0; i < this.chunks.length; i++) {
			byte[] chunk = this.chunks[i];
			if (chunk == null) {
				throw new IllegalStateException("Chunk " + i + " is missing");
			}

			baos.write(chunk, 0, chunk.length);
		}

		return baos.toByteArray();
	}
}
