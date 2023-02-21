import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
//import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import com.github.sarxos.webcam.Webcam;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String path = "C:\\Users\\agent\\FRC 2023\\CheckInProgram\\src\\studentData.csv";
        ArrayList<String[]> data = retriveStudentData(path);
        JFrame frame = new JFrame("Webcam Display Example");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.close();
        frame.hide();
        while (true) {
            System.out.print("Would you like to sign in through\n1. Camera \n2. Manual \n3. Quit\n:");
            Scanner scanner = new Scanner(System.in);
            String option = scanner.nextLine();
            if (option.equals("1")) {
                webcam.open();
                frame.show();
                String studentId = readBarcode(webcam, frame); //retrieve student id
                webcam.close();
                frame.hide();
                String studentName = getName(studentId, data);
                Locale loc = new Locale("en", "US");
                DateFormat date = DateFormat.getTimeInstance(DateFormat.DEFAULT, loc);
                System.out.println(studentName + " checked in at " + date.format(new Date()));
            } else if (option.equals("2")) {
                System.out.print("Enter your id here: ");
                String studentId = scanner.next();
                String studentName = getName(studentId, data);
                Locale loc = new Locale("en", "US");
                DateFormat date = DateFormat.getTimeInstance(DateFormat.DEFAULT, loc);
                System.out.println(studentName + " checked in at " + date.format(new Date()));
            }
            else {
                System.exit(0);
            }
        }
    }
    public static String getName(String id, ArrayList<String[]> studentData) { //retrieve name given id
        for (int i = 0; i < studentData.size(); i++) {
            if (id.equals(studentData.get(i)[0])) {
                return studentData.get(i)[1];
            }
        }
        return "not a student";
    }
    public static String readBarcode(Webcam webcam, JFrame frame) {

        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        MultiFormatReader reader = new MultiFormatReader();
        while (true) {
            BufferedImage image = webcam.getImage(); // capture a frame from the webcam
//            System.out.println(image.getWidth());
            Result result = null;
            try {
                result = reader.decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))));
            } catch (NotFoundException e) {
//                System.out.println("nothing found");
            }
            // decode the QR code in the frame
            if (result != null) {
                return result.getText(); // return id
            }
        }
    }
    public static ArrayList<String[]> retriveStudentData(String path) {
        String line;
        String delimeter = ",";
        ArrayList<String[]> data = new ArrayList<String[]>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                data.add(line.split(delimeter));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private static class BufferedImageLuminanceSource extends LuminanceSource {

        private final BufferedImage image;

        BufferedImageLuminanceSource(BufferedImage image) {
            super(image.getWidth(), image.getHeight());

            this.image = image;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            int width = getWidth();
            if (row == null || row.length < width) {
                row = new byte[width];
            }

            int[] pixels = new int[width];
            image.getRGB(0, y, width, 1, pixels, 0, width);

            for (int i = 0; i < width; i++) {
                row[i] = (byte) ((pixels[i] & 0xff0000) >> 16);
            }

            return row;
        }

        @Override
        public byte[] getMatrix() {
            int width = getWidth();
            int height = getHeight();

            byte[] matrix = new byte[width * height];
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            for (int i = 0; i < width * height; i++) {
                matrix[i] = (byte) ((pixels[i] & 0xff0000) >> 16);
            }

            return matrix;
        }

    }
}