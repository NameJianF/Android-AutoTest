package live.itrip.client.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

public class TestLinechart {
    private XYChart.Series<Number, Number> hourDataSeries;
    private NumberAxis xAxis;
    private Timeline animation;
    private double hours = 0;
    private double timeInHours = 0;
    private double prevY = 10;
    private double y = 10;
    private int minutes = 0;

    public TestLinechart(LineChart lineChartMemory) {
        // timeline to add new data every 60th of a second
        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / 60), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // 6 minutes data per frame
                for (int count = 0; count < 60; count++) {
                    nextTime();
                    plotTime();
                }
            }
        }));
        animation.setCycleCount(Animation.INDEFINITE);
//        xAxis = new NumberAxis(0, 24, 3);
//        final NumberAxis yAxis = new NumberAxis(0, 100, 10);
//        final LineChart<Number, Number> lc = new LineChart<Number, Number>(xAxis, yAxis);

        xAxis = (NumberAxis) lineChartMemory.getXAxis();
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(24);
        xAxis.setTickUnit(3);
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = (NumberAxis) lineChartMemory.getYAxis();
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);
        yAxis.setTickUnit(10);
        yAxis.setAutoRanging(true);

        lineChartMemory.setCreateSymbols(false);
        lineChartMemory.setAnimated(false);
        lineChartMemory.setLegendVisible(false);
        lineChartMemory.setTitle("ACME Company Stock");

        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
        yAxis.setLabel("Share Price");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "$", null));

        hourDataSeries = new XYChart.Series<Number, Number>();
        hourDataSeries.setName("Hourly Data");
        hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
        lineChartMemory.getData().add(hourDataSeries);

        animation.play();
    }

    private void nextTime() {
        if (minutes == 59) {
            hours++;
            minutes = 0;
        } else {
            minutes++;
        }
        timeInHours = hours + ((1d / 60d) * minutes);
    }

    private void plotTime() {
        if ((timeInHours % 1) == 0) {
            // change of hour
            double oldY = y;
            y = prevY - 10 + (Math.random() * 20);
            prevY = oldY;
            while (y < 10 || y > 90) y = y - 10 + (Math.random() * 20);
            hourDataSeries.getData().add(new XYChart.Data<Number, Number>(timeInHours, prevY));
            // after 25hours delete old data
            if (timeInHours > 25) hourDataSeries.getData().remove(0);
            // every hour after 24 move range 1 hour
            if (timeInHours > 24) {
                xAxis.setLowerBound(xAxis.getLowerBound() + 1);
                xAxis.setUpperBound(xAxis.getUpperBound() + 1);
            }
        }
    }
}
