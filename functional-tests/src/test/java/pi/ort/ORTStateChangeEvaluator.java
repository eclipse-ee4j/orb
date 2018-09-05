/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.ort;

import org.omg.PortableInterceptor.*;

/** 
 * ORTStateChangeEvaluator is a Singleton used for
 * 1. registering Adapter and AdapterState Changes from the IORInterceptor
 * 2. evaluating that the statechanges happed as expected
 * 3. notifying DelayServant.method() completion 
 */
public class ORTStateChangeEvaluator {
    private static ORTStateChangeEvaluator ortStateChangeEvaluatorInstance = 
        new ORTStateChangeEvaluator ( );

    String[] poasWhoseStateChangesAreReported;
    short currentAdapterState;
    int managerId;
    short currentAdapterManagerState;
    String notificationToken = null;
    

    boolean registerAdapterStateChangeCalled;

    private ORTStateChangeEvaluator( ) {
        resetAllStates( );
    }

    public static ORTStateChangeEvaluator getInstance( ) {
        return ortStateChangeEvaluatorInstance;
    }

    /**
     *  The AdapterStateChange from the IORInterceptor is notified here.
     */
    public void registerAdapterStateChange(ObjectReferenceTemplate[] templates,
        short  state ) 
    {
        System.out.println( "registerAdapterState Change called...." );
        System.out.flush( );
        if( ( templates == null )
          ||( templates.length == 0 ) )
        {
            System.err.println(
                "Adapter State Change called with no templates");
            System.exit( -1 );
        }
        this.poasWhoseStateChangesAreReported = new String[templates.length];
        for( int i = 0; i < templates.length; i++ ) {
            String[] adapterNames = templates[i].adapter_name( );
            poasWhoseStateChangesAreReported[i] = 
                adapterNames[ adapterNames.length - 1 ]; 
            System.out.println("\t- POAs Whose State Change Is Reported : " +
                poasWhoseStateChangesAreReported[i] );
            System.out.println("\t  - POA Parent to Child list.." );
            for( int j = 0; j < adapterNames.length; j++ ) {
                System.out.println( "\t\t + " + adapterNames[j] );
                System.out.flush( );
            }
            System.out.flush( );
        }
        this.currentAdapterState = state;
        System.out.println( "\t  - State Changed to " + state );
        System.out.flush( );
        registerAdapterStateChangeCalled = true; 
    }

    

    /**
     *  The AdapterManagerStateChange from the IORInterceptor is notified here.
     */
    public void registerAdapterManagerStateChange( int managerId, short state )
    {
        this.managerId = managerId;
        this.currentAdapterManagerState = state;
        System.out.println( 
            "\t- AdapterManagerStateChange Manager Id = " + managerId +
            " state = " + state );
        System.out.flush( );
    }

    /**
     * Compares the list of POAs whose destroyed notifications are expected Vs.
     * the list of POAs whose destroyed notifications are actually recieved.
     * If it doesn't match it returns false, else the evaluation passed.
     */
    public boolean evaluateAdapterStateChange( String[] poas ) {
        if( currentAdapterState != NON_EXISTENT.value ) 
        {
            System.err.println( "AdapterStateChange reported = " + 
                currentAdapterState );
            System.err.println( "AdapterStateChange Expected = " + 
                NON_EXISTENT.value );
            return false;
        }
        boolean check =  checkAllPOAStateChangesReported( poas );
        return check;
         
    }

    /**
     * Very similar to the previous method with an extra check to see if the
     * token passed matches the notificationToken recieved from DelayServant.
     */
    public boolean evaluateAdapterStateChange( String[] poas, String token ) {
        if( ( notificationToken == null )
          ||( !notificationToken.equals( token ) ) ) {
            System.err.println( "POA Destroy is notified before completing " +
                " invocations...." );
            System.err.flush( );
            return false;
        }
        return evaluateAdapterStateChange( poas );
    }


    /**
     * A simple utility to compare two arrays.
     * _REVISIT_: Ken might have an utility to compare two arrays, just use
     * that.
     */
    private boolean checkAllPOAStateChangesReported( String[] poas ) {
        if( poas.length != poasWhoseStateChangesAreReported.length ) {
            return false;
        }
        int i = 0, j = 0;
        boolean matchFound;
        for( i = 0; i < poas.length; i++ ) {
            matchFound = false;
            for( j = 0; j < poas.length; j++ ) {
                if( poasWhoseStateChangesAreReported[j].equals( poas[i] ) ) {
                    matchFound = true;
                    break;
                }
            }
            if( !matchFound ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the AdapterManagerState notified from IOR interceptor with
     * passed in state. If it doesn't match then, it returns false.
     */
    public boolean evaluateAdapterManagerState( short state ) {
        if( currentAdapterManagerState == state ) {
            return true;
        }
        System.err.println( "In evaluateAdapterManagerState " );
        System.err.print( "currentAdapterManagerState = " + 
            currentAdapterManagerState );
        System.err.print( " is Not Equal To " + state );
        return false;
    }

    /**
     * resets all the variable states to allow for a new test to start.
     */
    void resetAllStates( ) {
        currentAdapterState = 0;
        currentAdapterManagerState = 0;
        notificationToken = null;
        poasWhoseStateChangesAreReported = null;
        managerId = 0;
        registerAdapterStateChangeCalled = false;
    }

    /**
     * DelayServant will invoke this method with a notificationToken passed to
     * it to signal the method completion.
     */
    public void notificationTokenFromDelayServant( String token ) {
        System.out.println( "notificationTokenFromDelayServant called with " +
            " the token = " + token );
        System.out.flush( );
        notificationToken = token;
    }
}


  
