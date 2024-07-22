package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.html.Classes;
import dev.nipafx.ginevra.html.Element;
import dev.nipafx.ginevra.html.Id;
import dev.nipafx.ginevra.html.Src;
import dev.nipafx.ginevra.html.Video;
import dev.nipafx.ginevra.html.Video.Preload;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nipafx.ginevra.html.HtmlElement.video;
import static org.assertj.core.api.Assertions.assertThat;

class VideoRendererTest {

	private static final String TAG = "video";

	static class TestBasics implements HtmlRendererTest.TestBasics {

		@Override
		public String tag() {
			return TAG;
		}

	}

	@Nested
	class IdAndClasses extends TestBasics implements HtmlRendererTest.IdAndClasses<Video> {

		@Override
		public Video createWith(Id id, Classes classes) {
			return video.id(id).classes( classes);
		}

	}

	@Nested
	class Children extends TestBasics implements HtmlRendererTest.Children<Video> {

		@Override
		public Video createWith(Element... children) {
			return video.children(children);
		}

	}

	@Nested
	class VideoProperties extends TestBasics {

		@Test
		void without() {
			var element = video;
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<video></video>
					""");
		}

		@Test
		void withSrc() {
			var element = video.src(Src.direct("url"));
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<video src="url"></video>
					""");
		}

		@Test
		void withAll() {
			var element = video
					.src(Src.direct("src-url"))
					.height(320)
					.width(240)
					.poster(Src.direct("poster-url"))
					.preload(Preload.METADATA)
					.autoplay(true)
					.loop(false)
					.muted(true)
					.playInline(false)
					.controls(true)
					.disablePictureInPicture(false)
					.disableRemotePlayback(true);
			var rendered = renderer().render(element);

			assertThat(rendered).isEqualTo("""
					<video src="src-url" height="320" width="240" poster="poster-url" preload="metadata" autoplay="true" loop="false" muted="true" playinline="false" disablepictureinpicture="false" disableremoteplayback="true" controls></video>
					""");
		}

	}

}