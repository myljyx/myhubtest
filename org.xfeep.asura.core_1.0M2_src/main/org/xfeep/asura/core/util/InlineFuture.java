package org.xfeep.asura.core.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public  class InlineFuture<T> implements Future<T> {

	T result;
	public InlineFuture(T result) {
		this.result = result;
	}
	
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	public T get() throws InterruptedException, ExecutionException {
		return result;
	}

	public T get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return result;
	}

	public boolean isCancelled() {
		return false;
	}

	public boolean isDone() {
		return true;
	}
	
}