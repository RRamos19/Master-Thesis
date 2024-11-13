@ECHO OFF
cd iselthesis

set file="template.pdf"

set /p resposta="Deseja atualizar a bibliografia ? (s/n): "

IF EXIST %file% (
	IF /I "%resposta%"=="s" (
		make clean
	) else (
		del %file%
	)
)

make pdf

start "" %file%