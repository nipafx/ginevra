package dev.nipafx.ginevra.outline;

import dev.nipafx.ginevra.outline.Document.FileData;
import dev.nipafx.ginevra.outline.Document.StringData;

public interface TextFileDataStep<DATA extends Record & FileData & StringData> extends FileDataStep<DATA>, StringDataStep<DATA> {

}
