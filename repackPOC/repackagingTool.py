import sys, argparse, shutil, os

# This variable matches the local folder in the repo 
# where the submodule of Androguard is located
androguard_location = 'androguard/' 
sys.path.insert(1, androguard_location)

# Modules imported from Androguard.
import androguard.core.bytecodes.jvm as jvm
import androguard.core.bytecodes.dvm as dvm
import androguard.core.bytecodes.apk as apk
import androguard.core.analysis.analysis as analysis

# Library used for download of remote container
import urllib, urlparse, os.path

# Libary used for digest computation
import hashlib, base64, zipfile

# Library used to create grammars and parse files
from pyparsing import *

# Library used to launch and check results on command line
import subprocess

# Library used to parse the Android Manifest.
import xml.etree.ElementTree as ET

# Androguard script which automatizes collection of useful information
# on JAR and APK.
import androlyze

# Grammar elements for smali code (Used for smali parsing)
INTEGER = Word( nums, max = 1 )
ALPHABET = alphanums + '_-'
CLASS_PATH = Combine( Word( alphas ) + ZeroOrMore( "/" + Word( ALPHABET )) + ZeroOrMore( OneOrMore("$") + Word( ALPHABET ) ) )
CLASS_STRING = Group("L" + CLASS_PATH + ";")
RETURN_TYPE = Word( alphas, max = 1 )
OPERAND = Or( RETURN_TYPE + CLASS_STRING )
METHOD_NAME = Or( "<init>" + Word( alphas ))
METHOD_DECL = Group(METHOD_NAME + "(" + ZeroOrMore(OPERAND) + ")" + OPERAND)
NUMBER_EXA = Group("(" + Combine( "0x" + Word( "0123456789abcdef" )) + ")")
ACCESS_ATTR = Optional(oneOf("private protected public synthetic"))
RESERVED_KEYWORDS = ZeroOrMore(oneOf("final interface abstract enum annotation synthetic"))
VAR = Combine( Word('pv', max = 1) + Word( nums ) ) 

# Required standard permission
required_permissions = ['android.permission.ACCESS_NETWORK_STATE', 'android.permission.INTERNET', 'android.permission.READ_EXTERNAL_STORAGE']

# Output code for subprogramms
SUCCESS = 0
FAILURE = 1

# Apktool lib path
APKTOOL_JAR = "libs" + os.sep + "apktool.jar"

def isAValidContainer(containerPath, onlyAPKallowed = False):

	if containerPath:

		if os.path.exists(containerPath) and os.path.isfile(containerPath):
			# Try to open this as an APK container..
			try:
				a = apk.APK(containerPath)

				if a.is_valid_APK():
					return True
			except zipfile.BadZipfile:
				# This is not a valid APK container..
				pass

			if not onlyAPKallowed:
				# Try to open this as a JAR container..
				try:
					j = jvm.JAR(containerPath)

					# Check that in this JAR there is the
					# required 'classes.dex' entry.
					if 'classes.dex' in j.zip.namelist():
						return True

				except zipfile.BadZipfile:
					# This is not even a valid JAR container..
					pass

	# If the file reaches this branch, it means that 
	# this is not a valid container
	return False

def isARemoteURL(candidateRemoteURL, onlyHTTPSrequired = False):

	# Parse and check this remote URL
	parsePath = urlparse.urlparse(candidateRemoteURL)

	if onlyHTTPSrequired:
		if parsePath.scheme == "https":
			return True
	else:
		if parsePath.scheme == "http" or parsePath.scheme == "https":
			return True

	return False

def isAValidPackageName(candidatePackageName):

	if candidatePackageName:

		if len(candidatePackageName.split('.')) < 2:
			# Too general package name: at least two fields dot separated..
			return False

		# Each subfield must be not empty..
		for subString in candidatePackageName.split('.'):
			if not subString:
				return False

		# If previous conditions are valid than the package name is OK.
		return True

	return False

def downloadRemoteContainer(containerRemoteURL):

	if containerRemoteURL:
		# Check this remote URL
		if isARemoteURL(containerRemoteURL):

			# Download the remote file and store it in the current directory
			downloadResult = urllib.urlretrieve(containerRemoteURL, os.path.basename(containerRemoteURL))

			if isAValidContainer(downloadResult[0]):

				# Return final local path of the valid container
				print "[In progress] Found a valid container at " + containerRemoteURL
				return downloadResult[0]

			else:

				# Remove this resource and skip it
				print "[Warning] Found an invalid container at " + containerRemoteURL + ". This resource will be skipped!"
				os.remove(downloadResult[0])
				return None

	# If any of the previous steps fail, return None
	print "[Warning] Impossible to retrieve a resource at " + containerRemoteURL
	return None

def extractPackageNamesFromLocalContainer(containerPath):

	if containerPath:

		if os.path.exists(containerPath) and os.path.isfile(containerPath):

			# Initialize package name list
			packageNamesList = list()

			if isAValidContainer(containerPath):

				a = apk.APK(containerPath)
	
				if a.is_valid_APK():
					# In case of an APK it is pretty easy to extract the app package name
					packageNamesList.append(str(a.get_package()))
					return packageNamesList

				else:
					# In case of a JAR at first retrieve the dex translation of the classes
					d = dvm.DalvikVMFormat(a.get_dex())

					# Then extract the list of the full class names
					classNamesList = d.get_classes_names()

					for fullClassName in classNamesList:

						# Retrieve simple class name and package name
						simpleClassName = os.path.basename(fullClassName)
						packageName = fullClassName.lstrip("L").replace("/", ".").rstrip(simpleClassName).rstrip(".")
				
						# Filter out all packages which comes from standard Android libraries
						if not(packageName.startswith("android")) and len(packageName.split(".")) > 1:
							packageNamesList.append(packageName)

					# Remove duplicates from this set and order it in a list
					packageNamesList = list(sorted(set(packageNamesList)))

					# Analyze couples of adjacent package names
					counter = 0

					while (counter < len(packageNamesList) - 1):

						couple = list([packageNamesList[counter], packageNamesList[counter + 1]])
						common = os.path.commonprefix(couple)

						if not(common) or len(common.split(".")) <= 1:
							# There is no common path among the two packages
							# so the first one is valid
							counter = counter + 1

						else:
							# There is a common prefix so only the shortest among the 
							# two packages should be kept
							# Could also simply be:
							# del packageNamesList[counter + 1]
							# since following strings are always longer in this scenario.. 
							if (len(packageNamesList[counter]) > len(packageNamesList[counter + 1])):
								del packageNamesList[counter]
							else:
								del packageNamesList[counter + 1]

					# Finally the returned the purged and final package list
					return packageNamesList

	# If this branch is reached than the package extraction failed
	print "[Warning] Impossible to extract package names for " + containerPath
	return None

def computeDigestEncode(filePath):

	if filePath:

		if os.path.exists(filePath) and os.path.isfile(filePath):
				# Initialize sha1 hash object
				sha1 = hashlib.sha1()

				# Try to open local file in read byte mode
				with open(filePath, 'rb') as fileToDigest:

					# Insert the byte of the file to digest
					sha1.update(fileToDigest.read())

					# Recover the base64 safe URL encode of the
					# digested file
					return base64.urlsafe_b64encode(sha1.digest())

	print "[Warning] Impossible to compute digest for " + filePath
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

		print "[In progress] Rebuilding patched APK.."
		rebuildAPKcommand = subprocess.call(["java","-jar", APKTOOL_JAR, "b", decodeDirName])

		if (rebuildAPKcommand == SUCCESS):

			# An APK is expected now in dist folder
			rebuiltAPKpath = decodeDirName + os.sep + "dist" + os.sep + decodeDirName + ".apk"
			
			if os.path.isfile(rebuiltAPKpath):

				# In this case the operation was successful so return the built APK path
				print "[In progress] APK rebuilding was successful."
				return rebuiltAPKpath

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

		#print INTEGER.parseString("1")
		#print CLASS_STRING.parseString("Lcom/example/extractapp/MainActivity;")
		#print METHOD_DECL.parseString("setUpNormal()V")
		#print NUMBER_EXA.parseString("(0x66)")
		#print CLASS_STRING.parseString("Ldalvik/system/DexClassLoader;")

		# Define a grammar to parse line of the input file.
		parsing_format = INTEGER + CLASS_STRING + "->" + METHOD_DECL + NUMBER_EXA + "--->" + CLASS_STRING + "->" + METHOD_DECL

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

def addMissingPermsToAndroidManifest(decodeDirName, missingPerms):
	
	if not decodeDirName or not(os.path.isdir(decodeDirName)):
		print "[Exit] Invalid folder was provided!"
		sys.exit(FAILURE)

	print "[In progress] Adding missing permission to AndroidManifest.xml.."

	androidManifestPath = decodeDirName + os.sep + "AndroidManifest.xml"

	if not os.path.exists(androidManifestPath):
		print "[Exit] Decoded APK does not have an AndroidManifest.xml entry!"
		sys.exit(FAILURE)

	# Generate an XML tree from the Manifest
	tree = ET.parse(androidManifestPath)
	# Get the root entry which is manifest tag
	root = tree.getroot()
	# Add the scheme attibute for namespace
	root.set('xmlns:android', 'http://schemas.android.com/apk/res/android')

	# For each permission add an entry to the manifest
	for perm in missingPerms:

		# Setup a new Element under the root
		newPermNode = ET.Element('uses-permission')
		# And fix the required permission as a property
		newPermNode.set('android:name', perm)
		# Append this node to the root element
		root.insert(0, newPermNode)

	# Finally write the modifications on the original manifest
	tree.write(androidManifestPath, encoding='utf-8')

def addBlankPadding(line):

	padding = ''
	blanks = len(line) - len(line.lstrip())

	padding = padding + blanks * ' '

	return padding

def patchSmaliClasses(decodeDirName, classesWithDynCodeLoad):

	if not decodeDirName or not(os.path.isdir(decodeDirName)):
		print "[Exit] Invalid folder was provided!"
		sys.exit(FAILURE)

	print "[In progress] Patching .smali classes.."

	smaliFolder = decodeDirName + os.sep + "smali"

	firstLineFormat = ".class" + ACCESS_ATTR + RESERVED_KEYWORDS + CLASS_STRING
	repackHandler = "Lit/necst/grabnrun/RepackHandler;"
	labelNumber = 0

	# Analyze all the elements inside the smali folder incrementally.. 
	for root, dirs, files in os.walk(smaliFolder):
		
		if root == smaliFolder:
			# Remove from analysis smali classes in "android" folder
			index = 0
			while(index < len(dirs)):
				if dirs[index] == 'android':
					del dirs[index]
					break
				index = index + 1

		for smaliFile in files:

			smaliFilePath = os.path.join(root, smaliFile)

			# A couple of flags to see if the target file needs
			# to be patched
			mayBeAnActivity = False
			containDynCode = False

			smaliClassName = ''
			superClassName = ''

			with open(smaliFilePath, 'r') as smaliFileDesc:
				
				# Extract class name from first line of the smali file
				# and check whether this is in the classes set
				firstLine = smaliFileDesc.readline()
				#print firstLine
				tokens = firstLineFormat.parseString( firstLine )
				#print tokens
				smaliClassName = tokens[len(tokens) - 1][1]
				if smaliClassName in classesWithDynCodeLoad:
					containDynCode = True

				# Extract the superclass name and if it ends with Activity then mark
				# this class as a possible activity [Heuristic!!!]
				secondLine = smaliFileDesc.readline()
				superClassName = secondLine.lstrip(".super L").rstrip().rstrip(";")
				# print secondLine
				if superClassName.endswith("Activity"):
					mayBeAnActivity = True

			# Check whether one of the two flags was raised..
			if mayBeAnActivity or containDynCode:

				# This smali file needs to be patched
				print "[In progress] Patching " + smaliFilePath + ".."

				fileName, extension = os.path.splitext(smaliFilePath)
				smaliPatchedFilePath = fileName + "Patch" + extension

				with open(smaliFilePath, 'r') as original:
					with open(smaliPatchedFilePath, 'w') as patched:

						# Different grammars for the relevant lines to patch
						onCreateFormat = "invoke-super {" + VAR + "," + VAR + "}," + "L" + superClassName + ";->onCreate(Landroid/os/Bundle;)V"
						newInstanceDexClassLoader = "new-instance" + VAR + ", Ldalvik/system/DexClassLoader;"
						initDexClassLoader = "invoke-direct {" + delimitedList(VAR, delim = ',') + "}, Ldalvik/system/DexClassLoader;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V"
						loadClass = "invoke-virtual {" + VAR + "," + VAR + "}, Ldalvik/system/DexClassLoader;->loadClass(Ljava/lang/String;)Ljava/lang/Class;"
						movResAfterLoad = "move-result-object" + VAR

						# Flag variable..
						afterDynLoadInstruction = False

						# Read each line in the original file and if necessary
						# write changes in the patched version..
						for line in original:

							if afterDynLoadInstruction:
								
								if line.strip():
									afterDynLoadInstruction = False
									try:
										# Read and patch also next line since the move result 
										# must be integrated with a check on not null values 
										tokens = movResAfterLoad.parseString(line)
										patched.write(line + "\n")
										secGNRlabel = ":sec_checkgnr_" + str(labelNumber)
										labelNumber = labelNumber + 1
										# Write down sanity check on the final object..
										padding = addBlankPadding(line)
										patched.write(padding + "if-nez " + tokens[1] + ", " + secGNRlabel + "\n")
										patched.write(padding + "invoke-static {}, " + repackHandler + "->raiseSecurityException()V" + "\n")
										patched.write(padding + secGNRlabel + "\n")
										print "Added sanity check"
										continue
									except ParseException:
										print "[Warning] A dynamically loaded instance was not assigned to any variable!"
										#sys.exit(FAILURE)

								else:
									patched.write(line)

							try:
								tokens = onCreateFormat.parseString(line)
								patched.write(line + "\n")
								onCreatePatch = addBlankPadding(line) + "invoke-static {" + tokens[1] + "}, " + repackHandler + "->enqueRunningActivity(Landroid/app/Activity;)V" + "\n"
								patched.write(onCreatePatch)
								print onCreatePatch
								continue
							except ParseException:
								pass

							if 'Ldalvik/system/DexClassLoader;' in line:

								try:
									tokens = newInstanceDexClassLoader.parseString(line)
									# If no exception is raised do not write anything back
									# since this line should be erased
									print "Line removed"
									continue
								except ParseException:
									pass

								try:
									tokens = initDexClassLoader.parseString(line)
									if len(tokens) != 7:
										print "[Exit] Unexpected line while patching smali file!"
										sys.exit(FAILURE)

									initSecDex = addBlankPadding(line) + "invoke-static {" + tokens[2] + ", " + tokens[3] + ", " + tokens[4] + ", " + tokens[5] + "}, " + repackHandler + "->generateSecureDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)Lit/necst/grabnrun/SecureDexClassLoader;" + "\n"
									moveRes = addBlankPadding(line) + "move-result-object " + tokens[1] + "\n"
									patched.write(initSecDex + "\n")
									patched.write(moveRes)
									print initSecDex
									print moveRes
									continue
								except ParseException:
									pass

								try:
									loadClass.parseString(line)
									loadSecLine = line.replace('Ldalvik/system/DexClassLoader;', 'Lit/necst/grabnrun/SecureDexClassLoader;')
									patched.write(loadSecLine)
									# Inform that the next instruction must be a move-result-object..
									afterDynLoadInstruction = True
									print loadSecLine
									continue
								except ParseException:
									pass

								# All the particular cases have been handled. If the line reaches this part of the code,
								# simply replace standard DexClassLoader string..
								patchedLine = line.replace('Ldalvik/system/DexClassLoader;', 'Lit/necst/grabnrun/SecureDexClassLoader;')
								patched.write(patchedLine)
								print patchedLine

							else:
								# Those lines which reach this flow are the ones that do not match any relevant case.
								# Simply copy them in the patched file as they were in the original version.
								patched.write(line)

						# Here the process of patching the smali file is finished
						print "[In progress] " + smaliFilePath + " has been patched.."						

				# Erase the old copy and rename the patched version to the original name
				os.remove(smaliFilePath)
				os.rename(smaliPatchedFilePath, smaliFilePath)


def setUpRepackHandler(decodeDirName, hasStaticAssociativeMap, entriesDictionary):

	if not decodeDirName or not(os.path.isdir(decodeDirName)):
		print "[Exit] Invalid folder was provided!"
		sys.exit(FAILURE)

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

	print "[In progress] Creating RepackHandler.smali class"

	repackHandlerFilePath = pathGNRfolder + os.sep + "RepackHandler.smali"
	repackHandlerTailPath = pathGNRfolder + os.sep + "RepackHandlerTail.smali"

	with open(repackHandlerFilePath, 'a') as repackHandlerFile:

		if hasStaticAssociativeMap:
			# Simply consider all the entries as tuples of package names
			# and certificate remote URL. Populate the final map accordingly..
			for packageName in entriesDictionary.keys():

				# Get the certificate remote URL.
				remoteCertificateURL = entriesDictionary[packageName]

				if isAValidPackageName(packageName):
					if isARemoteURL(remoteCertificateURL, True):
						linkPackageNameToCertURL(repackHandlerFile, packageName, entriesDictionary[packageName])
					else:
						print "[Warning] Found an invalid remote certificate URL " + remoteCertificateURL + ". The linked package name will be skipped!"
				else:
					print "[Warning] Found an invalid package name " + packageName + ". It will be skipped!"

			# Finally set the hasStaticAssociativeMap attribute to True
			setHasStaticAssociativeMapBool(repackHandlerFile, True)

		else:

			# Flag initialization variable for package names set
			needToReinitializePackageNameSet = False

			# Each entry contains a container and a remote certificate URL that should be used
			# to validate all classes in its package names
			for containerPath in entriesDictionary.keys():

				# Get the certificate remote URL.
				remoteCertificateURL = entriesDictionary[containerPath]

				# Check that the remote URL of the certificate is valid..
				if isARemoteURL(remoteCertificateURL, True):
					
					# Check whether this entry is the special one used for default case
					if containerPath == 'default':
						linkPackageNameToCertURL(repackHandlerFile, containerPath, remoteCertificateURL)

					else:
						# Check whether this container path is a remote URL..
						if isARemoteURL(containerPath):

							# If remote, download the remote container at first..
							localContPath = downloadRemoteContainer(containerPath)

							if localContPath:

								# Make a write iteration to RepackHandler smali file
								makeOneWriteIteration(repackHandlerFile, needToReinitializePackageNameSet, localContPath, remoteCertificateURL, containerPath)
							
								# Reinitialize always after the first population of the set.
								if not needToReinitializePackageNameSet:
									needToReinitializePackageNameSet = True

								# Remove downloaded container at local path
								os.remove(localContPath)

						else:
							# Check that the local file is a valid container
							if isAValidContainer(containerPath):

								# Make a write iteration to RepackHandler smali file
								makeOneWriteIteration(repackHandlerFile, needToReinitializePackageNameSet, containerPath, remoteCertificateURL)
							
								# Reinitialize always after the first population of the set.
								if not needToReinitializePackageNameSet:
									needToReinitializePackageNameSet = True
						
							else:
								print "[Warning] Found an invalid container " + containerPath + ". This resource will be skipped!"

				else:
					print "[Warning] Found an invalid remote certificate URL " + remoteCertificateURL + ". The linked resource will be skipped!"

			# Finally set the hasStaticAssociativeMap attribute to False
			setHasStaticAssociativeMapBool(repackHandlerFile, False)

		# In the end append the content of RepackHandlerTail.smali to RepackHandler.smali
		with open(repackHandlerTailPath, 'r') as repackHandlerTail:

			# Copy the whole content..
			linesToAppend = repackHandlerTail.read()
			# ..and append it.
			repackHandlerFile.write(linesToAppend)

		# Finally remove the helper tail file
		os.remove(repackHandlerTailPath)

	print "[In progress] RepackHandler.smali class successfully created."

def makeOneWriteIteration(repackHandlerFile, needToReinitializePackageNameSet, containerPath, remoteCertificateURL, remoteContReference = ''):

	# A valid container was found. At first extract the package names
	# list from this container
	packageNamesList = extractPackageNamesFromLocalContainer(containerPath)

	if needToReinitializePackageNameSet:
		reinitializeSet(repackHandlerFile)

	# Insert all package names in a set in the smali class. 
	for packageName in packageNamesList:
		insertPackageNameInSet(repackHandlerFile, packageName)

	# Link the extracted package names to its remote container, if provided..
	if remoteContReference:  
		linkContainerToCurrentPackageNameSet(repackHandlerFile, remoteContReference)

	# Compute digest on the downloaded local copy of the file.
	containerDigest = computeDigestEncode(containerPath)

	if containerDigest:
		# Link the extracted package names to the digest as well.
		linkContainerToCurrentPackageNameSet(repackHandlerFile, containerDigest)

	# Finally associate each package name to the remote URL of the certificate
	for packageName in packageNamesList:
		linkPackageNameToCertURL(repackHandlerFile, packageName, remoteCertificateURL)

def reinitializeSet(repackHandlerFile):

	repackHandlerFile.write('    new-instance v1, Ljava/util/HashSet;' + 2 * '\n')
	repackHandlerFile.write('    .end local v1    # "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"' + '\n')
	repackHandlerFile.write('    invoke-direct {v1}, Ljava/util/HashSet;-><init>()V' + 2 * '\n')
	repackHandlerFile.write('    .restart local v1    # "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"' + 2 * '\n')

	print '[DEBUG] Set of package names reinitialized.'

def insertPackageNameInSet(repackHandlerFile, packageName):

	repackHandlerFile.write('    const-string v2, "' + packageName + '"' + 2 * '\n')
	repackHandlerFile.write('    invoke-interface {v1, v2}, Ljava/util/Set;->add(Ljava/lang/Object;)Z' + 2 * '\n')

	print '[DEBUG] Added package name ' + packageName + ' to set.'

def linkContainerToCurrentPackageNameSet(repackHandlerFile, container):

	repackHandlerFile.write('    sget-object v2, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;' + 2 * '\n')
	repackHandlerFile.write('    const-string v3, "' + container + '"' + 2 * '\n')
	repackHandlerFile.write('    invoke-interface {v2, v3, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;' + 2 * '\n')

	print '[DEBUG] Linked container ' + container + ' to current set of package names.'	

def linkPackageNameToCertURL(repackHandlerFile, packageName, certificateURL):

	repackHandlerFile.write('    sget-object v2, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;' + 2 * '\n')
	repackHandlerFile.write('    const-string v3, "' + packageName + '"' + 2 * '\n')
	repackHandlerFile.write('    const-string v4, "' + certificateURL + '"' + 2 * '\n')
	repackHandlerFile.write('    invoke-interface {v2, v3, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;' + 2 * '\n')

	print '[DEBUG] Linked package name ' + packageName + ' to remote certificate at ' + certificateURL

def setHasStaticAssociativeMapBool(repackHandlerFile, boolValue):

	if boolValue:
		repackHandlerFile.write('    const/4 v2, 0x1' + 2 * '\n')
	else:
		repackHandlerFile.write('    const/4 v2, 0x0' + 2 * '\n')

	print '[DEBUG] Set attribute hasStaticAssociativeMap to ' + str(boolValue)

def main(argv):

	# An argument parser is set up to handle user command line input.
	parser = argparse.ArgumentParser(description='Process an APK to automatically port it to use GNR secure API for dynamic code loading.')
	parser.add_argument('-p', '--preference-file', metavar = 'local file path', help='local path pointing to the preference file used to choose how the repackaging operation will be handled. If missing, a GUI will be shown to configure the required parameters')
	parser.add_argument('-k', '--keep-resources', action='store_true', help='avoid temporary files being erased at the end of the process')

	args = parser.parse_args()

	# Default path for the preference file in case that the GUI is used to
	# select preferences.
	userPrefsFilePath = os.curdir + os.sep + "preferences"

	if args.preference_file:

		# The user provide the location of a preference file so check that this
		# file actually exists..
		userPrefsFilePath = args.preference_file

		if os.path.exists(userPrefsFilePath) and os.path.isfile(userPrefsFilePath):
			# The provided preference path seems acceptable. Start the program
			print "[Start] User provided a preference file located at " + userPrefsFilePath
		else:
			# The provided preference path is invalid
			print "[Error] No preference file was found at the provided location! Aborting.."
			sys.exit(FAILURE)

	else:

		# Invoke the Java GUI to recover user preferences on the 
		# repackaging operation
		print "[Start] A GUI is shown to select repackaging options."
		inputSelector = subprocess.call(["java", "-jar", "libs" + os.sep + "RepackInputSelector.jar"])


		if inputSelector != SUCCESS:
			# User aborted the process. No repackaging will be done
			print "[Exit] User aborted the parameter selection steps. No repackaging.."
			sys.exit(FAILURE)

	# User completes successfully the preferences 
	# selection step. Now the repackaging operation starts..
	rebuiltAPK = ""

	with open(userPrefsFilePath, 'r') as userPrefsFile:
		
		# First line must be the apk path.
		apkPath = userPrefsFile.readline().rstrip();

		# Check that this path is actually pointing to
		# a valid APK container. 
		if isAValidContainer(apkPath, True):

			# Start Androguard analysis on this APK
			# Here we also check whether this APK needs to be patched
			missingPerms, classesWithDynCodeLoad = performAnalysis(apkPath)

			# print missingPerms
			# print classesWithDynCodeLoad

			# At first this APK should be decoded with apktool.
			decodeDirName = decodeTargetAPK(apkPath)

			# Then missing permissions, if any, must be added.
			addMissingPermsToAndroidManifest(decodeDirName, missingPerms)

			# Next all extension classes of Activities and classes
			# which uses dynamic code loading must be patched.
			patchSmaliClasses(decodeDirName, classesWithDynCodeLoad)

			# In the end the RepackHandler should be set up
			# depending on user preferences. Here smali classes from GNR
			# library will be copied as well.

			## Retrieve user preferences (first the boolean value)
			hasStaticAssociativeMap = userPrefsFile.readline().rstrip().lower().capitalize();
			
			## Required casting from String to bool value
			if hasStaticAssociativeMap == 'True':
				hasStaticAssociativeMap = True
			else:
				if hasStaticAssociativeMap == 'False':
					hasStaticAssociativeMap = False
				else:
					print "[Error] Invalid format of the preference file! Second line should be a True/False choice. Aborting.."
					if not args.keep_resources:
						shutil.rmtree(decodeDirName)
					sys.exit(FAILURE)

			## Initialize dictionary and add entries in the file to it
			entriesDictionary = {}

			for line in userPrefsFile:

				# Split the line according to the separator..
				subfields = line.split("|")

				# Sanity check on the preference file format
				if len(subfields) != 2:
					print "[Error] Invalid format of the preference file! Aborting.."
					if not args.keep_resources:
						shutil.rmtree(decodeDirName)
					sys.exit(FAILURE)

				entriesDictionary[subfields[0].strip()] = subfields[1].strip()

			#print entriesDictionary

			setUpRepackHandler(decodeDirName, hasStaticAssociativeMap, entriesDictionary)

			# Finally rebuild the APK with the patched resources
			rebuiltAPK = buildRepackagedAPK(decodeDirName)

			# Copy repackaged APK in main folder
			shutil.copy(rebuiltAPK, os.getcwd())

			# Raise success flag
			repackageSuccessful = True

		else:

			# The preference file did not point to a valid APK container
			print "[Error] Invalid format of the preference file! First line should point to a valid APK. Aborting.."
			sys.exit(FAILURE)

	# Clean up of all the resources
	# os.remove(userPrefsFilePath)
	if not args.keep_resources:
		shutil.rmtree(decodeDirName)

	# The repackaging process is finished with no errors :)
	finalPath = os.getcwd() + os.sep + os.path.basename(rebuiltAPK)
	print "[Success] Target APK was successfully patched! Result container can be found at " + finalPath
	sys.exit(SUCCESS)

if __name__ == "__main__":
	main(sys.argv[1:])