package es.iesjandula.prueba_de_voz;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

public class ReconocimientoVoz {

	public static void main(String[] args) {
	    try {
	        Model model = new Model("modelos/vosk-es");

	        AudioFormat format = new AudioFormat(48000.0f, 16, 2, true, false);
	        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
	        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);

	        microphone.open(format);
	        microphone.start();

	        Recognizer recognizer = new Recognizer(model, 16000);

	        System.out.println("🤖 Jarvis en reposo...");

	        byte[] buffer = new byte[4096];

	        while (true) {

	            int bytesRead = microphone.read(buffer, 0, buffer.length);
	            byte[] converted = convertStereo48kToMono16k(buffer, bytesRead);

	            if (recognizer.acceptWaveForm(converted, converted.length)) {

	                String resultado = recognizer.getResult().toLowerCase();

	                // 🔊 ACTIVACIÓN POR NOMBRE
	                if (resultado.contains("jarvis")) {
	                    System.out.println("🟢 Jarvis activado");

	                    microphone.stop();          // 🛑 Deja de escuchar
	                    hablar("Hola Manuel, en que puedo ayudarte...");           // 🗣️ Jarvis habla
	                    microphone.start();         // 🎧 Vuelve a escuchar JUSTO después
	                    Thread.sleep(300); // ⏳ pequeño margen para que empieces a hablar

	                    String comando = escucharComando(microphone, model);
	                    procesarComando(comando);

	                    System.out.println("🤖 Jarvis en reposo...");
	                }
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


    // 🎧 Convierte de 48kHz estéreo a 16kHz mono
    private static byte[] convertStereo48kToMono16k(byte[] input, int length) {
        int inputFrameSize = 4;
        int outputFrameSize = 2;

        int inputFrames = length / inputFrameSize;
        int outputFrames = inputFrames / 3;

        byte[] output = new byte[outputFrames * outputFrameSize];

        int outIndex = 0;

        for (int i = 0; i < outputFrames; i++) {
            int inIndex = i * 3 * inputFrameSize;
            output[outIndex++] = input[inIndex];
            output[outIndex++] = input[inIndex + 1];
        }

        return output;
    }
    
    private static String escucharComando(TargetDataLine mic, Model model) throws Exception {

        Recognizer rec = new Recognizer(model, 16000);
        byte[] buffer = new byte[4096];

        long ultimoSonido = System.currentTimeMillis();
        final long SILENCIO_LIMITE = 3000; // ⏳ más tiempo para terminar de hablar
        final long GRACIA_INICIAL = 1200;  // ⏳ tiempo donde NO contamos silencio al inicio

        System.out.println("🎧 Escuchando comando...");

        long inicioEscucha = System.currentTimeMillis();

        while (true) {
            int bytesRead = mic.read(buffer, 0, buffer.length);
            byte[] converted = convertStereo48kToMono16k(buffer, bytesRead);

            if (rec.acceptWaveForm(converted, converted.length)) {
                ultimoSonido = System.currentTimeMillis();
            }

            long ahora = System.currentTimeMillis();

            // Durante el primer segundo no se corta por silencio
            if (ahora - inicioEscucha < GRACIA_INICIAL) {
                continue;
            }

            if (ahora - ultimoSonido > SILENCIO_LIMITE) {
                break;
            }
        }

        String finalText = rec.getFinalResult().toLowerCase();
        System.out.println("🧠 Comando detectado: " + finalText);
        rec.close();

        return finalText;
    }
    
    private static void procesarComando(String resultado) {

        if (resultado.contains("enciende la luz")) {
            System.out.println("💡 LUZ ENCENDIDA");
            hablar("Luz encendida");
        }
        else if (resultado.contains("apaga la luz")) {
            System.out.println("🌑 LUZ APAGADA");
            hablar("Luz apagada");
        }
        else if (resultado.contains("salir")) {
            System.out.println("🛑 Cerrando Jarvis");
            hablar("Hasta luego");
            System.exit(0);
        }
        else {
            System.out.println("🤔 No entendí el comando");
            hablar("No entendí el comando");
        }
    }

    
    // 🔊 Jarvis habla
    private static void hablar(String texto) {
        try {
            String comando = "PowerShell -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.SelectVoice('Microsoft Pablo Desktop'); " +
                    "$speak.Speak('" + texto + "');\"";

            Process proceso = Runtime.getRuntime().exec(comando);
            proceso.waitFor(); // 🔥 Espera a que termine de hablar

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}