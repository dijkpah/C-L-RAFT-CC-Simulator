package nl.utwente.simulator.input;

import nl.utwente.simulator.input.ExcelGenerationException.EmptyOptionListException;
import nl.utwente.simulator.input.ExcelGenerationException.FieldModifierException;
import nl.utwente.simulator.input.ExcelGenerationException.NonInstantiableClassException;
import nl.utwente.simulator.input.ExcelGenerationException.UniquenessException;
import nl.utwente.simulator.utils.ClassFinder;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static nl.utwente.simulator.config.Settings.INPUT_DIRECTORY;
import static nl.utwente.simulator.config.Settings.INPUT_FILE;
import static nl.utwente.simulator.config.Settings.log;
import static nl.utwente.simulator.output.FileWriter.createDirectoryIfNonExistent;
import static nl.utwente.simulator.utils.ClassFinder.*;
import static org.apache.poi.ss.usermodel.CellType.*;
import static org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType.BETWEEN;


public class ExcelInput {

    public static final int HEADER_COLUMN = 0;
    public static final int DATA_COLUMN = 1;

    /**
     * Converts column index to the column name used in Excel (e.g. A,B,C,D,...)
     */
    public static String columnName(int n){
        if(n>701)
            throw new NotImplementedException();                                                                        //More than two digits should not be necessary
        else if(n>26)
            return "" + (char) (64+(Math.floor((n-1)/26))) + "" + (char) (64 + ((n-1) % 26)+1);
        else
            return "" + (char) (64+n);
    }

    public static Map<String, Field> getInputFields(Class clazz, InputSource src){
        LinkedHashMap result = new LinkedHashMap();                                                                     //Use LinkedHashMap to retain insertion order

        findAnnotatedFields(clazz, Input.class).stream()
                .filter(field -> Arrays.asList(field.getAnnotation(Input.class).src()).contains(src))
                .forEach(field -> result.put((field.getAnnotation(Input.class)).value(),field));
        return result;
    }

    public static CellType getCellTypeForClass(Class clazz){
        if(clazz == boolean.class || clazz == Boolean.class){
            return BOOLEAN;
        }else if(
            clazz == byte.class  ||
            clazz == short.class ||
            clazz == int.class   ||
            clazz == long.class  ||
            clazz == float.class ||
            clazz == double.class||
            Number.class.isAssignableFrom(clazz)
        ){
            return NUMERIC;
        }
        return STRING;
    }

    private static DataValidation createIntegerConstraint(DataValidationHelper helper, CellRangeAddressList cellAdress, String min, String max){
        DataValidationConstraint range = helper.createIntegerConstraint(BETWEEN, min, max);
        DataValidation validation = helper.createValidation(range, cellAdress);
        validation.createPromptBox("Number in range","("+min+", "+max+")");
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setShowPromptBox(true);
        return validation;
    }

    private static DataValidation createDecimalConstraint(DataValidationHelper helper, CellRangeAddressList cellAdress, String min, String max){
        DataValidationConstraint range = helper.createDecimalConstraint(BETWEEN, min, max);
        DataValidation validation = helper.createValidation(range, cellAdress);
        validation.createPromptBox("Decimal number in range","("+min+", "+max+")");
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setShowPromptBox(true);
        return validation;
    }

    private static DataValidation createCharConstraint(DataValidationHelper helper, CellRangeAddressList cellAdress){
        DataValidationConstraint maxLength = helper.createTextLengthConstraint(BETWEEN, "1", "1");
        DataValidation validation = helper.createValidation(maxLength, cellAdress);
        validation.createPromptBox("Text", "of length 1");
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        validation.setShowPromptBox(true);
        return validation;
    }

    private static DataValidation createListConstraint(DataValidationHelper helper, CellRangeAddressList cellAddress, String[] allowedValues){
        if(Arrays.stream(allowedValues).collect(Collectors.joining(",")).length() > 255)
            return null;                                                                                                //Cannot create Excel formula long than 255 characters
        DataValidationConstraint constraint = helper.createExplicitListConstraint(allowedValues);
        DataValidation validation = helper.createValidation(constraint, cellAddress);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        return validation;
    }

    private static DataValidation createPromptBox(DataValidationHelper validationHelper, CellRangeAddressList cellAdress, String title, String instruction){
        DataValidationConstraint dummy = new XSSFDataValidationConstraint(0, 0, null, null);                            //We do not actually want to include a constraint, just the prompt
        DataValidation validation = validationHelper.createValidation(dummy, cellAdress);
        validation.createPromptBox(title, instruction);
        validation.setShowPromptBox(true);
        return validation;
    }

    public static void generateExcel(Class inputSrc, InputSource src, boolean emptyForNull) throws ReflectiveOperationException, IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet inputSheet = wb.createSheet("input");
        XSSFDataValidationHelper helper = new XSSFDataValidationHelper(inputSheet);
        XSSFDataFormat dataFormatter = wb.createDataFormat();

        final String HIDDEN_SHEET_NAME = "hidden";                                                                      //A second, hidden worksheet in which we store the data
        XSSFSheet hiddenSheet = wb.createSheet(HIDDEN_SHEET_NAME);                                                      //for drop down lists
        int dataRows = 0;

        XSSFFont boldFont = wb.createFont();
        boldFont.setBold(true);

        XSSFCellStyle headerCell = wb.createCellStyle();                                                                //The header cells are bold and cannot be edited
        headerCell.setFont(boldFont);
        headerCell.setLocked(true);

        XSSFCellStyle dataCell = wb.createCellStyle();                                                                  //The data cells are not locked
        dataCell.setLocked(false);

        XSSFCellStyle decimalNumber = (XSSFCellStyle) dataCell.clone();                                                 //Create formatting for decimal numbers,
                                                                                                                        //if wanted a format like '0.###' can be chosen to restrict the amount of digits
        XSSFCellStyle integerNumber = (XSSFCellStyle) dataCell.clone();                                                 //Create formatting for long numbers
        integerNumber.setDataFormat(dataFormatter.getFormat("#,###,###0"));                                             //Separate thousands and show at least one digit

        Map<String, Field> inputFields = getInputFields(inputSrc, src);                                                 //Read out fields of inputSrc Class and filter ones annotated with InputSource src
        String outputFile = INPUT_DIRECTORY + File.separator + src.defaultFileName();                                   //Extract the filename from the InputSource
        try {
            int rowNumber = 0;
            for(Map.Entry<String, Field> e : inputFields.entrySet()) {                                                  //Loop over all annotated settings in config file
                Field field = e.getValue();
                String fieldDescription = e.getKey();
                Class fieldType = field.getType();
                Object value = field.get(null);

                XSSFRow row = inputSheet.createRow(rowNumber);
                XSSFCell description = row.createCell(HEADER_COLUMN, STRING);
                description.setCellValue(fieldDescription);
                description.setCellStyle(headerCell);

                XSSFCell data;

                CellType cellType = getCellTypeForClass(fieldType);                                                     //Find appropriate cell type for field
                data = row.createCell(DATA_COLUMN, cellType);
                DataValidation validation = null;
                CellRangeAddressList currentCell = new CellRangeAddressList(rowNumber, rowNumber, DATA_COLUMN, DATA_COLUMN);

                if (cellType == NUMERIC) {                                                                              //Set data cell style
                    if (fieldType == double.class ||
                        fieldType == Double.class ||
                        fieldType == float.class  ||
                        fieldType == Float.class  ||
                        fieldType == BigDecimal.class
                    ) {
                        data.setCellStyle(decimalNumber);
                    } else {
                        data.setCellStyle(integerNumber);
                    }
                } else {
                    data.setCellStyle(dataCell);
                }

                if (value == null && !emptyForNull) {                                                                   //set default value
                    switch(cellType){
                        case NUMERIC :
                            value = 0;break;
                        case BOOLEAN :
                            value = false;break;
                        default :
                            value = "";
                    }
                }

                if(fieldType == String.class) {                                                                         //Use field type to match correct excel cell type
                    if(value!=null)
                        data.setCellValue("" + value);
                    validation = createPromptBox(helper, currentCell, "Text", "of any length");
                }
                else if (fieldType == float.class  || fieldType == Float.class  ) {
                    if(value!=null)
                        data.setCellValue(((Number) value).floatValue());
                    validation = createDecimalConstraint(helper, currentCell, ""+Float.MIN_VALUE, ""+Float.MAX_VALUE);
                }
                else if (fieldType == double.class || fieldType == Double.class ) {                                     //Doubles do not work with constraints, possibly
                    if(value!=null)                                                                                     //due to the textual length of the resulting formula
                        data.setCellValue(((Number) value).doubleValue());
                    validation = createPromptBox(helper, currentCell, "Decimal number in range", "("+Double.MIN_VALUE+", "+Double.MAX_VALUE+")");
                }
                else if (fieldType == byte.class   || fieldType == Byte.class   ) {
                    if(value!=null)
                        data.setCellValue(((Number) value).byteValue());
                    validation = createIntegerConstraint(helper, currentCell, ""+Byte.MIN_VALUE, ""+Byte.MAX_VALUE);
                }
                else if (fieldType == short.class  || fieldType == Short.class  ) {
                    if(value!=null)
                        data.setCellValue(((Number) value).shortValue());
                    validation = createIntegerConstraint(helper, currentCell, ""+Short.MIN_VALUE, ""+Short.MAX_VALUE);
                }
                else if (fieldType == int.class    || fieldType == Integer.class) {
                    if(value!=null)
                        data.setCellValue(((Number) value).intValue());
                    validation = createIntegerConstraint(helper, currentCell, ""+Integer.MIN_VALUE, ""+Integer.MAX_VALUE);
                }
                else if (fieldType == long.class   || fieldType == Long.class   ) {
                    if(value!=null)
                        data.setCellValue(((Number) value).longValue());
                    validation = createIntegerConstraint(helper, currentCell, ""+Long.MIN_VALUE, ""+Long.MAX_VALUE);
                }
                else if (fieldType == BigDecimal.class){
                    if(value!=null ){
                        if (value.equals(0)) value = BigDecimal.valueOf(0D);
                        data.setCellValue(((BigDecimal) value).toPlainString());
                    }
                    validation = createPromptBox(helper, currentCell, "Decimal number", "of any value");
                }
                else if (fieldType == BigInteger.class){
                    if(value!=null) {
                        if (value.equals(0)) value = BigInteger.valueOf(0);
                        data.setCellValue(((BigInteger) value).toString());
                    }
                    validation = createPromptBox(helper, currentCell, "Number", "of any value");
                }
                else if (fieldType == char.class || fieldType == Character.class){                                      //chars can only be 1 char long
                    if(value!=null)
                        data.setCellValue("" + value);
                    validation = createCharConstraint(helper, currentCell);
                }
                else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    validation = createListConstraint(helper, currentCell, new String[]{"TRUE", "FALSE"});              //We cannot use checkboxes, so we will use a drop list
                    if(value!=null)
                        data.setCellValue((Boolean) value);
                    else
                        data.setCellValue("");                                                                          //Necessary to enforce null values
                }
                else {
                    String[] allowedValues;
                    String defaultValue;
                    if (Enum.class.isAssignableFrom(fieldType)){                                                        //Give string representation of enum values as possible values
                        allowedValues = Arrays.stream(fieldType.getEnumConstants())
                                .map(Object::toString).toArray(String[]::new);
                        defaultValue = (field.get(null) == null)
                                ? ((allowedValues.length > 0 && value!=null)? allowedValues[0] : "")
                                : value.toString();
                    } else {
                        Map<Class, Annotation> annotationValues = findAnnotatedClassesOfType(fieldType, InputValue.class);
                        allowedValues = annotationValues.values().stream()
                                .filter(ann -> Arrays.asList(((InputValue) ann).src()).contains(src))
                                .map(ann -> ((InputValue) ann).value())
                                .sorted()
                                .toArray(String[]::new);
                        InputValue inputValue = (value==null)? null : value.getClass().getAnnotation(InputValue.class);
                        if (allowedValues.length > 0) {                                                                 //If we have a list of annotated classes we use the annotation as a selectable value
                            defaultValue = (field.get(null) == null || inputValue == null || !Arrays.asList(inputValue.src()).contains(src))
                                    ? ((allowedValues.length > 0 && value!=null)? allowedValues[0] : "")
                                    : value.getClass().getAnnotation(InputValue.class).value();
                        } else {                                                                                        //Else we just use the class name as selectable value
                            Set<Class> possibleValues = ClassFinder.findClassesOfType(fieldType);
                            allowedValues = possibleValues.stream()
                                    .map(Class::getName)
                                    .sorted()
                                    .toArray(String[]::new);
                            defaultValue = (field.get(null) == null)
                                    ? ((allowedValues.length > 0 && value!=null)? allowedValues[0] : "")
                                    : value.getClass().getName();
                        }
                    }
                    data.setCellValue(defaultValue);                                                                    //Set default value for drop down list
                                                                                                                        //We create this constraint inline because
                    if (allowedValues.length > 0) {                                                                     //  we need access to both sheets and rowcount
                        XSSFRow dataRow = hiddenSheet.createRow(dataRows);                                              //Create a new row, columns would be too hard since
                        for (int i=0; i < allowedValues.length; i++)                                                    //  we do not know the number of rows we need beforehand
                            dataRow.createCell(i).setCellValue(allowedValues[i]);                                       //Fill it with all possible values
                        String cellName = "hidden"+dataRows;
                        XSSFName namedCell = wb.createName();                                                           //Create an alias for these cells
                        namedCell.setNameName(cellName);                                                                //  and make it unique by row number
                        namedCell.setRefersToFormula(String.format("%s!$A$%d:$%s$%d",                                   //Add the formula that describes this row
                                HIDDEN_SHEET_NAME, dataRows+1, columnName(allowedValues.length), dataRows+1)                  //Excel is 1-indexed
                        );
                        DataValidationConstraint constraint = helper.createFormulaListConstraint(cellName);             //And create a constraint stating
                        dataRows++;                                                                                     //  these are all possible values

                        validation = helper.createValidation(constraint, currentCell);                                  //Add constraint to current cell
                        validation.setSuppressDropDownArrow(true);                                                      //We definitely want a drop down arrow
                        validation.setShowErrorBox(true);
                    }
                }
                if(validation != null)
                    inputSheet.addValidationData(validation);
                rowNumber++;
            }

            inputSheet.autoSizeColumn(HEADER_COLUMN);                                                                   //Make sure columns have a readable size
            inputSheet.autoSizeColumn(DATA_COLUMN);
            inputSheet.protectSheet("");                                                                                //Make locked cells uneditable
            wb.setSheetHidden(wb.getNumberOfSheets()-1, true);


            createDirectoryIfNonExistent(INPUT_DIRECTORY);
            FileOutputStream fileOut = new FileOutputStream(outputFile);
            wb.write(fileOut);
            fileOut.close();
            log.infoln("[INFO]Created "+System.getProperty("user.dir")+File.separator+outputFile);
        } catch (IllegalAccessException e) {
            log.errorln("[ERROR]Unable to access all settings");
            throw e;
        } catch (FileNotFoundException e) {
            log.errorln("[ERROR]Unable to find "+outputFile+" in "+System.getProperty("user.dir"));
            throw e;
        } catch (IOException e) {
            log.errorln("[ERROR]Unable to write "+outputFile+" to "+System.getProperty("user.dir"));
            throw e;
        }
    }

    public static void readExcel(Class inputClass, InputSource src, boolean allowNullValues) throws ReflectiveOperationException, IOException {
        String inputFile = INPUT_DIRECTORY + File.separator + INPUT_FILE;

        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(inputFile));
            XSSFSheet inputSheet = wb.getSheet("input");

            Map<String, Field> inputFields = getInputFields(inputClass, src);                                           //Read out fields of inputSrc Class and filter ones annotated with InputSource src
            Map<String, XSSFCell> inputValues = new TreeMap<>();

            int rowNumber = 0;
            XSSFRow row = inputSheet.getRow(rowNumber);
            while(row != null){
                String description = row.getCell(HEADER_COLUMN).getStringCellValue();
                XSSFCell cell = row.getCell(DATA_COLUMN);
                inputValues.put(description, cell);

                rowNumber++;
                row = inputSheet.getRow(rowNumber);
            }

            for(Map.Entry<String, Field> e : inputFields.entrySet()) {

                Field field = e.getValue();
                String fieldDescription = e.getKey();
                Class fieldType = field.getType();
                XSSFCell cell = inputValues.get(fieldDescription);
                inputValues.remove(fieldDescription);
                Object value;


                if (cell == null || cell.toString() == null || (cell.toString().isEmpty() && fieldType!=String.class)) {
                    if (allowNullValues && !fieldType.isPrimitive()) {                                                  //if null allowed and possible to set to null
                        log.debugln("[DEBUG]WRITING 'null' TO "+fieldDescription);
                        field.set(null, null);
                        continue;
                    } else {                                                                                            //Otherwise default to default value
                        log.warnln(String.format(
                                "[WARNING]EMPTY VALUE FOR FIELD '%s'. REVERTING TO DEFAULT VALUE",
                                fieldDescription
                        ));
                        if (Number.class.isAssignableFrom(fieldType)) {
                            if (fieldType == BigInteger.class || fieldType == BigDecimal.class)
                                value = "0";
                            else
                                value = 0;
                        } else if (fieldType == Boolean.class) {
                            value = false;
                        } else if (fieldType == Character.class) {
                            value = "" + Character.MIN_VALUE;
                        } else if (fieldType.isEnum()) {
                            Object[] allowedValues = fieldType.getEnumConstants();
                            if(allowedValues.length == 0)
                                log.errorln("[ERROR]No default value available for field '"+fieldDescription+"'");
                            value = "" + allowedValues[0];
                        } else if (fieldType == String.class) {
                            value = "";
                        } else {
                            String[] allowedValues;
                            Map<Class, Annotation> annotationValues = findAnnotatedClassesOfType(fieldType, InputValue.class);
                            if(!annotationValues.isEmpty()) {                                                           //If we have a list of annotated classes we use the annotation as a selectable value
                                allowedValues = annotationValues.values().stream()                                      //NOTE: these classes should all be instantiable
                                        .map(ann -> ((InputValue) ann).value()).sorted().toArray(String[]::new);        //      as confirmed by the validator
                            } else {                                                                                    //Else we just use the class name as selectable value
                                Set<Class> possibleValues = ClassFinder.findClassesOfType(fieldType);
                                allowedValues = possibleValues.stream()
                                        .filter(clazz -> isInstantiable(clazz))                                         //Only show classes that can be instantiated
                                        .map(Class::getName).sorted().toArray(String[]::new);
                            }
                            if(allowedValues.length == 0)
                                log.errorln("[ERROR]No default value available for field '"+fieldDescription+"'");      //TODO: write test for this specific case
                            value = allowedValues[0];
                        }

                    }
                }else{
                    switch(cell.getCellTypeEnum()){
                        case NUMERIC :
                            value = cell.getNumericCellValue();break;
                        case BOOLEAN :
                            value = cell.getBooleanCellValue();break;
                        default :
                            value = cell.getStringCellValue();
                    }
                }

                log.debugln(String.format("[DEBUG]WRITING '%s' TO '%s'", ""+value, fieldDescription));

                if (fieldType == String.class) {                                                                        //Use field type to cast excel cell type
                    field.set(null, value);
                } else if (fieldType == float.class  || fieldType == Float.class    ) {
                    field.set(null, ((Number) value).floatValue());
                } else if (fieldType == double.class || fieldType == Double.class   ) {
                    field.set(null, ((Number) value).doubleValue());
                } else if (fieldType == byte.class   || fieldType == Byte.class     ) {
                    field.set(null, ((Number) value).byteValue());
                } else if (fieldType == short.class  || fieldType == Short.class    ) {
                    field.set(null, ((Number) value).shortValue());
                } else if (fieldType == int.class    || fieldType == Integer.class  ) {
                    field.set(null, ((Number) value).intValue());
                } else if (fieldType == long.class   || fieldType == Long.class     ) {
                    field.set(null, ((Number) value).longValue());
                } else if (fieldType == char.class   || fieldType == Character.class) {                                 //chars can only be 1 char long
                    field.set(null, ((String)value).charAt(0));
                } else if (fieldType == boolean.class|| fieldType == Boolean.class  ) {
                    field.set(null, (Boolean) value);
                } else if (fieldType == BigDecimal.class) {
                    field.set(null, new BigDecimal((String)value));
                } else if (fieldType == BigInteger.class) {
                    field.set(null, new BigInteger((String)value));
                } else if (Enum.class.isAssignableFrom(fieldType)) {
                    field.set(null, Enum.valueOf(fieldType, (String)value));
                } else {
                    Map<Class, Annotation> annotationValues = findAnnotatedClassesOfType(fieldType, InputValue.class);
                    if (!annotationValues.isEmpty()) {                                                                  //find by annotation name
                        for (Map.Entry<Class, Annotation> entry : annotationValues.entrySet()) {
                            if (((InputValue) entry.getValue()).value().equals(value)) {
                                field.set(null, getInstance(entry.getKey().getName()));
                                break;
                            }
                        }
                    } else {
                        field.set(null, getInstance((String)value));
                    }
                }
            }

            for(Map.Entry<String, XSSFCell> inputValue : inputValues.entrySet()){                                       //Al fields in the Excel file that do not have fields in the Java class
                log.warnln(String.format("[WARNING]Unknown field '%s' with value '%s'. This field will not be included",
                        inputValue.getKey(),
                        inputValue.getValue().toString()
                ));
            }

        } catch (IllegalAccessException e) {
            log.errorln("[ERROR]Unable to access all settings");
            throw e;
        } catch (FileNotFoundException e) {
            log.errorln("[ERROR]Unable to find "+inputFile+" in "+System.getProperty("user.dir"));
            throw e;
        } catch (IOException e) {
            log.errorln("[ERROR]Unable to read "+inputFile+" in "+System.getProperty("user.dir"));
            throw e;
        } catch (ClassNotFoundException e) {
            log.errorln("[ERROR]Referred to unknown entity");
            throw e;
        } catch (InstantiationException e) {
            log.errorln("[ERROR]Unable to create instance of selected value");
            throw e;
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw e;
        }
    }

    public static void deleteExcel(InputSource src){
        String inputFile = INPUT_DIRECTORY + File.separator + src.defaultFileName();
        File file = new File(inputFile);
        if(!file.delete()){
            log.errorln("Unable to delete file: "+inputFile);
        }
    }

    public static void validate(Class inputClass) throws ExcelGenerationException, IllegalAccessException {
        List<Field> annFields = findAnnotatedFields(inputClass, Input.class);
        if(annFields.isEmpty())
            log.warnln("[WARNING]Class "+inputClass.getSimpleName()+" has an no annotated fields. Add @Input to fields to include them in the generated Excel file(s)");

        Map<String, List<Field>> fields = new HashMap<>();
        for(Field field : annFields){
            if(!Modifier.isStatic(field.getModifiers())){
                throw new FieldModifierException(inputClass, "Field "+field.getName()+" does not have static modifier");
            }else if(Modifier.isFinal(field.getModifiers())){
                throw new FieldModifierException(inputClass, "Field "+field.getName()+" is final, so cannot be changed");
            }else if(!Modifier.isPublic(field.getModifiers())){
                throw new FieldModifierException(inputClass, "Field "+field.getName()+" is not public, so cannot be accessed");
            }else if(Modifier.isAbstract(field.getModifiers())){
                throw new FieldModifierException(inputClass, "Field "+field.getName()+" is abstract, so cannot be accessed");
            }
            Input ann = field.getAnnotation(Input.class);                                                               //Check uniqueness of annotation
            if(ann.value().isEmpty())
                log.warnln("[WARNING]Field "+field.getName()+" of class "+inputClass.getSimpleName()+" has an empty annotation, please add a value");
            if(ann.src().length==0)
                log.warnln("[WARNING]Field "+field.getName()+" of class "+inputClass.getSimpleName()+" has no input source. Add source(s) to include it in generated Excel file(s)");
            List<Field> collisions = fields.get(ann.value());
            if(collisions != null) {
                for (Field collision : collisions) {
                    for (InputSource src : ann.src()) {
                        if (Arrays.asList(collision.getAnnotation(Input.class).src()).contains(src))
                            throw new UniquenessException(inputClass, field, collision);
                    }
                }
                collisions.add(field);
            } else if(ann.src().length!=0){                                                                             //If there are no sources we cannot have a collision
                List<Field> fieldList = new ArrayList<>();
                fieldList.add(field);
                fields.put(ann.value(), fieldList);
            }


            Class fieldType = field.getType();
            if(!fieldType.isPrimitive()) {
                if(Enum.class.isAssignableFrom(fieldType)){                                                             //Give string representation of enum values as possible values
                    if(fieldType.getEnumConstants().length == 0)
                        throw new EmptyOptionListException(inputClass, field);
                } else if(fieldType!=String.class && !Number.class.isAssignableFrom(fieldType) && fieldType!=Character.class && fieldType!=Boolean.class){

                    Map<Class, Annotation> annotatedClasses = findAnnotatedClassesOfType(fieldType, InputValue.class);  //Find out whether possible values exist that are annotated
                    Set<Class> possibleValues;
                    if(!annotatedClasses.isEmpty()) {                                                                   //If we have a list of annotated classes we use the annotation as a selectable value
                        possibleValues = annotatedClasses.keySet();
                        for(InputSource src : ann.src()){
                            Map<String, Class> annValues = new TreeMap<>();
                            Set<Map.Entry<Class, Annotation>> annClassesForSource = annotatedClasses.entrySet().stream()
                                    .filter(e->Arrays.asList(((InputValue)e.getValue()).src()).contains(src))           //Filter on annotations containing src as source
                                    .collect(Collectors.toSet());
                            for(Map.Entry<Class, Annotation> e : annClassesForSource){                                  //Check uniqueness of annotation
                                String descr = ((InputValue)e.getValue()).value();
                                Class clazz = e.getKey();
                                if(descr.isEmpty())
                                    log.warnln("[WARNING]Class "+clazz.getSimpleName()+" has an empty annotation, please add a value");
                                if(annValues.get(descr) != null)
                                    throw new UniquenessException(inputClass, clazz, annValues.get(descr));
                                annValues.put(descr, clazz);
                            }
                            if(annValues.isEmpty())
                                throw new EmptyOptionListException(inputClass, field, src);
                        }

                        for(Class value : possibleValues ) {
                            if(!isInstantiable(value) && ((InputValue)annotatedClasses.get(value)).src().length > 0)
                                throw new NonInstantiableClassException(inputClass, value);                             //If annotated it should be instantiable
                        }
                    } else {
                        possibleValues = ClassFinder.findClassesOfType(fieldType);
                        if(possibleValues.size() == 0 )
                            throw new EmptyOptionListException(inputClass, field);
                        boolean hasInstantiableClass = possibleValues.stream()
                                .filter(clazz -> isInstantiable(clazz)).count()>0;
                        if(!hasInstantiableClass)
                            throw new NonInstantiableClassException(inputClass, fieldType);                             //If annotated it should be instantiable
                        log.warnln(String.format("[WARNING]Please annotate possible values for %s %s with @InputValue", //We prefer user-readable input options and not class names
                                fieldType.getSimpleName(),field.getName()));
                    }
                }
            }
        }
    }

}
