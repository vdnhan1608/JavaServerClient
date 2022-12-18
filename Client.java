import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    static int BYTE_LENGTH = 100;

    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 3300);

        if (s.isConnected())
            System.out.println("Connected");

        /* READ THE PATH TO FOLDER, SENT FROM SEVER */
        InputStream is = s.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String path = br.readLine();

        File file = new File(path);

        OutputStream os = s.getOutputStream();
        byte[] buf = new byte[BYTE_LENGTH];
        String msg = "";

        do {
            msg = "";
            String[] directories = file.list();

            /* SEND SUBFOLDER NAME TO SERVER */
            for (int i = 0; i < directories.length; i++) {
                System.out.print(directories[i] + " ");
                String pathName = path + "\\" + directories[i];
                File newFile = new File(pathName);
                if (newFile.isDirectory() && newFile.exists()) // Check if 1 path name is directory or file
                {
                    msg += directories[i] + "|subfolder|";
                    // os.write(msg.getBytes(), 0,msg.length());
                    // os.flush();
                }

                else 
                if (newFile.isFile() && newFile.exists()) {
                    int sizeOfFile = 0;

                    /*READ THE SIZE OF THE FILE */
                    /* IF SIZE CHANGE THEN FILE IS MODIFIED */
                    FileInputStream fis = new FileInputStream(pathName);
                    int length = fis.read(buf);

                    while (length != -1){
                        sizeOfFile += length;
                        length = fis.read(buf);
                    }

                   msg += directories[i] + "|" + Integer.toString(sizeOfFile) + "|";
                    fis.close();
                }
            }
            System.out.println();
            msg += "Done\n";
            os.write(msg.getBytes(), 0,msg.length());
            os.flush();

            is.read(buf);
            System.out.println(buf);

        } while (true);

    }

}