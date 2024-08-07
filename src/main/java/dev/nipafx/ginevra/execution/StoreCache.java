package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.util.RecordMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class StoreCache {

	private final ConcurrentMap<Class<? extends Record>, Record> rootCache;
	private final ConcurrentMap<String, Object> rootFieldCache;
	private final ConcurrentMap<CacheKey, Set<?>> collectionCache;

	public StoreCache() {
		rootCache = new ConcurrentHashMap<>();
		rootFieldCache = new ConcurrentHashMap<>();
		collectionCache = new ConcurrentHashMap<>();
	}

	public Record queryRoot(
			Class<? extends Record> queryType,
			Predicate<String> isCollection,
			QueryRootField queryRootField,
			QueryCollection queryCollection) {
		return rootCache.computeIfAbsent(queryType, _ -> {
			var valueMap = Stream
					.of(queryType.getRecordComponents())
					.map(component -> isCollection.test(component.getName())
							? findValueForCollectionComponent(component, queryCollection)
							: findValueForRootComponent(component, queryRootField))
					.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
			return RecordMapper.createRecordFromValueMap(queryType, valueMap);
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Entry<String, Set<?>> findValueForCollectionComponent(RecordComponent component, QueryCollection queryCollection) {
		if (!Collection.class.isAssignableFrom(component.getType()))
			throw new IllegalArgumentException("Root query fields that query a collection must be a collection type");

		try {
			var resultType = (Class) switch (component.getGenericType()) {
				case ParameterizedType param -> ByteArrayClassLoader
						.currentOrApp()
						.loadClass(param.getActualTypeArguments()[0].getTypeName());
				default -> throw new IllegalStateException("Unexpected generic type " + component.getGenericType());
			};
			Set<?> result = queryCollection(component.getName(), resultType, queryCollection);
			return Map.entry(component.getName(), result);
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Unexpected generic type " + component.getGenericType(), ex);
		}
	}

	private Entry<String, ?> findValueForRootComponent(RecordComponent component, QueryRootField queryRootField) {
		var result = rootFieldCache.computeIfAbsent(
				component.getName(),
				_ -> queryRootField.apply(component.getName(), component.getType()));
		return Map.entry(component.getName(), result);
	}

	@SuppressWarnings("unchecked")
	public <RESULT> Set<RESULT> queryCollection(String collection, Class<RESULT> type, QueryCollection query) {
		var key = new CacheKey(collection, type);
		return (Set<RESULT>) collectionCache.computeIfAbsent(key, _ -> query.apply(key.collection(), key.queryType()));
	}

	public void invalidateRoot() {
		rootCache.clear();
		rootFieldCache.clear();
	}

	public void invalidateCollection(String collection) {
		rootCache.clear();
		rootFieldCache.remove(collection);
		collectionCache.entrySet().removeIf(entry -> entry.getKey().collection().equals(collection));
	}

	public void invalidateAll() {
		rootCache.clear();
		rootFieldCache.clear();
		collectionCache.clear();
	}

	private record CacheKey(String collection, Class<?> queryType) { }

	public interface QueryRootField extends BiFunction<String, Class<?>, Object> { }
	public interface QueryCollection extends BiFunction<String, Class<?>, Set<?>> { }

}
