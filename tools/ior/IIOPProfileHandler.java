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
