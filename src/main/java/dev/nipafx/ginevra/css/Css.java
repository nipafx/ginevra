package dev.nipafx.ginevra.css;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.Src;
import dev.nipafx.ginevra.util.SHA256;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public record Css(List<String> fragments, List<Src> sources) {

	public Css {
		fragments = List.copyOf(fragments);
		sources = List.copyOf(sources);
	}

	public String interpolateSources() {
		var builder = new StringBuilder(fragments().getFirst());
		for (int i = 0; i < sources.size(); i++) {
			builder.append(sources.get(i).path());
			builder.append(fragments.get(i + 1));
		}
		return builder.toString();
	}

	public Css replaceSources(UnaryOperator<Src> replace) {
		var replacedSources = sources.stream()
				.map(replace)
				.toList();
		return new Css(fragments, replacedSources);
	}

	/* --- STATIC FACTORY --- */

	private static final String STYLE_COMPONENT_NAME = "css";

	public static <STYLE extends Record & CssStyle> STYLE parse(Class<STYLE> type, String cssFragment, Object... cssSourcesAndFragments) {
		// verify component types first
		var componentTypes = verifyAndExtractComponentTypes(type);

		var css = Css.fromVarArgs(cssFragment, cssSourcesAndFragments);
		var prefix = css.createCssPrefix(type);
		var names = css.identifyNames(type);
		var modularizedCss = css.applyPrefixToNames(names, prefix);

		var componentValues = modularizedCss.createComponentValues(type, prefix);
		return instantiateStyle(type, componentTypes, componentValues);
	}

	private static Css fromVarArgs(String cssFragment, Object... cssSourcesAndFragments) {
		var fragments = new ArrayList<String>();
		var sources = new ArrayList<Src>();

		fragments.add(cssFragment);
		for (int i = 0; i < cssSourcesAndFragments.length; i++)
			// the varargs argument starts with sources
			if (i % 2 == 0)
				if (cssSourcesAndFragments[i] instanceof Src source)
					sources.add(source);
				else
					throw new IllegalArgumentException(getErrorMessage(cssSourcesAndFragments, i));
			else
				if (cssSourcesAndFragments[i] instanceof String fragment)
					fragments.add(fragment);
				else
					throw new IllegalArgumentException(getErrorMessage(cssSourcesAndFragments, i));

		return new Css(fragments, sources);
	}

	private static String getErrorMessage(Object[] args, int index) {
		String text = "CSS templates must start with a `String` fragment and then alternate between `Src` and `String` instances but this one didn't at index %d: [ String, ".formatted(index + 1);
		return Stream.of(args)
				.map(arg -> arg.getClass().getSimpleName())
				.collect(joining(", ", text, " ]"));
	}

	private <STYLE extends Record & CssStyle> String createCssPrefix(Class<STYLE> type) {
		var typeNameInCss = type.getName().replaceAll("[.$]", "-");
		var cssHash = SHA256.hash(interpolateSources());
		return STR."\{typeNameInCss }--\{cssHash}--";
	}

	private <STYLE extends Record & CssStyle> List<Name> identifyNames(Class<STYLE> type) {
		return Stream
				.of(type.getRecordComponents())
				.filter(component -> component.getType() != Css.class)
				.map(component ->  new Name(component.getType() == Id.class ? '#' : '.', component.getName()))
				.toList();
	}

	private Css applyPrefixToNames(List<Name> names, String prefix) {
		var modularizedFragments = fragments.stream();
		for (var name : names) {
			modularizedFragments = modularizedFragments
					.map(fragment -> fragment.replaceAll(
							"[" + name.identifier() + "]" + name.name() + "([\\W\\d-_]+|$)",
							name.identifier() + prefix + name.name() + "$1"));
		}
		return new Css(modularizedFragments.toList(), sources);
	}

	private <STYLE extends Record & CssStyle> Object[] createComponentValues(Class<STYLE> type, String prefix) {
		return Stream
				.of(type.getRecordComponents())
				.map(component -> {
					if (component.getType() == Id.class)
						return Id.of(prefix + component.getName());
					if (component.getType() == Classes.class)
						return Classes.of(prefix + component.getName());
					if (component.getType() == Css.class)
						return this;
					throw new IllegalStateException();
				})
				.toArray();
	}

	private static <STYLE extends Record & CssStyle> Class<?>[] verifyAndExtractComponentTypes(Class<STYLE> type) {
		return Stream
				.of(type.getRecordComponents())
				.peek(component -> {
					if (component.getType() == Id.class || component.getType() == Classes.class)
						return;
					if (component.getType() == Css.class && component.getName().equals(STYLE_COMPONENT_NAME))
						return;

					var message = STR."""
						CSS record components must be of types `Id` or `Classes` \
						except for one `Css \{STYLE_COMPONENT_NAME}` component \
						but '\{component}' is neither.""";
					throw new IllegalArgumentException(message);
				})
				.map(RecordComponent::getType)
				.toArray(Class[]::new);
	}

	private static <STYLE extends Record & CssStyle> STYLE instantiateStyle(Class<STYLE> type, Class<?>[] componentTypes, Object[] componentValues) {
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
	private record Name(char identifier, String name) { }

}
