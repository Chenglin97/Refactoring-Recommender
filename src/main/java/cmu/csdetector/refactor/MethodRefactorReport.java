package cmu.csdetector.refactor;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;

public class MethodRefactorReport {

    public Method sourceMethod;
    public Method extractedMethod;
    public String newMethodName;
    public Type sourceClass;
    public int sourceMethodStartLine;
    public int sourceMethodEndLine;
    public SmellName smellType;

    public MethodRefactorReport(Method sourceMethod, Method extractedMethod, String newMethodName, Type sourceClass, int sourceMethodStartLine, int sourceMethodEndLine, SmellName smellType) {
        this.sourceMethod = sourceMethod;
        this.extractedMethod = extractedMethod;
        this.newMethodName = newMethodName;
        this.sourceClass = sourceClass;
        this.sourceMethodStartLine = sourceMethodStartLine;
        this.sourceMethodEndLine = sourceMethodEndLine;
        this.smellType = smellType;
    }

}
