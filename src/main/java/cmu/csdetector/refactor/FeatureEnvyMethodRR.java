package cmu.csdetector.refactor;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;

public class FeatureEnvyMethodRR extends MethodRefactorReport{

    public Type targetClass;

    public FeatureEnvyMethodRR(Method method, String methodName, Type sourceClass, int sourceMethodStartLine, int sourceMethodEndLine, SmellName smellType, Type targetClass) {
        super(method, methodName, sourceClass, sourceMethodStartLine, sourceMethodEndLine, smellType);
        targetClass = targetClass;
    }
}
