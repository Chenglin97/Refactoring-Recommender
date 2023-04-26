package cmu.csdetector.refactor;

import cmu.csdetector.ast.visitors.ClassMethodInvocationVisitor;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;

import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.detectors.FeatureEnvy;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodMover {
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

    public Resource moveMethodBasedOnFeatureEnvy(Method method, Resource source_class, ArrayList<Resource> classes) {
        Resource target_class = source_class;

        ITypeBinding target_class_binding = this.determineFeatureEnvy(method);

        return source_class;
    }

    public ITypeBinding determineFeatureEnvy(Method method) {

        // Get the class that the method is defined in and pass to constructor of visitor.
        ITypeBinding declaringType = method.getBinding().getDeclaringClass();
        ClassMethodInvocationVisitor cmiVisitor = new ClassMethodInvocationVisitor(declaringType);

        // Visit.
        MethodDeclaration methodDeclaration = (MethodDeclaration) method.getNode();
        methodDeclaration.accept(cmiVisitor);

        Map<ITypeBinding, Integer> types = cmiVisitor.getMethodsCalls();

        // Get the number of internal method calls within the targeted method
        Integer internalCalls = (types.get(declaringType) != null) ? types.get(declaringType) : 0;
        types.remove(declaringType);

        // Create a new Feature Envy smell for each external class in which the method invocations to that class outnumber the number of internal method calls.
        List<Smell> smells = new ArrayList<>();
        Map<ITypeBinding, Integer> externalCalls = new HashMap<>();
        Map<ITypeBinding, Integer> externalCallsWithoutParents = new HashMap<>();
        for (ITypeBinding type : types.keySet()) {
            if (types.get(type) > internalCalls) {
                externalCalls.put(type, types.get(type));
                externalCallsWithoutParents.put(type, types.get(type));
            }
        }

        // account for super classes
        for (ITypeBinding call : externalCalls.keySet()) {
            // determine if the class has a parent in the map, then add the parent's calls to the child if it does
            ITypeBinding parent = call.getSuperclass();
            while (parent != null && externalCalls.containsKey(parent)) {
                externalCallsWithoutParents.replace(call, externalCalls.get(parent) + externalCalls.get(call));
                parent = parent.getSuperclass();
            }
        }

        // remove any parent from external calls
        for (ITypeBinding call : externalCalls.keySet()) {
            if (externalCallsWithoutParents.containsKey(call.getSuperclass())) {
                externalCallsWithoutParents.remove(call.getSuperclass());
            }
        }

        // print out the external calls
        System.out.println("EXTERNAL CALLS ");
        for (ITypeBinding call : externalCallsWithoutParents.keySet()) {
            // print call
            System.out.println(call.getName() + " " + externalCallsWithoutParents.get(call));
        }

        // return the class with the most external calls
        ITypeBinding max = null;
        for (ITypeBinding call : externalCallsWithoutParents.keySet()) {
            if (max == null || externalCallsWithoutParents.get(call) > externalCallsWithoutParents.get(max)) {
                max = call;
            }
        }

        return max;

    }

}
