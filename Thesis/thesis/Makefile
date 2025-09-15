FINAL_NAME:=TFM35_Ricardo_Ramos_46638.pdf
AUX_OUTPUT:=aux_files

MASTER_FILE:=main
MASTER_PDF:=$(MASTER_FILE).pdf
MASTER_TEX:=$(MASTER_FILE).tex
FLAGS:=-output-format=pdf -outdir=$(AUX_OUTPUT) -out2dir=./ -file-line-error -silent
BUILDER:=latexmk $(FLAGS)
GLOSSARY_BUILDER:=makeglossaries -d $(AUX_OUTPUT)

pdf: $(S)
	cd aux_files;rm -r *;cd ..;
	rm -f $(FINAL_NAME)
	$(BUILDER) $(MASTER_TEX)
	$(GLOSSARY_BUILDER) $(MASTER_FILE)
	$(BUILDER) $(MASTER_TEX)
	mv $(MASTER_PDF) $(FINAL_NAME)