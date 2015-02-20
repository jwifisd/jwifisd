package io.github.jwifisd.flashair;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DNSPayload extends DNSData {

    List<Question> questions = new LinkedList<>();

    List<Record<?>> answers = new LinkedList<>();

    List<Record<?>> nameServerAuthorities = new LinkedList<>();

    List<Record<?>> additionalRecords = new LinkedList<>();

    public static class Question extends DNSObject {

        private String fullQualifiedDomainName;

        private InternetClassType internetClassType;

        private QClass qClass;

        public String getFullQualifiedDomainName() {
            return fullQualifiedDomainName;
        }

        public void setFullQualifiedDomainName(String fullQualifiedDomainName) {
            this.fullQualifiedDomainName = fullQualifiedDomainName;
        }

        @Override
        public void read(InputStream in) throws IOException {
            fullQualifiedDomainName = readNameString(in);
            internetClassType = readInternetClassType(in);
            qClass = readClass(in);
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

    }

    public static class Mailbox extends DNSObject {

        String userName;

        String domainName;

        @Override
        public void read(InputStream in) throws IOException {
            String[] email = readStringArray(in);
            this.userName = email[0];
            this.domainName = toDomainName(Arrays.copyOfRange(email, 1, email.length - 1));
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
    }

    public static class MailExchange extends DNSObject {

        int preference;

        String hostName;

        @Override
        public void read(InputStream in) throws IOException {
            preference = readUshort(in);
            hostName = readNameString(in);

        }
    }

    public static class Record<PAYLOAD> extends DNSObject {

        int owner;

        InternetClassType internetClassType;

        QClass qClass;

        PAYLOAD payload;

        long timeToLive;

        @Override
        public void read(InputStream in) throws IOException {
            owner=readUshort(in);
            internetClassType = readInternetClassType(in);
            qClass = readClass(in);
            timeToLive = readTimeToLive(in);
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
                    payload = (PAYLOAD) readHostInformation(in);
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
    }

    public DNSPayload(DNSHeader dnsHeader) {
        super(dnsHeader);
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

    public String getFullQualifiedDomainName() {
        if (dnsHeader.isResponse) {
            answers.get(0).payload.toString();
        } else {
            questions.get(0).getFullQualifiedDomainName();
        }
        return null;
    }

}
