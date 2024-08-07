package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.config.SiteConfiguration;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Template;

import java.util.ArrayList;
import java.util.List;

/**
 * @param changes The types that changed between compilations. Added and removed classes can be ignored.
 *                If no other class was modified, their addition/removal can only impact the build
 *                when the rest of the code uses advanced dynamic features (like a class path scan
 *                or service loader interaction) that is arguably out of scope of a static site build.
 */
record LiveCodeUpdate(Class<? extends SiteConfiguration> configType, Changes changes) {

	record Changes(
			List<Class<? extends Document>> documents,
			List<Class<? extends Template<?>>> templates,
			List<Class<? extends CustomElement>> components,
			List<Class<? extends CssStyle>> cssStyles,
			List<Class<?>> others) {

		@SuppressWarnings("unchecked")
		public static Changes forChangedTypes(List<Class<?>> allChangedTypes) {
			var documents = new ArrayList<Class<? extends Document>>();
			var templates = new ArrayList<Class<? extends Template<?>>>();
			var components = new ArrayList<Class<? extends CustomElement>>();
			var cssStyles = new ArrayList<Class<? extends CssStyle>>();
			var others = new ArrayList<Class<?>>();

			for (var changedType : allChangedTypes) {
				if (Document.class.isAssignableFrom(changedType))
					documents.add((Class<? extends Document>) changedType);
				else if (Template.class.isAssignableFrom(changedType))
					templates.add((Class<? extends Template<?>>) changedType);
				else if (CustomElement.class.isAssignableFrom(changedType))
					components.add((Class<? extends CustomElement>) changedType);
				else if (CssStyle.class.isAssignableFrom(changedType))
					cssStyles.add((Class<? extends CssStyle>) changedType);
				else
					others.add(changedType);
			}

			return new Changes(documents, templates, components, cssStyles, others);
		}

	}

}
