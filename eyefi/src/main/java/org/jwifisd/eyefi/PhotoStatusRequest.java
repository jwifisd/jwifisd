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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

public class PhotoStatusRequest extends EyefiRequest {

    String macaddress;

    String credential;

    String filename;

    String filesize;

    String filesignature;

    public PhotoStatusRequest(String postData) throws Exception {
        super(postData);
    }

    protected boolean handleElement(String localPart, XMLEventReader eventReader) throws XMLStreamException {
        if (localPart.equals("macaddress")) {
            macaddress = stringValue(eventReader);
            return true;
        } else if (localPart.equals("credential")) {
            credential = stringValue(eventReader);
            return true;
        } else if (localPart.equals("filename")) {
            filename = stringValue(eventReader);
            return true;
        } else if (localPart.equals("filesize")) {
            filesize = stringValue(eventReader);
            return true;
        } else if (localPart.equals("filesignature")) {
            filesignature = stringValue(eventReader);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(getClass().getSimpleName());
        result.append("(");
        result.append("\n\tmacaddress:").append(macaddress);
        result.append("\n\tcredential:").append(credential);
        result.append("\n\tfilename:").append(filename);
        result.append("\n\tfilesize:").append(filesize);
        result.append("\n\tfilesignature:").append(filesignature);
        result.append(")");
        return result.toString();
    }
}
