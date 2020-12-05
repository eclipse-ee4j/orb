/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package ibmspace.spacetest;

import ibmspace.common.BudgetSummary;
import ibmspace.common.GameView;
import ibmspace.common.Planet;
import ibmspace.common.PlanetView;
import ibmspace.common.SpaceConquest;
import java.rmi.Remote;
import test.ServantContext;
import test.RemoteTest;
import javax.rmi.PortableRemoteObject;

import java.lang.reflect.Array;
import org.glassfish.pfl.test.JUnitReportHelper;

public class SpaceTest extends RemoteTest {

    private static final String servantClass = "ibmspace.server.SpaceConquestServer";
    private static final String[] compileEm =
    {
        "ibmspace.server.SpaceConquestServer",
        "ibmspace.server.GameViewServer"
    };

    private static final int TIMING_ITERATIONS = 100;
 
    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
     
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */

    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-always", "-keep"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
     
    public void doTest (ServantContext context) throws Throwable {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() 
            + ( iiop ? "_iiop" : "_jrmp" ) ) ;
        
        helper.start( "spaceTest" ) ;

        try {
            // First ensure that the caches are cleared out so
            // that we can switch between IIOP and JRMP...
            
            //Utility.clearCaches();

            Remote remote = context.startServant(servantClass,"SpaceConquest",true,iiop);

            if (remote == null) {
                throw new Exception ("startServant() failed");
            }

            // Try narrow...

            SpaceConquest game = (SpaceConquest)PortableRemoteObject.narrow(remote,SpaceConquest.class);

            if (game == null) {
                throw new Exception ("narrow() failed for remote");
            }

            GameView gameView = game.joinGame ("Test");

            Planet[] planets = game.getGalaxyMap ();

            int numPlanets = Array.getLength (planets);
            PlanetView[] planetViews = new PlanetView [numPlanets];
            BudgetSummary[] planetBudgets = new BudgetSummary [numPlanets];

            for (int i=0; i<numPlanets; i++) {
                planetViews[i] = gameView.getPlanet (planets[i].getID());
                String name = planetViews[i].getName ();
                planetBudgets[i] = gameView.getPlanetBudget (planets[i].getID());
            }

            helper.pass() ;
        } catch (Throwable thr) {
            helper.fail( thr ) ;
            throw thr ;
        } finally {
            helper.done() ;
        }
    }
}
