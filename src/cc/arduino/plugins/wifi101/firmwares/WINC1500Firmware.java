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
package cc.arduino.plugins.wifi101.firmwares;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class WINC1500Firmware {

	public static WINC1500Firmware available[] = new WINC1500Firmware[] {
	    new WINC1500Firmware("WINC1501 Model B", "19.5.4", "firmwares/19.5.4/m2m_aio_3a0.bin"),
	    new WINC1500Firmware("WINC1501 Model B", "19.5.2", "firmwares/19.5.2/m2m_aio_3a0.bin"),
	    new WINC1500Firmware("WINC1501 Model B", "19.4.4", "firmwares/19.4.4/m2m_aio_3a0.bin"),
	    new WINC1500Firmware("WINC1501 Model A", "19.4.4", "firmwares/19.4.4/m2m_aio_2b0.bin") };

	public String name;
	public String version;
	public File file;

	public WINC1500Firmware(String _name, String _version, String _filename) {
		name = _name;
		version = _version;
		file = null;
		try {
			String jarPath = WINC1500Firmware.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File jarFolder = new File(jarPath).getParentFile();
			file = new File(jarFolder, _filename);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return name + " (" + version + ")";
	}

	public byte[] getData() throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			ByteArrayOutputStream res = new ByteArrayOutputStream();
			byte buff[] = new byte[4096];
			while (in.available() > 0) {
				int read = in.read(buff);
				if (read == -1)
					break;
				res.write(buff, 0, read);
			}
			return res.toByteArray();
		} finally {
			if (in != null)
				in.close();
		}
	}

	public File getFile() {
		return file;
	}
}
