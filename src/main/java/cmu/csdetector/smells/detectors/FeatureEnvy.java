package cmu.csdetector.smells.detectors;

import cmu.csdetector.ast.visitors.ClassMethodInvocationVisitor;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellDetector;
import cmu.csdetector.smells.SmellName;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureEnvy extends SmellDetector {
    @Override
    public List<Smell> detect(Resource resource) {
        Method method = (Method)resource;

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
                Smell smell = createSmell(resource, "Method Calls to " + type.getName() + "(" + types.get(type) + ") > " + "InternalCalls(" + internalCalls + ")");
                smells.add(smell);
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

        List<Smell> smells_without_parents = new ArrayList<>();
        // iterate through external calls and create smells
        for (ITypeBinding call : externalCallsWithoutParents.keySet()) {
            Smell smell = createSmell(resource, "Method Calls to " + call.getName() + "(" + externalCalls.get(call) + ") > " + "InternalCalls(" + internalCalls + ")");
            smells_without_parents.add(smell);
        }

        return smells_without_parents;
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.FeatureEnvy;
    }
}
