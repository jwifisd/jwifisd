package io.github.jwifisd.transcend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TranscendWiFiSD {

    String title;

    InetAddress ip;

    InetAddress netmask;

    InetAddress router;

    String mode;

    String essid;

    public TranscendWiFiSD(DatagramPacket response) {
        byte[] packet = new byte[response.getLength()];
        packet = Arrays.copyOfRange(response.getData(), response.getOffset(), response.getLength() + response.getOffset());
        String packetString = new String(packet, Charset.forName("UTF-8"));
        if (packetString.indexOf("Transcend WiFiSD") < 0) {
            // ok some other card stop this.
            throw new IllegalArgumentException("This is no Transcend card");
        }
        BufferedReader reader = new BufferedReader(new StringReader(packetString));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                int equalPos = line.indexOf('=');
                if (equalPos >= 0) {
                    String key = line.substring(0, equalPos);
                    String value = line.substring(equalPos + 1).trim();
                    if (key.equalsIgnoreCase("ip")) {
                        this.ip = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("netmask")) {
                        this.netmask = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("router")) {
                        this.router = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("mode")) {
                        this.mode = value;
                    } else if (key.equalsIgnoreCase("essid")) {
                        this.essid = value;
                    }
                }
            }
        } catch (IOException e) {
            // its a string so this can not happen..
            throw new IllegalStateException("io exception in strean reading?", e);
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("Transcend WiFiSD {");
        if (ip != null) {
            result.append("\n\tip=");
            result.append(ip.getHostAddress());
        }
        if (netmask != null) {
            result.append("\n\tnetmask=");
            result.append(netmask.getHostAddress());
        }
        if (router != null) {
            result.append("\n\trouter=");
            result.append(router.getHostAddress());
        }
        if (mode != null) {
            result.append("\n\tmode=");
            result.append(mode);
        }
        if (essid != null) {
            result.append("\n\tessid=");
            result.append(essid);
        }
        result.append("\n}");
        return result.toString();
    }
    /**
     * Transcend WiFiSD - interface=mlan0
     * ip=192.168.11.254
     * netmask=255.255.255.0
     * router=0.0.0.0
     * mode=server
     * essid=WIFISD
     */
}
