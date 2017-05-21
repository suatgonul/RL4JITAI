package tez.algorithm.collaborative_learning;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import tez.domain.SelfManagementDomain;
import tez.domain.SelfManagementDomainGenerator;
import tez.experiment.performance.SelfManagementEligibilityEpisodeAnalysis;
import tez.experiment.performance.SelfManagementEpisodeAnalysis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static tez.domain.SelfManagementDomainGenerator.*;
import static tez.domain.SelfManagementDomainGenerator.ACTION_NO_ACTION;

/**
 * Created by suat on 17-May-17.
 */
public class StateClassifierTest {
    private static StateClassifier stateClassifier;
    private static SelfManagementEpisodeAnalysis episodeAnalysis;
    private static HashableStateFactory hashableStateFactory;
    private static State s, s2, s3, s4, s5;

    @BeforeClass
    public static void initialize() {
        SelfManagementDomainGenerator smdg = new SelfManagementDomainGenerator(SelfManagementDomain.DomainComplexity.HARD);
        Domain domain = smdg.generateDomain();
        ObjectClass oc = new ObjectClass(domain, CLASS_STATE);
        s = new MutableState();
        s.addObject(new MutableObjectInstance(domain.getObjectClass(CLASS_STATE), CLASS_STATE));
        ObjectInstance o = s.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_DAY_TYPE, 0);
        o.setValue(ATT_LOCATION, 0);
        o.setValue(ATT_ACTIVITY_TIME, "15:40");
        o.setValue(ATT_ACTIVITY, 0);
        o.setValue(ATT_PHONE_USAGE, 0);
        o.setValue(ATT_EMOTIONAL_STATUS, 0);
        o.setValue(ATT_STATE_OF_MIND, 0);

        s2 = s.copy();
        s2.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s2.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_LOCATION, 1);

        s3 = s.copy();
        s3.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s3.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_LOCATION, 1);

        s4 = s.copy();
        s4.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s4.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_LOCATION, 2);

        s5 = s.copy();
        s5.addObject(new MutableObjectInstance(oc, CLASS_STATE));
        o = s5.getObjectsOfClass(CLASS_STATE).get(0);
        o.setValue(ATT_ACTIVITY, 3);

        episodeAnalysis = new SelfManagementEligibilityEpisodeAnalysis(s);
        episodeAnalysis.stateSequence.add(s2);
        episodeAnalysis.stateSequence.add(s3);
        episodeAnalysis.stateSequence.add(s4);
        episodeAnalysis.rewardSequence.add(5.0);
        episodeAnalysis.rewardSequence.add(5.0);
        episodeAnalysis.rewardSequence.add(-2.0);
        episodeAnalysis.actionSequence.add(domain.getAction(ACTION_INT_DELIVERY).getGroundedAction());
        episodeAnalysis.actionSequence.add(domain.getAction(ACTION_NO_ACTION).getGroundedAction());
        episodeAnalysis.actionSequence.add(domain.getAction(ACTION_NO_ACTION).getGroundedAction());

        stateClassifier = StateClassifier.getInstance();
        stateClassifier.setDomain(domain);
        hashableStateFactory = new SimpleHashableStateFactory();
    }

    @Test
    public void stateActionCountUpdateMap() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Method method = stateClassifier.getClass().getDeclaredMethod("updateStateActionCounts", SelfManagementEpisodeAnalysis.class);
        method.setAccessible(true);
        method.invoke(stateClassifier, episodeAnalysis);

        Field f = stateClassifier.getClass().getDeclaredField("stateActionCounts");
        f.setAccessible(true);
        Map<HashableState, Map<String, Integer>> stateActionCounts = (Map<HashableState, Map<String, Integer>>) f.get(stateClassifier);

        int count = stateActionCounts.get(hashableStateFactory.hashState(s2)).get(ACTION_NO_ACTION);
        Assert.assertEquals(count, 2);
        count = stateActionCounts.get(hashableStateFactory.hashState(s)).get(ACTION_INT_DELIVERY);
        Assert.assertEquals(count, 1);
    }
}
