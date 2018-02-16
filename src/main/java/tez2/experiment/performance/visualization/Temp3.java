package tez2.experiment.performance.visualization;

import java.util.Calendar;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import scala.Char;

public class Temp3 extends ApplicationFrame {

    public Temp3(String titel) {
        super(titel);

        final DefaultHighLowDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 350));
        setContentPane(chartPanel);
    }

    private DefaultHighLowDataset createDataset() {

        int serice = 3;

        Date[] date = new Date[serice];
        double[] high = new double[serice];
        double[] low = new double[serice];
        double[] open = new double[serice];
        double[] close = new double[serice];
        double[] volume = new double[serice];



        Calendar calendar = Calendar.getInstance();
        calendar.set(2008, 5, 1);

        for (int i = 0; i < serice; i++) {
            date[i] = createData(2008, 8, i + 1);
            high[i] = 30 + Math.round(10) + new Double(Math.random() * 20.0);
            low[i] = 30 + Math.round(10) + new Double(Math.random() * 20.0);
            open[i] = 10 + Math.round(10) + new Double(Math.random() * 20.0);
            close[i] = 10 + Math.round(10) + new Double(Math.random() * 20.0);
            volume[i] = 10.0 + new Double(Math.random() * 20.0);
        }

        DefaultHighLowDataset data = new DefaultHighLowDataset(
                "", date, high, low, open, close, volume);
        return data;
    }

    private Date createData(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, date);
        return calendar.getTime();
    }

    private JFreeChart createChart(final
                                   DefaultHighLowDataset dataset) {
        final JFreeChart chart = ChartFactory.createCandlestickChart(
                "Candlestick Demo", "Time", "Price", dataset, false);

//        ValueAxis timeAxis = new DateAxis(timeAxisLabel);
//        NumberAxis valueAxis = new NumberAxis(valueAxisLabel);
//        XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);
//        plot.setRenderer(new CandlestickRenderer());
//        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,
//                plot, true);
//        ChartFactory.getChartTheme().apply(chart);

//        final JFreeChart chart = ChartFactory.createBarChart("Bar Chart Demo", // chart
//                // title
//                "Category", // domain axis label
//                "Value", // range axis label
//                dataset, // data
//                PlotOrientation.VERTICAL, // orientation
//                true, // include legend
//                true, // tooltips?
//                false // URLs?
//        );


        SubCategoryAxis domainAxis = new SubCategoryAxis("Product / Month");
        domainAxis.setCategoryMargin(0.05);
        domainAxis.addSubCategory("Product 1");
        domainAxis.addSubCategory("Product 2");
        domainAxis.addSubCategory("Product 3");

        NumberAxis phaseAxis = new NumberAxis("Phase");
        phaseAxis.setRange(0, 2);
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis dateAxis = plot.getDomainAxis(0);
        plot.setDomainAxis(0, phaseAxis);
        plot.setDomainAxis(1, dateAxis);
        plot.mapDatasetToDomainAxis(0, 1);
        return chart;
    }

    public static void main(String args[]) {
        Temp3 chart = new Temp3("Candle Stick Chart");
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}