package dev.nipafx.ginevra.util;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MultiplexingQueue<ELEMENT> {

	private static final AtomicInteger QUEUE_NAME_SUFFIX = new AtomicInteger();

	private final BlockingQueue<ELEMENT> queue;
	private final List<Consumer<? super ELEMENT>> handlers;

	public MultiplexingQueue(String threadName, int capacity, boolean fair) {
		this.queue = new ArrayBlockingQueue<>(capacity, fair);
		this.handlers = new CopyOnWriteArrayList<>();
		startQueueTakingThread(threadName);
	}

	public MultiplexingQueue(String threadName, int capacity) {
		this(threadName, capacity, false);
	}

	public MultiplexingQueue(String threadName) {
		this(threadName, 1024, false);
	}

	public MultiplexingQueue(int capacity) {
		var threadName = "multiplexing-queue-" + QUEUE_NAME_SUFFIX.getAndIncrement();
		this(threadName, capacity, false);
	}

	public MultiplexingQueue() {
		var threadName = "multiplexing-queue-" + QUEUE_NAME_SUFFIX.getAndIncrement();
		this(threadName, 1024, false);
	}

	public MultiplexingQueue(Consumer<? super ELEMENT> handler, String threadName, int capacity, boolean fair) {
		this(threadName, capacity, fair);
		handlers.add(handler);
	}

	public MultiplexingQueue(Consumer<? super ELEMENT> handler, String threadName, int capacity) {
		this(handler, threadName, capacity, false);
	}

	public MultiplexingQueue(Consumer<? super ELEMENT> handler, String threadName) {
		this(handler, threadName, 1024, false);
	}

	public MultiplexingQueue(Consumer<? super ELEMENT> handler, int capacity) {
		var threadName = "multiplexing-queue-" + QUEUE_NAME_SUFFIX.getAndIncrement();
		this(handler, threadName, capacity, false);
	}

	public MultiplexingQueue(Consumer<? super ELEMENT> handler) {
		var threadName = "multiplexing-queue-" + QUEUE_NAME_SUFFIX.getAndIncrement();
		this(handler, threadName, 1024, false);
	}

	private void startQueueTakingThread(String threadName) {
		Thread
				.ofVirtual()
				.name(threadName)
				.start(() -> {
					while (true) {
						try {
							var element = queue.take();
							handlers.forEach(handler -> handler.accept(element));
						} catch (InterruptedException ex) {
							// if the thread is interrupted, exit the loop (and let the thread die)
							break;
						} catch (Exception ex) {
							//TODO: handle error
							ex.printStackTrace();
						}
					}
				});
	}

	public void addListener(Consumer<? super ELEMENT> handler) {
		handlers.add(handler);
	}

	// collection-like API

	public void add(ELEMENT element) {
		queue.add(element);
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

}
