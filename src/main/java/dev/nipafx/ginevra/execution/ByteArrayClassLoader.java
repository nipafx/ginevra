package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.Ginevra;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

class ByteArrayClassLoader extends ClassLoader {

	private static final AtomicReference<Optional<ByteArrayClassLoader>> CURRENT =
			new AtomicReference<>(Optional.empty());

	private final Map<String, byte[]> byteCode;
	private final ConcurrentMap<String, Class<?>> classes;

	ByteArrayClassLoader(ClassLoader parent, Map<String, byte[]> byteCode) {
		super("site", parent);
		this.byteCode = Map.copyOf(byteCode);
		this.classes = new ConcurrentHashMap<>();
	}

	@Override
	public Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
		if (byteCode.containsKey(className))
			return classes.computeIfAbsent(
					className,
					name -> defineClass(name, byteCode.get(name), 0, byteCode.get(name).length));
		return super.loadClass(className, resolve);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (byteCode.containsKey(name))
			return defineClass(name, byteCode.get(name), 0, byteCode.get(name).length);
		throw new ClassNotFoundException(name);
	}

	public Map<String, byte[]> byteCode() {
		return byteCode;
	}

	public static Optional<ByteArrayClassLoader> current() {
		return CURRENT.get();
	}

	public static ClassLoader currentOrApp() {
		var classLoader = CURRENT.get();
		return classLoader.isPresent()
				? classLoader.get()
				: Ginevra.class.getClassLoader();
	}

	public static Optional<ByteArrayClassLoader> swap(ByteArrayClassLoader next) {
		return CURRENT.getAndSet(Optional.ofNullable(next));
	}

}
