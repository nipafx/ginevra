package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.config.SiteConfiguration;

import java.util.ArrayList;
import java.util.List;

record LiveCodeUpdate(Class<? extends SiteConfiguration> configType, Rebuild rebuild) {

	sealed interface Rebuild {

		record None() implements Rebuild { }
		record Components() implements Rebuild { }
		record Templates(List<Class<?>> templates) implements Rebuild {

			Templates addTemplate(Class<?> template) {
				var templates = new ArrayList<>(this.templates);
				templates.add(template);
				return new Templates(templates);
			}

		}
		record Full() implements Rebuild { }

	}

}
