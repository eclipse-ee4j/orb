/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.dynamicrmiiiop.testclasses ; 

public class MyApplicationExceptionBase extends Exception
{
    public MyApplicationExceptionBase( String msg )
    {
        super( msg ) ;
    }

    public boolean equals( Object obj )
    {
        if (this == obj)
            return true ;

        if (!obj.getClass().equals(getClass()))
            return false ;

        MyApplicationExceptionBase other = (MyApplicationExceptionBase)obj ;

        if (getMessage() == null)
            return other.getMessage() == null ;

        return getMessage().equals( other.getMessage() ) ;
    }
}
