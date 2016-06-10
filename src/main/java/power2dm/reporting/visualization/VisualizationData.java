package power2dm.reporting.visualization;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Created by suat on 23-May-16.
 */
public class VisualizationData {
    private XYSeriesCollection dataset;
    private XYLineAndShapeRenderer renderer;

    public VisualizationData(XYSeriesCollection dataset, XYLineAndShapeRenderer renderer) {
        this.dataset = dataset;
        this.renderer = renderer;
    }

    public XYSeriesCollection getDataset() {
        return dataset;
    }

    public XYLineAndShapeRenderer getRenderer() {
        return renderer;
    }
}
