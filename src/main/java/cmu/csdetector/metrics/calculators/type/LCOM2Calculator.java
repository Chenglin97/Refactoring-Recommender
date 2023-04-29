package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.metrics.MetricName;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class LCOM2Calculator extends LCOMCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        TypeDeclaration type = (TypeDeclaration) target;

        this.calculateMetrics(type);
        double lcom2 = 1 - (this.sumMA / (this.m * this.a));
        return Double.isNaN(lcom2) ? 0 : lcom2;
    }

    @Override
    public MetricName getMetricName() { return MetricName.LCOM2; }

}
