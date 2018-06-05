package nl.utwente.simulator.input;

import java.math.BigDecimal;
import java.math.BigInteger;

import static nl.utwente.simulator.input.ExcelInputTest.TestEnum.VALUE_1;
import static nl.utwente.simulator.input.ExcelInputTest.TestEnum.VALUE_2;
import static nl.utwente.simulator.input.InputSource.TEST;

@InputValue(value = "This is the InputClass", src={TEST})
public class InputClass {

    @Input(value = "char", src={TEST})
    public static char A = 'A';
    @Input(value = "Character", src={TEST})
    public static Character B = 'B';
    @Input(value = "NullCharacter", src={TEST})
    public static Character C;
    @Input(value = "String", src={TEST})
    public static String D = "D";
    @Input(value = "NullString", src={TEST})
    public static String E;

    @Input(value = "NullFloat", src={TEST})
    public static Float f0;
    @Input(value = "Float", src={TEST})
    public static Float f1 = 10000.00100f;
    @Input(value = "float", src={TEST})
    public static float f2 = 20000.00200f;
    @Input(value = "NullDouble", src={TEST})
    public static Double d0;
    @Input(value = "Double", src={TEST})
    public static Double d1 = 10000.000010000D;
    @Input(value = "double", src={TEST})
    public static double d2 = 20000.000020000D;
    @Input(value = "BigDecimal", src={TEST})
    public static BigDecimal d3 = new BigDecimal("30000.000030000");
    @Input(value = "NullBigDecimal", src={TEST})
    public static BigDecimal d4 = null;

    @Input(value = "NullByte", src={TEST})
    public static Byte b0;
    @Input(value = "Byte", src={TEST})
    public static Byte b1 = 1;
    @Input(value = "byte", src={TEST})
    public static byte b2 = 2;
    @Input(value = "NullShort", src={TEST})
    public static Short s0;
    @Input(value = "Short", src={TEST})
    public static Short s1 = 1;
    @Input(value = "short", src={TEST})
    public static short s2 = 2;
    @Input(value = "NullInt", src={TEST})
    public static Integer i0;
    @Input(value = "Int", src={TEST})
    public static Integer i1 = 1;
    @Input(value = "int", src={TEST})
    public static int i2 = 2;
    @Input(value = "NullLong", src={TEST})
    public static Long l0;
    @Input(value = "Long", src={TEST})
    public static Long l1 = 1L;
    @Input(value = "long", src={TEST})
    public static long l2 = 2L;
    @Input(value = "NullBigInt", src={TEST})
    public static BigInteger B0;
    @Input(value = "BigInt", src={TEST})
    public static BigInteger B1 = new BigInteger("1");

    @Input(value = "booleanTrue", src={TEST})
    public static boolean bool0 = true;
    @Input(value = "booleanFalse", src={TEST})
    public static boolean bool1 = false;
    @Input(value = "BooleanTrue", src={TEST})
    public static Boolean bool2 = true;
    @Input(value = "BooleanFalse", src={TEST})
    public static Boolean bool3 = false;
    @Input(value = "NullBoolean", src={TEST})
    public static Boolean bool4;

    @Input(value = "Enum", src={TEST})
    public static ExcelInputTest.TestEnum Enum = VALUE_1;
    @Input(value = "Enum2", src={TEST})
    public static ExcelInputTest.TestEnum Enum2 = VALUE_2;
    @Input(value = "NullEnum", src={TEST})
    public static ExcelInputTest.TestEnum NullEnum = null;

    @Input(value = "LongClass", src={TEST})
    public static ExcelInputTest.LongClass t0 = new ExcelInputTest().new LongClass().new SubClass2();
    @Input(value = "NullClass", src={TEST})
    public static ExcelInputTest.TestClass t1 = null;
    @Input(value = "Class", src={TEST})
    public static ExcelInputTest.TestClass t2 = new ExcelInputTest().new TestClass().new SubClass2();
    @Input(value = "NullAnnotatedClass", src={TEST})
    public static ExcelInputTest.AnnClass t3 = null;
    @Input(value = "AnnotatedClass", src={TEST})
    public static ExcelInputTest.AnnClass t4 = new ExcelInputTest().new AnnClass().new AnnSubClass1().new AnnSubClass2();
    @Input(value = "PartiallyAnnotatedClass", src={TEST})
    public static ExcelInputTest.AnnClass t5 = new ExcelInputTest().new AnnClass().new AnnSubClass1().new AnnSubClass3();
    @Input(value = "UnenclosedAnnotatedClass", src={TEST})
    public static InputClass t6 = new InputClass();

    @Input(value = "InTEST", src={TEST})
    public static String inTest = "This value should only be visible in TEST xlsx";
    @Input(value = "NotInTEST", src={})
    public static String notInTest = "This value should not be visible in TEST xlsx";

}
