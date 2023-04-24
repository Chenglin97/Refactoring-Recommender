package cmu.csdetector.ast.visitors;

import cmu.csdetector.ast.CollectorVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class StatementCollector extends CollectorVisitor<ASTNode> {

	private Integer loc;

	private TreeMap<Integer, Set<String>> matrix;

	private Stack<Set<String>> ifStatementStack;

	private boolean isIf;

	public StatementCollector() {
		this.loc = 0;
		this.isIf = false;
		this.matrix = new TreeMap<>();
		this.ifStatementStack = new Stack<>();
	}

	public TreeMap<Integer, Set<String>> getMatrix() {
		return this.matrix;
	}

	// add accessible variables and method calls
	private void addName(String name) {
		if (Character.isUpperCase(name.charAt(0))){
			return;
		}
		if (isIf) {
			this.ifStatementStack.peek().add(name);
		} else {
			int lastKey = this.matrix.lastKey();
			Set<String> accessSet = this.matrix.get(lastKey);
			accessSet.add(name);
		}
	}

	@Override
	public boolean visit(SimpleName node) {
		if (this.matrix.size() > 0) {
			this.addName(node.getIdentifier());
		}
		return true;
	}

	private void createNewStatement(ASTNode node) {
		super.addCollectedNode(node);
		this.loc++;
		this.matrix.put(this.loc, new HashSet<>());
	}

	public boolean visit(DoStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(EnhancedForStatement node) {
		this.createNewStatement(node);
		return true;
	}

	// doesn't contain any variables or methods
	public boolean visit(TryStatement node) {
		return true;
	}

	// doesn't contain any variables or methods
	public boolean visit(CatchClause node) {
		return false;
	}

	public boolean visit(ExpressionStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(ForStatement node) {
		this.createNewStatement(node);
		// store the simple name
		return true;
	}

	public boolean visit(IfStatement node) {
		this.isIf = true;
		Set<String> newIfStatment = new HashSet<>();
		if (this.ifStatementStack.size() > 0) {
			newIfStatment = new HashSet<>(this.ifStatementStack.peek());
		}
		this.ifStatementStack.add(newIfStatment);
		return true;
	}

	public boolean visit(LabeledStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(ReturnStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(SwitchStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(SynchronizedStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(ThrowStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(VariableDeclarationStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(WhileStatement node) {
		this.createNewStatement(node);
		return true;
	}

	/**
	 * More accessible variables and method calls
	 */

	// for if statement, use block.getParent() to get parent node and then manipulate the parent node
	public boolean visit(Block node) {
		if (node.getParent() instanceof IfStatement) {
			this.createNewStatement(node);
			this.matrix.put(loc, this.ifStatementStack.peek());
			this.isIf = false;
		}
		return true;
	}

	public boolean visit(ThisExpression node) {
		this.addName("this");
		return true;
	}

	public boolean visit(SimpleType node) {
		return false;
	}

	public boolean visit(FieldAccess node) {
		this.addName(node.getExpression() + "." + node.getName());
		return true;
	}

	public boolean visit(MethodInvocation node) {
		if (node.getExpression() != null && !"\"\"".equals(node.getExpression().toString())) {
			this.addName(node.getExpression() + "." + node.getName());
		}
		return true;
	}

	public boolean visit(ParameterizedType node) {
		return false;
	}

	public boolean visit(QualifiedName node) {
		this.addName(node.getQualifier().getFullyQualifiedName() + "." + node.getName());
		return true;
	}

	public void endVisit(IfStatement node) {
		if(this.ifStatementStack.size() > 0) {
			this.ifStatementStack.pop();
		}
	}

}
