package nl.utwente.simulator;

import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.entities.RadicalPosition;
import nl.utwente.simulator.entities.species.structured.StructuredParticle;
import nl.utwente.simulator.entities.species.structured.StructuredSpeciesFactory;
import nl.utwente.simulator.entities.species.unstructured.UnstructuredSpeciesFactory;
import nl.utwente.simulator.input.ExcelInput;
import nl.utwente.simulator.input.InputSource;
import nl.utwente.simulator.output.CSVGenerator;
import nl.utwente.simulator.output.MOLGenerator;
import nl.utwente.simulator.output.PDBGenerator;
import nl.utwente.simulator.output.dynamicoutput.DynamicOutput;
import nl.utwente.simulator.simulator.*;
import nl.utwente.simulator.utils.IntegerFenwickTree;
import nl.utwente.simulator.utils.RadicalTracker;
import nl.utwente.simulator.utils.random.Random;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static nl.utwente.simulator.config.Settings.*;
import static nl.utwente.simulator.config.Settings.CSVColumn.*;

public class Simulator<SPECIES extends Species, ABSTRACTSPECIES extends AbstractSpecies> {

    private static final CSVGenerator csvGenerator = new CSVGenerator(CSVColumn.values());

    private final int maxILength, maxMLength, maxCLength, maxVinylGroupLength, maxWeightLength;                         //Maximum length of variables that will be logged for string formatter

    private long biggestParticleSize = 1;
    private SPECIES biggestParticle;
    private DynamicOutput dynamicOutput;

    private long startTime, stepNumber, sliceStartTime, sliceStartSteps;                                                //Time and progress checking variables
    private double previousConversion;

    private SpeciesFactory<SPECIES> speciesFactory;
    private AbstractSpeciesFactory<ABSTRACTSPECIES> keyFactory;
    private ABSTRACTSPECIES i, m, c;                                                                                    //Pointers to starting species
    public static int FIRST_POLYMER_INDEX;                                                                              //Index of first non-starting species

    private long initialNumberOfHalfInitiators, initialNumberOfMonomers, initialNumberOfCrosslinkers;                   //Initial number of molecules
    private long numberOfHalfInitiators, numberOfMonomers, numberOfCrosslinkers;                                        //Current number of molecules
    private long numberOfVinylGroups;
    private long numberOfPolymers = 0;
    private long numberOfVinylGroupsInParticles = 0;                                                                    //Can also be inferred from original number of molecules versus current ones if necessary
    private long numberOfReactiveCentersInPolymers = 0;

    private SortedSet<Integer> emptyIndices               = new TreeSet<>();
    private List<SPECIES> species                         = new ArrayList<>();
    private Map<ABSTRACTSPECIES, Integer> speciesIndices  = new HashMap<>();                                            //Molecules and their number of reactive groups
    private RadicalTracker radicalTracker                 = new RadicalTracker();
    private IntegerFenwickTree vinylIndices               = new IntegerFenwickTree();


    public Simulator(long i, long m, long c, AbstractSpeciesFactory<ABSTRACTSPECIES> abstractSpeciesFactory, SpeciesFactory<SPECIES> speciesFactory) throws ParseException {
        this.initialNumberOfHalfInitiators = i;
        this.initialNumberOfMonomers = m;
        this.initialNumberOfCrosslinkers = c;

        this.numberOfHalfInitiators = i;
        this.numberOfMonomers = m;
        this.numberOfCrosslinkers = c;

        this.keyFactory = abstractSpeciesFactory;
        this.speciesFactory = speciesFactory;

        SPECIES halfInitiator = speciesFactory.createI(i);
        SPECIES monomer       = speciesFactory.createM(m);
        SPECIES crosslinker   = speciesFactory.createC(c);

        this.i = abstractSpeciesFactory.abstractSpeciesForInitiatingMolecule();                                         //We add abstract species to species and vice versa to speed up the simulator
        this.m = abstractSpeciesFactory.abstractSpeciesForMonomer();                                                    //but at the cost of a bit more memory usage
        this.c = abstractSpeciesFactory.abstractSpeciesForCrosslinker();

        halfInitiator.setAbstractSpecies(this.i);
        monomer.setAbstractSpecies(this.m);
        crosslinker.setAbstractSpecies(this.c);

        this.numberOfVinylGroups = 0;
        int moleculesSpeciesIndex = 0;
        for(SPECIES p : Arrays.asList(halfInitiator, monomer, crosslinker)){
            long vinylGroupsInSpecies = p.number() * p.numberOfActiveVinylGroups();

            this.species.add(p);
            this.speciesIndices.put((ABSTRACTSPECIES) p.getAbstractSpecies(), moleculesSpeciesIndex);
            this.radicalTracker.addAll(p);                                                                              //Add species radical reactivity to BITs
            this.vinylIndices.add(vinylGroupsInSpecies);                                                                //Add species vinyl reactivity to BIT

            this.numberOfVinylGroups += vinylGroupsInSpecies;
            moleculesSpeciesIndex++;
        }
        FIRST_POLYMER_INDEX = moleculesSpeciesIndex;

        this.biggestParticle = crosslinker;

        this.maxILength = (""+ initialNumberOfHalfInitiators).length();                                                 //Calculate maximum number lengths for log formatting
        this.maxMLength = (""+ initialNumberOfMonomers).length();
        this.maxCLength = (""+ initialNumberOfCrosslinkers).length();
        this.maxVinylGroupLength = (""+(CROSSLINKER_VINYL_GROUPS * initialNumberOfCrosslinkers + initialNumberOfMonomers)).length();
        this.maxWeightLength = (""+ ((long)(initialNumberOfHalfInitiators * WEIGHT_HALF_INITIATOR + initialNumberOfMonomers* WEIGHT_MONOMER + initialNumberOfCrosslinkers* WEIGHT_CROSSLINKER))).length();

        this.dynamicOutput = new DynamicOutput();                                                                       //Create instances of custom output functions
        for(String f : OUTPUT_FUNCTIONS.split(",")){
            try {
                dynamicOutput.addFunc(f);
            }catch(ParseException e){
                log.errorln("Incorrect function: "+f);
                throw e;
            }
        }
    }


    public void run() throws Exception {
        this.init();

        ABSTRACTSPECIES abstractRadicalSpecies, abstractVinylSpecies, combinedAbstractSpecies;
        SPECIES radical, vinyl, combinedSpecies;
        boolean intraMolecular;
        int radicalIndex;                                                                                               //Index of the radical species that will undergo a reaction
        int reactiveCenterIndexInParticle;                                                                              //Index of the reactive center in the list of reactive centers
        int vinylIndex;                                                                                                 //Index of the vinyl species that will undergo a reaction

        stepNumber = 1;
        while( !(numberOfPolymers == 1 && numberOfHalfInitiators == 0 && STOP_AT_GEL_POINT) &&
                numberOfVinylGroups != 0){

            //Step 1: pick active radical
            radicalIndex = pickRadical();
            radical  = species.get(radicalIndex);

            RadicalPosition rp;
            if(radical.isPolymer()) {

                reactiveCenterIndexInParticle = (int) Random.getRandom(radical.numberOfRadicals());
                rp = radical.positionOfNthRadical(reactiveCenterIndexInParticle);

                //Step 2: decide between intermolecular and intramolecular reaction
                double localConcentration = radical.localConcentration();
                intraMolecular = Random.getRandom(localConcentration + globalConcentration(radical)) < localConcentration;

                //Step 3: pick vinyl
                if(!intraMolecular){
                    vinylIndex = pickVinylButNotIn(radical, radicalIndex);
                    vinyl = species.get(vinylIndex);

                    //Step 4: success rate
                    if(rp == RadicalPosition.MID_CHAIN_CROSSLINKER ){
                        if(vinyl.isPolymer()){
                            if(Random.getRandom(1.0) < STERIC_HINDRANCE_COMBINED_FACTOR){
                                continue;
                            }
                        }else if(Random.getRandom(1.0) < STERIC_HINDRANCE_RADICAL_FACTOR){
                            continue;
                        }
                    }else{
                        if(vinyl.isPolymer() && Random.getRandom(1.0) < STERIC_HINDRANCE_VINYL_FACTOR){
                            continue;
                        }
                    }

                }else{
                    vinylIndex = radicalIndex;
                    vinyl = radical;

                    //Step 4: success rate
                    if(Random.getRandom(1.0) < STERIC_HINDRANCE_VINYL_FACTOR){
                        continue;
                    }
                }
            }else{
                reactiveCenterIndexInParticle = 0;
                rp = RadicalPosition.CHAIN_END_NON_CROSSLINKER;

                //Step 2: decide between intermolecular and intramolecular reaction
                intraMolecular = false;

                //Step 3: pick vinyl
                vinylIndex = pickVinyl();
                vinyl = species.get(vinylIndex);

                //Step 4: success rate
                if(vinyl.isPolymer() & Random.getRandom(1.0) < STERIC_HINDRANCE_VINYL_FACTOR){
                    continue;
                }
            }

            //Simulate reaction

            //Step 1: find abstractions of selected species
            abstractRadicalSpecies = (ABSTRACTSPECIES) radical.getAbstractSpecies();
            abstractVinylSpecies   = (ABSTRACTSPECIES) vinyl.getAbstractSpecies();                                      //Here we can access the associated abstract species directly instead of using the particlegroup factory

            //Step 2: find abstraction of reaction product
            try{combinedAbstractSpecies = intraMolecular
                    ? keyFactory.abstractSpeciesAfterIntraMolecularReaction(abstractRadicalSpecies, rp)
                    : keyFactory.abstractSpeciesAfterInterMolecularReaction(abstractRadicalSpecies, rp, abstractVinylSpecies);
            }catch(RejectedReaction e){continue;}

            //Step 3: Check existence of abstraction map
            Integer combinedIndex = speciesIndices.get(combinedAbstractSpecies);

            //If one exists
            if(combinedIndex != null){
                //We use it to obtain the reaction product
                combinedSpecies = species.get(combinedIndex);
                this.increaseParticle(combinedIndex, combinedSpecies);
            }else{
                //Step 4: Otherwise we use the associated reaction rule to obtain the reaction product
                try {combinedSpecies = intraMolecular
                            ? speciesFactory.createSpecies(radical, reactiveCenterIndexInParticle)
                            : speciesFactory.createSpecies(radical, reactiveCenterIndexInParticle, vinyl);
                }catch(RejectedReaction e){continue;}                                                                   //Chosen reaction is not possible (which may not be obvious from selected abstract species)
                combinedSpecies.setAbstractSpecies(combinedAbstractSpecies);
                this.addParticle(combinedSpecies);
            }

            //Update

            if(abstractRadicalSpecies == this.i){                                                                       //Update bookkeeping numbers
                numberOfReactiveCentersInPolymers++;
                numberOfHalfInitiators--;
            }if(abstractVinylSpecies == this.m){
                numberOfVinylGroupsInParticles++;
                numberOfMonomers--;
            }else if(abstractVinylSpecies == this.c){
                numberOfVinylGroupsInParticles += CROSSLINKER_VINYL_GROUPS;
                numberOfCrosslinkers--;
            }

            this.decreaseParticle(radicalIndex, radical);
            if(!intraMolecular){                                                                                        //Inter-molecular reaction
                this.decreaseParticle(vinylIndex, vinyl);
                if (vinyl.isPolymer()) {
                    if(radical.isPolymer()) {                                                                           //P• + P=
                        numberOfPolymers--;
                    }
                }else if(abstractRadicalSpecies.equals(this.i)){                                                        //I• + M= || I• + =C-C=
                    numberOfPolymers++;
                }
            }
            numberOfVinylGroupsInParticles--;                                                                           //In each propagation reaction a vinyl group is consumed
            numberOfVinylGroups--;
            assert(numberOfVinylGroupsInParticles + numberOfCrosslinkers*CROSSLINKER_VINYL_GROUPS + numberOfMonomers == numberOfVinylGroups);

            //Iterate

            if(combinedSpecies.size() > biggestParticleSize){
                if( INPUT_SOURCE == InputSource.STRUCTURED &&
                    biggestParticle instanceof StructuredParticle &&
                    combinedSpecies.size()+combinedSpecies.numberOfC() >= MAX_3D_MODEL_SIZES.first()                    //NOTE: cross-linkers will be represented by two atoms
                ){
                    StructuredParticle p = (StructuredParticle) biggestParticle;
                    PDBGenerator.createDataFile(p);
                    MOLGenerator.createDataFile(p);
                    MAX_3D_MODEL_SIZES.remove(MAX_3D_MODEL_SIZES.first());

                    if(MAX_3D_MODEL_SIZES.isEmpty())
                        break;
                }
                biggestParticleSize = combinedSpecies.size();
                biggestParticle = combinedSpecies;
            }

            this.log();
            this.checkInvariants();
            stepNumber++;
        }

        this.finish();
    }

    private double globalConcentration(SPECIES radical){
        return (numberOfVinylGroups - radical.numberOfActiveVinylGroups())/ VESSEL_VOLUME;
    }

    private void checkInvariants(){
        assert(vinylIndices.size == species.size());                                                                    //BIT same size as list of species.
        assert(numberOfReactiveCentersInPolymers + numberOfHalfInitiators == initialNumberOfHalfInitiators);            //radical groups do not disappear
        assert(numberOfVinylGroupsInParticles + CROSSLINKER_VINYL_GROUPS* numberOfCrosslinkers + numberOfMonomers
                == (initialNumberOfMonomers +CROSSLINKER_VINYL_GROUPS* initialNumberOfCrosslinkers)- stepNumber);       //only one double bond is consumed in each time step
        assert(biggestParticleSize
                <= initialNumberOfCrosslinkers + initialNumberOfMonomers + initialNumberOfHalfInitiators);              //biggest particle cannot be bigger than all molecules combined
    }

    private int pickRadical(){
        int index = radicalTracker.pickRadical(this.numberOfVinylGroups);
        assert(species.get(index).numberOfRadicals() >0);
        assert(species.get(index).number() >0);
        return index;
    }

    private int pickVinyl(){
        assert(numberOfVinylGroups == vinylIndices.totalSum);
        long sample = Random.getRandom(numberOfVinylGroups);
        int index = vinylIndices.indexOf(sample);
        assert(species.get(index).numberOfActiveVinylGroups() >0);
        assert(species.get(index).number() >0);
        return index;
    }

    private int pickVinylButNotIn(SPECIES radical, int radicalIndex){
        assert(numberOfVinylGroups == vinylIndices.totalSum);
        long max = numberOfVinylGroups - radical.numberOfActiveVinylGroups();                                           //Exclude vinyl groups in radical from sampling
        long sample = Random.getRandom(max);
        long preRadicalSum = vinylIndices.rsq(radicalIndex);                                                            //Sum of all vinyl groups of species with index < radicalIndex
        int index;
        if(sample < preRadicalSum - radical.numberOfActiveVinylGroups()){                                               //return regular index if sample matches vinyl group with index smaller than excluded ones
            index = vinylIndices.indexOf(sample);
        }else{
            index = vinylIndices.indexOf(sample + radical.numberOfActiveVinylGroups());                                 //Include offset to exclude vinyl groups in radical particle
        }
        assert(species.get(index).numberOfActiveVinylGroups() >0);
        assert(species.get(index).number() >0);
        return index;
    }

    private void halfTree(){
        int subtreeSize = vinylIndices.subtreeSize();
        for(int i = vinylIndices.size-1; i>=subtreeSize; i--){
            if(i== emptyIndices.last()){                                                                                //Already empty
                emptyIndices.remove(i);
                species.remove(i);
            }else{                                                                                                      //Relocate data
                SPECIES p = species.get(i);
                int firstIndex = emptyIndices.first();
                vinylIndices.adj(i, -p.numberOfActiveVinylGroups()*p.number());                                         //Remove from old position
                radicalTracker.relocate(p, i, firstIndex);
                species.remove(i);
                emptyIndices.remove(firstIndex);

                vinylIndices.adj(firstIndex, p.numberOfActiveVinylGroups()*p.number());                                 //Add at new position
                species.set(firstIndex, p);
                speciesIndices.put((ABSTRACTSPECIES) p.getAbstractSpecies(), firstIndex);

                assert(i==0|| vinylIndices.rsq(i) == vinylIndices.rsq(vinylIndices.size-1));                            //Check whether number of reactive groups at position i is zero
            }
        }
        vinylIndices.halfData();
        radicalTracker.halfData();
    }

    private void addParticle(SPECIES p){
        int newIndex;
        if(emptyIndices.isEmpty()){
            newIndex = species.size();
        }else{
            newIndex = emptyIndices.first();
            emptyIndices.remove(newIndex);
        }

        speciesIndices.put((ABSTRACTSPECIES) p.getAbstractSpecies(), newIndex);
        if(newIndex == species.size()){                                                                                 //Add at end of particle list
            species.add(p);
            vinylIndices.add(p.numberOfActiveVinylGroups());
            radicalTracker.addOne(p);
        }else {                                                                                                         //Add at unused index
            species.set(newIndex, p);
            vinylIndices.adj(newIndex, p.numberOfActiveVinylGroups());
            radicalTracker.set(p, newIndex);
        }
    }

    private void removeParticle(int particleIndex, ABSTRACTSPECIES group){
        assert(particleIndex==0|| vinylIndices.rsq(speciesIndices.get(group)) == vinylIndices.rsq(speciesIndices.get(group)-1));
        emptyIndices.add(particleIndex);
        speciesIndices.remove(group);
        if(vinylIndices.size-emptyIndices.size() <= vinylIndices.subtreeSize()-SHRINK_THRESHOLD){                       //If data + threshold fits in subtree
            halfTree();
        }
    }

    private void increaseParticle(int particleIndex, SPECIES p){
        p.increaseNumber();
        radicalTracker.increaseParticle(p, particleIndex);
        vinylIndices.adj(particleIndex, p.numberOfActiveVinylGroups());
    }

    private void decreaseParticle(int particleIndex, SPECIES p){
        p.decreaseNumber();
        radicalTracker.decreaseParticle(p, particleIndex);
        vinylIndices.adj(particleIndex, -p.numberOfActiveVinylGroups());

        if(p.number()==0 && p.isPolymer()) {                                                                            //Only remove polymer species
            removeParticle(particleIndex, (ABSTRACTSPECIES) p.getAbstractSpecies());
        }
    }



    private void log(){
        double conversion =  ((double)(100 * stepNumber) / (CROSSLINKER_VINYL_GROUPS*initialNumberOfCrosslinkers + initialNumberOfMonomers));

        if (conversion < 100 && conversion < previousConversion + PERCENTAGE_PER_SLICE) {                               //Only show output at every percent of conversion
            return;
        }

        long endTime = System.currentTimeMillis();                                                                      //Calculate all values that will be logged, as they will be used for CSV export as well
        double sliceDuration   = endTime - sliceStartTime;
        double totalDuration   = endTime - startTime;
        double sliceSteps      = stepNumber - sliceStartSteps;
        long stepsPerSecond    = (long)((((double) sliceSteps)*1000D) / sliceDuration);
        long avgStepsPerSecond = (long)((((double) stepNumber)*1000D) / totalDuration);

        final String SPEED_FORMAT          = "step %"+ maxVinylGroupLength +"d (%7d steps/s, avg: %7d):";
        final String PARTICLE_FORMAT       = "Species: %"+maxILength+"d | Polymers: %"+maxILength+"d | Biggest: %"+maxWeightLength+"d Da | ";
        final String MOLECULES_FORMAT      = "%.2f%% | i/m/c %"+maxILength+"d/%"+maxMLength+"d/%"+maxCLength+"d | ";
        final String REACTIVE_GROUP_FORMAT = "= %"+ maxVinylGroupLength +"d | ";


        log.debug(String.format(SPEED_FORMAT, stepNumber, stepsPerSecond, avgStepsPerSecond));
        log.info( String.format(MOLECULES_FORMAT, conversion, numberOfHalfInitiators, numberOfMonomers, numberOfCrosslinkers));
        log.debug(String.format(REACTIVE_GROUP_FORMAT, numberOfVinylGroupsInParticles));
        log.infoln( String.format(PARTICLE_FORMAT, speciesIndices.size(), numberOfPolymers, (long)biggestParticle.getWeight()));

        dynamicOutput.addRow(conversion, species, biggestParticle);

        if(LOG_TO_FILE){
            Map<CSVColumn, Object> row = new HashMap<>();
            row.put(CONVERSION, String.format(Locale.ROOT, "%.2f",conversion));
            row.put(NUMBER_I, numberOfHalfInitiators);
            row.put(NUMBER_M, numberOfMonomers);
            row.put(NUMBER_C, numberOfCrosslinkers);
            row.put(NUMBER_POLYMERS, numberOfPolymers);
            row.put(NUMBER_SPECIES, speciesIndices.size());
            row.put(BIGGEST_PARTICLE_WEIGHT, biggestParticleSize);
            row.put(STEPS_PER_SECOND, stepsPerSecond);
            row.put(STEPS_PER_SECOND_AVG, avgStepsPerSecond);
            row.put(MEMORY_USAGE, this.getMemoryUsage());
            csvGenerator.addRow(row);
        }

        sliceStartSteps = stepNumber;                                                                                   //Update slice info
        sliceStartTime = System.currentTimeMillis();
        previousConversion = conversion;
    }

    private void init(){
        String speciesType = species.get(0).getClass().getSimpleName();
        String abstractSpeciesType = this.i.getClass().getSimpleName();

        log.infoln(String.format("i/m/c: %d/%d/%d", initialNumberOfHalfInitiators, initialNumberOfMonomers, initialNumberOfCrosslinkers));
        log.infoln(String.format("Abstract species type: %s", abstractSpeciesType));
        log.infoln(String.format("Species type: %s", speciesType));

        startTime = System.currentTimeMillis();
        sliceStartTime = startTime+1;

        if(LOG_TO_FILE){
            Map<CSVColumn, Object> row = new HashMap<>();
            row.put(CONVERSION, 0);
            row.put(NUMBER_I, numberOfHalfInitiators);
            row.put(NUMBER_M, numberOfMonomers);
            row.put(NUMBER_C, numberOfCrosslinkers);
            row.put(NUMBER_POLYMERS, numberOfPolymers);
            row.put(NUMBER_SPECIES, speciesIndices.size());
            row.put(BIGGEST_PARTICLE_WEIGHT, biggestParticleSize);
            row.put(STEPS_PER_SECOND, 0);
            row.put(STEPS_PER_SECOND_AVG, 0);
            row.put(MEMORY_USAGE, this.getMemoryUsage());
            row.put(NOTES, String.format("Species type: %s, Abstract Species type: %s", speciesType, abstractSpeciesType));
            csvGenerator.addRow(row);
        }

        dynamicOutput.addRow(0, species, biggestParticle);
    }

    private void finish(){
        long endTime = System.currentTimeMillis();
        log.debugln(String.format("Execution time: %d seconds",((endTime-startTime)/1000)));

        try {
            if(LOG_TO_FILE) {
                csvGenerator.export();
            }

            dynamicOutput.export();

            if( INPUT_SOURCE == InputSource.STRUCTURED){
                StructuredParticle p = (StructuredParticle) biggestParticle;
                PDBGenerator.createDataFile(p);
                MOLGenerator.createDataFile(p);
            }
        } catch (IOException e) {
            log.errorln("[ERROR]"+e.getMessage());
        }
    }

    private long getMemoryUsage(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }


    public static void main(String[] args) throws Exception {
        if(!(new File(INPUT_DIRECTORY + File.separator + InputSource.UNSTRUCTURED.defaultFileName()).exists())){        //Generate unstructed.xlsx is none exists
            ExcelInput.generateExcel(Settings.class, InputSource.UNSTRUCTURED, false);
        }
        if(!(new File(INPUT_DIRECTORY + File.separator + InputSource.STRUCTURED.defaultFileName()).exists())){          //Generate structed.xlsx is none exists
            ExcelInput.generateExcel(Settings.class, InputSource.STRUCTURED, false);
        }

        boolean generateExcel = false;
        boolean customExcel = false;
        SpeciesFactory speciesFactory = new UnstructuredSpeciesFactory();

        for(String arg : args){
            arg = arg.toLowerCase();

            switch(arg){
                case "-d" :
                case "-debug" :
                    LOG_LEVEL = Level.DEBUG;
                    LOG_TO_FILE = true;
                    log.setLevel(LOG_LEVEL);
                    break;
                case "--mode=structured" :
                case "-m=structured" :
                    INPUT_SOURCE = InputSource.STRUCTURED;
                    INPUT_FILE = INPUT_SOURCE.defaultFileName();
                    speciesFactory = new StructuredSpeciesFactory();
                    break;
                case "--mode=unstructured" :
                case "-m=unstructured" :
                    INPUT_SOURCE = InputSource.UNSTRUCTURED;
                    INPUT_FILE = INPUT_SOURCE.defaultFileName();
                    speciesFactory = new UnstructuredSpeciesFactory();
                    break;
                case "--generate-excel" :                                                                               //Allow excel generation even though excels are already present
                case "-ge" :
                    generateExcel = true;
                    break;
                default :
                    if((arg.startsWith("--mode") || arg.startsWith("-m")) && arg.split("=").length==2){
                        throw new RuntimeException("Error while reading \""+arg+"\": "+arg.split("=")[1]+" is not a valid option");
                    }if(arg.endsWith(".xlsx")) {
                        if(customExcel){
                            throw new RuntimeException("Error while reading \""+arg+"\": "+arg.split("=")[1]+" Excel file was already specified");
                        }else{                                                                                          //Use path to provided excel instead of the one in /input
                            customExcel = true;
                            File f = new File(arg);
                            INPUT_DIRECTORY = f.getParent();
                            INPUT_FILE = f.getName();
                            OUTPUT_DIRECTORY = f.getParent()+File.separator+"output"+File.separator+INPUT_FILE.split("\\.")[0];
                        }
                    }else {
                        log.errorln("[ERROR] Unknown argument "+arg);
                    }
            }
        }

        if(generateExcel) {
            ExcelInput.validate(Settings.class);
            ExcelInput.generateExcel(Settings.class, INPUT_SOURCE, false);
        } else {
            ExcelInput.readExcel(Settings.class, INPUT_SOURCE, false);                                                  //Read settings from INPUT_SOURCE and write to Settings.class
            Settings.init();                                                                                            //Calculate other settings

            if (INPUT_SOURCE != InputSource.STRUCTURED || speciesFactory instanceof StructuredSpeciesFactory) {

                Simulator sim = new Simulator(
                        NUMBER_HALF_INITIATORS,
                        NUMBER_MONOMERS,
                        NUMBER_CROSSLINKERS,
                        abstractSpeciesFactory,
                        speciesFactory
                );
                sim.run();
            } else {
                log.errorln("[ERROR]Cannot make particle structure when using an unstructured particle representation");
            }
        }
    }
}
