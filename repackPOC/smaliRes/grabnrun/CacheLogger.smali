.class final Lit/necst/grabnrun/CacheLogger;
.super Ljava/lang/Object;
.source "CacheLogger.java"


# static fields
.field private static final CREATION_TIMESTAMP:I = 0x2

.field private static final ELEMENTS_PER_LOG_LINE:I = 0x3

.field private static final LOCAL_FILE_NAME:I = 0x1

.field private static final REMOTE_URL:I = 0x0

.field private static final TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

.field private static final helperFileName:Ljava/lang/String; = "helper.txt"


# instance fields
.field private cacheDirectoryPath:Ljava/lang/String;

.field private hasBeenAlreadyFinalized:Z

.field private helperFile:Ljava/io/File;

.field private remoteURLToCreationTimestamp:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/lang/Long;",
            ">;"
        }
    .end annotation
.end field

.field private remoteURLToLocalFileMap:Ljava/util/Map;
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
    .line 44
    const-class v0, Lit/necst/grabnrun/CacheLogger;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lit/necst/grabnrun/CacheLogger;->TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

    .line 62
    return-void
.end method

.method constructor <init>(Ljava/lang/String;I)V
    .locals 14
    .param p1, "cacheDirectoryPath"    # Ljava/lang/String;
    .param p2, "daysTillConsideredFresh"    # I

    .prologue
    .line 81
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 85
    new-instance v7, Ljava/util/HashMap;

    invoke-direct {v7}, Ljava/util/HashMap;-><init>()V

    iput-object v7, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    .line 86
    new-instance v7, Ljava/util/HashMap;

    invoke-direct {v7}, Ljava/util/HashMap;-><init>()V

    iput-object v7, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToCreationTimestamp:Ljava/util/Map;

    .line 88
    iput-object p1, p0, Lit/necst/grabnrun/CacheLogger;->cacheDirectoryPath:Ljava/lang/String;

    .line 90
    const/4 v7, 0x0

    iput-boolean v7, p0, Lit/necst/grabnrun/CacheLogger;->hasBeenAlreadyFinalized:Z

    .line 95
    new-instance v7, Ljava/io/File;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v11

    invoke-direct {v10, v11}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v11, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    const-string v11, "helper.txt"

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-direct {v7, v10}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    iput-object v7, p0, Lit/necst/grabnrun/CacheLogger;->helperFile:Ljava/io/File;

    .line 97
    iget-object v7, p0, Lit/necst/grabnrun/CacheLogger;->helperFile:Ljava/io/File;

    invoke-virtual {v7}, Ljava/io/File;->exists()Z

    move-result v7

    if-eqz v7, :cond_1

    .line 99
    const/4 v5, 0x0

    .line 105
    .local v5, "in":Ljava/util/Scanner;
    :try_start_0
    new-instance v7, Ljava/util/Scanner;

    iget-object v10, p0, Lit/necst/grabnrun/CacheLogger;->helperFile:Ljava/io/File;

    invoke-direct {v7, v10}, Ljava/util/Scanner;-><init>(Ljava/io/File;)V

    const-string v10, ";\n"

    invoke-virtual {v7, v10}, Ljava/util/Scanner;->useDelimiter(Ljava/lang/String;)Ljava/util/Scanner;

    move-result-object v5

    .line 107
    :cond_0
    :goto_0
    invoke-virtual {v5}, Ljava/util/Scanner;->hasNext()Z
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    move-result v7

    if-nez v7, :cond_2

    .line 146
    if-eqz v5, :cond_1

    .line 147
    invoke-virtual {v5}, Ljava/util/Scanner;->close()V

    .line 151
    .end local v5    # "in":Ljava/util/Scanner;
    :cond_1
    :goto_1
    return-void

    .line 110
    .restart local v5    # "in":Ljava/util/Scanner;
    :cond_2
    :try_start_1
    invoke-virtual {v5}, Ljava/util/Scanner;->next()Ljava/lang/String;

    move-result-object v1

    .line 111
    .local v1, "currentLine":Ljava/lang/String;
    const-string v7, " "

    invoke-virtual {v1, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v6

    .line 113
    .local v6, "lineTokens":[Ljava/lang/String;
    array-length v7, v6

    const/4 v10, 0x3

    if-ne v7, v10, :cond_0

    .line 115
    new-instance v0, Ljava/io/File;

    new-instance v7, Ljava/lang/StringBuilder;

    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v10

    invoke-direct {v7, v10}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v10, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v7, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    const/4 v10, 0x1

    aget-object v10, v6, v10

    invoke-virtual {v7, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-direct {v0, v7}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 117
    .local v0, "checkContainerFile":Ljava/io/File;
    invoke-virtual {v0}, Ljava/io/File;->exists()Z

    move-result v7

    if-eqz v7, :cond_0

    .line 121
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v10

    const/4 v7, 0x2

    aget-object v7, v6, v7

    invoke-static {v7}, Ljava/lang/Long;->valueOf(Ljava/lang/String;)Ljava/lang/Long;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/Long;->longValue()J

    move-result-wide v12

    sub-long v2, v10, v12

    .line 124
    .local v2, "currentLivedTime":J
    mul-int/lit8 v7, p2, 0x18

    mul-int/lit8 v7, v7, 0x3c

    mul-int/lit16 v7, v7, 0x3e8

    int-to-long v8, v7

    .line 126
    .local v8, "maximumTimeToLive":J
    cmp-long v7, v2, v8

    if-gez v7, :cond_3

    .line 129
    iget-object v7, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    const/4 v10, 0x0

    aget-object v10, v6, v10

    const/4 v11, 0x1

    aget-object v11, v6, v11

    invoke-interface {v7, v10, v11}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 130
    iget-object v7, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToCreationTimestamp:Ljava/util/Map;

    const/4 v10, 0x0

    aget-object v10, v6, v10

    const/4 v11, 0x2

    aget-object v11, v6, v11

    invoke-static {v11}, Ljava/lang/Long;->valueOf(Ljava/lang/String;)Ljava/lang/Long;

    move-result-object v11

    invoke-interface {v7, v10, v11}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_1
    .catch Ljava/io/FileNotFoundException; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    goto :goto_0

    .line 142
    .end local v0    # "checkContainerFile":Ljava/io/File;
    .end local v1    # "currentLine":Ljava/lang/String;
    .end local v2    # "currentLivedTime":J
    .end local v6    # "lineTokens":[Ljava/lang/String;
    .end local v8    # "maximumTimeToLive":J
    :catch_0
    move-exception v4

    .line 143
    .local v4, "e":Ljava/io/FileNotFoundException;
    :try_start_2
    sget-object v7, Lit/necst/grabnrun/CacheLogger;->TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

    const-string v10, "Issue while opening helper file!"

    invoke-static {v7, v10}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 146
    if-eqz v5, :cond_1

    .line 147
    invoke-virtual {v5}, Ljava/util/Scanner;->close()V

    goto :goto_1

    .line 135
    .end local v4    # "e":Ljava/io/FileNotFoundException;
    .restart local v0    # "checkContainerFile":Ljava/io/File;
    .restart local v1    # "currentLine":Ljava/lang/String;
    .restart local v2    # "currentLivedTime":J
    .restart local v6    # "lineTokens":[Ljava/lang/String;
    .restart local v8    # "maximumTimeToLive":J
    :cond_3
    :try_start_3
    invoke-virtual {v0}, Ljava/io/File;->delete()Z

    move-result v7

    if-eqz v7, :cond_0

    .line 136
    sget-object v7, Lit/necst/grabnrun/CacheLogger;->TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

    new-instance v10, Ljava/lang/StringBuilder;

    const-string v11, "Issue while erasing "

    invoke-direct {v10, v11}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-static {v7, v10}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_3
    .catch Ljava/io/FileNotFoundException; {:try_start_3 .. :try_end_3} :catch_0
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    goto/16 :goto_0

    .line 144
    .end local v0    # "checkContainerFile":Ljava/io/File;
    .end local v1    # "currentLine":Ljava/lang/String;
    .end local v2    # "currentLivedTime":J
    .end local v6    # "lineTokens":[Ljava/lang/String;
    .end local v8    # "maximumTimeToLive":J
    :catchall_0
    move-exception v7

    .line 146
    if-eqz v5, :cond_4

    .line 147
    invoke-virtual {v5}, Ljava/util/Scanner;->close()V

    .line 149
    :cond_4
    throw v7
.end method


# virtual methods
.method final addCachedEntryToLog(Ljava/lang/String;Ljava/lang/String;)V
    .locals 4
    .param p1, "remoteURL"    # Ljava/lang/String;
    .param p2, "localFileName"    # Ljava/lang/String;

    .prologue
    .line 190
    iget-boolean v0, p0, Lit/necst/grabnrun/CacheLogger;->hasBeenAlreadyFinalized:Z

    if-eqz v0, :cond_0

    .line 195
    :goto_0
    return-void

    .line 193
    :cond_0
    iget-object v0, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToCreationTimestamp:Ljava/util/Map;

    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v2

    invoke-static {v2, v3}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v1

    invoke-interface {v0, p1, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 194
    iget-object v0, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v0, p1, p2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    goto :goto_0
.end method

.method final checkForCachedEntry(Ljava/lang/String;)Ljava/lang/String;
    .locals 4
    .param p1, "remoteURL"    # Ljava/lang/String;

    .prologue
    const/4 v1, 0x0

    .line 165
    iget-boolean v0, p0, Lit/necst/grabnrun/CacheLogger;->hasBeenAlreadyFinalized:Z

    if-eqz v0, :cond_0

    move-object v0, v1

    .line 174
    :goto_0
    return-object v0

    .line 169
    :cond_0
    iget-object v0, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v0, p1}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 170
    new-instance v2, Ljava/io/File;

    new-instance v0, Ljava/lang/StringBuilder;

    iget-object v3, p0, Lit/necst/grabnrun/CacheLogger;->cacheDirectoryPath:Ljava/lang/String;

    invoke-static {v3}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v3

    invoke-direct {v0, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v3, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    iget-object v0, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v0, p1}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/String;

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-direct {v2, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual {v2}, Ljava/io/File;->exists()Z

    move-result v0

    if-eqz v0, :cond_1

    .line 171
    iget-object v0, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v0, p1}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/String;

    goto :goto_0

    :cond_1
    move-object v0, v1

    .line 174
    goto :goto_0
.end method

.method final finalizeLog()V
    .locals 7

    .prologue
    .line 209
    iget-boolean v5, p0, Lit/necst/grabnrun/CacheLogger;->hasBeenAlreadyFinalized:Z

    if-eqz v5, :cond_1

    .line 255
    :cond_0
    :goto_0
    return-void

    .line 210
    :cond_1
    const/4 v5, 0x1

    iput-boolean v5, p0, Lit/necst/grabnrun/CacheLogger;->hasBeenAlreadyFinalized:Z

    .line 213
    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->helperFile:Ljava/io/File;

    invoke-virtual {v5}, Ljava/io/File;->exists()Z

    move-result v5

    if-eqz v5, :cond_2

    .line 214
    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->helperFile:Ljava/io/File;

    invoke-virtual {v5}, Ljava/io/File;->delete()Z

    move-result v5

    if-nez v5, :cond_2

    .line 215
    sget-object v5, Lit/necst/grabnrun/CacheLogger;->TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

    const-string v6, "Problem while erasing old copy of helper file!"

    invoke-static {v5, v6}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 217
    :cond_2
    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v5}, Ljava/util/Map;->isEmpty()Z

    move-result v5

    if-nez v5, :cond_0

    .line 222
    const/4 v2, 0x0

    .line 226
    .local v2, "mPrintWriter":Ljava/io/PrintWriter;
    :try_start_0
    new-instance v3, Ljava/io/PrintWriter;

    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->helperFile:Ljava/io/File;

    invoke-direct {v3, v5}, Ljava/io/PrintWriter;-><init>(Ljava/io/File;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_1
    .catchall {:try_start_0 .. :try_end_0} :catchall_1

    .line 228
    .end local v2    # "mPrintWriter":Ljava/io/PrintWriter;
    .local v3, "mPrintWriter":Ljava/io/PrintWriter;
    :try_start_1
    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v5}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v5

    invoke-interface {v5}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 230
    .local v4, "remoteURLIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_3
    :goto_1
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v5

    if-nez v5, :cond_4

    .line 242
    invoke-virtual {v3}, Ljava/io/PrintWriter;->checkError()Z

    move-result v5

    if-eqz v5, :cond_6

    .line 243
    new-instance v5, Ljava/io/IOException;

    invoke-direct {v5}, Ljava/io/IOException;-><init>()V

    throw v5
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 247
    .end local v4    # "remoteURLIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catch_0
    move-exception v1

    move-object v2, v3

    .line 248
    .end local v3    # "mPrintWriter":Ljava/io/PrintWriter;
    .local v1, "e":Ljava/io/IOException;
    .restart local v2    # "mPrintWriter":Ljava/io/PrintWriter;
    :goto_2
    :try_start_2
    sget-object v5, Lit/necst/grabnrun/CacheLogger;->TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

    const-string v6, "Problem while updating helper file!"

    invoke-static {v5, v6}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    .line 251
    if-eqz v2, :cond_0

    .line 252
    invoke-virtual {v2}, Ljava/io/PrintWriter;->close()V

    goto :goto_0

    .line 232
    .end local v1    # "e":Ljava/io/IOException;
    .end local v2    # "mPrintWriter":Ljava/io/PrintWriter;
    .restart local v3    # "mPrintWriter":Ljava/io/PrintWriter;
    .restart local v4    # "remoteURLIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_4
    :try_start_3
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/String;

    .line 234
    .local v0, "currentRemoteURL":Ljava/lang/String;
    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToCreationTimestamp:Ljava/util/Map;

    invoke-interface {v5, v0}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v5

    if-eqz v5, :cond_3

    .line 238
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-static {v0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v6

    invoke-direct {v5, v6}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const-string v6, " "

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    iget-object v5, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToLocalFileMap:Ljava/util/Map;

    invoke-interface {v5, v0}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/String;

    invoke-virtual {v6, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    const-string v6, " "

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    iget-object v6, p0, Lit/necst/grabnrun/CacheLogger;->remoteURLToCreationTimestamp:Ljava/util/Map;

    invoke-interface {v6, v0}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v5

    const-string v6, ";"

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v3, v5}, Ljava/io/PrintWriter;->println(Ljava/lang/String;)V
    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_0
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    goto :goto_1

    .line 249
    .end local v0    # "currentRemoteURL":Ljava/lang/String;
    .end local v4    # "remoteURLIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catchall_0
    move-exception v5

    move-object v2, v3

    .line 251
    .end local v3    # "mPrintWriter":Ljava/io/PrintWriter;
    .restart local v2    # "mPrintWriter":Ljava/io/PrintWriter;
    :goto_3
    if-eqz v2, :cond_5

    .line 252
    invoke-virtual {v2}, Ljava/io/PrintWriter;->close()V

    .line 253
    :cond_5
    throw v5

    .line 245
    .end local v2    # "mPrintWriter":Ljava/io/PrintWriter;
    .restart local v3    # "mPrintWriter":Ljava/io/PrintWriter;
    .restart local v4    # "remoteURLIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_6
    :try_start_4
    sget-object v5, Lit/necst/grabnrun/CacheLogger;->TAG_FILE_CACHE_LOGGER:Ljava/lang/String;

    const-string v6, "Helper file was correctly stored on the device."

    invoke-static {v5, v6}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_0
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    .line 251
    if-eqz v3, :cond_0

    .line 252
    invoke-virtual {v3}, Ljava/io/PrintWriter;->close()V

    goto/16 :goto_0

    .line 249
    .end local v3    # "mPrintWriter":Ljava/io/PrintWriter;
    .end local v4    # "remoteURLIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v2    # "mPrintWriter":Ljava/io/PrintWriter;
    :catchall_1
    move-exception v5

    goto :goto_3

    .line 247
    :catch_1
    move-exception v1

    goto :goto_2
.end method
