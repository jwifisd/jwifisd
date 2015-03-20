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
/**
 * the internat class type used to describe the dns records.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum InternetClassType {
    /**
     * a host address (ip v4).
     */
    A(1, "a host address"),
    /**
     * an authoritative name server.
     */
    NS(2, "an authoritative name server"),
    /**
     * a mail destination (Obsolete - use MX).
     */
    MD(3, "a mail destination (Obsolete - use MX)"),
    /**
     * a mail forwarder (Obsolete - use MX).
     */
    MF(4, "a mail forwarder (Obsolete - use MX)"),
    /**
     * the canonical name for an alias.
     */
    CNAME(5, "the canonical name for an alias"),
    /**
     * marks the start of a zone of authority.
     */
    SOA(6, "marks the start of a zone of authority"),
    /**
     * a mailbox domain name (EXPERIMENTAL).
     */
    MB(7, "a mailbox domain name (EXPERIMENTAL)"),
    /**
     * a mail group member (EXPERIMENTAL).
     */
    MG(8, "a mail group member (EXPERIMENTAL)"),
    /**
     * a mail rename domain name (EXPERIMENTAL).
     */
    MR(9, "a mail rename domain name (EXPERIMENTAL"),
    /**
     * a null RR (EXPERIMENTAL).
     */
    NULL(10, "a null RR (EXPERIMENTAL)"),
    /**
     * a well known service description.
     */
    WKS(11, "a well known service description"),
    /**
     * a domain name pointer.
     */
    PTR(12, "a domain name pointer"),
    /**
     * host information.
     */
    HINFO(13, "host information"),
    /**
     * mailbox or mail list information.
     */
    MINFO(14, "mailbox or mail list information"),
    /**
     * mail exchange.
     */
    MX(15, "mail exchange"),
    /**
     * text strings.
     */
    TXT(16, "text strings"),
    /**
     * for Responsible Person.
     */
    RP(17, "for Responsible Person"),
    /**
     * AFS Data Base location.
     */
    AFSDB(18, "for AFS Data Base location"),
    /**
     * for X.25 PSDN address.
     */
    X25(19, "for X.25 PSDN address"),
    /**
     * for ISDN address.
     */
    ISDN(20, "for ISDN address"),
    /**
     * for Route Through.
     */
    RT(21, "for Route Through"),
    /**
     * for NSAP address, NSAP style A record.
     */
    NSAP(22, "for NSAP address, NSAP style A record"),
    /**
     * (Unknown).
     */
    NSAP_PTR(23, "(Unknown)"),
    /**
     * for security signature.
     */
    SIG(24, "for security signature"),
    /**
     * for security key.
     */
    KEY(25, "for security key"),
    /**
     * X.400 mail mapping information.
     */
    PX(26, "X.400 mail mapping information"),
    /**
     * Geographical Position.
     */
    GPOS(27, "Geographical Position"),
    /**
     * IP6 Address.
     */
    AAAA(28, "IP6 Address"),
    /**
     * Location Information.
     */
    LOC(29, "Location Information"),
    /**
     * Next DomainName - OBSOLETE.
     */
    NXT(30, "Next DomainName - OBSOLETE"),
    /**
     * Endpoint Identifier.
     */
    EID(31, "Endpoint Identifier"),
    /**
     * Nimrod Locator.
     */
    NIMLOC(32, "Nimrod Locator"),
    /**
     * Server Selection.
     */
    SRV(33, "Server Selection"),
    /**
     * ATM Address.
     */
    ATMA(34, "ATM Address"),
    /**
     * Naming Authority Pointer.
     */
    NAPTR(35, "Naming Authority Pointer"),
    /**
     * Key Exchanger.
     */
    KX(36, "Key Exchanger"),
    /**
     * CERT.
     */
    CERT(37, "CERT"),
    /**
     * A6.
     */
    A6(38, "A6"),
    /**
     * DNAME.
     */
    DNAME(39, "DNAME"),
    /**
     * SINK.
     */
    SINK(40, "SINK"),
    /**
     * OPT.
     */
    OPT(41, "OPT"),
    /**
     * APL.
     */
    APL(42, "APL"),
    /**
     * Delegation Signer.
     */
    DS(43, "Delegation Signer"),
    /**
     * SSH Key Fingerprint.
     */
    SSHFP(44, "SSH Key Fingerprint"),
    /**
     * IPSECKEY.
     */
    IPSECKEY(45, "IPSECKEY"),
    /**
     * RRSIG.
     */
    RRSIG(46, "RRSIG"),
    /**
     * NSEC.
     */
    NSEC(47, "NSEC"),
    /**
     * DNSKEY.
     */
    DNSKEY(48, "DNSKEY"),
    /**
     * DHCID.
     */
    DHCID(49, "DHCID"),
    /**
     * NSEC3.
     */
    NSEC3(50, "NSEC3"),
    /**
     * NSEC3PARAM.
     */
    NSEC3PARAM(51, "NSEC3PARAM"),
    /**
     * HostName Identity Protocol.
     */
    HIP(55, "HostName Identity Protocol"),
    /**
     * (Unknown).
     */
    SPF(99, "(Unknown)"),
    /**
     * (Unknown).
     */
    UINFO(100, "(Unknown)"),
    /**
     * (Unknown).
     */
    UID(101, "(Unknown)"),
    /**
     * (Unknown).
     */
    GID(102, "(Unknown)"),
    /**
     * (Unknown).
     */
    UNSPEC(103, "(Unknown)"),
    /**
     * Transaction Key.
     */
    TKEY(249, "Transaction Key"),
    /**
     * Transaction Signature.
     */
    TSIG(250, "Transaction Signature"),
    /**
     * incremental transfer.
     */
    IXFR(251, "incremental transfer"),

    // Stictly speaking, these are QTYPE not TYPE and are only included in the
    // superset
    /**
     * transfer of an entire zone.
     */
    AXFR(252, "transfer of an entire zone"),
    /**
     * mailbox-related RRs (MB, MG or MR).
     */
    MAILB(253, "mailbox-related RRs (MB, MG or MR)"),
    /**
     * mail agent RRs (Obsolete - see MX).
     */
    MAILA(254, "mail agent RRs (Obsolete - see MX)"),
    /**
     * A request for all records.
     */
    Asterisk(255, "A request for all records"),
    /**
     * DNSSEC Trust Authorities.
     */
    TA(32768, "DNSSEC Trust Authorities"),
    /**
     * DNSSEC Lookaside Validation.
     */
    DLV(32769, "DNSSEC Lookaside Validation");

    /**
     * the mdns value of the internet class type.
     */
    private final int value;

    /**
     * @return the mdns value of the internet class type.
     */
    public int getValue() {
        return value;
    }

    /**
     * construct an internet class type.
     * 
     * @param value
     *            the mdns value of the class type.
     * @param description
     *            the description of the class type.
     */
    private InternetClassType(int value, String description) {
        this.value = value;
    }

    /**
     * find an internet class type with the specified value.
     * 
     * @param value
     *            the mdns value to search for.
     * @return the found class type of exception if it was not found.
     */
    public static InternetClassType internetClassType(int value) {
        for (InternetClassType internetClassType : values()) {
            if (internetClassType.value == value) {
                return internetClassType;
            }
        }
        throw new IllegalArgumentException("Unrecognised internet class type code ");
    }

}
