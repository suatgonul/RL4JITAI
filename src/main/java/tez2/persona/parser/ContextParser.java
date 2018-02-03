package tez2.persona.parser;

import org.joda.time.DateTime;
import tez.environment.context.*;
import tez.persona.Activity;
import tez.persona.parser.PersonaParserException;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static tez.persona.parser.Constant.*;

/**
 * Created by suatgonul on 12/25/2016.
 */
public class ContextParser {
    private DateTime middayTime;
    private DateTime eveningTime;

    public void parseContextValues(String line, Activity activity) throws PersonaParserException {
        List<String> words = Arrays.asList(line.split(","));

        Context context = activity.getContext();
        String valueStr = getContextValueRandomly(words.get(OFFSET_LOCATION)).trim();
        Location location = Location.valueOf(valueStr);
        context.setLocation(location);

        valueStr = getContextValueRandomly(words.get(OFFSET_PHYSICAL_ACTIVITY));
        PhysicalActivity physicalActivity = PhysicalActivity.valueOf(valueStr);
        context.setPhysicalActivity(physicalActivity);

        valueStr = getContextValueRandomly(words.get(OFFSET_PHONE_USAGE).trim());
        PhoneUsage phoneUsage = PhoneUsage.valueOf(valueStr);
        context.setPhoneUsage(phoneUsage);

        String psychologicalParametersStr;
        if (activity.getContext().getPhoneCheckSuitability() == true) {
            if(phoneUsage == PhoneUsage.TALKING) {
                context.setPhoneCheckSuitability(false);
            }

            if (words.get(OFFSET_PHONE_CHECK).trim().equals("1")) {
                psychologicalParametersStr = words.get(OFFSET_STATE_OF_MIND_EMOTIONAL_STATUS);
            } else {
                psychologicalParametersStr = words.get(OFFSET_PHONE_CHECK);
            }

        } else {
            psychologicalParametersStr = words.get(OFFSET_STATE_OF_MIND_EMOTIONAL_STATUS);
        }

        String[] psychologicalParameters = getContextValueRandomly(psychologicalParametersStr).split("/");
        StateOfMind stateOfMind = StateOfMind.valueOf(psychologicalParameters[0].trim());
        context.setStateOfMind(stateOfMind);
        EmotionalStatus emotionalStatus = EmotionalStatus.valueOf(psychologicalParameters[1].trim());
        context.setEmotionalStatus(emotionalStatus);

    }

    public String getContextValueRandomly(String values) throws PersonaParserException {
        if (values.contains("|")) {
            int randomProbability = new Random().nextInt(100) + 1;
            int aggregatedProbability = 0;

            List<String> valueList = Arrays.asList(values.split("\\|"));
            for (String valuePair : valueList) {
                String[] valueElements = valuePair.split("=");
                int valueProb = Integer.valueOf(valueElements[1]);

                if (randomProbability >= (aggregatedProbability + 1) && randomProbability <= (aggregatedProbability + valueProb)) {
                    return valueElements[0];
                }

                aggregatedProbability += valueProb;
            }
            throw new PersonaParserException("A value should have been chosen from: " + values);

            // only one alternative
        } else {
            return values;
        }
    }

    private boolean isSuitableForPhoneCheck(List<String> words) {
        if (words.get(OFFSET_PHONE_CHECK).trim().equals("0")) {
            return false;
        }
        return true;
    }

    private boolean isPhoneCheckHasSpecificContext(List<String> words) {
        if (!words.get(OFFSET_PHONE_CHECK).trim().equals("0") && !words.get(OFFSET_PHONE_CHECK).trim().equals("1")) {
            return true;
        }
        return false;
    }
}