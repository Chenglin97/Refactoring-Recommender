package cmu.csdetector.ast.visitors;

import cmu.csdetector.ast.CollectorVisitor;
import org.eclipse.jdt.core.dom.*;

import java.sql.Array;
import java.util.*;

/**
 * Visits a method body in order to find all accesses class fields. During
 * SimpleName visits, this visitor uses binding to determine if the simple name refers
 * to a variable or not. If the simple name refers to a variable, the visitor checks if it is a class field
 * 
 * @author leonardo
 */
public class ClassFieldAccessCollector extends CollectorVisitor<IVariableBinding> {
	
	/**
	 * Type that declares the method being visited
	 */
	private ITypeBinding declaringTypeBinding;
	
	private Set<IVariableBinding> allVariables;

	private List<IVariableBinding> localReferences = new ArrayList<>();

	public List<IVariableBinding> getLocalReferences() {
		return localReferences;
	}

	public ClassFieldAccessCollector(TypeDeclaration declaringType) {
		this.declaringTypeBinding = declaringType.resolveBinding();
		this.allVariables = this.getVariablesInHierarchy();
	}

	public boolean visit(SimpleName node) {
		/*
		fragment used to demonstrate how to collect info on the ast for debug purpose

		if (this.declaringTypeBinding.getName().toString().equals("DummyDad")){
			System.out.println("enter"); <- put a breakpoint here
		}
		*/

		if (this.declaringTypeBinding == null) {
			return false;
		}
		
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		
		/*
		 * Checks if the binding refers to a variable's access. If so,
		 * it checks whether the variable is a field in the class.
		 */
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;



			if (!this.localReferences.contains(variableBinding) && (this.allVariables.contains(variableBinding) || checkEquality(variableBinding))) {
				this.addCollectedNode(variableBinding);
				this.localReferences.add(variableBinding);
			}
//			if (!wasAlreadyCollected(variableBinding) && this.allVariables.contains(variableBinding)) {
//				this.addCollectedNode(variableBinding);
//			}
		}
		return true;
	}

	private boolean checkEquality(IVariableBinding variableBinding) {
		for (IVariableBinding variable: this.allVariables) {
			if (variableBinding.toString().equals(variable.toString())) {
				return true;
			}
		}
		return false;
	}

	public void clearLocalReferences() {
		localReferences.clear();
	}
	
	private Set<IVariableBinding> getVariablesInHierarchy() {
		Set<IVariableBinding> variables = new HashSet<>();
		ITypeBinding type = this.declaringTypeBinding;

		boolean local = true;
		while (type != null) {
			List<IVariableBinding> localVariables = Arrays.asList(type.getDeclaredFields());
			List<IVariableBinding> validVariables = new ArrayList<>();

			for (IVariableBinding variable : localVariables) {
				if (!local) {
					int flags = variable.getModifiers();
					if (Modifier.isPrivate(flags)) {
						continue;
					}
				}
				validVariables.add(variable);
			}

			variables.addAll(validVariables);
			type = type.getSuperclass();
			local = false;
		}
		return variables;
	}

	public Set<IVariableBinding> getAllVariables() {
		return this.allVariables;
	}
	
}
