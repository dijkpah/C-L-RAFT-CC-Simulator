package nl.utwente.simulator.output;

import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.entities.species.structured.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static nl.utwente.simulator.config.Settings.log;
import static nl.utwente.simulator.output.ProgressBar.logProgress;

public class PDBGenerator {
    /**
     * COLUMNS        DATA  TYPE    FIELD        DEFINITION
     -------------------------------------------------------------------------------------
     1 -  6         Record name   "ATOM  "
     7 - 11         Integer       serial       Atom  serial number.
     13 - 16        Atom          name         Atom name.
     17             Character     altLoc       Alternate location indicator.
     18 - 20        Residue name  resName      Residue name.
     22             Character     chainID      Chain id.
     23 - 26        Integer       resSeq       Residue sequence number.
     27             AChar         iCode        Code for insertion of residues.
     31 - 38        Real(8.3)     x            Orthogonal coordinates for X in Angstroms.
     39 - 46        Real(8.3)     y            Orthogonal coordinates for Y in Angstroms.
     47 - 54        Real(8.3)     z            Orthogonal coordinates for Z in Angstroms.
     55 - 60        Real(6.2)     occupancy    Occupancy.
     61 - 66        Real(6.2)     tempFactor   Temperature  factor.
     77 - 78        LString(2)    element      Element symbol, right-justified.
     79 - 80        LString(2)    charge       Charge  on the atom.

                                             1         2         3         4         5         6         7         8
                                    12345678901234567890123456789012345678901234567890123456789012345678901234567890     */
    public static final String atomFormat = "ATOM  %5d                   %8.3f%8.3f%8.3f                      %-2s  ";


    /**
     * COLUMNS       DATA  TYPE      FIELD        DEFINITION
     -------------------------------------------------------------------------
     1 -  6        Record name    "CONECT"
     7 - 11         Integer        serial       Atom  serial number
     12 - 16        Integer        serial       Serial number of bonded atom
     17 - 21        Integer        serial       Serial number of bonded atom
     22 - 26        Integer        serial       Serial number of bonded atom
     27 - 31        Integer        serial       Serial number of bonded atom*/
    public static final String bondFormat = "CONECT%5d%5d               \n";


    /**
     * EXAMPLE:
     ATOM      1                     10.975  -2.428   6.735                       N
     ATOM      2                      9.566  -2.578   6.405                       C
     ATOM      3                      8.689  -3.034   7.601                       C
     ATOM      4                      9.156  -3.908   8.335                       O
     ATOM      5                      9.513  -3.583   5.273                       C
     ATOM      6                      8.120  -3.932   4.767                       C
     ATOM      7                      8.059  -5.151   3.839                       C
     ATOM      8                      8.986  -5.978   3.838                       O
     ATOM      9                      7.059  -5.270   3.122                       O
     ATOM     10                      7.445  -2.564   7.760                       N
     TER
     CONECT    1    2
     END

     NOTE:
     - first number of a CONECT should be greater than the second
    **/
    public static final String fileFormat = "%sTER\n%s%s%sEND";

    private static String id(int chain, Molecule molecule){
        return molecule+"@"+chain;
    }

    private static String getType(Molecule molecule){
        if(molecule instanceof Initiating)             return Settings.I_REPRESENTATIVE_ATOM;
        if(molecule instanceof Monomer)                return Settings.M_REPRESENTATIVE_ATOM;
        if(molecule instanceof Crosslinker.FirstHalf)  return Settings.C_REPRESENTATIVE_ATOM;
        if(molecule instanceof Crosslinker.SecondHalf) return Settings.C_REPRESENTATIVE_ATOM;
        return null;
    }

    public static void createDataFile(StructuredParticle p) throws IOException {
        Locale.setDefault(new Locale("en", "US"));                                                                      //We want to use point separator instead of comma
        FileWriter exporter = new FileWriter("nanogel "+p.size()+" monomers", "pdb");

        int amountOfMolecules = 1;

        String nodes = "";
        StringBuilder bonds = new StringBuilder();
        StringBuilder vinylGroups = new StringBuilder();
        StringBuilder crosslinks = new StringBuilder();

        HashMap<String, Molecule> molecules = new HashMap<>();
        HashMap<String, Integer> molAliases = new HashMap<>();

        List<Molecule> radicals = p.getReactiveCenters();

        //Add nodes and edges for each chain
        log.infoln();
        log.infoln("Creating chains:");
        int nrRads = radicals.size();
        for (int i = 0; i < nrRads; i++) {
            int percentage = 100*i/nrRads;
            if(percentage > (100*(i-1)/nrRads) ) {
                logProgress(percentage, true);
            }
            Molecule molecule = radicals.get(i);
            int j = 0;
            while (molecule != null) {

                float z = (molecule instanceof Crosslinker.SecondHalf) ? 0F : 2F;
                exporter.appendLine(String.format(atomFormat, amountOfMolecules, 0F + i, 0F + j, z, getType(molecule)));

                molecules.put(id(i, molecule), molecule);
                molAliases.put(id(i, molecule), amountOfMolecules);
                //Add bonds
                molecule = molecule.prev;
                if (molecule != null) {
                    bonds.append(String.format(bondFormat,
                            amountOfMolecules,
                            amountOfMolecules+1
                    ));
                }
                amountOfMolecules++;
                j++;
            }
        }
        logProgress(100, true);

        int max = (int) (p.numberOfC - p.numberOfActiveVinylGroups());
        int j=0;

        //Add crosslink edges
        log.infoln();
        log.infoln("Creating cross-linkers:");
        for(CrossLink c : p.getCrosslinks()){
            int percentage = 100*j/max;
            if(percentage > (100*(j-1)/max) ) {
                logProgress(percentage, true);
            }

            int id1 = molAliases.get(id(c.firstHalf.chainNr,c.firstHalf.molecule));
            int id2 = molAliases.get(id(c.secondHalf.chainNr,c.secondHalf.molecule));
            crosslinks.append(String.format(bondFormat, Math.min(id1, id2), Math.max(id1, id2)));
            j++;
        }
        logProgress(100, true);


        //Add nodes and edges representing pendent groups
        log.infoln();
        log.infoln("Creating pendent groups:");
        for(int i = 0, nrVinylGroups = p.getActiveVinylGroups().size(); i<nrVinylGroups; i++){
            int percentage = 100*i/nrVinylGroups;
            if(percentage > (100*(i-1)/nrVinylGroups) ) {
                logProgress(percentage, true);
            }

            CrossLink.Pointer vinylGroup = p.getActiveVinylGroups().get(i);
            int y=0;
            Molecule mol = radicals.get(vinylGroup.chainNr);
            while(mol != vinylGroup.molecule){
                mol = mol.prev;
                y++;
            }
            exporter.appendLine(String.format(atomFormat,
                    amountOfMolecules,
                    0F + vinylGroup.chainNr,
                    0F + y,
                    4F,
                    Settings.VINYL_GROUP_REPRESENTATIVE_ATOM
            ));
            vinylGroups.append(String.format(bondFormat,
                    molAliases.get(id(vinylGroup.chainNr,vinylGroup.molecule)),
                    amountOfMolecules
            ));
            amountOfMolecules++;
        }
        logProgress(100, true);
        log.infoln();

        //Create PDB file
        String contents = String.format(fileFormat, "", bonds.toString(), crosslinks.toString(), vinylGroups.toString() );//nodes is empty
        exporter.append(contents);
        exporter.finish();
    }


}
