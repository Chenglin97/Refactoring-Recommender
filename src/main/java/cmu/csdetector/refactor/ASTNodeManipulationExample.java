package cmu.csdetector.refactor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ASTNodeManipulationExample {

    public static void main(String[] args) throws IOException {
        // Set up the AST parser
//        ASTParser parser = ASTParser.newParser(AST.JLS11);
//        parser.setKind(ASTParser.K_COMPILATION_UNIT);
//
//        // Set the source file
        String filePath = "/Users/kenfunk/Downloads/RefactoringTest/testFile.java";
//        String content = new String(Files.readAllBytes(Paths.get(filePath)));
//        parser.setSource(content.toCharArray());
//        parser.setResolveBindings(true);
//        parser.setBindingsRecovery(true);
//        parser.setKind(ASTParser.K_COMPILATION_UNIT);
//        // Parse the source code and obtain the AST
//
//        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//
//        System.out.println(content);


//        // Obtain the TypeRoot node
//        AST ast = cu.getAST();
//        ast.setSourceRange(cu.getStartPosition(), cu.getLength());
//        TypeRoot typeRoot = ast.newTypeRoot(parser.getCompilationUnit());
//
//        // Find the method declaration node to remove
//        MethodDeclaration methodDecl = (MethodDeclaration) cu.findDeclaringNode("myMethod", null);
//
//        // Remove the method declaration node from the AST
//        ASTRewrite rewrite = ASTRewrite.create(ast);
//        ListRewrite listRewrite = rewrite.getListRewrite(cu, CompilationUnit.BODY_DECLARATIONS_PROPERTY);
//        listRewrite.remove(methodDecl, null);
//
//        // Apply the rewrite to the AST
//        TextEdit edits = rewrite.rewriteAST();
//        try {
//            edits.apply(cu.getAST().getASTRewrite().getOriginalCompilationUnit().getBuffer());
//        } catch (MalformedTreeException | BadLocationException e) {
//            e.printStackTrace();
//        }
//
//        // Print the modified source code
//        System.out.println(cu.toString());
    }
}
