package cmu.csdetector.refactor;

import java.util.ArrayList;
import java.util.List;

public class ExtractMethodOpportunity {

    private List<Integer> cluster;
    private double benefit;
    private List<ExtractMethodOpportunity> alternatives = new ArrayList<>();
    private List<String> parameters = new ArrayList<>();
    private boolean isAlternative = false;

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public ExtractMethodOpportunity(List<Integer> cluster) {
        this.cluster = cluster;
    }

    public List<Integer> getCluster() { return this.cluster; }
    public void setBenefit(double benefit) { this.benefit = benefit; }
    public double getBenefit() { return this.benefit; }

}
