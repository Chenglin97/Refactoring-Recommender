package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.AggregateMetricValues;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;

import java.util.ArrayList;
import java.util.List;

public class GodClass extends SmellDetector {
    private static final double HIGHCLOC = 500;

    @Override
    public List<Smell> detect(Resource resource) {
        Double cloc = resource.getMetricValue(MetricName.CLOC);
        Double tcc = resource.getMetricValue(MetricName.TCC);

        // Get the average Tight Class Cohesion across the whole system.
        Double tccavg = AggregateMetricValues.getInstance().getAverageValue(MetricName.TCC);

        // God Class detection criteria
        if (cloc > HIGHCLOC && tcc < tccavg) {
            Smell smell = createSmell(resource, "CLOC = " + cloc + " and TCC(" + tcc + ") < TCC_Average(" + tccavg + ")");
            return List.of(smell);
        }
        return new ArrayList<>();
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.GodClass;
    }

}
