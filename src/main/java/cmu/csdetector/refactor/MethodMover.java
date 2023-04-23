package cmu.csdetector.refactor;

import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Resource;
import cmu.csdetector.resources.Type;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;



import java.util.ArrayList;

public class MethodMover {
    public Resource moveMethod(Method method, Resource source_class, ArrayList<Type> classes) {
        // returns the class the method should be moved to
        // Resource source_class = method.getBelongingClass();
        Double source_lcom3 = source_class.getMetricValue(MetricName.LCOM3);
        for (Resource target_class: classes) {
            Resource old_class = target_class;
            Double old_class_lcom3 = old_class.getMetricValue(MetricName.LCOM3);
            Resource new_class = simulateMove(method, target_class);
            Double new_class_lcom3 = new_class.getMetricValue(MetricName.LCOM3);
            Double new_source_lcom_3 = source_class.getMetricValue(MetricName.LCOM3);

            if (new_class_lcom3 < old_class_lcom3 && new_source_lcom_3 < source_lcom3) {
                return new_class;
            }
        }
        return null;
    }

    public Resource simulateMove(Resource method, Resource target_class) {
        // 1. move the method to the target class

        MethodDeclaration methodToMove = (MethodDeclaration) method.getNode();

        // Create a new AST node representing the method in the target class
        AST ast = AST.newAST(AST.JLS15); // Use the appropriate JLS level for your code
        MethodDeclaration newMethod = ast.newMethodDeclaration();
        newMethod.setName(ast.newSimpleName(methodToMove.getName().getIdentifier()));

        org.eclipse.jdt.core.dom.Type returnType = methodToMove.getReturnType2();
        if (returnType != null && !returnType.toString().equals("void")) {
            newMethod.setReturnType2(returnType);
        } else {
            newMethod.setReturnType2(null);
        }

        newMethod.parameters().addAll(methodToMove.parameters());
        newMethod.thrownExceptions().addAll(methodToMove.thrownExceptions());
        newMethod.setBody((Block) ASTNode.copySubtree(ast, methodToMove.getBody()));

        // Check if a method with the same name and parameter types already exists in the target class
        TypeDeclaration targetClass = (TypeDeclaration) target_class.getNode();
        boolean methodExists = false;
        for (Object obj : targetClass.bodyDeclarations()) {
            if (obj instanceof MethodDeclaration) {
                MethodDeclaration methodDecl = (MethodDeclaration) obj;
                if (methodDecl.getName().getIdentifier().equals(newMethod.getName().getIdentifier()) &&
                        methodDecl.parameters().toString().equals(newMethod.parameters().toString())) {
                    methodExists = true;
                    break;
                }
            }
        }

        if (methodExists) {
            throw new IllegalArgumentException("A method with the same name and parameters already exists in the target class");
        }

        // Remove the original method AST node from its containing class
        TypeDeclaration sourceClass = (TypeDeclaration) methodToMove.getParent();
        sourceClass.bodyDeclarations().remove(methodToMove);

        // Insert the new method AST node into the target class
        targetClass.bodyDeclarations().add(newMethod);

        return target_class;
    }


//    public Resource simulateMove(Resource method, Resource target_class) {
//        // TODO: implement this method
//        // 1. move the method to the target class
//
//        MethodDeclaration methodToMove = (MethodDeclaration) method.getNode();
//
//        //print
//        System.out.println("methodToMove: " + methodToMove);
//        System.out.println("methodToMove.getReturnType(): " + methodToMove.getReturnType2());
//
//        // Create a new AST node representing the method in the target class
//        AST ast = AST.newAST(AST.JLS15); // Use the appropriate JLS level for your code
//        MethodDeclaration newMethod = ast.newMethodDeclaration();
//        newMethod.setName(ast.newSimpleName(methodToMove.getName().getIdentifier()));
//
//        org.eclipse.jdt.core.dom.Type returnType = methodToMove.getReturnType2();
//        if (returnType != null && !returnType.toString().equals("void")) {
//            newMethod.setReturnType2(returnType);
//        } else {
//            newMethod.setReturnType2(null);
//        }
//
//        System.out.println("methodToMove.parameters(): " + methodToMove.parameters());
//
//        newMethod.parameters().addAll(methodToMove.parameters());
//        newMethod.thrownExceptions().addAll(methodToMove.thrownExceptions());
//        newMethod.setBody((Block) ASTNode.copySubtree(ast, methodToMove.getBody()));
//
//        // Remove the original method AST node from its containing class
//        TypeDeclaration sourceClass = (TypeDeclaration) methodToMove.getParent();
//        sourceClass.bodyDeclarations().remove(methodToMove);
//
//        // Insert the new method AST node into the target class
//        TypeDeclaration targetClass = (TypeDeclaration) target_class.getNode();
//        targetClass.bodyDeclarations().add(newMethod);
//
//        return target_class;

//        MethodDeclaration methodToMove = (MethodDeclaration)method.getNode();
//
//        // Create a new AST node representing the method in the target class
//        AST ast = AST.newAST(AST.getJLSLatest()); // Use the appropriate JLS level for your code
//        MethodDeclaration newMethod = ast.newMethodDeclaration();
//        newMethod.setName(ast.newSimpleName("methodName"));
//        newMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
//        newMethod.parameters().addAll(methodToMove.parameters());
//        newMethod.setBody(methodToMove.getBody());
//
//        // Remove the original method AST node from its containing class
//        methodToMove.getParent().bodyDeclarations().remove(methodToMove);
//
//        // Insert the new method AST node into the target class
//        TypeDeclaration targetClass = findClass(cu, targetClassName);
//        targetClass.bodyDeclarations().add(newMethod);
//
//        return target_class;
//    }
    public Resource getBelongingClass(Resource method) {
        return null;
    }
    // 0. Imagine we have a method to move
    // 1. get the class with the highest number of calls
    // 2. move the method to that class
    // 3. run feature envy detector again
    // not for now but maybe for the future: if the method is still feature envy, move it to the next class
    // not for now but maybe for the future: repeat until the method is not feature envy anymore
    // return the name of the class to move the method
}
