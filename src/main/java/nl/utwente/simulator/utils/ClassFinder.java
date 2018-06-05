package nl.utwente.simulator.utils;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ClassFinder {

    static Reflections reflections = new Reflections("nl.utwente.simulator");

    public static Set<Class> findClassesOfType(Class clazz){
        Set<Class> classes = reflections.getSubTypesOf(clazz);
        classes.add(clazz);
        return classes;
    }

    public static List<Field> findAnnotatedFields(Class<?> classs, Class<? extends Annotation> ann) {
        List<Field> set = new LinkedList<>();
        Class<?> c = classs;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(ann)) {
                    set.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return set;
    }

    public static Map<Class, Annotation> findAnnotatedClassesOfType(Class classz, Class<? extends Annotation> ann){
        Map<Class, Annotation> results = new HashMap<>();
        Set<Class> subTypes = reflections.getSubTypesOf(classz);
        for(Class subType : subTypes){
            if(subType.isAnnotationPresent(ann)){
                results.put(subType, subType.getAnnotation(ann));
            }
        }
        Annotation annotation = classz.getAnnotation(ann);
        if(annotation != null)
            results.put(classz, annotation);
        return results;
    }

    /**
     * @return Whether <code>getInstance</code> would be able to create a new instance of <code>clazz</code>
     *
     * Tests whether a class is instantiable via reflection without constructor parameters.
     * Includes static classes, constructor visibility,
     * enclosing classes and absence of constructor parameters
     */
    public static boolean isInstantiable(Class clazz){
        if(Modifier.isAbstract(clazz.getModifiers())|| Modifier.isInterface(clazz.getModifiers()))
            return false;
        while (!Modifier.isStatic(clazz.getModifiers()) &&  clazz.getEnclosingClass() != null) {                        //If (non-static) class has an enclosing class which
            Constructor cons;
            try {
                cons = clazz.getConstructor(clazz.getEnclosingClass());                                                 //It should have a constructor with this enclosing class as parameter
            } catch (NoSuchMethodException e) {
                return false;
            }
            if (cons == null || !Modifier.isPublic(cons.getModifiers()))                                                //This constructor should be publicly available
                return false;
            clazz = clazz.getEnclosingClass();
        }

        Constructor cons;                                                                                               //When class has no enclosing class
        try {
            cons = clazz.getConstructor();                                                                              //there should be a default constructor available
        } catch (NoSuchMethodException e) {
            return false;
        }
        if (cons == null || !Modifier.isPublic(cons.getModifiers()))                                                    //with public visibility
            return false;
        return true;
    }

    /**
     * Tries to create an instance of the class with name <code>className</code> using
     * an available public no-args constructor
     */
    public static Object getInstance(String className) throws ReflectiveOperationException {
        List<Class> enclosingClasses = new ArrayList<>();
        Class current = Class.forName(className);
        enclosingClasses.add(current);
        while(!Modifier.isStatic(current.getModifiers()) && current.getEnclosingClass() != null){
            current = current.getEnclosingClass();
            enclosingClasses.add(current);
        }
        Object instance = null;                                                                                         //Subclasses can only be instantiated
        for(int i = enclosingClasses.size()-1;i>=0;i--){                                                                //with instantiated enclosing class
            if(i == enclosingClasses.size()-1){
                instance = enclosingClasses.get(i).newInstance();
            }else{
                instance = enclosingClasses.get(i).getDeclaredConstructor(enclosingClasses.get(i+1)).newInstance(instance);
            }
        }
        return instance;
    }
}

