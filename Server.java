import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
    HashMap<String, Integer> files = null;

    ListenThread(Socket s) {
        super();
        this.s = s;
    }

    public void run() {
        try {
            if (this.folders == null)
                this.folders = new HashMap<String, Integer>();
            if (this.files == null)
                this.files = new HashMap<String, Integer>();

            InputStream is = this.s.getInputStream();
            OutputStream os = this.s.getOutputStream();
            byte[] msg = new byte[BYTE_LENGTH];
            String line = "";
            String regex = "\\d+";
            do {
                line = "";
                while (line.contains("Done") == false)
                {
                    is.read(msg,0, BYTE_LENGTH );
                    line += new String(msg);
                    String [] parts = line.split("\n");
                    line = parts[0];
                }
                // System.out.println(line);

                String[] parts = line.split(Pattern.quote("|"));
                if (isInitial == false) {
                    for (int i = 0; i < parts.length; i++)
                    {
                        if (i % 2 == 0 && i + 1 < parts.length && parts[i + 1].equals("subfolder")) {
                            /* FOLDER PROCESS */

                            if (this.folders.get(parts[i]) == null) {
                                System.out.println("a subfolder created");
                                this.folders.put(parts[i], 2);
                            }
                            else {
                                this.folders.remove(parts[i]);
                                this.folders.put(parts[i], 1);
                            }


                        }

                        else if (i % 2 == 0 && i + 1 < parts.length && parts[i + 1].matches(regex)) {
                            /* FILE PROCESS */
                            int sizeOfFile = Integer.parseInt(parts[i + 1]);
                            if (this.files.get(parts[i]) == null) {
                                System.out.println("a file created");
                                this.files.put(parts[i], sizeOfFile);
                            }

                            if (this.files.get(parts[i]) != sizeOfFile) {
                                System.out.println("a file modified");
                                this.files.remove(parts[i]);
                                this.files.put(parts[i], sizeOfFile);
                            }
                        }

                        else if (i == parts.length - 1) {

                            for (String folder : this.folders.keySet())
                                if (this.folders.get(folder) == 0) {
                                    System.out.println("A folder deleted");
                                    this.folders.remove(folder);
                                }

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
                            this.files.put(parts[i], size);
                        }

                        else if (i == parts.length - 1) {
                            // System.out.println(parts[i]);
                            isInitial = false;
                        }
                    }

                    for (String folder: this.folders.keySet())
                    System.out.print(folder + " ");
                    for (String file: this.files.keySet())
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