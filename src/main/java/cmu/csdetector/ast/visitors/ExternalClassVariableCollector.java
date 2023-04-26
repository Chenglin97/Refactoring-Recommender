package cmu.csdetector.ast.visitors;

import cmu.csdetector.resources.Resource;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.HashMap;
import java.util.Map;

public class ExternalClassVariableCollector extends ASTVisitor {
    private Map<String, Integer> externalClassVariableCount;

    public Map<String, Integer> getExternalClassVariableCount() {
        return externalClassVariableCount;
    }

    public ExternalClassVariableCollector() {
        this.externalClassVariableCount = new HashMap<>();
    }
    @Override
    public boolean visit(SimpleName node) {
        // Maybe need to add declaration check
        // if (this.declaringTypeBinding == null) {
        //    return false;
        // }
        IBinding binding = node.resolveBinding();
        if (binding == null) {
            return false;
        }
        if (binding.getKind() == IBinding.VARIABLE) {
            Expression expression = (Expression) node;
            String class_name = expression.resolveTypeBinding().getName();
            externalClassVariableCount.put(class_name, externalClassVariableCount.getOrDefault(class_name, 0) + 1);
        }

        return true;
    }
}
