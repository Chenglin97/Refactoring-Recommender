package cmu.csdetector.refactor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtractMethodOpportunity {

    private final List<Integer> cluster;
    private double benefit;
    private List<ExtractMethodOpportunity> alternatives = new ArrayList<>();
    private List<String> parameters = new ArrayList<>();
    private String returnType = "";
    public boolean isAlternative = false;

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public ExtractMethodOpportunity(List<Integer> cluster) {
        this.cluster = cluster;
    }

    public double differenceInSize(ExtractMethodOpportunity other) {
        return (double) Math.abs(this.getSize() - other.getSize()) / Math.min(this.getSize(), other.getSize());
    }
    public double overlap(ExtractMethodOpportunity other) {
        int maxStart = Math.max(this.cluster.get(0), other.cluster.get(0));
        int minEnd = Math.min(this.cluster.get(1), other.cluster.get(1));
        int overlap = Math.max(0, minEnd - maxStart);
        return (double) overlap / Math.max(this.getSize(), other.getSize());
    }

    public List<Integer> getCluster() { return this.cluster; }
    public int getSize() { return this.cluster.get(1) - this.cluster.get(0) + 1; }

    public void setBenefit(double benefit) { this.benefit = benefit; }
    public double getBenefit() { return this.benefit; }
    public String getReturnType(){
        return this.returnType;
    }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public void addAlternative(ExtractMethodOpportunity opp) { this.alternatives.add(opp); }
    public List<ExtractMethodOpportunity> getAlternatives() { return this.alternatives; }
}
