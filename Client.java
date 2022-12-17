import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    static int BYTE_LENGTH = 100;
    public static void main(String[] args) throws  IOException {
    Socket s = new Socket("localhost", 3300);
    
    if (s.isConnected()) System.out.println("Connected");
    
    /* READ THE PATH TO FOLDER, SENT FROM SEVER */
    InputStream is = s.getInputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String path = br.readLine();
    System.out.println(path);

    File file = new File(path);
    if (file.isDirectory()) System.out.println("Directory");
    else System.out.println("File");

    OutputStream os = s.getOutputStream();
    byte[] msg = new byte[BYTE_LENGTH];


    do {
        String[] directories = file.list();

        /*SEND SUBFOLDER NAME TO SERVER */
        for (int i = 0 ; i < directories.length; i++)
        {
            String pathName = path + "\\" + directories[i];
            if (new File(pathName).isDirectory()) os.write((directories[i] + "|subfoler").getBytes());
            else
            {
                int sizeOfFile = 0;
                // os.write((directories[i] + "|file").getBytes());
                FileInputStream fis = new FileInputStream(pathName);
                int length = fis.read(msg);
                sizeOfFile += length;
                if (length < BYTE_LENGTH) os.write((directories[i] + Integer.toString(sizeOfFile)).getBytes());
                else
                while (length >= BYTE_LENGTH){
                    sizeOfFile += length;
                    length = fis.read(msg);

                }
                os.write((directories[i] + Integer.toString(sizeOfFile)).getBytes());
                fis.close();
            }
        }

        os.write("Done".getBytes());

    }
    while (true);
    


}

    

}