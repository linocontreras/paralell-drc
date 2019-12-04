all: Parser SequentialCompressor ThreadsCompressor FJCompressor CSequentialCompressor TBBCompressor

Parser:
	javac -d ./bin ./src/parser/*

SequentialCompressor: Parser
	javac -d ./bin ./src/sequential/*

ThreadsCompressor: Parser
	javac -d ./bin ./src/threads/*

FJCompressor: Parser
	javac -d ./bin ./src/forkjoin/*

CSequentialCompressor: Parser
	gcc -o ./bin/csequential ./src/csequential/csequential.c -lm -ggdb -Wall

COpenMPCompressor: Parser
	gcc -o ./bin/copenmp ./src/copenmp/copenmp.c -lm -Wall -fopenmp

TBBCompressor: Parser
	gcc -o ./bin/tbbcompressor ./src/tbb/tbbcompressor.cpp -lm -Wall -ltbb