package nl.utwente.simulator.output;

import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.entities.species.structured.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static nl.utwente.simulator.config.Settings.log;

//TODO: sporadically generates a 3D model that is not supported by tools
//Tools seem to stop reading bonds in models with more than 255 atoms, which is the formal limit to MOL files

public class MOLGenerator {


    /**
     * The Atom Block
     *
     * xxxxx.xxxxyyyyy.yyyyzzzzz.zzzz aaaddcccssshhhbbbvvvHHHrrriiimmmnn
     *
     * Where:
     *    x y z atom coordinates [Generic]
     *    aaa atom symbol entry in periodic table or L for atom list
     */
    public static final String atomFormat = "%10.4f%10.4f%10.4f %s\n";


    /**
     *
     */
    public static final String bondFormat = "%-3d%-3d\n";

    /**
     * The Counts Line
     *
     * aaabbblllfffcccsssxxxrrrpppiiimmmvvvvvv
     *
     * Where:
     *    aaa = number of atoms (current max 255)* [Generic]
     *    bbb = number of bonds (current max 255)* [Generic]
     */

    /**
     * EXAMPLE:
     *
     [NAME]


     6 6
     1.9050    -0.7932    0.0000 S
     1.9050    -2.1232    0.0000 P
     0.7531    -0.1282    0.0000 C
     0.7531    -2.7882    0.0000 C
     -0.3987    -0.7932    0.0000 C
     -0.3987    -2.1232    0.0000 C
     2 1
     3 1
     4 2
     5 3
     6 4
     6 5
     M  END
     *
     * NOTE:
     * - line 1: particle name
     * - line 2: author info
     * - line 3: comment
     * - line 4: number of atoms and number of bonds
     **/
    public static final String fileFormat = "Nanogel Species\n\n\n%-3d%-3d\n%s%sM  END";

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
        if(p.size() + p.numberOfC > 999){
            log.error("Cannot create MOL file for particle with more than 999 molecules");
            return;
        }

        Locale.setDefault(new Locale("en", "US"));                                                                      //We want to use point separator instead of comma

        int amountOfMolecules = 1;
        int amountOfBonds = 0;

        String nodes = "";
        String edges = "";

        HashMap<String, Molecule> molecules = new HashMap<>();
        HashMap<String, Integer> molAliases = new HashMap<>();

        List<Molecule> radicals = p.getReactiveCenters();

        //Add nodes and edges for each chain
        for (int i = 0; i < radicals.size(); i++) {
            Molecule molecule = radicals.get(i);
            int j = 0;
            while (molecule != null) {

                float z = (molecule instanceof Crosslinker.SecondHalf) ? 0F : 2F;
                nodes += String.format(atomFormat, 0F + i, 0F + j, z, getType(molecule));

                molecules.put(id(i, molecule), molecule);
                molAliases.put(id(i, molecule), amountOfMolecules);
                //Add bonds
                molecule = molecule.prev;
                if (molecule != null) {
                    edges += String.format(bondFormat,
                            amountOfMolecules,
                            amountOfMolecules+1
                    );
                    amountOfBonds++;
                }
                amountOfMolecules++;
                j++;
            }
        }

        //Add crosslink edges
        for(CrossLink c : p.getCrosslinks()){
            int id1 = molAliases.get(id(c.firstHalf.chainNr,c.firstHalf.molecule));
            int id2 = molAliases.get(id(c.secondHalf.chainNr,c.secondHalf.molecule));
            edges += String.format(bondFormat, Math.min(id1, id2), Math.max(id1, id2));
            amountOfBonds++;
        }

        //Add nodes and edges representing pendant groups
        for(CrossLink.Pointer vinylGroup : p.getActiveVinylGroups()){
            int y=0;
            Molecule mol = radicals.get(vinylGroup.chainNr);
            while(mol != vinylGroup.molecule){
                mol = mol.prev;
                y++;
            }
            nodes += String.format(atomFormat,
                    0F + vinylGroup.chainNr,
                    0F + y,
                    4F,
                    Settings.VINYL_GROUP_REPRESENTATIVE_ATOM
            );
            edges += String.format(bondFormat,
                    molAliases.get(id(vinylGroup.chainNr,vinylGroup.molecule)),
                    amountOfMolecules
            );
            amountOfBonds++;
            amountOfMolecules++;
        }

        amountOfMolecules--;
        //Create PDB file
        String contents = String.format(fileFormat, amountOfMolecules, amountOfBonds, nodes, edges);
        FileWriter.export("nanogel "+p.size()+" monomers", "mol", contents);
    }
}
