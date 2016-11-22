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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import cc.arduino.packages.BoardPort;
import cc.arduino.plugins.wifi101.firmwares.WINC1500Firmware;
import cc.arduino.plugins.wifi101.flashers.Flasher;
import cc.arduino.plugins.wifi101.flashers.java.JavaFlasher;
import processing.app.Base;

@SuppressWarnings("serial")
public class UpdaterImpl extends UpdaterJFrame {

	private Flasher flasher;

	public UpdaterImpl() throws Exception {
		super();
		Base.registerWindowCloseKeys(getRootPane(), e -> {
			setVisible(false);
		});
		Base.setIcon(this);

		for (WINC1500Firmware firmware : WINC1500Firmware.available)
			getFirmwareSelector().addItem(firmware);

		refreshSerialPortList();

		websites.add("arduino.cc:443");

		refreshCertList();

		// flasher = new CLIFlasher() {
		flasher = new JavaFlasher() {
			@Override
			public void progress(int progress, String text) {
				if (text.length() > 60) {
					text = text.substring(0, 60) + "...";
				}
				getUpdateProgressBar().setValue(progress);
				getUpdateProgressBar().setStringPainted(true);
				getUpdateProgressBar().setString(text);
			}
		};
	}

	private List<String> websites = new ArrayList<>();

	private void refreshCertList() {
		CertificateListModel model = new CertificateListModel(websites);
		getCertSelector().setModel(model);
	}

	private SerialPortListModel listModel;

	@Override
	protected void refreshSerialPortList() {
		listModel = new SerialPortListModel();
		getSerialPortList().setModel(listModel);
	}

	private BoardPort getSelectedPort() {
		int i = getSerialPortList().getSelectedIndex();
		if (i == -1)
			return null;
		return listModel.getPort(i);
	}

	@Override
	protected void testConnection() {
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to run the test!");
			return;
		}

		setEnabled(false);
		new Thread() {
			public void run() {
				try {
					flasher.testConnection(port.getAddress());
					JOptionPane.showMessageDialog(UpdaterImpl.this, "The programmer is working!", "Test successful",
		          JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(UpdaterImpl.this, e.getMessage(), "Connection error.",
		          JOptionPane.ERROR_MESSAGE);
				}
				setEnabled(true);
				resetProgress();
			};
		}.start();
	}

	@Override
	protected void updateFirmware() {
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to update firmware!");
			return;
		}

		WINC1500Firmware fw = (WINC1500Firmware) getFirmwareSelector().getSelectedItem();
		setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					flasher.updateFirmware(port.getAddress(), fw);
					JOptionPane.showMessageDialog(UpdaterImpl.this, "The firmware has been updated!", "Success",
		          JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(UpdaterImpl.this, e.getMessage(), "Upload error.", JOptionPane.ERROR_MESSAGE);
				}
				setEnabled(true);
				resetProgress();
			}
		}.start();
	}

	@Override
	protected void addCertificate() {
		String website = (String) JOptionPane.showInputDialog(this, "Enter the website to fetch SSL certificate:",
		    "Add SSL certificate from website", JOptionPane.QUESTION_MESSAGE);
		if (website.startsWith("http://")) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Sorry \"http://\" protocol doesn't support SSL",
			    "Invalid URL error.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (website.startsWith("https://")) {
			website = website.substring(8);
		}
		if (website.endsWith("/")) {
			website = website.substring(0, website.length() - 1);
		}
		if (!website.contains(":")) {
			website += ":443";
		}
		if (website.contains("/")) {
			JOptionPane.showMessageDialog(UpdaterImpl.this,
			    "Error: please use enter the addres using the format:\nwww.example.com:443", "Invalid URL error.",
			    JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!websites.contains(website))
			websites.add(website);
		refreshCertList();
	}

	@Override
	protected void removeCertificate() {
		int idx = getCertSelector().getSelectedIndex();
		if (idx == -1)
			return;
		websites.remove(idx);
		refreshCertList();
	}

	@Override
	protected void uploadCertificates() {
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to upload SSL certificates!");
			return;
		}

		setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					flasher.uploadCertificates(port.getAddress(), websites);

					JOptionPane.showMessageDialog(UpdaterImpl.this, "The certificates have been uploaded!", "Success",
		          JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(UpdaterImpl.this, e.getMessage(), "Upload error.", JOptionPane.ERROR_MESSAGE);
				}
				setEnabled(true);
				resetProgress();
			}
		}.start();
	}

	public void resetProgress() {
		getUpdateProgressBar().setValue(0);
		getUpdateProgressBar().setStringPainted(false);
	}
}
