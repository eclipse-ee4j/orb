/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.nortel ;

import java.io.Serializable ;

public class UserInfo implements Serializable {
    private String firstName ;
    private String lastName ;

    public UserInfo( String fname, String lname ) {
        firstName = fname ;
        lastName = lname ;
    }

    public String toString() {
        return firstName + " " + lastName ;
    }

    public int hashCode() {
        return toString().hashCode() ;
    }

    public boolean equals( Object obj ) {
        return toString().equals( obj.toString() ) ;
    }

    public String getFirstName() {
        return firstName ;
    }

    public String getLastName() {
        return lastName ;
    }
}

