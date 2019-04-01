/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.logex.corba;

import org.omg.CORBA.CompletionStatus;

/**
 * Enum corresponding to CompletionStatus that can be used in annotations.
 *
 * @author ken
 */
public enum CSValue {
    YES() {
        @Override
        public CompletionStatus getCompletionStatus() {
            return CompletionStatus.COMPLETED_YES;
        }
    },

    NO {
        @Override
        public CompletionStatus getCompletionStatus() {
            return CompletionStatus.COMPLETED_NO;
        }
    },

    MAYBE {
        @Override
        public CompletionStatus getCompletionStatus() {
            return CompletionStatus.COMPLETED_MAYBE;
        }
    };

    public abstract CompletionStatus getCompletionStatus();
}
