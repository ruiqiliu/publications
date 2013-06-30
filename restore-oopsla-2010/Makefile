
# This Makefile uses rubber to compile .tex files. rubber is a wrapper
# around latex and pdflatex.
#
# From rubber's homepage (http://www.pps.jussieu.fr/~beffara/soft/rubber/)
#
#   Rubber is a program whose purpose is to handle all tasks related to the
#   compilation of LaTeX documents. This includes compiling the document
#   itself, of course, enough times so that all references are defined, and
#   running BibTeX to manage bibliographic references. Automatic execution
#   of dvips to produce PostScript documents is also included, as well as
#   usage of pdfLaTeX to produce PDF documents. 
#
# On UTCS machines, rubber is available as /lusr/bin/rubber.

# RUBBER = /lusr/bin/rubber
# RUBBER = rubber

PAPER = paper-pldi12
BIB = biblio

-include .dep

.PHONY: all clean

all:	$(PAPER).pdf

ifdef RUBBER
$(PAPER).pdf:
	$(RUBBER) --pdf $(PAPER).tex
else
$(PAPER).pdf: $(PAPER).tex experiments.tex $(BIB).bib
	pdflatex $(PAPER)
	bibtex $(PAPER)
	pdflatex $(PAPER)
	pdflatex $(PAPER)
endif

ifdef RUBBER
clean:
	$(RUBBER) --clean --pdf $(PAPER).tex
else
clean:
	$(RM) $(PAPER).log $(PAPER).aux $(PAPER).bbl $(PAPER).blg $(PAPER).pdf .dep
endif

.dep:
	@(echo "RUBBER = `which rubber`" > .dep; \
	if [ $$? != 0 ]; then \
	echo "RUBBER = " > .dep; \
	fi)


