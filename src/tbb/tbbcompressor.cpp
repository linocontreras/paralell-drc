/* This code implements the known sort algorithm "Counting sort" */

#include <iostream>
#include <cstring>
#include <tbb/task_scheduler_init.h>
#include <tbb/parallel_for.h>
#include <tbb/blocked_range.h>
#include "cppheader.h"
#include <cmath>

using namespace std;
using namespace tbb;

#define N 10
#define GRAIN 200

class Compressor {
private:
	double *frames;
	int size;
    double threshold;
    double ratio;
    double gain;

public:
	Compressor(double *frames, int size, double threshold, double ratio, double gain) 
		: frames(frames), size(size), threshold(threshold), ratio(ratio), gain(gain) {}

	void operator() 
		(const blocked_range<int> &r) const {
        int sign;
		for (int i = r.begin(); i != r.end(); i++) {
            if (frames[i] == 0)
                continue;

            sign = frames[i] < 0 ? -1 : 1;
            frames[i] = frames[i] < 0 ? -frames[i] : frames[i];

            frames[i] *= log10(10 + gain);
            if (frames[i] > threshold) {
                frames[i] = threshold + ((frames[i] - threshold) * (1 / ratio));
            }
            frames[i] = frames[i] <= 1 ? frames[i] : 1;
            frames[i] = frames[i] * sign;
		}
	}
};

void printUsage(char *program) {
    printf("Usage: %s file.frames threshold ratio gain\n", program);
}

typedef union
{
    double d;
    unsigned char s[8];
} Union_t;


// reverse little to big endian or vice versa as per requirement
double reverse_endian(double in)
{
    int i, j;
    unsigned char t;
    Union_t val;

    val.d = in;
    // swap MSB with LSB etc.
    for(i=0, j=7; i < j; i++, j--)
    {
        t = val.s[i];
        val.s[i] = val.s[j];
        val.s[j] = t;
    }
    return val.d;
}

int main(int argc, char* argv[]) {
    if (argc != 5) {
        printUsage(argv[0]);
        return 1;
    }
    FILE *file = fopen(argv[1], "rb");

    if (file == NULL) {
        perror(argv[0]);
        return 2;
    }

    double threshold = strtod(argv[2], NULL);

    if (threshold < 0 || threshold > 1) {
        fprintf(stderr, "El threshold debe ser entre 0 y 1.");
        return 3;
    }

    double ratio = strtod(argv[3], NULL);

    if (ratio < 1) {
        fprintf(stderr, "El ratio debe ser a partir de 1.");
        return 4;
    }

    double gain = strtod(argv[4], NULL);

    if (gain < 0) {
        fprintf(stderr, "La ganancia debe ser mayor o igual a 0.");
        return 5;
    }

    fseek(file, 0L, SEEK_END);
    int size = ftell(file) / sizeof (double);
    rewind(file);

    double *frames = new double[size * sizeof *frames];

    fread(frames, sizeof *frames, size, file);

    int i;
    for (i = 0;i < size; i++) {
        frames[i] = reverse_endian(frames[i]);
    }

    
	Timer t;
	double ms = 0;
	
    printf("Starting TBB...\n");
	for (int i = 0; i < N; i++) {
		t.start();

		parallel_for(
			blocked_range<int>(0, size, GRAIN),
			Compressor(frames, size, threshold, ratio, gain));

		ms += t.stop();
	}
    printf("Elapsed time: %lf ms.\n", (ms /N));

    char newpath[FILENAME_MAX];

    int lastindex = strrchr(argv[1], '.') - argv[1];

    strcpy(newpath, argv[1]);
    strcpy(newpath + lastindex, ".cps");

    FILE *newfile = fopen(newpath, "wb");

    for (i = 0;i < size; i++) {
        frames[i] = reverse_endian(frames[i]);
    }
    fwrite(frames, sizeof *frames, size, newfile);

    delete[] frames;
    fclose(file);
    fclose(newfile);
    return 0;
}

