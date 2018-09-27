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
package cc.arduino.plugins.wifi101;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import cc.arduino.packages.BoardPort;
import processing.app.Base;

class SerialPortListModel implements ListModel<String> {

	private List<BoardPort> ports;

	public SerialPortListModel() {
		ports = Base.getDiscoveryManager().discovery();
		ports.removeIf(port -> !port.getProtocol().equals("serial"));
	}

	@Override
	public int getSize() {
		return ports.size();
	}

	@Override
	public String getElementAt(int index) {
		return ports.get(index).getAddress();
	}

	@Override
	public void addListDataListener(ListDataListener l) {
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
	}

	public BoardPort getPort(int index) {
		return ports.get(index);
	}
}
