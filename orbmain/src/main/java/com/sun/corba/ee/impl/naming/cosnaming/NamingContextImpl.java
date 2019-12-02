/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.naming.cosnaming;

import org.omg.CORBA.BAD_PARAM;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

// Import org.omg.CosNaming classes
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.BindingTypeHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExtPOA;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import com.sun.corba.ee.impl.naming.namingutil.INSURLHandler;
import com.sun.corba.ee.spi.logging.NamingSystemException ;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.trace.Naming;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotEmpty;
import org.omg.CosNaming.NamingContextPackage.NotFoundReason;

/**
 * Class NamingContextImpl implements the org.omg.CosNaming::NamingContext
 * interface, but does not implement the methods associated with
 * maintaining the "table" of current bindings in a NamingContext.
 * Instead, this implementation assumes that the derived implementation
 * implements the NamingContextDataStore interface, which has the necessary
 * methods. This allows multiple
 * NamingContext implementations that differ in storage of the bindings,
 * as well as implementations of interfaces derived from
 * CosNaming::NamingContext that still reuses the implementation.
 * <p>
 * The operations bind(), rebind(), bind_context() and rebind_context()
 * are all really implemented by doBind(). resolve() is really implemented
 * by doResolve(), unbind() by doUnbind(). list(), new_context() and
 * destroy() uses the NamingContextDataStore interface directly. All the
 * doX() methods are public static.
 * They synchronize on the NamingContextDataStore object.
 * <p>
 * An implementation a NamingContext must extend this class and implement
 * the NamingContextDataStore interface with the operations:
 * Bind(), Resolve(),
 * Unbind(), List(), NewContext() and Destroy(). Calls
 * to these methods are synchronized; these methods should
 * therefore not be synchronized.
 */
@Naming
public abstract class NamingContextImpl 
    extends NamingContextExtPOA
    implements NamingContextDataStore
{

    protected ORB orb ;
    protected POA nsPOA;
    private static final NamingSystemException wrapper =
        NamingSystemException.self ;

    // The grammer for Parsing and Building Interoperable Stringified Names
    // are implemented in this class
    private InterOperableNamingImpl insImpl; 
    /**
     * Create a naming context servant.
     * Runs the super constructor.
     * @param orb an ORB object.
     * @param poa the POA.
     * @exception java.lang.Exception a Java exception.
     */
    public NamingContextImpl(ORB orb, POA poa) throws java.lang.Exception {
        super();
        this.orb = orb ;
        insImpl = new InterOperableNamingImpl( );
        this.nsPOA = poa;
    }

    public POA getNSPOA( ) {
        return nsPOA;
    }
  
    /**
     * Bind an object under a name in this NamingContext. If the name
     * contains multiple (n) components, n-1 will be resolved in this
     * NamingContext and the object bound in resulting NamingContext.
     * An exception is thrown if a binding with the supplied name already
     * exists. If the
     * object to be bound is a NamingContext it will not participate in
     * a recursive resolve.
     * @param n a sequence of NameComponents which is the name under which
     * the object will be bound.
     * @param obj the object reference to be bound.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could 
     * not proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).
     * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound An object
     * is already bound under the supplied name.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #doBind
     */
    @Naming
    public void bind(NameComponent[] n, org.omg.CORBA.Object obj)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound
    {
        if( obj == null ) {
            throw wrapper.objectIsNull() ;
        }
        // doBind implements all four flavors of binding
        NamingContextDataStore impl = this;
        doBind(impl,n,obj,false,BindingType.nobject);
    }

  
    /**
     * Bind a NamingContext under a name in this NamingContext. If the name
     * contains multiple (n) components, n-1 will be resolved in this
     * NamingContext and the object bound in resulting NamingContext.
     * An exception is thrown if a binding with the supplied name already
     * exists. The NamingContext will participate in recursive resolving.
     * @param n a sequence of NameComponents which is the name under which
     * the object will be bound.
     * @param nc the NamingContect object reference to be bound.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could 
     * not proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).
     * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound An object
     * is already bound under the supplied name.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #doBind
     */
    @Naming
    public void bind_context(NameComponent[] n, NamingContext nc)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound
    {
        if( nc == null ) {
            wrapper.objectIsNull() ;
        }
        // doBind implements all four flavors of binding
        NamingContextDataStore impl = this;
        doBind(impl,n,nc,false,BindingType.ncontext);
    }
  
    /**
     * Bind an object under a name in this NamingContext. If the name
     * contains multiple (n) components, n-1 will be resolved in this
     * NamingContext and the object bound in resulting NamingContext.
     * If a binding under the supplied name already exists it will be
     * unbound first. If the
     * object to be bound is a NamingContext it will not participate in
     * a recursive resolve.
     * @param n a sequence of NameComponents which is the name under which
     * the object will be bound.
     * @param obj the object reference to be bound.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not
     * proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #doBind
     */
    @Naming
    public  void rebind(NameComponent[] n, org.omg.CORBA.Object obj)
        throws       org.omg.CosNaming.NamingContextPackage.NotFound,
                     org.omg.CosNaming.NamingContextPackage.CannotProceed,
                     org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if( obj == null ) {
            throw wrapper.objectIsNull() ;
        }

        try {
            // doBind implements all four flavors of binding
            NamingContextDataStore impl = this;
            doBind(impl,n,obj,true,BindingType.nobject);
        } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {
            throw wrapper.namingCtxRebindAlreadyBound( ex ) ;
        }
    }

    /**
     * Bind a NamingContext under a name in this NamingContext. If the name
     * contains multiple (n) components, the first n-1 components will be
     * resolved in this NamingContext and the object bound in resulting 
     * NamingContext. If a binding under the supplied name already exists it 
     * will be unbound first. The NamingContext will participate in recursive 
     * resolving.
     * @param n a sequence of NameComponents which is the name under which
     * the object will be bound.
     * @param nc the object reference to be bound.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not
     * proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #doBind
     */
    @Naming
    public  void rebind_context(NameComponent[] n, NamingContext nc)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if( nc == null ) {
            throw wrapper.objectIsNull() ;
        }

        try {
            // doBind implements all four flavors of binding
            NamingContextDataStore impl = this;
            doBind(impl,n,nc,true,BindingType.ncontext);
        } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound ex) {      
            throw wrapper.namingCtxRebindctxAlreadyBound( ex ) ;
        }
    }

    /**
     * Resolve a name in this NamingContext and return the object reference
     * bound to the name. If the name contains multiple (n) components,
     * the first component will be resolved in this NamingContext and the
     * remaining components resolved in the resulting NamingContext, provided
     * that the NamingContext bound to the first component of the name was
     * bound with bind_context().
     * @param n a sequence of NameComponents which is the name to be resolved.
     * @return the object reference bound under the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not
     * proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).   
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #doResolve
     */
    @Naming
    public  org.omg.CORBA.Object resolve(NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        // doResolve actually resolves
        NamingContextDataStore impl = this;
        return doResolve(impl,n);
    }
            

    /**
     * Remove a binding from this NamingContext. If the name contains
     * multiple (n) components, the first n-1 components will be resolved
     * from this NamingContext and the final component unbound in
     * the resulting NamingContext.
     * @param n a sequence of NameComponents which is the name to be unbound.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not
     * proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).   
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #doUnbind
     */
    @Naming
    public  void unbind(NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        // doUnbind actually unbinds
        NamingContextDataStore impl = this;
        doUnbind(impl,n);
    }

    /**
     * List the contents of this NamingContest. A sequence of bindings
     * is returned (a BindingList) containing up to the number of requested
     * bindings, and a BindingIterator object reference is returned for
     * iterating over the remaining bindings.
     * @param how_many The number of requested bindings in the BindingList.
     * @param bl The BindingList as an out parameter.
     * @param bi The BindingIterator as an out parameter.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see BindingListHolder
     * @see BindingIteratorImpl
     */
    @Naming
    public  void list(int how_many, BindingListHolder bl, 
        BindingIteratorHolder bi)
    {
        // List actually generates the list
        NamingContextDataStore impl = this;
        synchronized (impl) {
            impl.listImpl(how_many,bl,bi);
        }
    }

    /**
     * Create a NamingContext object and return its object reference.
     * @return an object reference for a new NamingContext object implemented
     * by this Name Server.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    @Naming
    public synchronized NamingContext new_context()
    {
        NamingContextDataStore impl = this;
        synchronized (impl) {
            return impl.newContextImpl();
        }
    }

    /**
     * Create a new NamingContext, bind it in this Naming Context and return
     * its object reference. This is equivalent to using new_context() followed
     * by bind_context() with the supplied name and the object reference for
     * the newly created NamingContext.
     * @param n a sequence of NameComponents which is the name to be unbound.
     * @return an object reference for a new NamingContext object implemented
     * by this Name Server, bound to the supplied name.   
     * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound An object
     * is already bound under the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not
     * proceed in resolving the n-1 components of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).   
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #new_context
     * @see #bind_context
     */
    @Naming
    public  NamingContext bind_new_context(NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        NamingContext nc = null;
        NamingContext rnc = null;
        try {
            nc = this.new_context();
            this.bind_context(n,nc);
            rnc = nc;
            nc = null;
        } finally {
            try {
                if (nc != null) {
                    nc.destroy();
                }
            } catch (org.omg.CosNaming.NamingContextPackage.NotEmpty e) {
                throw new CannotProceed( "Old naming context is not empty", 
                    nc, n) ;
            }
        }

        return rnc;
    }

    /**
     * Destroy this NamingContext object. If this NamingContext contains
     * no bindings, the NamingContext is deleted.
     * @exception org.omg.CosNaming.NamingContextPackage.NotEmpty This 
     * NamingContext is not empty (i.e., contains bindings).
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    @Naming
    public  void destroy()
        throws org.omg.CosNaming.NamingContextPackage.NotEmpty
    {
        NamingContextDataStore impl = this;
        synchronized (impl) {
            if (impl.isEmptyImpl()) {
                // The context is empty so it can be destroyed
                impl.destroyImpl();
            } else {
                throw new NotEmpty();
            }
        }
    }

    /**
     * Implements all four flavors of binding. It uses Resolve() to
     * check if a binding already exists (for bind and bind_context), and
     * unbind() to ensure that a binding does not already exist.
     * If the length of the name is 1, then Bind() is called with
     * the name and the object to bind. Otherwise, the first component
     * of the name is resolved in this NamingContext and the appropriate
     * form of bind passed to the resulting NamingContext.
     * This method is static for maximal reuse - even for extended naming
     * context implementations where the recursive semantics still apply.
     * @param impl an implementation of NamingContextDataStore
     * @param n a sequence of NameComponents which is the name under which
     * the object will be bound.
     * @param obj the object reference to be bound.
     * @param rebind Replace an existing binding or not.
     * @param bt Type of binding (as object or as context).
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with 
     * multiple components was supplied, but the first component could not be
     * resolved.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not     * proceed
     * in resolving the first component of the supplied name.
     * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The 
     * supplied name is invalid (i.e., has length less than 1).
     * @exception org.omg.CosNaming.NamingContextPackage.AlreadyBound An object
     * is already bound under the supplied name.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see #resolve
     * @see #unbind
     * @see #bind
     * @see #bind_context
     * @see #rebind
     * @see #rebind_context
     */
    @Naming
    public static void doBind(NamingContextDataStore impl,
                              NameComponent[] n,
                              org.omg.CORBA.Object obj,
                              boolean rebind,
                              org.omg.CosNaming.BindingType bt)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName,
               org.omg.CosNaming.NamingContextPackage.AlreadyBound
    {
        if (n.length < 1) {
            throw new InvalidName();
        }
    
        if (n.length == 1) {
            if ( (n[0].id.length() == 0) && (n[0].kind.length() == 0 ) ) {
                throw new InvalidName();
            }

            synchronized (impl) {
                BindingTypeHolder bth = new BindingTypeHolder();
                if (rebind) {
                    org.omg.CORBA.Object objRef = impl.resolveImpl( n[0], bth );
                    if( objRef != null ) {
                        // Refer Naming Service Doc:00-11-01 section 2.2.3.4 
                        // If there is an object already bound with the name
                        // and the binding type is not ncontext a NotFound 
                        // Exception with a reason of not a context has to be
                        // raised.
                        // Fix for bug Id: 4384628
                        if ( bth.value.value() == BindingType.nobject.value() ){
                            if ( bt.value() == BindingType.ncontext.value() ) {
                                throw new NotFound(
                                    NotFoundReason.not_context, n);
                            }
                        } else {
                            // Previously a Context was bound and now trying to
                            // bind Object. It is invalid.
                            if ( bt.value() == BindingType.nobject.value() ) {
                                throw new NotFound(
                                    NotFoundReason.not_object, n);
                            }
                        }

                        impl.unbindImpl(n[0]);
                    }
                } else {
                    if (impl.resolveImpl(n[0],bth) != null) {
                        throw new AlreadyBound();
                    }
                }
        
                // Now there are no other bindings under this name
                impl.bindImpl(n[0],obj,bt);
            }
        } else {
            NamingContext context = resolveFirstAsContext(impl,n);
            NameComponent[] tail = new NameComponent[n.length - 1];
            System.arraycopy(n,1,tail,0,n.length-1);

            switch (bt.value()) {
            case BindingType._nobject:
                if (rebind) {
                    context.rebind(tail, obj);
                } else {
                    context.bind(tail, obj);
                }
                break;
            case BindingType._ncontext:
                NamingContext objContext = (NamingContext)obj;
                if (rebind) {
                context.rebind_context(tail, objContext);
            }
                else {
                context.bind_context(tail, objContext);
            }
                break;
            default:
                throw wrapper.namingCtxBadBindingtype() ;
            }
        }
    }

    /**
   * Implements resolving names in this NamingContext. The first component
   * of the supplied name is resolved in this NamingContext by calling
   * Resolve(). If there are no more components in the name, the
   * resulting object reference is returned. Otherwise, the resulting object
   * reference must have been bound as a context and be narrowable to
   * a NamingContext. If this is the case, the remaining
   * components of the name is resolved in the resulting NamingContext.
   * This method is static for maximal reuse - even for extended naming
   * context implementations where the recursive semantics still apply.
   * @param impl an implementation of NamingContextDataStore
   * @param n a sequence of NameComponents which is the name to be resolved.
   * @return the object reference bound under the supplied name.
   * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with
   * multiple components was supplied, but the first component could not be
   * resolved.
   * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not 
   * proceed
   * in resolving the first component of the supplied name.
   * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The supplied
   * name is invalid (i.e., has length less than 1).   
   * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system
   * exceptions.
   * @see #resolve
   */
    @Naming
    public static org.omg.CORBA.Object doResolve(NamingContextDataStore impl,
                                                 NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        org.omg.CORBA.Object obj;
        BindingTypeHolder bth = new BindingTypeHolder();
            
        if (n.length < 1) {
            throw new InvalidName();
        }

        if (n.length == 1) {
            synchronized (impl) {
                obj = impl.resolveImpl(n[0],bth);
            }
            if (obj == null) {
                throw new NotFound(NotFoundReason.missing_node,n);
            }
            return obj;
        } else {
            if ( (n[1].id.length() == 0) && (n[1].kind.length() == 0) ) {
                throw new InvalidName();
            }

            NamingContext context = resolveFirstAsContext(impl,n);
            NameComponent[] tail = new NameComponent[n.length -1];
            System.arraycopy(n,1,tail,0,n.length-1);

            try {
                // First try to resolve using the local call, this should work
                // most of the time unless there are federated naming contexts.
                Servant servant = impl.getNSPOA().reference_to_servant( 
                    context );
                return doResolve(((NamingContextDataStore)servant), tail) ;
            } catch( Exception e ) {
                return context.resolve(tail);
            }
        }
    }

    /**
   * Implements unbinding bound names in this NamingContext. If the
   * name contains only one component, the name is unbound in this
   * NamingContext using Unbind(). Otherwise, the first component
   * of the name is resolved in this NamingContext and 
   * unbind passed to the resulting NamingContext.
   * This method is static for maximal reuse - even for extended naming
   * context implementations where the recursive semantics still apply.
   * @param impl an implementation of NamingContextDataStore
   * @param n a sequence of NameComponents which is the name to be unbound.
   * @exception org.omg.CosNaming.NamingContextPackage.NotFound A name with multiple
   * components was supplied, but the first component could not be
   * resolved.
   * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed Could not proceed
   * in resolving the n-1 components of the supplied name.
   * @exception org.omg.CosNaming.NamingContextPackage.InvalidName The supplied name
   * is invalid (i.e., has length less than 1).   
   * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
   * @see #resolve
   */
    @Naming
    public static void doUnbind(NamingContextDataStore impl,
                                NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound,
               org.omg.CosNaming.NamingContextPackage.CannotProceed,
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if (n.length < 1) {
            throw new InvalidName();
        }

        if (n.length == 1) {
            if ( (n[0].id.length() == 0) && (n[0].kind.length() == 0 ) ) {
                throw new InvalidName();
            }

            org.omg.CORBA.Object objRef;
            synchronized (impl) {
                objRef = impl.unbindImpl(n[0]);
            }
      
            if (objRef == null) {
                throw new NotFound(NotFoundReason.missing_node, n);
            }
        } else {
            NamingContext context = resolveFirstAsContext(impl,n);
            NameComponent[] tail = new NameComponent[n.length - 1];
            System.arraycopy(n,1,tail,0,n.length-1);

            context.unbind(tail);
        }
    }

    /**
   * Implements resolving a NameComponent in this context and
   * narrowing it to CosNaming::NamingContext. It will throw appropriate
   * exceptions if not found or not narrowable.
   * @param impl an implementation of NamingContextDataStore
   * @param n a NameComponents which is the name to be found.
   * @exception org.omg.CosNaming.NamingContextPackage.NotFound The
   * first component could not be resolved.
   * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
   * @see #resolve
   * @return a naming context
   */
    @Naming
    protected static NamingContext resolveFirstAsContext(
        NamingContextDataStore impl, NameComponent[] n)
        throws org.omg.CosNaming.NamingContextPackage.NotFound {

        org.omg.CORBA.Object topRef;
        BindingTypeHolder bth = new BindingTypeHolder();
        NamingContext context;
    
        synchronized (impl) {
            topRef = impl.resolveImpl(n[0],bth);
            if (topRef == null) {
                throw new NotFound(NotFoundReason.missing_node,n);
            }
        }
      
        if (bth.value != BindingType.ncontext) {
            throw new NotFound(NotFoundReason.not_context,n);
        }
      
        try {
            context = NamingContextHelper.narrow(topRef);
        } catch (org.omg.CORBA.BAD_PARAM ex) {
            throw new NotFound(NotFoundReason.not_context,n);
        }

        return context;
    }


   /**
    * This operation creates a stringified name from the array of Name
    * components.
    * @param n Name of the object
    * @return the object name as a single string
    * @throws org.omg.CosNaming.NamingContextPackage.InvalidName
    * Indicates the name does not identify a binding.
    *
    */
    @Naming
    public String to_string(org.omg.CosNaming.NameComponent[] n)
         throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if ( (n == null ) || (n.length == 0) ) {
            throw new InvalidName();
        }

        String theStringifiedName = insImpl.convertToString( n );

        if (theStringifiedName == null) {
            throw new InvalidName();
        }
        
        return theStringifiedName;
    }


   /**
    * This operation  converts a Stringified Name into an  equivalent array
    * of Name Components.
    * @param sn Stringified Name of the object <p>
    * @return an array of name components
    * @throws org.omg.CosNaming.NamingContextPackage.InvalidName if the name is invalid
    *
    */
    @Naming
    @Override
    public org.omg.CosNaming.NameComponent[] to_name(String sn)
         throws org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if  ((sn == null ) || (sn.length() == 0)) {
            throw new InvalidName();
        }

        org.omg.CosNaming.NameComponent[] theNameComponents =
            insImpl.convertToNameComponent( sn );
        if (( theNameComponents == null ) || (theNameComponents.length == 0)) {
            throw new InvalidName();
        }

        for (NameComponent theNameComponent : theNameComponents) {
            if (((theNameComponent.id == null)
                    || (theNameComponent.id.length() == 0))
                    && ((theNameComponent.kind == null)
                    || (theNameComponent.kind.length() == 0))) {
                throw new InvalidName();
            }
        }

        return theNameComponents;
    }

   /**
    * This operation creates a URL based "iiopname://" format name
    * from the Stringified Name of the object.
    * @param addr internet based address of the host machine where  
    * Name Service is running <p>
    * @param sn Stringified Name of the object <p>
    * @return a url string
    * @throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress if the provided address is invalid
    * @throws org.omg.CosNaming.NamingContextPackage.InvalidName if the provided Name is invalid
    *
    */ 
    @Naming
    public String to_url(String addr, String sn)
        throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress, 
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        if ((sn == null ) || (sn.length() == 0)) {
            throw new InvalidName();
        }

        if( addr == null ) {
            throw new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        }

        String urlBasedAddress;
        urlBasedAddress = insImpl.createURLBasedAddress( addr, sn );

        try {
            INSURLHandler.getINSURLHandler( ).parseURL( urlBasedAddress );
        } catch( BAD_PARAM e ) {
            throw new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        }

        return urlBasedAddress;
    }

    /**
     * This operation resolves the Stringified name into the object
     * reference.
     * @param sn Stringified Name of the object 
     * @return  an object
     * @exception org.omg.CosNaming.NamingContextPackage.NotFound
     * Indicates there is no object reference for the given name.
     * @exception org.omg.CosNaming.NamingContextPackage.CannotProceed
     * Indicates that the given compound name is incorrect
     * @throws org.omg.CosNaming.NamingContextPackage.InvalidName if the provided Name was invalid
     *
     */
    @Naming
    public org.omg.CORBA.Object resolve_str(String sn)
        throws org.omg.CosNaming.NamingContextPackage.NotFound, 
               org.omg.CosNaming.NamingContextPackage.CannotProceed, 
               org.omg.CosNaming.NamingContextPackage.InvalidName
    {
        org.omg.CORBA.Object theObject;
        if ((sn == null) || (sn.length() == 0)) {
            throw new InvalidName();
        }

        org.omg.CosNaming.NameComponent[] theNameComponents =
            insImpl.convertToNameComponent( sn );

        if ((theNameComponents == null) || (theNameComponents.length == 0 )) {
            throw new InvalidName();
        }

        theObject = resolve( theNameComponents );
        return theObject;
    }

}
