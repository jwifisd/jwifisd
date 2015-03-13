package org.jwifisd.mdns;

/*
 * #%L
 * jwifisd-mdns
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class DNSObject {

    public abstract void read(InputStream in) throws IOException;

    public abstract void write(OutputStream out) throws IOException;

    protected int readUshort(InputStream in) throws IOException {
        return (in.read() << 8) + in.read();
    }

    protected void writeUshort(OutputStream out, int value) throws IOException {
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    protected long readUint(InputStream in) throws IOException {
        return (((long) in.read()) << 24L) | (((long) in.read()) << 16L) | (((long) in.read()) << 8L) | ((long) in.read());
    }

    protected void writeUint(OutputStream out, long value) throws IOException {
        out.write((int) ((value >> 24) & 0xFF));
        out.write((int) ((value >> 16) & 0xFF));
        out.write((int) ((value >> 8) & 0xFF));
        out.write((int) (value & 0xFF));
    }

    protected String readNameString(InputStream in) throws IOException {
        String[] strings = readStringArray(in);
        return toDomainName(strings);
    }

    protected void writeNameString(OutputStream out, String domainName) throws IOException {
        writeStringArray(out, fromDomainName(domainName));
    }

    protected String[] fromDomainName(String domainName) throws IOException {
        return domainName.split("\\.");
    }

    protected String toDomainName(String[] strings) throws IOException {
        StringBuffer result = new StringBuffer();
        for (String string : strings) {
            if (result.length() != 0) {
                result.append('.');
            }
            result.append(string);
        }
        return result.toString();
    }

    protected void writeStringArray(OutputStream out, String[] strings) throws IOException {
        for (String string : strings) {
            byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
            out.write(bytes.length);
            out.write(bytes);
        }
        out.write(0);
    }

    protected String[] readStringArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int current;
        while ((current = in.read()) > 0) {
            out.write(current);
        }
        if (current == 0) {
            List<String> result = new ArrayList<>();
            byte[] byteArray = out.toByteArray();
            int index = 0;
            while (index < byteArray.length) {
                int size = byteArray[index++] & 0xFF;
                result.add(new String(byteArray, index, size, Charset.forName("UTF-8")));
                index += size;
            }
            return result.toArray(new String[result.size()]);
        }
        throw new IOException("unexpected end of stream");
    }

    public void writeInternetAddress(OutputStream out, InetAddress address) throws IOException {
        final byte[] rawBytes = address.getAddress();
        writeUshort(out, rawBytes.length);
        out.write(rawBytes);
    }

    public InetAddress readInternetProtocolVersion4Address(InputStream in) throws IOException {
        try {
            int size = readUshort(in);
            final byte[] rawBytes = new byte[size];
            in.read(rawBytes);
            return Inet4Address.getByAddress(rawBytes);
        } catch (Exception e) {
            throw new IOException("could not read inet adress", e);
        }
    }

    public InetAddress readInternetProtocolVersion6Address(InputStream in) throws IOException {
        try {
            int size = readUshort(in);
            final byte[] rawBytes = new byte[size];
            in.read(rawBytes);
            return Inet6Address.getByAddress(rawBytes);
        } catch (Exception e) {
            throw new IOException("could not read inet adress", e);
        }
    }

}
