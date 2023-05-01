package cmu.csdetector.ast.visitors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.HashSet;
import java.util.Set;

/**
 * Assumes that the root node is a method declaration. This visitor
 * returns bindings for all distinct method calls performed inside the body of the
 * visited method
 * 
 * @author Diego Cedrim
 */
public class MethodInvocationVisitor2 extends ASTVisitor {

	private Set<MethodInvocation> calls;


	public MethodInvocationVisitor2() {
		this.calls = new HashSet<>();
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
//		if (node.getExpression() instanceof MethodInvocation) return false;
		this.calls.add(node);
		return true;
	}
	
	public Set<MethodInvocation> getCalls() {
		return calls;
	}
}

























