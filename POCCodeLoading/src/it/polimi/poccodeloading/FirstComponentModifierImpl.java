package it.polimi.poccodeloading;

import java.util.Iterator;
import java.util.List;

import android.graphics.Color;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

// Note that this class is not directly instantiated here but exported in a Jar container
// and then loaded and executed at run time.
public class FirstComponentModifierImpl implements ComponentModifier {

	@Override
	public void customizeButtons(List<Button> buttonList) {

		for (Iterator<Button> iterator = buttonList.iterator(); iterator.hasNext(); ) {
			
			Button btn = iterator.next();
			btn.setClickable(false);
			btn.setBackgroundColor(Color.YELLOW);
			btn.setText("Can't click anymore!");
		}
		
	}

	@Override
	public void customizeSwitch(Switch switchSlider) {

		switchSlider.setVisibility(android.view.View.VISIBLE);
		switchSlider.setChecked(false);
	}

	@Override
	public void customizeTextView(TextView textView) {

		textView.setText("This customization was performed dynamically by loading a new Java class" +
				"through a DexClassLoader. Please interact with the slider to end up this sample.");
		textView.setTextColor(Color.RED);
		textView.setTextSize(20);
		
	}

}
