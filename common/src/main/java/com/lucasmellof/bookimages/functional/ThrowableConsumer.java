package com.lucasmellof.bookimages.functional;

import java.util.function.Consumer;

/*
 * @author Lucasmellof, Lucas de Mello Freitas created on 17/03/2026
 */
@FunctionalInterface
public interface ThrowableConsumer<T, E extends Throwable> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the input argument
	 * @throws E an exception of type {@link E}
	 */
	void accept(T t) throws E;

}
