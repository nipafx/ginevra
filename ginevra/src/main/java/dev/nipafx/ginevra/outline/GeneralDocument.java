package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;

public record GeneralDocument<DATA extends Record & Data>(Id id, DATA data) implements Document<DATA> { }
