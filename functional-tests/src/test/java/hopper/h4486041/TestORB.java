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

package hopper.h4486041;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CORBA.ORBPackage.InvalidName;
import java.util.Properties;

public class TestORB extends ORB {
    public static final String ThrowError = "ThrowError";

    @Override
    protected void set_parameters(String[] args, Properties props) {
        System.out.println("\tTestORB.set_parameters - Enter");
        if (props.get(ThrowError) != null) {
            System.out.println("\tTestORB.set_parameters - Exception Exit");
            throw new ORBInitException("You asked for it.");
        }
        System.out.println("\tTestORB.set_parameters - Normal Exit");
    }

    @Override
    public String[] list_initial_services() {
        return null;
    }

    @Override
    public org.omg.CORBA.Object resolve_initial_references(String object_name) throws InvalidName {
        return null;
    }

    @Override
    public String object_to_string(org.omg.CORBA.Object obj) {
        return null;
    }

    @Override
    public org.omg.CORBA.Object string_to_object(String str) {
        return null;
    }

    @Override
    public NVList create_list(int count) {
        return null;
    }

    @Override
    public NamedValue create_named_value(String s, Any any, int flags) {
        return null;
    }

    @Override
    public ExceptionList create_exception_list() {
        return null;
    }

    @Override
    public ContextList create_context_list() {
        return null;
    }

    @Override
    public Context get_default_context() {
        return null;
    }

    @Override
    public Environment create_environment() {
        return null;
    }

    @Override
    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        return null;
    }

    @Override
    public void send_multiple_requests_oneway(Request[] req) {
    }

    @Override
    public void send_multiple_requests_deferred(Request[] req) {
    }

    @Override
    public boolean poll_next_response() {
        return false;
    }

    @Override
    public Request get_next_response() throws WrongTransaction {
        return null;
    }

    @Override
    public TypeCode get_primitive_tc(TCKind tcKind) {
        return null;
    }

    @Override
    public TypeCode create_struct_tc(String id, String name, StructMember[] members) {
        return null;
    }

    @Override
    public TypeCode create_union_tc(String id, String name, TypeCode discriminator_type, UnionMember[] members) {
        return null;
    }

    @Override
    public TypeCode create_enum_tc(String id, String name, String[] members) {
        return null;
    }

    @Override
    public TypeCode create_alias_tc(String id, String name, TypeCode original_type) {
        return null;
    }

    @Override
    public TypeCode create_exception_tc(String id, String name, StructMember[] members) {
        return null;
    }

    @Override
    public TypeCode create_interface_tc(String id, String name) {
        return null;
    }

    @Override
    public TypeCode create_string_tc(int bound) {
        return null;
    }

    @Override
    public TypeCode create_wstring_tc(int bound) {
        return null;
    }

    @Override
    public TypeCode create_sequence_tc(int bound, TypeCode element_type) {
        return null;
    }

    @Override
    public TypeCode create_recursive_sequence_tc(int bound, int offset) {
        return null;
    }

    @Override
    public TypeCode create_array_tc(int length, TypeCode element_type) {
        return null;
    }

    @Override
    public Any create_any() {
        return null;
    }
}

// End of file.
