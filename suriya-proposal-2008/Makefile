
RUBBER=rubber

.PHONY: all clean pdf dvi

all: pdf dvi

pdf:
	$(RUBBER) --pdf paper.tex

dvi:
	$(RUBBER)       paper.tex

clean:
	$(RUBBER) --pdf --clean paper.tex
	$(RUBBER)       --clean paper.tex
