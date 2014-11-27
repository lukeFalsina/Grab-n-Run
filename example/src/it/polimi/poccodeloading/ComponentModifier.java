/*******************************************************************************
 * Copyright 2014 Luca Falsina
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
