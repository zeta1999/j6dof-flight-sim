package com.chrisali.javaflightsim.aircraft;

public class Aircraft {
	protected double[] centerOfGravity;    // {CG_x,CG_y,CG_z}
	protected double[] aerodynamicCenter;  // {ac_x,ac_y,ac_z}
	protected double[] enginePosition;	   // {eng_x,eng_y,eng_z}
	
	protected double[] massProperties;     // {weight,Ix,Iy,Iz,Ixz}
	
	protected double[] wingDimensions;	   // {wingSfcArea,b,c_bar}
	
	protected double[] liftDerivs; 		   // {CL_alpha,CL_0,CL_q,CL_alphadot,CL_de,CL_df}
	protected double[] sideForceDerivs;	   // {CY_beta,CY_dr}
	protected double[] dragDerivs; 		   // {CD_alpha,CD_0,CD_df,CD_de,CD_dg}

	protected double[] rollMomentDerivs;   // {Cl_beta,Cl_p,Cl_r,Cl_da,Cl_dr}
	protected double[] pitchMomentDerivs;  // {CM_alpha,CM_0,CM_q,CM_alphadot,CM_de,CM_df}
	protected double[] yawMomentDerivs;    // {CN_beta,CN_p,CN_r,CN_da,CN_dr}
	
	// Default constructor to give default values for aircraft definition (Navion)
	public Aircraft() { 
		centerOfGravity     = new double[]{0,0,0};
		aerodynamicCenter   = new double[]{0,0,0};
		enginePosition		= new double[]{0,0,0};
		
		massProperties      = new double[]{2750/32.2,1048,3000,3050,0};
		
		wingDimensions		= new double[]{184,33.4,5.7};
		
		liftDerivs			= new double[]{4.44,0.41,3.8,0,0.355,0.355};
		sideForceDerivs		= new double[]{-0.564,0.157};
		dragDerivs			= new double[]{0.33,0.025,0.02,0.001,0.09};
		
		rollMomentDerivs	= new double[]{-0.074,-0.410,0.107,-0.134,0.107};
		pitchMomentDerivs	= new double[]{-0.683,0.02,-9.96,-4.36,-0.923,-0.050};
		yawMomentDerivs		= new double[]{0.071,-0.0575,-0.125,-0.0035,-0.072};
	}
	
	// TODO Read a text file with aircraft attributes, and assign them to arrays	
/*
	public Aircraft(String fileName){
		File file = new File(fileName);
		try (BufferedReader aircraftReader = new BufferedReader(new FileReader(file))){
			String tempLine;
			while ((tempLine = aircraftReader.readLine())!=null) {
				switch (tempLine.substring(0,1)){
					case "CL": {
						for(int i=0;i<6;i++)
					        liftDerivs[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "CY": {
						for(int i=0;i<6;i++)
							sideForceDerivs[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "CD": {
						for(int i=0;i<6;i++)
							dragDerivs[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "Cl": {
						for(int i=0;i<6;i++)
							rollMomentDerivs[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "CM": {
						for(int i=0;i<6;i++)
							pitchMomentDerivs[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "CN": {
						for(int i=0;i<5;i++)
							yawMomentDerivs[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "ac": {
						for(int i=0;i<3;i++)
							aerodynamicCenter[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "CG": {
						for(int i=0;i<3;i++)
							centerOfGravity[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
					case "en": {
						for(int i=0;i<3;i++)
							enginePosition[i]=tempLine.split("=")[1].parseDouble();
						break;
					}
				}
			}
		} catch (IOException e) {e.printStackTrace();}	
	}
*/
	public double[] getCenterOfGravity() {return centerOfGravity;}

	public double[] getAerodynamicCenter() {return aerodynamicCenter;}

	public double[] getEnginePosition() {return enginePosition;}

	public double[] getMassProperties() {return massProperties;}

	public double[] getWingDimensions() {return wingDimensions;}

	public double[] getLiftDerivs() {return liftDerivs;}

	public double[] getSideForceDerivs() {return sideForceDerivs;}

	public double[] getDragDerivs() {return dragDerivs;}

	public double[] getRollMomentDerivs() {return rollMomentDerivs;}

	public double[] getPitchMomentDerivs() {return pitchMomentDerivs;}

	public double[] getYawMomentDerivs() {return yawMomentDerivs;}
}
