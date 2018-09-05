/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package argparser ;

import java.util.Collection ;
import java.util.Collections ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;
import java.util.HashMap ;

import java.lang.reflect.Array ;
import java.lang.reflect.Method ;
import java.lang.reflect.Proxy ;
import java.lang.reflect.InvocationHandler ;

import java.net.URL ;
import java.util.Arrays;

/** A general purpose argument parser that uses annotations, reflection,
 * and generics.
 * <p>
 * This class solves a simple problem: How do I write an argument parser
 * that parses arguments of the form 
 * <p>
 * -xxx value 
 * <p>
 * There are many ways of doing this: the ORB has probably at least 6
 * different ways of attacking this problem (and I wrote half of them).
 * The approach taken here is to start with an interface, annotate it 
 * to indicate some information (like default values) for parsing, and then
 * use reflection to produce an implementation of the interface that uses a
 * dynamic proxy.
 * <p>
 * The argument parsing is entirely specified in terms of an interface with
 * annotated methods.  The name of the method is the name used as a -xxx 
 * argument when parsing the data.  The type of data determines the valid
 * inputs for the argument.  All arguments must be in the -xxx yyy form,
 * where yyy is a valid input for the type of method xxx() in the interface.
 * <p>
 * The interface is very simply: just create an ArgParser of the appropriate type,
 * and then call parse( String[] ) to get an instance of the interface.  There
 * is also a getHelpText() method that returns a String with formatted information
 * about all the valid arguments for this parser.
 * <p>
 * Not all required information can be derived from the method type and name.
 * Some annotations are defined for additional information:
 * <ol>
 * <li>@DefaultValue.  This gives a String that is used to construct a default value
 * for the given method.
 * <li>@Separator.  This is used to override the default "," separator used for
 * lists of values for fields of type List&lt;Type&gt; or Type[].
 * <li>@Help.  This is used to give any help text for the field that is displayed
 * by the getHelpText method.
 * </ol>
 *
 * TODO:
 *
 * <ol>
 * <li>Support I18N for this (required before it could be used for public 
 * parts of the ORB).
 * <li>Support Set&lt;Type&gt;  Similar to list, but all elements must be unique.
 * <li>Support Map&lt;Type,Type&gt;
 * <li>Do we want to support really complex types, like Map&lt;String,List&lt;Map&lt;String,Foo&gt;&gt;&gt;
 * How would we specify needed separators?
 * <li>Should we extend along the lines of the ORB arg parser, with an embedded 
 * language to describe how to parse a String?
 * <li>Extend this to support property parsing as well (required for ORB.init parsing).
 * Probably just want to parse Properties as well as String[].  This requires some
 * way to handle the property prefix (as in -ORBInitHost and org.omg.CORBA.ORBInitHost).
 * <li>Extend to support abstract classes.  Only non-private abstract methods 
 * that are read-only
 * beans and are annotated with @DefaultValue can be set by the parser.  Other 
 * methods are allowed, but not used by the parser.
 * <li>Support an @Complete annotation.  All such methods must be void(), and are
 * executed in some order after the parsing is complete.
 * <li>Add an @Keyword notation to override the default name of the argument (the
 * method name).  This allows having different names for the methods and the arguments.
 * Note that this can be used for fully-qualified property names.  This supports the
 * ORB dual use of properties and arguments (with perhaps a little fudging for
 * things like ORBInitRef: if there are multiple args, concat them together and use
 * a list separator?)
 * <li>Add an @ClassName annotation that takes no arguments. This means that the
 * string passed must be the name of a class that has a no-args constructor.
 * The class must be assignment compatible with the return type of the annotated
 * method.  At parse time, we just load the class named by the string and create
 * a new instance of it.
 * <li>Consider using apt for this.  This solves two problems:
 * <ol>
 * <li>We can generate a more efficient class than the current version that 
 * uses a dynamic proxy and a table lookup.  This becomes important when
 * we consider that ORBData may be referenced many times during an invocation.
 * <li>We can generate much of the required ORBConstants (say as ORBPropertyNames)
 * directly from the annotated interface.
 * <li>
 * </ol>
 * <li>Most of the extensions here are aimed at reducing the code size of the
 * ORB data stuff.
 * <li>If we don't want to use apt:
 * <ol>
 * <li>Use codegen to generate a class that implements the argument interface.
 * <li>Instead of generating ORBConstants, look things up from it using static
 * reflection on the name of the constant.  This would require some other annotations:
 * <ol>
 * <li>@IndirectKeyword gives the name of the constant in the @ConstantClass to look for.
 * Do we need multiple constant classes?
 * <li>@ConstantClass would give the class to use for looking up public static final
 * constants for @IndirectKeyword, which is like @Keyword.
 * </ol>
 * </ol>
 * </ol>
 *
 * Summary of current and proposed annotations:
 * <ol>
 * <li>@DefaultValue: current method-level takes string
 * <li>@Separator: current method-level takes string
 * <li>@Help: current method-level takes string
 * <li>@ConstantClass: proposed class-level takes class constant
 * <li>@Complete: proposed method-level marker
 * <li>@Keyword: proposed method-level takes string
 * <li>@IndirectKeyword: proposed method-level takes string
 * <li>@ClassName: proposed method-level marker
 * <li>
 * </ol>
 *
 * @author Ken Cavanaugh
 */
public class ArgParser {
    private final List<Class<?>> interfaceClasses =
        new ArrayList<Class<?>>() ;
    private final Map<String,String> helpText =
        new HashMap<String, String>() ;
    private final Map<String,Object> defaultValues =
        new HashMap<String,Object>() ;
    private final Map<String,ElementParser> parserData =
        new HashMap<String,ElementParser>() ;

    /** Construct an ArgParser that parses an argument string into an instance of the 
     * Class argument.
     * cls must be an interface.  Each method in this interface must take no arguments. 
     * Each method must be annotated with a DefaultValue annotation.
     * Each method must return one of the following types:
     * <ul>
     * <li>A primitive type
     * <li>A String type
     * <li>A type that has a public constructor that takes a single String argument
     * <li>An Enum 
     * <li>A List parameterized by one of the above types
     * <li>An array of one of the first four types
     * </ul>
     * The name of the method is the name of the keyword.
     */
    public ArgParser( Class<?> cls ) {
        init( cls ) ;
    }

    public ArgParser( List<Class<?>> classes ) {
        init(classes ) ;
    }

    private void init( Class<?> cls ) {
        List<Class<?>> classes = new ArrayList<Class<?>>() ;
        classes.add( cls ) ;
        init( classes ) ;
    }

    private void init( List<Class<?>> classes) {
        final Map<String,String> defaultValueData = new HashMap<String,String>() ;
        for (Class<?> cls : classes) {
            if (!cls.isInterface()) {
                error(cls.getName() + " is not an interface");
            }
            interfaceClasses.add( cls ) ;

            // Construct the list of ParserData entries for the methods in cls, and
            // also a map from keyword to default value data for the @DefaultValue
            // annotations.
            for (Method m : cls.getMethods()) {
                final String keyword = checkMethod( m ) ;
                final ElementParser ep = ElementParser.factory.evaluate( m ) ;
                parserData.put( keyword, ep ) ;

                final DefaultValue dv = m.getAnnotation( DefaultValue.class ) ;
                if (dv == null) {
                    error( "Method " + m.getName()
                        + " does not have a DefaultValue annotation" ) ;
                } else {
                    defaultValueData.put( keyword, dv.value() ) ;
                }

                final Help help = m.getAnnotation( Help.class ) ;
                if (help != null) {
                    helpText.put(keyword, help.value());
                }
            }
        }

        internalParse( defaultValueData, defaultValues ) ;
    }

    private String display( Object obj ) {
	if (obj.getClass().isArray()) {
	    StringBuilder sb = new StringBuilder() ;
	    sb.append( "[" ) ;
	    for (int ctr=0; ctr<Array.getLength( obj ); ctr++ ) {
		Object element = Array.get( obj, ctr ) ;
		if (ctr > 0) {
		    sb.append( "," ) ;
		}
		sb.append( element.toString() ) ;
	    }
	    sb.append( "]" ) ;
	    return sb.toString() ;
	} else if (obj instanceof Collection) {
	    StringBuilder sb = new StringBuilder() ;
	    sb.append( "[" ) ;
	    boolean first = true ;
	    for (Object element : (Collection)obj) {
		if (first) {
		    first = false ;
		} else {
		    sb.append( "," ) ;
		}
		sb.append( element.toString() ) ;
	    }
	    sb.append( "]" ) ;
	    return sb.toString() ;
	} else {
	    return obj.toString() ;
	}
    }

    /** Returns a formatted text string that describes the expected
     * arguments for this parser.
     */
    public String getHelpText() {
	StringBuilder sb = new StringBuilder() ;
	sb.append( "    Legal arguments are:\n" ) ;
	Set<String> keys = parserData.keySet() ;
	List<String> keyList = new ArrayList<String>( keys ) ;
	Collections.sort( keyList ) ;
	for (String keyword : keyList) {
	    ElementParser ep = parserData.get( keyword ) ;
	    sb.append("\t-").append(keyword).append( " <") ;
	    boolean first = true ;
	    for (String str : ep.describe() ) {
		if (first) {
                    first = false;
                }
		else {
                    sb.append("\n\t    ");
                }

		sb.append( str ) ;
	    }
	    sb.append( ">\n" ) ;

	    String defaultValue = display(defaultValues.get(keyword)) ;
	    sb.append("\t    " + "(default ").append(defaultValue)
                .append( ")\n") ;

	    String help = helpText.get( keyword ) ;
	    if (help != null) {
		sb.append("\t    ").append(help).append( "\n") ;
	    }

	    sb.append( "\n" ) ;
	}

	return sb.toString() ;
    }

    /** Parse the argument string into an instance of type T.
     */
    public Object parse( String[] args ) {
	Map<String,String> data = makeMap( args ) ;
	Map<String,Object> pdata = new HashMap<String,Object>() ;
        internalParse( data, pdata ) ;
	Object result = makeProxy( pdata ) ;
	return result ;
    }

    private void error( String msg ) {
	System.out.println( "Error in argument parser: " + msg ) ;
	System.out.println( getHelpText() ) ;
	throw new RuntimeException ( msg ) ;
    }

    // Check that method has no arguments 
    private String checkMethod( Method m ) {
	if (m.getParameterTypes().length == 0) {
            String result = m.getName() ;
            if (parserData.containsKey(result )) {
                error( "Keyword " + result + " is already defined.") ;
            } else {
                return m.getName();
            }
        } else {
            error("Method " + m.getName() + " must not have any parameters");
        }

	return null ;
    }

    private void internalParse( Map<String,String> data,
        Map<String,Object> result ) {
	for (Map.Entry<String,String> entry : data.entrySet()) {
	    String keyword = entry.getKey() ;
	    ElementParser ep = parserData.get( keyword ) ;
	    if (ep == null) {
                error(keyword + " is not a valid keyword");
            }
	    Object val = ep.evaluate( entry.getValue() ) ;
	    result.put( keyword, val ) ;
	}
    }

    private String getKeyword( String arg ) {
	if (arg.charAt(0) == '-') {
            return arg.substring(1);
        } else {
            error(arg + " is not a valid keyword");
        }
	return null ; // not reachable
    }

    // Data must all be of the form (-keyword value)*
    private Map<String,String> makeMap( String[] args ) {
	Map<String,String> result = new HashMap<String,String>() ;
	String keyword = null ;
	for (String arg : args) {
	    if (keyword == null) {
                keyword = getKeyword(arg);
            } else {
		result.put( keyword, arg ) ;
		keyword = null ;
	    }
	}
	if (keyword != null) {
            error("No argument supplied for keyword " + keyword);
        }
	return result ;
    }

    // Make a dynamic proxy of type T for the given data.
    // The keys in the data must be the same as the method names in
    // the type T.
    private Object makeProxy( final Map<String,Object> data ) {
	final InvocationHandler ih = new InvocationHandler() {
	    private Object getValue( final String keyword ) {
		Object result = data.get( keyword ) ;
		if (result == null) {
                    result = defaultValues.get(keyword);
                }
		return result ;
	    }

	    private String getString( final Object obj ) {
		final Class cls = obj.getClass() ;
		if (cls.isArray()) {
		    final StringBuilder sb = new StringBuilder() ;
		    sb.append( "[" ) ;
		    for (int ctr=0; ctr<Array.getLength( obj ); ctr++) {
			if (ctr>0) {
			    sb.append( "," ) ;
			}
			
			Object element = Array.get( obj, ctr ) ;
			sb.append( element.toString() ) ;
		    }

		    sb.append( "]" ) ;
		    return sb.toString() ;
		} else {
		    return obj.toString() ;
		}
	    }

            @Override
	    public Object invoke( final Object proxy, final Method method, 
                final Object[] args ) {

		final String name = method.getName() ;
		if (name.equals("toString")) {
		    final StringBuilder sb = new StringBuilder() ;
		    for (String keyword : parserData.keySet()) {
			sb.append( keyword ) ;
			sb.append( "=" ) ;
			sb.append( getString( getValue( keyword ) ) ) ;
			sb.append( "\n" ) ;
		    }
		    return sb.toString() ;
		} else {
		    return getValue( name ) ;
		}
	    }
	} ;

	final Class<?>[] interfaces = interfaceClasses.toArray(
            new Class<?>[interfaceClasses.size()] ) ;

        ClassLoader cl = this.getClass().getClassLoader() ;
        if (cl == null) {
            cl = interfaces[0].getClass().getClassLoader() ;
        }
        if (cl == null) {
            ClassLoader.getSystemClassLoader() ;
        }

	return Proxy.newProxyInstance( cl, interfaces, ih ) ;
    }

////////////////////////////////////////////////////////////////////////////////////
//
// Data for built-in test
//
////////////////////////////////////////////////////////////////////////////////////

    private enum PrimaryColor { RED, GREEN, BLUE } ;

    public static class StringPair extends Pair<String,String> {
	public StringPair( String data ) {
	    super( null, null ) ;
	    int index = data.indexOf( ':' ) ;
	    if (index < 0) {
                throw new IllegalArgumentException(data
                    + " does not contain a :");
            }
	    _first = data.substring( 0, index ) ;
	    _second = data.substring( index + 1 ) ;
	}
    }

    private interface TestInterface1 {
	@DefaultValue( "27" ) 
	@Help( "An integer value" )
	int value() ;

	@DefaultValue( "Michigan" ) 
	@Help( "The name of a lake" )
	String lake() ;

	@DefaultValue( "RED" ) 
	@Help( "Pick a color" )
	PrimaryColor color() ;
    }

    private interface TestInterface2 {
	@DefaultValue( "http://www.sun.com" ) 
	@Help( "your favorite URL" ) 
	URL url() ;

	@DefaultValue( "funny:thing,another:thing,something:else" ) 
	@Help( "A list of pairs of the form xxx:yyy" )
	@Separator( "," ) 
	StringPair[] arrayData() ;

	@DefaultValue( "funny:thing,another:thing,something:else" ) 
	@Help( "A list of pairs of the form xxx:yyy" )
	@Separator( "," )
	List<StringPair> listData() ;

        @DefaultValue( "" )
        @Help( "A list of strings")
        List<String> str() ;
    }

    public static void main( String[] args ) {
        Class<?>[] interfaces = { TestInterface1.class, TestInterface2.class } ;
	ArgParser ap = new ArgParser( Arrays.asList( interfaces) ) ;
	System.out.println( "Help text for this parser:\n" + ap.getHelpText() ) ;
	Object result = ap.parse( args ) ;
        if (!(result instanceof TestInterface1)
            && !(result instanceof TestInterface2)) {
            System.out.println( "Error: result not an instance of both test interfaces" ) ;
        }

	System.out.println( "Result is:\n" + result ) ;
    }
}
