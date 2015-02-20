package io.github.jwifisd.flashair;

import java.io.IOException;
import java.io.InputStream;

public abstract class DNSData extends DNSObject {

    protected final DNSHeader dnsHeader;

    public DNSData(DNSHeader dnsHeader) {
        this.dnsHeader = dnsHeader;
    }


}
