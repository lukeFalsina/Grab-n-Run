    sput-boolean v2, Lit/necst/grabnrun/RepackHandler;->hasStaticAssociativeMap:Z

    .line 159
    :try_start_0
    const-string v2, "SHA-1"

    invoke-static {v2}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

    move-result-object v2

    sput-object v2, Lit/necst/grabnrun/RepackHandler;->messageDigest:Ljava/security/MessageDigest;
    :try_end_0
    .catch Ljava/security/NoSuchAlgorithmException; {:try_start_0 .. :try_end_0} :catch_0

    .line 166
    :goto_0
    const/4 v2, 0x1

    sput-boolean v2, Lit/necst/grabnrun/RepackHandler;->gotUserInput:Z

    .line 168
    return-void

    .line 160
    :catch_0
    move-exception v0

    .line 162
    .local v0, "e":Ljava/security/NoSuchAlgorithmException;
    invoke-virtual {v0}, Ljava/security/NoSuchAlgorithmException;->printStackTrace()V

    goto :goto_0
.end method

.method private static insertURLEntriesInMap(Ljava/util/Map;Ljava/util/Iterator;)V
    .locals 4
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;",
            "Ljava/util/Iterator",
            "<",
            "Ljava/lang/String;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 285
    .local p0, "associativeMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    .local p1, "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_0
    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-nez v2, :cond_1

    .line 300
    return-void

    .line 287
    :cond_1
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    .line 289
    .local v1, "packageNameToInsert":Ljava/lang/String;
    sget-object v2, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    invoke-interface {v2, v1}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_0

    .line 293
    :try_start_0
    new-instance v3, Ljava/net/URL;

    sget-object v2, Lit/necst/grabnrun/RepackHandler;->packageNameToCertificateURLMap:Ljava/util/Map;

    invoke-interface {v2, v1}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/lang/String;

    invoke-direct {v3, v2}, Ljava/net/URL;-><init>(Ljava/lang/String;)V

    invoke-interface {p0, v1, v3}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_0
    .catch Ljava/net/MalformedURLException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    .line 294
    :catch_0
    move-exception v0

    .line 296
    .local v0, "e":Ljava/net/MalformedURLException;
    invoke-virtual {v0}, Ljava/net/MalformedURLException;->printStackTrace()V

    goto :goto_0
.end method

.method public static raiseSecurityException()V
    .locals 2
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/ClassNotFoundException;
        }
    .end annotation

    .prologue
    .line 358
    new-instance v0, Ljava/lang/ClassNotFoundException;

    const-string v1, "A security constraint was violated and so SecureDexClassLoader prevents target class from being loaded."

    invoke-direct {v0, v1}, Ljava/lang/ClassNotFoundException;-><init>(Ljava/lang/String;)V

    throw v0
.end method
