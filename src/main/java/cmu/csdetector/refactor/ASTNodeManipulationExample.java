package cmu.csdetector.refactor;
import org.eclipse.jdt.core.dom.*;

public class ASTNodeManipulationExample {

    public static void main(String[] args) {
        // Create the source AST
//        ASTParser parser = ASTParser.newParser(AST.JLS17);
//        String sourceCode = "public class MyClass {\n" +
//                "    public void myMethod() {\n" +
//                "        System.out.println(\"Hello, World!\");\n" +
//                "    }\n" +
//                "}";
//        parser.setSource(sourceCode.toCharArray());
//        CompilationUnit sourceASTRoot = (CompilationUnit) parser.createAST(null);
//
//        // Find the method declaration node to be extracted
//        MethodDeclaration methodDecl = (MethodDeclaration) sourceASTRoot.types().get(0).getMethods()[0];
//
//        // Create the target AST
//        AST targetAST = AST.newAST(AST.JLS17);
//
//        // Copy the method declaration subtree to the target AST
//        MethodDeclaration copiedMethodDecl = (MethodDeclaration) ASTNode.copySubtree(targetAST, methodDecl);
//
//        // Add the copied method declaration node to the target AST
//        TypeDeclaration targetClassDecl = targetAST.newTypeDeclaration();
//        targetClassDecl.setName(targetAST.newSimpleName("MyTargetClass"));
//        targetClassDecl.modifiers().add(targetAST.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
//        targetClassDecl.bodyDeclarations().add(copiedMethodDecl);
//
//        // Print the target AST
//        System.out.println(targetClassDecl);
    }
}
