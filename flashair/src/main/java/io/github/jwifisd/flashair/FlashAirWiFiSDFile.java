package io.github.jwifisd.flashair;

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

import io.github.jwifisd.api.IWifiFile;

public class FlashAirWiFiSDFile implements IWifiFile {

    static class DateTime {

        private static final int DAY_BIT_END = 4;

        private static final int DAY_BIT_START = 0;

        private static final int HOUR_BIT_END = 15;

        private static final int HOUR_BIT_START = 11;

        private static final int MINUTE_BIT_END = 10;

        private static final int MINUTE_BIT_START = 5;

        private static final int MONTH_BIT_END = 8;

        private static final int MONTH_BIT_START = 5;

        private static final int SECOND_BY_2_BIT_END = 4;

        private static final int SECOND_BY_2_BIT_START = 0;

        private static final int YEAR_BIT_END = 15;

        private static final int YEAR_BIT_START = 9;

        private int day;

        private int hour;

        private int millisecond;

        private int minute;

        private int month;

        private int second;

        private int year;

        public DateTime(String dateString, String timeString) {
            int date = Integer.parseInt(dateString);
            int time = Integer.parseInt(timeString);
            year = extractInt(date, YEAR_BIT_START, YEAR_BIT_END) + 1980;
            month = extractInt(date, MONTH_BIT_START, MONTH_BIT_END);
            day = extractInt(date, DAY_BIT_START, DAY_BIT_END);
            hour = extractInt(time, HOUR_BIT_START, HOUR_BIT_END);
            minute = extractInt(time, MINUTE_BIT_START, MINUTE_BIT_END);
            int secondHlf = extractInt(time, SECOND_BY_2_BIT_START, SECOND_BY_2_BIT_END);
            second = secondHlf / 2;
            millisecond = (secondHlf % 2) * 500;
        }

        private int extractInt(int base, int bitStart, int bitEnd) {
            int value = base >> bitStart;
            int mask = (1 << ((bitEnd - bitStart) + 1)) - 1;
            return value & mask;
        }
    }

    private static final int ARCHIVE_BIT = 5;

    private static final int DIRECTLY_BIT = 4;

    private static final int HIDDEN_FILE_BIT = 1;

    private static final int READ_ONLY_BIT = 0;

    private static final int SYSTEM_FILE_BIT = 2;

    private static final int VOLUME_BIT = 3;

    private final FlashAirWiFiSD card;

    private byte[] data;

    private final DateTime dateTime;

    private final String directory;

    private final String file;

    private final boolean isDirectory;

    private final String size;

    public FlashAirWiFiSDFile(String time, String date, String attribute, String size, String file, String directory, FlashAirWiFiSD card) {
        this.dateTime = new DateTime(date, time);
        this.isDirectory = (Integer.valueOf(attribute) & (1 << (ARCHIVE_BIT - 1))) != 0;
        this.size = size;
        this.file = file;
        this.directory = directory.charAt(directory.length() - 1) == '/' ? directory : directory + "/";
        this.card = card;
    }

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
        return new FlashAirWiFiSDFile(time, date, attribute, size, file, directory, card);
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
