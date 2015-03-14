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
 * parse the upload photo request of an eyefi card.
 * 
 * @author Richard van Nieuwenhoven
 */
public class UploadPhotoRequest extends EyefiRequest {

    /**
     * encryption attribute of the request.
     */
    private String encryption;

    /**
     * file name of the photo.
     */
    private String filename;

    /**
     * file signature of the photo.
     */

    private String filesignature;

    /**
     * file size of the photo.
     */
    private String filesize;

    /**
     * flags of the photo file.
     */
    private String flags;

    /**
     * mac address of the eyefi card.
     */
    private String macaddress;

    /**
     * parse the soap upload photo request message.
     * 
     * @param postData
     *            the soap body
     * @throws XMLStreamException
     *             if the body could not be parsed.
     */
    public UploadPhotoRequest(String postData) throws XMLStreamException {
        super(postData);
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(getClass().getSimpleName());
        result.append("(");
        result.append("\n\tmacaddress:").append(macaddress);
        result.append("\n\tfilename:").append(filename);
        result.append("\n\tfilesize:").append(filesize);
        result.append("\n\tfilesignature:").append(filesignature);
        result.append("\n\tencryption:").append(encryption);
        result.append("\n\tflags:").append(flags);
        result.append(")");
        return result.toString();
    }

    /**
     * @return encryption attribute of the request.
     */
    protected String getEncryption() {
        return encryption;
    }

    /**
     * @return file name of the photo.
     */
    protected String getFilename() {
        return filename;
    }

    /**
     * @return file signature of the photo.
     */
    protected String getFilesignature() {
        return filesignature;
    }

    /**
     * @return file size of the photo.
     */
    protected String getFilesize() {
        return filesize;
    }

    /**
     * @return flags of the photo file.
     */
    protected String getFlags() {
        return flags;
    }

    /**
     * @return mac address of the eyefi card.
     */
    protected String getMacaddress() {
        return macaddress;
    }

    @Override
    protected boolean handleElement(String localPart, XMLEventReader eventReader) throws XMLStreamException {
        if (localPart.equals("macaddress")) {
            macaddress = stringValue(eventReader);
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
        } else if (localPart.equals("encryption")) {
            encryption = stringValue(eventReader);
            return true;
        } else if (localPart.equals("flags")) {
            flags = stringValue(eventReader);
            return true;
        } else {
            return false;
        }
    }
}
