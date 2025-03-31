@ECHO OFF
CD iselthesis

SET output=template.pdf
SET final_output=RDT_TFM35_Ricardo_Ramos_46638.pdf
SET log_file=template.log

SET /p resposta="Atualizar a bibliografia ? (s/n): "

SET or_=false
IF EXIST %final_output% SET or_=true
IF EXIST %log_file% SET or_=true

IF "%or_%"=="true" (
	IF /I "%resposta%"=="s" (
		make clean
	) else (
		IF EXIST "%final_output%" DEL "%final_output%"
	)
)

make pdf

IF EXIST "%output%" REN "%output%" "%final_output%"

IF EXIST "%final_output%" START "" "%final_output%"