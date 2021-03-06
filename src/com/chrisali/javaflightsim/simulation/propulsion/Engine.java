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
package com.chrisali.javaflightsim.simulation.propulsion;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.chrisali.javaflightsim.simulation.enviroment.EnvironmentParameters;
import com.chrisali.javaflightsim.simulation.flightcontrols.FlightControl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Base abstract class for the flight simulation's engine model. 
 * It uses the 1976 NASA Standard Atmosphere model, and assumes that gravity is constant in the Z direction.
 */
@JsonDeserialize(as = FixedPitchPropEngine.class)
public abstract class Engine {
	
	// Propeller Engine Parameters
	@JsonIgnore
	final static protected double A_P        = 1.132; 
	@JsonIgnore
	final static protected double B_P        = 0.132;
	@JsonIgnore
	final static protected double RHO_SSL    = 0.002377;
	@JsonIgnore
	final static protected double HP_2_FTLBS = 550;
	
	protected double maxBHP;            //BHP at standard sea level
	protected double maxRPM;			//rev/min
	protected double propDiameter;		//ft
	@JsonIgnore
	protected double propArea;			//ft^2
	protected double propEfficiency;
		
	// Jet Engine Parameters
	//TODO add jet/turboprop
	
	// Universal Parameters
	protected String   engineName;
	protected int      engineNumber;
	protected double[] enginePosition; 	   			// {eng_x,eng_y,eng_z}  (ft)

	// State Parameters
	@JsonIgnore
	protected double rpm;
	@JsonIgnore
	protected double fuelFlow;
	@JsonIgnore
	protected double[] engineThrust   = {0, 0, 0};	// {T_x,T_y,T_z}	    (lbf)			
	@JsonIgnore
	protected double[] engineMoment;				// {M_x,M_y,M_z}        (lbf)
		
	//TODO need engine model properties (etaP, advance ratio, bhp curves) for lookup tables
	//TODO etaP needs to vary
  	
	/**
	 * Calculates all parameters of the engine given the input parameters specified below
	 * 
	 * @param controls
	 * @param environmentParameters
	 * @param windParameters
	 */
	public abstract void updateEngineState(Map<FlightControl, Double> controls,				
										   Map<EnvironmentParameters, Double> environmentParameters,
										   double[] windParameters);
	
	/**
	 * Calculates the moment generated by the engine as a function of its thrust and location
	 * relative to the aircraft's center of gravity. Used in {@link Engine#updateEngineState(EnumMap, EnumMap, double[])}
	 */
	protected void calculateEngMoments() {
		Vector3D forceVector = new Vector3D(engineThrust);
		Vector3D armVector   = new Vector3D(enginePosition);
		
		this.engineMoment = Vector3D.crossProduct(forceVector, armVector).toArray();
	}
	
	/**
	 * @return engine thrust as a double array vector (lbf)
	 */
	public double[] getEngineThrust() { return engineThrust; }
	
	/**
	 * @return engine moment as a double array vector (lb*ft)
	 */
	public double[] getEngineMoment() { return engineMoment; }
	
	/**
	 * @return engine RPM
	 */
	public double getRPM() { return rpm; }
	
	/**
	 * @return engine fuel flow (gal/hr)
	 */
	public double getFuelFlow() { return fuelFlow; }
			
	public double getMaxBHP() { return maxBHP; }

	public void setMaxBHP(double maxBHP) { this.maxBHP = maxBHP; }

	public double getMaxRPM() { return maxRPM; }

	public void setMaxRPM(double maxRPM) { this.maxRPM = maxRPM; }

	public double getPropDiameter() { return propDiameter; }

	public void setPropDiameter(double propDiameter) { this.propDiameter = propDiameter; }

	public double getPropEfficiency() { return propEfficiency; }

	public void setPropEfficiency(double propEfficiency) { this.propEfficiency = propEfficiency; }

	public String getEngineName() { return engineName; }

	public void setEngineName(String engineName) { this.engineName = engineName; }

	public int getEngineNumber() { return engineNumber; }

	public void setEngineNumber(int engineNumber) { this.engineNumber = engineNumber; }
	
	/**
	 * @return engine position relative to aircraft CG [x, y, z] (ft)
	 */
	public double[] getEnginePosition() {return enginePosition;}
	
	/**
	 * Sets engine position relative to aircraft CG [x, y, z] (ft)
	 * @param enginePosition
	 */
	public void setEnginePosition(double[] enginePosition) { this.enginePosition = enginePosition; }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((engineName == null) ? 0 : engineName.hashCode());
		result = prime * result + engineNumber;
		result = prime * result + Arrays.hashCode(enginePosition);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Engine other = (Engine) obj;
		if (engineName == null) {
			if (other.engineName != null)
				return false;
		} else if (!engineName.equals(other.engineName))
			return false;
		if (engineNumber != other.engineNumber)
			return false;
		if (!Arrays.equals(enginePosition, other.enginePosition))
			return false;
		return true;
	}
}
