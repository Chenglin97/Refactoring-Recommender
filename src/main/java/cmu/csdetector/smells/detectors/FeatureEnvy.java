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
        for (ITypeBinding type : types.keySet()) {
            if (types.get(type) > internalCalls) {
                Smell smell = createSmell(resource, "Method Calls to " + type.getName() + "(" + types.get(type) + ") > " + "InternalCalls(" + internalCalls + ")");
                smells.add(smell);
            }
        }

        return smells;
    }

    @Override
    protected SmellName getSmellName() {
        return SmellName.FeatureEnvy;
    }
}
