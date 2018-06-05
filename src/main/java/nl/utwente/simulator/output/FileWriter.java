package nl.utwente.simulator.output;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static nl.utwente.simulator.config.Settings.*;


public class FileWriter {

    /**
     * Creates file with name indicating number of each type of molecule in the simulator, steric hindrance factor, particle type, particle group type and dateTime of execution (in chinese format)
     * @param fileName Partial name of the file
     * @param extension Extension indicating file type
     * @param contents Contents of file
     */
    public static void export(String fileName, String extension, String contents) throws IOException {
        String file = createFileName(fileName, extension);
        write(file, contents);
    }

    /**
     * Creates file at <code>EXPORT_FOLDER/fileName.extension</code>
     * @param fileName  Name of the file
     * @param extension Extension indicating file type
     * @param contents Contents of file
     */
    public static void simpleExport(String fileName, String extension, String contents) throws IOException {
        String file = createSimpleFileName(fileName,extension);
        write(file, contents);
    }

    private static String createSimpleFileName(String fileName, String extension){
        return String.format("%s.%s",fileName, extension);
    }

    private static String createFileName(String fileName, String extension){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm");
        format.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));

        return String.format("%s (RAFT[%f] M[%f] C[%f])%s.%s",fileName, CONCENTRATION_RAFT,  CONCENTRATION_MONOMER, CONCENTRATION_CROSSLINKER, format.format(new Date()),extension);
    }

    private static void write(String file, String contents) throws IOException {
        log.infoln(String.format("[INFO]Exporting %s to %s", file, OUTPUT_DIRECTORY));
        createDirectoryIfNonExistent(OUTPUT_DIRECTORY);
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_DIRECTORY+ File.separator+file), "utf-8"));
        writer.write(contents);
        writer.close();
    }

    public static void createDirectoryIfNonExistent(String path) throws IOException {
        File f = new File(path);

        if(!f.exists()){
            f.mkdirs();
            log.infoln("[INFO]Created directory '"+path+"'");
        }else if(!f.isDirectory()){
            throw new IOException("Expected directory, got file: "+path);
        }
    }

    private PrintWriter writer;

    /**
     * For bigger files it would be more efficient to appendLine than to keep the full content in memory;
     * for this purpose an instance of FileWriter can be created, which allows appending
     */
    public FileWriter(String fileName, String extension) throws IOException {
        String file = createFileName(fileName, extension);
        log.infoln(String.format("[INFO]Exporting %s to %s", file, OUTPUT_DIRECTORY));
        createDirectoryIfNonExistent(OUTPUT_DIRECTORY);
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_DIRECTORY+File.separator+file), "utf-8")));
    }

    public void appendLine(String line){
        writer.println(line);
    }

    public void append(String line){
        writer.print(line);
    }

    public void finish(){
        writer.close();
    }

}
