package dev.nipafx.ginevra.html;

import java.util.List;

public record Video(
		Id id, Classes classes, Src src, Integer height, Integer width, Src poster, Preload preload,
		Boolean autoplay, Boolean loop, Boolean muted, Boolean playInline,
		Boolean controls, Boolean disablePictureInPicture, Boolean disableRemotePlayback,
		List<? extends Element> children) implements HtmlElement {

	public Video() {
		this(
				Id.none(), Classes.none(), Src.none(), null, null, Src.none(), null,
				null, null, null, null, null, null, null, List.of());
	}

	public Video id(Id id) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video classes(Classes classes) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video src(Src src) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video height(Integer height) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video width(Integer width) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video poster(Src poster) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video preload(Preload preload) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video autoplay(Boolean autoplay) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video loop(Boolean loop) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video muted(Boolean muted) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video playInline(Boolean playInline) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video controls(Boolean controls) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video disablePictureInPicture(Boolean disablePictureInPicture) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video disableRemotePlayback(Boolean disableRemotePlayback) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video children(List<? extends Element> children) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, children);
	}

	public Video children(Element... children) {
		return new Video(
				id, classes, src, height, width, poster, preload,
				autoplay, loop, muted, playInline, controls, disablePictureInPicture, disableRemotePlayback, List.of(children));
	}

	public enum Preload {
		NONE, METADATA, AUTO;

		@Override
		public String toString() {
			return switch (this) {
				case NONE -> "none";
				case METADATA -> "metadata";
				case AUTO -> "auto";
			};
		}

	}

}
