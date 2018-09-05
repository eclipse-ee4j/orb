/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.lbq ;

import org.glassfish.pfl.basic.func.UnaryFunction;

public class CommandQueue {
    private Command end ; // always points to sink
    private Command head ; // points to last command before sink
    private Command tail ; // points to first command to evaluate

    public interface Event { } 

    public interface Command extends UnaryFunction<Event, Command> { 
        void setNext( Command next ) ;
    }
  
    public static abstract class CommandBase implements Command {
        protected Command next = null ;

        public void setNext( Command next ) {
            this.next = next ;
        }

        public Command evaluate( Event ev ) {
            action( ev ) ;
            return next ;
        }

        protected void action( Event ev ) {
        }
    }

    private static class Sink extends CommandBase {
        public Sink() {
            setNext( this ) ;
        }
    }

    public class Delay extends CommandBase {
        private int count ;

        /** Do nothing the first numEvents calls.
         */
        public Delay( int numEvents ) {
            this.count = numEvents ;
        }

        public Command evaluate( Event ev ) {
            count-- ;
            if (count == 0)
                return next ;
            return this ;
        }
    }

    public CommandQueue() {
        end = new Sink() ;
        tail = end ;
        head = end ;
    }

    private void doAdd( Command cmd ) {
        if (tail == end) {
            head = cmd ;
        } else {
            tail.setNext( cmd ) ;
        }

        tail = cmd ;
        cmd.setNext( end ) ;
    }

    /** Add this command to the queue.  This command must be
     * triggered (by event()) count times before it calls cmd.
     * If count == 0, the first event() will execute cmd.
     */
    public void add( int count, Command cmd ) {
        if (count > 0)
            doAdd( new Delay( count ) ) ;
        doAdd( cmd ) ;
    }

    public void event( Event ev ) {
        head = head.evaluate( ev ) ;
        if (head == end)
            tail = end ;
    }

    private static void p( String msg ) {
        System.out.println( msg ) ;
    }

    private static class Display extends CommandBase {
        String msg ;

        public Display( String msg ) {
            this.msg = msg ;
        }

        public void action( Event ev ) {
            p( msg ) ;
        }
    }

    public static void main( String[] args ) {
        p( "Testing CommandQueue" ) ;
        CommandQueue cq = new CommandQueue() ;
        Event ev = new Event() {} ;
        cq.event( ev ) ; // should do nothing
        cq.event( ev ) ; // should do nothing

        Command d1 = new Display( "Display 1" ) ;       
        Command d2 = new Display( "Display 2" ) ;       
        Command d3 = new Display( "Display 3" ) ;       
        Command d4 = new Display( "Display 4" ) ;       

        cq.add( 0, d1 ) ;
        cq.add( 5, d2 ) ;
        cq.add( 8, d3 ) ;
        cq.add( 3, d4 ) ;

        for (int ctr=0; ctr<25; ctr++) {
            p( "Event " + ctr ) ;
            cq.event( ev ) ;
        }

        p( "Add to queue while running" ) ;

        cq.add( 0, d1 ) ;
        cq.add( 5, d2 ) ;
        cq.add( 8, d3 ) ;

        for (int ctr=0; ctr<11; ctr++) {
            p( "Event " + ctr ) ;
            cq.event( ev ) ;
        }

        cq.add( 3, d4 ) ;

        for (int ctr=11; ctr<25; ctr++) {
            p( "Event " + ctr ) ;
            cq.event( ev ) ;
        }

    }
}
