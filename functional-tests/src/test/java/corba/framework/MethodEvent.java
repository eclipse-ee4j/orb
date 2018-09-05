/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework ;

import java.lang.reflect.Method ;
import java.util.EventObject ;

/** An event representing entering or exiting a particular method
 * on a particular thread, as recorded by an interceptor with a
 * particular id.
 */
public class MethodEvent 
{
    private String threadId ;
    private String id ;
    private Method method ;

    public static MethodEvent make( String id, Method method ) 
    {
        return new MethodEvent( id, method ) ;
    }

    private MethodEvent( String id, Method method ) 
    {
        this.threadId = Thread.currentThread().toString() ;
        this.id = id ;
        this.method = method ;
    }

    public String getThreadId() { return threadId ; }

    public String getId() { return id ; } 

    public Method getMethod() { return method ; }

    public boolean equals( Object obj ) 
    {
        if (!(obj instanceof MethodEvent))
            return false ;

        MethodEvent other = (MethodEvent)obj ;

        return (id.equals( other.id ) &&
                method.equals( other.method )) ;
    }

    public int hashCode()
    {
        return id.hashCode() ^ method.hashCode() ;
    }

    public String toString() 
    {
        return "MethodEvent[threadId=" + threadId + " id=" + id + 
            " method=" + method + "]" ;
    }
}
