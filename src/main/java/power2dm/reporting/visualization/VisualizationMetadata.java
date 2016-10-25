package power2dm.reporting.visualization;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by suat on 14-Jun-16.
 */
public class VisualizationMetadata {
    private Map<Class, Map<String, Object>> metadataMap = new HashMap<Class, Map<String, Object>>();

    public VisualizationMetadata setMetadataForVisualizer(Class visualizer, String metadataKey, Object metadata) {
        Map<String, Object> visualizerMetadata = this.metadataMap.get(visualizer);
        if (visualizerMetadata == null) {
            visualizerMetadata = new HashMap<String, Object>();
            metadataMap.put(visualizer, visualizerMetadata);
        }

        visualizerMetadata.put(metadataKey, metadata);
        return this;
    }

    public Map<String, Object> getVisualizerMetadata(Class visualizer) {
        Map<String, Object> visualizerMetadata = metadataMap.get(visualizer);
        if(visualizerMetadata == null) {
            visualizerMetadata = new HashMap<String, Object>();
        }
        return visualizerMetadata;
    }

    public Object getVisualizerMetadata(Class visualizer, String metadataKey) {
        Map<String, Object> visualizetionMetadata = getVisualizerMetadata(visualizer);
        Object metadata = visualizetionMetadata.get(metadataKey);
        if(metadata == null) {
            metadata = "";
        }
        return metadata;
    }
}
