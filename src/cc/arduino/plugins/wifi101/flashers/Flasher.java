/*
 * This file is part of WiFi101 Updater Arduino-IDE Plugin.
 * Copyright 2016 Arduino LLC (http://www.arduino.cc/)
 *
 * Arduino is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, you may use this file as part of a free software
 * library without restriction.  Specifically, if other files instantiate
 * templates or use macros or inline functions from this file, or you compile
 * this file and link it with other files to produce an executable, this
 * file does not by itself cause the resulting executable to be covered by
 * the GNU General Public License.  This exception does not however
 * invalidate any other reasons why the executable file might be covered by
 * the GNU General Public License.
 */
package cc.arduino.plugins.wifi101.flashers;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import javax.swing.JProgressBar;

import cc.arduino.plugins.wifi101.flashers.java.FlasherSerialClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import java.io.ByteArrayOutputStream;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import javax.swing.JProgressBar;

public class Flasher {

	public String modulename;
	public String version;
	public File file;
	public JProgressBar progressBar;
	public String name;
	public String filename;
	public List<String> compatibleBoard;
	public boolean certavail;

	public Flasher() {}

	public Flasher(String _modulename, String _version, String _filename, boolean _certavail, ArrayList<String> _compatibleBoard) {
		modulename = _modulename;
		compatibleBoard = new ArrayList<String>();
		version = _version;
		file = null;
		name = "NINA";
		certavail = _certavail;
		compatibleBoard.addAll(_compatibleBoard);
		filename = _filename;
	}

	public void progress(int progress, String text) {
		if (text.length() > 60) {
			text = text.substring(0, 60) + "...";
		}
		progressBar.setValue(progress);
		progressBar.setStringPainted(true);
		progressBar.setString(text);
	}

	public void testConnection(String port) throws Exception {
		FlasherSerialClient client = null;
		try {
			progress(50, "Testing programmer...");
			client = new FlasherSerialClient();
			client.open(port);
			client.hello();
			progress(100, "Done!");
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	public void updateFirmware(String port) throws Exception {
		// To be overridden
	}

	public void uploadCertificates(String port, List<String> websites) throws Exception {
		// To be overridden
	}

	public void setProgressBar(JProgressBar _progressBar) {
		progressBar = _progressBar;
	}

	public String getName() {
		return name;
	}

	public byte[] getData() throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			ByteArrayOutputStream res = new ByteArrayOutputStream();
			byte buff[] = new byte[4096];
			while (in.available() > 0) {
				int read = in.read(buff);
				if (read == -1) {
					break;
				}
				res.write(buff, 0, read);
			}
			return res.toByteArray();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public File getFile() {
		return file;
	}

	public void setFileName(String _filename) {
		filename = _filename;
	}

	public boolean isCompatible(String boardName) {
		for (String name : compatibleBoard) {
			if (name.equals(boardName)) {
				return true;
			}
		}
		return false;
	}

	public boolean certificatesAvailable() {
		return certavail;
	}

	public File openFirmwareFile() throws Exception {
		try {
			String jarPath = Flasher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File jarFolder = new File(jarPath).getParentFile();
			File fwfile = new File(jarFolder, filename);
			return fwfile;
		} catch (URISyntaxException e) {
			String message = "File not found ";
			throw new Exception(message.concat(filename));
		}
	}

	public String toString() {
		String names = modulename + " (" + version + ") (";
		for (String lname : compatibleBoard) {
			names = names.concat(lname).concat(", ");
		}
		names = names.substring(0, (names.length() - 2)).concat(")");
		names = StringUtils.abbreviate(names, 75);
		return names;
	}
}
