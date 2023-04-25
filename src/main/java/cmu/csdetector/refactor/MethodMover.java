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

            // collector.collect(copied_target_class);
            // collector.collect(copied_candidate_class);

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
}
