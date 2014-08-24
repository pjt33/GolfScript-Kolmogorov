package org.cheddarmonk.util;

public interface Function<T, U> {
	public U eval(T t);
}
