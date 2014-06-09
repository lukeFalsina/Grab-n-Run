package it.polimi.poccodeloading;

import java.io.File;

import dalvik.system.DexClassLoader;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
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
	public static final String techinquesToExecute[] = {"DexClassLoader (.apk)", "DexClassLoader (.jar)", "PathClassLoader", "CreatePackageContext"};
	
	// Auxiliary constants used for readability..
	private static final int DEX_CLASS_LOADER_APK = 0;
	private static final int DEX_CLASS_LOADER_JAR = 1;
	private static final int PATH_CLASS_LOADER = 2;
	private static final int CREATE_PACK_CTX = 3;
	
	// Unique identifier used for Log entries
	private static final String TAG_MAIN = MainActivity.class.getSimpleName();
	
	// Used to validate dynamic code loading operations..
	private boolean effectiveDexClassLoader, effectivePathClassLoader;
	
	// Used to visualize helper toast messages..
	private Handler toastHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		effectiveDexClassLoader = false;
		effectivePathClassLoader = false;
		
		toastHandler = new Handler();
		
		// The list view element is retrieved..
		ListView listView = (ListView) findViewById(R.id.listview);
		// Generate a dynamic list depending on the labels
		listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, techinquesToExecute));
				
		// Create a message handling object as an anonymous class.
		OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						
			// Depending on the chosen button a different technique
			// is used..
			switch(position) {
			
				case DEX_CLASS_LOADER_APK:
					effectiveDexClassLoader = true;
					setUpDexClassLoader();
					effectiveDexClassLoader = false;
					Log.i(TAG_MAIN, "DexClassLoader case should be finished.");
					break;
				
				case DEX_CLASS_LOADER_JAR:
					
					break;
					
				case PATH_CLASS_LOADER:
					effectivePathClassLoader = true;
					setUpPathClassLoader();
					effectivePathClassLoader = false;
					Log.i(TAG_MAIN, "PathClassLoader case should be finished.");
					break;
				
				case CREATE_PACK_CTX:
					
					break;
				
				default:
					Log.d(TAG_MAIN, "Invalid button choice!");
			}
			
			}

		};

		listView.setOnItemClickListener(mMessageClickedHandler);
		
	}

	protected void setUpPathClassLoader() {
		
		// First check: this operation can only start after 
		// that the proper button has just been pressed..
		if (!effectivePathClassLoader) return;
				
		Log.i(TAG_MAIN, "Setting up Path Class Loader..");
	}

	/**
	 * This method is used to set up and manage a DexClassLoader component in 
	 * order to retrieve a new activity from an .apk, which has been 
	 * already downloaded and installed on the mobile device.
	 * If everything works fine, it will start running the main activity of 
	 * this .apk.
	 * 
	 */
	protected void setUpDexClassLoader() {
		
		// First check: this operation can only start after 
		// that the proper button has just been pressed..
		if (!effectiveDexClassLoader) return;
		
		Log.i(TAG_MAIN, "Setting up Dex Class Loader..");
		
		//String exampleAPKPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage.apk";
		//String exampleAPKPath = Environment.getRootDirectory().getAbsolutePath() + "/ext_card/download/NasaDailyImage/NasaDailyImage.apk";
		String exampleAPKPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage/NasaDailyImage.apk";
		
		File dexOutputDir = getDir("dex", MODE_PRIVATE);
		DexClassLoader mDexClassLoader = new DexClassLoader(	exampleAPKPath, 
																dexOutputDir.getAbsolutePath(), 
																null, 
																ClassLoader.getSystemClassLoader().getParent());
		
		try {
			
			Class<?> loadedClass = mDexClassLoader.loadClass("headfirstlab.nasadailyimage.NasaDailyImage");
			//Activity NasaDailyActivity = (Activity) loadedClass.newInstance();
			
			Log.i(TAG_MAIN, "Found class: " + loadedClass.getSimpleName() + "; APK path: " + exampleAPKPath.toString());
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							"DexClassLoader was successful! Starting a new activity..",
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			// An intent is defined to start the new loaded activity.
			Intent transitionIntent = new Intent(this, loadedClass);
			startActivity(transitionIntent);
			transitionIntent.setClassName("headfirstlab.nasadailyimage", "headfirstlab.nasadailyimage.NasaDailyImage");
			
		} catch (ClassNotFoundException e) {

			Log.e(TAG_MAIN, "Error: Class not found!");
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							"Error! No class found for DexClassLoader..",
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			e.printStackTrace();
		} catch (ActivityNotFoundException e) {
		
			Log.e(TAG_MAIN, "Error: Activity not found in the manifest!");
			
			toastHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this,
							"Error! The activity found by DexClassLoader is not a legitimate one..",
							Toast.LENGTH_SHORT).show();
				}
				
			});
			
			e.printStackTrace();
		}
	}
}
