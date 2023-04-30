package cmu.csdetector.refactor;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;

public class ComplexClassMethodRR extends MethodRefactorReport {

    public ComplexClassMethodRR(Method method, String methodName, Type sourceClass, int sourceMethodStartLine, int sourceMethodEndLine, SmellName smellType, Type targetClass) {
        super(method, methodName, sourceClass, sourceMethodStartLine, sourceMethodEndLine, smellType);
    }


}
