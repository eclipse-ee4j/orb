/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package rmic;

import test.Test;
import sun.rmi.rmic.iiop.StaticStringsHash;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.glassfish.pfl.test.JUnitReportHelper;

/*
 * @test
 */
public class StaticStringsHashTest extends Test {

    static final String[][] TEST_CASES = {
        {
            "a", "b", "c"   
        },
        {
            "one", "two", "three"   
        },
        {
            "a", "bb", "ccc"   
        },
        {
            "method_1", "method_3", "method_2"   
        },
        {
            "asd;lfjafd;ljadsfkja;dslfjadsiewqrpouew0-adsflkjfdsa[09qrewlkjafdm/asdfa.madsfpoiuawer;lkjadfa;lkjadsf;lkjqewrpoiqu", 
            "a;lkqreoiufds;lkafdsoiuqerw;nafdpoiuafd;ln;aqreopiuadslkfjafdpoiuqrew;lkjasdfpoiuawer;lnvc/nm/",
            "dfas;lkjrewqpoiuafd.,mn40987145lkhjafavpoiuqre,mnzv.,mvczxoiusatrnafd;lhjafo",
            "daf;lkjasdfp0uqrnmae0uafd;lkjaffjkl;asdpoiuqweru8a8fdsj;lfa/.m vc;'afd[poier",
            "affj;lkasfdpoiuqreq0-87450987afjafpoiuasdf87qwerjadfsakj;adsfuvc;lkvczjafdoupafup",
            "sadf;lkjasdf;ljafds;lkjdsaf;lkjasdf;lkjfdsapoiuewqrpoiuwqerojadsf;lkj",
            "adsf;lkjafds[uqewr;ljaf[iuqwer;lkjasadf;lkjasdfopiuqwer;lkjafpoiuqewr;lkjqerui",
            "afd;lkjasdfpoiuqrewpoiuqewr098745-08752430-9862-098450987tr5jartp0u",
        },
        {
            "IOR:0000000000000023524d493a6578616d706c652e48617368633a303030303030303030303030303030300000000000010000000000000054000101000000000b392e362e32322e32303200000479000000000018afabcafe0000000283d0ea290000000800000003000000000000000100000001000000140000000000010020000000000001010000000000",
            "IOR:0000000000000024524d493a6578616d706c652e53616d706c653a3030303030303030303030303030303000000000010000000000000054000101000000000b392e362e32322e32303200000479000000000018afabcafe0000000283d0ea290000000800000002000000000000000100000001000000140000000000010020000000000001010000000000",
        },
        {
            "imageUpdate",
            "_get_font",
            "remove",
            "postEvent",
            "_get_name",
            "_set_name",
            "_get_parent",
            "_get_peer",
            "_get_treeLock",
            "_get_toolkit",
            "_get_valid",
            "_get_visible",
            "_get_showing",
            "_get_enabled",
            "_set_enabled",
            "enable__",
            "enable__boolean",
            "disable",
            "_set_visible",
            "show__",
            "show__boolean",
            "hide",
            "_get_foreground",
            "_set_foreground",
            "_get_background",
            "_set_background",
            "_set_font",
            "_get_locale",
            "_set_locale",
            "_get_colorModel",
            "_get_location__",
            "_get_locationOnScreen",
            "location",
            "setLocation",
            "move",
            "_set_location__",
            "_get_size__",
            "size",
            "setSize",
            "resize__long__long",
            "_set_size__",
            "resize__java_awt_Dimension",
            "_get_bounds__",
            "bounds",
            "setBounds",
            "reshape",
            "_set_bounds__",
            "_get_preferredSize__",
            "preferredSize",
            "_get_minimumSize__",
            "minimumSize",
            "_get_maximumSize",
            "_get_alignmentX",
            "_get_alignmentY",
            "doLayout",
            "layout",
            "validate",
            "invalidate",
            "_get_graphics",
            "getFontMetrics",
            "_set_cursor",
            "_get_cursor",
            "paint",
            "update",
            "paintAll",
            "repaint__",
            "repaint__long_long",
            "repaint__long__long__long__long",
            "repaint__long_long__long__long__long__long",
            "print",
            "printAll",
            "createImage__java_awt_image_ImageProducer",
            "createImage__long__long",
            "prepareImage__java_awt_Image__java_awt_image_ImageObserver",
            "prepareImage__java_awt_Image__long__long__java_awt_image_ImageObserver",
            "checkImage__java_awt_Image__java_awt_image_ImageObserver",
            "checkImage__java_awt_Image__long__long__java_awt_image_ImageObserver",
            "contains__long__long",
            "inside",
            "contains__java_awt_Point",
            "getComponentAt__long__long",
            "locate",
            "getComponentAt__java_awt_Point",
            "deliverEvent",
            "dispatchEvent",
            "dispatchEventImpl",
            "areInputMethodsEnabled",
            "eventEnabled",
            "addComponentListener",
            "removeComponentListener",
            "addFocusListener",
            "removeFocusListener",
            "addKeyListener",
            "removeKeyListener",
            "addMouseListener",
            "removeMouseListener",
            "addMouseMotionListener",
            "removeMouseMotionListener",
            "_get_inputContext",
            "enableEvents",
            "disableEvents",
            "processEvent",
            "processComponentEvent",
            "processFocusEvent",
            "processKeyEvent",
            "processMouseEvent",
            "processMouseMotionEvent",
            "postsOldMouseEvents",
            "handleEvent",
            "mouseDown",
            "mouseDrag",
            "mouseUp",
            "mouseMove",
            "mouseEnter",
            "mouseExit",
            "keyDown",
            "keyUp",
            "action",
            "addNotify",
            "removeNotify",
            "gotFocus",
            "lostFocus",
            "_get_focusTraversable",
            "requestFocus",
            "transferFocus",
            "nextFocus",
            "add",
            "paramString",
            "toString",
            "list__",
            "list__java_io_PrintStream",
            "list__java_io_PrintStream__long",
            "list__java_io_PrintWriter",
            "list__java_io_PrintWriter__long",
            "_get_nativeContainer",
        },
        {
            "* This interface imposes a total ordering on the objects of each class that",
            "* implements it.  This ordering is referred to as the class's <i>natural",
            "* ordering</i>, and the class's <tt>compareTo</tt> method is referred to as",
            "* its <i>natural comparison method</i>.<p>",
            "* Lists (and arrays) of objects that implement this interface can be sorted",
            "* automatically by <tt>Collections.sort</tt> (and <tt>Arrays.sort</tt>).",
            "* Objects that implement this interface can be used as keys in a sorted map",
            "* or elements in a sorted set, without the need to specify a comparator.<p>",
            "* A class's natural ordering is said to be <i>consistent with equals</i> if",
            "* and only if <tt>(e1.compareTo((Object)e2)==0)</tt> has the same boolean",
            "* value as <tt>e1.equals((Object)e2)</tt> for every <tt>e1</tt> and",
            "* <tt>e2</tt> of class <tt>C</tt>.<p>",
            "* It is strongly recommended (though not required) that natural orderings be",
            "* consistent with equals.  This is so because sorted sets (and sorted maps)",
            "* without explicit comparators behave \"strangely\" when they are used with",
            "* elements (or keys) whose natural ordering is inconsistent with equals.  In",
            "* particular, such a sorted set (or sorted map) violates the general contract",
            "* for set (or map), which is defined in terms of the <tt>equals</tt>",
            "* operation.<p>",
            "* For example, if one adds two keys <tt>a</tt> and <tt>b</tt> such that",
            "* <tt>(a.equals((Object)b) && a.compareTo((Object)b) != 0)</tt> to a sorted",
            "* set that does not use an explicit comparator, the second <tt>add</tt>",
            "* operation returns false (and the size of the sorted set does not increase)",
            "* because <tt>a</tt> and <tt>b</tt> are equivalent from the sorted set's",
            "* perspective.<p>",
            "* Virtually all Java core classes that implement ComparableX have natural",
            "* orderings that are consistent with equals.  One exception is",
            "* <tt>java.math.BigDecimal</tt>, whose natural ordering equates",
            "* <tt>BigDecimals</tt> with equal values and different precisions (such as",
            "* 4.0 and 4.00).<p>",
            "* For the mathematically inclined, the <i>relation</i> that defines",
            "* the natural ordering on a given class C is:<pre>",
            "*   {(x, y) such that x.compareTo((Object)y) &lt;= 0}.",
            "* </pre> The <i>quotient</i> for this total order is: <pre>",
            "*   {(x, y) such that x.compareTo((Object)y) == 0}.",
            "* It follows immediately from the contract for <tt>compareTo</tt> that the",
            "* quotient is an <i>equivalence relation</i> on <tt>C</tt>, and that the",
            "* natural ordering is a <i>total order</i> on <tt>C</tt>.  When we say that a",
            "* class's natural ordering is <i>consistent with equals</i>, we mean that the",
            "* quotient for the natural ordering is the equivalence relation defined by",
            "* the class's <tt>equals(Object)</tt> method:<pre>",
            "* {(x, y) such that x.equals((Object)y)}.",
            "* </pre>",
        },
    };
    
    /**
     * Perform the test.
     */
    protected void check (String[] list) throws Throwable {

        StaticStringsHash hash = new StaticStringsHash(list);
        int length = list.length;

        // Make sure lengths are the same...
        
        if (hash.keys.length != hash.buckets.length) {
            throw new Error("lengths not equal");        
        }

        // Mark all the indices we find. Ensure that for each
        // bucket, each string in the bucket has the key
        // listed in the keys array...
        
        int[] found = new int[length];
        for (int i = 0; i < hash.buckets.length; i++) {
            int currentHash = hash.keys[i];
            for (int j = 0; j < hash.buckets[i].length; j++) {
                int index = hash.buckets[i][j];
                        
                // Make sure it is not already used...
                        
                if (found[index] == 1) {
                    throw new Error("index found more than once");
                } else {
                    found[index] = 1;
                }
                        
                int h = hash.getKey(list[index]);
                if (h != currentHash) {
                    throw new Error("hash does not match");   
                }
            }
        }
        
        // Make sure all indices are represented...
        
        for (int i = 0; i < length; i++) {
            if (found[i] != 1) {
                throw new Error("index "+i+" not found");
            }
        }
    }

    public void run () {
        JUnitReportHelper helper = new JUnitReportHelper(
            this.getClass().getName() ) ;

        try {
            for (int i = 0; i < TEST_CASES.length; i++) {
                helper.start( "test_" + i ) ;
                check(TEST_CASES[i]);   
                helper.pass() ;
            }
        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            status = new Error("Caught " + out.toString());
            helper.fail( e ) ;
        } finally {
            helper.done() ;
        }
    }
}
