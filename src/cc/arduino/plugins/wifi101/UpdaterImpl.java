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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import cc.arduino.packages.BoardPort;
import cc.arduino.plugins.wifi101.flashers.Flasher;
import cc.arduino.plugins.wifi101.flashers.java.NinaFlasher;
import cc.arduino.plugins.wifi101.flashers.java.WINCFlasher;
import processing.app.Base;

@SuppressWarnings("serial")
public class UpdaterImpl extends UpdaterJFrame {
	private SerialPortListModel listModel;
	private List<String> websites = new ArrayList<>();

	public ArrayList<String> compatibleBoard;

	public static ArrayList<Flasher> fwAvailable = new ArrayList<>();

	public UpdaterImpl() throws Exception {
		super();
		Base.registerWindowCloseKeys(getRootPane(), e -> {
			setVisible(false);
		});
		Base.setIcon(this);

		fwAvailable.add(new WINCFlasher("WINC1501 Model B", "19.6.1", "firmwares/WINC1500/19.6.1/m2m_aio_3a0.bin", true, 1000000, asList("Arduino/Genuino MKR1000")));
		fwAvailable.add(new WINCFlasher("WINC1501 Model B", "19.5.4", "firmwares/WINC1500/19.5.4/m2m_aio_3a0.bin", true, 1000000, asList("Arduino/Genuino MKR1000")));
		fwAvailable.add(new WINCFlasher("WINC1501 Model B", "19.5.2", "firmwares/WINC1500/19.5.2/m2m_aio_3a0.bin", true, 1000000, asList("Arduino/Genuino MKR1000")));
		fwAvailable.add(new WINCFlasher("WINC1501 Model B", "19.4.4", "firmwares/WINC1500/19.4.4/m2m_aio_3a0.bin", true, 1000000, asList("Arduino/Genuino MKR1000")));
		fwAvailable.add(new WINCFlasher("WINC1501 Model A", "19.4.4", "firmwares/WINC1500/19.4.4/m2m_aio_2b0.bin", true, 115200, asList("Arduino WiFi 101 Shield")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.5", "firmwares/NINA/1.4.5/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.5", "firmwares/NINA/1.4.5/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.5", "firmwares/NINA/1.4.5/NINA_W102-Nano_RP2040_Connect.bin", true, 1000000, asList("Arduino Nano RP2040 Connect")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.4", "firmwares/NINA/1.4.4/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.4", "firmwares/NINA/1.4.4/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.3", "firmwares/NINA/1.4.3/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.3", "firmwares/NINA/1.4.3/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.2", "firmwares/NINA/1.4.2/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.2", "firmwares/NINA/1.4.2/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.1", "firmwares/NINA/1.4.1/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.1", "firmwares/NINA/1.4.1/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.0", "firmwares/NINA/1.4.0/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.4.0", "firmwares/NINA/1.4.0/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.3.0", "firmwares/NINA/1.3.0/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.3.0", "firmwares/NINA/1.3.0/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.4", "firmwares/NINA/1.2.4/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino NANO 33 IoT")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.4", "firmwares/NINA/1.2.4/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.3", "firmwares/NINA/1.2.3/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.3", "firmwares/NINA/1.2.3/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.2", "firmwares/NINA/1.2.2/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.2", "firmwares/NINA/1.2.2/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.1", "firmwares/NINA/1.2.1/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.2.1", "firmwares/NINA/1.2.1/NINA_W102-Uno_WiFi_Rev2.bin", true, 1000000, asList("Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.1.0", "firmwares/NINA/1.1.0/NINA_W102.bin", true, 1000000, asList("Arduino MKR WiFi 1010", "Arduino MKR Vidor 4000", "Arduino Uno WiFi Rev2")));
		fwAvailable.add(new NinaFlasher("NINA firmware", "1.0.0", "firmwares/NINA/1.0.0/NINA_W102.bin", false, 1000000, asList("Arduino MKR WiFi 1010", "Arduino MKR Vidor 4000", "Arduino Uno WiFi Rev2")));

		for (Flasher firmware : fwAvailable) {
			getFirmwareSelector().addItem(firmware);
		}
		if (getSerialPortList().getModel().getSize() == 0) {
			setEnabledCommand(false);
		}
		refreshSerialPortList();
		websites.add("arduino.cc:443");
		refreshCertList();
	}

	private void refreshCertList() {
		CertificateListModel model = new CertificateListModel(websites);
		getCertSelector().setModel(model);
	}

	@Override
	protected void refreshSerialPortList() {
		DefaultListModel<String> model = new DefaultListModel<>();
		BoardPort board;
		listModel = new SerialPortListModel();
		for (int i = 0; i < listModel.getSize(); i++) {
			board = listModel.getPort(i);
			if (board.getBoardName() != null) {
				model.addElement(board.getBoardName().concat(" (").concat(board.getAddress()).concat(")"));
			} else {
				model.addElement(board.getAddress());
			}
		}
		getSerialPortList().removeAll();
		getSerialPortList().setModel(model);
	}

	@Override
	protected void updateCertSection() {
		Flasher fw = (Flasher) getFirmwareSelector().getSelectedItem();
		if (fw != null) {
			hideCertificatePanel(fw.certificatesAvailable());
		}
	}

	@Override
	protected void SelectBoardModule() {
		int added = 0;
		setEnabledCommand(true);
		hideCertificatePanel(true);
		getFirmwareSelector().removeAllItems();
		if (getSelectedPort() != null) {
			for (Flasher firmware : fwAvailable) {
				if (firmware.isCompatible(getSelectedPort().getBoardName())) {
					getFirmwareSelector().addItem(firmware);
					added++;
				}
			}
			if (added == 0) {
				// Board not automatically recognized, give the chance to flash any firmware
				for (Flasher firmware : fwAvailable) {
					getFirmwareSelector().addItem(firmware);
				}
			}
			Flasher fw = (Flasher) getFirmwareSelector().getSelectedItem();
			hideCertificatePanel(fw.certificatesAvailable());
		} else {
			setEnabledCommand(false);
			hideCertificatePanel(false);
		}
	}

	private BoardPort getSelectedPort() {
		int i = getSerialPortList().getSelectedIndex();
		if (i == -1)
			return null;
		return listModel.getPort(i);
	}

	@Override
	protected void testConnection() {
		Flasher fw = (Flasher) getFirmwareSelector().getSelectedItem();
		fw.setProgressBar(getUpdateProgressBar());
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to run the test!");
			return;
		}

		setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					fw.testConnection(port.getAddress(), fw.getBaudrate());
					JOptionPane.showMessageDialog(UpdaterImpl.this, "The programmer is working!", "Test successful",
		          JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(UpdaterImpl.this, e.getMessage(), "Connection error.",
		          JOptionPane.ERROR_MESSAGE);
				}
				setEnabled(true);
				resetProgress();
			}
		}.start();
	}

	@Override
	protected void openFirmwareUpdaterSketch() {
		Flasher fw = (Flasher) getFirmwareSelector().getSelectedItem();
		try {
			fw.openFirmwareUpdaterSketch(getSelectedPort());
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a board from the connected ones.", "No board selected",
				JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, e.getMessage(), "Missing library",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	protected void updateFirmware() {
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to update firmware!");
			return;
		}

		Flasher fw = (Flasher) getFirmwareSelector().getSelectedItem();
		fw.setProgressBar(getUpdateProgressBar());
		setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					fw.updateFirmware(port.getAddress());
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
		String website = JOptionPane.showInputDialog(this, "Enter the website to fetch SSL certificate:",
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
		Flasher fw = (Flasher) getFirmwareSelector().getSelectedItem();
		fw.setProgressBar(getUpdateProgressBar());
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to upload SSL certificates!");
			return;
		}

		setEnabled(false);
		new Thread() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				try {
					fw.uploadCertificates(port.getAddress(), websites);
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
