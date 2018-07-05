@ECHO OFF
setlocal
cd %~dp0
SET /A argsC = 0
FOR %%A IN (%*) DO Set /A argsC+=1

IF %argsC%==0 (
	java -jar simulator.jar -m=structured
)
IF NOT %argsC%==0 (
  IF NOT EXIST ".\logs\" MKDIR ".\logs\"
  FOR %%A IN (%*) DO (
    ECHO %%A
    java -jar simulator.jar -m=structured %%A > "logs\%%~nxA.log"
  )
)
endlocal
pause
