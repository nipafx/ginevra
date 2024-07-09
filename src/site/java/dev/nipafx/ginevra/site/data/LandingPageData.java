package dev.nipafx.ginevra.site.data;

import dev.nipafx.ginevra.outline.Document;

import java.util.List;

public record LandingPageData(String title, List<LandingPageText.Parsed> landingPageTexts) implements Document { }
