/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package performance.eesample.wspex ;

import java.io.Serializable;

public class EchoOrderRequest implements Serializable {

    protected Order echoOrderRequest;

    /**
     * Gets the value of the echoOrderRequest property.
     * 
     * @return
     *     possible object is
     *     {@link Order }
     *     
     */
    public Order getEchoOrderRequest() {
        return echoOrderRequest;
    }

    /**
     * Sets the value of the echoOrderRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Order }
     *     
     */
    public void setEchoOrderRequest(Order value) {
        this.echoOrderRequest = value;
    }

}
