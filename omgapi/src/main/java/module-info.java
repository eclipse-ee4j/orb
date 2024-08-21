/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

module org.glassfish.corba.omgapi {

    requires java.desktop;
    requires java.logging;
    requires java.rmi;

    exports com.sun.corba.ee.org.omg.CORBA;
    exports javax.rmi;
    exports javax.rmi.CORBA;
    exports org.omg.CORBA;
    exports org.omg.CORBA.ContainedPackage;
    exports org.omg.CORBA.ContainerPackage;
    exports org.omg.CORBA.InterfaceDefPackage;
    exports org.omg.CORBA.ValueDefPackage;
    exports org.omg.CORBA_2_3;
    exports org.omg.CORBA_2_3.portable;
    exports org.omg.CORBA.DynAnyPackage;
    exports org.omg.CORBA.ORBPackage;
    exports org.omg.CORBA.portable;
    exports org.omg.CORBA.TSIdentificationPackage;
    exports org.omg.CORBA.TypeCodePackage;
    exports org.omg.CosNaming;
    exports org.omg.CosNaming.NamingContextExtPackage;
    exports org.omg.CosNaming.NamingContextPackage;
    exports org.omg.CosTransactions;
    exports org.omg.CosTSPortability;
    exports org.omg.CosTSInteroperation;
    exports org.omg.Dynamic;
    exports org.omg.DynamicAny;
    exports org.omg.DynamicAny.DynAnyFactoryPackage;
    exports org.omg.DynamicAny.DynAnyPackage;
    exports org.omg.IOP;
    exports org.omg.IOP.CodecFactoryPackage;
    exports org.omg.IOP.CodecPackage;
    exports org.omg.Messaging;
    exports org.omg.PortableInterceptor;
    exports org.omg.PortableInterceptor.ORBInitInfoPackage;
    exports org.omg.PortableServer;
    exports org.omg.PortableServer.CurrentPackage;
    exports org.omg.PortableServer.POAManagerPackage;
    exports org.omg.PortableServer.POAPackage;
    exports org.omg.PortableServer.portable;
    exports org.omg.PortableServer.ServantLocatorPackage;
    exports org.omg.SendingContext;
    exports org.omg.SendingContext.CodeBasePackage;
    exports org.omg.TimeBase;
    exports org.omg.stub.java.rmi;

    opens org.omg.CORBA;
}
