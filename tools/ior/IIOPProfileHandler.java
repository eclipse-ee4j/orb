/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package tools.ior;

import java.io.IOException;
import org.omg.CORBA.portable.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;
import org.omg.CORBA.Any;

/**
 * Handles the IIOP Profile.  Correctly recognizes 1.0 and 1.1
 * versions, delegating to the appropriate ProfileBody helper.
 *
 * This is necessary since we have to decide what to do
 * depending on the Version at the beginning.
 */
public class IIOPProfileHandler implements EncapsHandler
{
    public void display(byte[] data, 
                        TextOutputHandler out,
                        Utility util)
        throws DecodingException {

        try {

            // Assumes that all IIOPProfiles contain only GIOP 1.0
            // primitives.
            Codec codec = util.getCDREncapsCodec(Utility.GIOP_1_0);

            // Check the version of the profile
            Any versionAny = codec.decode_value(data,
                                                VersionHelper.type());

            Version version = VersionHelper.extract(versionAny);

            // This assumes that the profile will change
            // after GIOP 1.2.  Currently this is handled
            // since the caller dumps the data when getting
            // a DecodingException.
            if (version.major != 1 || version.minor > 2)
                throw new DecodingException("Unknown IIOP Profile version: "
                                            + version.major
                                            + '.'
                                            + version.minor);

            if (version.minor == 0) {
                Any bodyAny = codec.decode_value(data,
                                                 ProfileBody_1_0Helper.type());
                java.lang.Object body
                    = ProfileBody_1_0Helper.extract(bodyAny);

                util.recursiveDisplay("ProfileBody_1_0", body, out);

            } else {
                // GIOP 1.1 and 1.2 use the same
                // profile body
                Any bodyAny = codec.decode_value(data,
                                                 ProfileBody_1_1Helper.type());

                java.lang.Object body
                    = ProfileBody_1_1Helper.extract(bodyAny);

                util.recursiveDisplay("ProfileBody_1_1", body, out);
            }
        } catch (Exception ex) {
            throw new DecodingException(ex.getMessage());
        }
    }
}
