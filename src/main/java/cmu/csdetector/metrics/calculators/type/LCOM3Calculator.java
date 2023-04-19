package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.metrics.MetricName;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class LCOM3Calculator extends LCOMCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        TypeDeclaration type = (TypeDeclaration)target;

        this.calculateMetrics(type);
        double lcom2 = (this.m - (this.sumMA / this.a)) / (this.m - 1);
        return Double.isNaN(lcom2) ? 0 : lcom2;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.LCOM3;
    }

}
