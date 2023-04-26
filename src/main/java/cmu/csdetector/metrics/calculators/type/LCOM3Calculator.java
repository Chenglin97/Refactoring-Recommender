package cmu.csdetector.metrics.calculators.type;

import cmu.csdetector.ast.visitors.ClassFieldAccessCollector;
import cmu.csdetector.ast.visitors.ExternalClassVariableCollector;
import cmu.csdetector.ast.visitors.MethodCollector;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;
import java.util.Map;

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

    public Double calculateWithoutMethod(Resource the_class, ASTNode method_node) {
        TypeDeclaration type = (TypeDeclaration) the_class.getNode();

        MethodCollector mCollector = new MethodCollector();

        type.accept(mCollector);
        this.calculateMetrics(type);
        double new_m = this.m - 1;
        double new_a = this.a;
        double new_sumMA = this.sumMA;
//        ExternalClassVariableCollector ecvCollector = new ExternalClassVariableCollector();
//        MethodDeclaration methodDeclaration = (MethodDeclaration) method_node;
//        methodDeclaration.accept(ecvCollector);
//        Map<String, Integer> externalClassVariableCount= ecvCollector.getExternalClassVariableCount();
//        String the_class_name = ((TypeDeclaration) the_class.getNode()).getName().toString();
//        new_sumMA -= externalClassVariableCount.getOrDefault(the_class_name, 0);
        ClassFieldAccessCollector cfaCollector = new ClassFieldAccessCollector((TypeDeclaration) the_class.getNode());
        MethodDeclaration methodDeclaration = (MethodDeclaration) method_node;
        methodDeclaration.accept(cfaCollector);
        new_sumMA -= cfaCollector.getLocalReferences().size();
        double lcom3 = (new_m - (new_sumMA / new_a)) / (new_m - 1);
        return Double.isNaN(lcom3) ? 0 : lcom3;
    }

    public Double calculateWithAdditionalMethod(Resource the_class, ASTNode method_node) {
        TypeDeclaration type = (TypeDeclaration) the_class.getNode();

        MethodCollector mCollector = new MethodCollector();

        type.accept(mCollector);
        List<MethodDeclaration> methods = mCollector.getNodesCollected();

        // Add the additional method to the list of methods
        methods.add((MethodDeclaration) method_node);
        this.calculateMetrics(type);
        double new_m = this.m + 1; // Increment the count of methods
        double new_a = this.a;
        double new_sumMA = this.sumMA;

        ExternalClassVariableCollector ecvCollector = new ExternalClassVariableCollector();
        MethodDeclaration methodDeclaration = (MethodDeclaration) method_node;
        methodDeclaration.accept(ecvCollector);
        Map<String, Integer> externalClassVariableCount= ecvCollector.getExternalClassVariableCount();
        String the_class_name = ((TypeDeclaration) the_class.getNode()).getName().toString();
        new_sumMA += externalClassVariableCount.getOrDefault(the_class_name, 0);


        double lcom3 = (new_m - (new_sumMA / new_a)) / (new_m - 1);
        return Double.isNaN(lcom3) ? 0 : lcom3;
    }


}
