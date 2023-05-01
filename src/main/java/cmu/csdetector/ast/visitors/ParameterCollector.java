package cmu.csdetector.ast.visitors;

import cmu.csdetector.ast.CollectorVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class ParameterCollector extends ASTVisitor{

	Set<String> parameters;

	public ParameterCollector() {
		this.parameters = new HashSet<>();
	}

	@Override
	public boolean visit(SimpleName node) {
		if (node.getParent() instanceof VariableDeclarationFragment && node.getParent().getParent() instanceof VariableDeclarationStatement) {
			return false;
		}
		if (Character.isUpperCase(node.getIdentifier().charAt(0))) {
			return false;
		}
		if (node.getParent() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node.getParent();
			if (methodInvocation.getExpression() != null && methodInvocation.getExpression().toString().equals(node.getIdentifier())) {
				this.parameters.add(node.getIdentifier());
			}
		} else {
			this.parameters.add(node.getIdentifier());
		}

		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		return true;
	}

	public Set<String> getParameters() {
		return this.parameters;
	}

}
