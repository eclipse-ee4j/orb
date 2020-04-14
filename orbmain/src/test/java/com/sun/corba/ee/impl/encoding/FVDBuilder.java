/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

public class FVDBuilder {

  private static Map<String,FullValueDescription> metas;
  private static List<FVDBuilder> builders;
  private static ORB orb;

  private String repId;
  private boolean customMarshalled;
  private List<FieldBuilder> fields = new ArrayList<>();
  private String baseRepId;

  public FVDBuilder(String repId) {
    this.repId = repId;
  }

  static void initialize(ORB orb) {
    FVDBuilder.orb = orb;
    metas = new HashMap<>();
    builders = new ArrayList<>();
  }

  static FullValueDescription getMeta(String repositoryID) {
    return Optional.ofNullable(metas.get(repositoryID))
          .orElseThrow(() -> new RuntimeException("No FVD defined for " + repositoryID));
  }

  static FVDBuilder defineFullValueDescription(String repId) {
    FVDBuilder fvdBuilder = new FVDBuilder(repId);
    builders.add(fvdBuilder);
    return fvdBuilder;
  }

  public FVDBuilder withCustomMarshalling() {
    customMarshalled = true;
    return this;
  }

  public FVDBuilder withBaseRepId(String repId) {
    baseRepId = repId;
    return this;
  }

  public FVDBuilder withMember(String fieldName, Class<?> fieldClass) {
    if (!fieldClass.isPrimitive()) throw new RuntimeException("Specify class for non-primitive fields");
    fields.add(new PrimitiveFieldBuilder(fieldName, fieldClass));
    return this;
  }

  public FVDBuilder withMember(String fieldName, String repId) {
    fields.add(new ValueFieldBuilder(fieldName, repId));
    return this;
  }

  public void build() {
    FullValueDescription fvd = new FullValueDescription("", repId, isAbstract(), isCustom(), "", "",
          new OperationDescription[0], new AttributeDescription[0], createMembers(), new Initializer[0],
          new String[0], new String[0], isTruncatable(), baseRepId, createTypeCode(orb));
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
    return new TypeCodeImpl(orb, TCKind._tk_value, repId, getClassName(repId), getTypeModifier(), getTypeCode(baseRepId), createMembers());
  }

  private String getClassName(String repId) {
    return repId.split(":")[1];
  }

  private TypeCode getTypeCode(String repId) {
    return Optional.ofNullable(repId).map(FVDBuilder::getMeta).map(f -> f.type).orElse(null);
  }

  public short getTypeModifier() {
    return isCustom() ? VM_CUSTOM.value :
           isTruncatable() ? VM_TRUNCATABLE.value :
           isAbstract() ? VM_ABSTRACT.value : VM_NONE.value;
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
      return new TypeCodeImpl(orb, TCKind._tk_string);
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
      return Serializable.class.isAssignableFrom(fieldClass)
            ? RepositoryId.createForJavaType((Serializable) fieldClass)
            : RepositoryId.createForJavaType(fieldClass);
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
