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

package performance.eesample;

import java.lang.reflect.Field ;
import java.lang.reflect.Method ;

import java.lang.annotation.ElementType ;
import java.lang.annotation.RetentionPolicy ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;

import java.util.Arrays ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.Set ;
import java.util.HashMap ;
import java.util.Properties ;

import java.rmi.RemoteException ;
import java.rmi.Remote ;

import javax.rmi.CORBA.Tie ;
import javax.rmi.CORBA.Stub ;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConstants;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.LocalObject ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NamingContextExt ;
import org.omg.CosNaming.NamingContextExtHelper ;
import org.omg.CosNaming.NameComponent ;
import org.omg.CosNaming.NamingContextPackage.CannotProceed ;
import org.omg.CosNaming.NamingContextPackage.InvalidName ;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound ;
import org.omg.CosNaming.NamingContextPackage.NotFound ;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;

import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager ;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService ;

import com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject ;
import com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl ;
import com.sun.corba.ee.impl.javax.rmi.CORBA.Util ;

import com.sun.corba.ee.spi.extension.ServantCachingPolicy ;

import performance.eesample.wspex.Address ;
import performance.eesample.wspex.ArrayOfLineItem ;
import performance.eesample.wspex.Customer ;
import performance.eesample.wspex.EchoOrderRequest ;
import performance.eesample.wspex.EchoOrderResponse ;
import performance.eesample.wspex.Item ;
import performance.eesample.wspex.ItemArrayType;
import performance.eesample.wspex.LineItem ;
import performance.eesample.wspex.Location ;
import performance.eesample.wspex.Order ;
import performance.eesample.wspex.Struct ;
import performance.eesample.wspex.Synthetic ;
import performance.eesample.wspex.components.OrderBL;

import com.sun.japex.JapexDriverBase ;
import com.sun.japex.TestCase ;
import org.glassfish.pfl.basic.tools.argparser.ArgParser;
import org.glassfish.pfl.basic.tools.argparser.DefaultValue;
import org.glassfish.pfl.dynamic.codegen.spi.GenericClass;
import org.glassfish.pfl.dynamic.codegen.spi.Type;
import org.glassfish.pfl.test.TestBase;

/** Standard top-level ORB test.  Does the following:
 * <OL>
 * <LI>Tests CDR stream marshaling (for a copy) for a trivial call (long as arg) and
 * for more complex data (ArrayList of simple instances)
 * <LI>Tests inter ORB call with trivial data (long arg and result) and returning
 * complex data (same ArrayList).  The ORB instances are in the same VM, so loopback
 * TCP is used.
 * </OL>
 * These tests are designed to be easily profiled.
 *
 * I am modifying this test to use Japex, and to include the wspex test cases so that
 * we can more easily compare CORBA vs. SOAP/TCP.  This should work as follows:
 *
 * interface SingleTest {
 *  void prepare( int size ) ;
 *  void run() ;
 * }
 *
 * Make StandardTest extend JapexDriver
 * Implement the driver methods as follows:
 *  initializeDriver
 *      set up ORB 
 *      init stub
 *  prepare
 *      Gather parameters from the TestCase:
 *          - Test name
 *          - Test size
 *      Initialize an instance of SingleTest with the size required for the test
 *  warmup
 *      Just calls run 
 *  run
 *      calls SingleTest.run
 *  terminateDriver
 *      shutdown the ORB
 *      
 * @author Ken Cavanaugh
 */
public class StandardTest extends JapexDriverBase {
    private static Field proDelegate ;
    private static Field stubDelegateClass ;
    private static Field utilDelegate ;
    private static Field singletonORB ;
    private static boolean corbaReinit = false ;

    private static final boolean useSingleClassLoader = true ;

    static {
        if (!useSingleClassLoader) {
            Type.clearCaches() ;
            try {
                proDelegate = javax.rmi.PortableRemoteObject.class.getDeclaredField(
                    "proDelegate" ) ;
                stubDelegateClass = Stub.class.getDeclaredField( 
                    "stubDelegateClass" ) ;
                utilDelegate = javax.rmi.CORBA.Util.class.getDeclaredField( 
                    "utilDelegate" ) ;
                singletonORB = org.omg.CORBA.ORB.class.getDeclaredField(
                    "singleton" ) ;

                proDelegate.setAccessible( true ) ;
                stubDelegateClass.setAccessible( true ) ;
                utilDelegate.setAccessible( true ) ;
                singletonORB.setAccessible( true ) ;
            } catch (Exception exc) {
                System.out.println( "Error in initializer: " + exc ) ;
            }
        }
    }

    // This ugly little hack is here so that the CORBA delegates set in one
    // Japex run (to objects in that run's JapexClassLoader) are not reused
    // in a subsequent run, which causes severe problems with objects
    // not having the correct type (because the class names are the same,
    // but the ClassLoaders are different).  This works by re-initializaing delegates
    // at the beginning of each run. 
    private synchronized static void reinitCorbaDelegateHack() {
        if (!corbaReinit) {
            try {
                stubDelegateClass.set( null, StubDelegateImpl.class ) ;
                proDelegate.set( null, new PortableRemoteObject() ) ;
                utilDelegate.set( null, new Util() ) ; 

                synchronized( org.omg.CORBA.ORB.class ) {
                    singletonORB.set( null, null ) ;
                }
            } catch (Exception exc) {
                System.out.println( "Error in corbaDelegateHack: " + exc ) ;
            }
        }

        corbaReinit = true ;
    }

    // This is static because it is shared between the command-line instantiation
    // of StandardTest, and the instance created by Japex, when run in Japex mode.
    private static ArgParser ap = new ArgParser(
        ArgumentData.class ) ;

    // set up default parameters
    private static TestBase testBase ;
    private static ArgumentData argData = ap.parse( new String[]{},
        ArgumentData.class ) ;
    private static int numInstances = 0 ;

    static {
        // The following must be set as system properties 
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" ) ;
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY,
            "true" ) ;
    }

    private int instance = numInstances++ ;

    // Client state
    private ORB clientORB = null ;
    private NamingContextExt clientNamingRoot = null ;

    // Server state
    private ORB serverORB = null ;
    private NamingContextExt serverNamingRoot = null ;

    // Client and server state are both active in LOCAL mode.
    
    public StandardTest() {
        // nothing to do here
    }

    private static String getTransportDescription() {
        return (argData.blocking() ? "Blocking transport(" : "Default transport(")
            + argData.fragmentSize() + ")" ;
    }

    private synchronized void log( String msg ) {
        System.out.println( "(" + instance + ") " + msg ) ;
    }

    private static void decodeAddress(Address address) {
        decodeString(address.getAddress1());
        decodeString(address.getAddress2());
        decodeString(address.getCity());
        decodeString(address.getFirstName());
        decodeString(address.getLastName());
        decodeString(address.getState());
        decodeString(address.getZip());
    }

    private static void decodeArray(ItemArrayType echoArray) {
        List<Item> items = echoArray.getItems();
        for (Item item : items) {
            decodeItem(item);
        }
    }

    private static void decodeCustomer(Customer customer) {
        decodeAddress(customer.getBillingAddress());
        decodeString(customer.getContactFirstName());
        decodeString(customer.getContactLastName());
        decodeString(customer.getContactPhone());
        decodeString(customer.getCreditCardExpirationDate());
        decodeString(customer.getCreditCardNumber());
        int id = customer.getCustomerId();
        XMLGregorianCalendar calendar = customer.getLastActivityDate();
        decodeAddress(customer.getShippingAddress());
    }

    private static void decodeItem(Item item) {
        XMLGregorianCalendar calendar = item.getCreationdate();
        decodeString(item.getDescription());
        decodeString(item.getId());
        int inv = item.getInventory();
        float price = item.getPrice();
        decodeLocation(item.getLocation());
    }

    private static void decodeLineItem(LineItem lineItem) {
        int itemId = lineItem.getItemId();
        int orderId = lineItem.getOrderId();
        int q = lineItem.getOrderQuantity();
        decodeString(lineItem.getProductDescription());
        int productId = lineItem.getProductId();
        float price = lineItem.getUnitPrice();
    }

    private static void decodeLineItems(ArrayOfLineItem lineItems) {
        List<LineItem> lineitems = lineItems.getLineItem();
        for (LineItem lineItem : lineitems) {
            decodeLineItem(lineItem);
        }
    }

    private static void decodeLocation(Location location) {
        decodeString(location.getAddress());
        decodeString(location.getDescription());
        decodeString(location.getId());
    }

    private static void decodeOrder(Order echoOrder) {
        decodeCustomer(echoOrder.getCustomer());
        decodeLineItems(echoOrder.getLineItems());
        XMLGregorianCalendar calendar = echoOrder.getOrderDate();
        int id = echoOrder.getOrderId();
        int status = echoOrder.getOrderStatus();
        float amount = echoOrder.getOrderTotalAmount();
    }

    private static void decodeString(String echoString) {
        int length = echoString.length();
    }
    
    private static void decodeStruct(Struct echoStruct) {
        float f = echoStruct.getVarFloat();
        int i = echoStruct.getVarInt();
        decodeString(echoStruct.getVarString());
    }

    private static void decodeSynthetic(Synthetic echoSynthetic) {
        byte[] b = echoSynthetic.getBytes();
        decodeStruct(echoSynthetic.getS());
        decodeString(echoSynthetic.getStr());
    }

    private synchronized void fatal( String msg, Throwable thr ) {
        thr.printStackTrace() ;
        log( msg ) ;
        System.exit( 1 ) ;
    }

    public interface SingleTest {
        String description() ;

        /** Initialize data needed for this test
         *
         * @param size Size of the test
         */
        void prepare( int size ) ;

        /** Run the test
         *
         * @throws Exception
         */
        void run() throws Exception ;
    }

    public abstract static class SingleTestBase implements SingleTest {
        protected int size = 0 ;
        protected final Test testRef ;

        public SingleTestBase( Test testRef ) {
            this.testRef = testRef ;
        }

        public String description() {
            String className = this.getClass().getName() ;
            final int lastDot = className.lastIndexOf( '.' ) ;
            if (lastDot >= 0) {
                className = className.substring(lastDot + 1);
            }

            if (size == 0) {
                return className + " " + getTransportDescription();
            } else {
                return className + size + " " + getTransportDescription();
            }
        }

        // Default implementation of prepare.
        public void prepare( int size ) {
        }

        // All subclasses must define run.
    }

    public static class EchoTest extends SingleTestBase {
        private int value = 0 ;

        public EchoTest( Test testRef ) {
            super( testRef ) ;
        }

        public void run() throws Exception {
            value += testRef.echo( value + 1 ) ;
        }
    }

    public static class GetDataTest extends SingleTestBase {
        public GetDataTest( Test testRef ) {
            super( testRef ) ;
        }

        public void run() throws Exception {
            testRef.getData();
        }
    }

    public static class GetDataArrayTest extends SingleTestBase {
        public GetDataArrayTest( Test testRef ) {
            super( testRef ) ;
        }

        public void run() throws Exception {
            testRef.getDataArray();
        }
    }

    public static class GetTestRefsTest extends SingleTestBase {
        public GetTestRefsTest( Test testRef ) {
            super( testRef ) ;
        }

        public void run() throws Exception {
            testRef.getTestRefs() ;
        }
    }

    public static class EchoStringTest extends SingleTestBase {
        public EchoStringTest( Test testRef ) {
            super( testRef ) ;
        }

        public void run() throws Exception {
            String str = testRef.echoString("Hello World");
            int i = str.length();
        }
    }

    public static class EchoVoidTest extends SingleTestBase {
        public EchoVoidTest( Test testRef ) {
            super( testRef ) ;
        }

        public void run() throws Exception {
            testRef.echoVoid() ;
        }
    }

    public static class EchoStructTest extends SingleTestBase {
        private Struct struct ;

        public EchoStructTest( Test testRef ) {
            super( testRef ) ;
        }

        @Override
        public void prepare( int size ) {
            struct = new Struct();
            struct.setVarInt(5);
            struct.setVarFloat(2.5f);
            struct.setVarString("Hello There!");            
        }

        public void run() throws Exception {
            decodeStruct( testRef.echoStruct(struct ) ) ;
        }
    }

    public static class EchoSyntheticTest extends SingleTestBase {
        private Synthetic synthetic ;

        public EchoSyntheticTest( Test testRef ) {
            super( testRef ) ;
        }

        @Override
        public void prepare( int size ) {
            this.size = size ;
            Struct struct = new Struct();
            struct.setVarInt(5);
            struct.setVarFloat(2.5f);
            struct.setVarString("Hello There!");
            byte[] bytes = new byte[size];
            synthetic = new Synthetic();
            synthetic.setStr("Hello World");
            synthetic.setS(struct);
            synthetic.setBytes(bytes);
        }

        public void run() throws Exception {
            decodeSynthetic(testRef.echoSynthetic(synthetic));
        }
    }

    public static class EchoArrayTest extends SingleTestBase {
        private ItemArrayType items ;

        public EchoArrayTest( Test testRef ) {
            super( testRef ) ;
        }

        @Override
        public void prepare( int size ) {
            this.size = size ;

            final Item[] itemlist = new Item[size];
            items = new ItemArrayType();
            
            for (int i = 0; i < size; i++) {
                itemlist[i] = new Item();
                itemlist[i].setId(java.lang.Integer.toString(i));
                itemlist[i].setDescription("Item Description");
                itemlist[i].setPrice(100.00f);
                itemlist[i].setInventory(i*100);

                Location loc = new Location();
                loc.setId("CA95050");
                loc.setDescription("Santa Clara, California");
                loc.setAddress("1000, network circle");
                itemlist[i].setLocation(loc);
                
                try {
                    DatatypeFactory df = DatatypeFactory.newInstance();
                    XMLGregorianCalendar cal = df.newXMLGregorianCalendar();
                    cal.setYear(2005);
                    cal.setMonth(DatatypeConstants.MARCH);
                    cal.setDay(29);
                    cal.setTime(11,11,11);
                    itemlist[i].setCreationdate(cal);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
                items.getItems().add(itemlist[i]);
            }
        }

        public void run() throws Exception {
            decodeArray(testRef.echoArray(items));
        }
    }

    public static class GetOrderTest extends SingleTestBase {
        private int orderId ;
        private int customerId ;

        public GetOrderTest( Test testRef ) {
            super( testRef ) ;
        }

        @Override
        public void prepare( int size ) {
            orderId = 999 ;
            customerId = 109292 ;
        }

        public void run() throws Exception {
            Order or = testRef.getOrder(++orderId, customerId);
            if (or.getOrderId() != orderId) {
                throw new IllegalStateException("Not equal!");
            }
            decodeOrder(or);
            // do nothing with the order
        }
    }

     public static class EchoOrderTest extends SingleTestBase {
        private int orderId ;
        private int customerId ;
        private int mySize ;
        private Order order ;

        public EchoOrderTest( Test testRef ) {
            super( testRef ) ;
        }

        @Override
        public void prepare( int size ) {
            orderId = 999 ;
            customerId = 109292 ;

            this.mySize = size ;

            int id = 1;
            
            // create the order object
            Address ship = new Address();
            ship.setFirstName("Ship FirstName "+ id);
            ship.setLastName("Ship LastName " + id);
            ship.setAddress1( "Ship StreetAddres " + id);
            ship.setAddress2("Street Address Line 2 " + id);
            ship.setCity( "City " + id);
            ship.setState( "State " + id);
            ship.setZip( "12345");
            Address bill = new Address();
            bill.setFirstName("Bill FirstName "+ id);
            bill.setLastName("Bill LastName " + id);
            bill.setAddress1( "Bill StreetAddres " + id);
            bill.setAddress2("Street Address Line 2 " + id);
            bill.setCity( "City " + id);
            bill.setState( "State " + id);
            bill.setZip( "12345");
            Customer customer = new Customer();
            customer.setCustomerId(customerId) ;
            customer.setContactFirstName("FirstName " + id);
            customer.setContactLastName( "LastName " + id);
            customer.setContactPhone(Integer.toString(id));
            DatatypeFactory df ;
            try {
                df = DatatypeFactory.newInstance();
            } catch (javax.xml.datatype.DatatypeConfigurationException ex){
                throw new RuntimeException(ex);
            }
            
            XMLGregorianCalendar date = df.newXMLGregorianCalendar();
            date.setYear(2005);
            date.setMonth(DatatypeConstants.MARCH);
            date.setDay(29);
            date.setTime(11,11,11);
            
            customer.setLastActivityDate(date) ;
            customer.setCreditCardNumber(""+id);
            customer.setCreditCardExpirationDate( ""+id) ;
            customer.setBillingAddress(bill) ;
            customer.setShippingAddress(ship) ;            
            
            ArrayOfLineItem linearray = new ArrayOfLineItem();
            List<LineItem> lines = linearray.getLineItem();
            
            for (int i = 0; i < size; i++){
                LineItem line = new LineItem();
                line.setOrderId(orderId);
                line.setItemId(i+1);
                line.setProductId(i);
                line.setProductDescription("Test Product " +i);
                line.setOrderQuantity(1);
                line.setUnitPrice((float) 1.00);
                lines.add(line);
            }
            
            order = new Order();
            order.setOrderId(orderId);
            order.setOrderStatus(1);
            order.setOrderDate(date);
            order.setOrderTotalAmount((float) 50);
            order.setCustomer(customer);
            order.setLineItems(linearray);
        }

        public void run() throws Exception {
            final EchoOrderRequest request= new EchoOrderRequest();
            request.setEchoOrderRequest(order);
            decodeOrder(testRef.echoOrder(request).getEchoOrderResponse());
        }
    }

    /*
    public static class IntCopyTest implements SingleTest {
        private int value = 0 ;

        public String description() {
            return "IntCopy" ;
        }

        public void prepare( int size ) {
            // nop
        }

        public void run() throws Exception {
            value += (Integer)copy( value + 1 ) ;
        }
    }

    public static class DataCopyTest implements SingleTest {
        private final List<SampleData.Data> value ;

        public DataCopyTest() {
            final SampleData sd = new SampleData() ;
            value = sd.parse( SampleData.bankData ) ;
        }

        public String description() {
            return "IntCopy" ;
        }

        public void prepare( int size ) {
            // nop
        }

        public void run() throws Exception {
            Object data = copy( value ) ;
        }
    }
    */

    @Target( ElementType.METHOD ) 
    @Retention( RetentionPolicy.RUNTIME )
    public @interface TestClass {
        Class<?> value() ;
    }

    public interface Test extends Remote {
        @TestClass( EchoTest.class ) 
        public int echo( int value ) throws RemoteException ;
        
        @TestClass( GetDataTest.class ) 
        public List<SampleData.Data> getData() throws RemoteException ;

        @TestClass( GetDataArrayTest.class ) 
        public SampleData.Data[] getDataArray() throws RemoteException ;

        @TestClass( GetTestRefsTest.class ) 
        public List<Test> getTestRefs() throws RemoteException ;

        public void done() throws RemoteException ;

        // The following methods are taken from the wspex test.
        @TestClass( EchoVoidTest.class ) 
        void echoVoid() throws RemoteException ;

        @TestClass( EchoStringTest.class ) 
        String echoString(String value) throws RemoteException ;
        
        @TestClass( EchoStructTest.class ) 
        Struct echoStruct(Struct parameters) throws RemoteException ;
        
        @TestClass( EchoSyntheticTest.class ) 
        Synthetic echoSynthetic(Synthetic s) throws RemoteException ;
        
        @TestClass( EchoArrayTest.class ) 
        ItemArrayType echoArray(ItemArrayType items) throws RemoteException ;
        
        @TestClass( GetOrderTest.class ) 
        Order getOrder(int orderId, int customerId) throws RemoteException ;
               
        @TestClass( EchoOrderTest.class ) 
        EchoOrderResponse echoOrder(EchoOrderRequest or) throws RemoteException ;
    }

    /** Class that scans an interface looking for @TestClass annotationed methods.
     * It provides APIs for getting the list of annotated methods,
     * and mapping a method name to an instance of SingleTest.
     */
    private class TestMapper {
        private Map<String,Class<?>> mmap ;

        public TestMapper( Class<?> cls ) {
            if (!cls.isInterface()) {
                throw new IllegalArgumentException("TestMapper requires an interface, not a class");
            }

            mmap = new HashMap<String,Class<?>>() ;

            for (Method m : cls.getDeclaredMethods()) {
                TestClass tc = m.getAnnotation( TestClass.class ) ;
                if (tc != null) {
                    Class<?> tclass = tc.value() ;
                    mmap.put( m.getName(), tclass ) ;
                }
            }
        }

        Set<String> getValidNames() {
            return mmap.keySet() ;
        }

        SingleTest getSingleTest( String name, Object... args ) {
            Class<?> cls = mmap.get( name ) ;
            if (cls == null) {
                return null ;
            }

            GenericClass<SingleTest> gclass = 
                new GenericClass<SingleTest>( SingleTest.class, cls ) ;
            return gclass.create( args ) ;
        }
    }

    public static class TestImpl extends PortableRemoteObject implements Test {
        SampleData sd = new SampleData() ;
        List<SampleData.Data> data = sd.parse( SampleData.bankData ) ;
        SampleData.Data[] dataArray = null ;
        List<Test> testRefList = null ;

        public TestImpl() throws RemoteException {
            super() ;
            dataArray = new SampleData.Data[data.size()] ;
            int ctr=0 ;
            for (SampleData.Data d : data) {
                dataArray[ctr++] = d;
            }
        }

        public int echo( int value ) throws RemoteException {
            return value ;
        }
        
        public List<SampleData.Data> getData() throws RemoteException {
            return data ;
        }

        public SampleData.Data[] getDataArray() throws RemoteException {
            return dataArray ;
        }

        public List<Test> getTestRefs() throws RemoteException {
            return testRefList ;
        }

        public void echoVoid() {
        }

        public String echoString(String value) {
            return value;
        }
        
        public Struct echoStruct(Struct parameters) {
            return parameters;
        }
        
        public Synthetic echoSynthetic(Synthetic s) {
            return s;
        }
        
        public ItemArrayType echoArray(ItemArrayType items) {
            return items;
        }
        
        public Order getOrder(int orderId, int customerId) {        
            OrderBL bl = new OrderBL();
            return bl.GetOrder(orderId, customerId);
        }
               
        public EchoOrderResponse echoOrder(EchoOrderRequest or){
            Order order = or.getEchoOrderRequest();
            EchoOrderResponse res = new EchoOrderResponse();
            res.setEchoOrderResponse(order);
            return res;
        }

        public void done() throws RemoteException {
            System.exit(0) ;
        }

        public void setRef( Test test ) {
            testRefList = new ArrayList<Test>() ;
            for (int ctr=0; ctr<data.size(); ctr++) {
                testRefList.add(test);
            }
        }
    }

    public class TestServantLocator extends LocalObject
        implements ServantLocator {
        private static final long serialVersionUID = -5947392338870158530L;
        private Servant servant ;
        private TestImpl impl = null; ;

        public TestServantLocator( ORB orb ) {
            try {
                impl = new TestImpl() ;
            } catch (Exception exc) {
                fatal( "Exception in creating servant: " + exc, exc ) ;
            }

            Tie tie = ORB.getPresentationManager().getTie() ;
            tie.setTarget( impl ) ;
            servant = Servant.class.cast( tie ) ;
        }

        public synchronized Servant preinvoke( byte[] oid, POA adapter,
            String operation, CookieHolder the_cookie 
        ) throws ForwardRequest {
            return servant ;
        }

        public void postinvoke( byte[] oid, POA adapter,
            String operation, Object the_cookie, Servant the_servant ) {
        }

        public void setRef( Test test ) {
            impl.setRef( test ) ;
        }
    }

    private void bindName( NamingContext ctx, String sname,
        org.omg.CORBA.Object objref )
        throws NotFound, CannotProceed, AlreadyBound, InvalidName 
    {
        NameComponent[] name = serverNamingRoot.to_name( sname ) ;
        NamingContext current = ctx ;
        for (int ctr=0; ctr<name.length; ctr++) {
            NameComponent[] arr = new NameComponent[] { name[ctr] } ;

            if (ctr < name.length - 1) {
                try {
                    org.omg.CORBA.Object ref = current.resolve( arr ) ;
                    if (ref._is_a(NamingContextHelper.id())) {
                        current = NamingContextHelper.narrow(ref);
                    } else {
                        throw new BAD_OPERATION("Name is bound to a non-context object reference");
                    }
                } catch (NotFound exc) {
                    current = current.bind_new_context( arr ) ;
                }
            } else {
                current.bind( arr, objref ) ; 
            }
        }
    }
  /* 
    private static Object copy( Object obj ) throws ReflectiveCopyException {
        ObjectCopierFactory ocf = 
            CopyobjectDefaults.makeORBStreamObjectCopierFactory( clientORB ) ;
        ObjectCopier oc = ocf.make() ;
        return oc.copy( obj ) ;
    }
*/
    private Properties getBaseProps() {
        // initializer client and server ORBs.
        // Initialize server using RFM and register objrefs in naming
        Properties baseProps = new Properties() ;
        // baseProps.setProperty( ORBConstants.DEBUG_PROPERTY, "cdrCache" ) ;
        baseProps.setProperty( "org.omg.CORBA.ORBSingletonClass",
            "com.sun.corba.ee.impl.orb.ORBSingleton" ) ;
        baseProps.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
        baseProps.setProperty( ORBConstants.INITIAL_HOST_PROPERTY,
            argData.hostName() ) ;
        baseProps.setProperty( ORBConstants.INITIAL_PORT_PROPERTY,
            Integer.toString( argData.port() + instance ) ) ;
        baseProps.setProperty( ORBConstants.GIOP_FRAGMENT_SIZE,
            Integer.toString( argData.fragmentSize() ) ) ;
        baseProps.setProperty( ORBConstants.USE_NIO_SELECT_TO_WAIT_PROPERTY,
            Boolean.toString( !argData.blocking() ) ) ;
    
        // baseProps.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
            // "true" ) ;
        // For debugging only
        // baseProps.setProperty( ORBConstants.DEBUG_PROPERTY,
            // "transport,subcontract" ) ;
        
        return baseProps ;
    }

    // Override this method to create test cases with different ORB properties
    protected Properties getClientProperties() {
        Properties baseProps = getBaseProps() ;
        Properties clientProps = new Properties( baseProps ) ;
        clientProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
            "clientORB" ) ;
        return clientProps ;
    }

    // Override this method to create test cases with different ORB properties
    protected Properties getServerProperties() {
        Properties baseProps = getBaseProps() ;
        Properties serverProps = new Properties( baseProps ) ;
        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
            Integer.toString( argData.port() + instance ) ) ;
        serverProps.setProperty( ORBConstants.SERVER_HOST_PROPERTY,
            argData.hostName() ) ;
        serverProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
            "serverORB" ) ;
        serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY,
            "300" ) ;
        serverProps.setProperty( ORBConstants.RFM_PROPERTY,
            "1" ) ;
        return serverProps ;
    }

    private void setDebugFlags( ORB orb ) {
        if (argData.getSize()) {
            orb.setDebugFlag( "giopSize" ) ;
        }
        if (argData.getRead()) {
            orb.setDebugFlag( "giopRead" ) ;
        }
    }

    // Test must call this method for initialization
    protected synchronized void initializeClientORB() {
        if (clientORB != null) {
            return;
        }

        try {
            String[] myArgs = {} ;

            Properties clientProps = getClientProperties() ;
            try {
                clientORB = (ORB)ORB.init( myArgs, clientProps ) ;
                        setDebugFlags( clientORB ) ;
            } catch (Throwable thr) {
                thr.printStackTrace();
                throw new RuntimeException( "Error in ORB.init", thr ) ;
            }
            if (argData.getSize()) {
                clientORB.setDebugFlag( "giopSize" ) ;
            }
            clientNamingRoot = NamingContextExtHelper.narrow(
                clientORB.resolve_initial_references( "NameService" )) ;
        } catch (Exception exc) {
            fatal( "Exception in client initialization: " + exc, exc ) ;
        }
    }

    // Test must call this method to clean up the ORBs used in the test
    private void cleanUpClient() {
        log( "Shutting down clientORB" ) ;
        clientORB.shutdown( true ) ;
        log( "Destroying clientORB" ) ;
        clientORB.destroy() ;
    }

    private final static String JAPEX_OBJREF_NAME = "testref/cache" ;

    private static Object[][] objrefData = {
        { JAPEX_OBJREF_NAME, true }
        // { "testref/nocache", false }
    } ;
   
    protected synchronized void initializeServer() {
        String[] myArgs = {} ;

        if (serverORB != null) {
            return;
        }

        Properties serverProps = getServerProperties() ;
        try {
            serverORB = (ORB)ORB.init( myArgs, serverProps ) ;
        } catch (Throwable thr) {
            thr.printStackTrace();
            throw new RuntimeException( "Error in ORB.init", thr ) ;
        }

        setDebugFlags( serverORB ) ;
        if (argData.getSize()) {
            serverORB.setDebugFlag( "giopSize" ) ;
        }
        new TransientNameService( serverORB ) ;
        
        // Get the RFM and naming service
        ReferenceFactoryManager rfm = null ;

        try {
            rfm = ReferenceFactoryManager.class.cast( 
                serverORB.resolve_initial_references( "ReferenceFactoryManager" )) ;
            rfm.activate() ;
            serverNamingRoot = NamingContextExtHelper.narrow(
                serverORB.resolve_initial_references( "NameService" )) ;
        } catch (Exception exc) {
            fatal( "Exception in getting initial references: " + exc, exc ) ;
        }

        TestServantLocator locator = new TestServantLocator( serverORB ) ;
        PresentationManager pm = ORB.getPresentationManager() ;

        String repositoryId ;
        try {
            repositoryId = pm.getRepositoryId( new TestImpl() ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }

        String nocacheFactoryName = "nocache" ;
        String cacheFactoryName = "cache" ;

        List<Policy> nocacheFactoryPolicies = null ;
        ReferenceFactory nocacheFactory = rfm.create( nocacheFactoryName,
            repositoryId, nocacheFactoryPolicies, locator ) ;

        List<Policy> cacheFactoryPolicies = Arrays.asList( 
            (Policy)ServantCachingPolicy.getPolicy() ) ;
        ReferenceFactory cacheFactory = rfm.create( cacheFactoryName, 
            repositoryId, cacheFactoryPolicies, locator ) ;

        // Use a ReferenceFactory to create objref and register it with naming
        for ( Object[] data : objrefData) {
            String sname = String.class.cast( data[0] ) ;
            boolean useCaching = Boolean.class.cast( data[1] ) ;
            ReferenceFactory factory = useCaching ? cacheFactory : nocacheFactory ;
            byte[] oid = new byte[] { (byte)0, (byte)1, (byte)2 } ;
            org.omg.CORBA.Object objref = factory.createReference( oid ) ; 
            Test ref = Test.class.cast( javax.rmi.PortableRemoteObject.narrow( objref,
                Test.class )) ;
            locator.setRef( ref ) ;
            try {
                bindName( serverNamingRoot, sname, objref ) ;
            } catch (Exception exc) {
                fatal( "Error in initializeServer: " + exc, exc ) ;
            }
        }
    }

    public void serverWait() {
        serverORB.run() ;
    }

    private void cleanUpServer() {
        log( "Shutting down serverORB" ) ;
        serverORB.shutdown( true ) ;
        log( "Destroying serverORB" ) ;
        serverORB.destroy() ;
    }

    private Test getTestRef( String name ) {
        try {
            org.omg.CORBA.Object obj = clientNamingRoot.resolve_str( name ) ;
            Test ref = (Test)javax.rmi.PortableRemoteObject.narrow( obj, Test.class ) ;
            return ref ;
        } catch (Exception exc) {
            fatal( "Could not get object reference: " + exc, exc ) ;
        }
        return null ; // not reachable
    }

    // Code for command-line testing
    //
    //
    private void performSingleTest( SingleTest test ) {
        try {
            final int warmup = argData.warmup() ;
            for (int ctr = 0; ctr < warmup; ctr ++ ) {
                test.run() ;
            }

            final long time = System.nanoTime() ;

            final int count = argData.count() ;
            for (int ctr = 0; ctr < count; ctr++) {
                test.run();
            }
                
            final double elapsed = System.nanoTime() - time ;

            log( test.description() + " : " + (elapsed/count)/1000 
                + " microseconds" ) ;
        } catch (Exception rex) {
            fatal( test.description() + ": error in test: ", rex ) ;
            rex.printStackTrace() ;
        }
    }

    /*
    public void testNullStreamCopy() {
        SingleTest st = new IntCopyTest() ;
        performSingleTest( st ) ;
    }
    
    public void testDataStreamCopy() {
        SingleTest st = new DataCopyTest() ;
        performSingleTest( st ) ;
    }
    */

    @org.glassfish.pfl.test.TestCase( "null" )
    public void testNullCall() {
        for ( Object[] data : objrefData) {
            String oname = (String)data[0] ;
            Test test = getTestRef( oname ) ;
            SingleTest st = new EchoTest( test ) ;
            performSingleTest( st ) ;
        }
    }
   
    @org.glassfish.pfl.test.TestCase( "data" )
    public void testDataCall() {
        for ( Object[] data : objrefData) {
            String oname = (String)data[0] ;
            Test test = getTestRef( oname ) ;
            SingleTest st = new GetDataTest( test ) ;
            performSingleTest( st ) ;
        }
    }
    
    @org.glassfish.pfl.test.TestCase( "data-array" )
    public void testDataArrayCall() {
        for ( Object[] data : objrefData) {
            String oname = (String)data[0] ;
            Test test = getTestRef( oname ) ;
            SingleTest st = new GetDataArrayTest( test ) ;
            performSingleTest( st ) ;
        }
    }
    
    @org.glassfish.pfl.test.TestCase( "get-test-refs" )
    public void testGetTestRefsCall() {
        for ( Object[] data : objrefData) {
            String oname = (String)data[0] ;
            Test test = getTestRef( oname ) ;
            SingleTest st = new GetTestRefsTest( test ) ;
            performSingleTest( st ) ;
        }
    }

    public void standaloneRun() {
        switch (argData.mode()) {
            case CLIENT :
                runClient() ;
                // st.stopServer() ;
                System.exit(0) ;
                break ;
            case SERVER :
                initializeServer() ;
                serverWait() ;
                break ;
            case LOCAL :
                // Colocated test:
                initializeServer() ;
                runClient() ;
                cleanUpServer() ;
                System.exit(0) ;
                break ;
        }
    }

    public class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                initializeClientORB() ;
                testBase.run() ;
                cleanUpClient() ;
                log( "Test Complete." ) ;
            } catch (Throwable thr) {
                fatal( "Test FAILED: Caught throwable " + thr, thr ) ;
            }
        }
    }

    public void runClient() {
        final List<ClientThread> clients = new ArrayList<ClientThread>() ;
        for (int ctr=0; ctr<argData.numThreads(); ctr++) {
            ClientThread ct = new ClientThread() ;
            ct.start() ;
            clients.add( ct ) ;
        }

        for( ClientThread ct : clients) {
            boolean interrupted = false ;
            do {
                try {
                    ct.join();
                } catch (InterruptedException ex) {
                    interrupted = true ;
                }
            } while (interrupted) ;
        }
    }

    //
    //
    // End of code for command line testing

    // JapexDriver methods
    //
    // data only used in JapexDriver methods
    private SingleTest testToRun = null ;
    private Test testRef = null ;

    @Override
    public void initializeDriver() {
        if (!useSingleClassLoader) {
            reinitCorbaDelegateHack();
        }

        String fragmentSize ;
        if (hasParam( "fragmentSize" )) {
            fragmentSize = getParam("fragmentSize");
        } else {
            fragmentSize = "4096";
        }

        String port ;
        if (hasParam( "port" )) {
            port = getParam("port");
        } else {
            port = "3700";
        }

        String[] args = { "-fragmentSize", fragmentSize, "-port", port } ;

        argData = ap.parse( args, ArgumentData.class ) ;

        log( "Calling initializeDriver with fragmentSize = " + fragmentSize ) ;

        if (argData.mode() == TestMode.LOCAL) {
            initializeServer() ;
        }
        initializeClientORB() ;

        testRef = getTestRef( JAPEX_OBJREF_NAME ) ;
        System.out.println( "My ClassLoader = " + this.getClass().getClassLoader() ) ;
    }

    @Override
    public void prepare( TestCase testCase ) {
        log( "Calling prepare" ) ;
        // find the SingleTest for this testCase and prepare it
        String methodName = testCase.getParam( "methodName" ) ;
        int size = testCase.getIntParam( "size" ) ;
        TestMapper tm = new TestMapper( Test.class ) ;
        testToRun = tm.getSingleTest( methodName, testRef ) ;
        testToRun.prepare( size ) ;
    }

    @Override
    public void warmup( TestCase testCase ) {
        // log( "Calling warmup" ) ;
        // no difference between warmup and run for us.
        run( testCase ) ;
    }

    @Override
    public void run( TestCase testCase ) {
        // log( "Calling run" ) ;
        // run the test method
        try {
            testToRun.run() ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    @Override
    public void finish( TestCase testCase ) {
        log( "Calling finish" ) ;
        // set japex.resultValue and japex.resultType here if necessary
    }

    @Override
    public void terminateDriver() {
        log( "Calling terminateDriver" ) ;
        cleanUpClient() ;
        if (argData.mode() == TestMode.LOCAL) {
            cleanUpServer() ;
        }
        // clearCorbaDelegateHack() ;
    }
    //
    //
    // end of JapexDriver methods

    public enum TestMode { LOCAL, CLIENT, SERVER, JAPEX } 

    public interface ArgumentData {
        @DefaultValue( "LOCAL" ) 
        TestMode mode() ;

        @DefaultValue( "3700" ) 
        int port() ;

        /* Default in server is to listen for all incoming requests. */
        @DefaultValue( "" ) 
        String hostName() ;

        @DefaultValue( "4096" ) 
        int fragmentSize() ;

        @DefaultValue( "false" ) 
        boolean blocking() ;

        @DefaultValue( "500" ) 
        int warmup() ;

        @DefaultValue( "500" ) 
        int count() ;

        /*
        @DefaultValue( "true" )
        boolean doCopyTests() ;
        */

        @DefaultValue( "false" ) 
        boolean getSize() ;

        @DefaultValue( "false" ) 
        boolean getRead() ;

        @DefaultValue( "false" ) 
        boolean useJapex() ;

        @DefaultValue( "1" )
        int numThreads() ;
    }

    /** By default, run client and server co-located.
     * Optional args:
     * <ul>
     * <li>-mode [local client server]: Run as a client only on the given host name.
     * which must be a name of the host running the program.
     * </ul>
     * @param args args for command
     */
    public static void main( String[] args ) {
        // override default argData
        StandardTest st = new StandardTest()  ;
        testBase = new TestBase(args, ArgumentData.class, st ) ;
        argData = testBase.getArguments( ArgumentData.class ) ;

        if (argData.mode() == TestMode.SERVER) {
            if (argData.useJapex()) {
                throw new IllegalArgumentException(
                    "This test does not run with Japex in server mode!" ) ;
            }
        }

        System.out.println( "Running StandardTest with arguments" ) ;
        System.out.println( argData ) ;
        System.out.println( "-----------------------------------------------" ) ;

        if (argData.useJapex()) {
            System.out.println( "Running with japex" ) ;
            String[] jargs = { "test.xml" } ;

            // This will re-use the static argData when it creates a new StandardTest
            // as a Japex driver.
            com.sun.japex.Japex.main( jargs ) ;
        } else {
            System.out.println( "Running standalone" ) ;
            st.standaloneRun() ;
        }
    }
}
