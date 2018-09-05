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

public class EchoOrderResponse implements Serializable {

    protected Order echoOrderResponse;

    /**
     * Gets the value of the echoOrderResponse property.
     * 
     * @return
     *     possible object is
     *     {@link Order }
     *     
     */
    public Order getEchoOrderResponse() {
        return echoOrderResponse;
    }

    /**
     * Sets the value of the echoOrderResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Order }
     *     
     */
    public void setEchoOrderResponse(Order value) {
        this.echoOrderResponse = value;
    }

}
