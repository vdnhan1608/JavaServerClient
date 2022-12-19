import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

/*LISTEN THREAD FOR RECEIVING INFORMATION FROM CLIENT */
class ListenThread extends Thread {
    static int BYTE_LENGTH = 100;
    static Boolean isInitial = true;
    Socket s;
    HashMap<String, Integer> folders; // int is used to check if the folder checked or not: 0: Not checked, 1:
                                      // Checked, 2: Created
    HashMap<String, HashMap<Integer, Integer>> files = null; // 1st Integer is the size of file
                                                             // 2nd Integer is (0: Not checked, 1: Checked, 2: Created)

    ListenThread(Socket s) {
        super();
        this.s = s;
    }

    public void run() {
        try {
            if (this.folders == null)
                this.folders = new HashMap<String, Integer>();
            if (this.files == null)
                this.files = new HashMap<String, HashMap<Integer, Integer>>();

            InputStream is = this.s.getInputStream();
            OutputStream os = this.s.getOutputStream();
            byte[] msg = new byte[BYTE_LENGTH];
            String line = "";
            String regex = "\\d+";
            do {
                line = "";
                while (line.contains("Done") == false) {
                    is.read(msg, 0, BYTE_LENGTH);
                    line += new String(msg);
                    String[] parts = line.split("\n");
                    line = parts[0];
                }
                // System.out.println(line);

                String[] parts = line.split(Pattern.quote("|"));
                if (isInitial == false) {
                    for (int i = 0; i < parts.length; i++) {
                        if (i % 2 == 0 && i + 1 < parts.length && parts[i + 1].equals("subfolder")) {
                            /* FOLDER PROCESS */

                            if (this.folders.get(parts[i]) == null) {
                                System.out.println("a subfolder created");
                                this.folders.put(parts[i], 2);
                            } else {
                                this.folders.remove(parts[i]);
                                this.folders.put(parts[i], 1);
                            }

                        }

                        else if (i % 2 == 0 && i + 1 < parts.length && parts[i + 1].matches(regex)) {
                            /* FILE PROCESS */
                            int sizeOfFile = Integer.parseInt(parts[i + 1]);
                            if (this.files.get(parts[i]) == null) {
                                System.out.println("a file created");
                                HashMap<Integer, Integer> fileInfo = new HashMap<Integer, Integer>();
                                fileInfo.put(sizeOfFile, 2);
                                this.files.put(parts[i], fileInfo);
                            }

                            else {
                                int oldSize = Integer
                                        .parseInt(this.files.get(parts[i]).keySet().toArray()[0].toString());
                                HashMap<Integer, Integer> oldInfo = this.files.get(parts[i]);
                                if (oldSize != sizeOfFile) {
                                    System.out.println("a file modified");
                                    // HashMap<Integer, Integer> fileInfo = new HashMap<Integer, Integer>();
                                    // fileInfo.put(sizeOfFile, 1);
                                    // this.files.replace(parts[i], oldInfo, fileInfo);
                                }

                                HashMap<Integer, Integer> fileInfo = new HashMap<Integer, Integer>();
                                fileInfo.put(sizeOfFile, 1);
                                this.files.replace(parts[i], oldInfo, fileInfo);

                            }

                        }

                        else if (i == parts.length - 1) {
                            /* Check folder deleted */
                            ArrayList<String> deletedFolders = new ArrayList<String>();
                            for (String folder : this.folders.keySet()) {
                                if (this.folders.get(folder) == 0) {
                                    System.out.println("A subfolder deleted");
                                    deletedFolders.add(folder);
                                } else if (this.folders.get(folder) == 1)
                                    this.folders.replace(folder, 1, 0);
                                else if (this.folders.get(folder) == 2)
                                    this.folders.replace(folder, 2, 0);
                            }

                            while (deletedFolders.size() > 0) {
                                String folder = deletedFolders.get(deletedFolders.size() - 1);
                                this.folders.remove(folder);
                                deletedFolders.remove(deletedFolders.size() - 1);
                            }

                            // /* Set all folder to not checked */
                            // for (String folder : this.folders.keySet())
                            // {
                            // if (this.folders.get(folder) == 1) this.folders.replace(folder, 1, 0);
                            // else
                            // if (this.folders.get(folder)== 2) this.folders.replace(folder, 2, 0);
                            // }

                            ArrayList<String> deletedFiles = new ArrayList<String>();
                            /* Check file deleted && set to not checked */
                            for (String file : this.files.keySet()) {
                                HashMap<Integer, Integer> fileInfo = this.files.get(file);

                                if (fileInfo.keySet().size() == 0)
                                    System.out.println(fileInfo);
                                int sizeOfFile = Integer.parseInt(fileInfo.keySet().toArray()[0].toString());

                                if (fileInfo.get(sizeOfFile) == 0) {
                                    System.out.println("A file deleted");
                                    deletedFiles.add(file);
                                } else if (fileInfo.get(sizeOfFile) == 1 || fileInfo.get(sizeOfFile) == 2) {

                                    HashMap<Integer, Integer> newFileInfo = new HashMap<Integer, Integer>();
                                    newFileInfo.put(sizeOfFile, 0);
                                    this.files.replace(file, fileInfo, newFileInfo);
                                }

                            }

                            while (deletedFiles.size() > 0) {
                                String file = deletedFiles.get(deletedFiles.size() - 1);
                                this.files.remove(file);
                                deletedFiles.remove(deletedFiles.size() - 1);
                            }

                            /* Set all file to not checked */

                        }
                    }

                }

                else {
                    /* FIRST LET SERVER KNOW WHAT FOLDER STRUCTURE IS LIKE IN CLIENT */
                    for (int i = 0; i < parts.length; i++) {
                        if (i % 2 == 0 && i + 1 < parts.length && parts[i + 1].equals("subfolder")) {
                            this.folders.put(parts[i], 1);
                        } else if (i % 2 == 0 && i + 1 < parts.length && parts[i + 1].matches(regex)) {
                            int size = Integer.parseInt(parts[i + 1]);
                            HashMap<Integer, Integer> fileInfo = new HashMap<Integer, Integer>();
                            fileInfo.put(size, 1);
                            this.files.put(parts[i], fileInfo);
                        }

                        else if (i == parts.length - 1) {
                            // System.out.println(parts[i]);
                            isInitial = false;
                        }
                    }

                    for (String folder : this.folders.keySet())
                        System.out.print(folder + " ");
                    for (String file : this.files.keySet())
                        System.out.print(file + " ");
                }

                os.write("OK\n".getBytes());
                os.flush();
            } while (true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}

/* SERVER CLASS */
public class Server {
    static int BYTE_LENGTH = 100;

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(3300);

        do {
            System.out.println("Listen....");
            Socket s = ss.accept();

            System.out.println("Connected to client");

            String path = "";
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter path: ");
            path = scanner.nextLine();

            OutputStream os = s.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

            bw.write(path);
            bw.newLine();
            bw.flush();

            ListenThread lt = new ListenThread(s);
            lt.start();
        } while (true);

    }
}