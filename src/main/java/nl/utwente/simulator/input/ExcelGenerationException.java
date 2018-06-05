package nl.utwente.simulator.input;

import java.lang.reflect.Field;

public abstract class ExcelGenerationException extends Exception{


    public ExcelGenerationException(Class inputClass, String s) {
        super("Exception when generating Excel file for class "+inputClass.getSimpleName()+": "+s);
    }

    public static class NonInstantiableClassException extends ExcelGenerationException{

        public NonInstantiableClassException(Class inputClass, Class errorClass) {
            super(inputClass, "Class "+errorClass.getName()+" is not instantiable, please create a public no-args constructor");
        }
    }

    public static class EmptyOptionListException extends ExcelGenerationException{

        public EmptyOptionListException(Class inputClass, Field f, InputSource src) {
            super(inputClass, "Field "+f.getType().getSimpleName()+" "+f.getName()+" does not have any instantiable values for source "+src.name());
        }

        public EmptyOptionListException(Class inputClass, Field f) {
            super(inputClass, "Field "+f.getType().getSimpleName()+" "+f.getName()+" does not have any instantiable values");
        }
    }

    public static class UniquenessException extends ExcelGenerationException{

        public UniquenessException(Class inputClass, Field f1, Field f2) {
            super(inputClass, "Field "+f1.getType().getSimpleName()+" "+f1.getName()+" does not have unique annotation value;Field "+f2.getType().getSimpleName()+" "+f2.getName()+" has same annotation");
        }

        public UniquenessException(Class inputClass, Class c1, Class c2) {
            super(inputClass, "Class "+c1.getName()+" does not have unique annotation value;Class "+c2.getName()+" has same annotation");
        }
    }

    public static class FieldModifierException extends ExcelGenerationException{

        public FieldModifierException(Class inputClass, String s) {
            super(inputClass, s);
        }
    }
}
