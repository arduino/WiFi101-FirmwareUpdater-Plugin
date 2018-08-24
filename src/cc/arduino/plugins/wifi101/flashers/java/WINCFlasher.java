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
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;

import cc.arduino.plugins.wifi101.certs.WiFi101Certificate;
import cc.arduino.plugins.wifi101.certs.WiFi101CertificateBundle;
import cc.arduino.plugins.wifi101.flashers.Flasher;
import javax.swing.JProgressBar;

public class WINCFlasher extends Flasher {

	public WINCFlasher(String _modulename, String _version, String _filename, boolean _certavail, ArrayList<String> _compatibleBoard) {
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
			int readed = 0;
			address = 0x00000000;
			while (readed < size) {
				progress(60 + readed * 40 / size, "Verifying...");
				int len = maxPayload;
				if (readed + len > size) {
					len = size - readed;
				}
				byte[] data = client.readFlash(address, len);
				if (!Arrays.equals(data, Arrays.copyOfRange(fwData, readed, readed + len))) {
					throw new Exception("Error during verify at address " + address);
				}
				readed += len;
				address += len;
			}
			progress(100, "Done!");
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
			progress(10, "Connecting to programmer...");
			client = new FlasherSerialClient();
			client.open(port);
			client.hello();
			int maxPayload = client.getMaximumPayload();

			progress(20, "Reading section header");
			byte[] startPattern = client.readFlash(0x00004000, 16);

			WiFi101CertificateBundle certBundle = createBundleFromWebsites(websites);

			byte[] certData;

			if (Arrays.equals(WiFi101CertificateBundle.START_PATTERN_V0, startPattern)) {
				certData = certBundle.getEncodedV0();
			} else if (Arrays.equals(WiFi101CertificateBundle.START_PATTERN_V1, startPattern)) {
				certData = certBundle.getEncodedV1();
			} else {
				throw new Exception("Unknown starting pattern, please reflash firmware!");
			}

			int size = certData.length;
			int address = 0x00004000;
			int written = 0;

			progress(50, "Erasing target...");
			client.eraseFlash(address, size);

			while (written < size) {
				progress(60 + written * 80 / size, "Programming...");
				int len = maxPayload;
				if (written + len > size) {
					len = size - written;
				}
				client.writeFlash(address, Arrays.copyOfRange(certData, written, written + len));
				written += len;
				address += len;
			}
			progress(100, "Done!");
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	public WiFi101CertificateBundle createBundleFromWebsites(List<String> websites) throws Exception {
		WiFi101CertificateBundle certBundle = new WiFi101CertificateBundle();
		int count = 0;
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

			WiFi101Certificate cert = new WiFi101Certificate(x509);
			certBundle.add(cert);
		}
		return certBundle;
	}
}
