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
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * thsi class contains the payload of the dns message. the header info is needed
 * to deserialize it.
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public class DNSPayload extends DNSObject {

    protected final DNSHeader dnsHeader;

    List<Question> questions = new LinkedList<>();

    List<Record<?>> answers = new LinkedList<>();

    List<Record<?>> nameServerAuthorities = new LinkedList<>();

    List<Record<?>> additionalRecords = new LinkedList<>();

    public static class Question extends DNSObject {

        private String fullQualifiedDomainName;

        private InternetClassType internetClassType = InternetClassType.A;

        private QClass qClass = QClass.Internet;

        public String getFullQualifiedDomainName() {
            return fullQualifiedDomainName;
        }

        public void setFullQualifiedDomainName(String fullQualifiedDomainName) {
            this.fullQualifiedDomainName = fullQualifiedDomainName;
        }

        @Override
        public void read(InputStream in) throws IOException {
            fullQualifiedDomainName = readNameString(in);
            internetClassType = InternetClassType.internetClassType(readUshort(in));
            qClass = QClass.qclass(readUshort(in));
        }

        @Override
        public void write(OutputStream out) throws IOException {
            writeNameString(out, fullQualifiedDomainName);
            writeUshort(out, internetClassType.getValue());
            writeUshort(out, qClass.getValue());

        }
    }

    public static class ServiceInformation extends DNSObject {

        int priority;

        int weight;

        int port;

        String canonicalTargetHostName;

        @Override
        public void read(InputStream in) throws IOException {
            priority = readUshort(in);
            weight = readUshort(in);
            port = readUshort(in);
            canonicalTargetHostName = readNameString(in);

        }

        @Override
        public void write(OutputStream out) throws IOException {
            writeUshort(out, priority);
            writeUshort(out, weight);
            writeUshort(out, port);
            writeNameString(out, canonicalTargetHostName);
        }

    }

    public static class Mailbox extends DNSObject {

        String userName;

        String domainName;

        @Override
        public void read(InputStream in) throws IOException {
            String[] email = readStringArray(in);
            userName = email[0];
            domainName = toDomainName(Arrays.copyOfRange(email, 1, email.length - 1));
        }

        @Override
        public void write(OutputStream out) throws IOException {
            String[] domain = fromDomainName(domainName);
            String[] all = new String[domain.length + 1];
            all[0] = userName;
            System.arraycopy(domain, 0, all, 1, domain.length);
            writeStringArray(out, all);
        }

    }

    public static class StatementOfAuthority extends DNSObject {

        private String primaryNameServerHostName;

        private Mailbox administratorMailbox = new Mailbox();

        private long serial;

        private long referesh;

        private long retry;

        private long expire;

        @Override
        public void read(InputStream in) throws IOException {
            primaryNameServerHostName = readNameString(in);
            administratorMailbox.read(in);
            serial = readUint(in);
            referesh = readUint(in);
            retry = readUint(in);
            expire = readUint(in);
        }

        @Override
        public void write(OutputStream out) throws IOException {
            writeNameString(out, primaryNameServerHostName);
            administratorMailbox.write(out);
            writeUint(out, serial);
            writeUint(out, referesh);
            writeUint(out, retry);
            writeUint(out, expire);

        }
    }

    public static class MailExchange extends DNSObject {

        int preference;

        String hostName;

        @Override
        public void read(InputStream in) throws IOException {
            preference = readUshort(in);
            hostName = readNameString(in);

        }

        @Override
        public void write(OutputStream out) throws IOException {
            writeUshort(out, preference);
            writeNameString(out, hostName);

        }
    }

    public static class Record<PAYLOAD> extends DNSObject {

        int owner;

        InternetClassType internetClassType = InternetClassType.A;

        QClass qClass = QClass.Internet;

        PAYLOAD payload;

        long timeToLive;

        @Override
        public void read(InputStream in) throws IOException {
            owner = readUshort(in);
            internetClassType = InternetClassType.internetClassType(readUshort(in));
            qClass = QClass.qclass(readUshort(in));
            timeToLive = readUint(in);
            switch (internetClassType) {
                case A:
                    payload = (PAYLOAD) readInternetProtocolVersion4Address(in);
                    break;
                case AAAA:
                    payload = (PAYLOAD) readInternetProtocolVersion6Address(in);
                    break;
                case NS:
                case TXT:
                case PTR:
                case CNAME:
                    payload = (PAYLOAD) readNameString(in);
                    break;
                case SOA:
                    StatementOfAuthority statementOfAuthority = new StatementOfAuthority();
                    statementOfAuthority.read(in);
                    payload = (PAYLOAD) statementOfAuthority;
                    break;
                case HINFO:
                    payload = (PAYLOAD) readNameString(in);
                    break;
                case MX:
                    MailExchange mailExchange = new MailExchange();
                    mailExchange.read(in);
                    payload = (PAYLOAD) mailExchange;
                    break;
                case SRV:
                    ServiceInformation info = new ServiceInformation();
                    info.read(in);
                    payload = (PAYLOAD) info;

                default:
                    break;
            }
        }

        @Override
        public void write(OutputStream out) throws IOException {
            writeUshort(out, owner);
            writeUshort(out, internetClassType.getValue());
            writeUshort(out, qClass.getValue());
            writeUint(out, timeToLive);
            switch (internetClassType) {
                case A:
                    writeInternetAddress(out, (InetAddress) payload);
                    break;
                case AAAA:
                    writeInternetAddress(out, (InetAddress) payload);
                    break;
                case NS:
                case TXT:
                case PTR:
                case CNAME:
                    writeNameString(out, (String) payload);
                    break;
                case SOA:
                    ((StatementOfAuthority) payload).write(out);
                    break;
                case HINFO:
                    writeNameString(out, (String) payload);
                    break;
                case MX:
                    ((MailExchange) payload).write(out);
                    break;
                case SRV:
                    ((ServiceInformation) payload).write(out);

                default:
                    break;
            }

        }

        public PAYLOAD getPayload() {
            return payload;
        }
    }

    public DNSPayload(DNSHeader dnsHeader) {
        this.dnsHeader = dnsHeader;
    }

    @Override
    public void read(InputStream in) throws IOException {
        int index = 0;
        while (index++ < dnsHeader.getNumberOfEntriesInQuestionSection()) {
            Question question = new Question();
            question.read(in);
            questions.add(question);
        }
        index = 0;
        while (index++ < dnsHeader.getNumberOfResourceRecordsInAnswerSection()) {
            Record<?> record = new Record();
            record.read(in);
            answers.add(record);
        }
        index = 0;
        while (index++ < dnsHeader.getNumberOfNameServerRecordsInAuthoritySection()) {
            Record<?> record = new Record();
            record.read(in);
            nameServerAuthorities.add(record);
        }
        while (index++ < dnsHeader.getNumberOfResourceRecordsInAdditionalRecordsAnswerSection()) {
            Record<?> record = new Record();
            record.read(in);
            additionalRecords.add(record);
        }

    }

    @Override
    public void write(OutputStream out) throws IOException {
        for (Question question : questions) {
            question.write(out);
        }
        for (Record<?> record : answers) {
            record.write(out);
        }
        for (Record<?> record : nameServerAuthorities) {
            record.write(out);
        }
        for (Record<?> record : additionalRecords) {
            record.write(out);
        }
    }

    public String getFullQualifiedDomainName() {
        if (dnsHeader.isResponse()) {
            answers.get(0).payload.toString();
        } else {
            questions.get(0).getFullQualifiedDomainName();
        }
        return null;
    }

    public Question getQuestion(int index) {
        return questions.get(index);
    }

    public Record<?> getAnswer(int index) {
        return answers.get(index);
    }

    public void updateHeader() {
        dnsHeader.setNumberOfEntriesInQuestionSection(questions.size());
        dnsHeader.setNumberOfResourceRecordsInAnswerSection(answers.size());
        dnsHeader.setNumberOfNameServerRecordsInAuthoritySection(nameServerAuthorities.size());
        dnsHeader.setNumberOfResourceRecordsInAdditionalRecordsAnswerSection(additionalRecords.size());

    }

    public void addQuestion(String fullQualifiedDomainName) {
        Question question = new Question();
        question.internetClassType = InternetClassType.A;
        question.qClass = QClass.Internet;
        question.fullQualifiedDomainName = fullQualifiedDomainName;
        questions.add(question);
    }

}
