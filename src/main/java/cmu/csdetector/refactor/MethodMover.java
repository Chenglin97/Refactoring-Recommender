package cmu.csdetector.refactor;

import cmu.csdetector.ast.visitors.ExternalClassVariableCollector;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.metrics.calculators.type.LCOM3Calculator;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodMover {
    private Map<Resource, Double> sum_lcom3_values = new HashMap<>();
    public Resource moveMethod(ASTNode method_node, Resource source_class, ArrayList<Resource> classes) {
        // returns the class the method should be moved to
        // Resource source_class = method.getBelongingClass();


        LCOM3Calculator lcom3Calculator = new LCOM3Calculator();
        Double old_source_lcom_3 = source_class.getMetricValue(MetricName.LCOM3);
        for (Resource target_class: classes) {
            Double old_target_lcom_3 = target_class.getMetricValue(MetricName.LCOM3);

            Double new_source_lcom_3 = calculateLCOM3WithoutMethod(method_node, source_class);
            Double new_target_lcom_3 = lcom3Calculator.calculateWithAdditionalMethod(target_class, method_node);
            System.out.println("checking target class: " + target_class.getFullyQualifiedName());
            System.out.println("old source lcom3: " + old_source_lcom_3);
            System.out.println("new source lcom3: " + new_source_lcom_3);
            System.out.println("old target lcom3: " + old_target_lcom_3);
            System.out.println("new target lcom3: " + new_target_lcom_3);

            sum_lcom3_values.put(target_class, new_target_lcom_3 + new_source_lcom_3);
        }
        Resource target_class = getBestTargetClass();
        System.out.println("recommended target class: " + target_class.getFullyQualifiedName());
        return target_class;
    }

    private Resource getBestTargetClass() {
        /* find the minimum lcom3 value */
        Resource target_class = null;
        Double min_lcom3_value = Double.MAX_VALUE;
        for( Map.Entry<Resource, Double> entry : sum_lcom3_values.entrySet()) {
            if (entry.getValue() < min_lcom3_value) {
                min_lcom3_value = entry.getValue();
                target_class = entry.getKey();
            }
        }
        return target_class;
    }

    public Double calculateLCOM3WithoutMethod(ASTNode method_node, Resource source_class) {
        TypeMetricValueCollector collector = new TypeMetricValueCollector();

         Resource classWithoutMethod = simulateRemoveMethodFromClass(method_node, source_class);
         collector.collect(classWithoutMethod);
         return classWithoutMethod.getMetricValue(MetricName.LCOM3);

    }

    public Resource moveMethodBasedOnLCOM3(Method method, Resource source_class, ArrayList<Resource> classes) {
        Resource target_class = source_class;


        for (Resource candidate_class : classes) {
            if (candidate_class.equals(target_class)) {
                continue;
            }

            TypeMetricValueCollector collector = new TypeMetricValueCollector();
            // collector.collect(target_class);

            Double old_candidate_lcom3 = candidate_class.getMetricValue(MetricName.LCOM3);
            Double old_source_lcom3 = target_class.getMetricValue(MetricName.LCOM3);
            MethodDeclaration methodNode = (MethodDeclaration) method.getNode();
            ResourcePair resourcePair = simulateMove(methodNode, target_class, candidate_class);
            Resource copied_target_class = resourcePair.getSourceClassCopy();
            Resource copied_candidate_class = resourcePair.getTargetClassCopy();

            collector.collect(copied_target_class);
            collector.collect(copied_candidate_class);

            Double new_source_lcom3 = copied_target_class.getMetricValue(MetricName.LCOM3);
            Double new_candidate_lcom3 = copied_candidate_class.getMetricValue(MetricName.LCOM3);

            if ((new_candidate_lcom3 <= old_candidate_lcom3) && (new_source_lcom3 <= old_source_lcom3)) {
                target_class = copied_target_class;
            }

            // print lcom3 values
            System.out.println("source: " + target_class);
            System.out.println("candidate: " + candidate_class);
            System.out.println("old_source_lcom3: " + old_source_lcom3);
            System.out.println("new_source_lcom3: " + new_source_lcom3);
            System.out.println("old_candidate_lcom3: " + old_candidate_lcom3);
            System.out.println("new_candidate_lcom3: " + new_candidate_lcom3 + "\n");

            if ((new_candidate_lcom3 <= old_candidate_lcom3) && (new_source_lcom3 <= old_source_lcom3)) {
                target_class = candidate_class;
                System.out.println("new target class: " + candidate_class + "\n");
            }
        }
        return target_class;
    }

    private ResourcePair simulateMove(MethodDeclaration method, Resource source_class, Resource target_class) {
        // Copy the AST node representing the method to move and add it to the target class
        Resource targetClassCopy = target_class.clone();
        AST ast = targetClassCopy.getNode().getAST();
        MethodDeclaration newMethod = (MethodDeclaration) ASTNode.copySubtree(ast, method);
        TypeDeclaration targetClass = (TypeDeclaration) target_class.getNode();

        targetClass.bodyDeclarations().add(newMethod);

        // Remove the original method AST node from its containing class
        Resource sourceClassCopy = source_class.clone();
        TypeDeclaration sourceClassNode = (TypeDeclaration) sourceClassCopy.getNode();
        sourceClassNode.bodyDeclarations().remove(method);

        return new ResourcePair(targetClassCopy, sourceClassCopy);
    }

    private Resource simulateRemoveMethodFromClass(ASTNode method, Resource source_class) {
        Resource sourceClassCopy = source_class.clone();
        TypeDeclaration sourceClassCopyDeclaration = (TypeDeclaration) sourceClassCopy.getNode();
        sourceClassCopyDeclaration.bodyDeclarations().remove(method);
        return sourceClassCopy;
    }
}
