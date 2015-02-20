package io.github.jwifisd.flashair;

import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

public class CopyOfMain {


    public static void main(String[] args) throws Exception {
        final JmDNS soc = JmDNS.create(InetAddress.getByName("192.168.0.255"),"192.168.0.11");
        soc.addServiceTypeListener(new ServiceTypeListener() {

            @Override
            public void subTypeForServiceTypeAdded(ServiceEvent arg0) {
                System.out.println(arg0);
            }

            @Override
            public void serviceTypeAdded(ServiceEvent arg0) {
                soc.addServiceListener(arg0.getType(), new ServiceListener() {

                    @Override
                    public void serviceResolved(ServiceEvent arg0) {
                        System.out.println(arg0);

                    }

                    @Override
                    public void serviceRemoved(ServiceEvent arg0) {
                        System.out.println(arg0);

                    }

                    @Override
                    public void serviceAdded(ServiceEvent arg0) {
                        System.out.println(arg0);

                    }
                });

            }
        });

    }

}
