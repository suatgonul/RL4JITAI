package tez.algorithm;

import burlap.oomdp.singleagent.SADomain;

/**
 * Created by suatgonul on 4/30/2017.
 */
public class SelfManagementDomain extends SADomain {
    private DomainComplexity complexity;

    public SelfManagementDomain(DomainComplexity complexity) {
        this.complexity = complexity;
    }

    public DomainComplexity getComplexity() {
        return complexity;
    }

    public enum DomainComplexity {
        EASY, MEDIUM, HARD;
    }
}
