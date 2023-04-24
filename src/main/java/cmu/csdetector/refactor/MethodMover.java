package cmu.csdetector.refactor;

import cmu.csdetector.metrics.MetricName;
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
        Resource target_class = null;
        Double max_reduction = 0.0;

        for (Resource candidate_class : classes) {
            if (candidate_class.equals(source_class)) {
                continue;
            }
            System.out.println("candidate class: " + candidate_class.getNode());
            Double old_class_lcom3 = candidate_class.getMetricValue(MetricName.LCOM3);
            MethodDeclaration methodNode = (MethodDeclaration) method.getNode();
            Resource new_class = simulateMove(methodNode, source_class, candidate_class);
            Double new_class_lcom3 = new_class.getMetricValue(MetricName.LCOM3);

            // print lcom3 values
            System.out.println("old class lcom3: " + old_class_lcom3);
            System.out.println("new class lcom3: " + new_class_lcom3);

            System.out.println("new class: " + new_class.getNode());



            Double reduction = old_class_lcom3 - new_class_lcom3;

            if (reduction > max_reduction) {
                // Choose the class with the maximum reduction in LCOM3 metric value
                max_reduction = reduction;
                target_class = candidate_class;
            }
        }

        if (target_class != null && max_reduction > 0.0 && source_lcom3 - source_class.getMetricValue(MetricName.LCOM3) > 0.0) {
            // Only move the method if it reduces LCOM3 metric value in both the source and target classes
            return target_class;
        }

        return null;
    }

    private Resource simulateMove(MethodDeclaration method, Resource source_class, Resource target_class) {
        // Copy the AST node representing the method to move and add it to the target class
        AST ast = target_class.getNode().getAST();
        MethodDeclaration newMethod = (MethodDeclaration) ASTNode.copySubtree(ast, method);
        TypeDeclaration targetClass = (TypeDeclaration) target_class.getNode();

        System.out.println("method to move:" + method.toString());
        System.out.println("target class B4:" + targetClass.toString());

        targetClass.bodyDeclarations().add(newMethod);

        // Remove the original method AST node from its containing class
        TypeDeclaration sourceClass = (TypeDeclaration) source_class.getNode();
        sourceClass.bodyDeclarations().remove(method);

        // print the new class
        System.out.println("target class After:" + targetClass.toString());

        // print the target resource
        System.out.println("target resource:" + target_class.getNode().toString());

        return target_class;
    }
}
