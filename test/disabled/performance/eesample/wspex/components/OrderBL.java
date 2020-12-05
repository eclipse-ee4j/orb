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

package performance.eesample.wspex.components;

import performance.eesample.wspex.*;

import java.util.*;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class OrderBL {
    
    static DatatypeFactory df;

    public OrderBL() {
    }
    
    public Order GetOrder(int orderId, int customerId) {
        int id = 1;
        
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
        
        try {
           if( df == null )
            df = DatatypeFactory.newInstance();
        }
        catch(javax.xml.datatype.DatatypeConfigurationException ex) {
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
        
        int numberLineItems = 50;
        ArrayOfLineItem linearray = new ArrayOfLineItem();
        List<LineItem> lines = linearray.getLineItem();
        
        for(int i = 0; i < numberLineItems; i++) {
            LineItem line = new LineItem();
            line.setOrderId(orderId);
            line.setItemId(i+1);
            line.setProductId(i);
            line.setProductDescription("Test Product " +i);
            line.setOrderQuantity(1);
            line.setUnitPrice((float) 1.00);
            
            lines.add(line);
        }
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderStatus( 1);
        order.setOrderDate(date);
        order.setOrderTotalAmount((float) 50);
        order.setCustomer(customer);
        order.setLineItems(linearray);        
        return order;
    }
}

