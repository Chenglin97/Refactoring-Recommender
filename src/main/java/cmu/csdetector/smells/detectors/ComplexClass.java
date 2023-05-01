package cmu.csdetector.smells.detectors;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ComplexClass extends SmellDetector {
    private static final double OVERCOMPLEXITY = 5;
    @Override
    public List<Smell> detect(Resource resource) {
        List<Smell> smells = new ArrayList<>();
        Type type = (Type)resource;
        List<Method> methods = type.getMethods();
        for (Method method : methods) {
            Double cc = method.getMetricValue(MetricName.CC);
            if (cc != null && cc > OVERCOMPLEXITY) {
                Smell smell = createSmell(resource, "CC = " + cc);
                smells.add(smell);
            }
        }
        return smells;
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.ComplexClass;
    }
}
