package org.jwifisd.transcend;

/*
 * #%L
 * jwifisd-transcend
 * %%
 * Copyright (C) 2015 jwifisd
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

import org.jwifisd.api.IWifiFile;

/**
 * a file respresentation of a file on the transcend wifi card.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TranscendWifiSDFile implements IWifiFile {

    /**
     * the card where the file is located.
     */
    private final TranscendWiFiSD card;

    /**
     * the lazy cached file contents of the file.
     */
    private byte[] data;

    /**
     * the name of the file.
     */
    private final String name;

    /**
     * the timestamp of the file.
     */
    private long timestamp = -1L;

    /**
     * the constructor for a file on the card.
     * 
     * @param card
     *            the card where the file is located.
     * @param name
     *            the name of the file.
     */
    protected TranscendWifiSDFile(TranscendWiFiSD card, String name) {
        this.name = name;
        this.card = card;
    }

    @Override
    public byte[] getData() {
        if (data == null) {
            card.downloadFile(this);
        }
        return data;
    }

    /**
     * @return the timestamp of the file as {@link java.io.File#lastModified()}.
     */
    public long getTimestamp() {
        if (timestamp < 0) {
            card.downloadFile(this);
        }
        return timestamp;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long timeStamp() {
        return 0;
    }

    /**
     * set the data cache of the card.
     * 
     * @param data
     *            the data to set.
     */
    protected void setData(byte[] data) {
        this.data = data;
    }

    /**
     * set the timestamp of the file.
     * 
     * @param timestamp
     *            the timestap to set.
     */
    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void clean() {
        data = null;
    }
}
