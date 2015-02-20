package io.github.jwifisd.flashair;

import java.io.IOException;
import java.io.InputStream;

public class DNSMessage {

    final DNSHeader dnsHeader = new DNSHeader();

    final DNSPayload data = new DNSPayload(dnsHeader);

    public DNSHeader getDnsHeader() {
        return dnsHeader;
    }

    public void read(InputStream in) throws IOException {
        dnsHeader.read(in);
        data.read(in);
    }

    public String getFullQualifiedDomainName() {
        return data.getFullQualifiedDomainName();
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
