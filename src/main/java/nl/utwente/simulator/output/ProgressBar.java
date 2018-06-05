package nl.utwente.simulator.output;

public class ProgressBar {

    public static void logProgress(int percentage, boolean clearLine){
        if(clearLine){
            System.out.print("\r");
        }

        StringBuilder bar = new StringBuilder(106);
        for(int j=0;j<=percentage;j++)
            bar.append("=");

        for(int j=percentage+1;j<=100;j++)
            bar.append(" ");

        System.out.print(String.format("[%s]%3d%%", bar.toString(), percentage));
        System.out.flush();
    }
}
