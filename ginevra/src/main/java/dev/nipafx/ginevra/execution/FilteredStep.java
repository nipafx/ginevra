package dev.nipafx.ginevra.execution;

import dev.nipafx.ginevra.outline.Document;
import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Step;

import java.util.function.Predicate;

record FilteredStep
		<DATA_IN extends Record & Data, DATA_OUT extends Record & Data>
		(Predicate<Document<DATA_IN>> filter, Step<DATA_OUT> step) { }
