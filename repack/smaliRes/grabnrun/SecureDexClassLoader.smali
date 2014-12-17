.class public Lit/necst/grabnrun/SecureDexClassLoader;
.super Ljava/lang/Object;
.source "SecureDexClassLoader.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;
    }
.end annotation


# static fields
.field private static final CERTIFICATE_DIR:Ljava/lang/String; = "valid_certs"

.field private static final KEEP_ALIVE_TIME_UNIT:Ljava/util/concurrent/TimeUnit;

.field private static final MINIMUM_NUMBER_OF_CONTAINERS_FOR_CONCURRENT_VERIFICATION:I = 0x2

.field private static final TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;


# instance fields
.field private certificateFactory:Ljava/security/cert/CertificateFactory;

.field private certificateFolder:Ljava/io/File;

.field private hasBeenWipedOut:Z

.field private lazyAlreadyVerifiedPackageNameSet:Ljava/util/Set;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private mDexClassLoader:Ldalvik/system/DexClassLoader;

.field private mFileDownloader:Lit/necst/grabnrun/FileDownloader;

.field private mPackageManager:Landroid/content/pm/PackageManager;

.field private mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

.field private packageNameToCertificateMap:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;"
        }
    .end annotation
.end field

.field private packageNameToContainerPathMap:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private performLazyEvaluation:Z

.field private resDownloadFolder:Ljava/io/File;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .prologue
    .line 88
    const-class v0, Lit/necst/grabnrun/SecureDexClassLoader;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    .line 113
    sget-object v0, Ljava/util/concurrent/TimeUnit;->MILLISECONDS:Ljava/util/concurrent/TimeUnit;

    sput-object v0, Lit/necst/grabnrun/SecureDexClassLoader;->KEEP_ALIVE_TIME_UNIT:Ljava/util/concurrent/TimeUnit;

    return-void
.end method

.method constructor <init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Landroid/content/ContextWrapper;Z)V
    .locals 16
    .param p1, "dexPath"    # Ljava/lang/String;
    .param p2, "optimizedDirectory"    # Ljava/lang/String;
    .param p3, "libraryPath"    # Ljava/lang/String;
    .param p4, "parent"    # Ljava/lang/ClassLoader;
    .param p5, "parentContextWrapper"    # Landroid/content/ContextWrapper;
    .param p6, "performLazyEvaluation"    # Z

    .prologue
    .line 134
    invoke-direct/range {p0 .. p0}, Ljava/lang/Object;-><init>()V

    .line 140
    new-instance v10, Ldalvik/system/DexClassLoader;

    move-object/from16 v0, p1

    move-object/from16 v1, p2

    move-object/from16 v2, p3

    move-object/from16 v3, p4

    invoke-direct {v10, v0, v1, v2, v3}, Ldalvik/system/DexClassLoader;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mDexClassLoader:Ldalvik/system/DexClassLoader;

    .line 142
    const-string v10, "valid_certs"

    const/4 v11, 0x0

    move-object/from16 v0, p5

    invoke-virtual {v0, v10, v11}, Landroid/content/ContextWrapper;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v10

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFolder:Ljava/io/File;

    .line 143
    const-string v10, "imported_cont"

    const/4 v11, 0x0

    move-object/from16 v0, p5

    invoke-virtual {v0, v10, v11}, Landroid/content/ContextWrapper;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v10

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->resDownloadFolder:Ljava/io/File;

    .line 146
    invoke-virtual/range {p5 .. p5}, Landroid/content/ContextWrapper;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v10

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageManager:Landroid/content/pm/PackageManager;

    .line 148
    new-instance v10, Lit/necst/grabnrun/FileDownloader;

    move-object/from16 v0, p5

    invoke-direct {v10, v0}, Lit/necst/grabnrun/FileDownloader;-><init>(Landroid/content/ContextWrapper;)V

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mFileDownloader:Lit/necst/grabnrun/FileDownloader;

    .line 150
    const/4 v10, 0x0

    move-object/from16 v0, p0

    iput-boolean v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->hasBeenWipedOut:Z

    .line 152
    move/from16 v0, p6

    move-object/from16 v1, p0

    iput-boolean v0, v1, Lit/necst/grabnrun/SecureDexClassLoader;->performLazyEvaluation:Z

    .line 154
    new-instance v10, Ljava/util/HashSet;

    invoke-direct {v10}, Ljava/util/HashSet;-><init>()V

    invoke-static {v10}, Ljava/util/Collections;->synchronizedSet(Ljava/util/Set;)Ljava/util/Set;

    move-result-object v10

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->lazyAlreadyVerifiedPackageNameSet:Ljava/util/Set;

    .line 156
    new-instance v10, Lit/necst/grabnrun/PackageNameTrie;

    invoke-direct {v10}, Lit/necst/grabnrun/PackageNameTrie;-><init>()V

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    .line 160
    :try_start_0
    const-string v10, "X.509"

    invoke-static {v10}, Ljava/security/cert/CertificateFactory;->getInstance(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;

    move-result-object v10

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFactory:Ljava/security/cert/CertificateFactory;
    :try_end_0
    .catch Ljava/security/cert/CertificateException; {:try_start_0 .. :try_end_0} :catch_0

    .line 166
    :goto_0
    new-instance v10, Ljava/util/LinkedHashMap;

    invoke-direct {v10}, Ljava/util/LinkedHashMap;-><init>()V

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    .line 168
    new-instance v10, Ljava/util/LinkedHashMap;

    invoke-direct {v10}, Ljava/util/LinkedHashMap;-><init>()V

    invoke-static {v10}, Ljava/util/Collections;->synchronizedMap(Ljava/util/Map;)Ljava/util/Map;

    move-result-object v10

    move-object/from16 v0, p0

    iput-object v10, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    .line 172
    sget-object v10, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-static {v10}, Ljava/util/regex/Pattern;->quote(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v10

    move-object/from16 v0, p1

    invoke-virtual {v0, v10}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v8

    .line 174
    .local v8, "pathStrings":[Ljava/lang/String;
    array-length v11, v8

    const/4 v10, 0x0

    :goto_1
    if-lt v10, v11, :cond_0

    .line 202
    return-void

    .line 161
    .end local v8    # "pathStrings":[Ljava/lang/String;
    :catch_0
    move-exception v5

    .line 162
    .local v5, "e":Ljava/security/cert/CertificateException;
    invoke-virtual {v5}, Ljava/security/cert/CertificateException;->printStackTrace()V

    goto :goto_0

    .line 174
    .end local v5    # "e":Ljava/security/cert/CertificateException;
    .restart local v8    # "pathStrings":[Ljava/lang/String;
    :cond_0
    aget-object v4, v8, v10

    .line 178
    .local v4, "currentPath":Ljava/lang/String;
    move-object/from16 v0, p0

    invoke-direct {v0, v4}, Lit/necst/grabnrun/SecureDexClassLoader;->getPackageNamesFromContainerPath(Ljava/lang/String;)Ljava/util/List;

    move-result-object v7

    .line 180
    .local v7, "packageNameList":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    if-eqz v7, :cond_2

    invoke-interface {v7}, Ljava/util/List;->isEmpty()Z

    move-result v12

    if-nez v12, :cond_2

    .line 182
    invoke-interface {v7}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v12

    :cond_1
    :goto_2
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v13

    if-nez v13, :cond_3

    .line 174
    :cond_2
    add-int/lit8 v10, v10, 0x1

    goto :goto_1

    .line 182
    :cond_3
    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Ljava/lang/String;

    .line 185
    .local v6, "packageName":Ljava/lang/String;
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v13, v6, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    .line 188
    .local v9, "previousPath":Ljava/lang/String;
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    invoke-virtual {v13, v6}, Lit/necst/grabnrun/PackageNameTrie;->generateEntriesForPackageName(Ljava/lang/String;)V

    .line 192
    if-eqz v9, :cond_1

    .line 196
    sget-object v13, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "Package Name "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v14, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    const-string v15, " is not unique!\n Previous path: "

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    .line 197
    invoke-virtual {v14, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    const-string v15, ";\n New path: "

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    const-string v15, ";"

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    .line 196
    invoke-static {v13, v14}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_2
.end method

.method static synthetic access$0(Lit/necst/grabnrun/SecureDexClassLoader;Ljava/lang/String;)Ljava/security/cert/X509Certificate;
    .locals 1

    .prologue
    .line 864
    invoke-direct {p0, p1}, Lit/necst/grabnrun/SecureDexClassLoader;->importCertificateFromPackageName(Ljava/lang/String;)Ljava/security/cert/X509Certificate;

    move-result-object v0

    return-object v0
.end method

.method static synthetic access$1(Lit/necst/grabnrun/SecureDexClassLoader;Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z
    .locals 1

    .prologue
    .line 901
    invoke-direct {p0, p1, p2}, Lit/necst/grabnrun/SecureDexClassLoader;->verifyContainerSignatureAgainstCertificate(Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z

    move-result v0

    return v0
.end method

.method private downloadCertificateRemotelyViaHttps(Ljava/lang/String;)Z
    .locals 4
    .param p1, "packageName"    # Ljava/lang/String;

    .prologue
    .line 1184
    iget-object v2, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    invoke-interface {v2, p1}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/net/URL;

    .line 1188
    .local v0, "certificateRemoteURL":Ljava/net/URL;
    new-instance v2, Ljava/lang/StringBuilder;

    iget-object v3, p0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFolder:Ljava/io/File;

    invoke-virtual {v3}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v3

    invoke-static {v3}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v3

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v3, "/"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    const-string v3, ".pem"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    .line 1191
    .local v1, "localCertPath":Ljava/lang/String;
    iget-object v2, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mFileDownloader:Lit/necst/grabnrun/FileDownloader;

    const/4 v3, 0x0

    invoke-virtual {v2, v0, v1, v3}, Lit/necst/grabnrun/FileDownloader;->downloadRemoteUrl(Ljava/net/URL;Ljava/lang/String;Z)Z

    move-result v2

    return v2
.end method

.method private getPackageNamesFromContainerPath(Ljava/lang/String;)Ljava/util/List;
    .locals 20
    .param p1, "containerPath"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            ")",
            "Ljava/util/List",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation

    .prologue
    .line 207
    if-eqz p1, :cond_0

    invoke-virtual/range {p1 .. p1}, Ljava/lang/String;->isEmpty()Z

    move-result v18

    if-eqz v18, :cond_2

    :cond_0
    const/4 v15, 0x0

    .line 324
    :cond_1
    :goto_0
    return-object v15

    .line 210
    :cond_2
    const-string v18, "."

    move-object/from16 v0, p1

    move-object/from16 v1, v18

    invoke-virtual {v0, v1}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v9

    .line 211
    .local v9, "extensionIndex":I
    move-object/from16 v0, p1

    invoke-virtual {v0, v9}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v8

    .line 213
    .local v8, "extension":Ljava/lang/String;
    new-instance v15, Ljava/util/ArrayList;

    invoke-direct {v15}, Ljava/util/ArrayList;-><init>()V

    .line 215
    .local v15, "packageNameList":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    const-string v18, ".apk"

    move-object/from16 v0, v18

    invoke-virtual {v8, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v18

    if-eqz v18, :cond_4

    .line 219
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageManager:Landroid/content/pm/PackageManager;

    move-object/from16 v18, v0

    const/16 v19, 0x0

    move-object/from16 v0, v18

    move-object/from16 v1, p1

    move/from16 v2, v19

    invoke-virtual {v0, v1, v2}, Landroid/content/pm/PackageManager;->getPackageArchiveInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v18

    if-eqz v18, :cond_3

    .line 221
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageManager:Landroid/content/pm/PackageManager;

    move-object/from16 v18, v0

    const/16 v19, 0x0

    move-object/from16 v0, v18

    move-object/from16 v1, p1

    move/from16 v2, v19

    invoke-virtual {v0, v1, v2}, Landroid/content/pm/PackageManager;->getPackageArchiveInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v18

    move-object/from16 v0, v18

    iget-object v0, v0, Landroid/content/pm/PackageInfo;->packageName:Ljava/lang/String;

    move-object/from16 v18, v0

    move-object/from16 v0, v18

    invoke-interface {v15, v0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto :goto_0

    .line 225
    :cond_3
    const/4 v15, 0x0

    goto :goto_0

    .line 228
    :cond_4
    const-string v18, ".jar"

    move-object/from16 v0, v18

    invoke-virtual {v8, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v18

    if-eqz v18, :cond_d

    .line 235
    const/4 v11, 0x0

    .line 236
    .local v11, "isAValidJar":Z
    const/4 v3, 0x0

    .line 241
    .local v3, "containerJar":Ljava/util/jar/JarFile;
    :try_start_0
    new-instance v4, Ljava/util/jar/JarFile;

    move-object/from16 v0, p1

    invoke-direct {v4, v0}, Ljava/util/jar/JarFile;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 244
    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .local v4, "containerJar":Ljava/util/jar/JarFile;
    :try_start_1
    const-string v18, "classes.dex"

    move-object/from16 v0, v18

    invoke-virtual {v4, v0}, Ljava/util/jar/JarFile;->getJarEntry(Ljava/lang/String;)Ljava/util/jar/JarEntry;
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_5
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    move-result-object v18

    if-eqz v18, :cond_5

    .line 245
    const/4 v11, 0x1

    .line 250
    :cond_5
    if-eqz v4, :cond_6

    .line 252
    :try_start_2
    invoke-virtual {v4}, Ljava/util/jar/JarFile;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_3

    .line 258
    :cond_6
    :goto_1
    if-eqz v11, :cond_c

    .line 261
    const/4 v6, 0x0

    .line 266
    .local v6, "dexFile":Ldalvik/system/DexFile;
    new-instance v16, Ljava/util/HashSet;

    invoke-direct/range {v16 .. v16}, Ljava/util/HashSet;-><init>()V

    .line 271
    .local v16, "packageNameSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :try_start_3
    new-instance v18, Ljava/lang/StringBuilder;

    const/16 v19, 0x0

    move-object/from16 v0, p1

    move/from16 v1, v19

    invoke-virtual {v0, v1, v9}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v19

    invoke-static/range {v19 .. v19}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v19

    invoke-direct/range {v18 .. v19}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v19, ".odex"

    invoke-virtual/range {v18 .. v19}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v18

    invoke-virtual/range {v18 .. v18}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v13

    .line 274
    .local v13, "outputDexTempPath":Ljava/lang/String;
    const/16 v18, 0x0

    move-object/from16 v0, p1

    move/from16 v1, v18

    invoke-static {v0, v13, v1}, Ldalvik/system/DexFile;->loadDex(Ljava/lang/String;Ljava/lang/String;I)Ldalvik/system/DexFile;

    move-result-object v6

    .line 276
    invoke-virtual {v6}, Ldalvik/system/DexFile;->entries()Ljava/util/Enumeration;

    move-result-object v5

    .line 278
    .local v5, "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    :cond_7
    :goto_2
    invoke-interface {v5}, Ljava/util/Enumeration;->hasMoreElements()Z

    move-result v18

    if-nez v18, :cond_a

    .line 300
    new-instance v18, Ljava/io/File;

    move-object/from16 v0, v18

    invoke-direct {v0, v13}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual/range {v18 .. v18}, Ljava/io/File;->delete()Z
    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_4

    .line 310
    invoke-interface/range {v16 .. v16}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v17

    .line 312
    .local v17, "packageNameSetIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :goto_3
    invoke-interface/range {v17 .. v17}, Ljava/util/Iterator;->hasNext()Z

    move-result v18

    if-eqz v18, :cond_1

    .line 313
    invoke-interface/range {v17 .. v17}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v18

    check-cast v18, Ljava/lang/String;

    move-object/from16 v0, v18

    invoke-interface {v15, v0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto :goto_3

    .line 247
    .end local v4    # "containerJar":Ljava/util/jar/JarFile;
    .end local v5    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .end local v6    # "dexFile":Ldalvik/system/DexFile;
    .end local v13    # "outputDexTempPath":Ljava/lang/String;
    .end local v16    # "packageNameSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    .end local v17    # "packageNameSetIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v3    # "containerJar":Ljava/util/jar/JarFile;
    :catch_0
    move-exception v7

    .line 250
    .local v7, "e":Ljava/io/IOException;
    :goto_4
    if-eqz v3, :cond_8

    .line 252
    :try_start_4
    invoke-virtual {v3}, Ljava/util/jar/JarFile;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_1

    .line 248
    :cond_8
    :goto_5
    const/4 v15, 0x0

    goto/16 :goto_0

    .line 253
    :catch_1
    move-exception v7

    .line 254
    invoke-virtual {v7}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_5

    .line 249
    .end local v7    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v18

    .line 250
    :goto_6
    if-eqz v3, :cond_9

    .line 252
    :try_start_5
    invoke-virtual {v3}, Ljava/util/jar/JarFile;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_2

    .line 256
    :cond_9
    :goto_7
    throw v18

    .line 253
    :catch_2
    move-exception v7

    .line 254
    .restart local v7    # "e":Ljava/io/IOException;
    invoke-virtual {v7}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_7

    .line 253
    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .end local v7    # "e":Ljava/io/IOException;
    .restart local v4    # "containerJar":Ljava/util/jar/JarFile;
    :catch_3
    move-exception v7

    .line 254
    .restart local v7    # "e":Ljava/io/IOException;
    invoke-virtual {v7}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_1

    .line 281
    .end local v7    # "e":Ljava/io/IOException;
    .restart local v5    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .restart local v6    # "dexFile":Ldalvik/system/DexFile;
    .restart local v13    # "outputDexTempPath":Ljava/lang/String;
    .restart local v16    # "packageNameSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_a
    :try_start_6
    invoke-interface {v5}, Ljava/util/Enumeration;->nextElement()Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    .line 286
    .local v10, "fullClassName":Ljava/lang/String;
    :goto_8
    const-string v18, " "

    move-object/from16 v0, v18

    invoke-virtual {v10, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v18

    if-nez v18, :cond_b

    .line 289
    const-string v18, "."

    move-object/from16 v0, v18

    invoke-virtual {v10, v0}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v12

    .line 291
    .local v12, "lastIndexPackageName":I
    const/16 v18, -0x1

    move/from16 v0, v18

    if-eq v12, v0, :cond_7

    .line 293
    const/16 v18, 0x0

    move/from16 v0, v18

    invoke-virtual {v10, v0, v12}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v14

    .line 294
    .local v14, "packageName":Ljava/lang/String;
    move-object/from16 v0, v16

    invoke-interface {v0, v14}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    goto :goto_2

    .line 302
    .end local v5    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .end local v10    # "fullClassName":Ljava/lang/String;
    .end local v12    # "lastIndexPackageName":I
    .end local v13    # "outputDexTempPath":Ljava/lang/String;
    .end local v14    # "packageName":Ljava/lang/String;
    :catch_4
    move-exception v7

    .line 304
    .restart local v7    # "e":Ljava/io/IOException;
    const/4 v15, 0x0

    goto/16 :goto_0

    .line 287
    .end local v7    # "e":Ljava/io/IOException;
    .restart local v5    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .restart local v10    # "fullClassName":Ljava/lang/String;
    .restart local v13    # "outputDexTempPath":Ljava/lang/String;
    :cond_b
    const/16 v18, 0x1

    invoke-virtual {v10}, Ljava/lang/String;->length()I

    move-result v19

    move/from16 v0, v18

    move/from16 v1, v19

    invoke-virtual {v10, v0, v1}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_4

    move-result-object v10

    goto :goto_8

    .line 319
    .end local v5    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .end local v6    # "dexFile":Ldalvik/system/DexFile;
    .end local v10    # "fullClassName":Ljava/lang/String;
    .end local v13    # "outputDexTempPath":Ljava/lang/String;
    .end local v16    # "packageNameSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_c
    const/4 v15, 0x0

    goto/16 :goto_0

    .line 324
    .end local v4    # "containerJar":Ljava/util/jar/JarFile;
    .end local v11    # "isAValidJar":Z
    :cond_d
    const/4 v15, 0x0

    goto/16 :goto_0

    .line 249
    .restart local v4    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v11    # "isAValidJar":Z
    :catchall_1
    move-exception v18

    move-object v3, v4

    .end local v4    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v3    # "containerJar":Ljava/util/jar/JarFile;
    goto :goto_6

    .line 247
    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v4    # "containerJar":Ljava/util/jar/JarFile;
    :catch_5
    move-exception v7

    move-object v3, v4

    .end local v4    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v3    # "containerJar":Ljava/util/jar/JarFile;
    goto :goto_4
.end method

.method private importCertificateFromAppPrivateDir(Ljava/lang/String;)Ljava/security/cert/X509Certificate;
    .locals 13
    .param p1, "packageName"    # Ljava/lang/String;

    .prologue
    const/4 v12, 0x0

    .line 1100
    iget-object v10, p0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFolder:Ljava/io/File;

    new-instance v11, Lit/necst/grabnrun/CertFileFilter;

    invoke-direct {v11, p1}, Lit/necst/grabnrun/CertFileFilter;-><init>(Ljava/lang/String;)V

    invoke-virtual {v10, v11}, Ljava/io/File;->listFiles(Ljava/io/FileFilter;)[Ljava/io/File;

    move-result-object v4

    .line 1102
    .local v4, "certMatchingFiles":[Ljava/io/File;
    const/4 v9, 0x0

    .line 1104
    .local v9, "verifiedCertificate":Ljava/security/cert/X509Certificate;
    if-eqz v4, :cond_1

    array-length v10, v4

    if-eqz v10, :cond_1

    .line 1107
    const/4 v6, 0x0

    .line 1114
    .local v6, "inStream":Ljava/io/InputStream;
    :try_start_0
    new-instance v7, Ljava/io/FileInputStream;

    const/4 v10, 0x0

    aget-object v10, v4, v10

    invoke-direct {v7, v10}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_1
    .catch Ljava/security/cert/CertificateException; {:try_start_0 .. :try_end_0} :catch_3
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1116
    .end local v6    # "inStream":Ljava/io/InputStream;
    .local v7, "inStream":Ljava/io/InputStream;
    :try_start_1
    iget-object v10, p0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFactory:Ljava/security/cert/CertificateFactory;

    invoke-virtual {v10, v7}, Ljava/security/cert/CertificateFactory;->generateCertificate(Ljava/io/InputStream;)Ljava/security/cert/Certificate;

    move-result-object v10

    move-object v0, v10

    check-cast v0, Ljava/security/cert/X509Certificate;

    move-object v9, v0
    :try_end_1
    .catch Ljava/io/FileNotFoundException; {:try_start_1 .. :try_end_1} :catch_9
    .catch Ljava/security/cert/CertificateException; {:try_start_1 .. :try_end_1} :catch_8
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 1123
    if-eqz v7, :cond_3

    .line 1125
    :try_start_2
    invoke-virtual {v7}, Ljava/io/InputStream;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_6

    move-object v6, v7

    .line 1134
    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v6    # "inStream":Ljava/io/InputStream;
    :cond_0
    :goto_0
    if-eqz v9, :cond_1

    .line 1137
    :try_start_3
    invoke-virtual {v9}, Ljava/security/cert/X509Certificate;->checkValidity()V

    .line 1141
    invoke-virtual {v9}, Ljava/security/cert/X509Certificate;->getKeyUsage()[Z

    move-result-object v10

    if-eqz v10, :cond_5

    .line 1143
    const/4 v8, 0x5

    .line 1144
    .local v8, "keyCertSignIndex":I
    invoke-virtual {v9}, Ljava/security/cert/X509Certificate;->getKeyUsage()[Z

    move-result-object v10

    aget-boolean v10, v10, v8

    if-eqz v10, :cond_4

    .line 1145
    new-instance v10, Ljava/security/cert/CertificateExpiredException;

    const-string v11, "This certificate should not be used for code verification!"

    invoke-direct {v10, v11}, Ljava/security/cert/CertificateExpiredException;-><init>(Ljava/lang/String;)V

    throw v10
    :try_end_3
    .catch Ljava/security/cert/CertificateExpiredException; {:try_start_3 .. :try_end_3} :catch_0
    .catch Ljava/security/cert/CertificateNotYetValidException; {:try_start_3 .. :try_end_3} :catch_7

    .line 1160
    .end local v8    # "keyCertSignIndex":I
    :catch_0
    move-exception v5

    .line 1164
    .local v5, "e":Ljava/security/cert/CertificateException;
    :goto_1
    const/4 v9, 0x0

    .line 1165
    aget-object v10, v4, v12

    invoke-virtual {v10}, Ljava/io/File;->getName()Ljava/lang/String;

    move-result-object v3

    .line 1166
    .local v3, "certFileToErase":Ljava/lang/String;
    aget-object v10, v4, v12

    invoke-virtual {v10}, Ljava/io/File;->delete()Z

    move-result v10

    if-eqz v10, :cond_7

    .line 1167
    sget-object v10, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v11, Ljava/lang/StringBuilder;

    const-string v12, "Expired certificate "

    invoke-direct {v11, v12}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v11, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    const-string v12, " has been erased."

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-static {v10, v11}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 1177
    .end local v3    # "certFileToErase":Ljava/lang/String;
    .end local v5    # "e":Ljava/security/cert/CertificateException;
    .end local v6    # "inStream":Ljava/io/InputStream;
    :cond_1
    :goto_2
    return-object v9

    .line 1118
    .restart local v6    # "inStream":Ljava/io/InputStream;
    :catch_1
    move-exception v5

    .line 1119
    .local v5, "e":Ljava/io/FileNotFoundException;
    :goto_3
    :try_start_4
    invoke-virtual {v5}, Ljava/io/FileNotFoundException;->printStackTrace()V
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    .line 1123
    if-eqz v6, :cond_0

    .line 1125
    :try_start_5
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_2

    goto :goto_0

    .line 1126
    :catch_2
    move-exception v5

    .line 1127
    .local v5, "e":Ljava/io/IOException;
    invoke-virtual {v5}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_0

    .line 1120
    .end local v5    # "e":Ljava/io/IOException;
    :catch_3
    move-exception v5

    .line 1121
    .local v5, "e":Ljava/security/cert/CertificateException;
    :goto_4
    :try_start_6
    invoke-virtual {v5}, Ljava/security/cert/CertificateException;->printStackTrace()V
    :try_end_6
    .catchall {:try_start_6 .. :try_end_6} :catchall_0

    .line 1123
    if-eqz v6, :cond_0

    .line 1125
    :try_start_7
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V
    :try_end_7
    .catch Ljava/io/IOException; {:try_start_7 .. :try_end_7} :catch_4

    goto :goto_0

    .line 1126
    :catch_4
    move-exception v5

    .line 1127
    .local v5, "e":Ljava/io/IOException;
    invoke-virtual {v5}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_0

    .line 1122
    .end local v5    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v10

    .line 1123
    :goto_5
    if-eqz v6, :cond_2

    .line 1125
    :try_start_8
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_5

    .line 1130
    :cond_2
    :goto_6
    throw v10

    .line 1126
    :catch_5
    move-exception v5

    .line 1127
    .restart local v5    # "e":Ljava/io/IOException;
    invoke-virtual {v5}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_6

    .line 1126
    .end local v5    # "e":Ljava/io/IOException;
    .end local v6    # "inStream":Ljava/io/InputStream;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    :catch_6
    move-exception v5

    .line 1127
    .restart local v5    # "e":Ljava/io/IOException;
    invoke-virtual {v5}, Ljava/io/IOException;->printStackTrace()V

    .end local v5    # "e":Ljava/io/IOException;
    :cond_3
    move-object v6, v7

    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v6    # "inStream":Ljava/io/InputStream;
    goto :goto_0

    .line 1147
    .restart local v8    # "keyCertSignIndex":I
    :cond_4
    :try_start_9
    sget-object v10, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    invoke-virtual {v9}, Ljava/security/cert/X509Certificate;->getKeyUsage()[Z

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-static {v10, v11}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 1153
    .end local v8    # "keyCertSignIndex":I
    :cond_5
    const-string v1, "C=US,O=Android,CN=Android Debug"

    .line 1154
    .local v1, "androidDebugModeDN":Ljava/lang/String;
    new-instance v2, Ljavax/security/auth/x500/X500Principal;

    invoke-direct {v2, v1}, Ljavax/security/auth/x500/X500Principal;-><init>(Ljava/lang/String;)V

    .line 1155
    .local v2, "androidDebugModePrincipal":Ljavax/security/auth/x500/X500Principal;
    invoke-virtual {v9}, Ljava/security/cert/X509Certificate;->getIssuerX500Principal()Ljavax/security/auth/x500/X500Principal;

    move-result-object v10

    invoke-virtual {v10, v2}, Ljavax/security/auth/x500/X500Principal;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-nez v10, :cond_6

    .line 1156
    invoke-virtual {v9}, Ljava/security/cert/X509Certificate;->getSubjectX500Principal()Ljavax/security/auth/x500/X500Principal;

    move-result-object v10

    invoke-virtual {v10, v2}, Ljavax/security/auth/x500/X500Principal;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-eqz v10, :cond_1

    .line 1157
    :cond_6
    new-instance v10, Ljava/security/cert/CertificateExpiredException;

    const-string v11, "Android Debug Certificate can\'t be accepted to sign containers!"

    invoke-direct {v10, v11}, Ljava/security/cert/CertificateExpiredException;-><init>(Ljava/lang/String;)V

    throw v10
    :try_end_9
    .catch Ljava/security/cert/CertificateExpiredException; {:try_start_9 .. :try_end_9} :catch_0
    .catch Ljava/security/cert/CertificateNotYetValidException; {:try_start_9 .. :try_end_9} :catch_7

    .line 1160
    .end local v1    # "androidDebugModeDN":Ljava/lang/String;
    .end local v2    # "androidDebugModePrincipal":Ljavax/security/auth/x500/X500Principal;
    :catch_7
    move-exception v5

    goto/16 :goto_1

    .line 1169
    .restart local v3    # "certFileToErase":Ljava/lang/String;
    .local v5, "e":Ljava/security/cert/CertificateException;
    :cond_7
    sget-object v10, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v11, Ljava/lang/StringBuilder;

    const-string v12, "Problems while deleting expired certificate "

    invoke-direct {v11, v12}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v11, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    const-string v12, "!"

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-static {v10, v11}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_2

    .line 1122
    .end local v3    # "certFileToErase":Ljava/lang/String;
    .end local v5    # "e":Ljava/security/cert/CertificateException;
    .end local v6    # "inStream":Ljava/io/InputStream;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    :catchall_1
    move-exception v10

    move-object v6, v7

    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v6    # "inStream":Ljava/io/InputStream;
    goto :goto_5

    .line 1120
    .end local v6    # "inStream":Ljava/io/InputStream;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    :catch_8
    move-exception v5

    move-object v6, v7

    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v6    # "inStream":Ljava/io/InputStream;
    goto :goto_4

    .line 1118
    .end local v6    # "inStream":Ljava/io/InputStream;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    :catch_9
    move-exception v5

    move-object v6, v7

    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v6    # "inStream":Ljava/io/InputStream;
    goto/16 :goto_3
.end method

.method private importCertificateFromPackageName(Ljava/lang/String;)Ljava/security/cert/X509Certificate;
    .locals 2
    .param p1, "packageName"    # Ljava/lang/String;

    .prologue
    .line 871
    invoke-direct {p0, p1}, Lit/necst/grabnrun/SecureDexClassLoader;->importCertificateFromAppPrivateDir(Ljava/lang/String;)Ljava/security/cert/X509Certificate;

    move-result-object v1

    .line 873
    .local v1, "verifiedCertificate":Ljava/security/cert/X509Certificate;
    if-nez v1, :cond_0

    .line 880
    invoke-direct {p0, p1}, Lit/necst/grabnrun/SecureDexClassLoader;->downloadCertificateRemotelyViaHttps(Ljava/lang/String;)Z

    move-result v0

    .line 884
    .local v0, "isCertificateDownloadSuccessful":Z
    if-eqz v0, :cond_0

    .line 889
    invoke-direct {p0, p1}, Lit/necst/grabnrun/SecureDexClassLoader;->importCertificateFromAppPrivateDir(Ljava/lang/String;)Ljava/security/cert/X509Certificate;

    move-result-object v1

    .line 896
    .end local v0    # "isCertificateDownloadSuccessful":Z
    :cond_0
    return-object v1
.end method

.method private revertPackageNameToURL(Ljava/lang/String;)Ljava/net/URL;
    .locals 11
    .param p1, "packageName"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/net/MalformedURLException;
        }
    .end annotation

    .prologue
    const/4 v5, -0x1

    const/16 v10, 0x2e

    .line 406
    invoke-virtual {p1, v10}, Ljava/lang/String;->indexOf(I)I

    move-result v1

    .line 408
    .local v1, "firstPointChar":I
    if-ne v1, v5, :cond_0

    .line 411
    new-instance v4, Ljava/net/URL;

    const-string v5, "https"

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v7, ".com"

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    const-string v7, "certificate.pem"

    invoke-direct {v4, v5, v6, v7}, Ljava/net/URL;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

    .line 428
    :goto_0
    return-object v4

    .line 415
    :cond_0
    const/4 v4, 0x0

    invoke-virtual {p1, v4, v1}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v0

    .line 416
    .local v0, "firstLevelDomain":Ljava/lang/String;
    add-int/lit8 v4, v1, 0x1

    invoke-virtual {p1, v10, v4}, Ljava/lang/String;->indexOf(II)I

    move-result v3

    .line 418
    .local v3, "secondPointChar":I
    if-ne v3, v5, :cond_1

    .line 420
    new-instance v4, Ljava/net/URL;

    const-string v5, "https"

    new-instance v6, Ljava/lang/StringBuilder;

    add-int/lit8 v7, v1, 0x1

    invoke-virtual {p1, v7}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v7

    invoke-static {v7}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v7, "."

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    const-string v7, "/certificate.pem"

    invoke-direct {v4, v5, v6, v7}, Ljava/net/URL;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_0

    .line 426
    :cond_1
    add-int/lit8 v4, v1, 0x1

    invoke-virtual {p1, v4, v3}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v2

    .line 428
    .local v2, "secondLevelDomain":Ljava/lang/String;
    new-instance v4, Ljava/net/URL;

    const-string v5, "https"

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-static {v2}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v7, "."

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    new-instance v7, Ljava/lang/StringBuilder;

    add-int/lit8 v8, v3, 0x1

    invoke-virtual {p1, v8}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v8

    sget-char v9, Ljava/io/File;->separatorChar:C

    invoke-virtual {v8, v10, v9}, Ljava/lang/String;->replace(CC)Ljava/lang/String;

    move-result-object v8

    invoke-static {v8}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v8

    invoke-direct {v7, v8}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v8, "/certificate.pem"

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-direct {v4, v5, v6, v7}, Ljava/net/URL;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_0
.end method

.method private verifyAllContainersSignature()V
    .locals 11

    .prologue
    .line 444
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    .line 447
    .local v0, "alreadyCheckedContainerMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Boolean;>;"
    iget-object v8, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v8}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v8

    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 449
    .local v4, "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_0
    :goto_0
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-nez v8, :cond_1

    .line 518
    return-void

    .line 451
    :cond_1
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 452
    .local v3, "currentPackageName":Ljava/lang/String;
    iget-object v8, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v8, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    .line 455
    .local v1, "containerPath":Ljava/lang/String;
    invoke-interface {v0, v1}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_2

    .line 459
    invoke-interface {v0, v1}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v8

    check-cast v8, Ljava/lang/Boolean;

    invoke-virtual {v8}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v8

    if-nez v8, :cond_0

    .line 460
    invoke-interface {v4}, Ljava/util/Iterator;->remove()V

    goto :goto_0

    .line 469
    :cond_2
    iget-object v8, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    invoke-virtual {v8, v3}, Lit/necst/grabnrun/PackageNameTrie;->getPackageNameWithAssociatedCertificate(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v5

    .line 471
    .local v5, "rootPackageNameWithCertificate":Ljava/lang/String;
    const/4 v7, 0x0

    .line 474
    .local v7, "verifiedCertificate":Ljava/security/cert/X509Certificate;
    invoke-virtual {v5}, Ljava/lang/String;->isEmpty()Z

    move-result v8

    if-nez v8, :cond_3

    .line 477
    invoke-direct {p0, v5}, Lit/necst/grabnrun/SecureDexClassLoader;->importCertificateFromPackageName(Ljava/lang/String;)Ljava/security/cert/X509Certificate;

    move-result-object v7

    .line 481
    :cond_3
    const/4 v6, 0x1

    .line 483
    .local v6, "signatureCheckIsSuccessful":Z
    if-eqz v7, :cond_4

    .line 489
    invoke-direct {p0, v1, v7}, Lit/necst/grabnrun/SecureDexClassLoader;->verifyContainerSignatureAgainstCertificate(Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z

    move-result v6

    .line 492
    if-eqz v6, :cond_4

    .line 496
    const/4 v8, 0x1

    invoke-static {v8}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    move-result-object v8

    invoke-interface {v0, v1, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 500
    :cond_4
    if-eqz v7, :cond_5

    if-eqz v7, :cond_0

    if-nez v6, :cond_0

    .line 504
    :cond_5
    const/4 v8, 0x0

    invoke-static {v8}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    move-result-object v8

    invoke-interface {v0, v1, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 507
    new-instance v2, Ljava/io/File;

    invoke-direct {v2, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 508
    .local v2, "containerToRemove":Ljava/io/File;
    invoke-virtual {v2}, Ljava/io/File;->delete()Z

    move-result v8

    if-nez v8, :cond_6

    .line 509
    sget-object v8, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v9, Ljava/lang/StringBuilder;

    const-string v10, "It was impossible to delete "

    invoke-direct {v9, v10}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v9, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-static {v8, v9}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 513
    :cond_6
    invoke-interface {v4}, Ljava/util/Iterator;->remove()V

    goto :goto_0
.end method

.method private verifyAllContainersSignatureConcurrently(Ljava/util/Set;)V
    .locals 22
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 528
    .local p1, "containersPathToVerifySet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    new-instance v5, Ljava/util/LinkedHashMap;

    invoke-direct {v5}, Ljava/util/LinkedHashMap;-><init>()V

    .line 531
    .local v5, "containerPathToRootPackageNameMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;"
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    move-object/from16 v18, v0

    invoke-interface/range {v18 .. v18}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v18

    invoke-interface/range {v18 .. v18}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v13

    .line 535
    .local v13, "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_0
    :goto_0
    invoke-interface {v13}, Ljava/util/Iterator;->hasNext()Z

    move-result v18

    if-nez v18, :cond_4

    .line 551
    new-instance v18, Ljava/util/HashSet;

    invoke-direct/range {v18 .. v18}, Ljava/util/HashSet;-><init>()V

    invoke-static/range {v18 .. v18}, Ljava/util/Collections;->synchronizedSet(Ljava/util/Set;)Ljava/util/Set;

    move-result-object v16

    .line 553
    .local v16, "successVerifiedContainerPathSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    invoke-interface {v5}, Ljava/util/Map;->isEmpty()Z

    move-result v18

    if-nez v18, :cond_1

    .line 557
    invoke-interface {v5}, Ljava/util/Map;->size()I

    move-result v18

    invoke-static/range {v18 .. v18}, Ljava/util/concurrent/Executors;->newFixedThreadPool(I)Ljava/util/concurrent/ExecutorService;

    move-result-object v17

    .line 558
    .local v17, "threadSignatureVerificationPool":Ljava/util/concurrent/ExecutorService;
    new-instance v11, Ljava/util/ArrayList;

    invoke-direct {v11}, Ljava/util/ArrayList;-><init>()V

    .line 560
    .local v11, "futureTaskList":Ljava/util/List;, "Ljava/util/List<Ljava/util/concurrent/Future<*>;>;"
    invoke-interface {v5}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v18

    invoke-interface/range {v18 .. v18}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 562
    .local v4, "containerPathIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :goto_1
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v18

    if-nez v18, :cond_5

    .line 573
    invoke-interface/range {v17 .. v17}, Ljava/util/concurrent/ExecutorService;->shutdown()V

    .line 575
    invoke-interface {v11}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v18

    :goto_2
    invoke-interface/range {v18 .. v18}, Ljava/util/Iterator;->hasNext()Z

    move-result v19

    if-nez v19, :cond_6

    .line 592
    const-wide/16 v18, 0x2

    :try_start_0
    sget-object v20, Lit/necst/grabnrun/SecureDexClassLoader;->KEEP_ALIVE_TIME_UNIT:Ljava/util/concurrent/TimeUnit;

    invoke-interface/range {v17 .. v20}, Ljava/util/concurrent/ExecutorService;->awaitTermination(JLjava/util/concurrent/TimeUnit;)Z
    :try_end_0
    .catch Ljava/lang/InterruptedException; {:try_start_0 .. :try_end_0} :catch_1

    .line 602
    .end local v4    # "containerPathIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v11    # "futureTaskList":Ljava/util/List;, "Ljava/util/List<Ljava/util/concurrent/Future<*>;>;"
    .end local v17    # "threadSignatureVerificationPool":Ljava/util/concurrent/ExecutorService;
    :cond_1
    :goto_3
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    move-object/from16 v18, v0

    invoke-interface/range {v18 .. v18}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v18

    invoke-interface/range {v18 .. v18}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v12

    .line 604
    .local v12, "packageNamesAfterVerificationIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_2
    :goto_4
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v18

    if-nez v18, :cond_7

    .line 622
    invoke-interface/range {p1 .. p1}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v6

    .line 624
    .local v6, "containersPathToVerifyIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_3
    :goto_5
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z

    move-result v18

    if-nez v18, :cond_9

    .line 636
    return-void

    .line 537
    .end local v6    # "containersPathToVerifyIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v12    # "packageNamesAfterVerificationIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v16    # "successVerifiedContainerPathSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_4
    invoke-interface {v13}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v8

    check-cast v8, Ljava/lang/String;

    .line 541
    .local v8, "currentPackageName":Ljava/lang/String;
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    move-object/from16 v18, v0

    move-object/from16 v0, v18

    invoke-virtual {v0, v8}, Lit/necst/grabnrun/PackageNameTrie;->getPackageNameWithAssociatedCertificate(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v15

    .line 543
    .local v15, "rootPackageNameWithCertificate":Ljava/lang/String;
    invoke-virtual {v15}, Ljava/lang/String;->isEmpty()Z

    move-result v18

    if-nez v18, :cond_0

    .line 546
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    move-object/from16 v18, v0

    move-object/from16 v0, v18

    invoke-interface {v0, v8}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v18

    check-cast v18, Ljava/lang/String;

    move-object/from16 v0, v18

    invoke-interface {v5, v0, v15}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    goto/16 :goto_0

    .line 564
    .end local v8    # "currentPackageName":Ljava/lang/String;
    .end local v15    # "rootPackageNameWithCertificate":Ljava/lang/String;
    .restart local v4    # "containerPathIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v11    # "futureTaskList":Ljava/util/List;, "Ljava/util/List<Ljava/util/concurrent/Future<*>;>;"
    .restart local v16    # "successVerifiedContainerPathSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    .restart local v17    # "threadSignatureVerificationPool":Ljava/util/concurrent/ExecutorService;
    :cond_5
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 568
    .local v7, "currentContainerPath":Ljava/lang/String;
    new-instance v19, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;

    invoke-interface {v5, v7}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v18

    check-cast v18, Ljava/lang/String;

    move-object/from16 v0, v19

    move-object/from16 v1, p0

    move-object/from16 v2, v18

    move-object/from16 v3, v16

    invoke-direct {v0, v1, v7, v2, v3}, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;-><init>(Lit/necst/grabnrun/SecureDexClassLoader;Ljava/lang/String;Ljava/lang/String;Ljava/util/Set;)V

    move-object/from16 v0, v17

    move-object/from16 v1, v19

    invoke-interface {v0, v1}, Ljava/util/concurrent/ExecutorService;->submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;

    move-result-object v10

    .line 569
    .local v10, "futureTask":Ljava/util/concurrent/Future;, "Ljava/util/concurrent/Future<*>;"
    invoke-interface {v11, v10}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto/16 :goto_1

    .line 575
    .end local v7    # "currentContainerPath":Ljava/lang/String;
    .end local v10    # "futureTask":Ljava/util/concurrent/Future;, "Ljava/util/concurrent/Future<*>;"
    :cond_6
    invoke-interface/range {v18 .. v18}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/util/concurrent/Future;

    .line 580
    .restart local v10    # "futureTask":Ljava/util/concurrent/Future;, "Ljava/util/concurrent/Future<*>;"
    :try_start_1
    invoke-interface {v10}, Ljava/util/concurrent/Future;->get()Ljava/lang/Object;
    :try_end_1
    .catch Ljava/lang/InterruptedException; {:try_start_1 .. :try_end_1} :catch_0
    .catch Ljava/util/concurrent/ExecutionException; {:try_start_1 .. :try_end_1} :catch_2

    goto/16 :goto_2

    .line 582
    :catch_0
    move-exception v9

    .line 585
    .local v9, "e":Ljava/lang/Exception;
    :goto_6
    sget-object v19, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v20, Ljava/lang/StringBuilder;

    const-string v21, "One of the thread failed during signature verification because of "

    invoke-direct/range {v20 .. v21}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v9}, Ljava/lang/Exception;->getCause()Ljava/lang/Throwable;

    move-result-object v21

    invoke-virtual/range {v21 .. v21}, Ljava/lang/Throwable;->toString()Ljava/lang/String;

    move-result-object v21

    invoke-virtual/range {v20 .. v21}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v20

    invoke-virtual/range {v20 .. v20}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v20

    invoke-static/range {v19 .. v20}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_2

    .line 593
    .end local v9    # "e":Ljava/lang/Exception;
    .end local v10    # "futureTask":Ljava/util/concurrent/Future;, "Ljava/util/concurrent/Future<*>;"
    :catch_1
    move-exception v9

    .line 596
    .local v9, "e":Ljava/lang/InterruptedException;
    sget-object v18, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    const-string v19, "At least one thread for signature verification was still busy and so it was interrupted"

    invoke-static/range {v18 .. v19}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_3

    .line 606
    .end local v4    # "containerPathIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v9    # "e":Ljava/lang/InterruptedException;
    .end local v11    # "futureTaskList":Ljava/util/List;, "Ljava/util/List<Ljava/util/concurrent/Future<*>;>;"
    .end local v17    # "threadSignatureVerificationPool":Ljava/util/concurrent/ExecutorService;
    .restart local v12    # "packageNamesAfterVerificationIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_7
    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v8

    check-cast v8, Ljava/lang/String;

    .line 610
    .restart local v8    # "currentPackageName":Ljava/lang/String;
    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    move-object/from16 v18, v0

    move-object/from16 v0, v18

    invoke-virtual {v0, v8}, Lit/necst/grabnrun/PackageNameTrie;->getPackageNameWithAssociatedCertificate(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v14

    .line 612
    .local v14, "rootPackageNameAllowedForLoading":Ljava/lang/String;
    invoke-virtual {v14}, Ljava/lang/String;->isEmpty()Z

    move-result v18

    if-nez v18, :cond_8

    move-object/from16 v0, p0

    iget-object v0, v0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    move-object/from16 v18, v0

    move-object/from16 v0, v18

    invoke-interface {v0, v8}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v18

    move-object/from16 v0, v16

    move-object/from16 v1, v18

    invoke-interface {v0, v1}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    move-result v18

    if-nez v18, :cond_2

    .line 616
    :cond_8
    invoke-interface {v12}, Ljava/util/Iterator;->remove()V

    goto/16 :goto_4

    .line 626
    .end local v8    # "currentPackageName":Ljava/lang/String;
    .end local v14    # "rootPackageNameAllowedForLoading":Ljava/lang/String;
    .restart local v6    # "containersPathToVerifyIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_9
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 628
    .restart local v7    # "currentContainerPath":Ljava/lang/String;
    move-object/from16 v0, v16

    invoke-interface {v0, v7}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    move-result v18

    if-nez v18, :cond_3

    .line 632
    new-instance v18, Ljava/io/File;

    move-object/from16 v0, v18

    invoke-direct {v0, v7}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual/range {v18 .. v18}, Ljava/io/File;->delete()Z

    move-result v18

    if-nez v18, :cond_3

    .line 633
    sget-object v18, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v19, Ljava/lang/StringBuilder;

    const-string v20, "Issue while deleting conainer located at "

    invoke-direct/range {v19 .. v20}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v19

    invoke-virtual {v0, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v19

    invoke-virtual/range {v19 .. v19}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v19

    invoke-static/range {v18 .. v19}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_5

    .line 582
    .end local v6    # "containersPathToVerifyIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v7    # "currentContainerPath":Ljava/lang/String;
    .end local v12    # "packageNamesAfterVerificationIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v4    # "containerPathIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v10    # "futureTask":Ljava/util/concurrent/Future;, "Ljava/util/concurrent/Future<*>;"
    .restart local v11    # "futureTaskList":Ljava/util/List;, "Ljava/util/List<Ljava/util/concurrent/Future<*>;>;"
    .restart local v17    # "threadSignatureVerificationPool":Ljava/util/concurrent/ExecutorService;
    :catch_2
    move-exception v9

    goto/16 :goto_6
.end method

.method private verifyContainerSignatureAgainstCertificate(Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z
    .locals 15
    .param p1, "containerPath"    # Ljava/lang/String;
    .param p2, "verifiedCertificate"    # Ljava/security/cert/X509Certificate;

    .prologue
    .line 908
    const-string v12, "."

    move-object/from16 v0, p1

    invoke-virtual {v0, v12}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v6

    .line 909
    .local v6, "extensionIndex":I
    move-object/from16 v0, p1

    invoke-virtual {v0, v6}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v5

    .line 911
    .local v5, "extension":Ljava/lang/String;
    const/4 v10, 0x0

    .line 915
    .local v10, "signatureCheckIsSuccessful":Z
    const-string v12, ".apk"

    invoke-virtual {v5, v12}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v12

    if-eqz v12, :cond_0

    .line 922
    iget-object v12, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageManager:Landroid/content/pm/PackageManager;

    const/16 v13, 0x40

    move-object/from16 v0, p1

    invoke-virtual {v12, v0, v13}, Landroid/content/pm/PackageManager;->getPackageArchiveInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v12

    iget-object v11, v12, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;

    .line 924
    .local v11, "signatures":[Landroid/content/pm/Signature;
    if-eqz v11, :cond_0

    .line 925
    array-length v14, v11

    const/4 v12, 0x0

    move v13, v12

    :goto_0
    if-lt v13, v14, :cond_3

    .line 966
    .end local v11    # "signatures":[Landroid/content/pm/Signature;
    :cond_0
    const-string v12, ".jar"

    invoke-virtual {v5, v12}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v12

    if-nez v12, :cond_1

    const-string v12, ".apk"

    invoke-virtual {v5, v12}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v12

    if-eqz v12, :cond_2

    if-eqz v10, :cond_2

    .line 969
    :cond_1
    const/4 v2, 0x0

    .line 973
    .local v2, "containerToVerify":Ljava/util/jar/JarFile;
    :try_start_0
    new-instance v3, Ljava/util/jar/JarFile;

    move-object/from16 v0, p1

    invoke-direct {v3, v0}, Ljava/util/jar/JarFile;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_4
    .catchall {:try_start_0 .. :try_end_0} :catchall_1

    .line 977
    .end local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    .local v3, "containerToVerify":Ljava/util/jar/JarFile;
    :try_start_1
    move-object/from16 v0, p2

    invoke-direct {p0, v3, v0}, Lit/necst/grabnrun/SecureDexClassLoader;->verifyJARContainer(Ljava/util/jar/JarFile;Ljava/security/cert/X509Certificate;)V
    :try_end_1
    .catch Ljava/lang/Exception; {:try_start_1 .. :try_end_1} :catch_8
    .catchall {:try_start_1 .. :try_end_1} :catchall_2

    .line 981
    const/4 v10, 0x1

    .line 989
    if-eqz v3, :cond_2

    .line 991
    :try_start_2
    invoke-virtual {v3}, Ljava/util/jar/JarFile;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_7

    .line 1003
    .end local v3    # "containerToVerify":Ljava/util/jar/JarFile;
    :cond_2
    :goto_1
    return v10

    .line 925
    .restart local v11    # "signatures":[Landroid/content/pm/Signature;
    :cond_3
    aget-object v9, v11, v13

    .line 926
    .local v9, "sign":Landroid/content/pm/Signature;
    if-eqz v9, :cond_5

    .line 928
    const/4 v1, 0x0

    .line 929
    .local v1, "certFromSign":Ljava/security/cert/X509Certificate;
    const/4 v7, 0x0

    .line 934
    .local v7, "inStream":Ljava/io/InputStream;
    :try_start_3
    new-instance v8, Ljava/io/ByteArrayInputStream;

    invoke-virtual {v9}, Landroid/content/pm/Signature;->toByteArray()[B

    move-result-object v12

    invoke-direct {v8, v12}, Ljava/io/ByteArrayInputStream;-><init>([B)V
    :try_end_3
    .catch Ljava/security/cert/CertificateException; {:try_start_3 .. :try_end_3} :catch_0
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    .line 935
    .end local v7    # "inStream":Ljava/io/InputStream;
    .local v8, "inStream":Ljava/io/InputStream;
    :try_start_4
    iget-object v12, p0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFactory:Ljava/security/cert/CertificateFactory;

    invoke-virtual {v12, v8}, Ljava/security/cert/CertificateFactory;->generateCertificate(Ljava/io/InputStream;)Ljava/security/cert/Certificate;

    move-result-object v12

    move-object v0, v12

    check-cast v0, Ljava/security/cert/X509Certificate;

    move-object v1, v0

    .line 938
    invoke-virtual {v1}, Ljava/security/cert/X509Certificate;->checkValidity()V

    .line 942
    move-object/from16 v0, p2

    invoke-virtual {v1, v0}, Ljava/security/cert/X509Certificate;->equals(Ljava/lang/Object;)Z
    :try_end_4
    .catch Ljava/security/cert/CertificateException; {:try_start_4 .. :try_end_4} :catch_9
    .catchall {:try_start_4 .. :try_end_4} :catchall_3

    move-result v12

    if-eqz v12, :cond_4

    .line 945
    const/4 v10, 0x1

    .line 950
    :cond_4
    if-eqz v8, :cond_5

    .line 952
    :try_start_5
    invoke-virtual {v8}, Ljava/io/InputStream;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_3

    .line 925
    .end local v1    # "certFromSign":Ljava/security/cert/X509Certificate;
    .end local v8    # "inStream":Ljava/io/InputStream;
    :cond_5
    :goto_2
    add-int/lit8 v12, v13, 0x1

    move v13, v12

    goto :goto_0

    .line 947
    .restart local v1    # "certFromSign":Ljava/security/cert/X509Certificate;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    :catch_0
    move-exception v12

    .line 950
    :goto_3
    if-eqz v7, :cond_5

    .line 952
    :try_start_6
    invoke-virtual {v7}, Ljava/io/InputStream;->close()V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_1

    goto :goto_2

    .line 953
    :catch_1
    move-exception v4

    .line 954
    .local v4, "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_2

    .line 949
    .end local v4    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v12

    .line 950
    :goto_4
    if-eqz v7, :cond_6

    .line 952
    :try_start_7
    invoke-virtual {v7}, Ljava/io/InputStream;->close()V
    :try_end_7
    .catch Ljava/io/IOException; {:try_start_7 .. :try_end_7} :catch_2

    .line 957
    :cond_6
    :goto_5
    throw v12

    .line 953
    :catch_2
    move-exception v4

    .line 954
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_5

    .line 953
    .end local v4    # "e":Ljava/io/IOException;
    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v8    # "inStream":Ljava/io/InputStream;
    :catch_3
    move-exception v4

    .line 954
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_2

    .line 983
    .end local v1    # "certFromSign":Ljava/security/cert/X509Certificate;
    .end local v4    # "e":Ljava/io/IOException;
    .end local v8    # "inStream":Ljava/io/InputStream;
    .end local v9    # "sign":Landroid/content/pm/Signature;
    .end local v11    # "signatures":[Landroid/content/pm/Signature;
    .restart local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    :catch_4
    move-exception v4

    .line 986
    .local v4, "e":Ljava/lang/Exception;
    :goto_6
    const/4 v10, 0x0

    .line 989
    if-eqz v2, :cond_2

    .line 991
    :try_start_8
    invoke-virtual {v2}, Ljava/util/jar/JarFile;->close()V
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_5

    goto :goto_1

    .line 992
    :catch_5
    move-exception v4

    .line 993
    .local v4, "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_1

    .line 988
    .end local v4    # "e":Ljava/io/IOException;
    :catchall_1
    move-exception v12

    .line 989
    :goto_7
    if-eqz v2, :cond_7

    .line 991
    :try_start_9
    invoke-virtual {v2}, Ljava/util/jar/JarFile;->close()V
    :try_end_9
    .catch Ljava/io/IOException; {:try_start_9 .. :try_end_9} :catch_6

    .line 996
    :cond_7
    :goto_8
    throw v12

    .line 992
    :catch_6
    move-exception v4

    .line 993
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_8

    .line 992
    .end local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    .end local v4    # "e":Ljava/io/IOException;
    .restart local v3    # "containerToVerify":Ljava/util/jar/JarFile;
    :catch_7
    move-exception v4

    .line 993
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_1

    .line 988
    .end local v4    # "e":Ljava/io/IOException;
    :catchall_2
    move-exception v12

    move-object v2, v3

    .end local v3    # "containerToVerify":Ljava/util/jar/JarFile;
    .restart local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    goto :goto_7

    .line 983
    .end local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    .restart local v3    # "containerToVerify":Ljava/util/jar/JarFile;
    :catch_8
    move-exception v4

    move-object v2, v3

    .end local v3    # "containerToVerify":Ljava/util/jar/JarFile;
    .restart local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    goto :goto_6

    .line 949
    .end local v2    # "containerToVerify":Ljava/util/jar/JarFile;
    .restart local v1    # "certFromSign":Ljava/security/cert/X509Certificate;
    .restart local v8    # "inStream":Ljava/io/InputStream;
    .restart local v9    # "sign":Landroid/content/pm/Signature;
    .restart local v11    # "signatures":[Landroid/content/pm/Signature;
    :catchall_3
    move-exception v12

    move-object v7, v8

    .end local v8    # "inStream":Ljava/io/InputStream;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    goto :goto_4

    .line 947
    .end local v7    # "inStream":Ljava/io/InputStream;
    .restart local v8    # "inStream":Ljava/io/InputStream;
    :catch_9
    move-exception v12

    move-object v7, v8

    .end local v8    # "inStream":Ljava/io/InputStream;
    .restart local v7    # "inStream":Ljava/io/InputStream;
    goto :goto_3
.end method

.method private verifyJARContainer(Ljava/util/jar/JarFile;Ljava/security/cert/X509Certificate;)V
    .locals 16
    .param p1, "jarFile"    # Ljava/util/jar/JarFile;
    .param p2, "trustedCert"    # Ljava/security/cert/X509Certificate;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .prologue
    .line 1009
    if-eqz p1, :cond_0

    if-nez p2, :cond_1

    .line 1010
    :cond_0
    new-instance v13, Ljava/lang/SecurityException;

    const-string v14, "JarFile or certificate are missing"

    invoke-direct {v13, v14}, Ljava/lang/SecurityException;-><init>(Ljava/lang/String;)V

    throw v13

    .line 1012
    :cond_1
    new-instance v5, Ljava/util/Vector;

    invoke-direct {v5}, Ljava/util/Vector;-><init>()V

    .line 1015
    .local v5, "entriesVec":Ljava/util/Vector;, "Ljava/util/Vector<Ljava/util/jar/JarEntry;>;"
    invoke-virtual/range {p1 .. p1}, Ljava/util/jar/JarFile;->getManifest()Ljava/util/jar/Manifest;

    move-result-object v8

    .line 1016
    .local v8, "man":Ljava/util/jar/Manifest;
    if-nez v8, :cond_2

    .line 1017
    sget-object v13, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    invoke-virtual/range {p1 .. p1}, Ljava/util/jar/JarFile;->getName()Ljava/lang/String;

    move-result-object v15

    invoke-static {v15}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v15, "is not signed."

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 1018
    new-instance v13, Ljava/lang/SecurityException;

    const-string v14, "The container is not signed"

    invoke-direct {v13, v14}, Ljava/lang/SecurityException;-><init>(Ljava/lang/String;)V

    throw v13

    .line 1022
    :cond_2
    const/16 v13, 0x2000

    new-array v1, v13, [B

    .line 1023
    .local v1, "buffer":[B
    invoke-virtual/range {p1 .. p1}, Ljava/util/jar/JarFile;->entries()Ljava/util/Enumeration;

    move-result-object v4

    .line 1025
    .local v4, "entries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/util/jar/JarEntry;>;"
    :cond_3
    :goto_0
    invoke-interface {v4}, Ljava/util/Enumeration;->hasMoreElements()Z

    move-result v13

    if-nez v13, :cond_5

    .line 1047
    invoke-virtual {v5}, Ljava/util/Vector;->elements()Ljava/util/Enumeration;

    move-result-object v10

    .line 1049
    .local v10, "signedEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/util/jar/JarEntry;>;"
    :cond_4
    invoke-interface {v10}, Ljava/util/Enumeration;->hasMoreElements()Z

    move-result v13

    if-nez v13, :cond_7

    .line 1094
    return-void

    .line 1028
    .end local v10    # "signedEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/util/jar/JarEntry;>;"
    :cond_5
    invoke-interface {v4}, Ljava/util/Enumeration;->nextElement()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/util/jar/JarEntry;

    .line 1031
    .local v7, "je":Ljava/util/jar/JarEntry;
    invoke-virtual {v7}, Ljava/util/jar/JarEntry;->isDirectory()Z

    move-result v13

    if-nez v13, :cond_3

    .line 1032
    invoke-virtual {v5, v7}, Ljava/util/Vector;->addElement(Ljava/lang/Object;)V

    .line 1033
    move-object/from16 v0, p1

    invoke-virtual {v0, v7}, Ljava/util/jar/JarFile;->getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;

    move-result-object v6

    .line 1037
    .local v6, "inStream":Ljava/io/InputStream;
    :cond_6
    const/4 v13, 0x0

    array-length v14, v1

    invoke-virtual {v6, v1, v13, v14}, Ljava/io/InputStream;->read([BII)I

    move-result v13

    const/4 v14, -0x1

    if-ne v13, v14, :cond_6

    .line 1042
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V

    goto :goto_0

    .line 1051
    .end local v6    # "inStream":Ljava/io/InputStream;
    .end local v7    # "je":Ljava/util/jar/JarEntry;
    .restart local v10    # "signedEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/util/jar/JarEntry;>;"
    :cond_7
    invoke-interface {v10}, Ljava/util/Enumeration;->nextElement()Ljava/lang/Object;

    move-result-object v11

    check-cast v11, Ljava/util/jar/JarEntry;

    .line 1054
    .local v11, "signedEntry":Ljava/util/jar/JarEntry;
    invoke-virtual {v11}, Ljava/util/jar/JarEntry;->getCertificates()[Ljava/security/cert/Certificate;

    move-result-object v2

    .line 1055
    .local v2, "certificates":[Ljava/security/cert/Certificate;
    if-eqz v2, :cond_8

    array-length v13, v2

    if-nez v13, :cond_9

    .line 1056
    :cond_8
    invoke-virtual {v11}, Ljava/util/jar/JarEntry;->getName()Ljava/lang/String;

    move-result-object v13

    const-string v14, "META-INF"

    invoke-virtual {v13, v14}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v13

    if-nez v13, :cond_4

    .line 1057
    sget-object v13, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    invoke-virtual {v11}, Ljava/util/jar/JarEntry;->getName()Ljava/lang/String;

    move-result-object v15

    invoke-static {v15}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v15, " is an unsigned class file"

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 1058
    new-instance v13, Ljava/lang/SecurityException;

    const-string v14, "The container has unsigned class files."

    invoke-direct {v13, v14}, Ljava/lang/SecurityException;-><init>(Ljava/lang/String;)V

    throw v13

    .line 1065
    :cond_9
    const/4 v9, 0x0

    .line 1067
    .local v9, "signedAsExpected":Z
    array-length v15, v2

    const/4 v13, 0x0

    move v14, v13

    :goto_1
    if-lt v14, v15, :cond_a

    .line 1088
    if-nez v9, :cond_4

    .line 1089
    sget-object v13, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "The trusted certificate was not used to sign "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v11}, Ljava/util/jar/JarEntry;->getName()Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 1090
    new-instance v13, Ljava/lang/SecurityException;

    const-string v14, "The provider is not signed by a trusted signer"

    invoke-direct {v13, v14}, Ljava/lang/SecurityException;-><init>(Ljava/lang/String;)V

    throw v13

    .line 1067
    :cond_a
    aget-object v12, v2, v14

    .line 1071
    .local v12, "signerCert":Ljava/security/cert/Certificate;
    :try_start_0
    move-object v0, v12

    check-cast v0, Ljava/security/cert/X509Certificate;

    move-object v13, v0

    invoke-virtual {v13}, Ljava/security/cert/X509Certificate;->checkValidity()V
    :try_end_0
    .catch Ljava/security/cert/CertificateExpiredException; {:try_start_0 .. :try_end_0} :catch_0
    .catch Ljava/security/cert/CertificateNotYetValidException; {:try_start_0 .. :try_end_0} :catch_2
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_1

    .line 1083
    :goto_2
    move-object/from16 v0, p2

    invoke-virtual {v12, v0}, Ljava/security/cert/Certificate;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_b

    .line 1085
    const/4 v9, 0x1

    .line 1067
    :cond_b
    add-int/lit8 v13, v14, 0x1

    move v14, v13

    goto :goto_1

    .line 1073
    :catch_0
    move-exception v3

    .line 1077
    .local v3, "e":Ljava/security/cert/CertificateException;
    :goto_3
    sget-object v13, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "One of the certificates used to sign "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v11}, Ljava/util/jar/JarEntry;->getName()Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    const-string v15, " is expired"

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 1078
    new-instance v13, Ljava/lang/SecurityException;

    const-string v14, "One of the used certificates is expired!"

    invoke-direct {v13, v14}, Ljava/lang/SecurityException;-><init>(Ljava/lang/String;)V

    throw v13

    .line 1079
    .end local v3    # "e":Ljava/security/cert/CertificateException;
    :catch_1
    move-exception v13

    goto :goto_2

    .line 1073
    :catch_2
    move-exception v3

    goto :goto_3
.end method


# virtual methods
.method public loadClass(Ljava/lang/String;)Ljava/lang/Class;
    .locals 13
    .param p1, "className"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            ")",
            "Ljava/lang/Class",
            "<*>;"
        }
    .end annotation

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/ClassNotFoundException;
        }
    .end annotation

    .prologue
    const/4 v10, 0x0

    .line 712
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    invoke-interface {v9}, Ljava/util/Map;->isEmpty()Z

    move-result v9

    if-eqz v9, :cond_0

    move-object v9, v10

    .line 857
    :goto_0
    return-object v9

    .line 716
    :cond_0
    iget-boolean v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->hasBeenWipedOut:Z

    if-eqz v9, :cond_1

    move-object v9, v10

    goto :goto_0

    .line 721
    :cond_1
    const/4 v9, 0x0

    const/16 v11, 0x2e

    invoke-virtual {p1, v11}, Ljava/lang/String;->lastIndexOf(I)I

    move-result v11

    invoke-virtual {p1, v9, v11}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v4

    .line 727
    .local v4, "packageName":Ljava/lang/String;
    iget-object v11, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    monitor-enter v11

    .line 729
    :try_start_0
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v9, v4}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    .line 727
    .local v1, "containerPath":Ljava/lang/String;
    monitor-exit v11

    .line 732
    if-nez v1, :cond_2

    move-object v9, v10

    goto :goto_0

    .line 727
    .end local v1    # "containerPath":Ljava/lang/String;
    :catchall_0
    move-exception v9

    monitor-exit v11
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v9

    .line 734
    .restart local v1    # "containerPath":Ljava/lang/String;
    :cond_2
    iget-boolean v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->performLazyEvaluation:Z

    if-eqz v9, :cond_c

    .line 741
    iget-object v11, p0, Lit/necst/grabnrun/SecureDexClassLoader;->lazyAlreadyVerifiedPackageNameSet:Ljava/util/Set;

    monitor-enter v11

    .line 744
    :try_start_1
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->lazyAlreadyVerifiedPackageNameSet:Ljava/util/Set;

    invoke-interface {v9, v4}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    move-result v0

    .line 741
    .local v0, "alreadyVerifiedPackageName":Z
    monitor-exit v11
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 747
    if-eqz v0, :cond_3

    .line 751
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    invoke-virtual {v9, v4}, Lit/necst/grabnrun/PackageNameTrie;->getPackageNameWithAssociatedCertificate(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v6

    .line 753
    .local v6, "rootPackageNameWithCertificate":Ljava/lang/String;
    invoke-virtual {v6}, Ljava/lang/String;->isEmpty()Z

    move-result v9

    if-nez v9, :cond_c

    .line 757
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mDexClassLoader:Ldalvik/system/DexClassLoader;

    invoke-virtual {v9, p1}, Ldalvik/system/DexClassLoader;->loadClass(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v9

    goto :goto_0

    .line 741
    .end local v0    # "alreadyVerifiedPackageName":Z
    .end local v6    # "rootPackageNameWithCertificate":Ljava/lang/String;
    :catchall_1
    move-exception v9

    :try_start_2
    monitor-exit v11
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    throw v9

    .line 766
    .restart local v0    # "alreadyVerifiedPackageName":Z
    :cond_3
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    invoke-virtual {v9, v4}, Lit/necst/grabnrun/PackageNameTrie;->getPackageNameWithAssociatedCertificate(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v6

    .line 768
    .restart local v6    # "rootPackageNameWithCertificate":Ljava/lang/String;
    const/4 v8, 0x0

    .line 771
    .local v8, "verifiedCertificate":Ljava/security/cert/X509Certificate;
    invoke-virtual {v6}, Ljava/lang/String;->isEmpty()Z

    move-result v9

    if-nez v9, :cond_4

    .line 774
    invoke-direct {p0, v6}, Lit/necst/grabnrun/SecureDexClassLoader;->importCertificateFromPackageName(Ljava/lang/String;)Ljava/security/cert/X509Certificate;

    move-result-object v8

    .line 777
    :cond_4
    if-eqz v8, :cond_b

    .line 783
    invoke-direct {p0, v1, v8}, Lit/necst/grabnrun/SecureDexClassLoader;->verifyContainerSignatureAgainstCertificate(Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z

    move-result v7

    .line 786
    .local v7, "signatureCheckIsSuccessful":Z
    if-eqz v7, :cond_7

    .line 801
    iget-object v10, p0, Lit/necst/grabnrun/SecureDexClassLoader;->lazyAlreadyVerifiedPackageNameSet:Ljava/util/Set;

    monitor-enter v10

    .line 803
    :try_start_3
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v9}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v9

    invoke-interface {v9}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v5

    .line 805
    .local v5, "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_5
    :goto_1
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-nez v9, :cond_6

    .line 801
    monitor-exit v10
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_2

    .line 817
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mDexClassLoader:Ldalvik/system/DexClassLoader;

    invoke-virtual {v9, p1}, Ldalvik/system/DexClassLoader;->loadClass(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v9

    goto :goto_0

    .line 807
    :cond_6
    :try_start_4
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 809
    .local v3, "currentPackageName":Ljava/lang/String;
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v9, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    invoke-virtual {v9, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_5

    .line 812
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->lazyAlreadyVerifiedPackageNameSet:Ljava/util/Set;

    invoke-interface {v9, v3}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    goto :goto_1

    .line 801
    .end local v3    # "currentPackageName":Ljava/lang/String;
    .end local v5    # "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catchall_2
    move-exception v9

    monitor-exit v10
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_2

    throw v9

    .line 822
    :cond_7
    new-instance v2, Ljava/io/File;

    invoke-direct {v2, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 823
    .local v2, "containerToRemove":Ljava/io/File;
    invoke-virtual {v2}, Ljava/io/File;->delete()Z

    move-result v9

    if-nez v9, :cond_8

    .line 824
    sget-object v9, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v11, Ljava/lang/StringBuilder;

    const-string v12, "It was impossible to delete "

    invoke-direct {v11, v12}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v11, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-static {v9, v11}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 829
    :cond_8
    iget-object v11, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    monitor-enter v11

    .line 831
    :try_start_5
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v9}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v9

    invoke-interface {v9}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v5

    .line 833
    .restart local v5    # "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_9
    :goto_2
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-nez v9, :cond_a

    .line 829
    monitor-exit v11

    move-object v9, v10

    .line 842
    goto/16 :goto_0

    .line 835
    :cond_a
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 837
    .restart local v3    # "currentPackageName":Ljava/lang/String;
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v9, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    invoke-virtual {v9, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_9

    .line 838
    invoke-interface {v5}, Ljava/util/Iterator;->remove()V

    goto :goto_2

    .line 829
    .end local v3    # "currentPackageName":Ljava/lang/String;
    .end local v5    # "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catchall_3
    move-exception v9

    monitor-exit v11
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_3

    throw v9

    .end local v2    # "containerToRemove":Ljava/io/File;
    .end local v7    # "signatureCheckIsSuccessful":Z
    :cond_b
    move-object v9, v10

    .line 849
    goto/16 :goto_0

    .line 857
    .end local v0    # "alreadyVerifiedPackageName":Z
    .end local v6    # "rootPackageNameWithCertificate":Ljava/lang/String;
    .end local v8    # "verifiedCertificate":Ljava/security/cert/X509Certificate;
    :cond_c
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mDexClassLoader:Ldalvik/system/DexClassLoader;

    invoke-virtual {v9, p1}, Ldalvik/system/DexClassLoader;->loadClass(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v9

    goto/16 :goto_0
.end method

.method setCertificateLocationMap(Ljava/util/Map;)V
    .locals 8
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 330
    .local p1, "extPackageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    if-eqz p1, :cond_0

    invoke-interface {p1}, Ljava/util/Map;->isEmpty()Z

    move-result v5

    if-nez v5, :cond_0

    .line 331
    iput-object p1, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    .line 336
    :cond_0
    iget-object v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    invoke-interface {v5}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v5

    invoke-interface {v5}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 338
    .local v4, "packageNameIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_1
    :goto_0
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v5

    if-nez v5, :cond_3

    .line 379
    iget-boolean v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->performLazyEvaluation:Z

    if-nez v5, :cond_2

    .line 385
    new-instance v1, Ljava/util/HashSet;

    iget-object v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToContainerPathMap:Ljava/util/Map;

    invoke-interface {v5}, Ljava/util/Map;->values()Ljava/util/Collection;

    move-result-object v5

    invoke-direct {v1, v5}, Ljava/util/HashSet;-><init>(Ljava/util/Collection;)V

    .line 388
    .local v1, "containersToVerifySet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    invoke-interface {v1}, Ljava/util/Set;->size()I

    move-result v5

    const/4 v6, 0x2

    if-ge v5, v6, :cond_5

    .line 391
    invoke-direct {p0}, Lit/necst/grabnrun/SecureDexClassLoader;->verifyAllContainersSignature()V

    .line 399
    .end local v1    # "containersToVerifySet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_2
    :goto_1
    return-void

    .line 340
    :cond_3
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/lang/String;

    .line 342
    .local v2, "currentPackageName":Ljava/lang/String;
    iget-object v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    invoke-interface {v5, v2}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v5

    if-nez v5, :cond_4

    .line 351
    :try_start_0
    invoke-direct {p0, v2}, Lit/necst/grabnrun/SecureDexClassLoader;->revertPackageNameToURL(Ljava/lang/String;)Ljava/net/URL;

    move-result-object v0

    .line 354
    .local v0, "certificateRemoteURL":Ljava/net/URL;
    iget-object v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    invoke-interface {v5, v2, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 356
    sget-object v5, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    const-string v7, "Package Name: "

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v6, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    .line 357
    const-string v7, "; Certificate Remote Location: "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v6

    const-string v7, ";"

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    .line 356
    invoke-static {v5, v6}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_0
    .catch Ljava/net/MalformedURLException; {:try_start_0 .. :try_end_0} :catch_0

    .line 370
    .end local v0    # "certificateRemoteURL":Ljava/net/URL;
    :cond_4
    :goto_2
    iget-object v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->packageNameToCertificateMap:Ljava/util/Map;

    invoke-interface {v5, v2}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v5

    if-eqz v5, :cond_1

    .line 375
    iget-object v5, p0, Lit/necst/grabnrun/SecureDexClassLoader;->mPackageNameTrie:Lit/necst/grabnrun/PackageNameTrie;

    invoke-virtual {v5, v2}, Lit/necst/grabnrun/PackageNameTrie;->setEntryHasAssociatedCertificate(Ljava/lang/String;)V

    goto :goto_0

    .line 359
    :catch_0
    move-exception v3

    .line 362
    .local v3, "e":Ljava/net/MalformedURLException;
    invoke-interface {v4}, Ljava/util/Iterator;->remove()V

    .line 364
    sget-object v5, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    const-string v7, "It was impossible to revert package name "

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    .line 365
    invoke-virtual {v6, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    const-string v7, " into a valid URL!"

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    .line 364
    invoke-static {v5, v6}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_2

    .line 395
    .end local v2    # "currentPackageName":Ljava/lang/String;
    .end local v3    # "e":Ljava/net/MalformedURLException;
    .restart local v1    # "containersToVerifySet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_5
    invoke-direct {p0, v1}, Lit/necst/grabnrun/SecureDexClassLoader;->verifyAllContainersSignatureConcurrently(Ljava/util/Set;)V

    goto :goto_1
.end method

.method public wipeOutPrivateAppCachedData(ZZ)V
    .locals 11
    .param p1, "containerPrivateFolder"    # Z
    .param p2, "certificatePrivateFolder"    # Z

    .prologue
    const/4 v8, 0x0

    .line 1216
    if-nez p1, :cond_0

    if-nez p2, :cond_0

    .line 1267
    :goto_0
    return-void

    .line 1218
    :cond_0
    new-instance v7, Ljava/util/ArrayList;

    invoke-direct {v7}, Ljava/util/ArrayList;-><init>()V

    .line 1220
    .local v7, "fileToEraseList":Ljava/util/List;, "Ljava/util/List<Ljava/io/File;>;"
    if-eqz p1, :cond_1

    .line 1224
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->resDownloadFolder:Ljava/io/File;

    invoke-virtual {v9}, Ljava/io/File;->listFiles()[Ljava/io/File;

    move-result-object v1

    .line 1226
    .local v1, "containerFiles":[Ljava/io/File;
    array-length v10, v1

    move v9, v8

    :goto_1
    if-lt v9, v10, :cond_4

    .line 1232
    .end local v1    # "containerFiles":[Ljava/io/File;
    :cond_1
    if-eqz p2, :cond_2

    .line 1236
    iget-object v9, p0, Lit/necst/grabnrun/SecureDexClassLoader;->certificateFolder:Ljava/io/File;

    invoke-virtual {v9}, Ljava/io/File;->listFiles()[Ljava/io/File;

    move-result-object v0

    .line 1238
    .local v0, "certificateFiles":[Ljava/io/File;
    array-length v9, v0

    :goto_2
    if-lt v8, v9, :cond_5

    .line 1244
    .end local v0    # "certificateFiles":[Ljava/io/File;
    :cond_2
    invoke-interface {v7}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v6

    .line 1246
    .local v6, "fileToEraseIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/io/File;>;"
    :cond_3
    :goto_3
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-nez v8, :cond_6

    .line 1265
    const/4 v8, 0x1

    iput-boolean v8, p0, Lit/necst/grabnrun/SecureDexClassLoader;->hasBeenWipedOut:Z

    goto :goto_0

    .line 1226
    .end local v6    # "fileToEraseIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/io/File;>;"
    .restart local v1    # "containerFiles":[Ljava/io/File;
    :cond_4
    aget-object v4, v1, v9

    .line 1228
    .local v4, "file":Ljava/io/File;
    invoke-interface {v7, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 1226
    add-int/lit8 v9, v9, 0x1

    goto :goto_1

    .line 1238
    .end local v1    # "containerFiles":[Ljava/io/File;
    .end local v4    # "file":Ljava/io/File;
    .restart local v0    # "certificateFiles":[Ljava/io/File;
    :cond_5
    aget-object v4, v0, v8

    .line 1240
    .restart local v4    # "file":Ljava/io/File;
    invoke-interface {v7, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 1238
    add-int/lit8 v8, v8, 0x1

    goto :goto_2

    .line 1248
    .end local v0    # "certificateFiles":[Ljava/io/File;
    .end local v4    # "file":Ljava/io/File;
    .restart local v6    # "fileToEraseIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/io/File;>;"
    :cond_6
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v4

    check-cast v4, Ljava/io/File;

    .line 1252
    .restart local v4    # "file":Ljava/io/File;
    invoke-virtual {v4}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v5

    .line 1253
    .local v5, "filePath":Ljava/lang/String;
    const-string v8, "."

    invoke-virtual {v5, v8}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v3

    .line 1254
    .local v3, "extensionIndex":I
    invoke-virtual {v5, v3}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v2

    .line 1256
    .local v2, "extension":Ljava/lang/String;
    const-string v8, ".apk"

    invoke-virtual {v2, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_7

    const-string v8, ".jar"

    invoke-virtual {v2, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_7

    const-string v8, ".pem"

    invoke-virtual {v2, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_3

    .line 1258
    :cond_7
    invoke-virtual {v4}, Ljava/io/File;->delete()Z

    move-result v8

    if-eqz v8, :cond_8

    .line 1259
    sget-object v8, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-static {v5}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v10

    invoke-direct {v9, v10}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v10, " has been erased."

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-static {v8, v9}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_3

    .line 1261
    :cond_8
    sget-object v8, Lit/necst/grabnrun/SecureDexClassLoader;->TAG_SECURE_DEX_CLASS_LOADER:Ljava/lang/String;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-static {v5}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v10

    invoke-direct {v9, v10}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v10, " was NOT erased."

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-static {v8, v9}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_3
.end method
