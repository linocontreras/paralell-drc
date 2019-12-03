all: Parser SequentialCompressor ThreadsCompressor FJCompressor CSequentialCompressor

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