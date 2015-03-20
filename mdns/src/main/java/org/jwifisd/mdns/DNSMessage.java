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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * this class represents a complete dns message.
 * 
 * @author Richard van Nieuwenhoven
 */
public class DNSMessage extends DNSObject {

    /**
     * the dns header of the message.
     */
    private final DNSHeader dnsHeader = new DNSHeader();

    /**
     * the payload data of the message.
     */
    private final DNSPayload payload = new DNSPayload(dnsHeader);

    /**
     * @return the payload data of the message.
     */
    public DNSPayload getPayload() {
        return payload;
    }

    /**
     * @return the haeder data of the message.
     */
    public DNSHeader getDnsHeader() {
        return dnsHeader;
    }

    @Override
    public void read(InputStream in) throws IOException {
        dnsHeader.read(in);
        payload.read(in);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        payload.updateHeader();
        dnsHeader.write(out);
        payload.write(out);
    }

    /**
     * @return the full qualified domain name from the body.
     */
    public String getFullQualifiedDomainName() {
        return payload.getFullQualifiedDomainName();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(getClass().getSimpleName());
        result.append('(');
        result.append(getFullQualifiedDomainName());
        result.append(')');
        return result.toString();
    }
}
