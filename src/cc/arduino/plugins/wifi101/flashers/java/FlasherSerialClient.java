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

import static jssc.SerialPort.PARITY_NONE;
import static jssc.SerialPort.STOPBITS_1;
import static processing.app.I18n.format;
import static processing.app.I18n.tr;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jssc.SerialPort;
import jssc.SerialPortException;
import processing.app.SerialException;
import processing.app.SerialNotFoundException;;

public class FlasherSerialClient {

	private SerialPort port;
	private volatile byte recvBuffer[] = new byte[1000000];
	private volatile int recvPos = 0, writePos = 0;

	public void open(String portName) throws SerialException, InterruptedException {

		try {
			port = new SerialPort(portName);
			if (!port.openPort()) {
				throw new SerialException("Error opening serial port");
			}
			boolean res = port.setParams(115200, 8, STOPBITS_1, PARITY_NONE, true, true);
			if (!res) {
				System.err.println(format(tr("Error while setting serial port parameters: {0} {1} {2} {3}"), 115200,
				    PARITY_NONE, 8, STOPBITS_1));
			}
			port.addEventListener((evt) -> {
				if (!evt.isRXCHAR())
					return;
				try {
					byte[] data = port.readBytes(evt.getEventValue());
					// System.out.println("READ: " + data.length);
					if (data == null || data.length == 0)
						return;
					synchronized (recvBuffer) {
						for (int i = 0; i < data.length; i++)
							recvBuffer[writePos++] = data[i];
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			});
		} catch (SerialPortException e) {
			if (e.getPortName().startsWith("/dev")
			    && SerialPortException.TYPE_PERMISSION_DENIED.equals(e.getExceptionType())) {
				throw new SerialException(format(
				    tr("Error opening serial port ''{0}''. Try consulting the documentation at http://playground.arduino.cc/Linux/All#Permission"),
				    portName));
			}
			throw new SerialException(format(tr("Error opening serial port ''{0}''."), portName), e);
		}

		if (port == null) {
			throw new SerialNotFoundException(format(tr("Serial port ''{0}'' not found."), portName));
		}

		// Wait for boards that do auto-reset on serial port opening
		Thread.sleep(2500);
	}

	public void close() throws SerialPortException {
		port.closePort();
	}

	public void hello() throws Exception {
		sendCommand((byte) 0x99, 0x11223344, 0x55667788, null);
		byte[] answer = waitAnswer(100, 6);
		if (answer.length != 6 || answer[0] != 'v')
			throw new Exception("Programmer not responding");
		String version = new String(answer);
		if (!version.equals("v10000"))
			throw new Exception("Programmer version mismatch: " + version + ", but v10000 is required!");
	}

	public int getMaximumPayload() throws Exception {
		sendCommand((byte) 0x50, 0, 0, null);
		byte[] answer = waitAnswer(100, 2);
		if (answer.length != 2)
			throw new Exception("Error while reading programmers parameters.");
		return answer[0] << 8 + answer[1];
	}

	public byte[] readFlash(int address, int length) throws Exception {
		sendCommand((byte) 0x01, address, length, null);
		byte[] data = waitAnswer(500, length);
		if (data.length != length) {
			throw new Exception("Error while reading flash memory.");
		}
		if (!ack())
			throw new Exception("Error while reading flash memory.");
		return data;
	}

	public byte[] md5Flash(int address, int length) throws Exception {
		sendCommand((byte) 0x04, address, length, null);
		if (ack(5000)) {
			byte[] data = waitAnswer(5000, 16);
			return data;
		}
		return null;
	}

	public void writeFlash(int address, byte data[]) throws Exception {
		sendCommand((byte) 0x02, address, 0, data);
		if (!ack())
			throw new Exception("Error while writing flash memory.");
	}

	public void eraseFlash(int address, int size) throws Exception {
		sendCommand((byte) 0x03, address, size, null);
		if (!ack(20000))
			throw new Exception("Error while erasing flash memory.");
	}

	public void addCertificateHighLevel(int command, byte payload[]) throws Exception {
		sendCommand((byte) 0x55, command, command, payload);
		if (!ack(20000))
			throw new Exception("Error while uploading certificate.");
	}

	private boolean ack() throws InterruptedException {
		return ack(200);
	}

	private boolean ack(int timeout) throws InterruptedException {
		byte[] ack = waitAnswer(timeout, 2);
		if (ack.length != 2 || ack[0] != 'O' || ack[1] != 'K')
			return false;
		return true;
	}

	private byte[] waitAnswer(int timeout, int expectedLen) throws InterruptedException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int written = 0;
		while (timeout > 0 && written < expectedLen) {
			int c = read();
			if (c != -1) {
				out.write(c);
				written++;
				continue;
			}
			Thread.sleep(10);
			timeout -= 10;
		}
		return out.toByteArray();
	}

	private int read() {
		synchronized (recvBuffer) {
			if (recvPos == writePos)
				return -1;
			return ((int) recvBuffer[recvPos++]) & 0xFF;
		}
	}

	private void sendCommand(byte command, int address, int val, byte payload[]) throws SerialPortException {
		short payloadLen = 0;
		if (payload != null)
			payloadLen = (short) payload.length;
		ByteBuffer buff = ByteBuffer.allocate(11 + payloadLen);
		buff.order(ByteOrder.BIG_ENDIAN);
		buff.put(command);
		buff.putInt(address);
		buff.putInt(val);
		buff.putShort(payloadLen);
		if (payload != null)
			buff.put(payload);
		byte data[] = buff.array();
		// System.out.println("CMD:" + DatatypeConverter.printHexBinary(data));
		port.writeBytes(data);
	}
}
