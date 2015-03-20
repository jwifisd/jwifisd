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

    /**
     * mailbox (= email address) object of a dns message.
     */
    public static class Mailbox extends DNSObject {

        /**
         * the domain name of the enail address.
         */
        private String domainName;

        /**
         * the name part of the email address.
         */
        private String userName;

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

    /**
     * Mail excange server information.
     */
    public static class MailExchange extends DNSObject {

        /**
         * the mail exchange host name.
         */
        private String hostName;

        /**
         * the mail excange preference.
         */
        private int preference;

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

    /**
     * dns Question representation.
     */
    public static class Question extends DNSObject {

        /**
         * the full qualified domain name.
         */
        private String fullQualifiedDomainName;

        /**
         * the internet class type (normally A = ip adress).
         */
        private InternetClassType internetClassType = InternetClassType.A;

        /**
         * the Q class of the question (normally Integet).
         */
        private QClass qClass = QClass.Internet;

        /**
         * @return the full qualified domain name.
         */
        public String getFullQualifiedDomainName() {
            return fullQualifiedDomainName;
        }

        @Override
        public void read(InputStream in) throws IOException {
            fullQualifiedDomainName = readNameString(in);
            internetClassType = InternetClassType.internetClassType(readUshort(in));
            qClass = QClass.qclass(readUshort(in));
        }

        /**
         * set the the full qualified domain name.
         * 
         * @param fullQualifiedDomainName
         *            the value to set.
         */
        public void setFullQualifiedDomainName(String fullQualifiedDomainName) {
            this.fullQualifiedDomainName = fullQualifiedDomainName;
        }

        @Override
        public void write(OutputStream out) throws IOException {
            writeNameString(out, fullQualifiedDomainName);
            writeUshort(out, internetClassType.getValue());
            writeUshort(out, qClass.getValue());

        }
    }

    /**
     * response record of a dns message.
     * 
     * @param <PAYLOAD>
     *            the contens of the response.
     */
    public static class Record<PAYLOAD> extends DNSObject {

        /**
         * the internet class type of the record.
         */
        private InternetClassType internetClassType = InternetClassType.A;

        /**
         * the owner index of the record.
         */
        private int owner;

        /**
         * the payload of the answer.
         */
        private PAYLOAD payload;

        /**
         * the qClass of the record.
         */
        private QClass qClass = QClass.Internet;

        /**
         * the time this record has to live.
         */
        private long timeToLive;

        /**
         * 
         * @return the payload of the answer.
         */
        public PAYLOAD getPayload() {
            return payload;
        }

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
    }

    /**
     * The service information record payload.
     */
    public static class ServiceInformation extends DNSObject {

        /**
         * the canonical target host name.
         */
        private String canonicalTargetHostName;

        /**
         * the port number of the service.
         */
        private int port;

        /**
         * the priority of the service.
         */
        private int priority;

        /**
         * the weight of the service.
         */
        private int weight;

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

    /**
     * Statement of authority record payload.
     */
    public static class StatementOfAuthority extends DNSObject {

        /**
         * the administrator mailbox information.
         */
        private Mailbox administratorMailbox = new Mailbox();

        /**
         * the expiration time.
         */
        private long expire;

        /**
         * the primary name server host.
         */
        private String primaryNameServerHostName;

        /**
         * the refresh interfall.
         */
        private long referesh;

        /**
         * the retry count.
         */
        private long retry;

        /**
         * the serial number.
         */
        private long serial;

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

    /**
     * the list of additional records in this message.
     */
    private List<Record<?>> additionalRecords = new LinkedList<>();

    /**
     * the list of answers in this message.
     */
    private List<Record<?>> answers = new LinkedList<>();

    /**
     * the dns header for needed infos.
     */
    private final DNSHeader dnsHeader;

    /**
     * the list of name server authorities in this message.
     */
    private List<Record<?>> nameServerAuthorities = new LinkedList<>();

    /**
     * the list of questions in this message.
     */
    private List<Question> questions = new LinkedList<>();

    /**
     * constructor for the payload using the header and information source.
     * 
     * @param dnsHeader
     *            the dns header
     */
    public DNSPayload(DNSHeader dnsHeader) {
        this.dnsHeader = dnsHeader;
    }

    /**
     * add a question for a domain name to the payload.
     * 
     * @param fullQualifiedDomainName
     *            the full qualified name to search.
     */
    public void addQuestion(String fullQualifiedDomainName) {
        Question question = new Question();
        question.internetClassType = InternetClassType.A;
        question.qClass = QClass.Internet;
        question.fullQualifiedDomainName = fullQualifiedDomainName;
        questions.add(question);
    }

    /**
     * get the payload answer with index.
     * 
     * @param index
     *            the answer index to return.
     * @return the answer at the specified inde.
     */
    public Record<?> getAnswer(int index) {
        return answers.get(index);
    }

    /**
     * @return the full qualified domain name.
     */
    public String getFullQualifiedDomainName() {
        if (dnsHeader.isResponse()) {
            answers.get(0).payload.toString();
        } else {
            questions.get(0).getFullQualifiedDomainName();
        }
        return null;
    }

    /**
     * get the question with the specified index.
     * 
     * @param index
     *            the index of the question
     * @return the question with the specified index.
     */
    public Question getQuestion(int index) {
        return questions.get(index);
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

    /**
     * update the header info accourding to the number of elelments ind the
     * differend collections.
     */
    public void updateHeader() {
        dnsHeader.setNumberOfEntriesInQuestionSection(questions.size());
        dnsHeader.setNumberOfResourceRecordsInAnswerSection(answers.size());
        dnsHeader.setNumberOfNameServerRecordsInAuthoritySection(nameServerAuthorities.size());
        dnsHeader.setNumberOfResourceRecordsInAdditionalRecordsAnswerSection(additionalRecords.size());

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

}
