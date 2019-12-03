all: Parser SequentialCompressor ThreadsCompressor

Parser:
	javac -d ./bin ./src/parser/*

SequentialCompressor: Parser
	javac -d ./bin ./src/sequential/*

ThreadsCompressor: Parser
	javac -d ./bin ./src/threads/*