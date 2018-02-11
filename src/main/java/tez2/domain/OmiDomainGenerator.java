package tez2.domain;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.environment.Environment;
import tez2.domain.omi.InterventionDeliveryAction;
import tez2.domain.omi.NoAction;

import static tez2.domain.DomainConfig.*;

/**
 * Created by suatgonul on 4/20/2017.
 */
public class OmiDomainGenerator implements DomainGenerator {




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

        stateClass.addAttribute(DomainConfig.getAtt(ATT_LOCATION, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_QUARTER_HOUR_OF_DAY, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_ACTIVITY, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_PHONE_USAGE, domain));
        stateClass.addAttribute(DomainConfig.getAtt(ATT_EMOTIONAL_STATUS, domain));

        new InterventionDeliveryAction(ACTION_SEND_JITAI, domain);
        new NoAction(ACTION_NO_ACTION, domain);

        return domain;
    }
}
