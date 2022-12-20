/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

package rmic;

import test.Test;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.glassfish.rmic.iiop.Constants;
import org.glassfish.rmic.iiop.CompoundType;
import org.glassfish.rmic.iiop.ContextStack;

public class InnerTest extends Test implements Constants {

    private ByteArrayOutputStream out = null;
    private TestEnv env = null;
    private ContextStack stack = null;

    /*
      Outer                   Inner                   Name
      ---------------------   ---------------------   ----
      Remote interface        Remote interface        RR
      Remote interface        interface               RI
      Remote interface        Value                   RV
      Remote interface        Servant                 RS
      Remote interface        Abstract interface      RA
 
      interface               Remote interface        IR
      interface               interface               II
      interface               Value                   IV
      interface               Servant                 IS
      interface               Abstract interface      IA

      Value                   Remote interface        VR
      Value                   interface               VI
      Value                   Value                   VV
      Value                   Servant                 VS
      Value                   Abstract interface      VA

      Servant                 Remote interface        SR
      Servant                 interface               SI
      Servant                 Value                   SV
      Servant                 Servant                 SS
      Servant                 Abstract interface      SA

      Abstract interface      Remote interface        AR
      Abstract interface      interface               AI
      Abstract interface      Value                   AV
      Abstract interface      Servant                 AS
      Abstract interface      Abstract interface      AA
    */
    private static final String[] CASES = { 
        "RR","RI","RV","RS","RA",
        "IR","II","IV","IS","IA",
        "VR","VI","VV","VS","VA",//"SR",
        "SI","SV","SS","SA",
        "AR","AI","AV","AS","AA",
    };
    private int typeCode(char c) {
        switch (c) {
        case 'R': return TYPE_REMOTE;
        case 'I': return TYPE_NC_INTERFACE;
        case 'V': return TYPE_VALUE;
        case 'S': return TYPE_IMPLEMENTATION;
        case 'A': return TYPE_ABSTRACT;
        default: throw new Error("Unkown type.");
        }
    }
    
    private void checkType(String className, char typeInitial) {
        env.reset();
        int typeCode = typeCode(typeInitial);
        CompoundType type = (CompoundType) MapType.getType(className,stack);
        
        if (type == null) {
            throw new Error(type + " is null");
        }

        if (!type.isType(typeCode)) {
            throw new Error(type + " is not expected type. Found " + type.getTypeDescription());
        }
    }
    
    
    /**
     * Run the test.
     */
    public void run () {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() ) ;

        try {
    
            out = new ByteArrayOutputStream();
            env = new TestEnv(rmic.ParseTest.createClassPath(),out);
            stack = new ContextStack(env);

            // Do the tests...
            for (int i = 0; i < CASES.length; i++) {
                helper.start( "test_" + CASES[i] ) ;
                String outerClass = "rmic." + CASES[i];   
                checkType(outerClass,CASES[i].charAt(0));
                checkType(outerClass + ".Inner",CASES[i].charAt(1));
                checkType(outerClass + ".Inner",CASES[i].charAt(1));
                helper.pass() ;
            }
    
            env.shutdown();

        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            helper.fail( e ) ;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            status = new Error("Caught " + out.toString());
        } finally {
            helper.done() ;
        }
    }
}

// Remote Outer...

interface RR extends Remote {
    public String hello () throws RemoteException;
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

interface RI extends Remote {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

interface RV extends Remote {
    public String hello () throws RemoteException;
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

interface RS extends Remote {
    public String hello () throws RemoteException;
    public class Inner implements RS {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
}
public static class Nested implements RS {
    public Nested () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
}
}

interface RA extends Remote {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}

// Interface Outer...

interface IR {
    public String hello ();
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

interface II {
    public String hello ();
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

interface IV {
    public String hello ();
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

interface IS {
    public String hello ();
    public class Inner implements RS {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
}
public static class Nested implements RS {
    public Nested () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
}
}

interface IA {
    public String hello ();
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}


// Value Outer...

class VR implements Serializable {
    public String hello;
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

class VI implements Serializable {
    public String hello;
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

class VV implements Serializable {
    public String hello;
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

class VS implements Serializable {
    public String hello;
    public class Inner implements RR {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
    public static class Nested implements RR {
        public Nested () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
}

class VA implements Serializable {
    public String hello;
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}

// Servant Outer...

class SR implements SRInner {
    public SR () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

class SI implements RR {
    public SI () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

class SV implements RR {
    public SV () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    
    // Note: Changed to static to avoid error caused by
    // rmic.SV data member and constructor...
    
    static public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

class SS implements RR {
    public SS () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public class Inner implements RR {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
    public static class Nested implements RR {
        public Nested () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
}

class SA implements RR {
    public SA () throws RemoteException {}
    public String hello () throws RemoteException {return "Hello";}
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}


// Abstract Outer...

interface AR {
    public String hello () throws RemoteException;
    public interface Inner extends Remote {
        public String hello () throws RemoteException;
    }
    public static interface Nested extends Remote {
        public String hello () throws RemoteException;
    }
}

interface AI {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello ();
    }
    public static interface Nested {
        public String hello ();
    }
}

interface AV {
    public String hello () throws RemoteException;
    public class Inner implements Serializable {
        public String hello;
    }
    public static class Nested implements Serializable {
        public String hello;
    }
}

interface AS {
    public String hello () throws RemoteException;
    public class Inner implements RS {
        public Inner () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
    public static class Nested implements RS {
        public Nested () throws RemoteException {}
        public String hello () throws RemoteException {return "Hello";}
    }
}

interface AA {
    public String hello () throws RemoteException;
    public interface Inner {
        public String hello () throws RemoteException;
    }
    public static interface Nested {
        public String hello () throws RemoteException;
    }
}
