package dev.nipafx.site.nipafx_dev.data;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.site.nipafx_dev.Main;

public record SiteData(String title) implements Data {

	public static SiteData create() {
		return new SiteData("nipafx // You. Me. Java.");
	}

}
