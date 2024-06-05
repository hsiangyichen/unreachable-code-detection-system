package ProgramAnalysis;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtLiteral;
import spoon.support.reflect.code.CtBinaryOperatorImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import static org.junit.jupiter.api.Assertions.*;

class FileTests {

    private BackendServerLocal analyzer;

    @BeforeEach
    void setUp(){
        analyzer = new BackendServerLocal();
    }

    @Test
    void testAnalyzeWithEmptyFile() throws IOException {
        String emptyFile = Files.readString(Path.of("EmptyFile.txt"));
        analyzer.analyze(emptyFile);

        assertTrue(analyzer.globalUnreachableMap.isEmpty());
        assertEquals(Collections.emptyMap(), analyzer.globalUnreachableMap);
    }

    @Test
    void testAnalyzeWithAllReachableCode() throws IOException {
        String allReachableCodeFile = Files.readString(Path.of("AllReachableCodeFile.txt"));
        analyzer.analyze(allReachableCodeFile);

        assertTrue(analyzer.globalUnreachableMap.containsKey("public void ReachableCode():"), "The unreachableMap should have a function name without any unreachable code");
        assertTrue(analyzer.globalUnreachableMap.get("public void ReachableCode():").isEmpty());
    }

    @Test
    void testAnalyzeWithUnreachableIfElseStatement() throws IOException {
        String javaFile = Files.readString(Path.of("JavaFile.txt"));
        analyzer.analyze(javaFile);
        Map<String, List<String>> globalUnreachableMap = analyzer.globalUnreachableMap;
        List<String> expectedResult = Arrays.asList(
                "line number 37: {\n" +
                        "    return bob;\n" +
                        "    int i = 2;\n" +
                "}",
                "line number 43: int i = 1",
                "line number 44: int j = 2"
        );

        List<String> actualUnreachableStatements = globalUnreachableMap.get("public String tst2(int joe):");
        assertEquals(actualUnreachableStatements, expectedResult);
//        System.out.println(actualUnreachableStatements);
    }

    @Test
    void testAnalyzeWithUnreachableForStatement() throws IOException {
        String javaFile = Files.readString(Path.of("JavaFile.txt"));
        analyzer.analyze(javaFile);
        Map<String, List<String>> globalUnreachableMap = analyzer.globalUnreachableMap;
        List<String> expectedFailedResult = Arrays.asList(
                "line number 7: for (int i = 11;i < 10;i += 2) {",
                "line number 8: System.out.println(\"foo\")",
                "line number 9: a += 1",
                "line number 10: }"
        );
        List<String> expectedWorkedResult = List.of();

        List<String> actualFailedStatements = globalUnreachableMap.get("public void michelleLoopFail():");
        List<String> actualWorkedStatements = globalUnreachableMap.get("public void michelleLoopWork():");

        assertEquals(actualFailedStatements, expectedFailedResult);
        assertEquals(actualWorkedStatements, expectedWorkedResult);
    }

    @Test
    void testAnalyzeWithUnreachableWhileStatement() throws IOException {
        String javaFile = Files.readString(Path.of("JavaFile.txt"));
        analyzer.analyze(javaFile);
        Map<String, List<String>> globalUnreachableMap = analyzer.globalUnreachableMap;
        List<String> expectedResult = Arrays.asList(
                "line number 23: bob > 2{",
                "line number 24: return bob",
                "line number 25: }",
                "line number 28: int i = 2"
        );

        List<String> actualUnreachableStatements = globalUnreachableMap.get("public String m(int joe):");
        assertEquals(actualUnreachableStatements, expectedResult);
//        System.out.println(actualUnreachableStatements);
    }

    @Test
    void testAnalyzeWithNestedUnreachableCode() throws IOException {
        String javaFile = Files.readString(Path.of("JavaFile.txt"));
        analyzer.analyze(javaFile);
        Map<String, List<String>> globalUnreachableMap = analyzer.globalUnreachableMap;
        List<String> expectedResultOne = List.of(
                "line number 53: {\n" +
                        "    System.out.println(\"Greater than 2\");\n" +
                        "}"
        );
        List<String> expectedResultTwo = List.of(
                "line number 70: {\n" +
                        "    System.out.println(\"Michelle\");\n" +
                        "}"
        );
        List<String> expectedResultThree = Arrays.asList(
                "line number 88: {\n" +
                "    System.out.println(\"Hoho\");\n" +
                "}",
                "line number 81: while (3 > 5) {\n" +
                "    System.out.println(\"sucks to suck\");\n" +
                "} ",
                "line number 94: {\n" +
                "    b += 2;\n" +
                "}"
        );

        List<String> actualStatementsOne = globalUnreachableMap.get("public void alwaysGreaterThan2Test():");
        assertEquals(actualStatementsOne, expectedResultOne);

        List<String> actualStatementsTwo = globalUnreachableMap.get("public void michelleLoopWork4():");
        assertEquals(actualStatementsTwo, expectedResultTwo);

        List<String> actualStatementsThree = globalUnreachableMap.get("public void michelleLoopWor3k():");
        assertEquals(actualStatementsThree, expectedResultThree);
    }

    @Test
    void testAnalyzeThrowsIOExceptionOnFileNotFound() throws IOException {
        String nonExistentFilePath = "non_existent_file.txt";
        assertThrows(spoon.SpoonException.class, () -> analyzer.analyze(nonExistentFilePath),
                "Expected analyze to throw SpoonException for a non-existent file path");
    }


    @Test
    public void testBinaryOperatorHelper() {
        CtBinaryOperator binaryOperator = new CtBinaryOperatorImpl();
        binaryOperator.setKind(BinaryOperatorKind.PLUS);

        CtLiteral<Integer> leftOperand = new CtLiteralImpl<>();
        leftOperand.setValue(5);

        CtLiteral<Integer> rightOperand = new CtLiteralImpl<>();
        rightOperand.setValue(3);

        binaryOperator.setLeftHandOperand(leftOperand);
        binaryOperator.setRightHandOperand(rightOperand);

        Map<String, VariableRange> varMapLocal = new HashMap<>();

        VariableRange result = analyzer.binaryOperatorHelper(binaryOperator, varMapLocal);

        assertNotNull(result);
        assertEquals(8, result.lowerBound);
        assertEquals(8, result.upperBound);
        assertFalse(result.infLowerBound);
        assertFalse(result.infUpperBound);
    }
}
