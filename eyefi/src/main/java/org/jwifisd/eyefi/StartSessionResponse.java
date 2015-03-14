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
/**
 * soap response for the start session request.
 * 
 * @author Richard van Nieuwenhoven
 */
public class StartSessionResponse extends EyefiResponse {

    /**
     * create a soap response for a start session request of an eyefi card.
     * 
     * @param credentialStr
     *            the credential string
     * @param cnonce
     *            the cnonce
     * @param transfermode
     *            the transfer mode
     * @param transfermodetimestamp
     *            the transfer mode timestamp
     */
    public StartSessionResponse(String credentialStr, String cnonce, String transfermode, String transfermodetimestamp) {
        super("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" + //
                /**/"<SOAP-ENV:Body>" + //
                /*  */"<StartSessionResponse xmlns=\"http://localhost/api/soap/eyefilm\">" + //
                /*    */"<credential>" + credentialStr + "</credential>" + //
                /*    */"<snonce>" + cnonce + "</snonce>" + //
                /*    */"<transfermode>" + transfermode + "</transfermode>" + //
                /*    */"<transfermodetimestamp>" + transfermodetimestamp + "</transfermodetimestamp>" + //
                /*    */"<upsyncallowed>false</upsyncallowed>" + //
                /*  */"</StartSessionResponse>" + //
                /**/"</SOAP-ENV:Body>" + //
                "</SOAP-ENV:Envelope>");
    }

}
