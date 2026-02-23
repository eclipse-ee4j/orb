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

package com.sun.corba.ee.impl.oa.poa ;

import com.sun.corba.ee.spi.logging.POASystemException ;
import com.sun.corba.ee.spi.trace.PoaFSM;

import java.util.concurrent.locks.Condition ;

import org.glassfish.pfl.basic.fsm.Action;
import org.glassfish.pfl.basic.fsm.FSM;
import org.glassfish.pfl.basic.fsm.FSMImpl;
import org.glassfish.pfl.basic.fsm.Guard;
import org.glassfish.pfl.basic.fsm.Guard.Base.IntFunc;
import org.glassfish.pfl.basic.fsm.Input;
import org.glassfish.pfl.basic.fsm.Runner;
import org.glassfish.pfl.basic.fsm.State;
import org.glassfish.pfl.basic.fsm.StateEngine;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;

import static org.glassfish.pfl.basic.fsm.Guard.Base.constant;
import static org.glassfish.pfl.basic.fsm.Guard.Base.eq;
import static org.glassfish.pfl.basic.fsm.Guard.Base.gt;
import static org.glassfish.pfl.basic.fsm.Guard.Base.makeGuard;

/** AOMEntry represents a Servant or potential Servant in the ActiveObjectMap.
* It may be in several states to allow for long incarnate or etherealize 
* operations.  The methods on this class mostly represent input symbols to
* the state machine that controls the lifecycle of the entry.  A library is
* used to build the state machine rather than the more usual state pattern
* so that the state machine transitions are explicitly visible.
*/
@PoaFSM
public class AOMEntry extends FSMImpl {
    private static final POASystemException wrapper =
        POASystemException.self ;

    private Runner runner ;
    private final Thread[] etherealizer ;   // The actual etherealize operation 
                                            // for this entry.  It is 
                                            // represented as a Thread because
                                            // the POA.deactivate_object never 
                                            // waits for the completion.
    private final int[] counter ;           // single element holder for counter 
                                            // accessed in actions
    private final Condition wait ;          // accessed in actions

    final POAImpl poa ;

    public static final State INVALID = new State( "Invalid", 
        State.Kind.INITIAL ) ;

    public static final State INCARN  = new State( "Incarnating" ) {
        @Override
        public void postAction( FSM fsm ) {
            AOMEntry entry = (AOMEntry)fsm ;
            entry.wait.signalAll() ;
        }
    };

    public static final State VALID   = new State( "Valid" ) ;

    public static final State ETHP    = new State( "EtherealizePending" ) ;

    public static final State ETH     = new State( "Etherealizing" ) {
        @Override
        public FSM preAction( FSM fsm ) {
            AOMEntry entry = (AOMEntry)fsm ;
            Thread etherealizer = entry.etherealizer[0] ;
            if (etherealizer != null) {
                etherealizer.start();
            }
            return null ;
        }

        @Override
        public void postAction( FSM fsm ) {
            AOMEntry entry = (AOMEntry)fsm ;
            entry.wait.signalAll() ;
        }
    };

    public static final State DESTROYED = new State( "Destroyed" ) ;

    static final Input START_ETH    = new Input.Base( "startEtherealize" ) ;
    static final Input ETH_DONE     = new Input.Base( "etherealizeDone" ) ;
    static final Input INC_DONE     = new Input.Base( "incarnateDone" ) ;
    static final Input INC_FAIL     = new Input.Base( "incarnateFailure" ) ;
    static final Input ACTIVATE     = new Input.Base( "activateObject" ) ;
    static final Input ENTER        = new Input.Base( "enter" ) ;
    static final Input EXIT         = new Input.Base( "exit" ) ;

    private static final Action incrementAction =
        new Action.Base( "increment" ) {
            public void doIt( FSM fsm, Input in ) {
                AOMEntry entry = (AOMEntry)fsm ;
                entry.counter[0]++ ;
            }
        } ;

    private static final Action decrementAction =
        new Action.Base( "decrement" ) {
            public void doIt( FSM fsm, Input in ) {
                AOMEntry entry = (AOMEntry)fsm ;
                if (entry.counter[0] > 0) {
                    entry.counter[0]--;
                } else {
                    throw wrapper.aomEntryDecZero();
                }
            }
        } ;

    private static final Action throwIllegalStateExceptionAction =
        new Action.Base( "throwIllegalStateException" ) {
            public void doIt( FSM fsm, Input in ) {
                throw new IllegalStateException(
                    "No transitions allowed from the DESTROYED state" ) ;
            }
        } ;

    private static final Action oaaAction =
        new Action.Base( "throwObjectAlreadyActive" ) {
            public void doIt( FSM fsm, Input in ) {
                throw new RuntimeException( new ObjectAlreadyActive() ) ;
            }
        } ;

    private static final Guard waitGuard = new Guard.Base( "wait" ) {
        public Guard.Result evaluate( FSM fsm, Input in ) {
            AOMEntry entry = (AOMEntry)fsm ;
            try {
                entry.wait.await() ;
            } catch (InterruptedException exc) {
                wrapper.waitGuardInterrupted() ;
            }

            return Guard.Result.DEFERRED ;
        }
    } ;

    private static final IntFunc counterFunc =
        new Guard.Base.IntFunc( "counterFunc" ) {
            public Integer evaluate( FSM fsm, Input in ) {
                AOMEntry entry = (AOMEntry)fsm ;
                return entry.counter[0] ;
            }
        } ;

    private static final IntFunc one = constant( 1 ) ;
    private static final IntFunc zero = constant( 0 ) ;

    private static final Guard greaterZeroGuard =
        makeGuard( gt( counterFunc, zero ) ) ;
    private static final Guard zeroGuard =
        makeGuard( eq( counterFunc, zero ) ) ;
    private static final Guard greaterOneGuard =
        makeGuard( gt( counterFunc, one ) ) ;
    private static final Guard oneGuard =
        makeGuard( eq( counterFunc, one ) ) ;

    private static final StateEngine engine = StateEngine.create() ;

    static {
        //          State,   Input,     Guard,                  Action,             new State

        engine.add( INVALID, ENTER,                             incrementAction,    INCARN      ) ;
        engine.add( INVALID, ACTIVATE,                          null,               VALID       ) ;
        engine.setDefault( INVALID ) ;

        engine.add( INCARN,  ENTER,     waitGuard,              null,               INCARN      ) ;
        engine.add( INCARN,  EXIT,                              null,               INCARN      ) ;
        engine.add( INCARN,  START_ETH, waitGuard,              null,               INCARN      ) ;
        engine.add( INCARN,  INC_DONE,                          null,               VALID       ) ;
        engine.add( INCARN,  INC_FAIL,                          decrementAction,    INVALID     ) ;  
        engine.add( INCARN,  ACTIVATE,                          oaaAction,          INCARN      ) ;  

        engine.add( VALID,   ENTER,                             incrementAction,    VALID       ) ;
        engine.add( VALID,   EXIT,                              decrementAction,    VALID       ) ;
        engine.add( VALID,   START_ETH, greaterZeroGuard,       null,               ETHP        ) ;
        engine.add( VALID,   START_ETH, zeroGuard,              null,               ETH         ) ;
        engine.add( VALID,   ACTIVATE,                          oaaAction,          VALID       ) ;  

        engine.add( ETHP,    ENTER,     waitGuard,              null,               ETHP        ) ;
        engine.add( ETHP,    START_ETH,                         null,               ETHP        ) ;
        engine.add( ETHP,    EXIT,      greaterOneGuard,        decrementAction,    ETHP        ) ;
        engine.add( ETHP,    EXIT,      oneGuard,               decrementAction,    ETH         ) ;
        engine.add( ETHP,    ACTIVATE,                          oaaAction,          ETHP        ) ;  

        engine.add( ETH,     START_ETH,                         null,               ETH         ) ;
        engine.add( ETH,     ETH_DONE,                          null,               DESTROYED   ) ;
        engine.add( ETH,     ENTER,     waitGuard,              null,               ETH         ) ;
        engine.add( ETH,     ACTIVATE,                          oaaAction,          ETH ) ;  
        
        engine.setDefault( DESTROYED, throwIllegalStateExceptionAction, DESTROYED ) ;

        engine.done() ;
    }

    public AOMEntry( POAImpl poa )
    {
        super( engine, INVALID ) ;
        runner = new Runner( this ) ;
        this.poa = poa ;
        etherealizer = new Thread[1] ;
        etherealizer[0] = null ;
        counter = new int[1] ;
        counter[0] = 0 ;
        wait = poa.makeCondition() ;
    }

    @InfoMethod
    private void state( State state ) { }

    @PoaFSM
    @Override
    public void setState( State state ) {
        super.setState( state ) ;
        state( getState() ) ;
    }

    // Methods that drive the FSM: the real interface to this class
    // Most just call the doIt method, but startEtherealize needs
    // the etherealizer.
    public void startEtherealize( Thread etherealizer ) 
    { 
        this.etherealizer[0] = etherealizer ;
        runner.doIt( START_ETH ) ; 
    }

    public void etherealizeComplete() { runner.doIt( ETH_DONE ) ; }
    public void incarnateComplete() { runner.doIt( INC_DONE ) ; }
    public void incarnateFailure() { runner.doIt( INC_FAIL ) ; }
    public void enter() { runner.doIt( ENTER ) ; }
    public void exit() { runner.doIt( EXIT ) ; }

    public void activateObject() throws ObjectAlreadyActive { 
        try {
            runner.doIt( ACTIVATE ) ; 
        } catch (RuntimeException exc) {
            Throwable thr = exc.getCause() ;
            if (thr instanceof ObjectAlreadyActive) {
                throw (ObjectAlreadyActive) thr;
            } else {
                throw exc;
            }
        }
    }
}
