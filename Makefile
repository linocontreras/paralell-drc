all: Parser SequentialCompressor

Parser:
	javac -d ./bin ./src/parser/*
SequentialCompressor: Parser
	javac -d ./bin ./src/sequential/*