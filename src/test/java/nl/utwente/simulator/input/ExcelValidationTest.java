package nl.utwente.simulator.input;

import nl.utwente.simulator.ValidationTest;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

import static nl.utwente.simulator.input.ExcelInput.*;
import static nl.utwente.simulator.input.InputSource.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Category(ValidationTest.class)
public class ExcelValidationTest extends ValidationTest{

    @AfterClass
    public static void cleanup(){
        ExcelInput.deleteExcel(TEST);
    }

    /**
     * Test all possible modifiers. Only public static fields should be allowed, the rest should throw exceptions
     * interface is not tested because field are final, which is already tested
     * abstract is not tested for fields because that combination is not allowed
     */
    @Test
    public void validateFieldModifiers() throws IllegalAccessException, ExcelGenerationException {
        validate(PublicStatic.class);
        assertThrows(ExcelGenerationException.FieldModifierException.class, () -> validate(NonStatic.class));
        assertThrows(ExcelGenerationException.FieldModifierException.class, () -> validate(PackageProtected.class));
        assertThrows(ExcelGenerationException.FieldModifierException.class, () -> validate(Protected.class));
        assertThrows(ExcelGenerationException.FieldModifierException.class, () -> validate(Private.class));
        assertThrows(ExcelGenerationException.FieldModifierException.class, () -> validate(Final.class));
    }

    public static class PublicStatic {
        @Input(value = "PublicStaticField", src = {TEST})
        public static String publicStaticField;
    }
    public static class Final {
        @Input(value = "FinalField", src = {TEST})
        public static final String finalField = null;
    }
    public static class NonStatic {
        @Input(value = "NonStaticField", src = {TEST})
        public String nonStaticField;
    }
    public static class PackageProtected {
        @Input(value = "PackageProtectedField", src = {TEST})
        static String packageProtectedField;
    }
    public static class Protected {
        @Input(value = "ProtectedField", src = {TEST})
        protected static String protectedField;
    }
    public static class Private {
        @Input(value = "PrivateField", src = {TEST})
        private static String privateField;
    }

    /**
     * Test whether empty option lists are detected. Empty enums and empty class lists should not be allowed
     */
    @Test
    public void validateEmptyOptions() throws IllegalAccessException, ExcelGenerationException {
        validate(NonEmptyEnumClass.class);
        validate(InstantiableAnnotatedClass.class);                                                                     //This one should give a warning, but no exception
        assertThrows(ExcelGenerationException.EmptyOptionListException.class, () -> validate(EmptyEnumClass.class));
        assertThrows(ExcelGenerationException.EmptyOptionListException.class, () -> validate(EmptyInputClass.class));
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(UninstantiableClass.class));
    }

    public static class EmptyEnumClass {
        public enum EmptyEnum {}
        @Input(value="emptyEnum", src={TEST})
        public static EmptyEnum emptyEnum;
    }
    @InputValue(value="EmptyInputClass", src={})
    public static class EmptyInputClass {
        @Input(value="EmptyInputClass", src={TEST})
        public static EmptyInputClass eptyInputClass;
    }
    public static class NonEmptyEnumClass {
        public enum NonEmptyEnum{OPTION}
        @Input(value="nonEmptyEnum", src={TEST})
        public static NonEmptyEnum nonEmptyEnum;
    }
    public static class UninstantiableClass{
        private UninstantiableClass(){}
        @Input(value="UninstantiableField", src={TEST})
        public static UninstantiableClass uninstantiableField;
    }
    @InputValue(value = "This is an instantiable annotated class", src={TEST})
    public static class InstantiableAnnotatedClass{
        @Input(value="InstantiableAnnotatedClass", src={TEST})
        public static InstantiableAnnotatedClass instantiableAnnotatedClass;
    }

    /**
     * Test whether annotation uniqueness problems are detected
     */
    @Test
    public void validateUniqueAnnotations() throws IllegalAccessException, ExcelGenerationException {
        validate(UniqueFieldAnnotation.class);
        validate(UniqueClassAnnotation.class);
        assertThrows(ExcelGenerationException.UniquenessException.class, () -> validate(NonUniqueFieldAnnotation.class));
        assertThrows(ExcelGenerationException.UniquenessException.class, () -> validate(NonUniqueFieldAnnotation2.class));
        assertThrows(ExcelGenerationException.UniquenessException.class, () -> validate(NonUniqueClassAnnotation.class));
    }

    public static class NonUniqueFieldAnnotation{
        @Input(value="collission",src={TEST})
        public static String s1;
        @Input(value="collission",src={TEST})
        public static String s2;
    }
    public static class NonUniqueFieldAnnotation2{
        @Input(value="collission",src={TEST, UNSTRUCTURED})
        public static String s1;
        @Input(value="collission",src={TEST})
        public static String s2;
    }
    public static class UniqueFieldAnnotation {
        //Both have no src, so no collission should be possible since fields are never included
        @Input(value="collission",src={})
        public static String s1;
        @Input(value="collission",src={})
        public static String s2;
        //Different sources, so collission will not occur
        @Input(value="collission",src={UNSTRUCTURED, STRUCTURED})
        public static String s3;
        @Input(value="collission",src={TEST})
        public static String s4;
    }
    public static class NonUniqueClassAnnotation{
        @Input(value="nonUniqueClass",src={TEST})
        public static Super clazz;

        public static class Super{}
        @InputValue(value = "collission", src={TEST})
        public static class Sub1 extends Super{}
        @InputValue(value = "collission", src={TEST})
        public static class Sub2 extends Super{}
    }
    public static class UniqueClassAnnotation {
        @Input(value="UniqueClass",src={TEST})
        public static Super clazz;

        public static class Super{}
        //Both have no src, so no collission should be possible since fields are never included
        @InputValue(value = "collission", src={})
        public static class Sub1 extends Super{}
        @InputValue(value = "collission", src={})
        public static class Sub2 extends Super{}

        //Different sources, so collission will not occur
        //Both have no src, so no collission should be possible since fields are never included
        @InputValue(value = "collission", src={UNSTRUCTURED, STRUCTURED})
        public static class Sub3 extends Super{}
        @InputValue(value = "collission", src={TEST})
        public static class Sub4 extends Super{}
    }
    /**
     * These tests show examples that are not incorrect per se, but are regarded as unwanted
     */
    @Test
    public void validateEmptyAnnotations() throws IllegalAccessException, ExcelGenerationException {
        validate(EmptyInputValueAnnotation.class);
        validate(EmptyInputAnnotation.class);
        validate(NoInputAnnotations.class);
        validate(NoInputValueAnnotations.class);
        validate(NoInputSource.class);
    }

    @InputValue
    public static class EmptyInputValueAnnotation{
        @Input(value="EmptyInputValueAnnotation", src={})
        public static EmptyInputValueAnnotation emptyInputValueAnnotationField;
    }
    public static class EmptyInputAnnotation{
        @Input
        public static EmptyInputAnnotation emptyInputAnnotationField;
    }
    public static class NoInputAnnotations {}
    public static class NoInputValueAnnotations{
        @Input(value="NoInputValue", src={TEST})
        public static NoInputValueAnnotations noInputValue;
    }
    public static class NoInputSource{
        @Input(value="NoSrc", src={})
        public static String noSrc;
    }

    /**
     * These test validate whether uninstantiable (by reflection) classes can be detected
     */
    @Test
    public void validateInstantiability() throws ReflectiveOperationException, ExcelGenerationException, IOException {
        validate(DefaultConstructor.class);                                                                             //Static class should always be instantiable
        generateExcel(DefaultConstructor.class, TEST, false);
        readExcel(DefaultConstructor.class, TEST, false);

        validate(DefaultConstructorNonStatic.class);                                                                    //Non-static class can only be instantiable when parent is instantiable as well
        generateExcel(DefaultConstructorNonStatic.class, TEST, false);
        readExcel(DefaultConstructorNonStatic.class, TEST, false);

        validate(UninstantiableSuperWithInstantiableStaticSub.class);                                                   //Static subclass should not need super to create instance
        generateExcel(UninstantiableSuperWithInstantiableStaticSub.class, TEST, false);
        readExcel(UninstantiableSuperWithInstantiableStaticSub.class, TEST, false);
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(PublicSuperWithPrivateSub.class));
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(PrivateSuperWithPublicSub.class));
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(UninstantiableSuperWithInstantiableSub.class));
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(PrivateConstructor.class));
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(ProtectedConstructor.class));
        assertThrows(ExcelGenerationException.NonInstantiableClassException.class, () -> validate(PackageProtectedConstructor.class));
    }

    public static class PublicSuperWithPrivateSub {
        @InputValue(value = "Private Subclass", src={TEST})
        private class PrivateSub extends PublicSuperWithPrivateSub {}
        @Input(value = "PrivateSub", src={TEST})
        public static PrivateSub privateSub;
    }
    private static class PrivateSuperWithPublicSub {
        @InputValue(value = "Public Subclass", src={TEST})
        public class PublicSub extends PrivateSuperWithPublicSub {}
        @Input(value = "PublicSub", src={TEST})
        public static PublicSub publicSub;
    }
    public static class UninstantiableSuperWithInstantiableSub {
        private UninstantiableSuperWithInstantiableSub(){}
        @InputValue(value = "Instantiable Subclass", src={TEST})
        public class InstantiableSub extends UninstantiableSuperWithInstantiableSub {
            public InstantiableSub(){}
        }
        @Input(value = "InstantiableSub", src={TEST})
        public static InstantiableSub instantiableSub;
    }
    public static class UninstantiableSuperWithInstantiableStaticSub {
        private UninstantiableSuperWithInstantiableStaticSub(){}
        @InputValue(value = "Instantiable static Subclass", src={TEST})
        public static class InstantiableSub extends UninstantiableSuperWithInstantiableStaticSub {
            public InstantiableSub(){}
        }
        @Input(value = "InstantiableSub", src={TEST})
        public static InstantiableSub instantiableSub;
    }
    public static class DefaultConstructorNonStatic{
        @Input(value="DefaultConstructorNonStaticClass", src={TEST})
        public static DefaultConstructorNonStaticClass defaultConstructorNonStaticClass;
    }
    @InputValue(value = "Non-static class with default constructor", src={TEST})
    public class DefaultConstructorNonStaticClass{}
    @InputValue(value = "Class with default constructor", src={TEST})
    public static class DefaultConstructor{
        @Input(value="DefaultConstructor", src={TEST})
        public static DefaultConstructor defaultConstructor;
    }
    @InputValue(value = "Class with private constructor", src={TEST})
    public static class PrivateConstructor{
        private PrivateConstructor(){}
        @Input(value="privateConstructor", src={TEST})
        public static PrivateConstructor privateConstructor;
    }
    @InputValue(value = "Class with protected constructor", src={TEST})
    public static class ProtectedConstructor{
        protected ProtectedConstructor(){}
        @Input(value="ProtectedConstructor", src={TEST})
        public static ProtectedConstructor protectedConstructor;
    }
    @InputValue(value = "Class with package protected constructor", src={TEST})
    public static class PackageProtectedConstructor{
        PackageProtectedConstructor(){}
        @Input(value="PackageProtectedConstructor", src={TEST})
        public static PackageProtectedConstructor packageProtectedConstructor;
    }
}
