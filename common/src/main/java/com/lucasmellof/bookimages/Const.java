package com.lucasmellof.bookimages;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Const {

	public static final String MOD_ID = "bookimages";
	public static final String MOD_NAME = "Book Images";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static Identifier of(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

}
