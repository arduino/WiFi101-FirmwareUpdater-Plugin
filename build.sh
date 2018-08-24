#!/bin/bash

# This file is part of WiFi101 Updater Arduino-IDE Plugin.
# Copyright 2016 Arduino LLC (http://www.arduino.cc/)
#
# Arduino is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
# As a special exception, you may use this file as part of a free software
# library without restriction.  Specifically, if other files instantiate
# templates or use macros or inline functions from this file, or you compile
# this file and link it with other files to produce an executable, this
# file does not by itself cause the resulting executable to be covered by
# the GNU General Public License.  This exception does not however
# invalidate any other reasons why the executable file might be covered by
# the GNU General Public License.

REV=0.9.2
ZIP_FILENAME=WiFi101-Updater-ArduinoIDE-Plugin-$REV
REQUIRED_JARS="pde.jar arduino-core.jar jssc-2.8.0-arduino2.jar bcpg-jdk15on-152.jar bcprov-jdk15on-152.jar commons-lang3-3.3.2.jar"

# Check existence of the IDE folder
if [[ -z "$IDE_FOLDER" ]]; then
	echo ""
	echo "Please set variable IDE_FOLDER to the path of the installed Arduino IDE"
	echo "For example:"
	echo ""
	echo "IDE_FOLDER=/home/user/ArduinoIDE/ ./build.sh"
	echo ""
	exit 1
fi

# Check needed libraries
CLASSPATH="src"
for JAR in $REQUIRED_JARS; do
	case "$OSTYPE" in
		darwin*)  JARFILE="$IDE_FOLDER/Java/$JAR" ;;
		*)        JARFILE="$IDE_FOLDER/lib/$JAR" ;;
	esac
	if [[ -z "$JARFILE" ]]; then
		echo "Could not find $JARFILE library in you IDE folder."
		exit 1
	fi
	CLASSPATH="$CLASSPATH:$JARFILE"
	echo $JAR
done

# Create staging folder
OUTPUT_FOLDER=`pwd`/WiFi101/tool
mkdir -p $OUTPUT_FOLDER

# Build java plugin
mkdir -p build
javac -target 1.8 -cp "$CLASSPATH" -d build src/cc/arduino/plugins/wifi101/WiFi101.java
cd build
zip -r $OUTPUT_FOLDER/WiFi101.jar *
cd ..
rm -r build

# Copy resources
cp -rv firmwares $OUTPUT_FOLDER

# Create distribution .zip
mkdir -p dist
zip -r dist/$ZIP_FILENAME.zip WiFi101/

# Cleanup
rm -r WiFi101

# Install in current IDE
case "$OSTYPE" in
	darwin*)  unzip -o dist/$ZIP_FILENAME.zip -d $IDE_FOLDER/Java/tools ;;
	*)        unzip -o dist/$ZIP_FILENAME.zip -d $IDE_FOLDER/tools ;;
esac
