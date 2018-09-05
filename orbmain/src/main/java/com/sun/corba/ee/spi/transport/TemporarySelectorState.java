/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 *
 * @author Charlie Hunt
 */

/**
 *
 * An interface which models the state and transitions of a temporary Selector.
 *
 */
public interface TemporarySelectorState {
    /**
     * Selects a set of keys whose corresponding SelectableChannel is ready for
     * I/O operations.
     *
     * <p> This method performs a blocking <a href="#selop">selection
     * operation</a> on theSelector.  It returns only after the
     * SelectableChannel is selected, theSelector's wakeup
     * method is invoked, the current thread is interrupted, or the given
     * timeout period expires, whichever comes first.
     *
     * <p> This method does not offer real-time guarantees: It schedules
     * theTimeout as if by invoking the {@link Object#wait(long)} method. </p>
     *
     * @param  theTimeout  If positive, block for up to <tt>theTimeout</tt>
     *                     milliseconds, more or less, while waiting for a
     *                     SelectableChannel to become ready; must be greater
     *                     than 0 in value
     *
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @throws  ClosedSelectorException
     *          If this theSelector is closed
     *
     * @throws  IllegalArgumentException
     *          If the value of the theTimeout argument is not greater than 0
     *
     * @return  The number of keys, possibly zero, whose ready-operation sets
     *          was updated.
     */
    public int select(Selector theSelector, long theTimeout) throws IOException;

   /**
     * Registers theSelectableChannel with theSelector, setting theSelection to
     * the key returned by the registeration.
     * 
     * @param  theSelector
     *         The selector with which this channel is to be registered
     *
     * @param  theSelectableChannel
     *         The SelectableChannel to register theSelector with.
     *
     * @param  theOps
     *         The interest set for the resulting key
     *
     * @throws  ClosedChannelException
     *          If theSelectableChannel is closed
     *
     * @throws  IllegalBlockingModeException
     *          If theSelectableChannel is in blocking mode
     *
     * @throws  IllegalSelectorException
     *          If thSelectableChannel was not created by the same provider
     *          as theSelector
     *
     * @throws  CancelledKeyException
     *          If theSelectableChannel is currently registered with theSelector
     *          but the corresponding key has already been cancelled
     *
     * @throws  IllegalArgumentException
     *          If a bit in <tt>theOps</tt> does not correspond to an operation
     *          that is supported by theSelectableChannel, that is, if <tt>set &
     *          ~theSeletableChannel.validOps() != 0</tt>
     *
     * @return  A key representing the registration of theSelectableChannel with
     *         theSelector.
     */
    public SelectionKey registerChannel(Selector theSelector,
                                        SelectableChannel theSelectableChannel,
                                        int theOps) throws IOException;

   /**
     * Requests that the registration of a SelectableChannel with theSelector, 
     * theSelectionKey be cancelled and flushed from theSelector.  Upon return
     * theSelectionKey will be invalid and will have been flushed from
     * theSelector's key sets.
     *
     * <p> If theSelectionKey has already been cancelled and it has been flushed
     * from theSelector, then invoking this method has no effect.  Once
     * theSelectionKey is cancelled and flushed, theSelectionKey remains forever
     * invalid. </p>
     *
     * <p> This method may be invoked at any time.  It synchronizes on the
     * theSelector's cancelled-key set, and therefore may block briefly if
     * invoked concurrently with a cancellation or selection operation
     * involving the same selector.  </p>
     *
     * @param  theSelector
     *         The selector with which this channel is to be registered
     *
     * @param  theSelectionKey
     *         A key representing the registration of theSelectableChannel with
     *         theSelector
     *
     * @return  TemporarySelectorState, the state of the TemporarySelector after
     *          invoking this method.
     */
    public TemporarySelectorState cancelKeyAndFlushSelector(Selector theSelector,
                                                            SelectionKey theSelectionKey)
                                                            throws IOException;

   /**
     * Closes theSelector.
     *
     * <p> If a thread is currently blocked in one of theSelector's selection
     * methods then it is interrupted as if by invoking theSelector's 
     * wakeup method.
     *
     * <p> Any uncancelled keys still associated with theSelector are
     * invalidated, their SelectableChannels are deregistered, and any other
     * resources associated with this selector are released.
     *
     * <p> If theSelector is already closed then invoking this method has no
     * effect.
     *
     * <p> After theSelector is closed, any further attempt to use it, except by
     * invoking this method, will cause a ClosedSelectorException to be
     * thrown. </p>
     *
     * @param  theSelector
     *         The selector with which this channel is to be registered
     * @throws  IOException
     *          If an I/O error occurs
     *
     * @return  TemporarySelectorState, the state of the TemporarySelector after
     *          invoking this method.
     */
    public TemporarySelectorState close(Selector theSelector) throws IOException;

   /**
     * Remove theSelectionKey from the theSelector's selected key set.
     *
     * @param  theSelector
     *         The selector whose selected key set should have theSelectionKey
     *         removed.
     *
     * @param  theSelectionKey
     *         A key representing the registration of theSelectableChannel with
     *         theSelector and the key should be removed key removed from the
     *         selected key set.
     *
     * @return  TemporarySelectorState, the state of the TemporarySelector after
     *          invoking this method.
     *
     * @throws  IOException
     *          If this selector is closed
     */
    public TemporarySelectorState removeSelectedKey(Selector theSelector,
                                                    SelectionKey theSelectionKey)
                                                    throws IOException;
}
