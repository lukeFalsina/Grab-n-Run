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
    .line 33
    const/4 v0, 0x0

    sput-boolean v0, Lit/necst/grabnrun/RepackHandler;->gotUserInput:Z

    .line 53
    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .prologue
    .line 30
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static cleanUpFinishedActivities()V
    .locals 3

    .prologue
    .line 69
    sget-object v2, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v2}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    .line 73
    .local v0, "activityStackIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Landroid/app/Activity;>;"
    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-nez v2, :cond_1

    .line 83
    return-void

    .line 75
    :cond_1
    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/app/Activity;

    .line 77
    .local v1, "currentActivity":Landroid/app/Activity;
    if-eqz v1, :cond_2

    invoke-virtual {v1}, Landroid/app/Activity;->isFinishing()Z

    move-result v2

    if-eqz v2, :cond_0

    .line 80
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

    .line 265
    const/4 v5, 0x0

    .line 266
    .local v5, "inStream":Ljava/io/FileInputStream;
    const/4 v2, 0x0

    .line 269
    .local v2, "digestString":Ljava/lang/String;
    if-eqz p0, :cond_0

    new-instance v9, Ljava/io/File;

    invoke-direct {v9, p0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual {v9}, Ljava/io/File;->exists()Z

    move-result v9

    if-nez v9, :cond_1

    .line 313
    :cond_0
    :goto_0
    return-object v8

    .line 272
    :cond_1
    const-string v9, "."

    invoke-virtual {p0, v9}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v4

    .line 274
    .local v4, "extensionIndex":I
    if-eq v4, v10, :cond_0

    .line 276
    invoke-virtual {p0, v4}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v3

    .line 278
    .local v3, "extension":Ljava/lang/String;
    const-string v9, ".apk"

    invoke-virtual {v3, v9}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-nez v9, :cond_2

    const-string v9, ".jar"

    invoke-virtual {v3, v9}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_0

    .line 282
    :cond_2
    :try_start_0
    new-instance v6, Ljava/io/FileInputStream;

    invoke-direct {v6, p0}, Ljava/io/FileInputStream;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_7
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_2
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 284
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .local v6, "inStream":Ljava/io/FileInputStream;
    const/16 v8, 0x2000

    :try_start_1
    new-array v0, v8, [B

    .line 286
    .local v0, "buffer":[B
    :goto_1
    invoke-virtual {v6, v0}, Ljava/io/FileInputStream;->read([B)I

    move-result v7

    .local v7, "length":I
    if-ne v7, v10, :cond_4

    .line 291
    sget-object v8, Lit/necst/grabnrun/RepackHandler;->messageDigest:Ljava/security/MessageDigest;

    invoke-virtual {v8}, Ljava/security/MessageDigest;->digest()[B

    move-result-object v1

    .line 295
    .local v1, "digestBytes":[B
    const/16 v8, 0x8

    invoke-static {v1, v8}, Landroid/util/Base64;->encodeToString([BI)Ljava/lang/String;

    move-result-object v2

    .line 296
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

    .line 303
    if-eqz v6, :cond_6

    .line 305
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

    .line 313
    goto :goto_0

    .line 288
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

    .line 298
    .end local v0    # "buffer":[B
    .end local v7    # "length":I
    :catch_0
    move-exception v8

    move-object v5, v6

    .line 303
    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    :goto_3
    if-eqz v5, :cond_3

    .line 305
    :try_start_4
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_1

    goto :goto_2

    .line 306
    :catch_1
    move-exception v8

    goto :goto_2

    .line 300
    :catch_2
    move-exception v8

    .line 303
    :goto_4
    if-eqz v5, :cond_3

    .line 305
    :try_start_5
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_3

    goto :goto_2

    .line 306
    :catch_3
    move-exception v8

    goto :goto_2

    .line 302
    :catchall_0
    move-exception v8

    .line 303
    :goto_5
    if-eqz v5, :cond_5

    .line 305
    :try_start_6
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_5

    .line 310
    :cond_5
    :goto_6
    throw v8

    .line 306
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

    .line 302
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    :catchall_1
    move-exception v8

    move-object v5, v6

    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    goto :goto_5

    .line 300
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v6    # "inStream":Ljava/io/FileInputStream;
    :catch_6
    move-exception v8

    move-object v5, v6

    .end local v6    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    goto :goto_4

    .line 298
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
    .line 58
    sget-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    if-nez v0, :cond_0

    .line 59
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    .line 62
    :cond_0
    if-eqz p0, :cond_1

    .line 63
    sget-object v0, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v0, p0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 64
    :cond_1
    return-void
.end method

.method public static generateSecureDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)Lit/necst/grabnrun/SecureDexClassLoader;
    .locals 10
    .param p0, "dexPath"    # Ljava/lang/String;
    .param p1, "optimizedDirectory"    # Ljava/lang/String;
    .param p2, "libraryPath"    # Ljava/lang/String;
    .param p3, "parent"    # Ljava/lang/ClassLoader;

    .prologue
    .line 171
    sget-boolean v6, Lit/necst/grabnrun/RepackHandler;->gotUserInput:Z

    if-nez v6, :cond_0

    .line 172
    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->initializeUserInput()V

    .line 175
    :cond_0
    new-instance v1, Ljava/util/HashMap;

    invoke-direct {v1}, Ljava/util/HashMap;-><init>()V

    .line 177
    .local v1, "finalAssociativeMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    sget-boolean v6, Lit/necst/grabnrun/RepackHandler;->hasStaticAssociativeMap:Z

    if-eqz v6, :cond_2

    .line 181
    sget-object v6, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v6

    invoke-interface {v6}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v6

    invoke-static {v1, v6}, Lit/necst/grabnrun/RepackHandler;->insertURLEntriesInMap(Ljava/util/Map;Ljava/util/Iterator;)V

    .line 236
    :cond_1
    new-instance v6, Lit/necst/grabnrun/SecureLoaderFactory;

    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->getLastRunningActivity()Landroid/app/Activity;

    move-result-object v7

    invoke-direct {v6, v7}, Lit/necst/grabnrun/SecureLoaderFactory;-><init>(Landroid/content/ContextWrapper;)V

    sput-object v6, Lit/necst/grabnrun/RepackHandler;->mSecureLoaderFactory:Lit/necst/grabnrun/SecureLoaderFactory;

    .line 239
    sget-object v6, Lit/necst/grabnrun/RepackHandler;->mSecureLoaderFactory:Lit/necst/grabnrun/SecureLoaderFactory;

    invoke-virtual {v6, p0, p2, p3, v1}, Lit/necst/grabnrun/SecureLoaderFactory;->createDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/util/Map;)Lit/necst/grabnrun/SecureDexClassLoader;

    move-result-object v6

    return-object v6

    .line 190
    :cond_2
    const-string v6, "http://"

    const-string v7, "http//"

    invoke-virtual {p0, v6, v7}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v5

    .line 191
    .local v5, "tempPath":Ljava/lang/String;
    const-string v6, "https://"

    const-string v7, "https//"

    invoke-virtual {v5, v6, v7}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v5

    .line 194
    sget-object v6, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-static {v6}, Ljava/util/regex/Pattern;->quote(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 196
    .local v4, "strings":[Ljava/lang/String;
    array-length v7, v4

    const/4 v6, 0x0

    :goto_0
    if-ge v6, v7, :cond_1

    aget-object v3, v4, v6

    .line 202
    .local v3, "path":Ljava/lang/String;
    const-string v8, "http//"

    invoke-virtual {v3, v8}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v8

    if-nez v8, :cond_3

    const-string v8, "https//"

    invoke-virtual {v3, v8}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v8

    if-eqz v8, :cond_6

    .line 206
    :cond_3
    const-string v8, "http//"

    invoke-virtual {v3, v8}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v8

    if-eqz v8, :cond_5

    .line 207
    new-instance v8, Ljava/lang/StringBuilder;

    const-string v9, "http:"

    invoke-direct {v8, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const/4 v9, 0x4

    invoke-virtual {v3, v9}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 219
    .local v0, "containerIdentifier":Ljava/lang/String;
    :goto_1
    if-eqz v0, :cond_4

    .line 222
    sget-object v8, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;

    invoke-interface {v8, v0}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v8

    if-eqz v8, :cond_4

    .line 225
    sget-object v8, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;

    invoke-interface {v8, v0}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/util/Set;

    .line 228
    .local v2, "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    invoke-interface {v2}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v8

    invoke-static {v1, v8}, Lit/necst/grabnrun/RepackHandler;->insertURLEntriesInMap(Ljava/util/Map;Ljava/util/Iterator;)V

    .line 196
    .end local v2    # "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    :cond_4
    add-int/lit8 v6, v6, 0x1

    goto :goto_0

    .line 209
    .end local v0    # "containerIdentifier":Ljava/lang/String;
    :cond_5
    new-instance v8, Ljava/lang/StringBuilder;

    const-string v9, "https:"

    invoke-direct {v8, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const/4 v9, 0x5

    invoke-virtual {v3, v9}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 211
    .restart local v0    # "containerIdentifier":Ljava/lang/String;
    goto :goto_1

    .line 215
    .end local v0    # "containerIdentifier":Ljava/lang/String;
    :cond_6
    invoke-static {v3}, Lit/necst/grabnrun/RepackHandler;->computeDigestEncodeFromFilePath(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .restart local v0    # "containerIdentifier":Ljava/lang/String;
    goto :goto_1
.end method

.method private static getLastRunningActivity()Landroid/app/Activity;
    .locals 2

    .prologue
    const/4 v0, 0x0

    .line 87
    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    if-eqz v1, :cond_0

    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->isEmpty()Z

    move-result v1

    if-eqz v1, :cond_1

    .line 97
    :cond_0
    :goto_0
    return-object v0

    .line 91
    :cond_1
    invoke-static {}, Lit/necst/grabnrun/RepackHandler;->cleanUpFinishedActivities()V

    .line 93
    sget-object v1, Lit/necst/grabnrun/RepackHandler;->activityStack:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->isEmpty()Z

    move-result v1

    if-nez v1, :cond_0

    .line 97
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

.method private static initializeUserInput()V
    .locals 5

    .prologue
    .line 105
    new-instance v2, Ljava/util/HashMap;

    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    sput-object v2, Lit/necst/grabnrun/RepackHandler;->containerToPackageNamesMap:Ljava/util/Map;

    .line 106
    new-instance v2, Ljava/util/HashMap;

    invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

    sput-object v2, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    .line 109
    new-instance v1, Ljava/util/HashSet;

    invoke-direct {v1}, Ljava/util/HashSet;-><init>()V

    .line 112
    .local v1, "packageNamesSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"

