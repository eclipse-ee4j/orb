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
