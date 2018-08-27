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
import cc.arduino.plugins.wifi101.firmwares.WiFiFirmware;
import cc.arduino.plugins.wifi101.flashers.Flasher;
import cc.arduino.plugins.wifi101.flashers.java.JavaFlasher;
import cc.arduino.plugins.wifi101.flashers.java.NinaFlasher;
import processing.app.Base;

@SuppressWarnings("serial")
public class UpdaterImpl extends UpdaterJFrame {
	private Flasher flasher;
  private SerialPortListModel listModel;

	public static WiFiFirmware fwAvailable[] = new WiFiFirmware[] {
			new WiFiFirmware("WifiNINA firmaware", "1.0.0", "firmwares/WifiNINA/1.0.0/m2m_aio_1a0.bin","WifiNINA",0x00000000,0x00000000, new NinaFlasher()),
			new WiFiFirmware("WINC1501 Model B", "19.5.4", "firmwares/WifiWINC1500/19.5.4/m2m_aio_3a0.bin","WINC1500",0x00000000,0x00004000, new JavaFlasher()),
			new WiFiFirmware("WINC1501 Model B", "19.5.2", "firmwares/WifiWINC1500/19.5.2/m2m_aio_3a0.bin","WINC1500",0x00000000,0x00004000, new JavaFlasher()),
			new WiFiFirmware("WINC1501 Model A", "19.4.4", "firmwares/WifiWINC1500/19.4.4/m2m_aio_2b0.bin","WINC1500",0x00000000,0x00004000,  new JavaFlasher())};

	public UpdaterImpl() throws Exception {
		super();
		Base.registerWindowCloseKeys(getRootPane(), e -> {
			setVisible(false);
		});
		Base.setIcon(this);

		for (WiFiFirmware firmware : fwAvailable)
	  	if (firmware.board.equals("WINC1500")) {
		    getFirmwareSelector().addItem(firmware);
      }

		getBoardSelector().addItem("WINC1500");
		getBoardSelector().addItem("WifiNINA");
  	refreshSerialPortList();
		websites.add("arduino.cc:443");

		refreshCertList();
	}

	private List<String> websites = new ArrayList<>();

	private void refreshCertList() {
		CertificateListModel model = new CertificateListModel(websites);
		getCertSelector().setModel(model);
	}

	@Override
	protected void refreshSerialPortList() {
		listModel = new SerialPortListModel();
		getSerialPortList().setModel(listModel);
	}

  @Override
  protected	void 	SelectBoardModule() {
	  String selectedBoard = listModel.getModuleBoard((getSelectedPort().getVID().toLowerCase()).concat(":").concat(getSelectedPort().getPID().toLowerCase()));
	  getBoardSelector().setSelectedItem( selectedBoard);
  }

	private BoardPort getSelectedPort() {
		int i = getSerialPortList().getSelectedIndex();
		if (i == -1)
			return null;
		return listModel.getPort(i);
	}

	@Override
	protected void testConnection() {
		WiFiFirmware fw = (WiFiFirmware) getFirmwareSelector().getSelectedItem();
		fw.setProgressBar(getUpdateProgressBar());
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to run the test!");
			return;
		}

		setEnabled(false);
		new Thread() {
			public void run() {
				try {
					fw.getFlasher().testConnection(port.getAddress());
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
	protected void updateFirmwareSelector() {
		getFirmwareSelector().removeAllItems();
		for (WiFiFirmware firmware : fwAvailable)
		if (firmware.board.equals(getBoardSelector().getSelectedItem())) {
			getFirmwareSelector().addItem(firmware);
		}
	}

	@Override
	protected void updateFirmware() {
		BoardPort port = getSelectedPort();
		if (port == null) {
			JOptionPane.showMessageDialog(UpdaterImpl.this, "Please select a port to update firmware!");
			return;
		}

		WiFiFirmware fw = (WiFiFirmware) getFirmwareSelector().getSelectedItem();
		fw.setProgressBar(getUpdateProgressBar());
		setEnabled(false);
		new Thread() {
			@Override
			public void run() {
				try {
					fw.getFlasher().updateFirmware(port.getAddress(), fw);
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
		WiFiFirmware fw = (WiFiFirmware) getFirmwareSelector().getSelectedItem();
		fw.setProgressBar(getUpdateProgressBar());
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
					fw.getFlasher().uploadCertificates(port.getAddress(), websites);

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
