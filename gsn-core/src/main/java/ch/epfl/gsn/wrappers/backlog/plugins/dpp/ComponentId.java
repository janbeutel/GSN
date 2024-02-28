package ch.epfl.gsn.wrappers.backlog.plugins.dpp;

public class ComponentId {

	/* general component id */
	public static final int DPP_COMPONENT_ID_INVALID      	= 0;    /* unknown / invalid component id */
	public static final int DPP_COMPONENT_ID_CC430     		= 1;    /* CC430 */
	public static final int DPP_COMPONENT_ID_WGPS2        	= 2;    /* WGPS2 */
	public static final int DPP_COMPONENT_ID_GEOPHONE    	= 3;    /* Geophone */
	public static final int DPP_COMPONENT_ID_DEVBOARD    	= 4;    /* DevBoard (BOLT bridge */
	public static final int DPP_COMPONENT_ID_SX1262    		= 5;    /* ComBoard with Semtech SX1262 */
	public static final int DPP_COMPONENT_ID_GEO3X    		= 6;    /* Geophone 3x */
	public static final int DPP_COMPONENT_ID_GEOMINI    	= 7;    /* Geophone mini */
	public static final int DPP_COMPONENT_ID_BASEBOARD    	= 8;    /* Baseboard */

	  /* no component id below this */
	public static final int DPP_COMPONENT_ID_LASTID       	= 0xFF;

}
