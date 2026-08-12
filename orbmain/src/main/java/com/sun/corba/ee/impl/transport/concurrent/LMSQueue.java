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

/** Java implementation of the Lock-Free FIFO queue in Ladan-Mozes and Shavit,
 * "An Optimistic Approach to Lock-Free FIFO Queues".
 */
package com.sun.corba.ee.impl.transport.concurrent;

import java.util.concurrent.atomic.AtomicReference;

public class LMSQueue<V> {
    static private class Node<V> {
        private V value;
        Node<V> next;
        Node<V> prev;

        public Node(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }
    }

    private AtomicReference<Node<V>> head;
    private AtomicReference<Node<V>> tail;

    public final Node<V> dummyNode = new Node<V>(null);

    public void enqueue(V val) {
        if (val == null)
            throw new IllegalArgumentException("Cannot enqueue null value");

        Node<V> tl;
        Node<V> nd = new Node<V>(val);
        while (true) {
            tl = tail.get();
            nd.next = tl;
            if (tail.compareAndSet(tl, nd)) {
                tail.get().prev = nd;
                break;
            }
        }
    }

    public V dequeue() {
        Node<V> tl;
        Node<V> hd;
        Node<V> firstNodePrev;
        Node<V> ndDummy;
        V val;

        while (true) { // D04
            hd = head.get(); // D05
            tl = tail.get(); // D06
            firstNodePrev = hd.prev; // D07
            val = hd.getValue(); // D08
            if (hd == head.get()) { // D09
                if (val != null) { // D10
                    if (tl != hd) { // D11
                        if (firstNodePrev == null) { // D12
                            fixList(tl, hd); // D13
                            continue; // D14
                        } // D15
                    } else { // D16,D17
                        ndDummy = new Node<V>(null); // D18,D19
                        ndDummy.next = tl; // D20
                        if (tail.compareAndSet(tl, ndDummy)) { // D21
                            hd.prev = ndDummy; // D22
                        } else { // D23,D24
                            ndDummy = null; // D25
                        } // D26
                        continue; // D27
                    } // D28
                    if (head.compareAndSet(hd, firstNodePrev)) { // D29
                        hd = null; // D30
                        return val; // D31
                    } // D32
                } else { // D33,D34
                    if (tail == head) { // D35
                        return null; // D36
                    } else { // D37,D38
                        if (firstNodePrev == null) { // D39
                            fixList(tl, hd); // D40
                            continue; // D41
                        } // D42
                        head.compareAndSet(hd, firstNodePrev); // D43
                    }
                }
            }
        }
    }

    private void fixList(Node<V> tl, Node<V> hd) {
        Node<V> curNode = tl;
        Node<V> curNodeNext = null;
        Node<V> nextNodePrev = null;

        while ((hd == head.get()) && (curNode != head.get())) {
            curNodeNext = curNode.next;
            if (curNodeNext == null) {
                return;
            }
            nextNodePrev = curNodeNext.prev;
            if (nextNodePrev != curNode) {
                curNodeNext.prev = curNode;
            }
            curNode = curNodeNext;
        }
    }
}
