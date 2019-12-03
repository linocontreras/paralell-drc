/**
 * Programación avanzada: Proyecto final
 * Fecha: 2019-12-03
 * Autor: A01700457 - Lino Ronaldo Contreras Gallegos
 */

package parser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class RebuildFile {
    private WavFile wav;
    private WavFile newWavFile;

    public RebuildFile(String inputFile) throws WavFileException, IOException {
        this.wav = WavFile.openWavFile(new File(inputFile));
        File outputFile = new File(inputFile.substring(0, inputFile.lastIndexOf(".")) + "_compressed.wav");
        this.newWavFile = WavFile.newWavFile(outputFile, this.wav.getNumChannels(), this.wav.getNumFrames(), this.wav.getValidBits(), this.wav.getSampleRate());

    }

    public void rebuild(DoubleBuffer data) throws WavFileException, IOException {
        this.newWavFile.writeFrames(data, (int)wav.getNumFrames());
    }

    public void close() throws IOException
    {
        this.wav.close();
        this.newWavFile.close();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            PrintUsage();
            System.exit(1);
        }
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(args[0] + ".cps"));
            byte[] data = in.readAllBytes();
            in.close();

            ByteBuffer dataBytesBuffer = ByteBuffer.wrap(data);//.order(ByteOrder.LITTLE_ENDIAN);            
            
            RebuildFile rebuildFile = new RebuildFile(args[0]);
            rebuildFile.rebuild(dataBytesBuffer.asDoubleBuffer());
            rebuildFile.close();
        } catch(Exception ex) {
            System.err.println("Ha ocurrido un error al intentar reconstruir el archivo.");
            System.err.print(ex);
            ex.printStackTrace();
            System.exit(2);
        }
    }

    private static void PrintUsage() {
        System.out.println("Usage: java parser.RebuildFile audiofile.wav");
    }
}