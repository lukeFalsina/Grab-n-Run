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

import static android.os.Environment.getExternalStorageDirectory;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;
import it.necst.grabnrun.SecureDexClassLoader;
import it.necst.grabnrun.SecureLoaderFactory;

/**
 * This activity is the entry point of the application.
 * By interacting with the different elements in the list of buttons
 * it is possible to trigger different ways to retrieve external code
 * from either a remote or a local path during the application execution.
 *
 * @author Luca Falsina
 */
public class MainActivity extends Activity {

    // Extra passed to the intent to trigger the new activity with correct test parameters
    public static final String IS_SECURE_LOADING_CHOSEN = "it.polimi.poccodeloading.IS_SECURE_LOADING_CHOSEN";
    // Variable used to activate profiling settings
    private static final boolean PROFILING_ON = false;
    // This array of strings contains the list of all the implemented
    // techniques for external code loading that should be visualized.
    private static final String techniquesToExecute[] = {	"DexClassLoader (.apk)",
            "SecureDexClassLoader (.apk)",
            "DexClassLoader (.jar)",
            "SecureDexClassLoader (.jar)"};
    // Auxiliary constants used for readability..
    private static final int DEX_CLASS_LOADER_APK = 0;
    private static final int SECURE_DEX_CLASS_LOADER_APK = 1;
    private static final int DEX_CLASS_LOADER_JAR = 2;
    private static final int SECURE_DEX_CLASS_LOADER_JAR = 3;
    // Unique identifier used for Log entries
    private static final String TAG_MAIN = MainActivity.class.getSimpleName();
    // Used to validate dynamic code loading operations..
    private boolean effectiveDexClassLoader, effectiveSecureDexClassLoader;

    // Strings which represent locations of the apk containers used for the test
    // and the name of the class to load dynamically..
    private String exampleTestAPKPath, exampleSignedAPKPath, exampleSignedChangedAPKPath, classNameInAPK;

    // Used to visualize helper toast messages..
    private Handler toastHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        effectiveDexClassLoader = false;
        effectiveSecureDexClassLoader = false;

        toastHandler = new Handler();

        //String exampleTestAPKPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage.apk";
        //String exampleTestAPKPath = Environment.getRootDirectory().getAbsolutePath() + "/ext_card/download/NasaDailyImage/NasaDailyImage.apk";
        exampleTestAPKPath = getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage/NasaDailyImageDebugSigned.apk";

        exampleSignedAPKPath = getExternalStorageDirectory().getAbsolutePath() + "/Download/NasaDailyImage/NasaDailyImageSigned.apk";

        exampleSignedChangedAPKPath = "https://dl.dropboxusercontent.com/u/28681922/NasaDailyImageSignedChangedDigest.apk";

        classNameInAPK = "headfirstlab.nasadailyimage.NasaDailyImage";

        // The list view element is retrieved..
        ListView listView = (ListView) findViewById(R.id.listview);
        // Generate a dynamic list depending on the labels
        listView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, techniquesToExecute));

        // Create a message handling object as an anonymous class.
        OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Depending on the chosen button a different example case is taken..
                switch(position) {

                    case DEX_CLASS_LOADER_APK:
                        effectiveDexClassLoader = true;
                        Log.d(TAG_MAIN, "DexClassLoader from apk case should start.");
                        setUpDexClassLoader();
                        effectiveDexClassLoader = false;
                        break;

                    case SECURE_DEX_CLASS_LOADER_APK:
                        effectiveSecureDexClassLoader = true;
                        Log.d(TAG_MAIN, "SecureDexClassLoader from apk case should start.");
                        if (PROFILING_ON)
                            setUpProfileSecureDexClassLoader();
                        else
                            setUpSecureDexClassLoader();
                        effectiveSecureDexClassLoader = false;
                        break;

                    case DEX_CLASS_LOADER_JAR:
                        Intent dexClassLoaderIntent = new Intent(MainActivity.this, DexClassSampleActivity.class);
                        dexClassLoaderIntent.putExtra(IS_SECURE_LOADING_CHOSEN, false);
                        Log.d(TAG_MAIN, "DexClassLoader from jar case should start.");
                        startActivity(dexClassLoaderIntent);
                        break;

                    case SECURE_DEX_CLASS_LOADER_JAR:
                        Intent secureDexClassLoaderIntent = new Intent(MainActivity.this, DexClassSampleActivity.class);
                        secureDexClassLoaderIntent.putExtra(IS_SECURE_LOADING_CHOSEN, true);
                        Log.d(TAG_MAIN, "SecureDexClassLoader from jar case should start.");
                        startActivity(secureDexClassLoaderIntent);
                        break;

                    default:
                        Log.d(TAG_MAIN, "Invalid button choice!");
                }
            }
        };

        listView.setOnItemClickListener(mMessageClickedHandler);
    }

    protected void setUpProfileSecureDexClassLoader() {

        // First check: this operation can only start after
        // that the proper button has just been pressed..
        if (!effectiveSecureDexClassLoader) return;

        Log.d(TAG_MAIN, "Setting up SecureDexClassLoader for profiling..");
        // For the profiling test with SecureDexClassLoader I consider the worst performance scenario and so:
        // 1. The container is in a remote location and must be downloaded first
        // 2. The certificate, as well it's not cached but found at a remote URL and then imported
        // 3. The container in the end is correct so the full signature verification step is performed
        // 4. After the loading operation, the method to wipe out both the certificate and the container is invoked

        Debug.startMethodTracing("SecureDexClassLoader");

        Debug.startMethodTracing("SecureDexFactory Preparation");

        // Create an instance of SecureLoaderFactory..
        // It needs as a parameter a Context object (an Activity is an extension of such a class..)
        SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);

        SecureDexClassLoader mSecureDexClassLoader;

        // Creating the apk paths list (only one path to a remote container in this case)
        String listAPKPaths = "https://dl.dropboxusercontent.com/u/28681922/NasaDailyImageSigned.apk";

        // Filling the associative map to link package name and certificate..
        Map<String, URL> packageNamesToCertMap = new HashMap<String, URL>();
        // 1st Entry: valid REMOTE certificate location
        try {
            packageNamesToCertMap.put("headfirstlab.nasadailyimage", new URL("https://dl.dropboxusercontent.com/u/28681922/test_cert.pem"));
        } catch (MalformedURLException e) {
            // An invalid URL was provided for remote certificate location
            Log.e(TAG_MAIN, "Invalid URL for remote certificate location!");
        }

        // Instantiation of SecureDexClassLoader
        mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(	listAPKPaths,
                null,
                ClassLoader.getSystemClassLoader().getParent(),
                packageNamesToCertMap);

        Debug.stopMethodTracing(); // end of "SecureDexFactory Preparation" section

        try {

            // Attempt to load dynamically the target class..
            Debug.startMethodTracing("Load Operation");
            Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);
            Debug.stopMethodTracing(); // end of "Load Operation" section

            // Immediately wipe out all the cached data (certificate, container)
            Debug.startMethodTracing("Wipe Cached Data");
            mSecureDexClassLoader.wipeOutPrivateAppCachedData(true, true);
            Debug.stopMethodTracing(); // end of "Wipe Cached Data" section

            Debug.stopMethodTracing(); // end of "SecureDexClassLoader" section

            if (loadedClass != null) {

                final Activity NasaDailyActivity = (Activity) loadedClass.newInstance();

                Log.i(TAG_MAIN, "Found valid class: " + loadedClass.getSimpleName() + "; APK path: " + exampleSignedAPKPath + "; Success!");

                toastHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "SecureDexClassLoader was successful! Found activity: " + NasaDailyActivity.getClass().getName(),
                                Toast.LENGTH_SHORT).show();
                    }

                });

            } else {

                Log.w(TAG_MAIN, "This time the chosen class should pass the security checks!");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.w(TAG_MAIN, "Class should be present in the provided path!!");
        } catch (InstantiationException e) {
            Log.w(TAG_MAIN, "Error while instantiating the loaded class!!");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.w(TAG_MAIN, "Error while instantiating the loaded class!!");
            e.printStackTrace();
        }
    }

    protected void setUpSecureDexClassLoader() {

        // First check: this operation can only start after
        // that the proper button has just been pressed..
        if (!effectiveSecureDexClassLoader) return;

        Log.d(TAG_MAIN, "Setting up SecureDexClassLoader..");

        // Create an instance of SecureLoaderFactory..
        // It needs as a parameter a Context object (an Activity is an extension of such a class..)
        SecureLoaderFactory mSecureLoaderFactory = new SecureLoaderFactory(this);

        SecureDexClassLoader mSecureDexClassLoader;

        // Aim: Retrieve NasaDailyImage apk securely

        // 1st Test: Provide a null associative map as a fourth parameter when
        // creating a SecureDexClassLoader. This test case was removed since the
        // latest versions of GNR (>= 1.0.3) will raise a NullPointerException.
        // For more details on this test case, see:
        // http://grab-n-run.readthedocs.org/en/latest/example.html#setupsecuredexclassloader

        // 2nd Test: Fetch the certificate by filling associative map
        // between package name and certificate --> FAIL cause the apk
        // was signed with the DEBUG ANDROID private key, and not with the trusted one.

        try {

            // Filling the associative map to link package names and certificates..
            Map<String, URL> packageNamesToCertMap = new HashMap<>();
            // 1st Entry: valid remote certificate location
            packageNamesToCertMap.put("headfirstlab.nasadailyimage", new URL("https://dl.dropboxusercontent.com/u/28681922/test_cert.pem"));
            // 2nd Entry: not existent certificate -> This link will be enforced to https,
            // but still there is no certificate at the secure endpoint
            packageNamesToCertMap.put("it.polimi.example", new URL("http://google.com/test_cert.pem"));
            // 3rd Entry: misspelled and so invalid URL (missing a p..)
            // packageNamesToCertMap.put("it.polimi.example2", "htt://google.com/test_cert2.pem");
            // 3rd Entry: reverse package name and then inexistent certificate at https://polimi.it/example3/certificate.pem
            packageNamesToCertMap.put("it.polimi.example3", null);

            Log.i(TAG_MAIN, "2nd Test: Evaluate container signed with the Android debug private key.");
            mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(
                    exampleTestAPKPath,
                    null,
                    ClassLoader.getSystemClassLoader(),
                    packageNamesToCertMap);

            try {
                Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);

                if (loadedClass != null) {

                    Log.w(TAG_MAIN, "No class should be loaded!");
                } else {

                    Log.i(TAG_MAIN, "The chosen class is signed but it does not pass " +
                            "the verification against the trusted certificate! CORRECT!");
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.w(TAG_MAIN, "Class should be present in the provided path!!");
            }

            // 3rd Test: Fetch the certificate by filling associative map
            // between package name and certificate --> FAIL cause some of
            // signatures in the container failed the verification process
            // against the trusted developer certificate.
            Log.i(TAG_MAIN, "3rd Test: Evaluate container with some tampered entries..");
            mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(
                    exampleSignedChangedAPKPath,
                    null,
                    ClassLoader.getSystemClassLoader(),
                    packageNamesToCertMap);

            try {
                Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);

                if (loadedClass != null) {

                    Log.w(TAG_MAIN, "No class should be loaded!");
                } else {

                    Log.i(TAG_MAIN, "The chosen class was signed but the " +
                            "apk container was tampered, thus not all the " +
                            "signatures match anymore. Therefore so no class loading! CORRECT!");
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.w(TAG_MAIN, "Class should be present in the provided path!!");
            }

            // 4th Test: Fetch the certificate by filling associative map
            // between package name and certificate --> SUCCESS cause this
            // time the apk was signed and successfully verified against the correct certificate
            Log.i(TAG_MAIN, "4th Test: Fetch the certificate by filling associative map..");

            // Creating the apk paths list (you can mix between remote and local URL)..
            String listAPKPaths =
                    "http://google.com/testApp2.apk:" + exampleSignedAPKPath;

            mSecureDexClassLoader = mSecureLoaderFactory.createDexClassLoader(
                    listAPKPaths,
                    null,
                    ClassLoader.getSystemClassLoader(),
                    packageNamesToCertMap);

            try {
                Class<?> loadedClass = mSecureDexClassLoader.loadClass(classNameInAPK);

                if (loadedClass != null) {

                    final Activity NasaDailyActivity = (Activity) loadedClass.newInstance();

                    Log.i(TAG_MAIN, "Found valid class: " + loadedClass.getSimpleName() + "; APK path: " + exampleSignedAPKPath + "; Success!");

                    toastHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "SecureDexClassLoader was successful! Found activity: " + NasaDailyActivity.getClass().getName(),
                                    Toast.LENGTH_SHORT).show();
                        }

                    });

                } else {

                    Log.w(TAG_MAIN, "This time the chosen class should pass the security verification!");
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.w(TAG_MAIN, "Class should be present in the provided path!!");
            } catch (InstantiationException e) {
                Log.w(TAG_MAIN, "Error while instantiating the loaded class!!");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.w(TAG_MAIN, "Error while instantiating the loaded class!!");
                e.printStackTrace();
            }

            // Remove the cached certificates and the certificate..
            mSecureDexClassLoader.wipeOutPrivateAppCachedData(true, true);
            Log.d(TAG_MAIN, "Cached data of SecureDexClassLoader have been wiped out..");

        } catch (MalformedURLException e) {

            // The previous entries for the map are not necessarily the right ones
            // but still they are not malformed so no exception should be raised.
            Log.e(TAG_MAIN, "A malformed URL was provided for a remote certificate location");

        }
    }

    /**
     * This method is used to set up and manage a DexClassLoader component in
     * order to retrieve a new activity from an .apk, which has been
     * already downloaded and installed on the mobile device.
     * If everything works fine, it will instantiate the main activity of
     * this .apk.
     */
    protected void setUpDexClassLoader() {

        // First check: this operation can only start after
        // that the proper button has just been pressed..
        if (!effectiveDexClassLoader) return;

        Log.d(TAG_MAIN, "Setting up DexClassLoader..");

        if (PROFILING_ON) Debug.startMethodTracing("DexClassLoader");

        File dexOutputDir = getDir("dex", MODE_PRIVATE);
        DexClassLoader mDexClassLoader = new DexClassLoader(
                exampleSignedAPKPath,
                dexOutputDir.getAbsolutePath(),
                null,
                ClassLoader.getSystemClassLoader());

        try {

            // TODO(lfalsina)
            // Use reflection to enforce the class loader of this activity
            // to be the DexClassLoader one instantiated in this function.
            // Reference: http://blog.pentests.pl/2015/02/android-dynamic-activities.html

            // Load NasaDailyImage Main Activity..
            Class<?> loadedClass = mDexClassLoader.loadClass(classNameInAPK);

            if (PROFILING_ON) Debug.stopMethodTracing(); // end of "DexClassLoader" trace

            final Activity NasaDailyActivity = (Activity) loadedClass.newInstance();

            // Note that in this case loading class operation was performed even if the APK which contains
            // the target class was signed just with the Android Debug key. This operation would have failed
            // if SecureDexClassLoader would have been used in stead..
            Log.i(TAG_MAIN, "Found class: " + loadedClass.getSimpleName() + "; APK path: " + exampleSignedAPKPath);

            toastHandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(
                            MainActivity.this,
                            "DexClassLoader was successful! Found activity: " + NasaDailyActivity.getClass().getName(),
                            Toast.LENGTH_SHORT).show();
                }

            });

            // An intent is defined to start the new loaded activity.
            //Intent transitionIntent = new Intent(this, loadedClass);
            //transitionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //this.startActivity(transitionIntent);

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
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
