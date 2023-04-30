package cmu.csdetector.refactor;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.smells.SmellName;

public class MethodRefactorReport {

    public Method method;
    public String methodName;
    public Type SourceClass;
    public int SourceMethodStartLine;
    public int SourceMethodEndLine;
    public SmellName smellType;

    public MethodRefactorReport(Method method, String methodName, Type SourceClass, int SourceMethodStartLine, int SourceMethodEndLine, SmellName smellType) {
        this.method = method;
        this.methodName = methodName;
        this.SourceClass = SourceClass;
        this.SourceMethodStartLine = SourceMethodStartLine;
        this.SourceMethodEndLine = SourceMethodEndLine;
        this.smellType = smellType;
    }

}
