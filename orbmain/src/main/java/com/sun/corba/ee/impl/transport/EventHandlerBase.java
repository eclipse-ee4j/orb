/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;
import com.sun.corba.ee.spi.threadpool.NoSuchWorkQueueException;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.EventHandler;

import java.nio.channels.SelectionKey;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public abstract class EventHandlerBase
    implements
        EventHandler
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    protected ORB orb;
    protected Work work;
    protected boolean useWorkerThreadForEvent;
    protected boolean useSelectThreadToWait;
    protected SelectionKey selectionKey;

    ////////////////////////////////////////////////////
    //
    // EventHandler methods
    //

    public void setUseSelectThreadToWait(boolean x)
    {
        useSelectThreadToWait = x;
    }

    public boolean shouldUseSelectThreadToWait()
    {
        return useSelectThreadToWait;
    }

    public void setSelectionKey(SelectionKey selectionKey)
    {
        this.selectionKey = selectionKey;
    }

    public SelectionKey getSelectionKey()
    {
        return selectionKey;
    }

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    /*
     * NOTE:
     * This is not thread-safe by design.
     * Only one thread should call it - a reader/listener/select thread.
     * Not stateless: interest ops, registration.
     */
    @Transport
    public void handleEvent()
    {
        getSelectionKey().interestOps(getSelectionKey().interestOps() &
                                      (~ getInterestOps()));
        if (shouldUseWorkerThreadForEvent()) {
            Throwable throwable = null;
            try {
                display( "add work to pool 0") ;
                orb.getThreadPoolManager().getThreadPool(0)
                    .getWorkQueue(0).addWork(getWork());
            } catch (NoSuchThreadPoolException e) {
                throwable = e;
            } catch (NoSuchWorkQueueException e) {
                throwable = e;
            }
            // REVISIT: need to close connection.
            if (throwable != null) {
                display( "unexpected exception", throwable ) ;
                throw wrapper.noSuchThreadpoolOrQueue(throwable, 0);
            }
        } else {
            display( "doWork" ) ;
            getWork().doWork();
        }
    }

    public boolean shouldUseWorkerThreadForEvent()
    {
        return useWorkerThreadForEvent;
    }

    public void setUseWorkerThreadForEvent(boolean x)
    {
        useWorkerThreadForEvent = x;
    }

    public void setWork(Work work)
    {
        this.work = work;
    }

    public Work getWork()
    {
        return work;
    }
}

// End of file.
