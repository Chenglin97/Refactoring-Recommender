package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.AggregateMetricValues;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;

import java.util.ArrayList;
import java.util.List;

public class LazyClass extends SmellDetector {
    @Override
    public List<Smell> detect(Resource resource) {
        Double cloc = resource.getMetricValue(MetricName.CLOC);
        Double clocFQ = AggregateMetricValues.getInstance().getFirstQuartileValue(MetricName.CLOC);
        if (cloc < clocFQ) {
            Smell smell = createSmell(resource, "CLOC(" + cloc + ") > CLOC_FirstQuartile(" + clocFQ + ")");
            return List.of(smell);
        }
        return new ArrayList<>();
    }

    @Override
    protected SmellName getSmellName() { return SmellName.LazyClass; }
}
