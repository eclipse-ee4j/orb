/*
 * Copyright (c) 1998, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.Identifier;

public interface Constants extends org.glassfish.rmic.Constants {

    // Identifiers for referenced classes:

    Identifier idReplyHandler =
        Identifier.lookup("org.omg.CORBA.portable.ResponseHandler");
    Identifier idStubBase =
        Identifier.lookup("javax.rmi.CORBA.Stub");
    Identifier idTieBase =
        Identifier.lookup("org.omg.CORBA.portable.ObjectImpl");
    Identifier idTieInterface =
        Identifier.lookup("javax.rmi.CORBA.Tie");
    Identifier idPOAServantType =
        Identifier.lookup( "org.omg.PortableServer.Servant" ) ;
    Identifier idDelegate =
        Identifier.lookup("org.omg.CORBA.portable.Delegate");
    Identifier idOutputStream =
        Identifier.lookup("org.omg.CORBA.portable.OutputStream");
    Identifier idExtOutputStream =
        Identifier.lookup("org.omg.CORBA_2_3.portable.OutputStream");
    Identifier idInputStream =
        Identifier.lookup("org.omg.CORBA.portable.InputStream");
    Identifier idExtInputStream =
        Identifier.lookup("org.omg.CORBA_2_3.portable.InputStream");
    Identifier idSystemException =
        Identifier.lookup("org.omg.CORBA.SystemException");
    Identifier idBadMethodException =
        Identifier.lookup("org.omg.CORBA.BAD_OPERATION");
    Identifier idPortableUnknownException =
        Identifier.lookup("org.omg.CORBA.portable.UnknownException");
    Identifier idApplicationException =
        Identifier.lookup("org.omg.CORBA.portable.ApplicationException");
    Identifier idRemarshalException =
        Identifier.lookup("org.omg.CORBA.portable.RemarshalException");
    Identifier idJavaIoExternalizable =
        Identifier.lookup("java.io.Externalizable");
    Identifier idCorbaObject =
        Identifier.lookup("org.omg.CORBA.Object");
    Identifier idCorbaORB =
        Identifier.lookup("org.omg.CORBA.ORB");
    Identifier idClassDesc =
        Identifier.lookup("javax.rmi.CORBA.ClassDesc");
    Identifier idJavaIoIOException =
        Identifier.lookup("java.io.IOException");
    Identifier idIDLEntity =
        Identifier.lookup("org.omg.CORBA.portable.IDLEntity");
    Identifier idValueBase =
        Identifier.lookup("org.omg.CORBA.portable.ValueBase");
    Identifier idBoxedRMI =
        Identifier.lookup("org.omg.boxedRMI");
    Identifier idBoxedIDL =
        Identifier.lookup("org.omg.boxedIDL");
    Identifier idCorbaUserException =
        Identifier.lookup("org.omg.CORBA.UserException");


    // Identifiers for primitive types:

    Identifier idBoolean =
        Identifier.lookup("boolean");
    Identifier idByte =
        Identifier.lookup("byte");
    Identifier idChar =
        Identifier.lookup("char");
    Identifier idShort =
        Identifier.lookup("short");
    Identifier idInt =
        Identifier.lookup("int");
    Identifier idLong =
        Identifier.lookup("long");
    Identifier idFloat =
        Identifier.lookup("float");
    Identifier idDouble =
        Identifier.lookup("double");
    Identifier idVoid =
        Identifier.lookup("void");

    // IndentingWriter constructor args:

    int INDENT_STEP = 4;
    int TAB_SIZE = Integer.MAX_VALUE; // No tabs.

    // Type status codes:

    int STATUS_PENDING = 0;
    int STATUS_VALID = 1;
    int STATUS_INVALID = 2;

    // Java Names:

    String NAME_SEPARATOR = ".";
    String SERIAL_VERSION_UID = "serialVersionUID";

    // IDL Names:

    String[] IDL_KEYWORDS = {
        "abstract",
        "any",
        "attribute",
        "boolean",
        "case",
        "char",
        "const",
        "context",
        "custom",
        "default",
        "double",
        "enum",
        "exception",
        "factory",
        "FALSE",
        "fixed",
        "float",
        "in",
        "inout",
        "interface",
        "long",
        "module",
        "native",
        "Object",
        "octet",
        "oneway",
        "out",
        "private",
        "public",
        "raises",
        "readonly",
        "sequence",
        "short",
        "string",
        "struct",
        "supports",
        "switch",
        "TRUE",
        "truncatable",
        "typedef",
        "unsigned",
        "union",
        "ValueBase",
        "valuetype",
        "void",
        "wchar",
        "wstring",
    };


    String EXCEPTION_SUFFIX = "Exception";
    String ERROR_SUFFIX = "Error";
    String EX_SUFFIX = "Ex";

    String IDL_REPOSITORY_ID_PREFIX = "IDL:";
    String IDL_REPOSITORY_ID_VERSION = ":1.0";

    String[]  IDL_CORBA_MODULE = {"CORBA"};
    String[]  IDL_SEQUENCE_MODULE = {"org","omg","boxedRMI"};
    String[]  IDL_BOXEDIDL_MODULE = {"org","omg","boxedIDL"};

    String    IDL_CLASS = "ClassDesc";
    String[]  IDL_CLASS_MODULE = {"javax","rmi","CORBA"};

    String    IDL_IDLENTITY = "IDLEntity";
    String    IDL_SERIALIZABLE = "Serializable";
    String    IDL_EXTERNALIZABLE = "Externalizable";
    String[]  IDL_JAVA_IO_MODULE = {"java","io"};
    String[]  IDL_ORG_OMG_CORBA_MODULE = {"org","omg","CORBA"};
    String[]  IDL_ORG_OMG_CORBA_PORTABLE_MODULE = {"org","omg","CORBA","portable"};

    String    IDL_JAVA_LANG_OBJECT = "_Object";
    String[]  IDL_JAVA_LANG_MODULE = {"java","lang"};

    String    IDL_JAVA_RMI_REMOTE = "Remote";
    String[]  IDL_JAVA_RMI_MODULE = {"java","rmi"};

    String  IDL_SEQUENCE = "seq";

    String  IDL_CONSTRUCTOR = "create";

    String  IDL_NAME_SEPARATOR = "::";
    String  IDL_BOOLEAN = "boolean";
    String  IDL_BYTE = "octet";
    String  IDL_CHAR = "wchar";
    String  IDL_SHORT = "short";
    String  IDL_INT = "long";
    String  IDL_LONG = "long long";
    String  IDL_FLOAT = "float";
    String  IDL_DOUBLE = "double";
    String  IDL_VOID = "void";

    String  IDL_STRING = "WStringValue";
    String  IDL_CONSTANT_STRING = "wstring";
    String  IDL_CORBA_OBJECT = "Object";
    String  IDL_ANY = "any";

    // File names:

    String SOURCE_FILE_EXTENSION = ".java";
    String IDL_FILE_EXTENSION = ".idl";

    // Type Codes:

    int TYPE_VOID                   = 0x00000001;   // In PrimitiveType
    int TYPE_BOOLEAN                = 0x00000002;   // In PrimitiveType
    int TYPE_BYTE                   = 0x00000004;   // In PrimitiveType
    int TYPE_CHAR                   = 0x00000008;   // In PrimitiveType
    int TYPE_SHORT                  = 0x00000010;   // In PrimitiveType
    int TYPE_INT                    = 0x00000020;   // In PrimitiveType
    int TYPE_LONG                   = 0x00000040;   // In PrimitiveType
    int TYPE_FLOAT                  = 0x00000080;   // In PrimitiveType
    int TYPE_DOUBLE                 = 0x00000100;   // In PrimitiveType

    int TYPE_STRING                 = 0x00000200;   // In SpecialClassType (String)
    int TYPE_ANY                    = 0x00000400;   // In SpecialInterfaceType (Serializable,Externalizable)
    int TYPE_CORBA_OBJECT   = 0x00000800;   // In SpecialInterfaceType (CORBA.Object,Remote)

    int TYPE_REMOTE                 = 0x00001000;   // In RemoteType
    int TYPE_ABSTRACT               = 0x00002000;   // In AbstractType
    int TYPE_NC_INTERFACE   = 0x00004000;   // In NCInterfaceType

    int TYPE_VALUE                  = 0x00008000;   // In ValueType
    int TYPE_IMPLEMENTATION = 0x00010000;   // In ImplementationType
    int TYPE_NC_CLASS               = 0x00020000;   // In NCClassType

    int TYPE_ARRAY                  = 0x00040000;   // In ArrayType
    int TYPE_JAVA_RMI_REMOTE = 0x00080000;  // In SpecialInterfaceType

    // Type code masks:

    int TYPE_NONE                   = 0x00000000;
    int TYPE_ALL                    = 0xFFFFFFFF;
    int TYPE_MASK                   = 0x00FFFFFF;
    int TM_MASK                             = 0xFF000000;

    // Type code modifiers:

    int TM_PRIMITIVE                = 0x01000000;
    int TM_COMPOUND                 = 0x02000000;
    int TM_CLASS                    = 0x04000000;
    int TM_INTERFACE                = 0x08000000;
    int TM_SPECIAL_CLASS    = 0x10000000;
    int TM_SPECIAL_INTERFACE= 0x20000000;
    int TM_NON_CONFORMING   = 0x40000000;
    int TM_INNER            = 0x80000000;

    // Attribute kinds...

    int ATTRIBUTE_NONE = 0;     // Not an attribute.
    int ATTRIBUTE_IS = 1;       // read-only, had "is" prefix.
    int ATTRIBUTE_GET = 2;      // read-only, had "get" prefix.
    int ATTRIBUTE_IS_RW = 3;    // read-write, had "is" prefix.
    int ATTRIBUTE_GET_RW = 4;   // read-write, had "get" prefix.
    int ATTRIBUTE_SET = 5;      // had "set" prefix.

    String[] ATTRIBUTE_WIRE_PREFIX = {
        "",
        "_get_",
        "_get_",
        "_get_",
        "_get_",
        "_set_",
    };
}
