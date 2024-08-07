package dev.nipafx.ginevra.execution;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StoreQueryTracker {

	private final Set<String> storedTypes;
	private final Set<String> queryTypes;

	public StoreQueryTracker() {
		storedTypes = Collections.newSetFromMap(new ConcurrentHashMap<>());
		queryTypes = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	public void recordStore(Class<?> storedType) {
		storedTypes.add(storedType.getName());
	}

	public void recordQuery(Class<?> queryType) {
		queryTypes.add(queryType.getName());
	}

	/**
	 * @return whether only types that were exclusively used in queries changed
	 */
	public boolean onlyQueryTypes(List<? extends Class<?>> types) {
		return types.stream()
				.allMatch(type -> queryTypes.contains(type.getName()) && !storedTypes.contains(type.getName()));
	}

	public void reset() {
		storedTypes.clear();
		queryTypes.clear();
	}

}
