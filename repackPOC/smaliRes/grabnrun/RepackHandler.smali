.class public Lit/necst/grabnrun/RepackHandler;
.super Ljava/lang/Object;
.source "RepackHandler.java"


# static fields
.field private static activityStack:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List",
            "<",
            "Landroid/app/Activity;",
            ">;"
        }
    .end annotation
.end field

.field private static containerToPackageNamesMap:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;>;"
        }
    .end annotation
.end field

.field private static gotUserInput:Z

.field private static hasStaticAssociativeMap:Z

.field private static mSecureLoaderFactory:Lit/necst/grabnrun/SecureLoaderFactory;

.field private static messageDigest:Ljava/security/MessageDigest;

.field private static packageNameToCertificateURLMap:Ljava/util/Map;
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


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .prologue
    .line 40
    const/4 v0, 0x0

    sput-boolean v0, Lit/necst/grabnrun/RepackHandler;->gotUserInput:Z

    .line 60
    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .prologue
    .line 37
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static cleanUpFinishedActivities()V
    .locals 3

    .prologue
    .line 76
    sget-object v2, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v2}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    .line 80
    .local v0, "activityStackIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Landroid/app/Activity;>;"
    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-nez v2, :cond_1

    .line 90
    return-void

    .line 82
    :cond_1
    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/app/Activity;

    .line 84
    .local v1, "currentActivity":Landroid/app/Activity;
    if-eqz v1, :cond_2

    invoke-virtual {v1}, Landroid/app/Activity;->isFinishing()Z

    move-result v2

    if-eqz v2, :cond_0

    .line 87
    :cond_2
    invoke-interface {v0}, Ljava/util/Iterator;->remove()V

    goto :goto_0
.end method

.method private static computeDigestEncodeFromFilePath(Ljava/lang/String;)Ljava/lang/String;
    .locals 11
    .param p0, "filePath"    # Ljava/lang/String;

    .prologue
    const/4 v10, -0x1

    const/4 v8, 0x0

    .line 310
    const/4 v5, 0x0

    .line 311
    .local v5, "inStream":Ljava/io/FileInputStream;
    const/4 v2, 0x0

    .line 314
    .local v2, "digestString":Ljava/lang/String;
    if-eqz p0, :cond_0

    new-instance v9, Ljava/io/File;

    invoke-direct {v9, p0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual {v9}, Ljava/io/File;->exists()Z

    move-result v9

    if-nez v9, :cond_1

    .line 358
    :cond_0
    :goto_0
    return-object v8

    .line 317
    :cond_1
    const-string v9, "."

    invoke-virtual {p0, v9}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v4

    .line 319
    .local v4, "extensionIndex":I
    if-eq v4, v10, :cond_0

    .line 321
    invoke-virtual {p0, v4}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v3

    .line 323
    .local v3, "extension":Ljava/lang/String;
    const-string v9, ".apk"

    invoke-virtual {v3, v9}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-nez v9, :cond_2

    const-string v9, ".jar"

    invoke-virtual {v3, v9}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_0

    .line 327
    :cond_2
    :try_start_0
    new-instance v6, Ljava/io/FileInputStream;

    invoke-direct {v6, p0}, Ljava/io/FileInputStream;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_7
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_2
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 329
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .local v6, "inStream":Ljava/io/FileInputStream;
    const/16 v8, 0x2000

    :try_start_1
    new-array v0, v8, [B

    .line 331
    .local v0, "buffer":[B
    :goto_1
    invoke-virtual {v6, v0}, Ljava/io/FileInputStream;->read([B)I

    move-result v7

    .local v7, "length":I
    if-ne v7, v10, :cond_4

    .line 336
    sget-object v8, Lit/necst/grabnrun/RepackHandler;->messageDigest:Ljava/security/MessageDigest;

    invoke-virtual {v8}, Ljava/security/MessageDigest;->digest()[B

    move-result-object v1

    .line 340
    .local v1, "digestBytes":[B
    const/16 v8, 0x8

    invoke-static {v1, v8}, Landroid/util/Base64;->encodeToString([BI)Ljava/lang/String;

    move-result-object v2

    .line 341
    const-string v8, "line.separator"

    invoke-static {v8}, Ljava/lang/System;->getProperty(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v8

    const-string v9, ""

    invoke-virtual {v2, v8, v9}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;

    move-result-object v8

    const-string v9, "\r"

    const-string v10, ""

    invoke-virtual {v8, v9, v10}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    :try_end_1
    .catch Ljava/io/FileNotFoundException; {:try_start_1 .. :try_end_1} :catch_0
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_6
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    move-result-object v2

    .line 348
    if-eqz v6, :cond_6

    .line 350
    :try_start_2
    invoke-virtual {v6}, Ljava/io/FileInputStream;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_4

    move-object v5, v6

    .end local v0    # "buffer":[B
    .end local v1    # "digestBytes":[B
    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .end local v7    # "length":I
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    :cond_3
    :goto_2
    move-object v8, v2

    .line 358
    goto :goto_0

    .line 333
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v0    # "buffer":[B
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v7    # "length":I
    :cond_4
    :try_start_3
    sget-object v8, Lit/necst/grabnrun/RepackHandler;->messageDigest:Ljava/security/MessageDigest;

    const/4 v9, 0x0

    invoke-virtual {v8, v0, v9, v7}, Ljava/security/MessageDigest;->update([BII)V
    :try_end_3
    .catch Ljava/io/FileNotFoundException; {:try_start_3 .. :try_end_3} :catch_0
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_6
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    goto :goto_1

    .line 343
    .end local v0    # "buffer":[B
    .end local v7    # "length":I
    :catch_0
    move-exception v8

    move-object v5, v6

    .line 348
    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    :goto_3
    if-eqz v5, :cond_3

    .line 350
    :try_start_4
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_1

    goto :goto_2

    .line 351
    :catch_1
    move-exception v8

    goto :goto_2

    .line 345
    :catch_2
    move-exception v8

    .line 348
    :goto_4
    if-eqz v5, :cond_3

    .line 350
    :try_start_5
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_3

    goto :goto_2

    .line 351
    :catch_3
    move-exception v8

    goto :goto_2

    .line 347
    :catchall_0
    move-exception v8

    .line 348
    :goto_5
    if-eqz v5, :cond_5

    .line 350
    :try_start_6
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_5

    .line 355
    :cond_5
    :goto_6
    throw v8

    .line 351
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v0    # "buffer":[B
    .restart local v1    # "digestBytes":[B
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v7    # "length":I
    :catch_4
    move-exception v8

    move-object v5, v6

    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    goto :goto_2

    .end local v0    # "buffer":[B
    .end local v1    # "digestBytes":[B
    .end local v7    # "length":I
    :catch_5
    move-exception v9

    goto :goto_6

    .line 347
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    :catchall_1
    move-exception v8

    move-object v5, v6

    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    goto :goto_5

    .line 345
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    :catch_6
    move-exception v8

    move-object v5, v6

    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    goto :goto_4

    .line 343
    :catch_7
    move-exception v8

    goto :goto_3

    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v0    # "buffer":[B
    .restart local v1    # "digestBytes":[B
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v7    # "length":I
    :cond_6
    move-object v5, v6

    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    goto :goto_2
.end method

.method public static enqueRunningActivity(Landroid/app/Activity;)V
    .locals 1
    .param p0, "newActivity"    # Landroid/app/Activity;

    .prologue
    .line 65
    sget-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    if-nez v0, :cond_0

    .line 66
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    .line 69
    :cond_0
    if-eqz p0, :cond_1

    .line 70
    sget-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v0, p0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 71
    :cond_1
    return-void
.end method

.method public static generateSecureDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)Lit/necst/grabnrun/SecureDexClassLoader;
    .locals 17
    .param p0, "dexPath"    # Ljava/lang/String;
    .param p1, "optimizedDirectory"    # Ljava/lang/String;
    .param p2, "libraryPath"    # Ljava/lang/String;
    .param p3, "parent"    # Ljava/lang/ClassLoader;

    .prologue
    .line 178
    sget-boolean v13, Lit/necst/grabnrun/RepackHandler;->gotUserInput:Z

    if-nez v13, :cond_0

    .line 179
    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->initializeUserInput()V

    .line 182
    :cond_0
    new-instance v7, Ljava/util/HashMap;

    invoke-direct {v7}, Ljava/util/HashMap;-><init>()V

    .line 184
    .local v7, "finalAssociativeMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    sget-boolean v13, Lit/necst/grabnrun/RepackHandler;->hasStaticAssociativeMap:Z

    if-eqz v13, :cond_2

    .line 188
    sget-object v13, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    invoke-interface {v13}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v13

    invoke-interface {v13}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v13

    invoke-static {v7, v13}, Lit/necst/grabnrun/RepackHandler;->insertURLEntriesInMap(Ljava/util/Map;Ljava/util/Iterator;)V

    .line 277
    :cond_1
    new-instance v13, Lit/necst/grabnrun/SecureLoaderFactory;

    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->getLastRunningActivity()Landroid/app/Activity;

    move-result-object v14

    invoke-direct {v13, v14}, Lit/necst/grabnrun/SecureLoaderFactory;-><init>(Landroid/content/ContextWrapper;)V

    sput-object v13, Lit/necst/grabnrun/RepackHandler;->mSecureLoaderFactory:Lit/necst/grabnrun/SecureLoaderFactory;

    .line 280
    const-string v13, "Repack Handler"

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "Original dexPath: "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, p0

    invoke-virtual {v14, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 281
    const-string v13, "Repack Handler"

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "Optimized cached dex directory: "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, p1

    invoke-virtual {v14, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 284
    sget-object v13, Lit/necst/grabnrun/RepackHandler;->mSecureLoaderFactory:Lit/necst/grabnrun/SecureLoaderFactory;

    move-object/from16 v0, p0

    move-object/from16 v1, p2

    move-object/from16 v2, p3

    invoke-virtual {v13, v0, v1, v2, v7}, Lit/necst/grabnrun/SecureLoaderFactory;->createDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/util/Map;)Lit/necst/grabnrun/SecureDexClassLoader;

    move-result-object v13

    return-object v13

    .line 197
    :cond_2
    const-string v13, "http://"

    const-string v14, "http//"

    move-object/from16 v0, p0

    invoke-virtual {v0, v13, v14}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v12

    .line 198
    .local v12, "tempPath":Ljava/lang/String;
    const-string v13, "https://"

    const-string v14, "https//"

    invoke-virtual {v12, v13, v14}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v12

    .line 201
    sget-object v13, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-static {v13}, Ljava/util/regex/Pattern;->quote(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v12, v13}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v11

    .line 203
    .local v11, "strings":[Ljava/lang/String;
    array-length v15, v11

    const/4 v13, 0x0

    move v14, v13

    :goto_0
    if-ge v14, v15, :cond_1

    aget-object v10, v11, v14

    .line 209
    .local v10, "path":Ljava/lang/String;
    const-string v13, "http//"

    invoke-virtual {v10, v13}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v13

    if-nez v13, :cond_3

    const-string v13, "https//"

    invoke-virtual {v10, v13}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v13

    if-eqz v13, :cond_6

    .line 213
    :cond_3
    const-string v13, "http//"

    invoke-virtual {v10, v13}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v13

    if-eqz v13, :cond_5

    .line 214
    new-instance v13, Ljava/lang/StringBuilder;

    const-string v16, "http:"

    move-object/from16 v0, v16

    invoke-direct {v13, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const/16 v16, 0x4

    move/from16 v0, v16

    invoke-virtual {v10, v0}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v16

    move-object/from16 v0, v16

    invoke-virtual {v13, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 226
    .local v3, "containerIdentifier":Ljava/lang/String;
    :goto_1
    if-eqz v3, :cond_4

    .line 229
    sget-object v13, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;

    invoke-interface {v13, v3}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_7

    .line 232
    sget-object v13, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;

    invoke-interface {v13, v3}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v8

    check-cast v8, Ljava/util/Set;

    .line 235
    .local v8, "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v13

    invoke-static {v7, v13}, Lit/necst/grabnrun/RepackHandler;->insertURLEntriesInMap(Ljava/util/Map;Ljava/util/Iterator;)V

    .line 203
    .end local v8    # "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_4
    :goto_2
    add-int/lit8 v13, v14, 0x1

    move v14, v13

    goto :goto_0

    .line 216
    .end local v3    # "containerIdentifier":Ljava/lang/String;
    :cond_5
    new-instance v13, Ljava/lang/StringBuilder;

    const-string v16, "https:"

    move-object/from16 v0, v16

    invoke-direct {v13, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const/16 v16, 0x5

    move/from16 v0, v16

    invoke-virtual {v10, v0}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v16

    move-object/from16 v0, v16

    invoke-virtual {v13, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 218
    .restart local v3    # "containerIdentifier":Ljava/lang/String;
    goto :goto_1

    .line 222
    .end local v3    # "containerIdentifier":Ljava/lang/String;
    :cond_6
    invoke-static {v10}, Lit/necst/grabnrun/RepackHandler;->computeDigestEncodeFromFilePath(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v3

    .restart local v3    # "containerIdentifier":Ljava/lang/String;
    goto :goto_1

    .line 241
    :cond_7
    sget-object v13, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    const-string v16, "default"

    move-object/from16 v0, v16

    invoke-interface {v13, v0}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_4

    .line 243
    sget-object v13, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    const-string v16, "default"

    move-object/from16 v0, v16

    invoke-interface {v13, v0}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v4

    check-cast v4, Ljava/lang/String;

    .line 248
    .local v4, "defaultRemoteCert":Ljava/lang/String;
    :try_start_0
    new-instance v5, Ljava/net/URL;

    invoke-direct {v5, v4}, Ljava/net/URL;-><init>(Ljava/lang/String;)V

    .line 252
    .local v5, "defaultRemoteCertURL":Ljava/net/URL;
    invoke-static {v10}, Lit/necst/grabnrun/RepackHandler;->getPackageNamesFromContainerPath(Ljava/lang/String;)Ljava/util/Set;

    move-result-object v8

    .line 254
    .restart local v8    # "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    if-eqz v8, :cond_4

    .line 256
    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v9

    .line 258
    .local v9, "packageNamesSetIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :goto_3
    invoke-interface {v9}, Ljava/util/Iterator;->hasNext()Z

    move-result v13

    if-eqz v13, :cond_4

    .line 262
    invoke-interface {v9}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v13

    check-cast v13, Ljava/lang/String;

    invoke-interface {v7, v13, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_0
    .catch Ljava/net/MalformedURLException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_3

    .line 266
    .end local v5    # "defaultRemoteCertURL":Ljava/net/URL;
    .end local v8    # "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    .end local v9    # "packageNamesSetIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catch_0
    move-exception v6

    .line 268
    .local v6, "e":Ljava/net/MalformedURLException;
    invoke-virtual {v6}, Ljava/net/MalformedURLException;->printStackTrace()V

    goto :goto_2
.end method

.method private static getLastRunningActivity()Landroid/app/Activity;
    .locals 2

    .prologue
    const/4 v0, 0x0

    .line 94
    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    if-eqz v1, :cond_0

    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->isEmpty()Z

    move-result v1

    if-eqz v1, :cond_1

    .line 104
    :cond_0
    :goto_0
    return-object v0

    .line 98
    :cond_1
    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->cleanUpFinishedActivities()V

    .line 100
    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->isEmpty()Z

    move-result v1

    if-nez v1, :cond_0

    .line 104
    sget-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->size()I

    move-result v1

    add-int/lit8 v1, v1, -0x1

    invoke-interface {v0, v1}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/app/Activity;

    goto :goto_0
.end method

.method private static getPackageNamesFromContainerPath(Ljava/lang/String;)Ljava/util/Set;
    .locals 20
    .param p0, "containerPath"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            ")",
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation

    .prologue
    .line 369
    if-eqz p0, :cond_0

    invoke-virtual/range {p0 .. p0}, Ljava/lang/String;->isEmpty()Z

    move-result v18

    if-nez v18, :cond_0

    new-instance v18, Ljava/io/File;

    move-object/from16 v0, v18

    move-object/from16 v1, p0

    invoke-direct {v0, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual/range {v18 .. v18}, Ljava/io/File;->exists()Z

    move-result v18

    if-nez v18, :cond_1

    :cond_0
    const/16 v17, 0x0

    .line 490
    :goto_0
    return-object v17

    .line 372
    :cond_1
    const-string v18, "."

    move-object/from16 v0, p0

    move-object/from16 v1, v18

    invoke-virtual {v0, v1}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v8

    .line 374
    .local v8, "extensionIndex":I
    const/16 v18, -0x1

    move/from16 v0, v18

    if-ne v8, v0, :cond_2

    const/16 v17, 0x0

    goto :goto_0

    .line 376
    :cond_2
    move-object/from16 v0, p0

    invoke-virtual {v0, v8}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v7

    .line 378
    .local v7, "extension":Ljava/lang/String;
    new-instance v17, Ljava/util/HashSet;

    invoke-direct/range {v17 .. v17}, Ljava/util/HashSet;-><init>()V

    .line 380
    .local v17, "packageNameSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->getLastRunningActivity()Landroid/app/Activity;

    move-result-object v12

    .line 382
    .local v12, "lastRunningActivity":Landroid/content/ContextWrapper;
    if-nez v12, :cond_3

    const/16 v17, 0x0

    goto :goto_0

    .line 384
    :cond_3
    const-string v18, ".apk"

    move-object/from16 v0, v18

    invoke-virtual {v7, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v18

    if-eqz v18, :cond_5

    .line 388
    invoke-virtual {v12}, Landroid/content/ContextWrapper;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v13

    .line 390
    .local v13, "mPackageManager":Landroid/content/pm/PackageManager;
    const/16 v18, 0x0

    move-object/from16 v0, p0

    move/from16 v1, v18

    invoke-virtual {v13, v0, v1}, Landroid/content/pm/PackageManager;->getPackageArchiveInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v18

    if-eqz v18, :cond_4

    .line 392
    const/16 v18, 0x0

    move-object/from16 v0, p0

    move/from16 v1, v18

    invoke-virtual {v13, v0, v1}, Landroid/content/pm/PackageManager;->getPackageArchiveInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;

    move-result-object v18

    move-object/from16 v0, v18

    iget-object v0, v0, Landroid/content/pm/PackageInfo;->packageName:Ljava/lang/String;

    move-object/from16 v18, v0

    invoke-interface/range {v17 .. v18}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    goto :goto_0

    .line 396
    :cond_4
    const/16 v17, 0x0

    goto :goto_0

    .line 399
    .end local v13    # "mPackageManager":Landroid/content/pm/PackageManager;
    :cond_5
    const-string v18, ".jar"

    move-object/from16 v0, v18

    invoke-virtual {v7, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v18

    if-eqz v18, :cond_e

    .line 406
    const/4 v10, 0x0

    .line 407
    .local v10, "isAValidJar":Z
    const/4 v2, 0x0

    .line 412
    .local v2, "containerJar":Ljava/util/jar/JarFile;
    :try_start_0
    new-instance v3, Ljava/util/jar/JarFile;

    move-object/from16 v0, p0

    invoke-direct {v3, v0}, Ljava/util/jar/JarFile;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_1
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 415
    .end local v2    # "containerJar":Ljava/util/jar/JarFile;
    .local v3, "containerJar":Ljava/util/jar/JarFile;
    :try_start_1
    const-string v18, "classes.dex"

    move-object/from16 v0, v18

    invoke-virtual {v3, v0}, Ljava/util/jar/JarFile;->getJarEntry(Ljava/lang/String;)Ljava/util/jar/JarEntry;
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_5
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    move-result-object v18

    if-eqz v18, :cond_6

    .line 416
    const/4 v10, 0x1

    .line 421
    :cond_6
    if-eqz v3, :cond_7

    .line 423
    :try_start_2
    invoke-virtual {v3}, Ljava/util/jar/JarFile;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_4

    .line 429
    :cond_7
    :goto_1
    if-eqz v10, :cond_d

    .line 432
    const/4 v5, 0x0

    .line 442
    .local v5, "dexFile":Ldalvik/system/DexFile;
    :try_start_3
    const-string v18, "packagesExtractor"

    const/16 v19, 0x0

    move-object/from16 v0, v18

    move/from16 v1, v19

    invoke-virtual {v12, v0, v1}, Landroid/content/ContextWrapper;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v15

    .line 445
    .local v15, "packageExtractFolder":Ljava/io/File;
    new-instance v18, Ljava/lang/StringBuilder;

    invoke-virtual {v15}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v19

    invoke-static/range {v19 .. v19}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v19

    invoke-direct/range {v18 .. v19}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v19, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual/range {v18 .. v19}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v18

    const-string v19, "container.odex"

    invoke-virtual/range {v18 .. v19}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v18

    invoke-virtual/range {v18 .. v18}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    .line 448
    .local v14, "outputDexTempPath":Ljava/lang/String;
    const/16 v18, 0x0

    move-object/from16 v0, p0

    move/from16 v1, v18

    invoke-static {v0, v14, v1}, Ldalvik/system/DexFile;->loadDex(Ljava/lang/String;Ljava/lang/String;I)Ldalvik/system/DexFile;

    move-result-object v5

    .line 450
    invoke-virtual {v5}, Ldalvik/system/DexFile;->entries()Ljava/util/Enumeration;

    move-result-object v4

    .line 452
    .local v4, "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    :cond_8
    :goto_2
    invoke-interface {v4}, Ljava/util/Enumeration;->hasMoreElements()Z

    move-result v18

    if-nez v18, :cond_b

    .line 474
    new-instance v18, Ljava/io/File;

    move-object/from16 v0, v18

    invoke-direct {v0, v14}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual/range {v18 .. v18}, Ljava/io/File;->delete()Z
    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_0

    goto/16 :goto_0

    .line 476
    .end local v4    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .end local v14    # "outputDexTempPath":Ljava/lang/String;
    .end local v15    # "packageExtractFolder":Ljava/io/File;
    :catch_0
    move-exception v6

    .line 478
    .local v6, "e":Ljava/io/IOException;
    const/16 v17, 0x0

    goto/16 :goto_0

    .line 418
    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .end local v5    # "dexFile":Ldalvik/system/DexFile;
    .end local v6    # "e":Ljava/io/IOException;
    .restart local v2    # "containerJar":Ljava/util/jar/JarFile;
    :catch_1
    move-exception v6

    .line 421
    .restart local v6    # "e":Ljava/io/IOException;
    :goto_3
    if-eqz v2, :cond_9

    .line 423
    :try_start_4
    invoke-virtual {v2}, Ljava/util/jar/JarFile;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_2

    .line 419
    :cond_9
    :goto_4
    const/16 v17, 0x0

    goto/16 :goto_0

    .line 424
    :catch_2
    move-exception v6

    .line 425
    invoke-virtual {v6}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_4

    .line 420
    .end local v6    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v18

    .line 421
    :goto_5
    if-eqz v2, :cond_a

    .line 423
    :try_start_5
    invoke-virtual {v2}, Ljava/util/jar/JarFile;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_3

    .line 427
    :cond_a
    :goto_6
    throw v18

    .line 424
    :catch_3
    move-exception v6

    .line 425
    .restart local v6    # "e":Ljava/io/IOException;
    invoke-virtual {v6}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_6

    .line 424
    .end local v2    # "containerJar":Ljava/util/jar/JarFile;
    .end local v6    # "e":Ljava/io/IOException;
    .restart local v3    # "containerJar":Ljava/util/jar/JarFile;
    :catch_4
    move-exception v6

    .line 425
    .restart local v6    # "e":Ljava/io/IOException;
    invoke-virtual {v6}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_1

    .line 455
    .end local v6    # "e":Ljava/io/IOException;
    .restart local v4    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .restart local v5    # "dexFile":Ldalvik/system/DexFile;
    .restart local v14    # "outputDexTempPath":Ljava/lang/String;
    .restart local v15    # "packageExtractFolder":Ljava/io/File;
    :cond_b
    :try_start_6
    invoke-interface {v4}, Ljava/util/Enumeration;->nextElement()Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    .line 460
    .local v9, "fullClassName":Ljava/lang/String;
    :goto_7
    const-string v18, " "

    move-object/from16 v0, v18

    invoke-virtual {v9, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v18

    if-nez v18, :cond_c

    .line 463
    const-string v18, "."

    move-object/from16 v0, v18

    invoke-virtual {v9, v0}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v11

    .line 465
    .local v11, "lastIndexPackageName":I
    const/16 v18, -0x1

    move/from16 v0, v18

    if-eq v11, v0, :cond_8

    .line 467
    const/16 v18, 0x0

    move/from16 v0, v18

    invoke-virtual {v9, v0, v11}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v16

    .line 468
    .local v16, "packageName":Ljava/lang/String;
    move-object/from16 v0, v17

    move-object/from16 v1, v16

    invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    goto :goto_2

    .line 461
    .end local v11    # "lastIndexPackageName":I
    .end local v16    # "packageName":Ljava/lang/String;
    :cond_c
    const/16 v18, 0x1

    invoke-virtual {v9}, Ljava/lang/String;->length()I

    move-result v19

    move/from16 v0, v18

    move/from16 v1, v19

    invoke-virtual {v9, v0, v1}, Ljava/lang/String;->substring(II)Ljava/lang/String;
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_0

    move-result-object v9

    goto :goto_7

    .line 485
    .end local v4    # "dexEntries":Ljava/util/Enumeration;, "Ljava/util/Enumeration<Ljava/lang/String;>;"
    .end local v5    # "dexFile":Ldalvik/system/DexFile;
    .end local v9    # "fullClassName":Ljava/lang/String;
    .end local v14    # "outputDexTempPath":Ljava/lang/String;
    .end local v15    # "packageExtractFolder":Ljava/io/File;
    :cond_d
    const/16 v17, 0x0

    goto/16 :goto_0

    .line 490
    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .end local v10    # "isAValidJar":Z
    :cond_e
    const/16 v17, 0x0

    goto/16 :goto_0

    .line 420
    .restart local v3    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v10    # "isAValidJar":Z
    :catchall_1
    move-exception v18

    move-object v2, v3

    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v2    # "containerJar":Ljava/util/jar/JarFile;
    goto :goto_5

    .line 418
    .end local v2    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v3    # "containerJar":Ljava/util/jar/JarFile;
    :catch_5
    move-exception v6

    move-object v2, v3

    .end local v3    # "containerJar":Ljava/util/jar/JarFile;
    .restart local v2    # "containerJar":Ljava/util/jar/JarFile;
    goto :goto_3
.end method

.method private static initializeUserInput()V
    .locals 5

    .prologue
    .line 112
    new-instance v2, Ljava/util/HashMap;

    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    sput-object v2, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;

    .line 113
    new-instance v2, Ljava/util/HashMap;

    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    sput-object v2, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    .line 116
    new-instance v1, Ljava/util/HashSet;

    invoke-direct {v1}, Ljava/util/HashSet;-><init>()V

    .line 119
    .local v1, "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"

