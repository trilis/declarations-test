import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DestructuringRefactor {

    private static List<AstNode> variableNames = new ArrayList<>();
    private static String arrayName;
    private static double index = -1;
    private static AstNode placeToInsert = null;
    private static org.mozilla.javascript.Node placeBeforeInsert = null;


    private static void insertRefactoredVariableDeclaration() {
        var declaration = new VariableDeclaration();
        declaration.setIsStatement(true);
        var initializer = new VariableInitializer();
        var name = new Name();
        name.setIdentifier(arrayName);
        var arrayLiteral = new ArrayLiteral();
        arrayLiteral.setElements(variableNames);
        declaration.addVariable(initializer);
        initializer.setInitializer(name);
        initializer.setTarget(arrayLiteral);
        if (placeBeforeInsert != null) {
            placeToInsert.addChildAfter(declaration, placeBeforeInsert);
        } else {
            placeToInsert.addChildToFront(declaration);
        }
        variableNames.clear();
        index = -1;
    }

    public static void refactor(String fileName) throws IOException {
        var parser = new Parser();
        var root = parser.parse(new FileReader(fileName + ".js"), "", 0);
        root.visit(node -> {
            System.out.println(node.getClass());
            if (node instanceof VariableDeclaration) {
                var initializer = (VariableInitializer) ((VariableDeclaration) node).getVariables().get(0);
                var body = initializer.getInitializer();
                if (body instanceof ElementGet) {
                    var elementGet = (ElementGet) body;
                    var currentArrayName = elementGet.getTarget().getString();
                    var currentIndex = elementGet.getElement().getDouble();
                    var currentVariableName = initializer.getTarget().getString();
                    var name = new Name();
                    if (!variableNames.isEmpty() && (!arrayName.equals(currentArrayName) || currentIndex - index != 1)) {
                        insertRefactoredVariableDeclaration();
                    }
                    if (!variableNames.isEmpty() || currentIndex == 0) {
                        name.setIdentifier(currentVariableName);
                        variableNames.add(name);
                        index = currentIndex;
                        arrayName = currentArrayName;
                        placeToInsert = node.getParent();
                        placeBeforeInsert = node.getParent().getChildBefore(node);
                        node.getParent().removeChild(node);
                        return false;
                    }
                }
            }
            if (!variableNames.isEmpty()) {
                insertRefactoredVariableDeclaration();
            }
            return true;
        });
        if (!variableNames.isEmpty()) {
            insertRefactoredVariableDeclaration();
        }
        var out = new PrintWriter(fileName + ".out.js");
        out.println(root.toSource());
        out.close();
    }

}
