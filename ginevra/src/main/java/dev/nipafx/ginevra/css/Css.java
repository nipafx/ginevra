package dev.nipafx.ginevra.css;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.util.SHA256;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.stream.Stream;

public class Css {

	private static final String STYLE_COMPONENT_NAME = "style";

	public static <STYLE extends Record & CssStyle> STYLE parse(Class<STYLE> type, String css) {
		Class<?>[] componentTypes = Stream
				.of(type.getRecordComponents())
				.peek(component -> {
					if (component.getType() == Id.class || component.getType() == Classes.class)
						return;
					if (component.getType() == String.class && component.getName().equals(STYLE_COMPONENT_NAME))
						return;

					var message = STR."""
						CSS record components must be of types `Id` or `Classes` \
						except for one `String \{STYLE_COMPONENT_NAME}` component \
						but '\{component}' is neither.""";
					throw new IllegalArgumentException(message);
				})
				.map(RecordComponent::getType)
				.toArray(Class[]::new);

		var prefix = createCssPrefix(type, css);
		var replacedCss = replaceNamesInCss(type, css, prefix);

		Object[] componentValues = Stream
				.of(type.getRecordComponents())
				.map(component -> {
					if (component.getType() == Id.class)
						return Id.of(prefix + component.getName());
					if (component.getType() == Classes.class)
						return Classes.of(prefix + component.getName());
					if (component.getType() == String.class)
						return replacedCss;
					throw new IllegalStateException();
				})
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

	private static <STYLE extends Record & CssStyle> String replaceNamesInCss(Class<STYLE> type, String css, String prefix) {
		var replacedCss = css;
		for (RecordComponent component : type.getRecordComponents()) {
			if (component.getType() == String.class)
				continue;

			var name = component.getName();
			var cssChar = component.getType() == Id.class ? '#' : '.';
			replacedCss = replacedCss.replaceAll(
					STR."[\{cssChar}]\{name}([\\W\\d-_]+|$)",
					STR."\{cssChar}\{prefix}\{name}$1");
		}
		return replacedCss;
	}

}
