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

public enum InternetClassType {
    A(1, "a host address"),
    NS(2, "an authoritative name server"),
    MD(3, "a mail destination (Obsolete - use MX)"),
    MF(4, "a mail forwarder (Obsolete - use MX)"),
    CNAME(5, "the canonical name for an alias"),
    SOA(6, "marks the start of a zone of authority"),
    MB(7, "a mailbox domain name (EXPERIMENTAL)"),
    MG(8, "a mail group member (EXPERIMENTAL)"),
    MR(9, "a mail rename domain name (EXPERIMENTAL"),
    NULL(10, "a null RR (EXPERIMENTAL)"),
    WKS(11, "a well known service description"),
    PTR(12, "a domain name pointer"),
    HINFO(13, "host information"),
    MINFO(14, "mailbox or mail list information"),
    MX(15, "mail exchange"),
    TXT(16, "text strings"),
    RP(17, "for Responsible Person"),
    AFSDB(18, "for AFS Data Base location"),
    X25(19, "for X.25 PSDN address"),
    ISDN(20, "for ISDN address"),
    RT(21, "for Route Through"),
    NSAP(22, "for NSAP address, NSAP style A record"),
    NSAP_PTR(23, "(Unknown)"),
    SIG(24, "for security signature"),
    KEY(25, "for security key"),
    PX(26, "X.400 mail mapping information"),
    GPOS(27, "Geographical Position"),
    AAAA(28, "IP6 Address"),
    LOC(29, "Location Information"),
    NXT(30, "Next DomainName - OBSOLETE"),
    EID(31, "Endpoint Identifier"),
    NIMLOC(32, "Nimrod Locator"),
    SRV(33, "Server Selection"),
    ATMA(34, "ATM Address"),
    NAPTR(35, "Naming Authority Pointer"),
    KX(36, "Key Exchanger"),
    CERT(37, "CERT"),
    A6(38, "A6"),
    DNAME(39, "DNAME"),
    SINK(40, "SINK"),
    OPT(41, "OPT"),
    APL(42, "APL"),
    DS(43, "Delegation Signer"),
    SSHFP(44, "SSH Key Fingerprint"),
    IPSECKEY(45, "IPSECKEY"),
    RRSIG(46, "RRSIG"),
    NSEC(47, "NSEC"),
    DNSKEY(48, "DNSKEY"),
    DHCID(49, "DHCID"),
    NSEC3(50, "NSEC3"),
    NSEC3PARAM(51, "NSEC3PARAM"),
    HIP(55, "HostName Identity Protocol"),
    SPF(99, "(Unknown)"),
    UINFO(100, "(Unknown)"),
    UID(101, "(Unknown)"),
    GID(102, "(Unknown)"),
    UNSPEC(103, "(Unknown)"),
    TKEY(249, "Transaction Key"),
    TSIG(250, "Transaction Signature"),
    IXFR(251, "incremental transfer"),

    // Stictly speaking, these are QTYPE not TYPE and are only included in the
    // superset
    AXFR(252, "transfer of an entire zone"),
    MAILB(253, "mailbox-related RRs (MB, MG or MR)"),
    MAILA(254, "mail agent RRs (Obsolete - see MX)"),
    Asterisk(255, "A request for all records"),

    TA(32768, "DNSSEC Trust Authorities"),
    DLV(32769, "DNSSEC Lookaside Validation");

    private final int value;

    public int getValue() {
        return value;
    }

    private final String description;

    private InternetClassType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static InternetClassType internetClassType(int value) {
        for (InternetClassType internetClassType : values()) {
            if (internetClassType.value == value) {
                return internetClassType;
            }
        }
        throw new IllegalArgumentException("Unrecognised internet class type code ");
    }

}
