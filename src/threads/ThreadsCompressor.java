/**
 * Programación avanzada: Proyecto final
 * Fecha: 2019-12-03
 * Autor: A01700457 - Lino Ronaldo Contreras Gallegos
 */

package threads;

import java.io.*;
import java.nio.*;

public class ThreadsCompressor implements Runnable {
    double[] frames;
    double threshold;
    double ratio;
    double gain;
    int nThreads;
    int threadId;

    public ThreadsCompressor(double[] frames, double threshold, double ratio, double gain, int nThreads, int threadId) {
        this.frames = frames;
        this.threshold = threshold;
        this.ratio = ratio;
        this.gain = gain;
        this.nThreads = nThreads;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        for (int i = threadId; i < frames.length; i += nThreads) {
            if (frames[i] == 0)
                continue;

            int sign = frames[i] < 0 ? -1 : 1;
            frames[i] = frames[i] < 0 ? -frames[i] : frames[i];

            frames[i] *= Math.log(10 + this.gain);
            if (frames[i] > threshold) {
                frames[i] = threshold + ((frames[i] - threshold) * (1 / ratio));
            }
            frames[i] = frames[i] <= 1 ? frames[i] : 1;
            frames[i] = frames[i] * sign;
        }
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

            Thread[] tcs = new Thread[cores];

            System.out.println("ThreadsCompressor Starting...");
            long start = System.nanoTime();

            for (int i = 0; i < cores; i++) {
                tcs[i] = new Thread(new ThreadsCompressor(data, threshold, ratio, gain, cores, i));
                tcs[i].start();
            }

            for (Thread t : tcs) {
                t.join();
            }

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
        System.err.println("Usage: java sequential.ThreadsCompressor file.frames threshold ratio gain");
    }
}