@echo off
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%.mvn\wrapper
"%JAVA_HOME%\bin\java" -cp "%MAVEN_HOME%\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal
