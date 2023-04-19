package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.ast.visitors.ClassFieldAccessCollector;
import cmu.csdetector.ast.visitors.MethodCollector;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.calculators.MetricValueCalculator;
import org.eclipse.jdt.core.dom.*;

import java.lang.reflect.Modifier;
import java.util.*;

public abstract class LCOMCalculator extends MetricValueCalculator {

    protected double m;
    protected double a;
    protected double sumMA;

    protected void calculateMetrics(TypeDeclaration type) {

        MethodCollector mCollector = new MethodCollector();
        ClassFieldAccessCollector cfaCollector = new ClassFieldAccessCollector(type);

        type.accept(mCollector);
        List<MethodDeclaration> methods = mCollector.getNodesCollected();
        this.m = methods.size();

        Set<IVariableBinding> attributes = cfaCollector.getAllVariables();
        this.a = attributes.size();

        /*
         * Iterate through all methods of a class and calculate sum(mA)
         * finding the number of distinct variable references
         * sum(mA) is equal to the total value across all methods
         */
        for (MethodDeclaration method : methods) {
            cfaCollector.clearLocalReferences();
            method.accept(cfaCollector);
        }
        this.sumMA = cfaCollector.getNodesCollected().size();

    }

    @Override
    public MetricName getMetricName() {
        return MetricName.LCOM;
    }

}
