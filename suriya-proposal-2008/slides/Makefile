
RUBBER=rubber

.PHONY: all clean epstopdf
pdfs = jvolve/vm-datastructures-after-dsu.pdf jvolve/vm-datastructures.pdf theme/ut-seal.pdf

all: epstopdf
	$(RUBBER) --pdf slides.tex

clean:
	$(RUBBER) --pdf --clean slides.tex

epstopdf: $(pdfs)

$(pdfs): %.pdf: %.eps
	epstopdf --outfile=$@ $<
