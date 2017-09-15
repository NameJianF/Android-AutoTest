package live.itrip.client.service.performance;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;

import java.util.Random;

public class PerformanceService_bak {
    private static Object lock = new Object();
    private static PerformanceService_bak instance;

    public static PerformanceService_bak getInstance() {
        if (instance == null) {
            synchronized (lock) {
                instance = new PerformanceService_bak();
            }
        }
        return instance;
    }

    private ObservableList<XYChart.Data<Integer, Integer>> dataSource = FXCollections.observableArrayList();
    private boolean stop = false;

    public void dynamicLoadMemory(ScrollPane scrollPane, LineChart lineChart) {
        scrollPane.setPrefSize(600, 300);

//        lineChart.setTitle("Chart");
        lineChart.setAnimated(false);
        lineChart.setPrefSize(500, 200);

        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
//        xAxis.setAnimated(false);
        xAxis.setTickUnit(10);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(4);

        // datas
        XYChart.Series<Integer, Integer> aSeries = new XYChart.Series<>();
        aSeries.setName("Time(sec)");
        aSeries.setData(dataSource);
        lineChart.getData().add(aSeries);
        getChartData();

        scrollPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) {
                lineChart.setMinSize(Math.max(lineChart.getPrefWidth(), newBounds.getWidth()), Math.max(lineChart.getPrefHeight(), newBounds.getHeight()));
                scrollPane.setPannable((lineChart.getPrefWidth() > newBounds.getWidth()) || (lineChart.getPrefHeight() > newBounds.getHeight()));
            }
        });
        lineChart.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent ev) {
                double zoomFactor = 1.05;
                double deltaY = ev.getDeltaY();

                if (deltaY < 0) {
                    zoomFactor = 2 - zoomFactor;
                }

                System.out.println("DeltaX = " + ev.getDeltaX());
                System.out.println("DeltaY = " + ev.getDeltaY());
                System.out.println("Zoomfactor = " + zoomFactor);

                NumberAxis xAxisLocal = ((NumberAxis) lineChart.getXAxis());

                xAxisLocal.setUpperBound(xAxisLocal.getUpperBound() * zoomFactor);
                xAxisLocal.setLowerBound(xAxisLocal.getLowerBound() * zoomFactor);
                xAxisLocal.setTickUnit(xAxisLocal.getTickUnit() * zoomFactor);

                ev.consume();
            }
        });
    }

    int aValue = 10;
    int i = 1;

    private void getChartData() {
        Random random = new Random(100);
        new Thread(() -> {
            while (!stop) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    aValue += random.nextInt(50);
                    dataSource.add(new XYChart.Data(i, aValue));
                    i++;
                    aValue = 10;
                });
            }
        }).start();

    }
}
