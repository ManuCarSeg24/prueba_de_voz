package es.iesjandula.prueba_de_voz;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.FileInputStream;
import java.io.InputStream;

public class ReconocimientoVoz {

    public static void main(String[] args) {
        try {
            Model model = new Model("modelos/vosk-es");

            InputStream ais = new FileInputStream("hola.wav");

            Recognizer recognizer = new Recognizer(model, 16000);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = ais.read(buffer)) >= 0) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    System.out.println("Texto parcial: " + recognizer.getResult());
                }
            }

            System.out.println("Texto final: " + recognizer.getFinalResult());

            ais.close();
            recognizer.close();
            model.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}