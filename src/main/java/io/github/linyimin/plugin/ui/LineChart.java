package io.github.linyimin.plugin.ui;

import com.intellij.ui.JBColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author banzhe
 * @date 2022/12/28 22:46
 **/
public class LineChart {


    private ChartPanel chartPanel;
    private JFreeChart chart;
    private final String title;
    private final String xAxisLabel;
    private final String yAxisLabel;

    public LineChart(String title, String xAxisLabel, String yAxisLabel) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        initUI();
    }

    private void initUI() {
        XYDataset dataset = createDataset();
        this.chart = createChart(dataset);

        this.chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    }

    private XYDataset createDataset() {

        TimeSeries series = new TimeSeries(title);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }

    private JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xAxisLabel, yAxisLabel, dataset, true, true, false );

        XYPlot plot = chart.getXYPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, JBColor.RED);

        renderer.setBaseShapesVisible(false);

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(JBColor.WHITE);

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(JBColor.BLACK);

        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(JBColor.BLACK);

        chart.getLegend().setFrame(BlockBorder.NONE);

        chart.setTitle(new TextTitle(title, new Font("Serif", java.awt.Font.BOLD, 13)));

        return chart;

    }

    public ChartPanel getChartPanel() {
        return this.chartPanel;
    }

    public void updateDataset(Map<Long, Double> data) {

        TimeSeries series = new TimeSeries(title);

        TimeSeriesCollection dataset = new TimeSeriesCollection();

        for (Map.Entry<Long, Double> entry : data.entrySet()) {
            Second second = new Second(new Date(entry.getKey()));
            series.addOrUpdate(second, entry.getValue());
        }

        int multiple = Math.max(data.size() / 5, 1);

        dataset.addSeries(series);

        XYPlot xyPlot = chart.getXYPlot();

        DateAxis dateAxis = (DateAxis) xyPlot.getDomainAxis();
        dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, multiple, new SimpleDateFormat("HH:mm:ss")));

        NumberAxis numberAxis = (NumberAxis)xyPlot.getRangeAxis();

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumFractionDigits(2);
        double min = data.values().stream().min(Double::compareTo).orElse(0.0);
        double max = data.values().stream().max(Double::compareTo).orElse(0.0);

        if (max == min) {
            max = max * 1.20;
            min = min * 0.80;
        }

        int size = Math.max((int) ((max - min) / 10), 10);

        numberAxis.setTickUnit(new NumberTickUnit(size, numberFormat));
        numberAxis.setLowerBound(min);
        numberAxis.setUpperBound(max);

        chart.getXYPlot().setDataset(dataset);
    }

}
