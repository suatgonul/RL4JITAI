package tez2.persona;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by suat on 15-Feb-18.
 */
public class PersonaConfig {
    private LinkedHashMap<Integer, Integer> jitaiGroups;
    private double commitmentIntensity;
    private double behaviorFrequency;
    private Map<String, Double> jitaiPreferences;
    private List<Integer> actionPlanRanges;

    public static void main(String[] args) {
        List<PersonaConfig> configs = getConfigs("D:\\mine\\odtu\\6\\tez\\codes\\RLTrials\\src\\main\\resources\\persona\\officejob");
        System.out.println(configs);
    }

    public static List<PersonaConfig> getConfigs(String configFileFolder) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(configFileFolder + "/config"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file");
        }

        int numberOfConfigs = Integer.parseInt(prop.getProperty("number_of_configs"));

        List<PersonaConfig> configs = new ArrayList<>();
        for(int i=0; i<numberOfConfigs; i++) {
            configs.add(new PersonaConfig());
        }

        for(int i=0; i<numberOfConfigs; i++) {
            PersonaConfig config = configs.get(i);

            // jitai types
            String[] jitaiTypesList = prop.getProperty("jitai_types").split(":");
            String[] jitaiTypes = jitaiTypesList[i].split(",");
            config.jitaiGroups = new LinkedHashMap();
            for (int j = 0; j < jitaiTypesList.length; j++) {
                config.jitaiGroups.put(i + 1, Integer.parseInt(jitaiTypes[j]));
            }

            // commitment intensity
            String[] commitmentIntensityList = prop.getProperty("commitment_intensity").split(":");
            config.commitmentIntensity = Double.parseDouble(commitmentIntensityList[i]);

            // behavior frequency
            String[] behaviorFrequencyList = prop.getProperty("behavior_frequency").split(":");
            config.behaviorFrequency = Double.parseDouble(behaviorFrequencyList[i]);

            // jitai preferences
            config.jitaiPreferences = new HashMap();
            String[] preferenceListList = prop.getProperty("reaction_to_jitais").split(":");
            String[] preferenceList = preferenceListList[i].split(",");
            for (int j = 0; j < preferenceListList.length; j++) {
                config.jitaiPreferences.put("JITAI_" + (j + 1), Double.parseDouble(preferenceList[j]));
            }

            // action plan ranges
            config.actionPlanRanges = new ArrayList<>();
            String[] rangesList = prop.getProperty("action_plan_ranges").split(":");
            String[] rangeList = rangesList[i].split(",");
            for (int j = 0; j < rangeList.length; j++) {
                config.actionPlanRanges.add(Integer.parseInt(rangeList[j]));
            }
        }

        return configs;
    }
}