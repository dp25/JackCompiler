import java.io.File;
import java.io.IOException;


public class JackAnalyzer {

    public static void main(String[] args) throws IOException {
       // read the file
       String file = args[0];
       System.out.println("the input = " + file);
       // creating the output
       File check = new File(file);
       System.out.println("the check = " + check.getAbsolutePath());


       // checks whether th file is a directory or a single file 
        if (check.isDirectory()){
     
            File[] directoryListing = check.listFiles();

            for (File f : directoryListing){
                System.out.println("File f name is: " + f.getName());
                if(f.getName().endsWith(".jack")){
                    System.out.println("good");
                    String outputName = f.getAbsolutePath().replace(".jack", ".xml");
                    File output = new File(outputName);
                    CompilationEngine e = new CompilationEngine(f,output);
                    e.compileClass();
                }
            }
        } else{
            String outputName = file.replace(".jack", ".xml");
            File output = new File(outputName);
            CompilationEngine e = new CompilationEngine(check,output);
            e.compileClass();
        }
    }
    
}
