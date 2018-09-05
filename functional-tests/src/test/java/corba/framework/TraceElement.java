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

public class TraceElement
{
    private boolean isEnter ;
    private MethodEvent event ;

    public TraceElement( boolean isEnter, MethodEvent event ) 
    {
        this.isEnter = isEnter ;
        this.event = event ;
    }

    public boolean isEnter() { return isEnter ; }

    public MethodEvent getEvent() { return event ; }

    public boolean equals( Object obj ) 
    {
        if (!(obj instanceof TraceElement)) 
            return false ;

        TraceElement other = (TraceElement)obj ;

        return (event.equals( other.event ) &&
                isEnter == other.isEnter) ;
    }

    public int hashCode()
    {
        return event.hashCode() + (isEnter ? 1711 : 0) ;
    }

    public String toString() 
    {
        return "TraceElement[isEnter=" + isEnter + " event=" + event + "]" ;
    }
}
