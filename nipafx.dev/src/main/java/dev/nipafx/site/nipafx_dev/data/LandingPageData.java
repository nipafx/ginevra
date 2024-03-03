package dev.nipafx.site.nipafx_dev.data;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.site.nipafx_dev.data.ArticleData.Page;

import java.util.List;

public record LandingPageData(String title, List<Page> articles) implements Data { }
