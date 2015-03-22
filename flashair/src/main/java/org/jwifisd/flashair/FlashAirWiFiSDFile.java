package org.jwifisd.flashair;

/*
 * #%L
 * jwifisd-flashair
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
 * flashair wifi file representation. data is loaded lazy but cached.
 * 
 * @author Richard van Nieuwenhoven
 */
public class FlashAirWiFiSDFile implements IWifiFile {

    /**
     * File date time class that parses the flashair file time.
     * 
     * @author Richard van Nieuwenhoven
     */
    static class DateTime {

        /**
         * how many milliseconds are there in a half second.
         */
        private static final int MILLISECONDS_IN_HALF_SECOND = 500;

        /**
         * years are counted from this offset.
         */
        private static final int YEAR_OFFSET = 1980;

        /**
         * end bit position of the day field.
         */
        private static final int DAY_BIT_END = 4;

        /**
         * start bit position of the day field.
         */
        private static final int DAY_BIT_START = 0;

        /**
         * end bit position of the hour field.
         */
        private static final int HOUR_BIT_END = 15;

        /**
         * start bit position of the hour field.
         */
        private static final int HOUR_BIT_START = 11;

        /**
         * end bit position of the minute field.
         */
        private static final int MINUTE_BIT_END = 10;

        /**
         * start bit position of the minute field.
         */
        private static final int MINUTE_BIT_START = 5;

        /**
         * end bit position of the month field.
         */
        private static final int MONTH_BIT_END = 8;

        /**
         * start bit position of the month field.
         */
        private static final int MONTH_BIT_START = 5;

        /**
         * end bit position of the second/2 field.
         */
        private static final int SECOND_BY_2_BIT_END = 4;

        /**
         * start bit position of the second/2 field.
         */
        private static final int SECOND_BY_2_BIT_START = 0;

        /**
         * end bit position of the year field.
         */
        private static final int YEAR_BIT_END = 15;

        /**
         * start bit position of the year field.
         */
        private static final int YEAR_BIT_START = 9;

        /**
         * day of month field.
         */
        private int day;

        /**
         * hour of day field.
         */
        private int hour;

        /**
         * milliseconds in second field.
         */
        private int millisecond;

        /**
         * minute in hour.
         */
        private int minute;

        /**
         * month in year.
         */
        private int month;

        /**
         * second in minute.
         */
        private int second;

        /**
         * year field.
         */
        private int year;

        /**
         * parse the date file strings and set the date/time attributes.
         * 
         * @param dateString
         *            string with an integer that respresents a date
         * @param timeString
         *            string with an integer that represents a timestamp
         */
        public DateTime(String dateString, String timeString) {
            int date = Integer.parseInt(dateString);
            int time = Integer.parseInt(timeString);
            year = extractInt(date, YEAR_BIT_START, YEAR_BIT_END) + YEAR_OFFSET;
            month = extractInt(date, MONTH_BIT_START, MONTH_BIT_END);
            day = extractInt(date, DAY_BIT_START, DAY_BIT_END);
            hour = extractInt(time, HOUR_BIT_START, HOUR_BIT_END);
            minute = extractInt(time, MINUTE_BIT_START, MINUTE_BIT_END);
            int secondHlf = extractInt(time, SECOND_BY_2_BIT_START, SECOND_BY_2_BIT_END);
            second = secondHlf / 2;
            millisecond = (secondHlf % 2) * MILLISECONDS_IN_HALF_SECOND;
        }

        /**
         * extract an integer from an integer using bit start and end positions.
         * 
         * @param base
         *            the base integer to use
         * @param bitStart
         *            the start bit in the base (including)
         * @param bitEnd
         *            the end bin in the base (including)
         * @return the integer represented by the bit range.
         */
        private int extractInt(int base, int bitStart, int bitEnd) {
            int value = base >> bitStart;
            int mask = (1 << ((bitEnd - bitStart) + 1)) - 1;
            return value & mask;
        }
    }

    /**
     * archive bit, is the file archived.
     */
    private static final int ARCHIVE_BIT = 5;

    /**
     * directly bit see flashair api.
     */
    private static final int DIRECTLY_BIT = 4;

    /**
     * hidden file bit, is the file a hidden file?
     */
    private static final int HIDDEN_FILE_BIT = 1;

    /**
     * readonly bit, is the file readonly.
     */
    private static final int READ_ONLY_BIT = 0;

    /**
     * system file bit, is the file a system file?
     */
    private static final int SYSTEM_FILE_BIT = 2;

    /**
     * volume bit, see flashair api.
     */
    private static final int VOLUME_BIT = 3;

    /**
     * card where the file is located.
     */
    private final FlashAirWiFiSD card;

    /**
     * cached data of the file.
     */
    private byte[] data;

    /**
     * date/time of last write access.
     */
    private final DateTime dateTime;

    /**
     * directory where the file is located.
     */
    private final String directory;

    /**
     * the file name without directory.
     */
    private final String file;

    /**
     * is this a directory.
     */
    private final boolean isDirectory;

    /**
     * the size of the file.
     */
    private final String size;

    /**
     * time column index in the file list.
     */
    private static final int FILE_LIST_COLUMN_TIME = 0;

    /**
     * date column index in the file list.
     */
    private static final int FILE_LIST_COLUMN_DATE = 1;

    /**
     * attribute column index in the file list.
     */
    private static final int FILE_LIST_COLUMN_ATTRIBUTE = 2;

    /**
     * size column index in the file list.
     */
    private static final int FILE_LIST_COLUMN_SIZE = 3;

    /**
     * name column index in the file list.
     */
    private static final int FILE_LIST_COLUMN_NAME = 4;

    /**
     * name directory index in the file list.
     */
    private static final int FILE_LIST_COLUMN_DIRECTORY = 5;

    /**
     * constructor for a flashair file.
     * 
     * @param card
     *            card where the file is located
     * @param columns
     *            columns of the file list.
     */
    public FlashAirWiFiSDFile(FlashAirWiFiSD card, String... columns) {
        this.dateTime = new DateTime(columns[FILE_LIST_COLUMN_DATE], columns[FILE_LIST_COLUMN_TIME]);
        this.isDirectory = (Integer.valueOf(columns[FILE_LIST_COLUMN_ATTRIBUTE]) & (1 << (ARCHIVE_BIT - 1))) != 0;
        this.size = columns[FILE_LIST_COLUMN_SIZE];
        this.file = columns[FILE_LIST_COLUMN_NAME];
        this.directory =
                columns[FILE_LIST_COLUMN_DIRECTORY].charAt(columns[FILE_LIST_COLUMN_DIRECTORY].length() - 1) == '/' ? columns[FILE_LIST_COLUMN_DIRECTORY]
                        : columns[FILE_LIST_COLUMN_DIRECTORY] + "/";
        this.card = card;
    }

    /**
     * parse a file listing line, and create a wifi file from the data.
     * 
     * @param line
     *            the listing line
     * @param card
     *            the flashair card where the listing is from.
     * @return the file or null if this line represents no file.
     */
    protected static FlashAirWiFiSDFile parseLine(String line, FlashAirWiFiSD card) {

        int indexOfComma = line.lastIndexOf(',');
        if (indexOfComma < 0) {
            return null;
        }
        String time = line.substring(indexOfComma + 1);
        int lastIndexOfComma = indexOfComma;
        indexOfComma = line.lastIndexOf(',', indexOfComma - 1);
        if (indexOfComma < 0) {
            return null;
        }
        String date = line.substring(indexOfComma + 1, lastIndexOfComma);
        lastIndexOfComma = indexOfComma;
        indexOfComma = line.lastIndexOf(',', indexOfComma - 1);
        if (indexOfComma < 0) {
            return null;
        }
        String attribute = line.substring(indexOfComma + 1, lastIndexOfComma);
        lastIndexOfComma = indexOfComma;
        indexOfComma = line.lastIndexOf(',', indexOfComma - 1);
        if (indexOfComma < 0) {
            return null;
        }
        String size = line.substring(indexOfComma + 1, lastIndexOfComma);
        lastIndexOfComma = indexOfComma;
        indexOfComma = line.indexOf(',');
        if (indexOfComma < 0) {
            return null;
        }
        String file = line.substring(indexOfComma + 1, lastIndexOfComma);
        String directory = line.substring(0, indexOfComma);
        return new FlashAirWiFiSDFile(card, time, date, attribute, size, file, directory);
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FlashAirWiFiSDFile) {
            return ((FlashAirWiFiSDFile) obj).directory.equals(directory) && ((FlashAirWiFiSDFile) obj).file.equals(file);
        }
        return false;
    }

    @Override
    public byte[] getData() {
        if (data == null) {
            data = card.getData(this);
        }
        return data;
    }

    @Override
    public int hashCode() {
        return directory.hashCode() + file.hashCode();
    }

    /**
     * @return true if this file is a directory.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String name() {
        return this.directory + this.file;
    }

    @Override
    public long timeStamp() {
        return -1;
    }

    @Override
    public void clean() {
        data = null;
    }
}
