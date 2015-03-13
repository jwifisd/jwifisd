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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.RequestContext;

public class Main3 {

    public static void main(String[] args) throws Exception {
        MultipartStream stream =
                new MultipartStream(new FileInputStream("src/main/resources/NanoHTTPD-977513220698581430"),
                        "---------------------------02468ace13579bdfcafebabef00d".getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String readHeaders = stream.readHeaders();
        System.out.println(readHeaders.toString());
        stream.readBodyData(output);
        output = new ByteArrayOutputStream();
        readHeaders = stream.readHeaders();
        System.out.println(readHeaders.toString());
        stream.readBodyData(output);

        final InputStream in = new FileInputStream("src/main/resources/NanoHTTPD-977513220698581430");

        FileItemIterator iter = new FileUpload().getItemIterator(new RequestContext() {

            @Override
            public InputStream getInputStream() throws IOException {
                return in;
            }

            @Override
            public String getContentType() {
                // TODO Auto-generated method stub
                return "multipart/form-data; boundary=---------------------------02468ace13579bdfcafebabef00d";
            }

            @Override
            public int getContentLength() {
                return 4237763;
            }

            @Override
            public String getCharacterEncoding() {
                // TODO Auto-generated method stub
                return "UTF-8";
            }
        });

        while (iter.hasNext()) {
            FileItemStream item = iter.next();

            System.out.println("name:" + item.getName());
            System.out.println("name:" + item.getContentType());
            //item.openStream();
            
        }
    }

}
