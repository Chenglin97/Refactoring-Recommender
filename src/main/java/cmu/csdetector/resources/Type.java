package cmu.csdetector.resources;

import cmu.csdetector.ast.visitors.MethodCollector;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.loader.SourceFile;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class Type extends Resource {

    private List<Method> methods;

    private transient Set<Type> children;

    public TypeDeclaration getNodeAsTypeDeclaration() {
        return (TypeDeclaration) getNode();
    }

    public ITypeBinding getBinding() {
        ITypeBinding binding = this.getNodeAsTypeDeclaration().resolveBinding();
        return binding;
    }

    public ITypeBinding getSuperclassBinding() {
        ITypeBinding binding = this.getNodeAsTypeDeclaration().resolveBinding();
        if (binding != null) {
            ITypeBinding superclass = binding.getSuperclass();
            return superclass;
        }
        return null;
    }



    protected void identifyKind() {
        TypeDeclaration typeDeclaration = (TypeDeclaration) getNode();
        StringBuffer buffer = new StringBuffer();
        int modifiers = typeDeclaration.getModifiers();

        if (Modifier.isPublic(modifiers)) {
            buffer.append("public ");
        }

        if (Modifier.isPrivate(modifiers)) {
            buffer.append("private ");
        }

        if (Modifier.isProtected(modifiers)) {
            buffer.append("protected ");
        }

        if (Modifier.isAbstract(modifiers)) {
            buffer.append("abstract ");
        }

        if (Modifier.isFinal(modifiers)) {
            buffer.append("final ");
        }

        if (typeDeclaration.isInterface()) {
            buffer.append("interface");
        } else {
            buffer.append("class");
        }

        this.setKind(buffer.toString());
    }

    public Type(SourceFile sourceFile, TypeDeclaration typeDeclaration) {
        super(sourceFile, typeDeclaration);
        this.children = new HashSet<>();

        IBinding binding = typeDeclaration.resolveBinding();
        if (binding != null) {
            String fqn = typeDeclaration.resolveBinding().getQualifiedName();
            setFullyQualifiedName(fqn);
        }
        this.searchForMethods();

        //register itself in the ParenthoodRegistry
        ParenthoodRegistry.getInstance().registerChild(this);
    }

    private void searchForMethods() {
        this.methods = new ArrayList<>();
        MethodCollector visitor = new MethodCollector();
        this.getNode().accept(visitor);

        List<MethodDeclaration> methodsDeclarations = visitor.getNodesCollected();

        for (MethodDeclaration methodDeclaration : methodsDeclarations) {
            Method method = new Method(getSourceFile(), methodDeclaration);
            this.methods.add(method);
        }
    }

    public Method findMethodByName(String name) {
        for (Method method : this.methods) {
            String toBeFound = this.getFullyQualifiedName() + "." + name;
            if (method.getFullyQualifiedName().equals(toBeFound)) {
                return method;
            }
        }
        return null;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public Set<Type> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Type [fqn=" + getFullyQualifiedName() + "]";
    }

    @Override
    public Type clone() {
        Type clone = new Type(getSourceFile(), (TypeDeclaration) getNode());
        clone.setFullyQualifiedName(getFullyQualifiedName());
        clone.setKind(getKind());

        for (Map.Entry<MetricName, Double> entry : getMetricsValues().entrySet()) {
            clone.addMetricValue(entry.getKey(), entry.getValue());
        }

        // Copy the methods list
        List<Method> clonedMethods = new ArrayList<>();
        for (Method method : methods) {
            Method clonedMethod = method.clone();
            clonedMethods.add(clonedMethod);
        }
        clone.methods = clonedMethods;

        Set<Type> clonedChildren = new HashSet<>(children);
        clone.children = clonedChildren;

        return clone;
    }
}
