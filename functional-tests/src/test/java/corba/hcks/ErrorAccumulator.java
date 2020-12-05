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

//
// Created       : 2000 Nov 24 (Fri) 11:12:04 by Harold Carr.
// Last Modified : 2000 Nov 26 (Sun) 09:30:15 by Harold Carr.
//

package corba.hcks;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.glassfish.pfl.basic.contain.Pair;

public class ErrorAccumulator
{
    public int numberOfErrors;
    public ArrayList<MessageAndException> errors;

    private int numberOfErrorsInTest ;
    public ArrayList<MessageAndException> errorsInTest ;

    public void startTest() {
        numberOfErrorsInTest = 0 ;
        errorsInTest = new ArrayList<MessageAndException>() ;
    }

    public List<MessageAndException> getTestErrors() {
        return errorsInTest ;
    }

    public ErrorAccumulator() 
    {
        numberOfErrors = 0; 
        errors = new ArrayList<MessageAndException>();
        startTest() ;
    }

    public void add(String errorMessage, Throwable t)
    {
        MessageAndException mae = new MessageAndException(errorMessage, t);
        numberOfErrors++;
        errors.add( mae );
        numberOfErrorsInTest++ ;
        errorsInTest.add( mae ) ;
    }

    public int getNumberOfErrors()
    {
        return numberOfErrors;
    }

    public Collection getErrors()
    {
        return errors;
    }

    public void reportErrors(boolean printErrors, boolean printStackTrace)
    {
        U.lf();
        U.sop("==================================================");
        U.sop("Number of errors: " + numberOfErrors);
        U.sop("==================================================");
        U.lf();

        if (printErrors) {
            Iterator iterator = errors.iterator();
            while (iterator.hasNext()) {
                MessageAndException messageAndException = 
                    (MessageAndException) iterator.next();
                U.reportError(printStackTrace,
                              messageAndException.getMessage(),
                              messageAndException.getException());
            }
        }
    }

    public class MessageAndException extends Pair<String,Throwable> {
        public MessageAndException(String message, Throwable exception) {
            super( message, exception ) ;
        }

        public String getMessage() {
            return first() ;
        }

        public Throwable getException() { 
            return second() ; 
        }
    }
}

// End of file.
