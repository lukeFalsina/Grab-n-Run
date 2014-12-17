import sys, argparse, shutil, os
# Customize this variable to match the local folder 
# where Androguard is located on your machine 
androguard_location = '/home/luca/TesiCS/androguard/' 
sys.path.insert(1, androguard_location)

#from androguard.core import *
#from androguard.core.androgen import *
#from androguard.core.androconf import *
#from androguard.core.bytecode import *
#from androguard.core.bytecodes.jvm import *
import androguard.core.bytecodes.dvm as dvm
import androguard.core.bytecodes.apk as apk

import androguard.core.analysis.analysis as analysis
#from androguard.core.analysis.ganalysis import *
#from androguard.core.analysis.risk import *
#from androguard.decompiler.decompiler import *

# Library used for download of remote container
import urllib, urlparse, os.path

# Libary used for digest computation
import hashlib, base64

# Library used to create grammars and parse files
from pyparsing import *

# Library used to launch and check results on command line
import subprocess

# Androguard script which automatizes collection of useful information
# on JAR and APK.
import androlyze

# Store app to patch permissions here
# to_add_permissions = list()

# An helper list which points put classes using standard DexClassLoader
# classesToPatch = list()

# Required standard permission
required_permissions = ['android.permission.ACCESS_NETWORK_STATE', 'android.permission.INTERNET', 'android.permission.READ_EXTERNAL_STORAGE']

# Output code for subprogramms
SUCCESS = 0
FAILURE = 1

# Apktool lib path
APKTOOL_JAR = "libs" + os.sep + "apktool.jar"

def downloadRemoteContainer(containerRemotePath):

	if containerRemotePath:
		# Parse and check this remote URL
		parsePath = urlparse.urlparse(containerRemotePath)

		if 	(parsePath.scheme == "http" or parsePath.scheme == "https") and	(parsePath.path.endswith('.apk') or parsePath.path.endswith('.jar')):

			# Download the remote file and store it in the current directory
			downloadResult = urllib.urlretrieve(containerRemotePath, os.path.basename(containerRemotePath))

			# Return final local path
			return downloadResult[0]

	# If any of the previous steps fail return None
	print "[Warning] Impossible to retrieve resource at " + containerRemotePath + ". This resource will be skipped!"
	return None

def extractPackageNameFromLocalContainer(containerPath):

	if containerPath:

		# Extract and check container extension:
		fileName, fileExtension = os.path.splitext(containerPath)

		packageNameSet = list()

		if fileExtension == ".apk":

			# In case of an APK it is pretty easy to extract package names
			a = apk.APK(containerPath)
			packageNameSet.append(str(a.get_package()))
			return packageNameSet

		if fileExtension == ".jar":

			# In case of a JAR at first retrieve the classes.dex entry
			a = apk.APK(containerPath)
			d = dvm.DalvikVMFormat(a.get_dex())

			# Then extract the list of the full class names
			classNamesList = d.get_classes_names()

			for fullClassName in classNamesList:

				# Retrieve simple class name and package name
				simpleClassName = os.path.basename(fullClassName)
				packageName = fullClassName.lstrip("L").replace("/", ".").rstrip(simpleClassName).rstrip(".")
				
				# Filter out all packages which comes from standard Android libraries
				if not(packageName.startswith("android")) and len(packageName.split(".")) > 1:
					packageNameSet.append(packageName)

			# Remove duplicates from this set and order it in a list
			packageNameSet = list(sorted(set(packageNameSet)))

			# Analyze couples of package names
			counter = 0

			while (counter < len(packageNameSet) - 1):

				couple = list([packageNameSet[counter], packageNameSet[counter + 1]])
				common = os.path.commonprefix(couple)

				if not(common) or len(common.split(".")) <= 1:
					# There is no common path among the two packages
					# so the first one is valid
					counter = counter + 1

				else:
					# There is a common prefix so only the shortest among the 
					# two packages should be kept
					# Could also simply be:
					# del packageNameSet[counter + 1]
					# since following strings are always longer in this scenario.. 
					if (len(packageNameSet[counter]) > len(packageNameSet[counter + 1])):
						del packageNameSet[counter]
					else:
						del packageNameSet[counter + 1]

			# Finally the returned the purged and final package list
			return packageNameSet

	return None

def computeDigestEncode(containerPath):

	if containerPath:
		# Initialize sha1 hash object
		sha1 = hashlib.sha1()

		# Try to open local file in read byte mode
		with open(containerPath, 'rb') as fileToDigest:

			# Insert the byte of the file to digest
			sha1.update(fileToDigest.read())

			# Recover the base64 safe URL encode of the
			# digested file
			return base64.urlsafe_b64encode(sha1.digest())

			# Finally look for the extension of the initial file
			# fileName, fileExtension = os.path.splitext(containerPath)

			#if fileExtension:
				#return digestString + "\n" + fileExtension

	return None

def decodeTargetAPK(apkPath):

	if apkPath:

		print "[In progress] Decode target APK.."
		decodeAPK = subprocess.call(["java","-jar", APKTOOL_JAR, "d", "-f", apkPath])

		decodeDirName = os.path.splitext(os.path.basename(apkPath))[0]

		if (decodeAPK == SUCCESS):

			# Check that the actual directory exists after this operation
			if (os.path.isdir(decodeDirName)):

				# In this case the operation was successful so return the directory name
				print "[In progress] APK decoding was successful."
				return decodeDirName

		# Here the decode of the APK fails so remove any partial files generated
		# during the APK decoding.
		shutil.rmtree(decodeDirName)
		#subprocess.call(["rm", "-rf", decodeDirName])

	print "[Exit] An error occured while decoding target APK."
	sys.exit(FAILURE)

def buildRepackagedAPK(decodeDirName):

	if decodeDirName and os.path.isdir(decodeDirName):

		print "[In progress] Rebuild patched APK.."
		rebuildAPK = subprocess.call(["java","-jar", APKTOOL_JAR, "b", decodeDirName])

		if (rebuildAPK == SUCCESS):

			# An APK is expected now in dist folder
			rebuiltAPK = decodeDirName + os.sep + "dist" + os.sep + decodeDirName + ".apk"
			
			if os.path.isfile(rebuiltAPK):

				# In this case the operation was successful so return the built APK path
				print "[In progress] APK rebuilding was successful."
				return rebuildAPK

	print "[Exit] An error occured while rebuilding APK from patched resources."
	sys.exit(FAILURE)

def performAnalysis(apkPath):

	# Perform the analysis by recalling public method of androlyze.py
	a, d, dx = androlyze.AnalyzeAPK(apkPath)

	if not a.is_valid_APK():
		print "[Exit] The selected resource is not a valid APK!"
		return sys.exit(FAILURE)

	if not analysis.is_dyn_code(dx):
		print "[Exit] No DexClassLoader use in this APK and so there is nothing to patch!"
		return sys.exit(SUCCESS)

	print "[In progress] Analyze target APK.."

	# Store app permissions (used later on while patching Android Manifest)
	app_permissions = set(a.get_permissions())

	#app_permissions.add('android.permission.ACCESS_NETWORK_STATE')
	#app_permissions.add('android.permission.BLABLABLA')

	# Reference to global variable
	missing_permissions = list()

	for current_perm in required_permissions:
		if not(current_perm in app_permissions):
			missing_permissions.append(current_perm)

	#print missing_permissions

	# Flag variable for later use
	dynamicCallsWhereTraced = False

	# Save a reference for standard output
	stdout = sys.stdout

	# Redirect output to an helper variable
	# sys.stdout = open('./dynamicCalls', 'w')
	dynamicCallsFilePath = os.curdir + os.sep + "dynamicCalls"

	with open(dynamicCallsFilePath, 'w') as dynamicCallsFile:
		# Redirect output to an helper variable
		sys.stdout = dynamicCallsFile

		# Highlight dynamic calls linked to a DexClassLoader.
		analysis.show_DynCode(dx)

		# Set back usual stdout
		sys.stdout = stdout

	with open(dynamicCallsFilePath, 'r') as dynamicCallsFile:
		# Define a grammar to parse line of the input file.
		integer = Word( nums, max = 1 )
		path = Combine( Word( alphas ) + OneOrMore( "/" + Word( alphas )) )
		class_string = Group("L" + path + ";")
		return_type = Word("VZ", max = 1)
		operand = Or( return_type + class_string )
		method_name = Or( "<init>" + Word( alphas ))
		method_decl = Group(method_name + "(" + ZeroOrMore(operand) + ")" + operand)
		number_exa = Group("(" + Combine( "0x" + Word( "0123456789abcdef" )) + ")")

		#print integer.parseString("1")
		#print class_string.parseString("Lcom/example/extractapp/MainActivity;")
		#print method_decl.parseString("setUpNormal()V")
		#print number_exa.parseString("(0x66)")
		#print class_string.parseString("Ldalvik/system/DexClassLoader;")

		parsing_format = integer + class_string + "->" + method_decl + number_exa + "--->" + class_string + "->" + method_decl

		# Reference to global variable
		classesWithDynCodeLoad = list()

		for line in dynamicCallsFile:
			# print line
			tokens = parsing_format.parseString( line )

			# This is a sort specific parsing constant(magic numbers)..
			className = tokens[1][1]

			# Extract only name of classes to patch which are not the ones of Grab'n Run
			if className != "it/necst/grabnrun/SecureDexClassLoader":
				classesWithDynCodeLoad.append(className)

		# In the end remove duplicates from this list (use a set)
		classesWithDynCodeLoad = set(classesWithDynCodeLoad)

		# print classesWithDynCodeLoad

		# Raise flag variable
		print "[In progress] Dynamic calls have been detected.."
		dynamicCallsWhereTraced = True

	# Now the helper file should be closed and erased.
	os.remove(dynamicCallsFilePath)

	if dynamicCallsWhereTraced:
		# This APK should be patched!
		return missing_permissions, classesWithDynCodeLoad

	# Something went wrong..
	print "[Exit] Dynamic calls have not been detected!"
	return sys.exit(FAILURE)

def addMissingPermsToAndroidManifest(decodeDirName):

	print "[In progress] Adding missing permission to AndroidManifest.xml.."

def patchSmaliClasses(decodeDirName, classesWithDynCodeLoad):

	print "[In progress] Patching .smali classes.."

def setUpRepackHandler(decodeDirName, hasStaticAssociativeMap, entriesDictionary):

	if not decodeDirName or not(os.path.isdir(decodeDirName)):
		print "[Exit] Invalid folder was provided!"

	print "[In progress] Copying Grab'n Run .smali classes"
	pathGNRparentFolder = decodeDirName + os.sep + "smali" + os.sep + "it" + os.sep + "necst"
	pathGNRfolder = pathGNRparentFolder + os.sep + "grabnrun"

	# Create parent folder if necessry..
	if not os.path.exists(pathGNRparentFolder):
		os.makedirs(pathGNRparentFolder)

	# Remove already present copy of GNR library, if any..
	if os.path.exists(pathGNRfolder):
		shutil.rmtree(pathGNRfolder)

	resourceGNRfolder = "smaliRes" + os.sep + "grabnrun"

	# Copy from the smaliRes folder GNR smali classes
	shutil.copytree(resourceGNRfolder, pathGNRfolder)

	print "[In progress] Creating RepackHandler .smali class"

def test(apkPath):

	print extractPackageNameFromLocalContainer(apkPath)

	print computeDigestEncode(apkPath)

	remoteContainerURL = "https://dl.dropboxusercontent.com/u/28681922/jsoup-dex-1.8.1.jar"

	downloadLocation = downloadRemoteContainer(remoteContainerURL)

	print downloadLocation

	print extractPackageNameFromLocalContainer(downloadLocation)

	print computeDigestEncode(downloadLocation)

def main(argv):

	# Invoke the Java GUI to recover user preferences on the 
	# repackaging operation
	print "[Start] A GUI is shown to select repackaging options."
	inputSelector = subprocess.call(["java", "-jar", "libs" + os.sep + "RepackInputSelector.jar"])

	userPrefsFilePath = os.curdir + os.sep + "preferences"

	if inputSelector == SUCCESS:
		# User completes successfully the preferences 
		# selection step. Now the repackaging operation starts..
		repackageSuccessful = False
		rebuiltAPK = ""

		with open(userPrefsFilePath, 'r') as userPrefsFile:
		
			# First line is the apk path.
			apkPath = userPrefsFile.readline().rstrip();

			# Check that the apk path is not null and ends 
			# with an APK extension.
			if apkPath and apkPath.endswith('.apk'):

				# Start Androguard analysis on this APK
				# Here we also check whether this APK needs to be patched
				missingPerms, classesWithDynCodeLoad = performAnalysis(apkPath)

				print missingPerms
				print classesWithDynCodeLoad
				
				# test subroutine
				# test(apkPath)

				# At first this APK should be decoded with apktool.
				decodeDirName = decodeTargetAPK(apkPath)

				# Then missing permissions, if any, must be added.
				addMissingPermsToAndroidManifest(decodeDirName)

				# Next all extension classes of Activities and classes
				# which uses dynamic code loading must be patched.
				patchSmaliClasses(decodeDirName, classesWithDynCodeLoad)

				# In the end the RepackHandler should be set up
				# depending on user preferences. Here are also smali classes
				# from GNR library will be copied.

				## Retrieve user preferences (first the boolean value)
				hasStaticAssociativeMap = userPrefsFile.readline().rstrip().lower().capitalize();
				print hasStaticAssociativeMap

				## Initialize dictionary and add entries in the file to it
				entriesDictionary = {}

				for line in userPrefsFile:

					# Split the line according to the separator..
					subfields = line.split("|")

					# Sanity check on the preference file format
					if len(subfields) != 2:
						print "[Error] Invalid format of the preference file! Aborting.."
						shutil.rmtree(decodeDirName)
						sys.exit(FAILURE)

					entriesDictionary[subfields[0].strip()] = subfields[1].strip()

				print entriesDictionary

				setUpRepackHandler(decodeDirName, hasStaticAssociativeMap, entriesDictionary)

				# Finally rebuild the APK with the patched resources
				rebuiltAPK = buildRepackagedAPK(decodeDirName)

				# Copy repackaged APK in main folder
				shutil.copy(rebuiltAPK, os.getcwd())

				# Raise success flag
				repackageSuccessful = True

		if repackageSuccessful:

			# Clean up of all the resources
			os.remove(userPrefsFilePath)
			# shutil.rmtree(decodeDirName)

			# The repackaging process is finished with no errors :)
			finalPath = os.getcwd() + os.sep +  + os.path.basename(rebuiltAPK)
			print "[Success] Target APK was successfully patched! Result container can be found at " + finalPath
			sys.exit(SUCCESS)

		else:
			print "[Exit] No preference file was found!"
			sys.exit(FAILURE)	

	else:
		# User aborted the process. No repackaging will be done
		print "[Exit] User aborted the parameter selection steps. No repackaging.."
		sys.exit(FAILURE)

	# parser = argparse.ArgumentParser(description='Process an input APK to generate information related to dynamic code loading')
	# parser.add_argument('apkPath', metavar='apk_path', help='local path to the APK file to analyze')

	# args = parser.parse_args()

if __name__ == "__main__":
	main(sys.argv[1:])