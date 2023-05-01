package cmu.csdetector.refactor;

import cmu.csdetector.ast.ASTBuilder;
import cmu.csdetector.ast.visitors.ExpressionStatementVisitor;
import cmu.csdetector.ast.visitors.ParameterCollector;
import cmu.csdetector.ast.visitors.StatementCollector;
import cmu.csdetector.metrics.calculators.type.LCOM2Calculator;
import cmu.csdetector.resources.Method;
import org.eclipse.jdt.core.dom.*;
import java.util.*;

public class Heuristic1 {

    private static final double MAX_SIZE_DIFFERENCE = 0.2;
    private static final double MIN_OVERLAP = 0.1;
    private static final double SIGNIFICANT_DIFFERENCE_THRESHOLD = 0.01;

    private final Method method;
    private final String[] sourcePaths;
    private List<ASTNode> statementNodes;
    private TreeMap<Integer, Set<String>> matrix;
    private Set<List<Integer>> clusters = new HashSet<>();
    private List<ExtractMethodOpportunity> opportunities = new ArrayList<>();
    private TypeDeclaration classAfterAddingCluster;


    public Heuristic1(Method method, List<String> sourcePaths) {
        this.method = method;

        String[] paths = new String[sourcePaths.size()];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = sourcePaths.get(i);
        }

        this.sourcePaths = paths;
    }

    public List<Integer> generateExtractOpportunity() {
        ASTNode node = this.method.getNode();
        StatementCollector statementCollector = new StatementCollector();
        node.accept(statementCollector);
        this.statementNodes = statementCollector.getNodesCollected();
        this.matrix = statementCollector.getMatrix();
        HashMap<String, List<Integer>> transformedMatrix = this.transformMatrix(this.matrix);

        // Generate clusters
        this.generateClusters(transformedMatrix, matrix.size());
        System.out.println("\nAll Clusters: " + this.clusters);
        if (this.clusters.isEmpty()) return new ArrayList<>();

        // Remove invalid clusters
//        System.out.println("Nodes: " + ASTNode.nodeClassForType(statementNodes.get(28).getNodeType()).getSimpleName());
//        System.out.println("Nodes: " + statementNodes.get(41));
//        for (ASTNode ancestor : this.getAncestors(statementNodes.get(7))) {
//            System.out.println(ASTNode.nodeClassForType(ancestor.getNodeType()));
//        }

        this.removeInvalidClusters();
        System.out.println("\nValid Clusters: " + this.clusters);

        for (List<Integer> cluster : this.clusters) {
            ExtractMethodOpportunity emo = new ExtractMethodOpportunity(cluster);
            emo.setParameters(this.getParameters(cluster));
            emo.setReturnType(this.getReturnType(cluster));
            System.out.println(emo.getReturnType());
            this.opportunities.add(emo);
        }

        double originalLCOM2 = calculateLCOM2(List.of(1, this.matrix.size()));
        System.out.println("OriginalLCOM2: " + originalLCOM2);
        for (ExtractMethodOpportunity opportunity : this.opportunities) {
            double opportunityLCOM2 = calculateLCOM2(opportunity.getCluster());
            double refactoredLCOM2 = calculateRestLCOM2(opportunity.getCluster());
            double benefit = originalLCOM2 - Math.max(opportunityLCOM2, refactoredLCOM2);
//            System.out.println("Cluster: " + opportunity.getCluster() + " Benefit: " + benefit);
            opportunity.setBenefit(benefit);
        }
        this.groupClusters();


        return getBestCluster();
    }

    private List<String> getParameters(List<Integer> cluster) {
        List<ASTNode> nodes = this.statementToMove(this.statementNodes, cluster);
        Set<String> parameters = new HashSet<>();
        for (ASTNode node : nodes) {
            ParameterCollector parameterCollector = new ParameterCollector();
            node.accept(parameterCollector);
            parameters.addAll(parameterCollector.getParameters());
        }
        return new ArrayList<>(parameters);
    }
    private double calculateLCOM2(List<Integer> cluster) {
        double p = 0, q = 0;
        for (int i = cluster.get(0); i < cluster.get(1); i++) {
            for (int j = i+1; j <= cluster.get(1); j++) {
                Set<String> intersection = new HashSet<>(this.matrix.get(i));
                intersection.retainAll(this.matrix.get(j));
                if (!intersection.isEmpty()) {
                    q++;
                } else {
                    p++;
                }
            }
        }
        return Math.max(p - q, 0);
    }
    private double calculateRestLCOM2(List<Integer> cluster) {
        double p = 0, q = 0;
        for (int i = 1; i < this.matrix.size(); i++) {
            if (i >= cluster.get(0) && i <= cluster.get(1)) continue;
            for (int j = i+1; j <= this.matrix.size(); j++) {
                if (j >= cluster.get(0) && j <= cluster.get(1)) continue;
                Set<String> intersection = new HashSet<>(this.matrix.get(i));
                intersection.retainAll(this.matrix.get(j));
                if (!intersection.isEmpty()) {
                    q++;
                } else {
                    p++;
                }
            }
        }
        return Math.max(p - q, 0);
    }

    private String getReturnType(List<Integer> cluster) {
        List<ASTNode> nodes = this.statementToMove(this.statementNodes, cluster);
        Set<String> assignments = new HashSet<>();

        for (ASTNode node : nodes) {
            ExpressionStatementVisitor assignStatementVisitor = new ExpressionStatementVisitor();
            node.accept(assignStatementVisitor);
            assignments.addAll(assignStatementVisitor.getExpressions());
        }

        List<String> potentialReturnType = new ArrayList<>(assignments);
        if (cluster.get(1) + 1 > this.matrix.size()) {
            return "";
        }

        List<String> parametersAfterCluster = this.getParameters(List.of(cluster.get(1)+1, this.matrix.size()));

        boolean isIntersected = potentialReturnType.retainAll(parametersAfterCluster);

        if (isIntersected) {
            if (potentialReturnType.size() == 1) {
                return potentialReturnType.get(0);
            }
        }

        return "";
    }

    private List<Integer> getBestCluster() {

        // temp ranking
        LCOM2Calculator lcom2Calculator = new LCOM2Calculator();
        CompilationUnit c = (CompilationUnit) this.statementNodes.get(0).getRoot();

        Double oldLcom2 = lcom2Calculator.getValue((TypeDeclaration) c.types().get(0));
        System.out.println("Old LCOM2: " + oldLcom2);

        double bestBenefit = 0;
        List<Integer> bestCluster = null;
        Set<List<Integer>> clusters = this.clusters;

        // TODO use ranking system to rank the clusters
        for (List<Integer> cluster : clusters) {

            TypeDeclaration classAfterAddingCluster = this.createNewClassAfterAddingCluster(cluster);
            this.classAfterAddingCluster = classAfterAddingCluster;

            Double newLcom2 = lcom2Calculator.getValue(classAfterAddingCluster);

            double benefit = oldLcom2 - Math.max(oldLcom2, newLcom2);
            System.out.println("New LCOM2: " + newLcom2 + ", benefit: " + benefit);

            if (benefit >= bestBenefit) {
                bestBenefit = benefit;
                bestCluster = cluster;
            }
        }

        return bestCluster;
    }

    public TypeDeclaration createNewClassAfterAddingCluster(List<Integer> cluster) {
        String newSourceCode = this.getNewSourceCode(cluster);
        ASTBuilder builder = new ASTBuilder(this.sourcePaths);
        ASTParser parser = builder.create();
        parser.setSource(newSourceCode.toCharArray());
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        return (TypeDeclaration) compilationUnit.types().get(0);
    }

    public TypeDeclaration getClassAfterAddingCluster() {
        return this.classAfterAddingCluster;
    }

    private String getNewSourceCode(List<Integer> cluster){

        CompilationUnit compilationUnit = this.method.getSourceFile().getCompilationUnit();

        AST ast = compilationUnit.getAST();

        // Get nodes to move
        List<ASTNode> nodes = this.statementToMove(this.statementNodes, cluster);

        if (nodes.size() == 0) {
            return compilationUnit.toString();
        }

//        MethodInvocation methodInvocation = ast.newMethodInvocation();
//
//        // Set the name of the method to call
//        SimpleName methodToCall = ast.newSimpleName("iLoveRefactoringSoMuchFunMethod");
//        methodInvocation.setName(methodToCall);
//
//        // Create the arguments for the method call
//        List<Expression> arguments = methodInvocation.arguments();
//        Expression argument1 = ast.newSimpleName("arg1");
//        Expression argument2 = ast.newSimpleName("arg2");
//        arguments.add(argument1);
//        arguments.add(argument2);
//
//        // Set the expression to call the method on
//        Expression expression = ast.newSimpleName("object");
//        methodInvocation.setExpression(expression);
//
//        // Add the method call to a statement
//        ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
//
//        Block parentBlock = (Block) nodes.get(0).getParent();
//        int index = parentBlock.statements().indexOf(nodes.get(0));
//        parentBlock.statements().add(index, expressionStatement);

        // New a method
        MethodDeclaration methodDeclaration = ast.newMethodDeclaration();
        methodDeclaration.setName(ast.newSimpleName("iLoveRefactoringSoMuchFunMethod"));
        Block body = ast.newBlock();
        methodDeclaration.setBody(body);

        List<Integer> deleteNodesIndex = new ArrayList<>();
        for (ASTNode node : nodes) {
            if (node instanceof Block) {
                node = node.getParent();
            }

            Block block = (Block) node.getParent();
            int index = block.statements().indexOf(node);
            deleteNodesIndex.add(index);
        }

        List<Block> deteteNodesParent = new ArrayList<>();
        for (ASTNode node : nodes) {

            // Sometimes it's a block, which is ifstatement
            if (node instanceof Block) {
                node = node.getParent();
            }

            // Remove child from parent
            Block block = (Block) node.getParent();
            deteteNodesParent.add(block);
            block.statements().remove(node);

            // Add statement into new method
            body.statements().add(node);
        }

        TypeDeclaration type = (TypeDeclaration) compilationUnit.types().get(0);
        type.bodyDeclarations().add(methodDeclaration);

        String returnString = compilationUnit.toString();

        type.bodyDeclarations().remove(methodDeclaration);

        for (int i = 0; i < deteteNodesParent.size(); i++) {

            ASTNode node = nodes.get(i);

            // Sometimes it's a block, which is ifstatement
            if (node instanceof Block) {
                node = node.getParent();
            }

            // Remove child from parent
            Block block = (Block) node.getParent();
            block.statements().remove(node);

            Block originalBlock = deteteNodesParent.get(i);
            int index = deleteNodesIndex.get(i);
            originalBlock.statements().add(index, node);
        }

        String recoverString = compilationUnit.toString();

        return returnString;
    }

    private List<ASTNode> statementToMove(List<ASTNode> statementNodes, List<Integer> cluster) {
        if (statementNodes == null || statementNodes.size() == 0 || cluster == null || cluster.size() == 0) {
            return new ArrayList<>();
        }
        int start = cluster.get(0)-1;
        int end = cluster.get(1)-1;
        ASTNode node = statementNodes.get(start);

        if (node instanceof Block) {
            node = node.getParent();
        }

        if (!(node.getParent() instanceof Block)) {
            return new ArrayList<>();
        }

        HashSet<ASTNode> moveList = new HashSet<>();
        List<ASTNode> returnList = new ArrayList<>();
        ASTNode parent = node.getParent();
        for (int i = start; i <= end; i++) {
            ASTNode candidate = statementNodes.get(i);
            if (candidate instanceof Block) {
                candidate = candidate.getParent();
            }
            if (moveList.contains(candidate)) {
                continue;
            }
            if (candidate.getParent().equals(parent)) {
                returnList.add(candidate);
                moveList.add(candidate);
            }
        }
        return returnList;
    }

    private List<List<ExtractMethodOpportunity>> groupClusters() {
        // TODO: Group by benefit
        for (int i = 0; i < this.opportunities.size(); i++) {
            ExtractMethodOpportunity opp = this.opportunities.get(i);
            if (opp.isAlternative) continue;
            for (int j = i+1; j < this.opportunities.size(); j++) {
                ExtractMethodOpportunity other_opp = this.opportunities.get(j);
                if (!other_opp.isAlternative && opp.differenceInSize(other_opp) <= MAX_SIZE_DIFFERENCE && opp.overlap(other_opp) >= MIN_OVERLAP) {
                    if (opp.getBenefit() > other_opp.getBenefit()) {
                        opp.addAlternative(other_opp);
                        other_opp.isAlternative = true;
                    } else {
                        other_opp.addAlternative(opp);
                        opp.isAlternative = true;
                        opp = this.opportunities.get(j);
                    }
                }
            }
        }
        List<List<ExtractMethodOpportunity>> groups = new ArrayList<>();
        for (ExtractMethodOpportunity opp : this.opportunities) {
            if (!opp.isAlternative) {
                List<ExtractMethodOpportunity> group = new ArrayList<>();
                group.add(opp);
                group.addAll(opp.getAlternatives());
                groups.add(group);
            }
        }
        groups.sort(Comparator.comparingDouble(a -> a.get(0).getBenefit()));
        Collections.reverse(groups);
        for (List<ExtractMethodOpportunity> group : groups) {
            System.out.print("\nGroup: ");
            for (ExtractMethodOpportunity opp : group) {
                System.out.print(opp.getCluster() + " Benefit: " + opp.getBenefit() + ", ");
            }
        }
        return groups;
    }

    private void removeInvalidClusters() {
        Set<List<Integer>> invalidClusters = new HashSet<>();
        for (List<Integer> cluster : this.clusters) {
            if (!this.isSyntacticallyValid(cluster)) invalidClusters.add(cluster);
            if (!this.isLoopValid(cluster)) invalidClusters.add(cluster);
        }
        this.clusters.removeAll(invalidClusters);
    }

    private boolean isSyntacticallyValid(List<Integer> cluster) {
        ASTNode start = this.statementNodes.get(cluster.get(0)-1);
        if (start.getParent() instanceof IfStatement) start = start.getParent();
        if (start.getParent() instanceof IfStatement) { return false; } // a.k.a. the cluster starts on an else if
        ASTNode end = this.statementNodes.get(cluster.get(1)-1);
        ASTNode next = (cluster.get(1) < this.statementNodes.size()) ? this.statementNodes.get(cluster.get(1)) : null;
        if ((next == null || this.getAncestors(start).contains(next.getParent())) && this.getAncestors(end).contains(start.getParent())) {
//            int c = (int)Math.signum(this.getAncestors(end).size() - this.getAncestors(start).size());
//            switch (c) {
//                case -1:
//                    break;
//                case 0:
//                    if (end.getParent().equals(start.getParent())) return true;
//                case 1:
//                    return true;
//            }
            return true;
        }
        return false;
    }
    private boolean isLoopValid(List<Integer> cluster) {
        List<ASTNode> clusterNodes = new ArrayList<>();
        for (int i = cluster.get(0)-1; i < cluster.get(1); i++) {
            ASTNode node = this.statementNodes.get(i);
            clusterNodes.add(node);
            switch (ASTNode.nodeClassForType(node.getNodeType()).getSimpleName()) {
                case "BreakStatement":
                    while (!(node instanceof ForStatement || node instanceof WhileStatement || node instanceof SwitchStatement)) {
                        node = node.getParent();
                    }
                    if (!clusterNodes.contains(node)) return false;
                    break;
                case "ContinueStatement":
                    while (!(node instanceof ForStatement || node instanceof WhileStatement)) {
                        node = node.getParent();
                    }
                    if (!clusterNodes.contains(node)) return false;
                    break;
                case "SwitchCase":
                    while (!(node instanceof SwitchStatement)) {
                        node = node.getParent();
                    }
                    if (!clusterNodes.contains(node)) return false;
                    break;
            }
        }
        return true;
    }

    private List<ASTNode> getAncestors(ASTNode node) {
        List<ASTNode> ancestors = new ArrayList<>();
        while (node.getParent() != null) {
            node = node.getParent();
            ancestors.add(node);
        }
        return ancestors;
    }

    public void testClustering() {
        Map<String, List<Integer>> matrix = new HashMap<>();
        matrix.put("manifests", List.of(2,25,29,30,33));
        matrix.put("rcs", List.of(2,3,5,6,7,8,12,13,14,16,18));
        matrix.put("length", List.of(2,3,10));
        matrix.put("rcs.length", List.of(2,3,10));
        matrix.put("i", List.of(3,5,6,7,8,12,13,14,16,18,29,30));
        matrix.put("rec", List.of(4,6,8,10,11,25));
        matrix.put("grabRes", List.of(6));
        matrix.put("grabNonFileSetRes", List.of(8));
        matrix.put("j", List.of(10,11,25));
        matrix.put("name", List.of(11,15,21,24));
        matrix.put("rec.getName.replace", List.of(11));
        matrix.put("getName.replace", List.of(11));
        matrix.put("getName", List.of(11));
        matrix.put("replace", List.of(11));
        matrix.put("afs", List.of(13,14,15,16,17,18));
        matrix.put("equals", List.of(14,16,18));
        matrix.put("afs.getFullpath", List.of(14,15,16,18));
        matrix.put("getProj;", List.of(14,15,16,17));
        matrix.put("name.afs.getFullpath", List.of(15));
        matrix.put("afs.getPref;", List.of(16,17,18));
        matrix.put("getPref", List.of(16,17,18));
        matrix.put("pr", List.of(17,18,19,21));
        matrix.put("pr.endsWith", List.of(18));
        matrix.put("endsWith", List.of(18));
        matrix.put("name.equalsIgnoreCase", List.of(24));
        matrix.put("equalsIgnoreCase", List.of(24));
        matrix.put("rec.length", List.of(10));
        generateClusters(matrix, 34);
    }

    private void generateClusters(Map<String, List<Integer>> matrix, int loc) {
//        TreeMap<Integer, List<List<Integer>>> clustersByStep = new TreeMap<>();
        for (int step = 1; step <= loc; step++) {
//            System.out.print("\nStep " + step + ": ");
            List<List<Integer>> stepClusters = new ArrayList<>();
            for (String node : matrix.keySet()) {
                List<Integer> sortedLines = matrix.get(node);
                List<List<Integer>> individualClusters = generateStepClusters(sortedLines, step);
                stepClusters.addAll(individualClusters);
            }
            stepClusters = new ArrayList<>(new HashSet<>(stepClusters));
            stepClusters = mergeAndSortClusters(stepClusters);
            this.clusters.addAll(stepClusters);
//            clustersByStep.put(step, stepClusters);

//            System.out.println(stepClusters);
        }
    }

    public static int findIntegerInRange(List<Integer> list, int minRange, int maxRange) {
        for (int i : list) {
            if (i >= minRange && i <= maxRange) {
                return i;
            }
        }
        return -1; // Indicates that no integer in the range was found
    }

    private List<List<Integer>> mergeAndSortClusters(List<List<Integer>> baseClusters) {
        if (baseClusters.isEmpty()) return baseClusters;

        baseClusters.sort(Comparator.comparingInt((List<Integer> a) -> a.get(0)));
        List<List<Integer>> mergedClusters = new ArrayList<>();

        for (int i = 0; i < baseClusters.size(); i++) {
            int i_low = baseClusters.get(i).get(0);
            int i_high = baseClusters.get(i).get(1);
            List<Integer> endPoints = new ArrayList<>();
            for (int j = i+1; j < baseClusters.size(); j++) {
                int j_low = baseClusters.get(j).get(0);
                int j_high = baseClusters.get(j).get(1);
                // case 1
                if (j_low < i_high && j_high > i_high) {
                    mergedClusters.add(List.of(i_low, j_high));
                    endPoints.add(j_high);
                } else {
                    // case 2
                    int foundIndex = findIntegerInRange(endPoints, j_low, j_high);
                    if (foundIndex != -1){
                        mergedClusters.add(List.of(i_low, j_high));
                        endPoints.add(j_high);
                    }
                }
            }
        }

        mergedClusters.addAll(baseClusters);
        mergedClusters = new ArrayList<>(new HashSet<>(mergedClusters));
        mergedClusters.sort(Comparator.comparingInt((List<Integer> a) -> a.get(0)));
        return mergedClusters;
    }

    private List<List<Integer>> generateStepClusters(List<Integer> sortedLines, int step) {
        List<List<Integer>> clusters = new ArrayList<>();
        List<Integer> cluster = new ArrayList<>();
        for (int i = 0; i < sortedLines.size(); i++) {
            if (i == 0 || sortedLines.get(i) - sortedLines.get(i-1) > step) {
                if (cluster.size() > 1) {
                    clusters.add(List.of(cluster.get(0), cluster.get(cluster.size()-1)));
                }
                cluster = new ArrayList<>();
                cluster.add(sortedLines.get(i));
            } else {
                cluster.add(sortedLines.get(i));
            }
        }
        if (cluster.size() > 1) {
            clusters.add(List.of(cluster.get(0), cluster.get(cluster.size()-1)));
        }
        return clusters;
    }

    private HashMap<String, List<Integer>> transformMatrix(TreeMap<Integer, Set<String>> matrix){
        HashMap<String, List<Integer>> transformedMatrix =new HashMap<>();

        for(Integer key : matrix.keySet()) {
            Set<String> s = matrix.get(key);
            List<Integer> newList = new ArrayList<Integer>();
            for(String name: s) {
                if(transformedMatrix.keySet().contains(name)){
                    newList = transformedMatrix.get(name);
                    newList.add(key);
                } else{
                    newList.add(key);
                }
                HashSet<Integer> finalSet = new HashSet<>(newList);
                List<Integer> finalList = new ArrayList<>(finalSet);
                Collections.sort(finalList);
                transformedMatrix.put(name, finalList);
            }
        }
        return transformedMatrix;
    }

    public List<ASTNode> getStatementNodes() {
        return statementNodes;
    }
}


//    back up code
//    private String move(List<ASTNode> statementNodes, Type type){
//
//        ASTNode nodeToMove = statementNodes.get(9);
//        if (nodeToMove instanceof Block) {
//            nodeToMove = nodeToMove.getParent();
//        }
//
//        CompilationUnit cu = (CompilationUnit) nodeToMove.getRoot();
//
//        // Create ASTRewrite
//        AST ast = cu.getAST();
//        ASTRewrite rewriter = ASTRewrite.create(ast);
//
//        MethodDeclaration newMethod = ast.newMethodDeclaration();
//        newMethod.setName(ast.newSimpleName("newMethod"));
//
//        // create new variable declaration statements
//        VariableDeclarationFragment var1 = ast.newVariableDeclarationFragment();
//        var1.setName(ast.newSimpleName("var1"));
//        var1.setInitializer(ast.newNumberLiteral("1"));
//        VariableDeclarationStatement varStmt1 = ast.newVariableDeclarationStatement(var1);
//
////        // add variable declarations to method body
//        Block body = ast.newBlock();
//        newMethod.setBody(body);
//
////        newMethod.getBody();
//
//        newMethod.getBody().statements().add(nodeToMove);
//        newMethod.getBody().statements().add(statementNodes.get(0));
//        newMethod.getBody().statements().add(statementNodes.get(18).getParent());
//
//        ListRewrite listRewrite = rewriter.getListRewrite(cu, CompilationUnit.TYPES_PROPERTY);
//        listRewrite.insertLast(newMethod, null);
//
//        Path path = Paths.get(type.getSourceFile().getFile().getAbsolutePath());
//        String sourceCode = null;
//        try {
//            sourceCode = Files.readString(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Apply the rewrite to the document
//        Document document = new Document(sourceCode);
//        TextEdit edits = rewriter.rewriteAST(document, null);
//        try {
//            edits.apply(document);
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//        }
//
//        ASTParser parser = ASTParser.newParser(AST.JLS11);
//        parser.setKind(ASTParser.K_COMPILATION_UNIT);
//        parser.setSource(sourceCode.toCharArray());
//        parser.setResolveBindings(true);
//
//        // Create a new AST from the modified document
//        parser.setSource(document.get().toCharArray());
//        CompilationUnit newCu = (CompilationUnit) parser.createAST(null);
//
//        // Print the new AST
//        System.out.println(newCu.toString());
//
//        sourceCode = document.get();
//
//        return sourceCode;
//    }

// Merge clusters algorithms
// Old Algorithm
//        int low = baseClusters.get(0).get(0);
//        int high = baseClusters.get(0).get(1);
//        for (int i = 1; i < baseClusters.size(); i++) {
//            int i_low = baseClusters.get(i).get(0);
//            int i_high = baseClusters.get(i).get(1);
//            if (i_low <= high) {
//                if (high < i_high) high = i_high;
//            } else {
//                mergedClusters.add(List.of(low, high));
//                low = i_low;
//                high = i_high;
//            }
//        }
//        mergedClusters.add(List.of(low, high));

// New Algorithm
//        for (int i = 0; i < baseClusters.size(); i++) {
//            int low = baseClusters.get(i).get(0);
//            int i_high = baseClusters.get(i).get(1);
//            int high = i_high;
//            for (int j = i+1; j < baseClusters.size(); j++) {
//                int j_low = baseClusters.get(j).get(0);
//                int j_high = baseClusters.get(j).get(1);
//                if (j_low <= high) {
//                    if (j_low <= i_high) {
//                        if (i_high < j_high) {
//                            mergedClusters.add(List.of(low, j_high));
//                        }
//                    }
//                    if (high < j_high) {
//                        high = j_high;
//                        mergedClusters.add(List.of(low, high));
//                    }
//                } else {
//                    mergedClusters.add(List.of(low, high));
//                    break;
//                }
//            }
//        }


//        // add input parameters with cluster
//        for (ExtractMethodOpportunity extraMethodOpp : this.opportunities){
//            System.out.println(extraMethodOpp.getCluster());
//            int left_cluster = extraMethodOpp.getCluster().get(0);
//            int right_cluster = extraMethodOpp.getCluster().get(1);
//            ASTNode finalASTNode = statementNodes.get(left_cluster-1);
//            String str = statementNodes.get(left_cluster-1).toString();
//            // check whether it's a cluster or not
//            if (str.charAt(0) == '{'){
//                finalASTNode = statementNodes.get(left_cluster-1).getParent();
//            }
//        }


