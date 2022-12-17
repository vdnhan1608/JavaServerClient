import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

class ListenThread extends Thread {
    static int BYTE_LENGTH = 100;
    Socket s;
    HashMap<String,Integer>folders; // int is used to check if the folder checked or not: 0: Not checked, 1:
                                         // Checked, 2: Created
    HashMap<String, Integer> files = null;

    ListenThread(Socket s) {
        super();
        this.s = s;
    }

    public void run()
    {
        try {
            if (this.folders == null) this.folders = new HashMap<String, Integer>();
            if (this.files == null) this.files = new HashMap<String, Integer>();

            InputStream is = this.s.getInputStream();
            byte[] msg = new byte[BYTE_LENGTH];
            String line = "";
            do{
                is.read(msg);
                line = new String(msg);
                
                String[] parts = line.split("|") ;
                if (parts[1] == "folder")
                {
                    /*FOLDER PROCESS */
                    if (this.folders.size() == 0) this.folders.put(parts[0], 2);
                    else
                    {
                        if (this.folders.get(parts[0]) == null) 
                        {
                            System.out.println("a subfolder created");
                            this.folders.put(parts[0], 2);
                        }
                    
                        this.folders.remove(parts[0]);
                        this.folders.put(parts[0],1);
                    }
                }

                else
                {
                    /* FILE PROCESS */
                    int sizeOfFile = Integer.parseInt(parts[1]);
                    if (this.files.get(parts[0])== null)
                    {
                        System.out.println("a file created");
                        this.files.put(parts[0], sizeOfFile);
                    }

                    
                    if (this.files.get(parts[0]) != sizeOfFile) 
                    {
                        System.out.println("a file modified");
                        this.files.remove(parts[0]);
                        this.files.put(parts[0], sizeOfFile);
                    }
                }
                
                if (line == "Done") {
                    for (String folder: this.folders.keySet())
                        if (this.folders.get(folder)== 0) 
                        {
                            System.out.println("A folder deleted");
                            this.folders.remove(folder);
                        }
                }
            }
            while (true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}

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