/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * This class is based on example code by David Brown.
 */

package test;

import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer extends Thread implements HttpConstants {

    PrintStream log = null;
    Vector threads = new Vector();
    File root = null;
    int timeout = 5000;
    int workers = 5;
    int port = 8080;
    boolean run;
    Properties aliases = null;
    boolean started = false;
    ServerSocket ss = null;
    
    public static void main(String[] a) throws Exception {
        int port = 8080;
        String rootPath = System.getProperty("user.dir");
        int threads = 5;
        boolean error = false;
        Properties aliases = new Properties();
        
        for (int i = 0; i < a.length; i++) {
            String arg = a[i];
            if (arg.equals("-port")) {
                port = Integer.parseInt(a[i+1]);
                i++;
            } else if (arg.equals("-docroot")) {
                rootPath = a[i+1];
                i++;
            } else if (arg.equals("-threads")) {
                threads = Integer.parseInt(a[i+1]);
                i++;
            } else if (arg.equals("-alias")) {
                String alias = a[i+1];
                int index = alias.indexOf('=');
                if (index >=0) {
                    String key = alias.substring(0,index);
                    String value = alias.substring(index+1);
                    aliases.put(key,value);
                } else {
                    System.out.println(alias + " is an invalid alias. Use x=y form.");
                    error = true;
                }
                i++;
            } else {
                error = true;
            }
        }
        
        File root = new File(rootPath);
        if (!root.exists()) {
            System.out.println("docroot '" + rootPath + "' does not exist.");
            error = true;
        }
        
        if (!error) {
            WebServer server = new WebServer(port,root,threads,aliases,null);
            server.start();
            System.out.println("Ready.");
        } else {
            System.out.println("Usage: test.WebServer [-port n][-docroot docRootPath][-threads n][-alias x=y]");
        }
    }
    
    public WebServer (int port, File root, int threads) throws Exception {
        this(port,root,threads,null,null);
    }
    
    public WebServer (int port, File root, int threads,
                      Properties aliases, PrintStream log) throws Exception {
        this.root = root;
        this.port = port;
        this.workers = threads;
        this.log = log;
        run = true;
        this.aliases = aliases;
        if (this.log == null) {
            this.log = new PrintStream(new FileOutputStream(new File(root,"WebServer.log")));
        }
        if (aliases != null) {
            if (aliases.size() > 0) {
                this.log.println("aliases: " + aliases);
            } else {
                this.log.println("aliases: <empty>");
            }
        }
    }

    /* print to the log file */
    protected void log(String s) {
        if (log != null) {
            synchronized (log) {
                log.println(s);
                log.flush();
            }
        }
    }
    
    public boolean waitTillReady() {
        if (!Thread.currentThread().isAlive()) {
            return false; // Died.
        }
        synchronized(this) {
            while (!started) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
        return true;
    }
    
    public void quit() {
        try {
            ss.close();
        } catch (Exception e){}
        
        run = false;
    }
    
    public void run () {
        
        try {
            /* start worker threads */
            for (int i = 0; i < workers; ++i) {
                Worker w = new Worker(this);
                (new Thread(w, "worker #"+i)).start();
                threads.addElement(w);
            }

            ss = new ServerSocket(port);
            
            synchronized(this) {
                started = true;
                notifyAll();
            }
            
            while (run) {
                try {
                    Socket s = ss.accept();
                    Worker w = null;
                    synchronized (threads) {
                        if (threads.isEmpty()) {
                            Worker ws = new Worker(this);
                            ws.setSocket(s);
                            (new Thread(ws, "additional worker")).start();
                        } else {
                            w = (Worker) threads.elementAt(0);
                            threads.removeElementAt(0);
                            w.setSocket(s);
                        }
                    }
                } catch (IOException e) {}
            }
            
            
            
        } catch (Exception e) {
            log.println("WebServer died. Caught " + e);
        }
    }
}


class Worker implements HttpConstants, Runnable {
    final static int BUF_SIZE = 2048;

    static final byte[] EOL = {(byte)'\r', (byte)'\n' };

    /* buffer to use for requests */
    byte[] buf;
    /* Socket to client we're handling */
    private Socket s;
    private WebServer server;
    
    Worker(WebServer server) {
        this.server = server;
        buf = new byte[BUF_SIZE];
        s = null;
    }

    synchronized void setSocket(Socket s) {
        this.s = s;
        notify();
    }

    public synchronized void run() {
        while(server.run) {
            if (s == null) {
                /* nothing to do */
                try {
                    wait();
                } catch (InterruptedException e) {
                    /* should not happen */
                    continue;
                }
            }
            try {
                handleClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
            /* go back in wait queue if there's fewer
             * than numHandler connections.
             */
            s = null;
            Vector pool = server.threads;
            synchronized (pool) {
                if (pool.size() >= server.workers) {
                    /* too many threads, exit this one */
                    return;
                } else {
                    pool.addElement(this);
                }
            }
        }
    }

    void handleClient() throws IOException {
        InputStream is = new BufferedInputStream(s.getInputStream());
        PrintStream ps = new PrintStream(s.getOutputStream());
        /* we will only block in read for this many milliseconds
         * before we fail with java.io.InterruptedIOException,
         * at which point we will abandon the connection.
         */
        s.setSoTimeout(server.timeout);
        s.setTcpNoDelay(true);
        /* zero out the buffer from last time */
        for (int i = 0; i < BUF_SIZE; i++) {
            buf[i] = 0;
        }
        try {
            /* We only support HTTP GET/HEAD, and don't
             * support any fancy HTTP options,
             * so we're only interested really in
             * the first line.
             */
            int nread = 0, r = 0;

        outerloop:
            while (nread < BUF_SIZE) {
                r = is.read(buf, nread, BUF_SIZE - nread);
                if (r == -1) {
                    /* EOF */
                    return;
                }
                int i = nread;
                nread += r;
                for (; i < nread; i++) {
                    if (buf[i] == (byte)'\n' || buf[i] == (byte)'\r') {
                        /* read one line */
                        break outerloop;
                    }
                }
            }

            /* are we doing a GET or just a HEAD */
            boolean doingGet;
            /* beginning of file name */
            int index;
            if (buf[0] == (byte)'G' &&
                buf[1] == (byte)'E' &&
                buf[2] == (byte)'T' &&
                buf[3] == (byte)' ') {
                doingGet = true;
                index = 4;
            } else if (buf[0] == (byte)'H' &&
                       buf[1] == (byte)'E' &&
                       buf[2] == (byte)'A' &&
                       buf[3] == (byte)'D' &&
                       buf[4] == (byte)' ') {
                doingGet = false;
                index = 5;
            } else {
                /* we don't support this method */
                ps.print("HTTP/1.0 " + HTTP_BAD_METHOD +
                         " unsupported method type: ");
                ps.write(buf, 0, 5);
                ps.write(EOL);
                ps.flush();
                s.close();
                return;
            }

            int i = 0;
            /* find the file name, from:
             * GET /foo/bar.html HTTP/1.0
             * extract "/foo/bar.html"
             */
            for (i = index; i < nread; i++) {
                if (buf[i] == (byte)' ') {
                    break;
                }
            }
            String fname = new String(buf, 0, index,i-index);
            fname = checkAliases(fname).replace('/', File.separatorChar);
            if (fname.startsWith(File.separator)) {
                fname = fname.substring(1);
            }

            File targ = new File(server.root, fname);
            if (targ.isDirectory()) {
                File ind = new File(targ, "index.html");
                if (ind.exists()) {
                    targ = ind;
                }
            }
 
            boolean OK = printHeaders(targ, ps);
            if (doingGet) {
                if (OK) {
                    server.log("GET "+targ+". OK");
                    sendFile(targ, ps);
                } else {
                    server.log("GET "+targ+". NOT FOUND");
                    send404(targ, ps);
                }
            } else {
                server.log("HEAD "+targ+" OK");   
            }
        } finally {
            ps.flush();
            ps.close();
            s.close();
        }
    }

    String checkAliases (String name) {
        String result = name;
        if (server.aliases != null) {
            String temp = (String) server.aliases.get(name);
            if (temp != null) {
                result = temp;
            }
        }
        server.log.println("checkAliases: " + name + " --> " + result);
        return result;
    }
    
    boolean printHeaders(File targ, PrintStream ps) throws IOException {
        boolean ret = false;
        int rCode = 0;
        if (!targ.exists()) {
            rCode = HTTP_NOT_FOUND;
            ps.print("HTTP/1.0 " + HTTP_NOT_FOUND + " not found");
            ps.write(EOL);
            ret = false;
        }  else {
            rCode = HTTP_OK;
            ps.print("HTTP/1.0 " + HTTP_OK+" OK");
            ps.write(EOL);
            ret = true;
        }
        server.log("From " +s.getInetAddress().getHostAddress()+": GET " +
                   targ.getAbsolutePath()+"-->"+rCode);
        ps.print("Server: Simple java");
        ps.write(EOL);
        ps.print("Date: " + (new Date()));
        ps.write(EOL);
        if (ret) {
            if (!targ.isDirectory()) {
                ps.print("Content-length: "+targ.length());
                ps.write(EOL);
                ps.print("Last Modified: " + (new
                                              Date(targ.lastModified())));
                ps.write(EOL);
                String name = targ.getName();
                int ind = name.lastIndexOf('.');
                String ct = null;
                if (ind > 0) {
                    ct = (String) map.get(name.substring(ind));
                }
                if (ct == null) {
                    ct = "unknown/unknown";
                }
                ps.print("Content-type: " + ct);
                ps.write(EOL);
            } else {
                ps.print("Content-type: text/html");
                ps.write(EOL);
            }
            ps.write(EOL);  // Bryan
        }
        return ret;
    }

void send404(File targ, PrintStream ps) throws IOException {
    ps.write(EOL);
    ps.write(EOL);
    ps.println("Not Found\n\n"+
               "The requested resource was not found.\n");
}

void sendFile(File targ, PrintStream ps) throws IOException {
    InputStream is = null;
    if (targ.isDirectory()) {
        listDirectory(targ, ps);
        return;
    } else {
        server.log.print("sendFile: " + targ.getAbsolutePath() + ", length = " + targ.length());
        is = new FileInputStream(targ.getAbsolutePath());
    }
    int sent = 0;
    try {
        int n;
        while ((n = is.read(buf)) > 0) {
            ps.write(buf, 0, n);
            sent += n;
        }
        server.log.println(", sent = " + sent);
    } catch (Exception e) {
        server.log.println(", CAUGHT = " + e);
    } finally {
        //            ps.flush();
        is.close();
    }
}

/* mapping of file extensions to content-types */
static java.util.Hashtable map = new java.util.Hashtable();

static {
    fillMap();
}
static void setSuffix(String k, String v) {
    map.put(k, v);
}

static void fillMap() {
    setSuffix("", "content/unknown");
    setSuffix(".uu", "application/octet-stream");
    setSuffix(".exe", "application/octet-stream");
    setSuffix(".ps", "application/postscript");
    setSuffix(".zip", "application/zip");
    setSuffix(".sh", "application/x-shar");
    setSuffix(".tar", "application/x-tar");
    setSuffix(".snd", "audio/basic");
    setSuffix(".au", "audio/basic");
    setSuffix(".wav", "audio/x-wav");
    setSuffix(".gif", "image/gif");
    setSuffix(".jpg", "image/jpeg");
    setSuffix(".jpeg", "image/jpeg");
    setSuffix(".htm", "text/html");
    setSuffix(".html", "text/html");
    setSuffix(".text", "text/plain");
    setSuffix(".c", "text/plain");
    setSuffix(".cc", "text/plain");
    setSuffix(".c++", "text/plain");
    setSuffix(".h", "text/plain");
    setSuffix(".pl", "text/plain");
    setSuffix(".txt", "text/plain");
    setSuffix(".java", "text/plain");
    setSuffix(".class", "application/java");
    setSuffix(".clz", "application/java");
}

    void listDirectory(File dir, PrintStream ps) throws IOException {
        ps.println("<TITLE>Directory listing</TITLE><P>\n");
        ps.println("<A HREF=\"..\">Parent Directory</A><BR>\n");
        String[] list = dir.list();
        for (int i = 0; list != null && i < list.length; i++) {
            File f = new File(dir, list[i]);
            if (f.isDirectory()) {
                ps.println("<A HREF=\""+list[i]+"/\">"+list[i]+"/</A><BR>");
            } else {
                ps.println("<A HREF=\""+list[i]+"\">"+list[i]+"</A><BR");
            }
        }
        ps.println("<P><HR><BR><I>" + (new Date()) + "</I>");
    }

}

interface HttpConstants {
    /** 2XX: generally "OK" */
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_RESET = 205;
    public static final int HTTP_PARTIAL = 206;

    /** 3XX: relocation/redirect */
    public static final int HTTP_MULT_CHOICE = 300;
    public static final int HTTP_MOVED_PERM = 301;
    public static final int HTTP_MOVED_TEMP = 302;
    public static final int HTTP_SEE_OTHER = 303;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_USE_PROXY = 305;

    /** 4XX: client error */
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_BAD_METHOD = 405;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_PROXY_AUTH = 407;
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_PRECON_FAILED = 412;
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    public static final int HTTP_REQ_TOO_LONG = 414;
    public static final int HTTP_UNSUPPORTED_TYPE = 415;

    /** 5XX: server error */
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_INTERNAL_ERROR = 501;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_UNAVAILABLE = 503;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION = 505;
}



