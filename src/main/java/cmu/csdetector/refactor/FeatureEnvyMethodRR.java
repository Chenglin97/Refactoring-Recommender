package cmu.csdetector.refactor;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;

public class FeatureEnvyMethodRR extends MethodRefactorReport{

    public Type targetClass;

    public FeatureEnvyMethodRR(Method sourceMethod, Method extractedMethod, String newMethodName, Type sourceClass, int sourceMethodStartLine, int sourceMethodEndLine, SmellName smellType, Type targetClass) {
        super(sourceMethod, extractedMethod, newMethodName, sourceClass, sourceMethodStartLine, sourceMethodEndLine, smellType);
        this.targetClass = targetClass;
    }

    @Override
    public String toString() {
        String report = "Feature Envy: Move a portion of " + sourceMethod.getFullyQualifiedName() + " in " + sourceClass.getFullyQualifiedName() + " to " + targetClass.getFullyQualifiedName();
        report += "\n Lines " + sourceMethodStartLine + " to " + sourceMethodEndLine + " to " + targetClass.getFullyQualifiedName();
//        report += "\n Potential Method Name: " + newMethodName; /
        return report;
    }
}
