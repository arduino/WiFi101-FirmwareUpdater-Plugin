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
package cc.arduino.plugins.wifi101.flashers.java;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.*;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import cc.arduino.plugins.wifi101.certs.WiFi101Certificate;
import cc.arduino.plugins.wifi101.certs.WiFi101CertificateBundle;

import cc.arduino.plugins.wifi101.flashers.Flasher;
import javax.swing.JProgressBar;

public class NinaFlasher extends Flasher {

	public byte[] md5Checksum;

	public NinaFlasher(String _modulename, String _version, String _filename, boolean _certavail, ArrayList<String> _compatibleBoard) {
		super(_modulename, _version, _filename, _certavail, _compatibleBoard);
	}

	@Override
	public void updateFirmware(String port) throws Exception {
		FlasherSerialClient client = null;
		try {
			file = openFirmwareFile();
			progress(10, "Connecting to programmer...");
			client = new FlasherSerialClient();
			client.open(port);
			client.hello();
			int maxPayload = client.getMaximumPayload();

			byte[] fwData = this.getData();
			int size = fwData.length;
			int address = 0x00000000;
			int written = 0;

			progress(20, "Erasing target...");

			client.eraseFlash(address, size);

			while (written < size) {
				progress(20 + written * 40 / size, "Programming " + size + " bytes ...");
				int len = maxPayload;
				if (written + len > size) {
					len = size - written;
				}
				client.writeFlash(address, Arrays.copyOfRange(fwData, written, written + len));
				written += len;
				address += len;
			}

			progress(55, "Verifying...");

			md5Checksum = getMD5Checksum(fwData);

			address = 0x00000000;
			byte[] md5rec = client.md5Flash(address, size);
			progress(75, "Verifying...");
			if (Arrays.equals(md5rec, md5Checksum)) {
				progress(100, "Done!");
			} else {
				throw new Exception("Error validating flashed firmware");
			}
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	@Override
	public void uploadCertificates(String port, List<String> websites) throws Exception {
		FlasherSerialClient client = null;
		try {
			file = openFirmwareFile();
			progress(10, "Connecting to programmer...");
			client = new FlasherSerialClient();
			client.open(port);
			client.hello();
			int maxPayload = client.getMaximumPayload();
			int count = websites.size();
			String pem = "";

			for (String website : websites) {
				URL url;
				try {
					url = new URL(website);
				} catch (MalformedURLException e1) {
					url = new URL("https://" + website);
				}

				progress(30 + 20 * count / websites.size(), "Downloading certificate from " + website + "...");
				Certificate[] certificates = SSLCertDownloader.retrieveFromURL(url);

				// Pick the latest certificate (that should be the root cert)
				X509Certificate x509 = (X509Certificate) certificates[certificates.length - 1];
				pem = convertToPem(x509) + "\n" + pem;
			}

			byte[] pemArray = pem.getBytes();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			output.write(pemArray);
			while (output.size() % maxPayload != 0) {
				output.write(0);
			}
			byte[] fwData = output.toByteArray();

			int size = fwData.length;
			int address = 0x10000;
			int written = 0;

			if (size > 0x20000) {
				throw new Exception("Too many certificates!");
			}

			progress(20, "Erasing target...");

			client.eraseFlash(address, size);

			while (written < size) {
				progress(20 + written * 40 / size, "Programming " + size + " bytes ...");
				int len = maxPayload;
				if (written + len > size) {
					len = size - written;
				}
				client.writeFlash(address, Arrays.copyOfRange(fwData, written, written + len));
				written += len;
				address += len;
			}
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	protected static String convertToPem(X509Certificate cert) {
		Base64 encoder = new Base64(64, "\n".getBytes());
		String cert_begin = "-----BEGIN CERTIFICATE-----\n";
		String end_cert = "-----END CERTIFICATE-----";

		try {
			byte[] derCert = cert.getEncoded();
			String pemCertPre = new String(encoder.encode(derCert));
			String pemCert = cert_begin + pemCertPre + end_cert;
			return pemCert;
		} catch (Exception e) {
			// do nothing
			return "";
		}
	}

	public static byte[] getMD5Checksum(byte[] buffer) throws Exception{
		try {
			MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead = buffer.length;
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
			return complete.digest();
		} catch (Exception e) {
			throw new Exception("Error in MD5 checksum computation.");
		}
	}
}