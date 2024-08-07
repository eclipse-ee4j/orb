/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.org.omg.CORBA.AttributeDescription;
import com.sun.org.omg.CORBA.Initializer;
import com.sun.org.omg.CORBA.OperationDescription;
import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.VM_ABSTRACT;
import org.omg.CORBA.VM_CUSTOM;
import org.omg.CORBA.VM_NONE;
import org.omg.CORBA.VM_TRUNCATABLE;
import org.omg.CORBA.ValueMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class uses the fluent builder pattern (see https://dzone.com/articles/fluent-builder-pattern) to create a
 * FullValueDescription (FVD) object for use in a unit test. It also serves as a cache for created FVDs. See CORBA 3.1
 * section 14.5.31
 */
public class FVDBuilder {

    private static Map<String, FullValueDescription> metas;
    private static ORB orb;

    private String repId;
    private boolean customMarshalled;
    private List<FieldBuilder> fields = new ArrayList<>();
    private String baseRepId;

    private FVDBuilder(String repId) {
        this.repId = repId;
    }

    /**
     * Initializes this class for a new unit test. This clears any existing FVDs and specifies the ORB to be used to create
     * new ones.
     * 
     * @param orb an ORB used to create TypeCodes.
     */
    static void initialize(ORB orb) {
        FVDBuilder.orb = orb;
        metas = new HashMap<>();
    }

    /**
     * Obtains the FVD for the specified repository ID.
     * 
     * @param repositoryID the repository ID to look up
     * @throws RuntimeException if a FVD has not been defined for the repository ID
     * @return the matching FVD
     */
    static FullValueDescription getMeta(String repositoryID) {
        return Optional.ofNullable(metas.get(repositoryID)).orElseThrow(() -> new RuntimeException("No FVD defined for " + repositoryID));
    }

    /**
     * Creates a builder for a FVD, specifying the CORBA repository ID
     * 
     * @param repId the respository ID
     * @return the new builder
     */
    static FVDBuilder defineFullValueDescription(String repId) {
        return new FVDBuilder(repId);
    }

    /**
     * Modifies this builder to set the is_custom flag of the FVD it will create.
     * 
     * @return this object
     */
    FVDBuilder withCustomMarshalling() {
        customMarshalled = true;
        return this;
    }

    /**
     * Modifies this builder to specify a base class, using its repository iD. This will set the baseValue flag of the
     * create FVD.
     * 
     * @param repId the repository ID of the base class
     * @return this object
     */
    FVDBuilder withBaseRepId(String repId) {
        baseRepId = repId;
        return this;
    }

    /**
     * Modifies this builder to add a primitive or String member field. This will add an entry to the members entry of the
     * created FVD.
     * 
     * @param fieldName the name of the field
     * @param fieldClass the class that will be used to select a repository ID for the member
     * @return this object
     */
    FVDBuilder withMember(String fieldName, Class<?> fieldClass) {
        if (!fieldClass.equals(String.class) && !fieldClass.isPrimitive())
            throw new RuntimeException(
                    String.format("%s field %s is not primitive; define it with its repository ID", fieldClass.getName(), fieldName));

        fields.add(new PrimitiveFieldBuilder(fieldName, fieldClass));
        return this;
    }

    /**
     * Modifies this builder to add a member field of a Java class type. This will add an entry to the members entry of the
     * created FVD.
     * 
     * @param fieldName the name of the field
     * @param repId the repository ID for the field
     * @return this object
     */
    FVDBuilder withMember(String fieldName, String repId) {
        fields.add(new ValueFieldBuilder(fieldName, repId));
        return this;
    }

    /**
     * Creates a FullValueDescription from the builder configuration, and caches it. Callers may obtain the FVD by invoking
     * {@link #getMeta(String)}.
     */
    public void build() {
        FullValueDescription fvd = new FullValueDescription("", repId, isAbstract(), isCustom(), "", "", new OperationDescription[0],
                new AttributeDescription[0], createMembers(), new Initializer[0], new String[0], new String[0], isTruncatable(), baseRepId,
                createTypeCode(orb));
        metas.put(repId, fvd);
    }

    private boolean isCustom() {
        return customMarshalled;
    }

    private boolean isAbstract() {
        return false;
    }

    private boolean isTruncatable() {
        return false;
    }

    private TypeCode createTypeCode(ORB orb) {
        return new TypeCodeImpl(orb, TCKind._tk_value, repId, getClassName(repId), getTypeModifier(), getTypeCode(baseRepId),
                createMembers());
    }

    private String getClassName(String repId) {
        return repId.split(":")[1];
    }

    private TypeCode getTypeCode(String repId) {
        return Optional.ofNullable(repId).map(FVDBuilder::getMeta).map(f -> f.type).orElse(null);
    }

    public short getTypeModifier() {
        return isCustom() ? VM_CUSTOM.value : isTruncatable() ? VM_TRUNCATABLE.value : isAbstract() ? VM_ABSTRACT.value : VM_NONE.value;
    }

    private ValueMember[] createMembers() {
        return fields.stream().map(FieldBuilder::build).toArray(ValueMember[]::new);
    }

    private static TypeCode fromType(ORB orb, Class<?> type) {
        if (type.equals(char.class))
            return new TypeCodeImpl(orb, TCKind._tk_wchar);
        else if (type.equals(boolean.class))
            return new TypeCodeImpl(orb, TCKind._tk_boolean);
        else if (type.equals(byte.class))
            return new TypeCodeImpl(orb, TCKind._tk_octet);
        else if (type.equals(short.class))
            return new TypeCodeImpl(orb, TCKind._tk_short);
        else if (type.equals(int.class))
            return new TypeCodeImpl(orb, TCKind._tk_long);
        else if (type.equals(float.class))
            return new TypeCodeImpl(orb, TCKind._tk_float);
        else if (type.equals(double.class))
            return new TypeCodeImpl(orb, TCKind._tk_double);
        else if (type.equals(String.class))
            return new TypeCodeImpl(orb, TCKind._tk_value);
        else if (type.equals(long.class))
            return new TypeCodeImpl(orb, TCKind._tk_longlong);
        else
            throw new RuntimeException("Test doesn't support type codes for " + type);
    }

    private static abstract class FieldBuilder {
        final String fieldName;

        public FieldBuilder(String fieldName) {
            this.fieldName = fieldName;
        }

        public abstract ValueMember build();
    }

    private static class PrimitiveFieldBuilder extends FieldBuilder {

        final Class<?> fieldClass;

        PrimitiveFieldBuilder(String fieldName, Class<?> fieldClass) {
            super(fieldName);
            this.fieldClass = fieldClass;
        }

        @Override
        public ValueMember build() {
            return new ValueMember(fieldName, getRepositoryId(), "", "", fromType(orb, fieldClass), null, (short) 0);
        }

        private String getRepositoryId() {
            return fieldClass.equals(String.class) ? RepositoryId.kWStringValueRepID : RepositoryId.createForJavaType(fieldClass);
        }
    }

    private static class ValueFieldBuilder extends FieldBuilder {

        private final String repositoryID;

        public ValueFieldBuilder(String fieldName, String repositoryID) {
            super(fieldName);
            this.repositoryID = repositoryID;
        }

        @Override
        public ValueMember build() {
            return new ValueMember(fieldName, repositoryID, "", "", getMeta(repositoryID).type, null, (short) 0);
        }

    }
}
