package cmu.csdetector.resources;

import cmu.csdetector.ast.visitors.MethodInvocationVisitor;
import cmu.csdetector.graph.CallGraph;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.loader.SourceFile;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Method extends Resource {

    private List<String> parametersTypes;

    public IMethodBinding getBinding() {
        MethodDeclaration declaration = (MethodDeclaration)this.getNode();
        IMethodBinding binding = declaration.resolveBinding();
        return binding;
    }

    protected void identifyKind() {
        MethodDeclaration declaration = (MethodDeclaration)this.getNode();
        StringBuffer buffer = new StringBuffer();
        int modifiers = declaration.getModifiers();

        if (Modifier.isPublic(modifiers)) {
            buffer.append("public ");
        }

        if (Modifier.isPrivate(modifiers)) {
            buffer.append("private ");
        }

        if (Modifier.isProtected(modifiers)) {
            buffer.append("protected ");
        }

        if (Modifier.isStatic(modifiers)) {
            buffer.append("static ");
        }

        if (Modifier.isAbstract(modifiers)) {
            buffer.append("abstract ");
        }

        if (Modifier.isFinal(modifiers)) {
            buffer.append("final ");
        }

        buffer.append("method");
        this.setKind(buffer.toString());
    }

    /**
     * Every time a new method is declared, it must be
     * registered in the call Graph
     */
    private void registerOnCallGraph(MethodDeclaration node) {
        CallGraph graph = CallGraph.getInstance();
        IMethodBinding thisBinding = this.getBinding();
        if (thisBinding == null) {
            //TODO LOG!
            return;
        }

        /*
         * Retrieves the list of method calls made by the new
         * declared method
         */
        MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor();
        node.accept(invocationVisitor);

        for (IMethodBinding methodBinding : invocationVisitor.getCalls()) {
            if (!(methodBinding.getDeclaringClass().getQualifiedName().startsWith("java"))){
                graph.addMethodCall(thisBinding, methodBinding);
            }
        }

    }

    public Method(SourceFile sourceFile, MethodDeclaration node) {
        super(sourceFile, node);
        this.registerOnCallGraph(node);

        this.parametersTypes = new ArrayList<>();
        for(Object obj : node.parameters()) {
            SingleVariableDeclaration declaration = (SingleVariableDeclaration)obj;
            declaration.getName();
            parametersTypes.add(declaration.getType().toString());
        }

        IBinding binding = node.resolveBinding();
        if (binding != null) {
            IMethodBinding methodBinding = (IMethodBinding)binding;
            String classFqn = methodBinding.getDeclaringClass().getQualifiedName();
            setFullyQualifiedName(classFqn + "." + node.getName());
        }
    }

    public List<String> getParametersTypes() {
        return parametersTypes;
    }

    @Override
    public String toString() {
        return "Method [fqn=" + getFullyQualifiedName() + "]";
    }

    @Override
    public Method clone() {
        MethodDeclaration methodDeclaration = (MethodDeclaration) getNode();
        Method clone = new Method(getSourceFile(), (MethodDeclaration) ASTNode.copySubtree(methodDeclaration.getAST(), methodDeclaration));
        clone.setFullyQualifiedName(getFullyQualifiedName());
        clone.setKind(getKind());

        for (Map.Entry<MetricName, Double> entry : getMetricsValues().entrySet()) {
            clone.addMetricValue(entry.getKey(), entry.getValue());
        }

        // Copy the parametersTypes list
        List<String> clonedParametersTypes = new ArrayList<>(parametersTypes);
        clone.parametersTypes = clonedParametersTypes;

        return clone;
    }

}
