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
