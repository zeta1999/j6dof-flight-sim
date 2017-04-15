/*******************************************************************************
 * Copyright (C) 2016-2017 Christopher Ali
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  If you have any questions about this project, you can visit
 *  the project's GitHub repository at: http://github.com/chris-ali/j6dof-flight-sim/
 ******************************************************************************/
package com.chrisali.javaflightsim.controllers;

import java.awt.Canvas;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chrisali.javaflightsim.consoletable.ConsoleTablePanel;
import com.chrisali.javaflightsim.datatransfer.EnvironmentData;
import com.chrisali.javaflightsim.datatransfer.FlightData;
import com.chrisali.javaflightsim.menus.MainFrame;
import com.chrisali.javaflightsim.menus.SimulationWindow;
import com.chrisali.javaflightsim.menus.optionspanel.AudioOptions;
import com.chrisali.javaflightsim.menus.optionspanel.DisplayOptions;
import com.chrisali.javaflightsim.otw.RunWorld;
import com.chrisali.javaflightsim.otw.renderengine.DisplayManager;
import com.chrisali.javaflightsim.plotting.PlotWindow;
import com.chrisali.javaflightsim.simulation.aircraft.AircraftBuilder;
import com.chrisali.javaflightsim.simulation.aircraft.MassProperties;
import com.chrisali.javaflightsim.simulation.controls.FlightControlType;
import com.chrisali.javaflightsim.simulation.controls.FlightControls;
import com.chrisali.javaflightsim.simulation.integration.Integrate6DOFEquations;
import com.chrisali.javaflightsim.simulation.integration.SimOuts;
import com.chrisali.javaflightsim.simulation.setup.InitialConditions;
import com.chrisali.javaflightsim.simulation.setup.IntegrationSetup;
import com.chrisali.javaflightsim.simulation.setup.IntegratorConfig;
import com.chrisali.javaflightsim.simulation.setup.Options;
import com.chrisali.javaflightsim.simulation.setup.Trimming;
import com.chrisali.javaflightsim.utilities.FileUtilities;

/**
 * Controls the configuration and running of processes supporting the simulation component of JavaFlightSim. This consists of: 
 * <p>The simulation engine that integrates the 6DOF equations ({@link Integrate6DOFEquations})</p>
 * <p>Plotting of the simulation states and data ({@link PlotWindow})</p>
 * <p>Raw data display of simulation states ({@link ConsoleTablePanel})</p>
 * <p>Transmission of flight data to the instrument panel and out the window display ({@link FlightData})</p>
 * <p>Transmission of environment data to the simulation ({@link EnvironmentData})</p>
 * 
 * @author Christopher Ali
 *
 */
public class SimulationController {
	
	// Configuration
	private EnumMap<DisplayOptions, Integer> displayOptions;
	private EnumMap<AudioOptions, Float> audioOptions;
	private EnumSet<Options> simulationOptions;
	private EnumMap<InitialConditions, Double> initialConditions;
	private EnumMap<IntegratorConfig, Double> integratorConfig;
	private EnumMap<FlightControlType, Double> initialControls; 
	
	// Simulation
	private FlightControls flightControls;
	private Thread flightControlsThread;
	private Integrate6DOFEquations runSim;
	private Thread simulationThread;
	private FlightData flightData;
	private Thread flightDataThread;
	
	// Aircraft
	private AircraftBuilder ab;
	private EnumMap<MassProperties, Double> massProperties;
	
	// Menus and Integrated Simulation Window
	private MainFrame mainFrame;
	
	// Plotting
	private PlotWindow plotWindow;
	private Set<String> plotCategories = new HashSet<>(Arrays.asList("Controls", "Instruments", "Position", "Rates", "Miscellaneous"));
	
	// Raw Data Console
	private ConsoleTablePanel consoleTablePanel;
	
	// Out the Window
	private RunWorld outTheWindow;
	private Thread outTheWindowThread;
	private Thread environmentDataThread;
	private EnvironmentData environmentData;
	
	/**
	 * Constructor for the controller that initializes initial settings, configurations and conditions
	 * to be edited through the menu options in the view
	 */
	public SimulationController() {
		simulationOptions = FileUtilities.parseSimulationSetup();
		displayOptions = FileUtilities.parseDisplaySetup();
		audioOptions = FileUtilities.parseAudioSetup();
		
		initialConditions = IntegrationSetup.gatherInitialConditions(null);
		integratorConfig = IntegrationSetup.gatherIntegratorConfig(null);
		initialControls = IntegrationSetup.gatherInitialControls(null);
		
		String aircraftName = FileUtilities.parseSimulationSetupForAircraft();
		ab = new AircraftBuilder(aircraftName);
	}
	
	//=============================== Configuration ===========================================================
	
	/**
	 * @return simulationOptions EnumSet
	 */
	public EnumSet<Options> getSimulationOptions() {return simulationOptions;}
	
	/**
	 * @return displayOptions EnumMap
	 */
	public EnumMap<DisplayOptions, Integer> getDisplayOptions() {return displayOptions;}
	
	/**
	 * @return audioOptions EnumMap
	 */
	public EnumMap<AudioOptions, Float> getAudioOptions() {return audioOptions;}
	
	/**
	 * Updates simulation and display options and then saves the configurations to text files using either
	 * <p>{@link FileUtilities#writeConfigFile(String, String, Set, String)}</p>
	 * <br/>or
	 * <p>{@link FileUtilities#writeConfigFile(String, String, Map, String)}</p>
	 * 
	 * @param newOptions
	 * @param newDisplayOptions
	 * @param newAudioOptions
	 */
	public void updateOptions(EnumSet<Options> newOptions, EnumMap<DisplayOptions, Integer> newDisplayOptions,
							  EnumMap<AudioOptions, Float> newAudioOptions) {
		simulationOptions = EnumSet.copyOf(newOptions);
		displayOptions = newDisplayOptions;
		audioOptions = newAudioOptions;
		
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.SIMULATION_SETUP_FILE, simulationOptions, ab.getAircraft().getName());
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.DISPLAY_SETUP_FILE, newDisplayOptions);
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.AUDIO_SETUP_FILE, newAudioOptions);
	}
	
	/**
	 * Calls the {@link AircraftBuilder} constructor with using the aircraftName argument and updates the SimulationSetup.txt
	 * configuration file with the new selected aircraft
	 * 
	 * @param aircraftName
	 */
	public void updateAircraft(String aircraftName) {
		ab = new AircraftBuilder(aircraftName);
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.SIMULATION_SETUP_FILE, simulationOptions, aircraftName);
	}
	
	/**
	 * Updates the MassProperties config file for the selected aircraft using aircraftName
	 * 
	 * @param aircraftName
	 * @param fuelWeight
	 * @param payloadWeight
	 */
	public void updateMassProperties(String aircraftName, double fuelWeight, double payloadWeight) {
		massProperties = FileUtilities.parseMassProperties(aircraftName);
		
		massProperties.put(MassProperties.WEIGHT_FUEL, fuelWeight);
		massProperties.put(MassProperties.WEIGHT_PAYLOAD, payloadWeight);
		
		FileUtilities.writeConfigFile(FileUtilities.AIRCRAFT_DIR + File.pathSeparator + aircraftName, FileUtilities.MASS_PROPERTIES_FILE, massProperties);
	}
	
	/**
	 * @return integratorConfig EnumMap
	 */
	public EnumMap<IntegratorConfig, Double> getIntegratorConfig() {return integratorConfig;}

	/**
	 * Updates the IntegratorConfig file with stepSize inverted and converted to a double  
	 * 
	 * @param stepSize
	 */
	public void updateIntegratorConfig(int stepSize) {
		integratorConfig.put(IntegratorConfig.DT, (1/((double)stepSize)));
		
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.INTEGRATOR_CONFIG_FILE, integratorConfig);
	}
	
	/**
	 * @return initialConditions EnumMap
	 */
	public EnumMap<InitialConditions, Double> getInitialConditions() {return initialConditions;}

	/**
	 * Updates initialConditions file with the following arguments, converted to radians and ft/sec:
	 * 
	 * @param coordinates [latitude, longitude]
	 * @param heading 
	 * @param altitude 
	 * @param airspeed
	 */
	public void updateInitialConditions(double[] coordinates, double heading, double altitude, double airspeed) {
		initialConditions.put(InitialConditions.INITLAT, Math.toRadians(coordinates[0]));
		initialConditions.put(InitialConditions.INITLON, Math.toRadians(coordinates[1]));
		initialConditions.put(InitialConditions.INITPSI, Math.toRadians(heading));
		initialConditions.put(InitialConditions.INITU,   FileUtilities.toFtPerSec(airspeed));
		initialConditions.put(InitialConditions.INITD,   altitude);
		
		// Temporary method to calcuate north/east position from lat/lon position 
		initialConditions.put(InitialConditions.INITN, (Math.sin(Math.toRadians(coordinates[0])) * 20903520));
		initialConditions.put(InitialConditions.INITE, (Math.sin(Math.toRadians(coordinates[1])) * 20903520));
		
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.INITIAL_CONDITIONS_FILE, initialConditions);
	}
	
	/**
	 * Updates the InitialControls config file
	 */
	public void updateIninitialControls() {
		FileUtilities.writeConfigFile(FileUtilities.SIM_CONFIG_DIR, FileUtilities.INITIAL_CONTROLS_FILE, initialControls);
	}

	/**
	 * @return initialControls EnumMap
	 */
	public EnumMap<FlightControlType, Double> getInitialControls() {return initialControls;}
	
	//=============================== Simulation ===========================================================

	/**
	 * @return instance of simulation
	 */
	public Integrate6DOFEquations getSimulation() {return runSim;}
	
	/**
	 * @return {@link AircraftBuilder} object
	 */
	public AircraftBuilder getAircraftBuilder() {return ab;}
	
	/**
	 * Allows {@link AircraftBuilder} to be changed to a different aircraft outside of being parsed in
	 * the SimulationSetup.txt configuration file
	 * 
	 * @param ab
	 */
	public void setAircraftBuilder(AircraftBuilder ab) {this.ab = ab;}
	
	/**
	 * @return ArrayList of simulation output data 
	 * @see SimOuts
	 */
	public List<Map<SimOuts, Double>> getLogsOut() {return runSim.getLogsOut();}
	
	/**
	 * @return if runSim was able to clear simulation data kept in logsOut
	 */
	public boolean clearLogsOut() {
		if(runSim != null)
			return runSim.clearLogsOut();
		else
			return false;
	}
	
	/**
	 * Initializes, trims and starts the flight controls, simulation (and flight and environment data, if selected) threads.
	 * Depending on options specified, a console panel and/or plot window will also be initialized and opened 
	 */
	public void startSimulation() {
		Trimming.trimSim(this, false);
		
		flightControls = new FlightControls(this);
		flightControlsThread = new Thread(flightControls);
		
		runSim = new Integrate6DOFEquations(flightControls, this);
		simulationThread = new Thread(runSim);

		flightControlsThread.start();
		simulationThread.start();
		
		if (simulationOptions.contains(Options.CONSOLE_DISPLAY))
			initializeConsole();
		
		if (simulationOptions.contains(Options.ANALYSIS_MODE)) {
			try {
				// Wait a bit to allow the simulation to finish running
				Thread.sleep(1000);
				plotSimulation();
				//Stop flight controls thread after analysis finished
				FlightControls.setRunning(false);
			} catch (InterruptedException e) {}
			
		} else {
			outTheWindow = new RunWorld(this);
			//(Re)initalize simulation window to prevent scaling issues with instrument panel
			getMainFrame().initSimulationWindow();
			
			environmentData = new EnvironmentData(outTheWindow);
			environmentData.addEnvironmentDataListener(runSim);
			
			environmentDataThread = new Thread(environmentData);
			environmentDataThread.start();
			
			flightData = new FlightData(runSim);
			flightData.addFlightDataListener(mainFrame.getInstrumentPanel());
			flightData.addFlightDataListener(outTheWindow);
			
			flightDataThread = new Thread(flightData);
			flightDataThread.start();
		}
	}
	
	/**
	 * Stops simulation, flight controls and data transfer threads (if running), closes the raw data {@link ConsoleTablePanel},
	 * {@link SimulationWindow}, and opens the main menus window again
	 */
	public void stopSimulation() {
		if (runSim != null && Integrate6DOFEquations.isRunning() && simulationThread != null && simulationThread.isAlive()) {
			Integrate6DOFEquations.setRunning(false);
			FlightControls.setRunning(false);
		}
		
		if (flightDataThread != null && flightDataThread.isAlive())
			FlightData.setRunning(false);
		
		if (outTheWindowThread != null && outTheWindowThread.isAlive())
			EnvironmentData.setRunning(false);
		
		getMainFrame().getSimulationWindow().dispose();
		getMainFrame().setVisible(true);
	}
	
	//=============================== Plotting =============================================================
	
	/**
	 * Initializes the plot window if not already initialized, otherwise refreshes the window and sets it visible again
	 */
	public void plotSimulation() {
		if(plotWindow == null)
			plotWindow = new PlotWindow(plotCategories, this);
		else
			plotWindow.refreshPlots(runSim.getLogsOut());
		
		if (!isPlotWindowVisible())
			plotWindow.setVisible(true);
	}
	
	/**
	 * @return if the plot window is visible
	 */
	public boolean isPlotWindowVisible() {
		if (plotWindow == null) return false;
		else return plotWindow.isVisible();
	}
	
	//=============================== Console =============================================================
	
	/**
	 * Initializes the raw data console window and starts the auto-refresh of its contents
	 */
	public void initializeConsole() {
		consoleTablePanel = new ConsoleTablePanel(this);
		consoleTablePanel.startTableRefresh();
	}
	
	/**
	 * @return if the raw data console window is visible
	 */
	public boolean isConsoleWindowVisible() {
		if (consoleTablePanel == null) return false;
		else return consoleTablePanel.isVisible();
	}
	
	/**
	 * Saves the raw data in the console window to a .csv file 
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void saveConsoleOutput(File file) throws IOException {
		FileUtilities.saveToCSVFile(file, runSim.getLogsOut());
	}
	
	//========================== Main Frame Menus =========================================================
	
	/**
	 * Sets {@link MainFrame} reference for {@link RunWorld}, which needs it to 
	 * set the parent {@link Canvas} in {@link DisplayManager}
	 * 
	 * @param mainFrame
	 */
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	
	/**
	 * @return reference to {@link MainFrame} object in {@link SimulationController}
	 */
	public MainFrame getMainFrame() {
		return mainFrame;
	}

	//=========================== OTW Threading ==========================================================
	
	/**
	 * Initalizes and starts out the window thread; called from {@link SimulationWindow}'s addNotify() method
	 * to allow OTW thread to start gracefully; uses the Stack Overflow solution shown here:
	 * <p>http://stackoverflow.com/questions/26199534/how-to-attach-opengl-display-to-a-jframe-and-dispose-of-it-properly</p>
	 */
	public void startOTWThread() {
		outTheWindowThread = new Thread(outTheWindow);
		outTheWindowThread.start(); 
	}
	
	/**
	 * Stops out the window thread; called from {@link SimulationWindow}'s removeNotify() method
	 * to allow OTW thread to stop gracefully; uses the Stack Overflow solution shown here:
	 * <p>http://stackoverflow.com/questions/26199534/how-to-attach-opengl-display-to-a-jframe-and-dispose-of-it-properly</p>
	 */
	public void stopOTWThread() {
		RunWorld.requestClose(); // sets running boolean in RunWorld to false to begin the clean up process
		
		try {outTheWindowThread.join();
		} catch (InterruptedException e) {}
	}
}
