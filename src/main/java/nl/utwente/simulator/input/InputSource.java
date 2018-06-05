package nl.utwente.simulator.input;

public enum InputSource {
    STRUCTURED("structured.xlsx"),
    UNSTRUCTURED("unstructured.xlsx"),
    TEST("test.xlsx");

    private final String s;
    InputSource(String s){this.s = s;}
    public String defaultFileName(){return s;}
    @Override public String toString(){return this.s;}

}
