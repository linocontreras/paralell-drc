/**
 * Programación avanzada: Proyecto final
 * Fecha: 2019-12-03
 * Autor: A01700457 - Lino Ronaldo Contreras Gallegos
 */
package parser;

import java.io.*;
import java.nio.ByteBuffer;

public class ExtractFrames {
    private WavFile wav;

    public ExtractFrames(String inputFile) throws WavFileException, IOException {
        this.wav = WavFile.openWavFile(new File(inputFile));
    }

    public ByteBuffer extract() throws WavFileException, IOException {
        ByteBuffer data = ByteBuffer.allocate((int) wav.getNumFrames() * wav.getNumChannels() * Double.BYTES);
        this.wav.readFrames(data.asDoubleBuffer(), (int)wav.getNumFrames() * wav.getNumChannels());
        return data;
    }

    public void close() throws IOException
    {
        this.wav.close();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            PrintUsage();
            System.exit(1);
        }
        try {
            ExtractFrames extractFrames = new ExtractFrames(args[0]);
            ByteBuffer data = extractFrames.extract();
            extractFrames.close();
    
            DataOutputStream out = new DataOutputStream(new FileOutputStream(args[0] + ".frames"));
            out.write(data.array());
            out.close();   
        } catch(Exception ex) {
            System.err.println("Ha ocurrido un error al intentar extraer los frames.");
            System.err.print(ex);
            ex.printStackTrace();
            System.exit(2);
        }
    }

    private static void PrintUsage() {
        System.out.println("Usage: java parser.ExtractFrames audiofile.wav");
    }
}