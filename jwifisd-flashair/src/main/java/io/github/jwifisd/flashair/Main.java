package io.github.jwifisd.flashair;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

public class Main {

    static String query = "000000000001000000000000" + Hex.encodeHexString("flashair".getBytes(Charset.forName("UTF-8"))) + "056c6f63616c0000010001";

    public static void main(String[] args) throws Exception {
        MulticastSocket socket = new MulticastSocket(5353);
        socket.setReuseAddress(true);
        socket.setSoTimeout(5000);

        NetworkInterface nic = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.0.11"));
        socket.joinGroup(new InetSocketAddress("224.0.0.251", 5353), nic);

        byte[] qd = Hex.decodeHex(query.toCharArray());
        DatagramPacket q = new DatagramPacket(qd, qd.length, new InetSocketAddress("192.168.0.255", 5353));

        socket.send(q);
        byte[] buffer = new byte[1000];

        while (true) {
            try {
                DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                socket.receive(data);

                byte[] packet = new byte[data.getLength()];
                packet = Arrays.copyOfRange(data.getData(), data.getOffset(), data.getLength() + data.getOffset());
                DNSMessage message = new DNSMessage();

                message.read(new ByteArrayInputStream(packet));

                if (message.dnsHeader.isResponse && message.data.questions.get(0).getFullQualifiedDomainName().indexOf("flashair") >= 0) {
                    System.out.println(message.data.answers.get(0).payload);
                }
                System.out.println(Hex.encodeHexString(packet));
                System.out.println(message);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
