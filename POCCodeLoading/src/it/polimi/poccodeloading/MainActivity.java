package it.polimi.poccodeloading;

import android.app.Activity;
//import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity is the entry point of the application.
 * By interacting with the different elements in the list of buttons
 * it is possible to trigger different ways to retrieve external code
 * from either a remote or a local path during the application execution.
 * 
 * @author Luca Falsina
 *
 */
public class MainActivity extends Activity {

	// This array of strings contains the list of all the implemented
	// techniques for external code loading that should be visualized.
	public static final String techinqueToExecute[] = {"DexClassLoader", "PathClassLoader", "createPackageContext"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// The list view element is retrieved..
		ListView listView = (ListView) findViewById(R.id.listview);
		// Generate a dynamic list depending on the labels
		listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, techinqueToExecute));
				
		// Create a message handling object as an anonymous class.
		OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
			// Depending on the chosen button a different technique
			// is used..
			
			}
		};

		listView.setOnItemClickListener(mMessageClickedHandler);
	}
}
