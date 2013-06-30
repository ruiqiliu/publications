
.PHONY: all clean pdf

all: pdf tags

pdf:
	rubber --pdf thesis.tex

tags: thesis.aux
	./aux2tags.py

clean:
	rubber --pdf --clean thesis.tex
