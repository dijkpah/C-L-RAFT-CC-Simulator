package nl.utwente.simulator.output;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.utwente.simulator.config.Settings.*;

public class CSVGenerator {

    private CSVColumn[] columnNames;
    private List<Map<CSVColumn, Object>> data = new LinkedList<>();

    public CSVGenerator(CSVColumn[] columnNames){
        this.columnNames = columnNames;
    }

    public void addRow(Map<CSVColumn, Object> columnValues){
        data.add(columnValues);
    }

    public void export() throws IOException {
        String contents = Arrays.asList(columnNames).stream()
                .map(CSVColumn::toString)
                .collect(Collectors.joining(CSV_DELIMITER))
                +LINE_END;

        for(Map<CSVColumn, Object> entry : data){
            for(CSVColumn column : columnNames){
                String s = entry.get(column) == null
                        ? ""
                        : ""+entry.get(column);
                contents += s+ CSV_DELIMITER;
            }
            contents += LINE_END;
        }
        FileWriter.export("log", "csv", contents);
    }
}
