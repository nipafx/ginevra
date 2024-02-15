package dev.nipafx.ginevra.css;

import dev.nipafx.ginevra.util.SHA256;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class Css {

	private static final String STYLE_COMPONENT_NAME = "style";

	public static <STYLE extends Record & CssStyle> STYLE parse(Class<STYLE> type, String css) {
		Class<?>[] componentTypes = Stream
				.of(type.getRecordComponents())
				.peek(component -> {
					if (component.getType() != String.class) {
						var message = STR."CSS records must have all string components but '\{component}' isn't.";
						throw new IllegalArgumentException(message);
					}
				})
				.map(RecordComponent::getType)
				.toArray(Class[]::new);


		var prefix = createCssPrefix(type, css);
		Map<String, String> componentsToCssClass = Stream
				.of(type.getRecordComponents())
				.map(RecordComponent::getName)
				.filter(componentName -> !componentName.equals(STYLE_COMPONENT_NAME))
				.collect(toMap(identity(), componentName -> prefix + componentName));

		var replacedCss = new AtomicReference<>(css);
		componentsToCssClass
				.forEach((componentName, cssName) -> replacedCss.getAndUpdate(_css -> _css
						.replaceAll("([.#])" + componentName + "([\\W\\d-_]+|$)", "$1" + cssName + "$2")));

		Object[] componentValues = Stream
				.of(type.getRecordComponents())
				.map(RecordComponent::getName)
				.map(componentName -> componentName.equals(STYLE_COMPONENT_NAME)
						? replacedCss.get()
						: componentsToCssClass.get(componentName))
				.toArray();

		try {
			return type.getConstructor(componentTypes).newInstance(componentValues);
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new IllegalArgumentException("Could not invoke constructor", ex);
		} catch (InvocationTargetException ex) {
			throw new IllegalArgumentException("Constructor invocation failed", ex);
		} catch (NoSuchMethodException ex) {
			var message = "A record must have a constructor that matches its components: " + Arrays.toString(componentValues);
			throw new IllegalStateException(message, ex);
		}
	}

	private static <STYLE extends Record & CssStyle> String createCssPrefix(Class<STYLE> type, String css) {
		var typeNameInCss = type.getName().replaceAll("[.$]", "-");
		var cssHash = SHA256.hash(css);
		return STR."\{typeNameInCss }--\{cssHash}--";
	}

}
