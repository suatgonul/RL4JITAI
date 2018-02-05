package tez2.domain;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.environment.Environment;
import com.sun.java.browser.plugin2.DOM;
import tez.domain.SelfManagementDomain;
import tez.domain.action.InterventionDeliveryAction;
import tez.domain.action.NoAction;
import tez.domain.action.SelfManagementAction;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suatgonul on 4/20/2017.
 */
public class OpportuneMomentDomainGenerator implements DomainGenerator {




    private SelfManagementDomain.DomainComplexity complexity;
    private Domain domain;
    private Environment environment;

    public void setEnvironment(Environment environment) {
        this.environment = environment;

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

        new InterventionDeliveryAction(ACTION_JITAI_1, domain);
        new NoAction(ACTION_NO_ACTION, domain);

        return domain;
    }
}
