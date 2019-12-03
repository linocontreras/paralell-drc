#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <sys/time.h>
#include <sys/types.h>

struct timeval startTime, stopTime;
int started = 0;

void start_timer() {
	started = 1;
	gettimeofday(&startTime, NULL);
}

double stop_timer() {
	long seconds, useconds;
	double duration = -1;

	if (started) {
		gettimeofday(&stopTime, NULL);
		seconds  = stopTime.tv_sec  - startTime.tv_sec;
		useconds = stopTime.tv_usec - startTime.tv_usec;
		duration = (seconds * 1000.0) + (useconds / 1000.0);
		started = 0;
	}
	return duration;
}

void compress(double *frames, int size, double threshold, double ratio, double gain) {
    int i;
    for (int i = 0; i < size; i++) {
        if (frames[i] == 0)
                continue;

        int sign = frames[i] < 0 ? -1 : 1;
        frames[i] = frames[i] < 0 ? -frames[i] : frames[i];

        frames[i] *= log10(10 + gain);
        if (frames[i] > threshold) {
            frames[i] = threshold + ((frames[i] - threshold) * (1 / ratio));
        }
        frames[i] = frames[i] <= 1 ? frames[i] : 1;
        frames[i] = frames[i] * sign;
        }
}

void printUsage(char *program) {
    printf("Usage: %s file.frames threshold ratio gain\n", program);
}

int main(int argc, char **argv) {
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

    double gain = strtod(argv[3], NULL);

    if (gain < 0) {
        fprintf(stderr, "La ganancia debe ser mayor o igual a 0.");
        return 5;
    }

    fseek(file, 0L, SEEK_END);
    long size = ftell(file) / sizeof(double);
    rewind(file);

    double *frames = malloc(size * sizeof *frames);

    fread(frames, size, sizeof *frames, file);

    start_timer();
    double ms;

    printf("Starting csequential...\n");
    compress(frames, size, threshold, ratio, gain);
    ms = stop_timer();

    printf("Elapsed time: %lf ms.\n", ms);

    free(frames);
    return 0;
}