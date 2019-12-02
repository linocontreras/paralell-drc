
/**
 * Programación avanzada: Proyecto final
 * Fecha: 2019-12-03
 * Autor: A01700457 - Lino Ronaldo Contreras Gallegos
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class ExtractFrames {
    public static void main(String[] args) {
        if (args.length != 1) {
            PrintUsage();
            System.exit(1);
        }

        WavFile wavFile = WavFile.openWavFile(new File(args[0]));

        DataOutputStream out = new DataOutputStream(new FileOutputStream(args[0] + ".frames"));
        ByteBuffer data = ByteBuffer.allocate((int) wavFile.getNumFrames() * Double.BYTES);
        
        wavFile.readFrames(data.asDoubleBuffer().array(), (int)wavFile.getNumFrames());
        
        out.write(data.array());

        out.close();
        wavFile.close();
    }

    private static void PrintUsage() {
        System.out.println("Usage: java ExtractFrames audiofile.wav");
    }
}