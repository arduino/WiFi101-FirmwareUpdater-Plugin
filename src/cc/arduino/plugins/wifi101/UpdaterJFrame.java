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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cc.arduino.plugins.wifi101.flashers.Flasher;

@SuppressWarnings("serial")
public class UpdaterJFrame extends JFrame {

	private JPanel contentPane;
	private JList<String> serialPortList;
	private JComboBox<Flasher> firmwareSelector;

	private JProgressBar updateProgressBar;
	private JButton removeCertificateButton;
	private JList<String> certSelector;
	private JPanel panel_2;
	private JPanel panel;
	private JPanel panel_1;
	private JLabel textArea;
	private JLabel textFwNot;

	private JButton uploadCertificatesButton;
	private JButton addCertificateButton;
	private JButton updateFirmwareButton;
	private JButton testConnectionButton;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UpdaterJFrame frame = new UpdaterJFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public UpdaterJFrame() {
		setTitle("WiFi101 / WiFiNINA Firmware/Certificates Updater");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 500, 520);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "1. Select port of the WiFi module", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setMinimumSize(new Dimension(500, 150));
		panel_1.setPreferredSize(new Dimension(500, 150));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(5, 5, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		contentPane.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel textSelectPort = new JLabel("If the port is not listed click \"Refresh list\" button to regenerate the list");
		textSelectPort.setOpaque(false);
		GridBagConstraints gbc_textSelectPort = new GridBagConstraints();
		gbc_textSelectPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSelectPort.gridwidth = 2;
		gbc_textSelectPort.insets = new Insets(5, 5, 5, 5);
		gbc_textSelectPort.gridx = 0;
		gbc_textSelectPort.gridy = 0;
		panel_1.add(textSelectPort, gbc_textSelectPort);

		serialPortList = new JList<String>();
		JScrollPane sp = new JScrollPane(serialPortList);
		serialPortList.setMaximumSize(new Dimension(300, 50));
		GridBagConstraints gbc_serialPortList = new GridBagConstraints();
		gbc_serialPortList.insets = new Insets(5, 5, 5, 5);
		gbc_serialPortList.fill = GridBagConstraints.BOTH;
		gbc_serialPortList.gridx = 0;
		gbc_serialPortList.gridy = 1;
		gbc_serialPortList.gridheight = 2;
		panel_1.add(sp, gbc_serialPortList);
		serialPortList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean enabled = (serialPortList.getSelectedIndex() != -1);
				SelectBoardModule();
			}
		});

		JButton refreshListButton = new JButton("Refresh list");
		refreshListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshSerialPortList();
			}
		});
		GridBagConstraints gbc_refreshListButton = new GridBagConstraints();
		gbc_refreshListButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_refreshListButton.insets = new Insets(5, 5, 5, 5);
		gbc_refreshListButton.gridx = 1;
		gbc_refreshListButton.gridy = 1;
		panel_1.add(refreshListButton, gbc_refreshListButton);

		testConnectionButton = new JButton("Test connection");
		testConnectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testConnection();
			}
		});

		GridBagConstraints gbc_testConnectionButton = new GridBagConstraints();
		gbc_testConnectionButton.insets = new Insets(5, 5, 5, 5);
		gbc_testConnectionButton.gridx = 1;
		gbc_testConnectionButton.gridy = 2;
		panel_1.add(testConnectionButton, gbc_testConnectionButton);

		panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "2. Update firmware", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.setMinimumSize(new Dimension(500, 150));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 0, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel textSelectTheFirmware = new JLabel();
		textSelectTheFirmware.setText("Select the firmware from the dropdown box below");
		textSelectTheFirmware.setOpaque(false);
		GridBagConstraints gbc_textSelectTheFirmware = new GridBagConstraints();
		gbc_textSelectTheFirmware.insets = new Insets(5, 5, 5, 0);
		gbc_textSelectTheFirmware.fill = GridBagConstraints.BOTH;
		gbc_textSelectTheFirmware.gridx = 0;
		gbc_textSelectTheFirmware.gridy = 1;
		panel.add(textSelectTheFirmware, gbc_textSelectTheFirmware);

		firmwareSelector = new JComboBox<Flasher>();
		GridBagConstraints gbc_firmwareSelector = new GridBagConstraints();
		gbc_firmwareSelector.insets = new Insets(5, 5, 5, 0);
		gbc_firmwareSelector.fill = GridBagConstraints.HORIZONTAL;
		gbc_firmwareSelector.gridx = 0;
		gbc_firmwareSelector.gridy = 2;
		panel.add(firmwareSelector, gbc_firmwareSelector);

		firmwareSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCertSection();
			}
		});

		updateFirmwareButton = new JButton("Update Firmware");
		updateFirmwareButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFirmware();
			}
		});
		GridBagConstraints gbc_updateFirmwareButton = new GridBagConstraints();
		gbc_updateFirmwareButton.insets = new Insets(5, 5, 0, 0);
		gbc_updateFirmwareButton.gridx = 0;
		gbc_updateFirmwareButton.gridy = 3;
		panel.add(updateFirmwareButton, gbc_updateFirmwareButton);

		panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "3. Update SSL root certificates", TitledBorder.LEFT, TitledBorder.TOP, null, null));
		panel_2.setMinimumSize(new Dimension(500, 200));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(5, 5, 0, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 2;
		contentPane.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, 0.0};
		gbl_panel_2.rowWeights = new double[]{1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		textArea = new JLabel();
		textArea.setText("Add domains in the list below using \"Add domain\" button");
		textArea.setOpaque(false);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 2;
		gbc_textArea.insets = new Insets(5, 5, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 0;
		panel_2.add(textArea, gbc_textArea);

		textFwNot= new JLabel();
		textFwNot.setText("No certificates available");
		textFwNot.setOpaque(false);
		textFwNot.setMinimumSize(new Dimension(500,500));
		GridBagConstraints gbc_textFwNot = new GridBagConstraints();
		gbc_textFwNot.gridwidth = 2;
		gbc_textFwNot.insets = new Insets(5, 5, 5, 0);
		gbc_textFwNot.fill = GridBagConstraints.BOTH;
		gbc_textFwNot.gridx = 0;
		gbc_textFwNot.gridy = 0;
		panel_2.add(textFwNot, gbc_textFwNot);
		textFwNot.setVisible(false);

		certSelector = new JList<>();
		certSelector.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean enabled = (certSelector.getSelectedIndex() != -1);
				getRemoveCertificateButton().setEnabled(enabled);
			}
		});
		GridBagConstraints gbc_certSelector = new GridBagConstraints();
		gbc_certSelector.gridheight = 2;
		gbc_certSelector.insets = new Insets(0, 0, 5, 5);
		gbc_certSelector.fill = GridBagConstraints.BOTH;
		gbc_certSelector.gridx = 0;
		gbc_certSelector.gridy = 1;
		panel_2.add(certSelector, gbc_certSelector);

		addCertificateButton = new JButton("Add domain");
		addCertificateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addCertificate();
			}
		});
		GridBagConstraints gbc_addCertificateButton = new GridBagConstraints();
		gbc_addCertificateButton.insets = new Insets(0, 0, 5, 0);
		gbc_addCertificateButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_addCertificateButton.gridx = 1;
		gbc_addCertificateButton.gridy = 1;
		panel_2.add(addCertificateButton, gbc_addCertificateButton);

		removeCertificateButton = new JButton("Remove domain");
		removeCertificateButton.setEnabled(false);
		removeCertificateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeCertificate();
			}
		});
		GridBagConstraints gbc_removeCertificateButton = new GridBagConstraints();
		gbc_removeCertificateButton.insets = new Insets(0, 0, 5, 0);
		gbc_removeCertificateButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_removeCertificateButton.gridx = 1;
		gbc_removeCertificateButton.gridy = 2;
		panel_2.add(removeCertificateButton, gbc_removeCertificateButton);

		uploadCertificatesButton = new JButton("Upload Certificates to WiFi module");
		uploadCertificatesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uploadCertificates();
			}
		});
		GridBagConstraints gbc_uploadCertificatesButton = new GridBagConstraints();
		gbc_uploadCertificatesButton.gridwidth = 2;
		gbc_uploadCertificatesButton.insets = new Insets(0, 0, 0, 5);
		gbc_uploadCertificatesButton.gridx = 0;
		gbc_uploadCertificatesButton.gridy = 3;
		panel_2.add(uploadCertificatesButton, gbc_uploadCertificatesButton);

		updateProgressBar = new JProgressBar();
		GridBagConstraints gbc_updateProgressBar = new GridBagConstraints();
		gbc_updateProgressBar.insets = new Insets(5, 5, 5, 5);
		gbc_updateProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_updateProgressBar.gridx = 0;
		gbc_updateProgressBar.gridy = 3;
		contentPane.add(updateProgressBar, gbc_updateProgressBar);
	}

	protected void hideCertificatePanel(boolean visible){
		panel_2.setEnabled(visible);
		certSelector.setVisible(visible);
		uploadCertificatesButton.setVisible(visible);
		addCertificateButton.setVisible(visible);
		removeCertificateButton.setVisible(visible);
		textArea.setVisible(visible);
		textFwNot.setVisible(!visible);
	}

	protected void setEnabledCommand(boolean state) {
		uploadCertificatesButton.setEnabled(state);
		addCertificateButton.setEnabled(state);
		testConnectionButton.setEnabled(state);
		updateFirmwareButton.setEnabled(state);
		firmwareSelector.setEnabled(state);
		certSelector.setEnabled(state);
	}

	protected void uploadCertificates() {
		// To be overridden
	}

	protected void removeCertificate() {
		// To be overridden
	}

	protected void addCertificate() {
		// To be overridden
	}

	protected void updateFirmware() {
		// To be overridden
	}

	protected void testConnection() {
		// To be overridden
	}

	protected void refreshSerialPortList() {
		// To be overridden
	}

	protected void SelectBoardModule() {
		// To be overridden
	}

	protected void updateCertSection() {
		// To be overridden
	}

	protected JList<String> getSerialPortList() {
		return serialPortList;
	}
	protected JComboBox<Flasher> getFirmwareSelector() {
		return firmwareSelector;
	}
	protected JProgressBar getUpdateProgressBar() {
		return updateProgressBar;
	}
	protected JButton getRemoveCertificateButton() {
		return removeCertificateButton;
	}
	protected JList<String> getCertSelector() {
		return certSelector;
	}
}
