package io.github.jwifisd.eyefi;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.codec.binary.Hex;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class Main {

    public static void main(String[] args) {
        HttpServer server = HttpServer.createSimpleServer("/", "0.0.0.0", 59278);
        server.getServerConfiguration().addHttpHandler(new HttpHandler() {

            public void service(Request request, Response response) throws Exception {
                final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                final String date = format.format(new Date(System.currentTimeMillis()));
                if ("\"urn:StartSession\"".equals(request.getHeader("SOAPAction"))) {
                    startSession(request, response, date);
                    return;
                }
                if ("\"urn:GetPhotoStatus\"".equals(request.getHeader("SOAPAction"))) {
                    getPhotoStatus(request, response, date);
                    return;
                }
                System.out.println("connect");

                response.setContentType("text/plain");
                response.setContentLength(date.length());
                response.getWriter().write(date);
            }
        }, "/api/soap/eyefilm/v1");
        server.getServerConfiguration().addHttpHandler(new HttpHandler() {

            public void service(Request request, Response response) throws Exception {
                final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                final String date = format.format(new Date(System.currentTimeMillis()));
                uploadPhoto(request, response, date);
            }
        }, "/api/soap/eyefilm/v1/upload");
        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            System.in.read();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private static void uploadPhoto(Request request, Response response, final String date) throws IOException {
        response.setHeader("Server", "Eye-Fi Agent/2.0.4.0");
        response.setHeader("Date", date);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Type", "text/xml; charset=\"utf-8\"");

        String responseText =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Body><UploadPhotoResponse><success>true</success></UploadPhotoResponse></SOAP-ENV:Body></SOAP-ENV:Envelope>";

        response.setContentLength(responseText.length());
        response.getWriter().write(responseText);
    }

    private static void getPhotoStatus(Request request, Response response, final String date) throws FactoryConfigurationError, XMLStreamException, IOException {
        String macaddress = null;
        String credential = null;
        String filename = null;
        String filesize = null;
        String filesignature = null;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(request.getInputStream());
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                if (event.asStartElement().getName().getLocalPart().equals("macaddress")) {
                    event = eventReader.nextEvent();
                    macaddress = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("credential")) {
                    event = eventReader.nextEvent();
                    credential = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("filename")) {
                    event = eventReader.nextEvent();
                    filename = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("filesize")) {
                    event = eventReader.nextEvent();
                    filesize = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("filesignature")) {
                    event = eventReader.nextEvent();
                    filesignature = event.asCharacters().getData();
                    continue;
                }
            }
        }
        System.out.println("macaddress:" + macaddress);
        System.out.println("credential:" + credential);
        System.out.println("filename:" + filename);
        System.out.println("filesize:" + filesize);
        System.out.println("filesignature:" + filesignature);

        response.setHeader("Server", "Eye-Fi Agent/2.0.4.0");
        response.setHeader("Date", date);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Type", "text/xml; charset=\"utf-8\"");

        String responseText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" + //
                /**/"<SOAP-ENV:Body>\n" + //
                /*  */"<GetPhotoStatusResponse xmlns=\"http://localhost/api/soap/eyefilm\">" + //
                /*    */"<fileid>1</fileid><offset>0</offset>" + //
                /*  */"</GetPhotoStatusResponse>" + //
                /**/"</SOAP-ENV:Body>" + //
                "</SOAP-ENV:Envelope>";

        response.setContentLength(responseText.length());
        response.getWriter().write(responseText);
    }

    private static void startSession(Request request, Response response, final String date) throws Exception, IOException {
        String macaddress = null;
        String cnonce = null;
        String transfermode = null;
        String transfermodetimestamp = null;
        System.out.println("connectx");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(request.getInputStream());
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                if (event.asStartElement().getName().getLocalPart().equals("macaddress")) {
                    event = eventReader.nextEvent();
                    macaddress = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("cnonce")) {
                    event = eventReader.nextEvent();
                    cnonce = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("transfermode")) {
                    event = eventReader.nextEvent();
                    transfermode = event.asCharacters().getData();
                    continue;
                }
                if (event.asStartElement().getName().getLocalPart().equals("transfermodetimestamp")) {
                    event = eventReader.nextEvent();
                    transfermodetimestamp = event.asCharacters().getData();
                    continue;
                }
            }
        }
        System.out.println("macaddress:" + macaddress);
        System.out.println("cnonce:" + cnonce);
        System.out.println("transfermode:" + transfermode);
        System.out.println("transfermodetimestamp:" + transfermodetimestamp);
        byte[] cnonceBytes = Hex.decodeHex(cnonce.toCharArray());
        byte[] uploadKey = Hex.decodeHex("00000000000000000000000000000000".toCharArray());
        byte[] credential = md5(Hex.decodeHex(macaddress.toCharArray()), cnonceBytes, uploadKey);
        String credentialStr = Hex.encodeHexString(credential);

        response.setHeader("Server", "Eye-Fi Agent/2.0.4.0");
        response.setHeader("Date", date);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Type", "text/xml; charset=\"utf-8\"");

        String responseText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" + //
                /**/"<SOAP-ENV:Body>" + //
                /*  */"<StartSessionResponse xmlns=\"http://localhost/api/soap/eyefilm\">" + //
                /*    */"<credential>" + credentialStr + "</credential>" + //
                /*    */"<snonce>" + cnonce + "</snonce>" + //
                /*    */"<transfermode>2</transfermode>" + //
                /*    */"<transfermodetimestamp>1230268824</transfermodetimestamp>" + //
                /*    */"<upsyncallowed>false</upsyncallowed>" + //
                /*  */"</StartSessionResponse>" + //
                /**/"</SOAP-ENV:Body>" + //
                "</SOAP-ENV:Envelope>";
        System.out.println(responseText);
        response.setContentLength(responseText.length());
        response.getWriter().write(responseText);
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
