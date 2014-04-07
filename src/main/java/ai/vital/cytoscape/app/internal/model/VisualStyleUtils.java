package ai.vital.cytoscape.app.internal.model;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class VisualStyleUtils {

	public static VisualMappingManager vmmServiceRef;
	
	public static VisualStyleFactory visualStyleFactoryServiceRef;
	
	public static VisualMappingFunctionFactory vmfFactoryContinious;
	
	public static VisualMappingFunctionFactory vmfFactoryDiscrete;
	
	public static VisualMappingFunctionFactory vmfFactoryPassthrough;
	
	public static VisualStyle style;
	
	public static void init(VisualMappingManager _vmmServiceRef, VisualStyleFactory _visualStyleFactoryServiceRef,
			VisualMappingFunctionFactory _vmfFactoryContinious, VisualMappingFunctionFactory _vmfFactoryDiscrete,
			VisualMappingFunctionFactory _vmfFactoryPassthrough) {
		
		vmmServiceRef = _vmmServiceRef;
		visualStyleFactoryServiceRef = _visualStyleFactoryServiceRef;
		vmfFactoryContinious = _vmfFactoryContinious;
		vmfFactoryDiscrete = _vmfFactoryDiscrete;
		vmfFactoryPassthrough = _vmfFactoryPassthrough;
		
	}
	
	public static void applyVisualStyle(CyNetworkView networkView) {
		
		if(style == null) {
			style =  createStyle();
		}
		
		style.apply(networkView);
		networkView.updateView();
		
	}

	private static VisualStyle createStyle() {

		VisualStyle s = visualStyleFactoryServiceRef.createVisualStyle("Vital visual style");
		
		s = vmmServiceRef.getDefaultVisualStyle();
		
		//Use pass-through mapping
		String ctrAttrName1 = "SUID";
		
//		PassthroughMapping pMapping = (PassthroughMapping) vmfFactoryPassthrough.createVisualMappingFunction(ctrAttrName1, String.class, attrForTest, BasicVisualLexicon.NODE_LABEL);
//		s.addVisualMappingFunction(pMapping);                        
 
		s.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		
		// Add the new style to the VisualMappingManager
//		vmmServiceRef.addVisualStyle(s);
		
		return s;
	}
	
}
