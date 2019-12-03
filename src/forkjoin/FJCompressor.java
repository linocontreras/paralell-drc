/**
 * Programación avanzada: Proyecto final
 * Fecha: 2019-12-03
 * Autor: A01700457 - Lino Ronaldo Contreras Gallegos
 */

package forkjoin;

import java.io.*;
import java.nio.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class FJCompressor extends RecursiveAction {
    private static final long serialVersionUID = -6712683024124632801L;
    private static final int th = 200;

    double[] frames;
    double threshold;
    double ratio;
    double gain;
    int start;
    int end;

    public FJCompressor(double[] frames, double threshold, double ratio, double gain, int start, int end) {
        this.frames = frames;
        this.threshold = threshold;
        this.ratio = ratio;
        this.gain = gain;
        this.start = start;
        this.end = end;
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

        if (gain < 0) {
            System.err.println("La ganancia debe ser mayor o igual a 0.");
            System.exit(5);
        }

        try {
            FileInputStream input = new FileInputStream(inputFile);
            byte[] dataBytes = input.readAllBytes();
            ByteBuffer dataBytesBuffer = ByteBuffer.wrap(dataBytes);// .order(ByteOrder.BIG_ENDIAN);
            DoubleBuffer dataBuffer = DoubleBuffer.allocate(dataBytes.length / Double.BYTES);
            double[] data = dataBuffer.put(dataBytesBuffer.asDoubleBuffer()).array();
            input.close();

            int cores = Runtime.getRuntime().availableProcessors();

            ForkJoinPool pool = new ForkJoinPool(cores);

            System.out.println("FJCompressor Starting...");
            long start = System.nanoTime();

            pool.invoke(new FJCompressor(data, threshold, ratio, gain, 0, data.length));

            System.out.println("Time elapsed: " + ((System.nanoTime() - start) / 1000000) + "ms");

            String outputFilePath = args[0].substring(0, args[0].lastIndexOf("."));
            FileOutputStream output = new FileOutputStream(new File(outputFilePath + ".cps"));

            ByteBuffer compressedData = ByteBuffer.allocate(data.length * Double.BYTES);

            compressedData.asDoubleBuffer().put(data);

            output.write(compressedData.array());
            output.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(100);
        }
    }

    private static void PrintUsage() {
        System.err.println("Usage: java forkjoin.FJCompressor file.frames threshold ratio gain");
    }

    @Override
    protected void compute() {
        if (end - start <= FJCompressor.th) {
            for (int i = start; i < end; i++) {
                if (frames[i] == 0)
                    continue;
    
                int sign = frames[i] < 0 ? -1 : 1;
                frames[i] = frames[i] < 0 ? -frames[i] : frames[i];
    
                frames[i] *= Math.log10(10 + this.gain);
                if (frames[i] > threshold) {
                    frames[i] = threshold + ((frames[i] - threshold) * (1 / ratio));
                }
                frames[i] = frames[i] <= 1 ? frames[i] : 1;
                frames[i] = frames[i] * sign;
            }
        }
        else {
            int mid = start + (end - start) / 2;
            FJCompressor a = new FJCompressor(frames, threshold, ratio, gain, start, mid);
            FJCompressor b = new FJCompressor(frames, threshold, ratio, gain, mid, end);

            invokeAll(a, b);
        }
    }
}