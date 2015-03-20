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
 * message header of a mdns message.
 * 
 * @author Richard van Nieuwenhoven
 */
public class DNSHeader extends DNSObject {

    /**
     * In both multicast query and multicast response messages, the OPCODE
     * MUST be zero on transmission (only standard queries are currently
     * supported over multicast). Multicast DNS messages received with an
     * OPCODE other than zero MUST be silently ignored.
     */
    enum OperationCode {
        /**
         * inverse query operation.
         */
        InverseQuery(1),
        /**
         * normal query operation.
         */
        Query(0),
        /**
         * status request operation.
         */
        Status(2);

        /**
         * the value in the mdns header.
         */
        private final int value;

        /**
         * Constructor for the Operation code.
         * 
         * @param operationCodeToSet
         *            operation code to set
         */
        private OperationCode(int operationCodeToSet) {
            value = operationCodeToSet;
        }

        /**
         * find the operation code for the given value.
         * 
         * @param operationCodeToSearch
         *            the value to search for
         * @return the operation code or null when not found
         */
        public static OperationCode operationCode(int operationCodeToSearch) {
            for (OperationCode operationCode : values()) {
                if (operationCode.value == operationCodeToSearch) {
                    return operationCode;
                }
            }
            throw new IllegalArgumentException("Unknown operationcode");
        }
    }

    /**
     * In both multicast query and multicast response messages, the Response
     * Code MUST be zero on transmission. Multicast DNS messages received
     * with non-zero Response Codes MUST be silently ignored.
     */
    enum ResponseCode {
        /**
         * formatting error.
         */
        FormatError(1, true),
        /**
         * name error.
         */
        NameError(3, true),
        /**
         * no error condition.
         */
        NoErrorCondition(0, false),
        /**
         * not implemented.
         */
        NotImplemented(4, true),
        /**
         * refused.
         */
        Refused(5, true),
        /**
         * server failuer.
         */
        ServerFailure(2, true);

        /**
         * is this an error response.
         */
        private final boolean isError;

        /**
         * the response code value.
         */
        private final int value;

        /**
         * Constructor for a response code.
         * 
         * @param responseCodeValue
         *            the value to use.
         * @param isError
         *            is this response an error response?
         */
        private ResponseCode(int responseCodeValue, final boolean isError) {
            value = responseCodeValue;
            this.isError = isError;
        }

        /**
         * search a response with a special response code.
         * 
         * @param responseCodeValueToSearch
         *            the value to search for
         * @return the found response code or an exception if it was not found
         */
        public static ResponseCode responseCode(int responseCodeValueToSearch) {
            for (ResponseCode responseCode : values()) {
                if (responseCode.value == responseCodeValueToSearch) {
                    return responseCode;
                }
            }
            throw new IllegalArgumentException("No ResponseCode known");
        }

        /**
         * @return true if the response code represents an error.
         */
        public boolean isError() {
            return isError;
        }
    }

    /**
     * In query messages, the Authoritative Answer bit MUST be zero on
     * transmission, and MUST be ignored on reception.
     * 
     * In response messages for Multicast domains, the Authoritative Answer
     * bit MUST be set to one (not setting this bit would imply there's some
     * other place where "better" information may be found) and MUST be
     * ignored on reception.
     */
    private static final int MESSAGE_HEADER_AUTHORITATIVE_ANSWER_BIT = 0b0000010000000000;

    /**
     * bit shift for {@link #MESSAGE_HEADER_OPERATION_CODE_BITS}.
     */
    private static final int MESSAGE_HEADER_OPERATION_CODE_BIT_SHIFT = 11;

    /**
     * In both multicast query and multicast response messages, the OPCODE
     * MUST be zero on transmission (only standard queries are currently
     * supported over multicast). Multicast DNS messages received with an
     * OPCODE other than zero MUST be silently ignored.
     */
    private static final int MESSAGE_HEADER_OPERATION_CODE_BITS = 0b0111100000000000;

    /**
     * In both multicast query and multicast response messages, the
     * Recursion Available bit MUST be zero on transmission, and MUST be
     * ignored on reception.
     */
    private static final int MESSAGE_HEADER_RECURSION_AVAILABLE_BIT = 0b0000000010000000;

    /**
     * In both multicast query and multicast response messages, the
     * Recursion Desired bit SHOULD be zero on transmission, and MUST be
     * ignored on reception.
     */
    private static final int MESSAGE_HEADER_RECURSION_DESIRED_BIT = 0b0000000100000000;

    /**
     * In query messages the QR bit MUST be zero.
     * In response messages the QR bit MUST be one.
     */
    private static final int MESSAGE_HEADER_RESPONSE_BIT = 0b1000000000000000;

    /**
     * In both multicast query and multicast response messages, the Response
     * Code MUST be zero on transmission. Multicast DNS messages received
     * with non-zero Response Codes MUST be silently ignored.
     */
    private static final int MESSAGE_HEADER_RESPONSE_BITS = 0b0000000000001111;

    /**
     * In query messages, if the TC bit is set, it means that additional
     * Known-Answer records may be following shortly. A responder SHOULD
     * record this fact, and wait for those additional Known-Answer records,
     * before deciding whether to respond. If the TC bit is clear, it means
     * that the querying host has no additional Known Answers.
     * 
     * In multicast response messages, the TC bit MUST be zero on
     * transmission, and MUST be ignored on reception.
     * 
     * In legacy unicast response messages, the TC bit has the same meaning
     * as in conventional Unicast DNS: it means that the response was too
     * large to fit in a single packet, so the querier SHOULD reissue its
     * query using TCP in order to receive the larger response.
     */
    private static final int MESSAGE_HEADER_TRUNCATION_BIT = 0b0000001000000000;

    /**
     * bit shift for {@link #MESSAGE_HEADER_Z_BITS}.
     */

    private static final int MESSAGE_HEADER_Z_BIT_SHIFT = 4;

    /**
     * In both query and response messages, the Zero bit MUST be zero on
     * transmission, and MUST be ignored on reception.
     */
    private static final int MESSAGE_HEADER_Z_BITS = 0b0000000001110000;

    /**
     * In query messages, the Authoritative Answer bit MUST be zero on
     * transmission, and MUST be ignored on reception.
     * 
     * In response messages for Multicast domains, the Authoritative Answer
     * bit MUST be set to one (not setting this bit would imply there's some
     * other place where "better" information may be found) and MUST be
     * ignored on reception.
     */
    private boolean authoritativeAnswer;

    /**
     * is this message a response massage?
     */
    private boolean isResponse;

    /**
     * the message header flags (bits are seperatly avaliable as booleans). the
     * separate bits are explained with the appropriate constants.
     */
    private int messageHeaderFlags;

    /**
     * Multicast DNS implementations SHOULD listen for unsolicited responses
     * issued by hosts booting up (or waking up from sleep or otherwise
     * joining the network). Since these unsolicited responses may contain
     * a useful answer to a question for which the querier is currently
     * awaiting an answer, Multicast DNS implementations SHOULD examine all
     * received Multicast DNS response messages for useful answers, without
     * regard to the contents of the ID field or the Question Section. In
     * Multicast DNS, knowing which particular query message (if any) is
     * responsible for eliciting a particular response message is less
     * interesting than knowing whether the response message contains useful
     * information.
     * 
     * Multicast DNS implementations MAY cache data from any or all
     * Multicast DNS response messages they receive, for possible future
     * use, provided of course that normal TTL aging is performed on these
     * cached resource records.
     * 
     * In multicast query messages, the Query Identifier SHOULD be set to
     * zero on transmission.
     * 
     * In multicast responses, including unsolicited multicast responses,
     * the Query Identifier MUST be set to zero on transmission, and MUST be
     * ignored on reception.
     * 
     * In legacy unicast response messages generated specifically in
     * response to a particular (unicast or multicast) query, the Query
     * Identifier MUST match the ID from the query message.
     */
    private int messageId;

    /**
     * number of entries in the question section.
     */
    private int numberOfEntriesInQuestionSection;

    /**
     * number of name server records in authority section.
     */
    private int numberOfNameServerRecordsInAuthoritySection;

    /**
     * number of resource records in additional records answer section.
     */
    private int numberOfResourceRecordsInAdditionalRecordsAnswerSection;

    /**
     * number of resource records in answer section.
     */
    private int numberOfResourceRecordsInAnswerSection;

    /**
     * In both multicast query and multicast response messages, the OPCODE
     * MUST be zero on transmission (only standard queries are currently
     * supported over multicast). Multicast DNS messages received with an
     * OPCODE other than zero MUST be silently ignored.
     */
    private OperationCode operationCode;

    /**
     * In both multicast query and multicast response messages, the
     * Recursion Available bit MUST be zero on transmission, and MUST be
     * ignored on reception.
     */
    private boolean recursionAvailable;

    /**
     * In both multicast query and multicast response messages, the
     * Recursion Desired bit SHOULD be zero on transmission, and MUST be
     * ignored on reception.
     */
    private boolean recursionDesired;

    /**
     * In both multicast query and multicast response messages, the Response
     * Code MUST be zero on transmission. Multicast DNS messages received
     * with non-zero Response Codes MUST be silently ignored.
     */

    private ResponseCode responseCode;

    /**
     * In query messages, if the TC bit is set, it means that additional
     * Known-Answer records may be following shortly. A responder SHOULD
     * record this fact, and wait for those additional Known-Answer records,
     * before deciding whether to respond. If the TC bit is clear, it means
     * that the querying host has no additional Known Answers.
     * 
     * In multicast response messages, the TC bit MUST be zero on
     * transmission, and MUST be ignored on reception.
     * 
     * In legacy unicast response messages, the TC bit has the same meaning
     * as in conventional Unicast DNS: it means that the response was too
     * large to fit in a single packet, so the querier SHOULD reissue its
     * query using TCP in order to receive the larger response.
     */
    private boolean truncation;

    /**
     * In both query and response messages, the Zero bit MUST be zero on
     * transmission, and MUST be ignored on reception.
     */
    private int z;

    /**
     * @return the message header flags (bits are seperatly avaliable as
     *         booleans).
     */
    public int getMessageHeaderFlags() {
        return messageHeaderFlags;
    }

    /**
     * @return the message id of the message.
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * @return number of entries in question section.
     */
    public int getNumberOfEntriesInQuestionSection() {
        return numberOfEntriesInQuestionSection;
    }

    /**
     * @return number of name server records in authoritySection.
     */
    public int getNumberOfNameServerRecordsInAuthoritySection() {
        return numberOfNameServerRecordsInAuthoritySection;
    }

    /**
     * @return number of resource records in additional records answer section
     */
    public int getNumberOfResourceRecordsInAdditionalRecordsAnswerSection() {
        return numberOfResourceRecordsInAdditionalRecordsAnswerSection;
    }

    /**
     * @return number of resource records in answer section.
     */
    public int getNumberOfResourceRecordsInAnswerSection() {
        return numberOfResourceRecordsInAnswerSection;
    }

    /**
     * @return true if this is a response message.
     */
    public boolean isResponse() {
        return isResponse;
    }

    @Override
    public void read(InputStream in) throws IOException {
        messageId = readUshort(in);
        messageHeaderFlags = readUshort(in);
        numberOfEntriesInQuestionSection = readUshort(in);
        numberOfResourceRecordsInAnswerSection = readUshort(in);
        numberOfNameServerRecordsInAuthoritySection = readUshort(in);
        numberOfResourceRecordsInAdditionalRecordsAnswerSection = readUshort(in);
        readMessageHeaderFlags();
    }

    /**
     * deseriaize the flags from the header bits.
     */
    public void readMessageHeaderFlags() {
        isResponse = /*                           */(messageHeaderFlags & MESSAGE_HEADER_RESPONSE_BIT) != 0;
        operationCode = OperationCode.operationCode((messageHeaderFlags & MESSAGE_HEADER_OPERATION_CODE_BITS) >> MESSAGE_HEADER_OPERATION_CODE_BIT_SHIFT);
        authoritativeAnswer = /*                  */(messageHeaderFlags & MESSAGE_HEADER_AUTHORITATIVE_ANSWER_BIT) != 0;
        truncation = /*                           */(messageHeaderFlags & MESSAGE_HEADER_TRUNCATION_BIT) != 0;
        recursionDesired = /*                     */(messageHeaderFlags & MESSAGE_HEADER_RECURSION_DESIRED_BIT) != 0;
        recursionAvailable = /*                   */(messageHeaderFlags & MESSAGE_HEADER_RECURSION_AVAILABLE_BIT) != 0;
        z = /*                                    */(messageHeaderFlags & MESSAGE_HEADER_Z_BITS) >> MESSAGE_HEADER_Z_BIT_SHIFT;
        responseCode = /**/ResponseCode.responseCode(messageHeaderFlags & MESSAGE_HEADER_RESPONSE_BITS);
    }

    /**
     * set the message id.
     * 
     * @param messageId
     *            the new message id value
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    /**
     * set the number of entries in question section.
     * 
     * @param numberOfEntriesInQuestionSection
     *            the new value
     */
    public void setNumberOfEntriesInQuestionSection(int numberOfEntriesInQuestionSection) {
        this.numberOfEntriesInQuestionSection = numberOfEntriesInQuestionSection;
    }

    /**
     * set the number of name server records in authority section.
     * 
     * @param numberOfNameServerRecordsInAuthoritySection
     *            the new value
     */
    public void setNumberOfNameServerRecordsInAuthoritySection(int numberOfNameServerRecordsInAuthoritySection) {
        this.numberOfNameServerRecordsInAuthoritySection = numberOfNameServerRecordsInAuthoritySection;
    }

    /**
     * set the number of resource records in additional records answer section.
     * 
     * @param numberOfResourceRecordsInAdditionalRecordsAnswerSection
     *            the new value
     */
    public void setNumberOfResourceRecordsInAdditionalRecordsAnswerSection(int numberOfResourceRecordsInAdditionalRecordsAnswerSection) {
        this.numberOfResourceRecordsInAdditionalRecordsAnswerSection = numberOfResourceRecordsInAdditionalRecordsAnswerSection;
    }

    /**
     * set the number of resource records in answer section.
     * 
     * @param numberOfResourceRecordsInAnswerSection
     *            the new value
     */
    public void setNumberOfResourceRecordsInAnswerSection(int numberOfResourceRecordsInAnswerSection) {
        this.numberOfResourceRecordsInAnswerSection = numberOfResourceRecordsInAnswerSection;
    }

    /**
     * set the {@link #isResponse} flag.
     * 
     * @param value
     *            the new value.
     */
    public void setResponse(boolean value) {
        isResponse = value;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeMessageHeaderFlags();
        writeUshort(out, messageId);
        writeUshort(out, messageHeaderFlags);
        writeUshort(out, numberOfEntriesInQuestionSection);
        writeUshort(out, numberOfResourceRecordsInAnswerSection);
        writeUshort(out, numberOfNameServerRecordsInAuthoritySection);
        writeUshort(out, numberOfResourceRecordsInAdditionalRecordsAnswerSection);
    }

    /**
     * serialize the message header flags into the heaser flag attribbute.
     */
    public void writeMessageHeaderFlags() {
        messageHeaderFlags = 0;
        if (isResponse) {
            messageHeaderFlags = messageHeaderFlags | MESSAGE_HEADER_RESPONSE_BIT;
        }
        if (operationCode != null) {
            messageHeaderFlags = messageHeaderFlags | operationCode.value << MESSAGE_HEADER_OPERATION_CODE_BIT_SHIFT & MESSAGE_HEADER_OPERATION_CODE_BITS;
        }
        if (authoritativeAnswer) {
            messageHeaderFlags = messageHeaderFlags | MESSAGE_HEADER_AUTHORITATIVE_ANSWER_BIT;
        }
        if (truncation) {
            messageHeaderFlags = messageHeaderFlags | MESSAGE_HEADER_TRUNCATION_BIT;
        }
        if (recursionDesired) {
            messageHeaderFlags = messageHeaderFlags | MESSAGE_HEADER_RECURSION_DESIRED_BIT;
        }
        if (recursionAvailable) {
            messageHeaderFlags = messageHeaderFlags | MESSAGE_HEADER_RECURSION_AVAILABLE_BIT;
        }
        messageHeaderFlags = messageHeaderFlags | z << MESSAGE_HEADER_Z_BIT_SHIFT & MESSAGE_HEADER_Z_BITS;
        if (responseCode != null) {
            messageHeaderFlags = messageHeaderFlags | responseCode.value & MESSAGE_HEADER_RESPONSE_BITS;

        }
    }
}
