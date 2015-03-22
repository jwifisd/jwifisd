package org.jwifisd.api;

import java.util.List;

/*
 * #%L
 * jwifisd-api
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
 * This is a card api that allows active (without events) browsing of files and
 * directories. Not all cards support this way of access.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface IBrowse {

    /**
     * List all directories under the specified directory. Only directories are
     * returned all normal files are omitted.
     * 
     * @param directory
     *            the directory to scan.
     * @return the list of directories under this directory.
     */
    List<String> listDirectories(String directory);

    /**
     * List all files in the specified directory. Only files are returned -
     * directories are omitted.
     * 
     * @param directory
     *            the directory to scan.
     * @return the list of files in this directory.
     */
    List<IWifiFile> listFiles(String directory);

}
