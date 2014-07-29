package it.polimi.poccodeloading;

import java.util.List;

import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Interface defined to mask the dynamic retrieval of the customizer
 * used to modify the layout of GUI components.
 * 
 * @author Luca Falsina
 *
 */
public interface ComponentModifier {
	
	public void customizeButtons(List<Button> buttonList);
	
	public void customizeSwitch(Switch switchSlider);

	public void customizeTextView(TextView textView);

}
