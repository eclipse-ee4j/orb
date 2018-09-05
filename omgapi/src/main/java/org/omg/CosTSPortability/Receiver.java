/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// org/omg/CosTSPortability/Receiver.java

package org.omg.CosTSPortability;

public interface Receiver {

    void received_request(int id, org.omg.CosTransactions.PropagationContext ctx);

    void sending_reply(int id, org.omg.CosTransactions.PropagationContextHolder ctxh);
}
