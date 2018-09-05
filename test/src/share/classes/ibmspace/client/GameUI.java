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

// This class grew to be too big and ugly and I'll probably split it
// up in the future.  It's purpose is to create all custom UI components
// and keep track of them so that they can be manipulated later when
// events (actions) occur.

package ibmspace.client;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import javax.swing.*;
import ibmspace.common.*;
import java.util.Vector;

public class GameUI implements ActionListener
{
    private SpaceFrame          fFrame;
    private GameSurrogate       fGame;
    private PlanetStatsView     fPlanetStatsUI;
    private GalaxyView          fGalaxyUI;

    private JButton             fTurnButton;

    private PieControl          fPlanetSpending;

    private ResourceLevelsPanel fResourceLevels;

    private BarGroup            fBudgetUI;
    private LabeledBarControl   fShipSavings;
    private LabeledBarControl   fTechSpending;


    private ShipListPanel       fShipListView;

    private TechLevelsPanel     fTechLevels;
    private BarGroup            fTechSpendingUI;
    private LabeledBarControl   fRangeSpending;
    private LabeledBarControl   fSpeedSpending;
    private LabeledBarControl   fWeaponsSpending;
    private LabeledBarControl   fShieldsSpending;
    private LabeledBarControl   fMiniSpending;

    private ID                  fSelection;


    public GameUI(SpaceFrame frame, GameSurrogate game)
    {
        fFrame = frame;
        fGame = game;
        fGame.addActionListener (this);
        fGame.joinGame ();
        fGame.updatePlanetMap ();
        fSelection = fGame.getHome ();
    }

    public void init ()
    {
        System.out.println ("GameUI initializing...");
        handleNewTurn ();
        fFrame.setEnabled (false);
        fFrame.setEnabled (true);

    }

    public synchronized void beginTurn ()
    {
        try { wait (1000); } catch (Exception e) {};
        fGame.takeTurn ();

        Vector messages = fGame.getMessages ();
        if ( messages != null ) {
            for (int i=0; i<messages.size(); i++) {
                String message = (String)messages.elementAt (i);
                if ( message != null ) {
                    JOptionPane.showMessageDialog (fFrame, message, "Turn Results",
                                                   JOptionPane.INFORMATION_MESSAGE );
                }
            }
        }

    }

    public synchronized void endTurn ()
    {
        //fFrame.setEnabled (true);
        fTurnButton.setEnabled (true);
        fTurnButton.setFont (new Font ("SansSerif", Font.BOLD, 20));
        fTurnButton.setForeground (Color.blue);
        fTurnButton.setText ("Next Turn");
    }


    public synchronized void takeTurn ()
    {
        //fFrame.setEnabled (false);
        fTurnButton.setEnabled (false);
        fTurnButton.setFont (new Font ("SansSerif", Font.ITALIC, 14));
        fTurnButton.setForeground (Color.darkGray);
        fTurnButton.setText ("Waiting for other players...");
        pushBudgetSettings ();
        TurnTaker t = new TurnTaker (this);
        t.start ();
    }

    //
    // Menu Creation
    //

    public Menu createDoMenu ()
    {
        Menu mDo = new Menu ();
        MenuItem miAbandonPlanet = new MenuItem ();
        MenuItem miScrapFleet = new MenuItem ();
        MenuItem miSurrender = new MenuItem ();
        //MenuItem miArmageddon = new MenuItem ();

        miAbandonPlanet.addActionListener(new AbandonPlanet());
        miScrapFleet.addActionListener(new ScrapFleet());
        miSurrender.addActionListener(new Surrender());

        mDo.setLabel ("Do");
        miAbandonPlanet.setLabel ("Abandon Planet");
        miScrapFleet.setLabel("Scrap Fleet");
        miSurrender.setLabel("Surrender");
        //miArmageddon.setLabel("Armageddon");

        mDo.add(miAbandonPlanet);
        mDo.add(miScrapFleet);
        mDo.add(miSurrender);
        //mDo.add(miArmageddon);

        return mDo;
    }

    public Menu createBuildMenu ()
    {
        Menu mBuild = new Menu ();
        MenuItem miColonyShip = new MenuItem ();
        MenuItem miScout = new MenuItem ();
        MenuItem miFighters = new MenuItem ();
        MenuItem miSatelites = new MenuItem ();

        mBuild.setLabel ("Build");
        miColonyShip.setLabel ("Colony Ship...");
        miColonyShip.addActionListener(new CreateShip(ShipDesign.COLONY_SHIP));
        miScout.setLabel ("Scout...");
        miScout.addActionListener(new CreateShip(ShipDesign.SCOUT));
        miFighters.setLabel ("Fighters...");
        miFighters.addActionListener(new CreateShip(ShipDesign.FIGHTER));
        miSatelites.setLabel ("Satelites...");
        miSatelites.addActionListener(new CreateShip(ShipDesign.SATELITE));
        mBuild.add (miColonyShip);
        mBuild.add (miScout);
        mBuild.add (miFighters);
        mBuild.add (miSatelites);
        return mBuild;
    }


    public PieControl createPlanetSpendingUI ()
    {
        System.out.println ("  planet spending pie control created");
        fPlanetSpending = new PieControl ();
        return fPlanetSpending;
    }

    public JButton createTurnButton ()
    {
        System.out.println ("  turn button created");
        fTurnButton = new JButton ("Take Turn");
        fTurnButton.setFont (new Font ("SansSerif", Font.BOLD, 20));
        fTurnButton.setForeground (Color.blue);
        return fTurnButton;
    }

    public JComponent createShipListUI ()
    {
        fShipListView = new ShipListPanel ();
        return fShipListView;
    }

    public BarGroup createBudgetUI ()
    {
        System.out.println ("  main budget bar controls created");
        fBudgetUI = new BarGroup ();

        fBudgetUI.setLayout(new BoxLayout(fBudgetUI, BoxLayout.Y_AXIS));

        fShipSavings = new LabeledBarControl("Ship Savings");
        fTechSpending = new LabeledBarControl("Technology");
        fShipSavings.getBarControl().setColor (Color.orange);
        fTechSpending.getBarControl().setColor (Color.magenta);

        fBudgetUI.addBar(fShipSavings);
        fBudgetUI.addBar(fTechSpending);
        return fBudgetUI;
    }

    public PlanetStatsView createPlanetStatsUI ()
    {
        System.out.println ("  planet stats ui created");
        fPlanetStatsUI = new PlanetStatsView ();
        return fPlanetStatsUI;
    }

    public JPanel createResourceLevelsUI ()
    {
        fResourceLevels = new ResourceLevelsPanel ();
        JPanel container = new JPanel ();
        container.setLayout (new BorderLayout ());
        container.add (fResourceLevels, BorderLayout.CENTER);
        return container;
    }


    public GalaxyView createGalaxyUI ()
    {
        System.out.println ("  galaxy view created");
        fGalaxyUI = new GalaxyView (this, fGame);
        fGalaxyUI.addActionListener (this);
        return fGalaxyUI;
    }


    public JPanel createTechSpendingUI ()
    {
        System.out.println ("  tech spending bar controls created");

        fTechSpendingUI = new BarGroup ();
        fTechSpendingUI.setLayout (new FlowLayout());

        fRangeSpending = new LabeledBarControl ("Range", BarControl.VERTICAL);
        fSpeedSpending = new LabeledBarControl ("Speed", BarControl.VERTICAL);
        fWeaponsSpending = new LabeledBarControl ("Weapon", BarControl.VERTICAL);
        fShieldsSpending = new LabeledBarControl ("Shield", BarControl.VERTICAL);
        fMiniSpending = new LabeledBarControl ("Mini", BarControl.VERTICAL);

        fRangeSpending.getBarControl().setColor (Color.magenta);
        fSpeedSpending.getBarControl().setColor (Color.magenta);
        fWeaponsSpending.getBarControl().setColor (Color.magenta);
        fShieldsSpending.getBarControl().setColor (Color.magenta);
        fMiniSpending.getBarControl().setColor (Color.magenta);

        fTechSpendingUI.addBar (fRangeSpending);
        fTechSpendingUI.addBar (fSpeedSpending);
        fTechSpendingUI.addBar (fWeaponsSpending);
        fTechSpendingUI.addBar (fShieldsSpending);
        fTechSpendingUI.addBar (fMiniSpending);

        JPanel ui = new JPanel ();

        fTechLevels = new TechLevelsPanel ();
        fTechLevels.setRange (6);
        fTechLevels.setSpeed (2);
        fTechLevels.setWeapons (2);
        fTechLevels.setShields (2);
        fTechLevels.setMini (1);

        ui.setLayout (new PercentLayout(PercentLayout.HORZ));
        ui.add (fTechLevels, new Float(.2f));
        ui.add (fTechSpendingUI, new Float(.8f));

        return ui;
    }

    public void actionPerformed (ActionEvent event)
    {
        if ( event.getActionCommand() == "Selection Changed" ) {
            handleSelectionChanged ((PlanetView)event.getSource ());
        }

        if ( event.getActionCommand() == "New Turn" ) {
            handleNewTurn ();
        }

    }

    protected void handleSelectionChanged (PlanetView planet)
    {

        //
        // Record current planet budget mix before swithing selection
        //

        if ( fSelection != null ) {
            BudgetSummary budget = fGame.getPlanetBudget (fSelection);
            if ( budget != null ) {
                double terraforming = fPlanetSpending.getFirstPercentage ();
                double mining = fPlanetSpending.getSecondPercentage ();
                budget.setPercentage ("Terraforming", terraforming);
                budget.setPercentage ("Mining", mining);
                //fGame.setPlanetBudget (fSelection, budget);
            }
        }

        //
        // Switch selection and update planet stats
        //

        if ( fPlanetStatsUI != null ) {
            fPlanetStatsUI.presentPlanet (planet);
            fSelection = planet.getID ();
        }

        //
        // Set planet budget mix for new selection
        //

        if ( fSelection != null ) {
            BudgetSummary budget = fGame.getPlanetBudget (fSelection);
            if ( budget != null ) {
                double terraforming = budget.getPercentage("Terraforming");
                fPlanetSpending.setFirstPercentage (terraforming);
                fPlanetSpending.setFirstColor (Color.green);
                fPlanetSpending.setSecondColor (Color.blue);
                fPlanetSpending.repaint ();
            } else {
                fPlanetSpending.setFirstPercentage (0.5);
                fPlanetSpending.setFirstColor (Color.black);
                fPlanetSpending.setSecondColor (Color.black);
                fPlanetSpending.repaint ();
            }
        }

        //
        // Update list of fleets
        //

        updateShipListView ();
        fGalaxyUI.fBackBuffer = null;

    }

  
    protected void updateShipListView ()
    {
        fShipListView.removeAll ();
        if ( fSelection != null ) {
            ID[] fleets = fGame.getFleetsAt (fSelection);
            if ( fleets != null ) {
                int size = Array.getLength (fleets);
                for (int i=0; i<size; i++) {
                    Fleet fleet = fGame.getFleet (fleets[i]);
                    if ( fleet != null ) {
                        fShipListView.addItem (fleet);
                    }
                }
            }
        }
        fShipListView.repaint ();
    }

    public ID getSelectedFleet ()
    {
        Fleet[] sel = fShipListView.getSelection ();
        if ( sel != null && Array.getLength(sel) == 1) {
            return sel[0].getID ();
        } else {
            return null;
        }
    }


    public int getSelectedFleetRange ()
    {
        Fleet[] sel = fShipListView.getSelection ();

        if ( sel != null && Array.getLength(sel) == 1) {
            return sel[0].getCurrentRange ();
        } else {
            return 0;
        }
    }

    protected void updateResourcesView ()
    {
        fResourceLevels.setShipSavings (fGame.getShipSavings());
        fResourceLevels.setShipMetal (fGame.getShipMetal());
        fResourceLevels.setIncome (fGame.getIncome());
        fResourceLevels.setIIOPCalls (fGame.getCalls());
        fResourceLevels.repaint ();
    }


    protected void pushBudgetSettings ()
    {
        BudgetSummary budget = null;

        //
        // Main Budget
        //

        budget = fGame.getMainBudget ();
        LabeledBarControl[] bars = fBudgetUI.getBars ();
        for (int i=0; i<Array.getLength(bars); i++) {
            LabeledBarControl bar = bars[i];
            budget.setPercentage (bar.getLabelText(), bar.getPercentage());
        }
        fGame.setMainBudget (budget);

        //
        // Planet Budget
        //


        if ( fSelection != null ) {
            budget = fGame.getPlanetBudget (fSelection);
            if ( budget != null ) {
                double terraforming = fPlanetSpending.getFirstPercentage ();
                double mining = fPlanetSpending.getSecondPercentage ();
                budget.setPercentage ("Terraforming", terraforming);
                budget.setPercentage ("Mining", mining);
                fGame.setPlanetBudget (fSelection, budget);
            }
        }

        fGame.pushPlanetBudgetData ();

        //
        // Tech Budget
        //

        budget = fGame.getTechBudget ();
        double range = fRangeSpending.getPercentage ();
        double speed = fSpeedSpending.getPercentage ();
        double weapons = fWeaponsSpending.getPercentage ();
        double shields = fShieldsSpending.getPercentage ();
        double mini = fMiniSpending.getPercentage ();
        budget.setPercentage ("Range", range);
        budget.setPercentage ("Speed", speed);
        budget.setPercentage ("Weapons", weapons);
        budget.setPercentage ("Shields",shields);
        budget.setPercentage ("Mini", mini);
        fGame.setTechBudget (budget);
    }

    private void updateMainBudgetUI ()
    {
        fBudgetUI.removeAll ();
        BudgetSummary budget = fGame.getMainBudget ();
        String[] names = budget.getNames ();

        for (int i=0; i<Array.getLength(names); i++) {
            LabeledBarControl bar = new LabeledBarControl (names[i]);
            bar.setPercentage (budget.getPercentage (names[i]));
            String name = bar.getLabelText ();
            if ( name.equals ("Ship Savings") ) {
                bar.setBarColor (Color.orange);
            } else if ( name.equals ("Technology") ) {
                bar.setBarColor (Color.magenta);
            } else {
                bar.setBarColor (Color.green);
            }
            fBudgetUI.addBar (bar);
        }
        fBudgetUI.revalidate ();
        fBudgetUI.repaint ();
    }

    protected void handleNewTurn ()
    {
        updateShipListView ();
        fGame.updateJourneys ();
        fGalaxyUI.refreshCache ();
        fGalaxyUI.repaint();

        //
        // Update planet spending control
        //

        if ( fSelection != null ) {
            BudgetSummary budget = fGame.getPlanetBudget (fSelection);
            if ( budget != null ) {
                double terraforming = budget.getPercentage("Terraforming");
                fPlanetSpending.setFirstPercentage (terraforming);
                fPlanetSpending.setFirstColor (Color.green);
                fPlanetSpending.setSecondColor (Color.blue);
                fPlanetSpending.repaint ();
            } else {
                fPlanetSpending.setFirstPercentage (0.5);
                fPlanetSpending.setFirstColor (Color.black);
                fPlanetSpending.setSecondColor (Color.black);
                fPlanetSpending.repaint ();
            }
        }
        fPlanetSpending.repaint ();

        PlanetView planet = fGame.getPlanet (fSelection);
        fPlanetStatsUI.presentPlanet (planet);
        fPlanetStatsUI.repaint ();

        //
        // Update tech profile
        //

        TechProfile tech = fGame.getTechProfile ();
        fTechLevels.setRange (tech.getRange());
        fTechLevels.setSpeed (tech.getSpeed());
        fTechLevels.setWeapons (tech.getWeapons());
        fTechLevels.setShields (tech.getShields());
        fTechLevels.setMini (tech.getMini());


        updateResourcesView ();

        BudgetSummary budget = null;


        //
        // Update budget bars
        //

        updateMainBudgetUI ();

        fBudgetUI.validate ();

        //
        // Update technology budget bars
        //

        budget = fGame.getTechBudget ();
        double range = budget.getPercentage("Range");
        double speed = budget.getPercentage("Speed");
        double weapons = budget.getPercentage("Weapons");
        double shields = budget.getPercentage("Shields");
        double mini = budget.getPercentage("Mini");
        fRangeSpending.setPercentage (range);
        fSpeedSpending.setPercentage (speed);
        fWeaponsSpending.setPercentage (weapons);
        fShieldsSpending.setPercentage (shields);
        fMiniSpending.setPercentage (mini);
    }

    class CreateShip implements java.awt.event.ActionListener
    {
        private int fType = 0;

        CreateShip (int type)
        {
            fType = type;
        }

        public void actionPerformed (ActionEvent e)
        {
            System.out.println ("Create ship");
            ShipDesign design = new ShipDesign ("", fType, fGame.getTechProfile());
            long cost = design.getCostPerShip();
            long metal = design.getMetalPerShip();
            long shipSavings = fGame.getShipSavings ();
            long shipMetal = fGame.getShipMetal ();

            String message = "You can only build ships at a colonized planet!";

            PlanetView planet = fGame.getPlanet (fSelection);
            if ( planet != null ) {
                if ( planet.isOwned() ) {
                    if ( planet.getPopulation() > 0 ) {
                        message = "";
                    }
                }
            }

            if ( message.equals("") ) {
                if ( cost > shipSavings || metal > shipMetal ) {
                    message = "You do not have enough money or metal!";
                } 
            }

            if ( !message.equals("") ) {
                JOptionPane.showMessageDialog (fFrame, message, "IBM Space Conquest",
                                               JOptionPane.OK_OPTION);
                return;
            }

            message = "Cost = " + cost + ", Metal = " + metal + ".";
            int build = JOptionPane.showConfirmDialog (fFrame, message, "IBM Space Conquest",
                                                       JOptionPane.YES_NO_OPTION);
            if ( build == JOptionPane.YES_OPTION) {
                fGame.buildFleet (design, 1, fSelection);
            }
            updateShipListView ();
            updateResourcesView ();
        }
    }

    class AbandonPlanet implements java.awt.event.ActionListener
    {
        AbandonPlanet ()
        {
        }

        public void actionPerformed (ActionEvent e)
        {
            fGame.abandonPlanet (fSelection);
            updateMainBudgetUI ();
        }
    }

    class Surrender implements java.awt.event.ActionListener
    {
        Surrender ()
        {
        }

        public void actionPerformed (ActionEvent e)
        {
            fGame.surrender ();
            fGame.updateJourneys ();
            updateMainBudgetUI ();
            updateShipListView ();
            updateResourcesView ();
            fGalaxyUI.refreshCache ();
            fGalaxyUI.repaint();
        }
    }

    class ScrapFleet implements java.awt.event.ActionListener
    {
        ScrapFleet ()
        {
        }

        public void actionPerformed (ActionEvent e)
        {
            fGame.scrapFleet (getSelectedFleet());
            updateShipListView ();
            updateResourcesView ();
        }
    }


}
