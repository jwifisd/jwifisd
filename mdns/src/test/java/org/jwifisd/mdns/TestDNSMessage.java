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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.jwifisd.mdns.DNSMessage;

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
        int index = 0;
        for (String message : messages) {
            String string = new String(Hex.decodeHex(message.toCharArray()), 12, 10, Charset.forName("US-ASCII"));
            InputStream in = new ByteArrayInputStream(Hex.decodeHex(message.toCharArray()));
            DNSMessage dns = new DNSMessage();
            dns.read(in);
            Assert.assertEquals("flashair.local", dns.getPayload().getQuestion(0).getFullQualifiedDomainName());

            if (index > 0) {
                Assert.assertEquals(1, dns.getDnsHeader().getNumberOfResourceRecordsInAnswerSection());
                Assert.assertEquals("/192.168.0.1", dns.getPayload().getAnswer(0).getPayload().toString());
            }
            index++;
        }
    }

    @Test
    public void testDNSSerialisation() throws Exception {
        int index = 0;
        for (String message : messages) {
            String string = new String(Hex.decodeHex(message.toCharArray()), 12, 10, Charset.forName("US-ASCII"));
            InputStream in = new ByteArrayInputStream(Hex.decodeHex(message.toCharArray()));
            DNSMessage dns = new DNSMessage();
            dns.read(in);
            Assert.assertEquals("flashair.local", dns.getPayload().getQuestion(0).getFullQualifiedDomainName());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            dns.write(out);
            Assert.assertEquals(message, Hex.encodeHexString(out.toByteArray()));

        }
    }

    @Test
    public void testManualDNSQuery() throws Exception {
        DNSMessage message = new DNSMessage();
        message.getDnsHeader().setResponse(false);
        message.getPayload().addQuestion("flashair.local");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.write(out);
        Assert.assertEquals(messages[0], Hex.encodeHexString(out.toByteArray()));

    }

    @Test
    public void testStrangeMessage() throws Exception {
        // TODO implement this message (should not throw an exception...
        String message = "000000000002000000000000055f69707073045f746370056c6f63616c00000c0001045f697070c012000c0001a800010000010001000000000000";
    }

}
