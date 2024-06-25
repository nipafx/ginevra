package dev.nipafx.ginevra.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class RecordMapper {

	public static <TYPE extends Record> TYPE createRecordFromRecord(Class<TYPE> type, Record instance) {
		return createRecordFromValueMap(type, createValueMapFromRecord(instance));
	}

	public static Map<String, Object> createValueMapFromRecord(Record instance) {
		return Stream
				.of(instance.getClass().getRecordComponents())
				.collect(toMap(
						RecordComponent::getName,
						component -> getComponentValue(instance, component)));
	}

	private static Object getComponentValue(Record instance, RecordComponent component) {
		try {
			var componentValue = component.getAccessor().invoke(instance);
			if (component.getType().isRecord())
				return createValueMapFromRecord((Record) componentValue);
			else
				return componentValue;
		} catch (IllegalAccessException ex) {
			var message = STR."Record '\{instance.getClass().getName()}' is inaccessible.";
			throw new IllegalStateException(message, ex);
		} catch (InvocationTargetException ex) {
			var message = STR."Invoking accessor of component '\{instance.getClass().getName()}#\{component.getName()}' failed.";
			throw new IllegalStateException(message, ex);
		}
	}

	public static <TYPE extends Record> TYPE createRecordFromValueMap(Class<TYPE> type, Map<String, ?> values) {
		var constructorArguments = Stream.of(type.getRecordComponents())
				.map(component -> createValueForRecordComponent(component, values.get(component.getName())))
				.toArray();
		return createRecordFromValues(type, constructorArguments);
	}

	private static Object createValueForRecordComponent(RecordComponent component, Object value) {
		if (component.getType().isRecord()) {
			@SuppressWarnings("unchecked")
			var nestedRecordType = (Class<? extends Record>) component.getType();
			if (!(guaranteeNotNull(value, component) instanceof Map)) {
				var message = STR."The component '\{component.getName()}' is of type '\{component.getType()}' but the associated map value '\{value}' is no 'Map'";
				throw new IllegalArgumentException(message);
			}
			@SuppressWarnings("unchecked")
			var nestedRecordValues = (Map<String, Object>) guaranteeNotNull(value, component);
			return createRecordFromValueMap(nestedRecordType, nestedRecordValues);
		}

		return switch (component.getGenericType()) {
			case Class<?> classType -> classType.cast(guaranteeNotNull(value, component));
			case ParameterizedType paramType -> switch (paramType.getRawType().getTypeName()) {
				case "java.util.Optional" -> {
					if (value instanceof Optional)
						yield guaranteeNotNull(value, component);
					if (value == null)
						yield Optional.empty();

					var valueType = paramType.getActualTypeArguments()[0];
					yield Optional.of(guaranteeCorrectType(value, valueType));
				}
				case "java.util.List" -> {
					if (value instanceof List)
						yield guaranteeNotNull(value, component);
					if (value == null)
						yield List.of();

					var valueType = paramType.getActualTypeArguments()[0];
					yield List.of(guaranteeCorrectType(value, valueType));
				}
				case "java.util.Set" -> {
					if (value instanceof Set)
						yield guaranteeNotNull(value, component);
					if (value == null)
						yield Set.of();

					var valueType = paramType.getActualTypeArguments()[0];
					yield Set.of(guaranteeCorrectType(value, valueType));
				}
				default -> guaranteeNotNull(value, component);
			};
			case null, default ->
					throw new IllegalStateException("Unexpected component type: " + component.getGenericType());
		};
	}

	private static <T> T guaranteeNotNull(T value, RecordComponent component) {
		if (value == null) {
			var message = STR."No values are defined for the component '\{component.getName()}' of type '\{component.getType()}'";
			throw new IllegalArgumentException(message);
		}
		return value;
	}

	private static Object guaranteeCorrectType(Object value, Type type) {
		try {
			var correctType = Class
					.forName(type.getTypeName())
					.isInstance(value);
			if (!correctType) {
				String message = STR."Value '\{value}' is supposed to be of type '\{type}' but isn't";
				throw new IllegalArgumentException(message);
			}
			return value;
		} catch (ClassNotFoundException ex) {
			String message = STR."Value '\{value}' is supposed to be of type '\{type}' but the type wasn't found";
			throw new IllegalStateException(message, ex);
		}
	}

	private static <TYPE extends Record> TYPE createRecordFromValues(Class<TYPE> type, Object[] constructorArguments) {
		var constructorParameters = Stream.of(type.getRecordComponents())
				.map(RecordComponent::getType)
				.toArray(Class[]::new);
		try {
			var constructor = type.getConstructor(constructorParameters);
			return constructor.newInstance(constructorArguments);
		} catch (ReflectiveOperationException ex) {
			var message = STR."Instantiating '\{type}' failed";
			throw new IllegalStateException(message, ex);
		}
	}

	public static <TYPE extends Record> TYPE createRecordFromStringListMap(Class<TYPE> type, Map<String, List<String>> stringValues) {
		var constructorArguments = Stream.of(type.getRecordComponents())
				.map(component -> parseValueForComponent(component, stringValues.get(component.getName())))
				.toArray();
		return createRecordFromValues(type, constructorArguments);
	}

	private static Object parseValueForComponent(RecordComponent component, List<String> values) {
		return switch (component.getGenericType()) {
			case Class<?> classType -> {
				if (guaranteeNotNull(values, component).size() != 1) {
					var message = STR."\{values.size()} values are defined for the component '\{component.getName()}' of type '\{component.getType()} but there should be exactly 1.";
					throw new IllegalArgumentException(message);
				}
				yield parseValue(classType, values.getFirst());
			}
			case ParameterizedType paramType -> switch (paramType.getRawType().getTypeName()) {
				case "java.util.Optional" -> values == null
						? Optional.empty()
						: switch (values.size()) {
							case 0 -> Optional.empty();
							case 1 -> Optional.of(parseValue(paramType.getActualTypeArguments()[0], values.getFirst()));
							default -> {
								var message = STR."\{values.size()} values are defined for the component '\{component.getName()}' of type '\{component.getType()} but there should be 0 or 1.";
								throw new IllegalArgumentException(message);
							}
				};
				case "java.util.List" -> values == null
						? List.of()
						: values.stream()
								.map(value -> parseValue(paramType.getActualTypeArguments()[0], value))
								.toList();
				case "java.util.Set" -> values == null
						? Set.of()
						: values.stream()
								.map(value -> parseValue(paramType.getActualTypeArguments()[0], value))
								.collect(toUnmodifiableSet());
				default -> throw new IllegalStateException("Unexpected component type: " + paramType);
			};
			case null, default ->
					throw new IllegalStateException("Unexpected component type: " + component.getGenericType());
		};
	}

	private static Object parseValue(Type type, String value) {
		return switch (type.getTypeName()) {
			case "java.lang.String" -> value;
			case "java.lang.Path" -> Path.of(value);
			case "java.lang.Integer", "int" -> Integer.parseInt(value);
			case "java.lang.Long", "long" -> Long.parseLong(value);
			case "java.lang.Float", "float" -> Float.parseFloat(value);
			case "java.lang.Double", "double" -> Double.parseDouble(value);
			case "java.lang.Boolean", "boolean" -> Boolean.parseBoolean(value);
			default -> {
				var message = "The unsupported type %s (with value '%s') was encountered after those should already have been detected.";
				throw new IllegalStateException(message.formatted(type, value));
			}
		};
	}

	public static <TYPE extends Record> TYPE createRecordFromMaps(Class<TYPE> type, Map<String, ?> values, Map<String, List<String>> stringValues) {
		var constructorArguments = Stream.of(type.getRecordComponents())
				.map(component -> values.containsKey(component.getName())
						? createValueForRecordComponent(component, values.get(component.getName()))
						: parseValueForComponent(component, stringValues.get(component.getName())))
				.toArray();
		return createRecordFromValues(type, constructorArguments);
	}

}
