import java.io.*;

public class DirClassifier {
    private String src;
    private File dirRepo = new File("directory");
    private File fileRepo = new File("files");
    private BufferedWriter writer_dir;
    private BufferedWriter writer_file;

    public DirClassifier(String path) {
        this.src = path;
        try {
            this.writer_dir = new BufferedWriter(new FileWriter(dirRepo));
            this.writer_file = new BufferedWriter(new FileWriter(fileRepo));
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void exploreChild(File f) {
        // if this is a directory, recurse in it
        File[] all_children = f.listFiles();
        for(File child : all_children) {
            if(child.isDirectory()) {
                // prints the directory name to the file "dirRepo"
                System.out.println(child + " -> directory");
                try {
                    this.writer_dir.write(child.toString() + '\n');
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // recursively call on this directory
                exploreChild(child);
            }
            // otherwise it's a file
            else {
                System.out.println(child + " -> files");
                // print the filename to the file "fileRepo"
                try {
                    this.writer_file.write(child.toString() +'\n');
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void explore() {
        exploreChild(new File(this.src));
        try {
            this.writer_dir.close();
            this.writer_file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String path = args[0];
        System.out.println("Exploring " + path);
        DirClassifier dc = new DirClassifier(path);
        dc.explore();
    }
}