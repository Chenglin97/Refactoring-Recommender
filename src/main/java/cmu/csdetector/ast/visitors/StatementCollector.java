package cmu.csdetector.ast.visitors;

import cmu.csdetector.ast.CollectorVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class StatementCollector extends CollectorVisitor<ASTNode> {

	private Integer loc;

	private List<Set<String>> matrix;

	public StatementCollector() {
		this.loc = 0;
		this.matrix = new ArrayList<>();
	}

	public List<Set<String>> getMatrix() {
		return this.matrix;
	}

	// add accessible variables and method calls
	private void addName(String name) {
		int lastIndex = this.matrix.size() - 1;
		Set<String> accessSet = this.matrix.get(lastIndex);
		accessSet.add(name);
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
		this.matrix.add(new HashSet<>());
	}

	public boolean visit(DoStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(EmptyStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(EnhancedForStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(TryStatement node) {
		return true;
	}

	public boolean visit(CatchClause node) {
		return false;
	}

	public boolean visit(ExpressionStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(ForStatement node) {
		this.createNewStatement(node);
		return true;
	}

	public boolean visit(IfStatement node) {
		this.createNewStatement(node);
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

	public boolean visit(ThisExpression node) {
		this.addName("this");
		return true;
	}

	public boolean visit(SimpleType node) {
		return false;
	}
	// not sure   throw new Exception(name);
	public boolean visit(ClassInstanceCreation node) {
		return true;
	}

	public boolean visit(CreationReference node) {
		return true;
	}
	//System.out::println
	public boolean visit(ExpressionMethodReference node) {
		return true;
	}
	// this.rentals
	public boolean visit(FieldAccess node) {
		this.addName(node.getExpression() + "." + node.getName());
		return true;
	}

	public boolean visit(MethodRefParameter node) {
		return true;
	}
	// this.rentals.iterator(), rentals.hasNext()
	public boolean visit(MethodInvocation node) {
		this.addName(node.getExpression() + "." + node.getName());
		return true;
	}

	public boolean visit(NameQualifiedType node) {
		return true;
	}

	// Iterator<Rental>
	public boolean visit(ParameterizedType node) {
		return false;
	}

	//System.out, Movie.NEW_RELEASE
	public boolean visit(QualifiedName node) {
		return false;
	}


}
