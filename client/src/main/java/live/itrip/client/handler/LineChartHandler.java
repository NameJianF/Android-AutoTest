package live.itrip.client.handler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import sun.swing.StringUIClientPropertyKey;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LineChartHandler {
    private LineChart<LocalDateTime, Number> lineChart;
    final ObservableList<XYChart.Data<LocalDateTime, Number>> dataset = FXCollections.observableArrayList();

    public LineChartHandler() {
    }

    public LineChart<LocalDateTime, Number> init(String seriesName, String yAxisName, String title, String chatStyle) {
        final StringConverter<LocalDateTime> stringConverter = new StringConverter<LocalDateTime>() {
            @Override
            public String toString(LocalDateTime localDateTime) {
//                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("mm:ss");
                return dtf.format(localDateTime);
            }

            @Override
            public LocalDateTime fromString(String s) {
                return LocalDateTime.parse(s);
            }
        };

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yAxisName);

        LocalDateTimeAxis xAxis = new LocalDateTimeAxis();
        xAxis.setTickLabelFormatter(stringConverter);

        XYChart.Series series = new XYChart.Series<LocalDateTime, Number>();
        series.setName(seriesName);
        series.setData(dataset);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setCreateSymbols(false); // hide dots
        lineChart.setTitle(title);
        lineChart.getData().add(series);
        lineChart.setAnimated(true);
        lineChart.setLegendVisible(false);
        if (StringUtils.isNotEmpty(chatStyle)) {
            lineChart.setStyle(chatStyle);
        }

        return lineChart;
    }

    public void addData(XYChart.Data<LocalDateTime, Number> data) {
        if (dataset.size() > 10) {
            dataset.remove(0);
        }
        dataset.add(data);
    }

    public void clearDatas() {
        dataset.clear();
    }
}
