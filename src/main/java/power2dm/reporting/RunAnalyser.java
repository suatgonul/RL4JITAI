package power2dm.reporting;

import power2dm.reporting.visualization.VisualizationMetadata;
import power2dm.reporting.visualization.Visualizer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by suat on 28-Apr-16.
 */
public class RunAnalyser {
    private List<P2DMEpisodeAnalysis> episodeAnalysisList = new ArrayList<P2DMEpisodeAnalysis>();

    public void recordEpisodeReward(P2DMEpisodeAnalysis ea) {
        if(ea.episodeNo >= 100000)
            episodeAnalysisList.add(ea);
    }

    public void drawRewardCharts(List<Class> visualizationClasses, VisualizationMetadata visualizerMetadata) {
        for (Class visualizerClass : visualizationClasses) {
            Constructor<?> cons = null;
            try {
                cons = visualizerClass.getConstructor(Map.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Reflection exception while getting the constructor of visualizer", e);
            }

            try {
                Visualizer visualizer = (Visualizer) cons.newInstance(visualizerMetadata.getVisualizerMetadata(visualizerClass));
                visualizer.createRewardGraph(episodeAnalysisList);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Reflection exception while calling the constructor of visualizer", e);
            } catch (InstantiationException e) {
                throw new RuntimeException("Reflection exception while calling the constructor of visualizer", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Reflection exception while calling the constructor of visualizer", e);
            }

        }
    }
}
