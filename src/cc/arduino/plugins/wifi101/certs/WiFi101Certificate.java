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
package cc.arduino.plugins.wifi101.certs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.x509.Time;

public class WiFi101Certificate {

	byte data[];
	String subject;
	String hash;

	public WiFi101Certificate(X509Certificate x509) throws Exception, NoSuchAlgorithmException, IOException {
		String algo = x509.getPublicKey().getAlgorithm();
		if (!algo.equals("RSA") || !(x509.getPublicKey() instanceof RSAPublicKey))
			throw new Exception("SSL Certificate must have an RSA Public Key");
		RSAPublicKey publicKey = (RSAPublicKey) x509.getPublicKey();

		byte[] publicExponent = publicKey.getPublicExponent().toByteArray();
		byte[] publicExponentLen = shortToBytes(publicExponent.length);
		byte[] publicModulus = publicKey.getModulus().toByteArray();
		if (publicModulus.length == 257 || publicModulus[0] == 0) {
			publicModulus = Arrays.copyOfRange(publicModulus, 1, publicModulus.length);
		}
		byte[] publicModulusLen = shortToBytes(publicModulus.length);
		byte[] name1hash = getSubjectValueHash(x509);
		byte[] notBefore = encodeTimestamp(x509.getNotBefore());
		byte[] notAfter = encodeTimestamp(x509.getNotAfter());

		ByteArrayOutputStream res = new ByteArrayOutputStream();
		res.write(name1hash);
		res.write(publicModulusLen);
		res.write(publicExponentLen);
		res.write(notBefore);
		res.write(notAfter);
		res.write(publicModulus);
		res.write(publicExponent);
		while (res.size() % 4 != 0)
			res.write(0xFF);
		data = res.toByteArray();

		byte[] digest = MessageDigest.getInstance("SHA-1").digest(data);
		hash = DatatypeConverter.printHexBinary(digest).substring(0, 6);
	}

	@Override
	public String toString() {
		return "(" + hash + ")";
	}

	private static byte[] shortToBytes(int x) {
		// little endian
		byte ret[] = new byte[2];
		ret[0] = (byte) x;
		ret[1] = (byte) (x >> 8);
		return ret;
	}

	private static byte[] encodeTimestamp(Date notBefore) throws IOException {
		ByteArrayOutputStream encoded = new ByteArrayOutputStream();
		ASN1OutputStream asn1 = new ASN1OutputStream(encoded);
		asn1.writeObject(new Time(notBefore));
		return Arrays.copyOfRange(encoded.toByteArray(), 2, 22);
	}

	private static byte[] getSubjectValueHash(X509Certificate x509) throws NoSuchAlgorithmException, IOException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		ASN1InputStream ais = new ASN1InputStream(x509.getSubjectX500Principal().getEncoded());
		while (ais.available() > 0) {
			ASN1Primitive obj = ais.readObject();
			sha1.update(extractPrintableString(obj));
		}
		ais.close();
		return sha1.digest();
	}

	private static byte[] extractPrintableString(ASN1Encodable obj) throws IOException {
		if (obj instanceof DERPrintableString) {
			DERPrintableString s = (DERPrintableString) obj;
			// System.out.println("'" + s.getString() + "'");
			return s.getString().getBytes();
		}
		ByteArrayOutputStream res = new ByteArrayOutputStream();
		if (obj instanceof DLSequence) {
			DLSequence s = (DLSequence) obj;
			for (int i = 0; i < s.size(); i++) {
				res.write(extractPrintableString(s.getObjectAt(i)));
			}
		}
		if (obj instanceof DLSet) {
			DLSet s = (DLSet) obj;
			for (int i = 0; i < s.size(); i++) {
				res.write(extractPrintableString(s.getObjectAt(i)));
			}
		}
		return res.toByteArray();
	}

	public byte[] getEncoded() {
		return data.clone();
	}
}
