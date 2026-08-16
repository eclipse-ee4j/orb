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

package corba.connectioncache;

import java.util.Random;

public class RandomDelay {
    Random random = new Random();

    private final int minDelay;
    private final int maxDelay;

    public RandomDelay(int minDelay, int maxDelay) {
        if (minDelay < 0) {
            throw new RuntimeException("minDelay must be >= 0");
        }
        if (maxDelay < minDelay) {
            throw new RuntimeException("maxDelay must be >= minDelay");
        }

        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
    }

    void randomWait() {
        int delay = minDelay;
        if (maxDelay > minDelay) {
            delay = minDelay + random.nextInt(maxDelay - minDelay);
        }

        if (delay > 0) {
            try {
                wait(delay);
            } catch (InterruptedException ex) {
                // ignore this
            }
        }
    }
}
