package cmu.csdetector.refactor;

import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;

public class ComplexClassMethodRR extends MethodRefactorReport {

    public ComplexClassMethodRR(Method sourceMethod, Method extractedMethod, String newMethodName, Type sourceClass, int sourceMethodStartLine, int sourceMethodEndLine, SmellName smellType) {
        super(sourceMethod, extractedMethod, newMethodName, sourceClass, sourceMethodStartLine, sourceMethodEndLine, smellType);
    }
}
