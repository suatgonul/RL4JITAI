package tez.environment;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import org.joda.time.DateTime;
import tez.domain.SelfManagementRewardFunction;
import tez.domain.SelfManagementStateGenerator;
import tez.persona.Activity;
import tez.persona.TimePlan;

/**
 * Created by suat on 26-May-17.
 */
/*public class SelfManagementEnvironment extends SimulatedEnvironment {
    private int stateChangeFrequency;

    private int dayOffset;
    private DateTime previousTime;
    private Activity previousActivity;
    private TimePlan currentTimePlan;
    private DateTime currentTime;
    private Activity currentActivity;
    private boolean lastActivity;
    private boolean lastUserReaction;
    private DateTime lastInterventionCheckTime;

    public SelfManagementEnvironment(Domain domain, RewardFunction rf, TerminalFunction tf, String personaFolder, int stateChangeFrequency) {
        super(domain, rf, tf);
        ((SelfManagementRewardFunction) rf).setEnvironment(this);
        this.stateChangeFrequency = stateChangeFrequency;
        dayOffset = 1;
        episodeInit(dayOffset);
        //stateGenerator = new ConstantStateGenerator(getStateFromCurrentContext());
        stateGenerator = new SelfManagementStateGenerator(this);
        super.resetEnvironment();
    }

}*/
