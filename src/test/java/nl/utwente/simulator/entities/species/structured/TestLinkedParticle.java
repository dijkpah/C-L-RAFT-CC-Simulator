package nl.utwente.simulator.entities.species.structured;

import nl.utwente.simulator.ValidationTest;
import nl.utwente.simulator.config.Settings;
import nl.utwente.simulator.simulator.RejectedReaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.text.ParseException;

import static nl.utwente.simulator.config.Settings.CROSSLINKER_VINYL_GROUPS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(ValidationTest.class)
public class TestLinkedParticle extends ValidationTest{

    StructuredSpeciesFactory factory = new StructuredSpeciesFactory();
    StructuredParticle i;
    StructuredParticle m;
    StructuredParticle c;
    StructuredParticle p1;
    StructuredParticle p2;
    StructuredParticle p3;
    StructuredParticle p4;
    StructuredParticle p5;


    @Before
    public void init() throws ParseException {
        Settings.init();
        i = factory.createI(1);
        m = factory.createM(1);
        c = factory.createC(1);
    }

    @Test
    public void testI(){
        assertTrue(i.numberOfRadicals()==1);
        assertTrue(i.numberOfActiveVinylGroups()==0);
        assertTrue(i.numberOfI()==1);
        assertTrue(i.numberOfC()==0);
        assertTrue(i.numberOfM()==0);
        assertTrue(i.size()==1);
    }

    @Test
    public void testM(){
        assertTrue(m.numberOfRadicals()==0);
        assertTrue(m.numberOfActiveVinylGroups()==1);
        assertTrue(m.numberOfI()==0);
        assertTrue(m.numberOfC()==0);
        assertTrue(m.numberOfM()==1);
        assertTrue(m.size()==1);
    }


    @Test
    public void testC(){
        assertTrue(c.numberOfRadicals()==0);
        assertTrue(c.numberOfActiveVinylGroups()==CROSSLINKER_VINYL_GROUPS);
        assertTrue(c.numberOfI()==0);
        assertTrue(c.numberOfC()==1);
        assertTrue(c.numberOfM()==0);
        assertTrue(c.size()==1);
    }

    private void preparePropagation() throws RejectedReaction {
        try {
            // (I) ← (C1)
            //         ⇑
            //        (=)
            p1 = factory.createSpecies(i, 0, c);
            // (I) ← (M1)
            p2 = factory.createSpecies(i, 0, m);
            // (I) ← (C1)
            //         ⇑
            //       (C2) → (M1) → (I)
            p3 = factory.createSpecies(p2, 0, p1);
            // (I) ← (C1)    (=)
            //         ⇑      ⇓
            //       (C2) → (C1) → (I)
            p4 = factory.createSpecies(p1, 0, p1);
            // (I) ← (C1) ← (C2)
            //         ⇑      ⇓
            //       (C2) → (C1) → (I)
            p5 = factory.createSpecies(p4, 0);
        }catch(UnsupportedOperationException e){
            preparePropagation();
        }catch(AssertionError e){
            e.printStackTrace();
        }
    }

    @Test
    public void testPropagation() throws RejectedReaction {
        preparePropagation();

        assertEquals(p1.numberOfRadicals(),1);
        assertEquals(p1.numberOfActiveVinylGroups(),1);
        assertEquals(p1.numberOfI(),1);
        assertEquals(p1.numberOfC(),1);
        assertEquals(p1.numberOfM(),0);
        assertEquals(p1.size(),2);
        assertEquals(p1.numberOfC(), countC1(p1));
        assertEquals(p1.numberOfC() - p1.numberOfActiveVinylGroups(), countC2(p1));
        assertEquals(p1.numberOfM(), countM(p1));
        assertEquals(p1.numberOfI(), countI(p1));

        assertEquals(p2.numberOfRadicals(),1);
        assertEquals(p2.numberOfActiveVinylGroups(),0);
        assertEquals(p2.numberOfI(),1);
        assertEquals(p2.numberOfC(),0);
        assertEquals(p2.numberOfM(),1);
        assertEquals(p2.size(),2);
        assertEquals(p2.numberOfC(), countC1(p2));
        assertEquals(p2.numberOfC() - p2.numberOfActiveVinylGroups(), countC2(p2));
        assertEquals(p2.numberOfM(), countM(p2));
        assertEquals(p2.numberOfI(), countI(p2));

        assertEquals(p3.numberOfRadicals(),2);
        assertEquals(p3.numberOfActiveVinylGroups(),0);
        assertEquals(p3.numberOfI(),2);
        assertEquals(p3.numberOfC(),1);
        assertEquals(p3.numberOfM(),1);
        assertEquals(p3.size(),4);
        assertEquals(p3.numberOfC(), countC1(p3));
        assertEquals(p3.numberOfC() - p3.numberOfActiveVinylGroups(), countC2(p3));
        assertEquals(p3.numberOfM(), countM(p3));
        assertEquals(p3.numberOfI(), countI(p3));

        assertEquals(p4.numberOfRadicals(),2);
        assertEquals(p4.numberOfActiveVinylGroups(),1);
        assertEquals(p4.numberOfI(),2);
        assertEquals(p4.numberOfC(),2);
        assertEquals(p4.numberOfM(),0);
        assertEquals(p4.size(),4);
        assertEquals(p4.numberOfC(), countC1(p4));
        assertEquals(p4.numberOfC() - p4.numberOfActiveVinylGroups(), countC2(p4));
        assertEquals(p4.numberOfM(), countM(p4));
        assertEquals(p4.numberOfI(), countI(p4));

        assertEquals(p5.numberOfRadicals(),2);
        assertEquals(p5.numberOfActiveVinylGroups(),0);
        assertEquals(p5.numberOfI(),2);
        assertEquals(p5.numberOfC(),2);
        assertEquals(p5.numberOfM(),0);
        assertEquals(p5.size(),4);
        assertEquals(p5.numberOfC(), countC1(p5));
        assertEquals(p5.numberOfC() - p5.numberOfActiveVinylGroups(), countC2(p5));
        assertEquals(p5.numberOfM(), countM(p5));
        assertEquals(p5.numberOfI(), countI(p5));
    }

    private int countM(StructuredParticle particle){
        int result = 0;
        for(Molecule rad : particle.getReactiveCenters()){
            Molecule mol = rad;
            while(mol!=null){
                if(mol instanceof Monomer){
                    result++;
                }
                mol = mol.prev;
            }
        }
        return result;
    }

    private int countC1(StructuredParticle particle){
        int result = 0;
        for(Molecule rad : particle.getReactiveCenters()){
            Molecule mol = rad;
            while(mol!=null){
                if(mol instanceof Crosslinker.FirstHalf){
                    result++;
                }
                mol = mol.prev;
            }
        }
        return result;
    }

    private int countC2(StructuredParticle particle){
        int result = 0;
        for(Molecule rad : particle.getReactiveCenters()){
            Molecule mol = rad;
            while(mol!=null){
                if(mol instanceof Crosslinker.SecondHalf){
                    result++;
                }
                mol = mol.prev;
            }
        }
        return result;
    }

    private int countI(StructuredParticle particle){
        int result = 0;
        for(Molecule rad : particle.getReactiveCenters()){
            Molecule mol = rad;
            while(mol!=null){
                if(mol instanceof Initiating){
                    result++;
                }
                mol = mol.prev;
            }
        }
        return result;
    }
}
