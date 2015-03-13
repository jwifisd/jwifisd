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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

        private final int value;

        private OperationCode(int unsigned4BitInteger) {
            this.value = unsigned4BitInteger;
        }

        public static OperationCode operationCode(int unsigned4BitInteger) {
            for (OperationCode operationCode : values()) {
                if (operationCode.value == unsigned4BitInteger) {
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

        private final int value;

        private final boolean isError;

        private ResponseCode(int unsigned4BitInteger, final boolean isError) {
            this.value = unsigned4BitInteger;
            this.isError = isError;
        }

        public static ResponseCode responseCode(int unsigned4BitInteger) {
            for (ResponseCode responseCode : values()) {
                if (responseCode.value == unsigned4BitInteger) {
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

    public void writeMessageHeaderFlags() {
        messageHeaderFlags = 0;
        if (isResponse) {
            messageHeaderFlags = messageHeaderFlags | 0b1000000000000000;
        }
        if (operationCode != null) {
            messageHeaderFlags = messageHeaderFlags | ((operationCode.value << 11) & 0b0111100000000000);
        }
        if (authoritativeAnswer) {
            messageHeaderFlags = messageHeaderFlags | 0b0000010000000000;
        }
        if (truncation) {
            messageHeaderFlags = messageHeaderFlags | 0b0000001000000000;
        }
        if (recursionDesired) {
            messageHeaderFlags = messageHeaderFlags | 0b0000000100000000;
        }
        if (recursionAvailable) {
            messageHeaderFlags = messageHeaderFlags | 0b0000000010000000;
        }
        messageHeaderFlags = messageHeaderFlags | ((z << 4) & 0b0000000001110000);
        if (responseCode != null) {
            messageHeaderFlags = messageHeaderFlags | (responseCode.value & 0b0000000000001111);

        }
    }

    public void write(OutputStream out) throws IOException {
        writeMessageHeaderFlags();
        writeUshort(out, messageId);
        writeUshort(out, messageHeaderFlags);
        writeUshort(out, numberOfEntriesInQuestionSection);
        writeUshort(out, numberOfResourceRecordsInAnswerSection);
        writeUshort(out, numberOfNameServerRecordsInAuthoritySection);
        writeUshort(out, numberOfResourceRecordsInAdditionalRecordsAnswerSection);
    }

    public boolean isResponse() {
        return isResponse;
    }

    public void setResponse(boolean value) {
        isResponse = value;
    }
}
