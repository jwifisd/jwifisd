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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * super class for all dns message with helper methods.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class DNSObject {

    /**
     * shift bits for three bytes.
     */
    private static final long BIT_SHIFT_THREE_BYTES = 24L;

    /**
     * shift bits for two bytes.
     */
    private static final long BIT_SHIFT_TWO_BYTES = 16L;

    /**
     * shift bits for one byte.
     */
    private static final long BIT_SHIFT_ONE_BYTE = 8L;

    /**
     * bit mask for a byte.
     */
    private static final int BYTE_BIT_MASK = 0xFF;

    /**
     * deserialize this dns object from a inputStream.
     * 
     * @param in
     *            the input stream to read the message from.
     * @throws IOException
     *             if the message could not be read from the stream
     */
    public abstract void read(InputStream in) throws IOException;

    /**
     * serialize this dns object to the byte stream.
     * 
     * @param out
     *            stream to write the message to.
     * @throws IOException
     *             is the message could not be written.
     */
    public abstract void write(OutputStream out) throws IOException;

    /**
     * write an Internet ip address to the stream, 4 bytes for a ip version 4
     * address and 6 bytes for a version 6 address.
     * 
     * @param out
     *            the output stream to write to.
     * @param address
     *            the address to write.
     * @throws IOException
     *             if the output stream could not be written to.
     */
    protected void writeInternetAddress(OutputStream out, InetAddress address) throws IOException {
        final byte[] rawBytes = address.getAddress();
        writeUshort(out, rawBytes.length);
        out.write(rawBytes);
    }

    /**
     * Split a domain name into parts that are separated by dots.
     * 
     * @param domainName
     *            the domain name
     * @return the array with the splittet parts
     */
    protected String[] fromDomainName(String domainName) {
        return domainName.split("\\.");
    }

    /**
     * deserialize an version 4 ip address from the input stream.
     * 
     * @param in
     *            the input stream to read from.
     * @return the deserialized value
     * @throws IOException
     *             if the input stream could not be read from.
     */
    protected InetAddress readInternetProtocolVersion4Address(InputStream in) throws IOException {
        try {
            int size = readUshort(in);
            final byte[] rawBytes = new byte[size];
            in.read(rawBytes);
            return InetAddress.getByAddress(rawBytes);
        } catch (Exception e) {
            throw new IOException("could not read inet adress", e);
        }
    }

    /**
     * deserialize an version 6 ip adrress from the input stream.
     * 
     * @param in
     *            the input stream to read from.
     * @return the deserialized value
     * @throws IOException
     *             if the input stream could not be read from.
     */
    protected InetAddress readInternetProtocolVersion6Address(InputStream in) throws IOException {
        try {
            int size = readUshort(in);
            final byte[] rawBytes = new byte[size];
            in.read(rawBytes);
            return InetAddress.getByAddress(rawBytes);
        } catch (Exception e) {
            throw new IOException("could not read inet adress", e);
        }
    }

    /**
     * deserialize a name string from the input stream.
     * 
     * @param in
     *            the input stream to read from.
     * @return the deserialized value
     * @throws IOException
     *             if the input stream could not be read from.
     */
    protected String readNameString(InputStream in) throws IOException {
        String[] strings = readStringArray(in);
        return toDomainName(strings);
    }

    /**
     * deserialize an array of strings from the input stream.
     * 
     * @param in
     *            the input stream to read from.
     * @return the deserialized value
     * @throws IOException
     *             if the input stream could not be read from.
     */
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
                int size = byteArray[index++] & BYTE_BIT_MASK;
                result.add(new String(byteArray, index, size, Charset.forName("UTF-8")));
                index += size;
            }
            return result.toArray(new String[result.size()]);
        }
        throw new IOException("unexpected end of stream");
    }

    /**
     * deserialize a unsigned integer from the input stream.
     * 
     * @param in
     *            the input stream to read from.
     * @return the deserialized value
     * @throws IOException
     *             if the input stream could not be read from.
     */
    protected long readUint(InputStream in) throws IOException {
        return (long) in.read() << BIT_SHIFT_THREE_BYTES | //
                (long) in.read() << BIT_SHIFT_TWO_BYTES | //
                (long) in.read() << BIT_SHIFT_ONE_BYTE | //
                in.read();
    }

    /**
     * deserialize an unsigned short from the input stream.
     * 
     * @param in
     *            the input stream to read from.
     * @return the deserialized value
     * @throws IOException
     *             if the input stream could not be read from.
     */
    protected int readUshort(InputStream in) throws IOException {
        return (in.read() << BIT_SHIFT_ONE_BYTE) + in.read();
    }

    /**
     * convert an array of strings to a domain name.
     * 
     * @param strings
     *            the array of strings to convert
     * @return the domain name string
     */
    protected String toDomainName(String[] strings) {
        StringBuffer result = new StringBuffer();
        for (String string : strings) {
            if (result.length() != 0) {
                result.append('.');
            }
            result.append(string);
        }
        return result.toString();
    }

    /**
     * serialize a name string to the output stream.
     * 
     * @param out
     *            the output stream to serialize to
     * @param domainName
     *            the value to serialize
     * @throws IOException
     *             if the value could not be written to the output stream
     */
    protected void writeNameString(OutputStream out, String domainName) throws IOException {
        writeStringArray(out, fromDomainName(domainName));
    }

    /**
     * serialize a string array to the output stream.
     * 
     * @param out
     *            the output stream to serialize to
     * @param strings
     *            the value to serialize
     * @throws IOException
     *             if the value could not be written to the output stream
     */
    protected void writeStringArray(OutputStream out, String[] strings) throws IOException {
        for (String string : strings) {
            byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
            out.write(bytes.length);
            out.write(bytes);
        }
        out.write(0);
    }

    /**
     * serialize a unsigned integer to the output stream.
     * 
     * @param out
     *            the output stream to serialize to
     * @param value
     *            the value to serialize
     * @throws IOException
     *             if the value could not be written to the output stream
     */
    protected void writeUint(OutputStream out, long value) throws IOException {
        out.write((int) (value >> BIT_SHIFT_THREE_BYTES & BYTE_BIT_MASK));
        out.write((int) (value >> BIT_SHIFT_TWO_BYTES & BYTE_BIT_MASK));
        out.write((int) (value >> BIT_SHIFT_ONE_BYTE & BYTE_BIT_MASK));
        out.write((int) (value & BYTE_BIT_MASK));
    }

    /**
     * serialize a unsigned short to the output stream.
     * 
     * @param out
     *            the output stream to serialize to
     * @param value
     *            the value to serialize
     * @throws IOException
     *             if the value could not be written to the output stream
     */
    protected void writeUshort(OutputStream out, int value) throws IOException {
        out.write(value >> BIT_SHIFT_ONE_BYTE & BYTE_BIT_MASK);
        out.write(value & BYTE_BIT_MASK);
    }

}
