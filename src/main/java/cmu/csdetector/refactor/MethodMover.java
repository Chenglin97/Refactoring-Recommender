package cmu.csdetector.refactor;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.TypeMetricValueCollector;
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
import java.util.List;

public class MethodMover {
    public Resource moveMethod(Method method, Resource source_class, ArrayList<Resource> classes) {
        Double source_lcom3 = source_class.getMetricValue(MetricName.LCOM3);
        Resource target_class = source_class;
        Double best_lcom3 = 0.0;

        for (Resource candidate_class : classes) {
            if (candidate_class.equals(source_class)) {
                continue;
            }
            Double old_canidate_lcom3 = candidate_class.getMetricValue(MetricName.LCOM3);
            Double old_source_lcom3 = source_class.getMetricValue(MetricName.LCOM3);
            MethodDeclaration methodNode = (MethodDeclaration) method.getNode();
            Resource new_class = simulateMove(methodNode, source_class, candidate_class);

            TypeMetricValueCollector collector = new TypeMetricValueCollector();
            collector.collect(new_class);
            TypeMetricValueCollector collector2 = new TypeMetricValueCollector();
            collector2.collect(source_class);

            Double new_canidate_lcom3 = new_class.getMetricValue(MetricName.LCOM3);
            Double new_source_lcom3 = source_class.getMetricValue(MetricName.LCOM3);

            // print lcom3 values

            System.out.println("old_source_lcom3: " + old_source_lcom3);
            System.out.println("new_source_lcom3: " + new_source_lcom3);
            System.out.println("old_canidate_lcom3: " + old_canidate_lcom3);
            System.out.println("new_canidate_lcom3: " + new_canidate_lcom3 + "\n");

            if ((new_canidate_lcom3 <= old_canidate_lcom3) && (new_source_lcom3 <= old_source_lcom3)) {
                target_class = new_class;
            }
        }
        return target_class;
    }

    private Resource simulateMove(MethodDeclaration method, Resource source_class, Resource target_class) {
        // Copy the AST node representing the method to move and add it to the target class
        AST ast = target_class.getNode().getAST();
        MethodDeclaration newMethod = (MethodDeclaration) ASTNode.copySubtree(ast, method);
        TypeDeclaration targetClass = (TypeDeclaration) target_class.getNode();

        targetClass.bodyDeclarations().add(newMethod);

        // Remove the original method AST node from its containing class
        TypeDeclaration sourceClass = (TypeDeclaration) source_class.getNode();
        sourceClass.bodyDeclarations().remove(method);

        return target_class;
    }
}
