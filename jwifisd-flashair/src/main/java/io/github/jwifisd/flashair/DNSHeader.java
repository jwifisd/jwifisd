package io.github.jwifisd.flashair;

import java.io.IOException;
import java.io.InputStream;

public class DNSHeader extends DNSObject {

    private int messageId;

    private int messageHeaderFlags;

    private int numberOfEntriesInQuestionSection;

    private int numberOfResourceRecordsInAnswerSection;

    public int getMessageId() {
        return messageId;
    }

    public int getMessageHeaderFlags() {
        return messageHeaderFlags;
    }

    public int getNumberOfEntriesInQuestionSection() {
        return numberOfEntriesInQuestionSection;
    }

    public int getNumberOfResourceRecordsInAnswerSection() {
        return numberOfResourceRecordsInAnswerSection;
    }

    public int getNumberOfNameServerRecordsInAuthoritySection() {
        return numberOfNameServerRecordsInAuthoritySection;
    }

    public int getNumberOfResourceRecordsInAdditionalRecordsAnswerSection() {
        return numberOfResourceRecordsInAdditionalRecordsAnswerSection;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setMessageHeaderFlags(int messageHeaderFlags) {
        this.messageHeaderFlags = messageHeaderFlags;
    }

    public void setNumberOfEntriesInQuestionSection(int numberOfEntriesInQuestionSection) {
        this.numberOfEntriesInQuestionSection = numberOfEntriesInQuestionSection;
    }

    public void setNumberOfResourceRecordsInAnswerSection(int numberOfResourceRecordsInAnswerSection) {
        this.numberOfResourceRecordsInAnswerSection = numberOfResourceRecordsInAnswerSection;
    }

    public void setNumberOfNameServerRecordsInAuthoritySection(int numberOfNameServerRecordsInAuthoritySection) {
        this.numberOfNameServerRecordsInAuthoritySection = numberOfNameServerRecordsInAuthoritySection;
    }

    public void setNumberOfResourceRecordsInAdditionalRecordsAnswerSection(int numberOfResourceRecordsInAdditionalRecordsAnswerSection) {
        this.numberOfResourceRecordsInAdditionalRecordsAnswerSection = numberOfResourceRecordsInAdditionalRecordsAnswerSection;
    }

    private int numberOfNameServerRecordsInAuthoritySection;

    private int numberOfResourceRecordsInAdditionalRecordsAnswerSection;

    public void read(InputStream in) throws IOException {
        messageId = readUshort(in);
        messageHeaderFlags = readUshort(in);
        numberOfEntriesInQuestionSection = readUshort(in);
        numberOfResourceRecordsInAnswerSection = readUshort(in);
        numberOfNameServerRecordsInAuthoritySection = readUshort(in);
        numberOfResourceRecordsInAdditionalRecordsAnswerSection = readUshort(in);
        readMessageHeaderFlags();
    }

    enum OperationCode {
        Query(0),
        InverseQuery(1),
        Status(2);

        private final int unsigned4BitInteger;

        private OperationCode(int unsigned4BitInteger) {
            this.unsigned4BitInteger = unsigned4BitInteger;
        }

        public static OperationCode operationCode(int unsigned4BitInteger) {
            for (OperationCode operationCode : values()) {
                if (operationCode.unsigned4BitInteger == unsigned4BitInteger) {
                    return operationCode;
                }
            }
            throw new IllegalArgumentException("Unknown operationcode");
        }
    }

    enum ResponseCode {
        NoErrorCondition(0, false),
        FormatError(1, true),
        ServerFailure(2, true),
        NameError(3, true),
        NotImplemented(4, true),
        Refused(5, true);

        private final int unsigned4BitInteger;

        private final boolean isError;

        private ResponseCode(int unsigned4BitInteger, final boolean isError) {
            this.unsigned4BitInteger = unsigned4BitInteger;
            this.isError = isError;
        }

        public static ResponseCode responseCode(int unsigned4BitInteger) {
            for (ResponseCode responseCode : values()) {
                if (responseCode.unsigned4BitInteger == unsigned4BitInteger) {
                    return responseCode;
                }
            }
            throw new IllegalArgumentException("No ResponseCode known");
        }

        public boolean isError() {
            return isError;
        }
    }

    boolean isResponse;

    OperationCode operationCode;

    boolean authoritativeAnswer;

    boolean truncation;

    boolean recursionDesired;

    boolean recursionAvailable;

    int z;

    ResponseCode responseCode;

    public void readMessageHeaderFlags() {
        isResponse /*                           */= (messageHeaderFlags & 0b1000000000000000) != 0;
        operationCode = OperationCode.operationCode((messageHeaderFlags & 0b0111100000000000) >> 11);
        authoritativeAnswer = /*                  */(messageHeaderFlags & 0b0000010000000000) != 0;
        truncation = /*                           */(messageHeaderFlags & 0b0000001000000000) != 0;
        recursionDesired = /*                     */(messageHeaderFlags & 0b0000000100000000) != 0;
        recursionAvailable = /*                   */(messageHeaderFlags & 0b0000000010000000) != 0;
        z = /*                                    */(messageHeaderFlags & 0b0000000001110000) >> 4;
        responseCode = /**/ResponseCode.responseCode(messageHeaderFlags & 0b0000000000001111);
    }
}
