
RUBBER=rubber

.PHONY: all clean epstopdf
pdfs = 

all: epstopdf
	$(RUBBER) --pdf slides.tex

clean:
	$(RUBBER) --pdf --clean slides.tex
	rm -fv slides.*.vrb

epstopdf: $(pdfs)

$(pdfs): %.pdf: %.eps
	epstopdf --outfile=$@ $<
