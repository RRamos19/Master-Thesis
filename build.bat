@ECHO OFF
cd iselthesis

set file="template.pdf"

IF EXIST %file% (
	make clean
)

make pdf

start "" %file%