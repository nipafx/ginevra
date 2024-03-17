package dev.nipafx.site.nipafx_dev.components;

import dev.nipafx.ginevra.css.Css;
import dev.nipafx.ginevra.css.CssStyle;
import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.CustomSingleElement;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.Span;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.html.HtmlElement.div;
import static dev.nipafx.ginevra.html.HtmlElement.h1;
import static dev.nipafx.ginevra.html.HtmlElement.hr;
import static dev.nipafx.ginevra.html.HtmlElement.p;
import static dev.nipafx.ginevra.html.HtmlElement.span;

public record Header(String title, String description, String head, Optional<ChannelTags> channelTags)
		implements CustomSingleElement, CssStyled<Header.Style> {

	public record ChannelTags(String channel, List<String> tags) { }

	public record Style(
			Classes container, Id title, Id description, Id head, Id tags, Classes channel, Classes divider,
			Classes tag, String style) implements CssStyle { }

	private static final Style STYLE = Css.parse(Style.class, """
			.container {
				display: flex;
				flex-direction: column;
				align-items: center;
				gap: 18px;
				margin: 1em 0 0.5em;
			}
			
			#head {
				margin: 0;
				font-size: 0.8em;
				color: #aaa;
			}
			
			#title {
				margin: 0;
				font-size: 2.4em;
				text-align: center;
				font-weight: bold;
			}
			
			#tags {
				display: flex;
				gap: 12px;
				width: fit-content;
				font-family: monospace;
			}
			
			.channel {
				color: #69ea7d;
			}
			
			#description {
				margin: 0;
				font-size: 1.2em;
			}
			""");

	@Override
	public Style style() {
		return STYLE;
	}

	@Override
	public Element composeSingle() {
		return div.classes(STYLE.container).children(
				p.id(STYLE.head).text(head),
				h1.id(STYLE.title).text(title),
				div.id(STYLE.tags).children(tagSpans()),
				p.id(STYLE.description).text(description),
				hr);
	}

	private List<Span> tagSpans() {
		if (channelTags.isEmpty())
			return List.of();

		var ch = Stream.of(
				span.classes(STYLE.channel).text("#" + channelTags.get().channel),
				span.classes(STYLE.divider).text("//"));
		var t = channelTags.get().tags.stream().map(tag -> span.classes(STYLE.tag).text("#" + tag));
		return Stream.concat(ch, t).toList();
	}

}
