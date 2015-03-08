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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.jwifisd.api.INotifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.utils.IOUtils;

import fi.iki.elonen.NanoHTTPD;

public class EyeFiServer extends NanoHTTPD {

    private static final String DEFAULT_UPLOADKEY = "00000000000000000000000000000000";

    private INotifier notifier;

    class EyeFiUploadFile {

        private final File eyefiFile;

        private final String integerityDigest;

        private final String soapEnvelope;

        public EyeFiUploadFile(IHTTPSession session, Map<String, String> files) {
            String pathname = files.get("FILENAME");
            if (pathname == null) {
                throw new IllegalArgumentException("MissingFileName");
            }
            this.eyefiFile = new File(pathname);

            this.integerityDigest = session.getParms().get("INTEGRITYDIGEST");
            this.soapEnvelope = session.getParms().get("SOAPENVELOPE");

        }

    }

    public EyeFiServer(INotifier notifier) {
        super(59278);
        this.notifier = notifier != null ? notifier : INotifier.DUMMY;

    }

    public static void main(String[] args) throws Exception {
        EyeFiServer server = new EyeFiServer(null);
        server.start();
        while (true) {
            Thread.sleep(500);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        try {
            session.parseBody(files);
            String soapAction = session.getHeaders().get("soapaction");
            if (soapAction == null) {
                soapAction = "";
            }
            if (String.valueOf(session.getUri()).startsWith("/api/soap/eyefilm/v1")) {
                if (soapAction.indexOf("MarkLastPhotoInRoll") >= 0) {
                    return new MarkLastPhotoInRollResponse();
                } else if (String.valueOf(session.getUri()).startsWith("/api/soap/eyefilm/v1/upload")) {
                    return uploadPhoto(new EyeFiUploadFile(session, files));
                } else {
                    if (soapAction.indexOf("StartSession") >= 0) {
                        return startSession(session.getParms(), files, session.getHeaders().get("http-client-ip"));
                    } else if (soapAction.indexOf("GetPhotoStatus") >= 0) {
                        return getPhotoStatus(session.getParms(), files);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FaultResponce();
    }

    private Response uploadPhoto(EyeFiUploadFile context) throws Exception {
        UploadPhotoRequest uploadRequest = new UploadPhotoRequest(context.soapEnvelope);
        System.out.println(uploadRequest.toString());
        String fileName = uploadRequest.filename.replace(".tar", "");
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        boolean extraced = extractTarFile(context.eyefiFile, fout);
        cards.get(uploadRequest.macaddress).reportNewFile(new EyeFiPhoto(fileName, fout.toByteArray()));
        return new UploadPhotoResponse(extraced);
    }

    private boolean extractTarFile(File tarFile, OutputStream out) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        boolean result = false;
        byte[] tarBytes = IOUtils.toByteArray(new FileInputStream(tarFile));
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes("US-ASCII"), 0, tarBytes, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);

        TarArchiveInputStream tar = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));

        final byte[] buffer = new byte[1024 * 32];
        try {
            ArchiveEntry tarEntry = tar.getNextEntry();
            if (tarEntry != null) {
                int n = 0;
                while ((n = tar.read(buffer)) >= 0) {
                    out.write(buffer, 0, n);
                }
                result = true;
            }
        } finally {
            tar.close();
            out.close();
        }
        return result;
    }

    private int photoStatusId = 1;

    private Map<Integer, PhotoStatusRequest> photoStatuses = new HashMap<>();

    private Response getPhotoStatus(Map<String, String> parms, Map<String, String> files) throws Exception {
        PhotoStatusRequest photoStatus = new PhotoStatusRequest(files.get("postData"));
        int statusId = photoStatusId++;
        photoStatuses.put(statusId, photoStatus);
        System.out.println(photoStatus.toString());
        return new GetPhotoStatusResponse(statusId);
    }

    private Response startSession(Map<String, String> parms, Map<String, String> files, String remoteIp) throws Exception {
        StartSessionRequest startSession = new StartSessionRequest(files.get("postData"), remoteIp);
        registerCart(startSession);
        System.out.println(startSession.toString());
        byte[] cnonceBytes = Hex.decodeHex(startSession.cnonce.toCharArray());
        byte[] credential = md5(Hex.decodeHex(startSession.macaddress.toCharArray()), cnonceBytes, uploadKey(startSession));
        return new StartSessionResponse(Hex.encodeHexString(credential), startSession.cnonce, startSession.transfermode, startSession.transfermodetimestamp);
    }

    private Map<String, EyeFiCard> cards = new HashMap<>();

    private void registerCart(StartSessionRequest startSession) throws UnknownHostException {
        EyeFiCard card = cards.get(startSession.macaddress);
        if (card == null) {
            card = new EyeFiCard(startSession);
            cards.put(startSession.macaddress, card);
            notifier.newCard(card);
        }
    }

    private byte[] uploadKey(StartSessionRequest startSession) throws Exception {
        String uploadKey = notifier.getProperty("uploadkey-" + startSession.macaddress);
        if (uploadKey == null || uploadKey.isEmpty()) {
            uploadKey = DEFAULT_UPLOADKEY;
        }
        try {
            return Hex.decodeHex(uploadKey.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
            return Hex.decodeHex(DEFAULT_UPLOADKEY.toCharArray());
        }
    }

    public static byte[] md5(byte[]... args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (byte[] arg : args) {
                digest.update(arg);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
