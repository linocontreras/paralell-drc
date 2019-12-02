all: ExtractFrames SequentialCompressor

ExtractFrames:
	javac -d ./bin ./src/parser/*
SequentialCompressor: ExtractFrames
	javac -d ./bin ./src/sequential/*