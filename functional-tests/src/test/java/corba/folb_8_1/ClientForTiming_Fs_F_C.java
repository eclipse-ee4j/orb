/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Apr 27 (Wed) 15:46:01 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:01:14 by Harold Carr.
//

package corba.folb_8_1;

/**
 * @author Harold Carr
 */
public class ClientForTiming_Fs_F_C
{
    public static void main(String[] av)
    {
        String[] args = { Common.FAILOVER_SUPPORT, Common.FAILOVER, Common.CACHE };
        ClientForTiming.main(Common.concat(av, args));
    }
}

// End of file.
