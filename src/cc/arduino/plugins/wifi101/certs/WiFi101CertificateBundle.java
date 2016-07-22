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
import java.util.ArrayList;

@SuppressWarnings("serial")
public class WiFi101CertificateBundle extends ArrayList<WiFi101Certificate> {

	private final byte START_PATTERN[] = new byte[] { 0x01, (byte) 0xF1, 0x02, (byte) 0xF2, 0x03, (byte) 0xF3, 0x04,
	    (byte) 0xF4, 0x05, (byte) 0xF5, 0x06, (byte) 0xF6, 0x07, (byte) 0xF7, 0x08, (byte) 0xF8 };

	public byte[] getEncoded() {
		ByteArrayOutputStream res = new ByteArrayOutputStream();
		try {
			res.write(START_PATTERN);
			
			// Write number of certs, little endian
			res.write(size());
			res.write(0);
			res.write(0);
			res.write(0);
			
			for (WiFi101Certificate cert : this) {
				res.write(cert.getEncoded());
			}
		} catch (IOException e) {
			// Should never happen...
			e.printStackTrace();
			return null;
		}
		return res.toByteArray();
	}
}
