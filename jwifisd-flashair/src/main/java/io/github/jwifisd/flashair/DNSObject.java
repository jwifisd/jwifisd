package io.github.jwifisd.flashair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class DNSObject {

    public abstract void read(InputStream in) throws IOException;

    protected int readUshort(InputStream in) throws IOException {
        return (in.read() << 8) + in.read();
    }

    protected long readUint(InputStream in) throws IOException {
        return (((long) in.read()) << 24L) | (((long) in.read()) << 16L) | (((long) in.read()) << 8L) | ((long) in.read());
    }

    protected String readNameString(InputStream in) throws IOException {
        String[] strings = readStringArray(in);
        return toDomainName(strings);
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

    public InternetClassType readInternetClassType(InputStream in) throws IOException {
        return InternetClassType.internetClassType(readUshort(in));
    }

    public QClass readClass(InputStream in) throws IOException {
        int qclassInt = readUshort(in);
        return QClass.qclass(qclassInt);
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

    public long readTimeToLive(InputStream in) throws IOException {
        return readUint(in);
    }

    public String readHostInformation(InputStream in) throws IOException {
        return readNameString(in);
    }

}
