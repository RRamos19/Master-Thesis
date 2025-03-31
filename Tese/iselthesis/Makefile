SILENT:=-interaction=batchmode
SYNCTEX:=1
MASTER = template
VERSION = v4.4
NAME    = Ricardo
SURNAME = Ramos

TIMES=$(shell printf "TIMINGS for last run:" ;\
	fgrep "TIME process" -A 1 *.log | while read a; do \
	  read b; read c; \
	  S=`echo $$a | cut -d ' '  -f3 | cut -d '=' -f 1`; \
	  T=`echo $$b | cut -d ':' -f 2 | cut -d ' ' -f 2`; \
	  printf "\n%12s = %7.2f s" "$$S" "$$T"; \
	done  | tr '\n ' '\1\2')


B:=${MASTER}
F:=-time -shell-escape -synctex=$(SYNCTEX) -output-format=pdf -file-line-error $(FLAGS)
T:=$(B).pdf
S:=$(B).tex
L:=latexmk $(F)
V:=open -a skim

DATE:=`date +%Y%m%d`
AUXFILES:=$(shell ls $(B)*.* | fgrep -v .tex | fgrep -v $(B).pdf | sed 's: :\\ :g' | sed 's:(:\\(:g' | sed 's:):\\):g')

.PHONY: pdf
pdf: $(S)
	$(L) -pdf -f $(SILENT) $(B)
	@echo $(TIMES)  | tr '\1@\2' '\n\t '

.PHONY: v view
v view: $(T)
	$(V) $(T)

.PHONY: vv verb verbose
vv verb verbose:
	$(L) -pdf $(B)
	@echo $(TIMES)  | tr '\1@\2' '\n\t '

.PHONY: $(VERSION)
$(VERSION):
	hash -r
	PATH="$(wildcard /usr/local/texlive/$@/bin/*-darwin/):$(PATH)" make $(filter-out $@,$(MAKECMDGOALS))

.PHONY: clean
clean:
	@$(L) -c $(B)
	@rm -f $(AUXFILES) "*(1)*"
	@find . -name .DS_Store -o -name '_minted*' | xargs rm -rf
	@rm -f $(B).pdf

.PHONY: times
times:
	@echo $(TIMES)  | tr '\1@\2' '\n\t '