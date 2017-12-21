/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rutgers.winlab.mobilityfirst.ses.sesudptest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author ubuntu
 */
public class UDPSender {

    public static void sendPacket(String dstAddress, int dstPort,
            int packetSizeBytes, int count,
            int bitsPerSecond) throws UnknownHostException, SocketException, IOException {
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[packetSizeBytes];
        for (int i = 0; i < packetSizeBytes; i++) {
            buf[i] = (byte) (i & 0xFF);
        }
        InetAddress address = InetAddress.getByName(dstAddress);

        DatagramPacket packet = new DatagramPacket(buf, packetSizeBytes, address, dstPort);
        socket.send(packet);
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        sendPacket("239.11.0.2", 10000, 1000, 1, 2097152);
    }
}
