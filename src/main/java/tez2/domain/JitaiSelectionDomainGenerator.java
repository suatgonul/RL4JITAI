package tez2.domain;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.environment.Environment;
import tez2.domain.action.*;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suatgonul on 4/20/2017.
 */
public class JitaiSelectionDomainGenerator implements DomainGenerator {

    private SelfManagementDomain.DomainComplexity complexity;
    private Domain domain;

    public JitaiSelectionDomainGenerator(SelfManagementDomain.DomainComplexity complexity) {
        this.complexity = complexity;
    }


    public void setEnvironment(Environment environment) {
        for (Action a : domain.getActions()) {
            ((SelfManagementAction) a).setEnvironment(environment);
        }
    }

    @Override
    public Domain generateDomain() {
        domain = new SelfManagementDomain(complexity);

        ObjectClass stateClass = new ObjectClass(domain, CLASS_STATE);

        stateClass.addAttribute(DomainConfig.getAtt(ATT_REMEMBER_BEHAVIOR, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_BEHAVIOR_FREQUENCY, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_HABIT_STRENGTH, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_PART_OF_DAY, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_DAY_TYPE, domain));

        new Jitai1Action(ACTION_JITAI_1, domain);
        new Jitai2Action(ACTION_JITAI_2, domain);
        new Jitai3Action(ACTION_JITAI_3, domain);
        new NoAction(ACTION_NO_ACTION, domain);

        return domain;
    }
}
