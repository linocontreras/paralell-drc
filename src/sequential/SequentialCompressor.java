/**
 * Programación avanzada: Proyecto final
 * Fecha: 2019-12-03
 * Autor: A01700457 - Lino Ronaldo Contreras Gallegos
 */

package sequential;

import java.io.*;
import java.nio.*;

public class SequentialCompressor {
    double[] frames;
    double threshold;
    double ratio;
    double gain;

    public SequentialCompressor(double[] frames, double threshold, double ratio, double gain) {
        this.frames = frames;
        this.threshold = threshold;
        this.ratio = ratio;
        this.gain = gain;
    }

    public double[] compress() {
        double[] data = new double[this.frames.length];
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == 0)
                continue;
            int sign = frames[i] < 0 ? -1 : 1;
            frames[i] = frames[i] < 0 ? -frames[i] : frames[i];

            frames[i] += this.gain;
            if (frames[i] > threshold) {
                frames[i] = threshold + ((frames[i] - threshold) * (1 / ratio));
            }
            frames[i] = frames[i] <= 1 ? frames[i] : 1;
            frames[i] = frames[i] * sign;
        }

        return data;
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            PrintUsage();
            System.exit(1);
        }

        File inputFile = new File(args[0]);

        if (!inputFile.exists()) {
            System.err.println("El archivo especificado no existe.");
            System.exit(2);
        }

        double threshold = Double.parseDouble(args[1]);

        if (threshold < 0 || threshold > 1) {
            System.err.println("El threshold debe ser entre 0 y 1.");
            System.exit(3);
        }

        double ratio = Double.parseDouble(args[2]);

        if (ratio < 1) {
            System.err.println("El ratio debe ser a partir de 1.");
            System.exit(4);
        }

        double gain = Double.parseDouble(args[3]);

        if (gain < 0 || gain > 1) {
            System.err.println("La ganancia debe ser entre 0 y 1.");
            System.exit(5);
        }

        try {

            FileInputStream input = new FileInputStream(inputFile);
            double[] data = ByteBuffer.wrap(input.readAllBytes()).asDoubleBuffer().array();

            SequentialCompressor sc = new SequentialCompressor(data, threshold, ratio, gain);

            System.out.println("SequentialCompressor Starting...");
            long start = System.nanoTime();

            double[] compressed = sc.compress();

            System.out.println("Time elapsed: " + (System.nanoTime() - start / 1e6) + "ms");

            String outputFilePath = args[0].substring(0, args[0].lastIndexOf("."));
            FileOutputStream output = new FileOutputStream(new File(outputFilePath + ".cps"));

            ByteBuffer compressedData = ByteBuffer.allocate(compressed.length * Double.BYTES);

            compressedData.asDoubleBuffer().put(compressed);

            output.write(compressedData.array());
            output.close();


        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(100);
        }
    }

    private static void PrintUsage() {
        System.err.println("Usage: java sequential.SequentialCompressor file.frames threshold ratio gain");
    }
}