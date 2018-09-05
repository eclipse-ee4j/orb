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

import java.util.Iterator ;
import java.util.List ;
import java.util.ArrayList ;

public class TraceAccumulator implements MethodEventListener
{
    private List elements ;

    public TraceAccumulator()
    {
        clear() ;
    }

    public void clear() 
    {
        elements = new ArrayList() ;
    }

    private void addElement( boolean isEnter, MethodEvent event ) 
    {
        TraceElement tel = new TraceElement( isEnter, event ) ;
        elements.add( tel ) ;
    }

    public void methodEntered( MethodEvent event ) 
    {
        addElement( true, event ) ;
    }

    public void methodExited( MethodEvent event ) 
    {
        addElement( false, event ) ;
    }

    public List getTrace()      // List<TraceElement>
    {
        return elements ;
    }

    public boolean validate( List expectedTrace ) // List<TraceElement>
    {
        Iterator iter1 = elements.iterator() ;
        Iterator iter2 = expectedTrace.iterator() ;
        while (iter1.hasNext() && iter2.hasNext()) {
            TraceElement tel1 = (TraceElement)(iter1.next()) ;
            TraceElement tel2 = (TraceElement)(iter2.next()) ;
            if (!tel1.equals( tel2 ))
                return false ;
        }

        return iter1.hasNext() == iter2.hasNext() ;
    }
}

