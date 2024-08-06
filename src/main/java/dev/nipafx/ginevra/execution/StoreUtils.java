package dev.nipafx.ginevra.execution;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

class StoreUtils {

	public static Map<String, ?> queryRootOrCollection(
			Class<? extends Record> queryType,
			Predicate<String> isCollection,
			BiFunction<String, Class<?>, ?> queryRoot, BiFunction<String, Class<?>, Set<?>> queryCollection) {
		return Stream
				.of(queryType.getRecordComponents())
				.map(component -> {
							if (isCollection.test(component.getName())) {
								if (!Collection.class.isAssignableFrom(component.getType()))
									throw new IllegalArgumentException("Root query fields that query a collection must be a collection type");

								try {
									var type = switch (component.getGenericType()) {
										case ParameterizedType param -> ByteArrayClassLoader
												.currentOrApp()
												.loadClass(param.getActualTypeArguments()[0].getTypeName());
										default ->
												throw new IllegalStateException("Unexpected generic type " + component.getGenericType());
									};
									Set<?> result = queryCollection.apply(component.getName(), type);
									return Map.entry(component.getName(), result);
								} catch (ClassNotFoundException ex) {
									throw new IllegalStateException("Unexpected generic type " + component.getGenericType(), ex);
								}
							} else {
								var result = queryRoot.apply(component.getName(), component.getType());
								return Map.entry(component.getName(), result);
							}
						}
				)
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}

}
