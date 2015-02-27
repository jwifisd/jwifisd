package io.github.jwifisd.eyefi;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fi.iki.elonen.NanoHTTPD.Response;

public class EyefiResponse extends Response {

    public EyefiResponse(String responseText) {
        super(Status.OK, "text/xml", responseText);
        addHeader("Server", "Eye-Fi Agent/2.0.4.0");
        final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        final String date = format.format(new Date(System.currentTimeMillis()));
        addHeader("Date", date);
        addHeader("Pragma", "no-cache");
        addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
    }
}
