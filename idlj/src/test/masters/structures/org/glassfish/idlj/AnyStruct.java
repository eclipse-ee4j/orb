package org.glassfish.idlj;


/**
* org/glassfish/idlj/AnyStruct.java .
* Generated by the IDL-to-Java compiler (portable), version "4.1"
* from /Users/rgold/projects/glassfish/glassfish-corba/idlj/src/main/idl/org/glassfish/idlj/CORBAServerTest.idl
* Monday, January 29, 2018 11:19:41 AM EST
*/

public final class AnyStruct implements org.omg.CORBA.portable.IDLEntity
{
  public org.omg.CORBA.Any a = null;
  public org.glassfish.idlj.CORBAStruct s = null;

  public AnyStruct ()
  {
  } // ctor

  public AnyStruct (org.omg.CORBA.Any _a, org.glassfish.idlj.CORBAStruct _s)
  {
    a = _a;
    s = _s;
  } // ctor

} // class AnyStruct
