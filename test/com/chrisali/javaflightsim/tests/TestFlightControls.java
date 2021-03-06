/*******************************************************************************
 * Copyright (C) 2016-2018 Christopher Ali
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
package com.chrisali.javaflightsim.tests;

import java.util.concurrent.atomic.AtomicInteger;

import com.chrisali.javaflightsim.initializer.LWJGLSwingSimulationController;
import com.chrisali.javaflightsim.interfaces.SimulationController;
import com.chrisali.javaflightsim.simulation.flightcontrols.FlightControlsState;
import com.chrisali.javaflightsim.simulation.flightcontrols.FlightControlsStateManager;
import com.chrisali.javaflightsim.simulation.setup.Options;
import com.chrisali.javaflightsim.simulation.utilities.FileUtilities;

/**
 * Test class for {@link FlightControlsState}. Creates flight controls object and thread to
 * run, and outputs values for each flight control deflection/setting
 * 
 * @author Christopher Ali
 *
 */
public class TestFlightControls implements Runnable {
	private FlightControlsStateManager flightControls;
	private SimulationController simController;
	
	public TestFlightControls() {
		simController = new LWJGLSwingSimulationController(FileUtilities.readSimulationConfiguration());
		simController.getConfiguration().getSimulationOptions().add(Options.USE_JOYSTICK);
		
		flightControls = new FlightControlsStateManager(simController, new AtomicInteger(0));
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(500);
			
			while (true) {
				flightControls.step();
				System.out.println(flightControls.getControlsState().toString());
				System.out.println();
				Thread.sleep((long) (250));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {new Thread(new TestFlightControls()).start();}
}
