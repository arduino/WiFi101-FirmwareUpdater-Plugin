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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLCertDownloader {

	public static Certificate[] retrieveFromURL(URL url) throws NoSuchAlgorithmException, KeyManagementException,
			SSLPeerUnverifiedException, CertificateEncodingException, FileNotFoundException, IOException {

		SSLContext ssl = SSLContext.getInstance("TLS");
		ssl.init(null, new TrustManager[] { new X509TrustManager() {
			private X509Certificate[] accepted;

			@Override
			public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				accepted = xcs;
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return accepted;
			}
		} }, null);

		// This is a workaround to reduce the impact of this bug:
		// http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8159569
		try {
			return retireveWithVerification(url, ssl);
		} catch (Exception e) {
			return retireveWithoutVerification(url, ssl);
		}
	}

	public static Certificate[] retireveWithVerification(URL url, SSLContext ssl)
			throws IOException, SSLPeerUnverifiedException {
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setSSLSocketFactory(ssl.getSocketFactory());
		connection.connect();
		Certificate[] certificates = connection.getServerCertificates();
		connection.disconnect();
		return certificates;
	}

	public static Certificate[] retireveWithoutVerification(URL url, SSLContext ssl)
			throws IOException, SSLPeerUnverifiedException {
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setHostnameVerifier((str, sess) -> true);
		connection.setSSLSocketFactory(ssl.getSocketFactory());
		connection.connect();
		Certificate[] certificates = connection.getServerCertificates();
		connection.disconnect();
		return certificates;
	}
}
