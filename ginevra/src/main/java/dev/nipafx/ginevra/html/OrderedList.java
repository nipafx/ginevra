package dev.nipafx.ginevra.html;

import java.util.List;

public record OrderedList(String id, Classes classes, Integer start, List<ListItem> children) implements HtmlElement {

	public OrderedList {
		children = List.copyOf(children);
	}

	public OrderedList() {
		this(null, Classes.none(), null, List.of());
	}

	public OrderedList id(String id) {
		return new OrderedList(id, this.classes, this.start, this.children);
	}

	public OrderedList classes(Classes classes) {
		return new OrderedList(this.id, classes, this.start, this.children);
	}

	public OrderedList start(Integer start) {
		return new OrderedList(this.id, this.classes, start, this.children);
	}

	public OrderedList children(List<ListItem> children) {
		return new OrderedList(this.id, this.classes, this.start, children);
	}

	public OrderedList children(ListItem... children) {
		return new OrderedList(this.id, this.classes, this.start, List.of(children));
	}

}
