package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.ast.visitors.ClassFieldAccessCollector;
import cmu.csdetector.ast.visitors.MethodCollector;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

public class LCOM3Calculator extends LCOMCalculator {

    @Override
    protected Double computeValue(ASTNode target) {
        TypeDeclaration type = (TypeDeclaration)target;

        this.calculateMetrics(type);
        double lcom3 = (this.m - (this.sumMA / this.a)) / (this.m - 1);
        return Double.isNaN(lcom3) ? 0 : lcom3;
    }

    @Override
    public MetricName getMetricName() {
        return MetricName.LCOM3;
    }

    public Double calculateWithoutMethod(Resource the_class, Method the_method) {
        TypeDeclaration type = (TypeDeclaration) the_class.getNode();

        MethodCollector mCollector = new MethodCollector();
        ClassFieldAccessCollector cfaCollector = new ClassFieldAccessCollector(type);

        type.accept(mCollector);
        List<MethodDeclaration> methods = mCollector.getNodesCollected();

        double new_m = this.m - 1;
        double new_a = this.a;
        for (MethodDeclaration method : methods) {
            if (method.equals(the_method.getNode())) {
                continue;
            }
            cfaCollector.clearLocalReferences();
            method.accept(cfaCollector);

        }
        double new_sumMA = cfaCollector.getNodesCollected().size();

        double lcom3 = (new_m - (new_sumMA / new_a)) / (new_m - 1);
        return Double.isNaN(lcom3) ? 0 : lcom3;
    }
    public Double calculateWithAdditionalMethod(Resource the_class, Method method) {
        return 0.0;
    }

}
