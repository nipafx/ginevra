package dev.nipafx.ginevra.render;

import dev.nipafx.ginevra.css.CssStyled;
import dev.nipafx.ginevra.html.CustomElement;
import dev.nipafx.ginevra.html.Link;
import dev.nipafx.ginevra.util.SHA256;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static dev.nipafx.ginevra.html.HtmlElement.link;

class CssRenderer {

	private final Path rootFolder;
	private final ConcurrentHashMap<String, CssFile> files;

	public CssRenderer(Path rootFolder) {
		this.rootFolder = rootFolder;
		this.files = new ConcurrentHashMap<>();
	}

	public Optional<Link> process(CustomElement element) {
		return (element instanceof CssStyled styled)
				? Optional.of(processStyle(styled))
				: Optional.empty();
	}

	private Link processStyle(CssStyled styled) {
		var css = getCssFile(styled);
		return link
				.href(css.file().toString())
				.rel("stylesheet");
	}

	private CssFile getCssFile(CssStyled styled) {
		var style = styled.style();
		var contentHash = SHA256.hash(style.style());
		return files.computeIfAbsent(contentHash, _ -> {
			var baseName = style.getClass().getName().replaceAll("[.$]", "-");
			var fileName = STR."\{baseName}--\{contentHash}.css";
			var file = Path.of(fileName);
			return new CssFile(rootFolder.resolve(file), style.style());
		});
	}

	public Stream<CssFile> cssFiles() {
		return files.values().stream();
	}

}
