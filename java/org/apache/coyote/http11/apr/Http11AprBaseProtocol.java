/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.coyote.http11.apr;

import java.net.InetAddress;
import java.net.URLEncoder;

import org.apache.coyote.ActionCode;
import org.apache.coyote.ActionHook;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.RequestGroupInfo;
import org.apache.coyote.http11.Constants;
import org.apache.coyote.http11.Http11BaseProtocol;
import org.apache.tomcat.util.net.apr.AprEndpoint;
import org.apache.tomcat.util.net.apr.AprEndpoint.Handler;


/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public class Http11AprBaseProtocol extends Http11BaseProtocol implements ProtocolHandler
{
    public Http11AprBaseProtocol() {
        ep=new AprEndpoint();
        cHandler = new AprHttp11ConnectionHandler( this );
        setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
        setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
        // this line is different from super.
        //setServerSoTimeout(Constants.DEFAULT_SERVER_SOCKET_TIMEOUT);
        setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
    }


    /** Start the protocol
     */
    public void init() throws Exception {
        ep.setName(getName());
        ep.setHandler((AprEndpoint.Handler)cHandler);

        try {
            ep.init();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.initerror"), ex);
            throw ex;
        }
        if(log.isInfoEnabled())
            log.info(sm.getString("http11protocol.init", getName()));

    }

    public void start() throws Exception {
        try {
            ep.start();
        } catch (Exception ex) {
            log.error(sm.getString("http11protocol.endpoint.starterror"), ex);
            throw ex;
        }
        if(log.isInfoEnabled())
            log.info(sm.getString("http11protocol.start", getName()));
    }

    public void destroy() throws Exception {
        if(log.isInfoEnabled())
            log.info(sm.getString("http11protocol.stop", getName()));
        ep.destroy();
    }

    // -------------------- Properties--------------------
    protected AprEndpoint ep=new AprEndpoint();

    public int getFirstReadTimeout() {
        return ep.getFirstReadTimeout();
    }

    public void setFirstReadTimeout( int i ) {
        ep.setFirstReadTimeout(i);
        setAttribute("firstReadTimeout", "" + i);
    }

    public int getPollTime() {
        return ep.getPollTime();
    }

    public void setPollTime( int i ) {
        ep.setPollTime(i);
        setAttribute("pollTime", "" + i);
    }

    public void setPollerSize(int i) {
        ep.setPollerSize(i); 
        setAttribute("pollerSize", "" + i);
    }
    
    public int getPollerSize() {
        return ep.getPollerSize();
    }
    
    public void setSendfileSize(int i) {
        ep.setSendfileSize(i); 
        setAttribute("sendfileSize", "" + i);
    }
    
    public int getSendfileSize() {
        return ep.getSendfileSize();
    }
    
    public boolean getUseSendfile() {
        return ep.getUseSendfile();
    }

    public void setUseSendfile(boolean useSendfile) {
        ep.setUseSendfile(useSendfile);
    }

    public InetAddress getAddress() {
        return ep.getAddress();
    }

    public void setAddress(InetAddress ia) {
        ep.setAddress( ia );
        setAttribute("address", "" + ia);
    }

    public String getName() {
        String encodedAddr = "";
        if (getAddress() != null) {
            encodedAddr = "" + getAddress();
            if (encodedAddr.startsWith("/"))
                encodedAddr = encodedAddr.substring(1);
            encodedAddr = URLEncoder.encode(encodedAddr) + "-";
        }
        return ("http-" + encodedAddr + ep.getPort());
    }

    public boolean getTcpNoDelay() {
        return ep.getTcpNoDelay();
    }

    public void setTcpNoDelay( boolean b ) {
        ep.setTcpNoDelay( b );
        setAttribute("tcpNoDelay", "" + b);
    }

    public int getSoLinger() {
        return ep.getSoLinger();
    }

    public void setSoLinger( int i ) {
        ep.setSoLinger( i );
        setAttribute("soLinger", "" + i);
    }

    public int getSoTimeout() {
        return ep.getSoTimeout();
    }

    public void setSoTimeout( int i ) {
        ep.setSoTimeout(i);
        setAttribute("soTimeout", "" + i);
    }

    public String getProtocol() {
        return getProperty("protocol");
    }

    public void setProtocol( String k ) {
        setSecure(true);
        setAttribute("protocol", k);
    }

    public boolean getSecure() {
        return secure;
    }

    public void setSecure( boolean b ) {
        secure=b;
        setAttribute("secure", "" + b);
    }

    // --------------------  SSL related properties --------------------

    /**
     * SSL engine.
     */
    public String getSSLEngine() { return ep.getSSLEngine(); }
    public void setSSLEngine(String SSLEngine) { ep.setSSLEngine(SSLEngine); }


    /**
     * SSL protocol.
     */
    public String getSSLProtocol() { return ep.getSSLProtocol(); }
    public void setSSLProtocol(String SSLProtocol) { ep.setSSLProtocol(SSLProtocol); }


    /**
     * SSL password (if a cert is encrypted, and no password has been provided, a callback
     * will ask for a password).
     */
    public String getSSLPassword() { return ep.getSSLPassword(); }
    public void setSSLPassword(String SSLPassword) { ep.setSSLPassword(SSLPassword); }


    /**
     * SSL cipher suite.
     */
    public String getSSLCipherSuite() { return ep.getSSLCipherSuite(); }
    public void setSSLCipherSuite(String SSLCipherSuite) { ep.setSSLCipherSuite(SSLCipherSuite); }


    /**
     * SSL certificate file.
     */
    public String getSSLCertificateFile() { return ep.getSSLCertificateFile(); }
    public void setSSLCertificateFile(String SSLCertificateFile) { ep.setSSLCertificateFile(SSLCertificateFile); }


    /**
     * SSL certificate key file.
     */
    public String getSSLCertificateKeyFile() { return ep.getSSLCertificateKeyFile(); }
    public void setSSLCertificateKeyFile(String SSLCertificateKeyFile) { ep.setSSLCertificateKeyFile(SSLCertificateKeyFile); }


    /**
     * SSL certificate chain file.
     */
    public String getSSLCertificateChainFile() { return ep.getSSLCertificateChainFile(); }
    public void setSSLCertificateChainFile(String SSLCertificateChainFile) { ep.setSSLCertificateChainFile(SSLCertificateChainFile); }


    /**
     * SSL CA certificate path.
     */
    public String getSSLCACertificatePath() { return ep.getSSLCACertificatePath(); }
    public void setSSLCACertificatePath(String SSLCACertificatePath) { ep.setSSLCACertificatePath(SSLCACertificatePath); }


    /**
     * SSL CA certificate file.
     */
    public String getSSLCACertificateFile() { return ep.getSSLCACertificateFile(); }
    public void setSSLCACertificateFile(String SSLCACertificateFile) { ep.setSSLCACertificateFile(SSLCACertificateFile); }


    /**
     * SSL CA revocation path.
     */
    public String getSSLCARevocationPath() { return ep.getSSLCARevocationPath(); }
    public void setSSLCARevocationPath(String SSLCARevocationPath) { ep.setSSLCARevocationPath(SSLCARevocationPath); }


    /**
     * SSL CA revocation file.
     */
    public String getSSLCARevocationFile() { return ep.getSSLCARevocationFile(); }
    public void setSSLCARevocationFile(String SSLCARevocationFile) { ep.setSSLCARevocationFile(SSLCARevocationFile); }


    /**
     * SSL verify client.
     */
    public String getSSLVerifyClient() { return ep.getSSLVerifyClient(); }
    public void setSSLVerifyClient(String SSLVerifyClient) { ep.setSSLVerifyClient(SSLVerifyClient); }


    /**
     * SSL verify depth.
     */
    public int getSSLVerifyDepth() { return ep.getSSLVerifyDepth(); }
    public void setSSLVerifyDepth(int SSLVerifyDepth) { ep.setSSLVerifyDepth(SSLVerifyDepth); }

    // --------------------  Connection handler --------------------

    Http11AprProcessor newProcessor() {
        Http11AprProcessor processor = null;
        processor = new Http11AprProcessor(maxHttpHeaderSize, ep);
        processor.setAdapter(adapter);
        processor.setMaxKeepAliveRequests(maxKeepAliveRequests);
        processor.setTimeout(timeout);
        processor.setDisableUploadTimeout(disableUploadTimeout);
        processor.setCompression(compression);
        processor.setCompressionMinSize(compressionMinSize);
        processor.setNoCompressionUserAgents(noCompressionUserAgents);
        processor.setCompressableMimeTypes(compressableMimeTypes);
        processor.setRestrictedUserAgents(restrictedUserAgents);
        processor.setSocketBuffer(socketBuffer);
        processor.setMaxSavePostSize(maxSavePostSize);
        processor.setServer(server);
        return processor;
    }

    protected void registerWorker(Http11AprProcessor processor, int count, RequestGroupInfo global) {        
    }
    
    static class AprHttp11ConnectionHandler extends Http11ConnectionHandler implements Handler {
        Http11AprBaseProtocol proto;
        static int count=0;
        RequestGroupInfo global=new RequestGroupInfo();

        // equivalent with old connector params
        ThreadLocal localProcessor = new ThreadLocal();

        AprHttp11ConnectionHandler( Http11AprBaseProtocol proto ) {
            super(null);
            this.proto=proto;
        }

        public boolean process(long socket) {
            Http11AprProcessor processor = null;
            try {
                processor = (Http11AprProcessor) localProcessor.get();
                if (processor == null) {
                    processor=proto.newProcessor();
                    
                    localProcessor.set(processor);
                    if (proto.getDomain() != null) {
                        proto.registerWorker(processor, count++, global);
                    }
                }

                if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_START, null);
                }

                // FIXME: SSL implementation
                /*
                if( proto.secure ) {
                    SSLSupport sslSupport=null;
                    if(proto.sslImplementation != null)
                        sslSupport = proto.sslImplementation.getSSLSupport(socket);
                    processor.setSSLSupport(sslSupport);
                } else {
                    processor.setSSLSupport( null );
                }
                processor.setSocket( socket );
                */

                return processor.process(socket);

            } catch(java.net.SocketException e) {
                // SocketExceptions are normal
                Http11AprBaseProtocol.log.debug
                    (sm.getString
                     ("http11protocol.proto.socketexception.debug"), e);
            } catch (java.io.IOException e) {
                // IOExceptions are normal
                Http11AprBaseProtocol.log.debug
                    (sm.getString
                     ("http11protocol.proto.ioexception.debug"), e);
            }
            // Future developers: if you discover any other
            // rare-but-nonfatal exceptions, catch them here, and log as
            // above.
            catch (Throwable e) {
                // any other exception or error is odd. Here we log it
                // with "ERROR" level, so it will show up even on
                // less-than-verbose logs.
                Http11AprBaseProtocol.log.error
                    (sm.getString("http11protocol.proto.error"), e);
            } finally {
                //       if(proto.adapter != null) proto.adapter.recycle();
                //                processor.recycle();

                if (processor instanceof ActionHook) {
                    ((ActionHook) processor).action(ActionCode.ACTION_STOP, null);
                }
            }
            return false;
        }
    }

    protected static org.apache.commons.logging.Log log
        = org.apache.commons.logging.LogFactory.getLog(Http11AprBaseProtocol.class);

    // -------------------- Various implementation classes --------------------

    protected String domain;

    public String getDomain() {
        return domain;
    }
}
