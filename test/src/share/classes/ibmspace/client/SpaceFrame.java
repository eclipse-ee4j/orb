/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// This is the main window.   It creates and lays out all the controls
// and views that are its contents.

package ibmspace.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SpaceFrame extends JFrame
{
    GameUI fGameUI = null;


    public SpaceFrame(String username)
    {
        fGameUI = new GameUI (this, new GameSurrogate(username));

        getContentPane().setLayout (new PercentLayout(PercentLayout.HORZ));
        setSize(new Dimension(762, 591));
        setTitle("IBM Space Conquest! RMI-IIOP Demo (" + username + ")");

        // Menu Bar

        MenuBar menuBar = new MenuBar ();
        menuBar.add (fGameUI.createBuildMenu());
        menuBar.add (fGameUI.createDoMenu());
        setMenuBar(menuBar);


        //
        // Create UI Components
        //

        JPanel controlPanel = new JPanel ();
        controlPanel.setLayout (new PercentLayout(PercentLayout.VERT));
        getContentPane().add (controlPanel, new Float(.35f));


        // Planet Stats Pane

        JPanel PlanetStatsPanel = new JPanel();
        PlanetStatsPanel.setLayout(new BorderLayout());
        JComponent planetStatsUI = fGameUI.createPlanetStatsUI ();
        PlanetStatsPanel.add(planetStatsUI, BorderLayout.CENTER);


        // Planet Resource Control Pane

        JPanel PlanetResourceControl = new JPanel();
        PlanetResourceControl.setBackground(Color.black);
        PlanetResourceControl.setLayout(new BorderLayout());
        PieControl pie = fGameUI.createPlanetSpendingUI ();
        pie.setSecondPercentage(0.3);
        pie.setFirstPercentage(20.0);
        pie.setBorder (new EmptyBorder(5,5,5,5));
        PlanetResourceControl.add(pie, BorderLayout.CENTER);


        // Planet Group

        JPanel planetGroup = new JPanel();
        planetGroup.setLayout (new PercentLayout(PercentLayout.HORZ));
        planetGroup.setBorder (new EtchedBorder());
        planetGroup.add(PlanetStatsPanel, new Float(.5f));
        planetGroup.add(PlanetResourceControl, new Float(.5f));
        controlPanel.add(planetGroup, new Float(.2f));


        // Turn Button

        JButton turnButton = fGameUI.createTurnButton ();
        turnButton.addActionListener(new SpaceFrame_miTakeTurn_actionAdapter(this));
        controlPanel.add(turnButton, new Float(.07f));


        // Ship List Pane

        JComponent shipListUI = fGameUI.createShipListUI ();
        controlPanel.add(shipListUI, new Float(.13f));


        // Resources Pane

        JComponent resourceLevelsUI = fGameUI.createResourceLevelsUI ();
        resourceLevelsUI.setBorder (new EtchedBorder());
        controlPanel.add (resourceLevelsUI, new Float(.1f));


        // Budget Pane

        JComponent budgetUI = fGameUI.createBudgetUI ();
        JScrollPane budgetScroller = new JScrollPane ();
        budgetScroller.getViewport().add(budgetUI, null);
        controlPanel.add(budgetScroller, new Float(.3f));

    
        // Tech Spending Pane

        JComponent techSpendingUI = fGameUI.createTechSpendingUI ();
        JScrollPane techSpendingScroller = new JScrollPane ();
        techSpendingScroller.getViewport().add (techSpendingUI, null);
        controlPanel.add (techSpendingScroller, new Float(.2f));


        // Space Pane

        JScrollPane galaxyScroller = new JScrollPane();
        galaxyScroller.setPreferredSize(new Dimension(300, 300));
        galaxyScroller.setMinimumSize(new Dimension(300, 300));
        GalaxyView galaxyView = fGameUI.createGalaxyUI ();
        galaxyScroller.getViewport().add(galaxyView, null);
        getContentPane().add (galaxyScroller, new Float(.65f));

        fGameUI.init ();

    }

    public void fileExit_actionPerformed(ActionEvent e)
    {
        System.exit(0);
    }


    void miTakeTurn_actionPerformed(ActionEvent e)
    {
        System.out.println ("Taking turn");
        fGameUI.takeTurn ();
    }

}



class SpaceFrame_miTakeTurn_actionAdapter implements java.awt.event.ActionListener
{
    SpaceFrame adaptee;

  
    SpaceFrame_miTakeTurn_actionAdapter(SpaceFrame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.miTakeTurn_actionPerformed(e);
    }
}
