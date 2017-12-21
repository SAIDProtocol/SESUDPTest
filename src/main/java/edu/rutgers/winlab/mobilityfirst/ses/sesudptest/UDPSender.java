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
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ubuntu
 */
public class UDPSender {

    public static final int ETH_IP_UDP_HEADER_SIZE = 42;
    private static final Logger LOG = Logger.getLogger(UDPSender.class.getName());

    public static void writeHeader(byte[] buf, int start, int value) {
        if (buf.length < start + 4) {
            throw new IllegalArgumentException("Buffer size not large enough");
        }
        buf[start] = (byte) ((value >> 24) & 0xFF);
        buf[start + 1] = (byte) ((value >> 16) & 0xFF);
        buf[start + 2] = (byte) ((value >> 8) & 0xFF);
        buf[start + 3] = (byte) (value & 0xFF);
    }

    public static int readerHeader(byte[] buf, int start) {
        if (buf.length < start + 4) {
            throw new IllegalArgumentException("Buffer size not large enough");
        }
        int ret = 0;
        ret += (buf[start] & 0xFF) << 24;
        ret += (buf[start + 1] & 0xFF) << 16;
        ret += (buf[start + 2] & 0xFF) << 8;
        ret += (buf[start + 3] & 0xFF);
        return ret;
    }

    public static void sendPacket(String dstAddress, int dstPort,
            int packetSizeBytes, final int packetCount,
            int bitsPerSecond) throws UnknownHostException, SocketException, IOException {
        final MulticastSocket mSocket = new MulticastSocket();
        mSocket.setTimeToLive(64);
//                final DatagramSocket socket = new DatagramSocket();
        int sleepTime = 8000 * (packetSizeBytes + ETH_IP_UDP_HEADER_SIZE) / bitsPerSecond;
        System.out.printf("Per packet duration: %dms%n", sleepTime);
        final byte[] buf = new byte[packetSizeBytes];
        for (int i = 0; i < packetSizeBytes; i++) {
            buf[i] = (byte) (i & 0xFF);
        }
        InetAddress address = InetAddress.getByName(dstAddress);

        final DatagramPacket packet = new DatagramPacket(buf, packetSizeBytes, address, dstPort);

        if (sleepTime > 0) {
            final AtomicInteger counter = new AtomicInteger();
            final Timer timer = new Timer("Send timer");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int val = counter.getAndIncrement();
                    if (val == packetCount) {
                        timer.cancel();
                        return;
                    }
                    writeHeader(buf, 0, val);
                    try {
                        mSocket.send(packet);
//                    socket.send(packet);
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Failed in sending packet " + val, ex);
                    }
                }
            }, 0, sleepTime);
        } else {
            for (int i = 0; i < packetCount; i++) {
                writeHeader(buf, 0, i);
                try {
                    mSocket.send(packet);
//                    socket.send(packet);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Failed in sending packet " + i, ex);
                }
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(UDPSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void usage() {
        System.err.println("usage: java %s %dstip% %dstport% %pktSizeInBytes% %pktCount% %bandwidthInBitsPerSecond%");
        System.exit(0);
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        if (args.length < 5) {
            usage();
        }
        String dstIP = args[0];
        int dstPort = Integer.parseInt(args[1]);
        int pktSizeInBytes = Integer.parseInt(args[2]);
        int pktCount = Integer.parseInt(args[3]);
        int bandwidthInBitsPerSecond = Integer.parseInt(args[4]);
//        sendPacket("239.11.0.2", 10000, 1050 - ETH_IP_UDP_HEADER_SIZE, 5, 2 * 1024 * 1024);
        sendPacket(dstIP, dstPort, pktSizeInBytes, pktCount, bandwidthInBitsPerSecond);
    }
}
