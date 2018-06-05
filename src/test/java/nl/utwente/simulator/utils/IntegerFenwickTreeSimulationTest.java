package nl.utwente.simulator.utils;

import nl.utwente.simulator.ValidationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(ValidationTest.class)
public class IntegerFenwickTreeSimulationTest extends ValidationTest {

    @Test
    public void testInsert(){
        IntegerFenwickTree ft = new IntegerFenwickTree(4);
        int[] t1 = new int[]{8,4,6,3,0,5,2,1,11,4};
        int[] t2 = new int[]{8,4,6,3,0,9,2,1,11,4};
        int[] t3 = new int[]{0,4,6,3,0,9,2,1,0,4};
        for(int i=0;i<t1.length;i++){
            ft.adj(i,t1[i]);
        }

        int sum=0;
        for(int i=0;i<t1.length;i++) {
            sum+=t1[i];
            assertEquals(ft.rsq(i), sum);
        }

        ft.adj(5,4);

        sum=0;
        for(int i=0;i<t2.length;i++) {
            sum+=t2[i];
            assertEquals(ft.rsq(i), sum);
        }

        ft.adj(8,-11);
        ft.adj(0,-8);

        sum=0;
        for(int i=0;i<t3.length;i++) {
            sum+=t3[i];
            assertEquals(ft.rsq(i), sum);
        }
    }

    @Test
    public void testIndex(){
        IntegerFenwickTree ft = new IntegerFenwickTree();
        int[] t1 = new int[]{8,4,6,3,0,5,2,1,11,4};
        for(int i=0;i<t1.length;i++){
            ft.add(t1[i]);
        }

        System.out.println("VAL:"+ft);

        int sum = 0;
        String s = "SUM:";
        String r = "RQS:";
        for(int i=0;i<t1.length;i++) {
            r+=ft.rsq(i)+", ";
            sum+=t1[i];
            s+=sum+", ";
        }
        System.out.println(r);
        System.out.println(s);

        for(int i=0;i<t1.length;i++) {
            for(long j=ft.rsq(i-1);j<ft.rsq(i);j++){
                System.out.println(j+": "+ft.indexOf(j));
                assertEquals(i, ft.indexOf(j));
            }
        }

        ft = new IntegerFenwickTree();
        int[] t2 = new int[]{0,0,1,0,0,1,0,1,1,0,0,1};
        for(int i=0;i<t2.length;i++){
            ft.add(t2[i]);
        }

        System.out.println("VAL:"+ft);

        sum = 0;
        s = "SUM:";
        r = "RQS:";
        for(int i=0;i<t2.length;i++) {
            r+=ft.rsq(i)+", ";
            sum+=t2[i];
            s+=sum+", ";
        }
        System.out.println(r);
        System.out.println(s);

        for(int i=0;i<t2.length;i++) {
            for(long j=ft.rsq(i-1);j<ft.rsq(i);j++){
                System.out.println(j+": "+ft.indexOf(j));
                assertEquals(i, ft.indexOf(j));
            }
        }
    }

    @Test
    public void testSampling(){
        IntegerFenwickTree ft = new IntegerFenwickTree();
        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(2);

        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);

        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);

        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);
        ft.add(0);

        ft.add(0);
        ft.add(0);
        ft.add(1);

        assertEquals(4, ft.indexOf(0));
        assertEquals(4, ft.indexOf(1));
        assertEquals(22, ft.indexOf(2));
    }
}
