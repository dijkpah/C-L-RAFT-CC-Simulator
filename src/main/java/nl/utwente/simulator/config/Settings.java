package nl.utwente.simulator.config;

import nl.utwente.simulator.entities.abstractSpecies.CompositionalSpeciesFactory;
import nl.utwente.simulator.input.Input;
import nl.utwente.simulator.input.InputSource;
import nl.utwente.simulator.output.CustomLogger;
import nl.utwente.simulator.simulator.AbstractSpeciesFactory;
import nl.utwente.simulator.utils.codegeneration.ExpressionGenerator;
import org.apache.log4j.Level;

import java.text.ParseException;
import java.util.TreeSet;

import static nl.utwente.simulator.input.InputSource.STRUCTURED;
import static nl.utwente.simulator.input.InputSource.UNSTRUCTURED;

public class Settings {

    //DEFINITIONS
    public static final int CROSSLINKER_VINYL_GROUPS = 2;                                                               //TODO: use this in SpeciesFactories as well, disable structured sim when value is not 2
    public static final double NUMBER_AVOGADRO = 602214085700000000000000d;                                             //Avogadro's number (CODATA 2014 taken from http://arxiv.org/pdf/1507.07956.pdf)

    //DEFAULT EXECUTION MODE
    public static InputSource INPUT_SOURCE = UNSTRUCTURED;

    //DIRECTORIES
    public static String INPUT_DIRECTORY  = "input";                                                                    //NOTE: no trailing file separator
    public static String OUTPUT_DIRECTORY = "output";                                                                   //NOTE: no trailing file separator
    public static String INPUT_FILE = INPUT_SOURCE.defaultFileName();

    //LOGGING CONFIGURATION
    public static final boolean ASSERTIONS_ENABLED = Settings.class.desiredAssertionStatus();                           //This call is inefficient, so we cache it
    public static final boolean LOG_COLORED = false;
    public static final String  LOG_FORMAT  = "%m";
    public static Level         LOG_LEVEL   = Level.INFO;

    public static final String CSV_DELIMITER = ";";
    public static final String LINE_END = "\n";

    public static final CustomLogger log = new CustomLogger(Settings.class);

    //RUN CONFIGURATION
    public static final boolean STOP_AT_GEL_POINT = true;
    public static final int     SHRINK_THRESHOLD = 10;                                                                  //Shrink Fenwick trees when they contain tree.length/2 - threshold elements;
                                                                                                                        // this prevents doubling immediately afterwards
    //OUTPUT CONFIGURATION
    @Input("Percent conversion per data point")
    public static double  PERCENTAGE_PER_SLICE = 0.1D;

    //OUTPUT FOR CSV LOG
    public static boolean LOG_TO_FILE;                                                                                  //Create CSV log
    public enum CSVColumn {                                                                                             //Use enum for prettier code rather than hardcoded print formatting
        CONVERSION("% conversion"),
        NUMBER_I("#i"),
        NUMBER_M("#m"),
        NUMBER_C("#c"),
        NUMBER_POLYMERS("#p"),
        NUMBER_SPECIES("#p_groups"),
        BIGGEST_PARTICLE_WEIGHT("biggest particle (Da)"),
        STEPS_PER_SECOND("steps/s"),
        STEPS_PER_SECOND_AVG("avg steps/s"),
        MEMORY_USAGE("memory (bytes)"),
        NOTES("notes");

        private final String s;
        CSVColumn(String s){this.s = s;}
        @Override public String toString(){return this.s;}
    }

    //OUTPUT FOR MOLECULAR WEIGHT DISTRIBUTION
    @Input(value = "Weight of initiator radical(Da)", src = {UNSTRUCTURED})
    public static double WEIGHT_HALF_INITIATOR = 68.097331;
    @Input(value = "Weight of monomer(Da)", src = {UNSTRUCTURED})
    public static double WEIGHT_MONOMER = 200.235;
    @Input(value = "Weight of crosslinker(Da)", src = {UNSTRUCTURED})
    public static double WEIGHT_CROSSLINKER = 198.22;

    //OUTPUT FOR MOLECULAR STRUCTURES
    @Input(value = "Maximum molecule sizes (comma separated)", src = {STRUCTURED})
    public static String MAX_3D_MODEL_SIZES_TEXT = "255, 999";
    public static TreeSet<Integer> MAX_3D_MODEL_SIZES = new TreeSet<>();

    @Input(value = "Atom representing initiator molecule", src = {STRUCTURED})
    public static String I_REPRESENTATIVE_ATOM = "Ar";
    @Input(value = "Atom representing monomer", src = {STRUCTURED})
    public static String M_REPRESENTATIVE_ATOM = "He";
    @Input(value = "Atom representing (half) crosslinker", src = {STRUCTURED})
    public static String C_REPRESENTATIVE_ATOM = "Ne";
    @Input(value = "Atom representing pendent group", src = {STRUCTURED})
    public static String VINYL_GROUP_REPRESENTATIVE_ATOM = "Kr";

    //INPUT CONFIGURATION
    @Input("Concentration of (instantly splitting) initiator(M)")
    public static double CONCENTRATION_INITIATORS = 0.008;                                                              //concentration of initiators (I-I)
    @Input("Concentration of non-crosslinker monomer(M)")
    public static double CONCENTRATION_MONOMER = 0.3;                                                                   //concentration of monomers (M=)
    @Input("Concentration of crosslinker(M)")
    public static double CONCENTRATION_CROSSLINKER = 0.1;                                                               //concentration of crosslinkers (=C-C=)
    @Input("Concentration of RAFT agent(M)")
    public static double CONCENTRATION_RAFT = 0.0075;                                                                   //concentration of RAFT agent
    @Input("Number of initial molecules in simulation")
    public static long NUMBER_OF_MOLECULES = 1*1000*1000*1000;

    @Input("Steric hindrance probability for reaction between vinyl group in polymer and chain end radical(%)")
    public static double STERIC_HINDRANCE_VINYL_PERCENTAGE = 47;
    @Input("Steric hindrance probability for reaction between monomer and mid-chain radical(%)")
    public static double STERIC_HINDRANCE_RADICAL_PERCENTAGE = 99.845;
    @Input("Steric hindrance probability for reaction between vinyl group in polymer and mid-chain radical(%)")
    public static double STERIC_HINDRANCE_COMBINED_PERCENTAGE = 100.0;

    @Input("Group molecules by")
    public static AbstractSpeciesFactory abstractSpeciesFactory = new CompositionalSpeciesFactory();

    @Input("Length of monomer segment in polymer(nm)")
    public static double MONOMER_LENGTH = 0.2487;                                                                       //Default is one C-C-C bond, measured by Chem3D
    @Input("Formula for radius of interaction")

    //Tobita: l√(n/6) (√(1+(R-1)/6)+(4(R-1)/(3π)))^-0.25
    //Pomposo: 0.144w^0.561
    public static String INTERACTION_RADIUS_FORMULA = "0.144w^0.561";
    @Input("Interaction volume calibration factor")
    public static double COMPENSATION_FACTOR = 2.2;

    @Input(value="Custom output functions (comma separated)", src = {UNSTRUCTURED})
    public static String OUTPUT_FUNCTIONS = "PDI, MW, MN, MWD, ZAV(SIZE)";


    //Initialize these using the user-provided data in init();
    public static long NUMBER_HALF_INITIATORS;                                                                          //Number of initiator radical (I•) obtained from RAFT agent and initiator molecules
    public static long NUMBER_MONOMERS;                                                                                 //Number of monomers (M=)
    public static long NUMBER_CROSSLINKERS;                                                                             //Number of crosslinkers (=C-C=)
    public static double VESSEL_VOLUME;                                                                                 //Volume of reaction vessel (in dm^3)
    public static double MIN_MOL_VOLUME;                                                                                //Minimum volume of a molecule
    public static double STERIC_HINDRANCE_VINYL_FACTOR;                                                                 //psi
    public static double STERIC_HINDRANCE_RADICAL_FACTOR;                                                               //phi
    public static double STERIC_HINDRANCE_COMBINED_FACTOR;                                                              //omega
    public static Expression RADIUS_EXPRESSION;

    /**
     * Calculates static parameters which depend on others
     */
    public static void init() throws ParseException {
        LOG_TO_FILE = LOG_LEVEL.toInt() <= Level.DEBUG.toInt();                                                         //Only log in debug mode

        for(String s : MAX_3D_MODEL_SIZES_TEXT.split(",")){
            if(!s.trim().isEmpty())
                MAX_3D_MODEL_SIZES.add(Integer.valueOf(s.trim()));
        }

        STERIC_HINDRANCE_RADICAL_FACTOR  = STERIC_HINDRANCE_RADICAL_PERCENTAGE  / 100;
        STERIC_HINDRANCE_VINYL_FACTOR    = STERIC_HINDRANCE_VINYL_PERCENTAGE    / 100;
        STERIC_HINDRANCE_COMBINED_FACTOR = STERIC_HINDRANCE_COMBINED_PERCENTAGE / 100;

        double totalConcentration =
                CONCENTRATION_RAFT + CONCENTRATION_CROSSLINKER + CONCENTRATION_MONOMER + 2*CONCENTRATION_INITIATORS;
        VESSEL_VOLUME = NUMBER_OF_MOLECULES /((totalConcentration)* NUMBER_AVOGADRO) * Math.pow(10, 24);                //dm3 to nm3
        MIN_MOL_VOLUME = 4.1887902 * Math.pow(MONOMER_LENGTH,3);                                                        //pi*4/3(l*sqrt(1/6))^3

        NUMBER_HALF_INITIATORS = (long) (NUMBER_OF_MOLECULES * 2*CONCENTRATION_INITIATORS / totalConcentration);        //Split initiators into half-initiators
        NUMBER_HALF_INITIATORS +=(long) (NUMBER_OF_MOLECULES * CONCENTRATION_RAFT         / totalConcentration);        //Add half-initiators introduced via RAFT equilibrium
        NUMBER_CROSSLINKERS =    (long) (NUMBER_OF_MOLECULES * CONCENTRATION_CROSSLINKER  / totalConcentration);
        NUMBER_MONOMERS =        (long) (NUMBER_OF_MOLECULES * CONCENTRATION_MONOMER      / totalConcentration);

        RADIUS_EXPRESSION = ExpressionGenerator.generate(INTERACTION_RADIUS_FORMULA);                                   //Generate function for gyration radius

        log.debugln("VESSEL VOLUME (nm3): "+VESSEL_VOLUME);
    }

}
