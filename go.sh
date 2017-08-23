#!/bin/bash
set -e;
debug_suspend="n"
while getopts ":ds" opt
do
	case $opt in
		d)
			debugMode=true
			debug_suspend="y"
			echo "Halting execution to await debugger"
		;;
		s)
			skipMode=true
			echo "Option set to skip build."
		;;
		help|\?)
			echo -e "Usage: [-d]  [-s]"
			echo -e "\t d - debug. Starts the API in debug mode, which an IDE can attach to on port 8000"
			echo -e "\t s - skip.  Skips the build"
			exit 0
		;;
	esac
done

if [ -z "${skipMode}" ]
then
  mvn clean package
fi

debugParams="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=${debug_suspend},address=8000"
appConfig="--spring.config.location=src/main/resources/application.properties"
memConfig="-Xmx5g -Xms2g"
version=`head -10 pom.xml | tail | grep version | sed -n 's:.*<version>\(.*\)</version>.*:\1:p'`
jarFile="target/classification-service-${version}.jar"
set -x;
java -jar $memConfig $debugParams $jarFile $appConfig

