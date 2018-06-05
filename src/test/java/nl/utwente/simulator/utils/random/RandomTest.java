package nl.utwente.simulator.utils.random;

import nl.utwente.simulator.config.Settings;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import static nl.utwente.simulator.config.Settings.*;

public abstract class RandomTest {

    public static final long samples = 100000000;

    public abstract long getRandom(long min, long max);
    public abstract long getRandom(long max);
    public abstract String getName();

    @Before
    public void init() throws ParseException {
        Settings.init();
    }

    @Test
    public void testRandomWithMin(){
        long startTime = System.currentTimeMillis();

        long i=0;
        long m=0;
        long c=0;

        long sum = NUMBER_CROSSLINKERS + NUMBER_HALF_INITIATORS + NUMBER_MONOMERS;

        for(int j=0;j<samples;j++){
            long sample = getRandom(0, sum);
            if(sample< NUMBER_HALF_INITIATORS){
                i++;
            }else if(sample < NUMBER_HALF_INITIATORS +NUMBER_MONOMERS){
                m++;
            }else{
                c++;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("[TEST WITH MINIMUM]");
        System.out.println("Name: "+getName());
        System.out.println("Execution time:"+(endTime-startTime)+"ms");
        System.out.println("i:"+i+" | c:"+c+" | m:"+m);
        System.out.println("m/i="+((double)m/(double)i));
        System.out.println("m/c="+((double)m/(double)c));
    }

    @Test
    public void testRandomWithoutMin(){
        long startTime = System.currentTimeMillis();

        long i=0;
        long m=0;
        long c=0;

        long sum = NUMBER_CROSSLINKERS + NUMBER_HALF_INITIATORS + NUMBER_MONOMERS;

        for(int j=0;j<samples;j++){
            long sample = getRandom(sum);
            if(sample< NUMBER_HALF_INITIATORS){
                i++;
            }else if(sample < NUMBER_HALF_INITIATORS + NUMBER_MONOMERS){
                m++;
            }else{
                c++;
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("[TEST WITHOUT MINIMUM]");
        System.out.println("Name: "+getName());
        System.out.println("Execution time:"+(endTime-startTime)+"ms");
        System.out.println("i:"+i+" | c:"+c+" | m:"+m);
        System.out.println("m/i="+((double)m/(double)i));
        System.out.println("m/c="+((double)m/(double)c));
    }

    @Test
    public void createRandomImageColor(){
        //image dimension
        int width = 1024;
        int height = 1024;
        //create buffered image object img
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //file object
        File f;
        //create random image pixel by pixel
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int a = 255; //alpha
                int r = (int)getRandom(256); //red
                int g = (int)getRandom(256); //green
                int b = (int)getRandom(256); //blue

                int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel
                img.setRGB(x, y, p);
            }
        }
        //write image
        try{
            System.out.println(System.getProperty("user.dir"));
            f = new File(OUTPUT_DIRECTORY+File.separator+getName().toLowerCase()+"_color.png");
            ImageIO.write(img, "png", f);
        }catch(IOException e){
            System.out.println("Error: " + e);
        }
    }
    @Test
    public void createRandomImageBlackWhite(){
        //image dimension
        int width = 1024;
        int height = 1024;
        //create buffered image object img
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //file object
        File f;
        //create random image pixel by pixel
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int a = 255; //alpha
                int r;
                int g;
                int b;
                if(getRandom(2)>0){
                    r = 0; //red
                    g = 0; //green
                    b = 0; //blue
                }else{
                    r = 255; //red
                    g = 255; //green
                    b = 255; //blue
                }
                int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel
                img.setRGB(x, y, p);
            }
        }
        //write image
        try{
            f = new File(OUTPUT_DIRECTORY+File.separator+getName().toLowerCase()+"_black.png");
            ImageIO.write(img, "png", f);
        }catch(IOException e){
            System.out.println("Error: "+System.getProperty("user.dir")+ ":"  + e);
        }
    }
}
