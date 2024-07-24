package dev.nipafx.ginevra.site.documents;

import dev.nipafx.ginevra.outline.Document;

import java.util.List;

public record LandingPageDoc(String title, List<LandingPageText.Parsed> landingPageTexts) implements Document { }
