package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.Data;
import dev.nipafx.ginevra.outline.Document.StringData;

public interface StringDataStep<DATA extends Record & StringData> extends Step<DATA> {

	<DATA_OUT extends Record & Data>
	Step<DATA_OUT> transformMarkdown(Class<DATA_OUT> frontMatterType);

}
