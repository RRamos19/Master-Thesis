@ECHO OFF
CD iselthesis

SET output=template.pdf
SET log_file=template.log

SET /p resposta="Atualizar a bibliografia ? (s/n): "

SET or_=false
IF EXIST %output% SET or_=true
IF EXIST %log_file% SET or_=true

IF "%or_%"=="true" (
	IF /I "%resposta%"=="s" (
		make clean
	) else (
		IF EXIST "%output%" DEL "%output%"
	)
)

make pdf

start "" "%output%"