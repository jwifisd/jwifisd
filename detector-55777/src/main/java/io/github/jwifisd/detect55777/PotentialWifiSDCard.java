package io.github.jwifisd.detect55777;

/*
 * #%L
 * jwifisd-detector-55777
 * %%
 * Copyright (C) 2012 - 2015 jwifisd
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.jwifisd.api.IBrowse;
import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

public class PotentialWifiSDCard implements ICard {

    private String title;

    private InetAddress ip;

    private InetAddress netmask;

    private InetAddress router;

    private String mode;

    private String essid;

    private String id;

    public PotentialWifiSDCard(DatagramPacket response) {
        byte[] packet = new byte[response.getLength()];
        packet = Arrays.copyOfRange(response.getData(), response.getOffset(), response.getLength() + response.getOffset());
        String packetString = new String(packet, Charset.forName("UTF-8"));
        this.ip = response.getAddress();
        BufferedReader reader = new BufferedReader(new StringReader(packetString));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                int equalPos = line.indexOf('=');
                if (equalPos >= 0) {
                    String key = line.substring(0, equalPos);
                    String value = line.substring(equalPos + 1).trim();
                    if (key.equalsIgnoreCase("ip")) {
                        // this.ip = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("netmask")) {
                        this.netmask = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("router")) {
                        this.router = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("mode")) {
                        this.mode = value;
                    } else if (key.equalsIgnoreCase("essid")) {
                        this.essid = value;
                    } else if (line.indexOf(' ') < equalPos && title == null && !line.trim().isEmpty()) {
                        this.title = line.trim();
                    }
                }
            }
        } catch (IOException e) {
            // its a string so this can not happen..
            throw new IllegalStateException("io exception in strean reading?", e);
        }
        id = this.ip.getHostAddress();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("55777 type WiFiSD {");
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

    @Override
    public IBrowse browse() {
        return null;
    }

    @Override
    public IEvent event() {
        return null;
    }

    @Override
    public InetAddress ipAddress() {
        return ip;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public int level() {
        return 100;
    }

    @Override
    public String id() {
        return title;
    }

    @Override
    public void reconnect() {
        // TODO Auto-generated method stub

    }
}
