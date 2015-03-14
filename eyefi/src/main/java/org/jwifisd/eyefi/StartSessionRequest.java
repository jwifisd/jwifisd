package org.jwifisd.eyefi;

/*
 * #%L
 * jwifisd-eyefi
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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * soap request of a eyefi card that requests the start of a session.
 * 
 * @author Richard van Nieuwenhoven
 */
public class StartSessionRequest extends EyefiRequest {

    /**
     * the cnonce to use.
     */
    private String cnonce;

    /**
     * the ip address of the card.
     */
    private String ipAddress;

    /**
     * the mac adress of the card.
     */
    private String macaddress;

    /**
     * transfermode of the card.
     */
    private String transfermode;

    /**
     * transfermode timestamp of the card.
     */
    private String transfermodetimestamp;

    /**
     * parse the StartSessionRequest soap message that was send by the eyefi
     * card.
     * 
     * @param postData
     *            the soap body of the message.
     * @param ipAddress
     *            the ip address of the card
     * @throws XMLStreamException
     *             if the body could not be parsed.
     */
    public StartSessionRequest(String postData, String ipAddress) throws XMLStreamException {
        super(postData);
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(getClass().getSimpleName());
        result.append("(");
        result.append("\n\tmacaddress:").append(macaddress);
        result.append("\n\tcnonce:").append(cnonce);
        result.append("\n\ttransfermode:").append(transfermode);
        result.append("\n\ttransfermodetimestamp:").append(transfermodetimestamp);
        result.append(")");
        return result.toString();
    }

    /**
     * @return the cnonce of the request.
     */
    protected String getCnonce() {
        return cnonce;
    }

    /**
     * @return the ip address of the card.
     */
    protected String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return the mac address of the card.
     */
    protected String getMacaddress() {
        return macaddress;
    }

    /**
     * @return the transfer mode of the request.
     */
    protected String getTransfermode() {
        return transfermode;
    }

    /**
     * @return the transfer mode timestamp of the request.
     */
    protected String getTransfermodetimestamp() {
        return transfermodetimestamp;
    }

    @Override
    protected boolean handleElement(String localPart, XMLEventReader eventReader) throws XMLStreamException {
        if (localPart.equals("macaddress")) {
            macaddress = stringValue(eventReader);
            return true;
        } else if (localPart.equals("cnonce")) {
            cnonce = stringValue(eventReader);
            return true;
        } else if (localPart.equals("transfermode")) {
            transfermode = stringValue(eventReader);
            return true;
        } else if (localPart.equals("transfermodetimestamp")) {
            transfermodetimestamp = stringValue(eventReader);
            return true;
        }
        return false;
    }
}
