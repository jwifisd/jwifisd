package io.github.jwifisd.flashair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class TestDNSMessage {

    private static final String FLASHAIR_IN_HEX = Hex.encodeHexString(new byte[]{
        (byte) "flashair".length()
    }) + Hex.encodeHexString("flashair".getBytes(Charset.forName("UTF-8")));

    static final String[] messages = {
        "000000000001000000000000" + FLASHAIR_IN_HEX + "056c6f63616c0000010001",
        "000085000001000100000000" + FLASHAIR_IN_HEX + "056c6f63616c0000ff8001c00c00018001000000780004c0a80001"
    };

    @Test
    public void testDNSDeserialisation() throws Exception {
        System.out.println(FLASHAIR_IN_HEX);
        System.out.println(Hex.encodeHex(new byte[]{
            (byte) 192,
            (byte) 168,
            (byte) 0,
            (byte) 1
        }));
        for (String message : messages) {
            String string = new String(Hex.decodeHex(message.toCharArray()), 12, 10, Charset.forName("US-ASCII"));
            InputStream in = new ByteArrayInputStream(Hex.decodeHex(message.toCharArray()));
            DNSMessage dns = new DNSMessage();
            dns.read(in);
            System.out.println(dns);
        }
    }
}
