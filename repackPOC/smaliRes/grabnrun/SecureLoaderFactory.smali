.class public Lit/necst/grabnrun/SecureLoaderFactory;
.super Ljava/lang/Object;
.source "SecureLoaderFactory.java"


# static fields
.field static final CONT_IMPORT_DIR:Ljava/lang/String; = "imported_cont"

.field public static final DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRACY:I = 0x5

.field private static final TAG_SECURE_FACTORY:Ljava/lang/String;


# instance fields
.field private daysBeforeContainerCacheExpiration:I

.field private mContextWrapper:Landroid/content/ContextWrapper;

.field private mFileDownloader:Lit/necst/grabnrun/FileDownloader;

.field private messageDigest:Ljava/security/MessageDigest;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .prologue
    .line 49
    const-class v0, Lit/necst/grabnrun/SecureLoaderFactory;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    .line 78
    return-void
.end method

.method public constructor <init>(Landroid/content/ContextWrapper;)V
    .locals 1
    .param p1, "parentContextWrapper"    # Landroid/content/ContextWrapper;

    .prologue
    .line 95
    const/4 v0, 0x5

    invoke-direct {p0, p1, v0}, Lit/necst/grabnrun/SecureLoaderFactory;-><init>(Landroid/content/ContextWrapper;I)V

    .line 96
    return-void
.end method

.method public constructor <init>(Landroid/content/ContextWrapper;I)V
    .locals 3
    .param p1, "parentContextWrapper"    # Landroid/content/ContextWrapper;
    .param p2, "daysBeforeContainerCacheExpiration"    # I

    .prologue
    .line 112
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 114
    if-ltz p2, :cond_0

    .line 117
    iput p2, p0, Lit/necst/grabnrun/SecureLoaderFactory;->daysBeforeContainerCacheExpiration:I

    .line 125
    :goto_0
    iput-object p1, p0, Lit/necst/grabnrun/SecureLoaderFactory;->mContextWrapper:Landroid/content/ContextWrapper;

    .line 127
    new-instance v1, Lit/necst/grabnrun/FileDownloader;

    iget-object v2, p0, Lit/necst/grabnrun/SecureLoaderFactory;->mContextWrapper:Landroid/content/ContextWrapper;

    invoke-direct {v1, v2}, Lit/necst/grabnrun/FileDownloader;-><init>(Landroid/content/ContextWrapper;)V

    iput-object v1, p0, Lit/necst/grabnrun/SecureLoaderFactory;->mFileDownloader:Lit/necst/grabnrun/FileDownloader;

    .line 130
    :try_start_0
    const-string v1, "SHA-1"

    invoke-static {v1}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

    move-result-object v1

    iput-object v1, p0, Lit/necst/grabnrun/SecureLoaderFactory;->messageDigest:Ljava/security/MessageDigest;
    :try_end_0
    .catch Ljava/security/NoSuchAlgorithmException; {:try_start_0 .. :try_end_0} :catch_0

    .line 135
    :goto_1
    return-void

    .line 122
    :cond_0
    const/4 v1, 0x5

    iput v1, p0, Lit/necst/grabnrun/SecureLoaderFactory;->daysBeforeContainerCacheExpiration:I

    goto :goto_0

    .line 131
    :catch_0
    move-exception v0

    .line 132
    .local v0, "e":Ljava/security/NoSuchAlgorithmException;
    sget-object v1, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v2, "Wrong algorithm choice for message digest!"

    invoke-static {v1, v2}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 133
    invoke-virtual {v0}, Ljava/security/NoSuchAlgorithmException;->printStackTrace()V

    goto :goto_1
.end method

.method private computeDigestFromFilePath(Ljava/lang/String;)Ljava/lang/String;
    .locals 10
    .param p1, "filePath"    # Ljava/lang/String;

    .prologue
    .line 492
    const/4 v4, 0x0

    .line 493
    .local v4, "inStream":Ljava/io/FileInputStream;
    const/4 v2, 0x0

    .line 497
    .local v2, "digestString":Ljava/lang/String;
    :try_start_0
    new-instance v5, Ljava/io/FileInputStream;

    invoke-direct {v5, p1}, Ljava/io/FileInputStream;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_7
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_2
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 499
    .end local v4    # "inStream":Ljava/io/FileInputStream;
    .local v5, "inStream":Ljava/io/FileInputStream;
    const/16 v7, 0x2000

    :try_start_1
    new-array v0, v7, [B

    .line 501
    .local v0, "buffer":[B
    :goto_0
    invoke-virtual {v5, v0}, Ljava/io/FileInputStream;->read([B)I

    move-result v6

    .local v6, "length":I
    const/4 v7, -0x1

    if-ne v6, v7, :cond_1

    .line 506
    iget-object v7, p0, Lit/necst/grabnrun/SecureLoaderFactory;->messageDigest:Ljava/security/MessageDigest;

    invoke-virtual {v7}, Ljava/security/MessageDigest;->digest()[B

    move-result-object v1

    .line 510
    .local v1, "digestBytes":[B
    const/16 v7, 0x8

    invoke-static {v1, v7}, Landroid/util/Base64;->encodeToString([BI)Ljava/lang/String;

    move-result-object v2

    .line 511
    const-string v7, "line.separator"

    invoke-static {v7}, Ljava/lang/System;->getProperty(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v7

    const-string v8, ""

    invoke-virtual {v2, v7, v8}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;

    move-result-object v7

    const-string v8, "\r"

    const-string v9, ""

    invoke-virtual {v7, v8, v9}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    :try_end_1
    .catch Ljava/io/FileNotFoundException; {:try_start_1 .. :try_end_1} :catch_0
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_6
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    move-result-object v2

    .line 518
    if-eqz v5, :cond_3

    .line 520
    :try_start_2
    invoke-virtual {v5}, Ljava/io/FileInputStream;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_5

    move-object v4, v5

    .line 528
    .end local v0    # "buffer":[B
    .end local v1    # "digestBytes":[B
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .end local v6    # "length":I
    .restart local v4    # "inStream":Ljava/io/FileInputStream;
    :cond_0
    :goto_1
    return-object v2

    .line 503
    .end local v4    # "inStream":Ljava/io/FileInputStream;
    .restart local v0    # "buffer":[B
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v6    # "length":I
    :cond_1
    :try_start_3
    iget-object v7, p0, Lit/necst/grabnrun/SecureLoaderFactory;->messageDigest:Ljava/security/MessageDigest;

    const/4 v8, 0x0

    invoke-virtual {v7, v0, v8, v6}, Ljava/security/MessageDigest;->update([BII)V
    :try_end_3
    .catch Ljava/io/FileNotFoundException; {:try_start_3 .. :try_end_3} :catch_0
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_6
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    goto :goto_0

    .line 513
    .end local v0    # "buffer":[B
    .end local v6    # "length":I
    :catch_0
    move-exception v3

    move-object v4, v5

    .line 514
    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .local v3, "e":Ljava/io/FileNotFoundException;
    .restart local v4    # "inStream":Ljava/io/FileInputStream;
    :goto_2
    :try_start_4
    sget-object v7, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v8, Ljava/lang/StringBuilder;

    const-string v9, "No file found at "

    invoke-direct {v8, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v8, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    invoke-static {v7, v8}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    .line 518
    if-eqz v4, :cond_0

    .line 520
    :try_start_5
    invoke-virtual {v4}, Ljava/io/FileInputStream;->close()V
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_1

    goto :goto_1

    .line 521
    :catch_1
    move-exception v3

    .line 522
    .local v3, "e":Ljava/io/IOException;
    sget-object v7, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v8, "Issue while closing file stream in message digest computation!"

    invoke-static {v7, v8}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_1

    .line 515
    .end local v3    # "e":Ljava/io/IOException;
    :catch_2
    move-exception v3

    .line 516
    .restart local v3    # "e":Ljava/io/IOException;
    :goto_3
    :try_start_6
    sget-object v7, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v8, "Something went wrong while calculating the digest!"

    invoke-static {v7, v8}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_6
    .catchall {:try_start_6 .. :try_end_6} :catchall_0

    .line 518
    if-eqz v4, :cond_0

    .line 520
    :try_start_7
    invoke-virtual {v4}, Ljava/io/FileInputStream;->close()V
    :try_end_7
    .catch Ljava/io/IOException; {:try_start_7 .. :try_end_7} :catch_3

    goto :goto_1

    .line 521
    :catch_3
    move-exception v3

    .line 522
    sget-object v7, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v8, "Issue while closing file stream in message digest computation!"

    invoke-static {v7, v8}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_1

    .line 517
    .end local v3    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v7

    .line 518
    :goto_4
    if-eqz v4, :cond_2

    .line 520
    :try_start_8
    invoke-virtual {v4}, Ljava/io/FileInputStream;->close()V
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_4

    .line 525
    :cond_2
    :goto_5
    throw v7

    .line 521
    :catch_4
    move-exception v3

    .line 522
    .restart local v3    # "e":Ljava/io/IOException;
    sget-object v8, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v9, "Issue while closing file stream in message digest computation!"

    invoke-static {v8, v9}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_5

    .line 521
    .end local v3    # "e":Ljava/io/IOException;
    .end local v4    # "inStream":Ljava/io/FileInputStream;
    .restart local v0    # "buffer":[B
    .restart local v1    # "digestBytes":[B
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v6    # "length":I
    :catch_5
    move-exception v3

    .line 522
    .restart local v3    # "e":Ljava/io/IOException;
    sget-object v7, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v8, "Issue while closing file stream in message digest computation!"

    invoke-static {v7, v8}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .end local v3    # "e":Ljava/io/IOException;
    :cond_3
    move-object v4, v5

    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v4    # "inStream":Ljava/io/FileInputStream;
    goto :goto_1

    .line 517
    .end local v0    # "buffer":[B
    .end local v1    # "digestBytes":[B
    .end local v4    # "inStream":Ljava/io/FileInputStream;
    .end local v6    # "length":I
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    :catchall_1
    move-exception v7

    move-object v4, v5

    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v4    # "inStream":Ljava/io/FileInputStream;
    goto :goto_4

    .line 515
    .end local v4    # "inStream":Ljava/io/FileInputStream;
    .restart local v5    # "inStream":Ljava/io/FileInputStream;
    :catch_6
    move-exception v3

    move-object v4, v5

    .end local v5    # "inStream":Ljava/io/FileInputStream;
    .restart local v4    # "inStream":Ljava/io/FileInputStream;
    goto :goto_3

    .line 513
    :catch_7
    move-exception v3

    goto :goto_2
.end method

.method private downloadContainerIntoFolder(Ljava/lang/String;Ljava/io/File;)Ljava/lang/String;
    .locals 16
    .param p1, "urlPath"    # Ljava/lang/String;
    .param p2, "resOutputDir"    # Ljava/io/File;

    .prologue
    .line 612
    if-nez p1, :cond_1

    const/4 v11, 0x0

    .line 723
    :cond_0
    :goto_0
    return-object v11

    .line 615
    :cond_1
    if-eqz p2, :cond_2

    invoke-virtual/range {p2 .. p2}, Ljava/io/File;->exists()Z

    move-result v13

    if-nez v13, :cond_3

    :cond_2
    const/4 v11, 0x0

    goto :goto_0

    .line 616
    :cond_3
    invoke-virtual/range {p2 .. p2}, Ljava/io/File;->isDirectory()Z

    move-result v13

    if-eqz v13, :cond_4

    invoke-virtual/range {p2 .. p2}, Ljava/io/File;->canRead()Z

    move-result v13

    if-eqz v13, :cond_4

    invoke-virtual/range {p2 .. p2}, Ljava/io/File;->canWrite()Z

    move-result v13

    if-nez v13, :cond_5

    :cond_4
    const/4 v11, 0x0

    goto :goto_0

    .line 620
    :cond_5
    :try_start_0
    new-instance v12, Ljava/net/URL;

    move-object/from16 v0, p1

    invoke-direct {v12, v0}, Ljava/net/URL;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/net/MalformedURLException; {:try_start_0 .. :try_end_0} :catch_0

    .line 626
    .local v12, "url":Ljava/net/URL;
    invoke-virtual {v12}, Ljava/net/URL;->getProtocol()Ljava/lang/String;

    move-result-object v13

    const-string v14, "http"

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_6

    invoke-virtual {v12}, Ljava/net/URL;->getProtocol()Ljava/lang/String;

    move-result-object v13

    const-string v14, "https"

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_6

    const/4 v11, 0x0

    goto :goto_0

    .line 621
    .end local v12    # "url":Ljava/net/URL;
    :catch_0
    move-exception v4

    .line 622
    .local v4, "e":Ljava/net/MalformedURLException;
    invoke-virtual {v4}, Ljava/net/MalformedURLException;->printStackTrace()V

    .line 623
    const/4 v11, 0x0

    goto :goto_0

    .line 629
    .end local v4    # "e":Ljava/net/MalformedURLException;
    .restart local v12    # "url":Ljava/net/URL;
    :cond_6
    invoke-virtual {v12}, Ljava/net/URL;->getPath()Ljava/lang/String;

    move-result-object v13

    const-string v14, "/"

    invoke-virtual {v13, v14}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v9

    .line 630
    .local v9, "finalSeparatorIndex":I
    invoke-virtual {v12}, Ljava/net/URL;->getFile()Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v13, v9}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v2

    .line 632
    .local v2, "containerName":Ljava/lang/String;
    if-eqz v2, :cond_7

    invoke-virtual {v2}, Ljava/lang/String;->isEmpty()Z

    move-result v13

    if-eqz v13, :cond_8

    :cond_7
    const/4 v11, 0x0

    goto :goto_0

    .line 635
    :cond_8
    const-string v13, "."

    invoke-virtual {v2, v13}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v6

    .line 636
    .local v6, "extensionIndex":I
    const/4 v5, 0x0

    .line 638
    .local v5, "extension":Ljava/lang/String;
    const/4 v13, -0x1

    if-eq v6, v13, :cond_9

    .line 640
    invoke-virtual {v2, v6}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v5

    .line 641
    const-string v13, ".jar"

    invoke-virtual {v5, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_9

    const-string v13, ".apk"

    invoke-virtual {v5, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_9

    const/4 v11, 0x0

    goto/16 :goto_0

    .line 664
    :cond_9
    move-object v7, v2

    .line 667
    .local v7, "finalContainerName":Ljava/lang/String;
    new-instance v1, Ljava/io/File;

    new-instance v13, Ljava/lang/StringBuilder;

    invoke-virtual/range {p2 .. p2}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v14

    invoke-static {v14}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v14

    invoke-direct {v13, v14}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v13, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v13

    invoke-direct {v1, v13}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 670
    .local v1, "checkFile":Ljava/io/File;
    invoke-virtual {v1}, Ljava/io/File;->exists()Z

    move-result v13

    if-eqz v13, :cond_a

    .line 671
    invoke-virtual {v1}, Ljava/io/File;->delete()Z

    .line 675
    :cond_a
    new-instance v13, Ljava/lang/StringBuilder;

    invoke-virtual/range {p2 .. p2}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v14

    invoke-static {v14}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v14

    invoke-direct {v13, v14}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v13, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    .line 678
    .local v11, "localContainerPath":Ljava/lang/String;
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/SecureLoaderFactory;->mFileDownloader:Lit/necst/grabnrun/FileDownloader;

    const/4 v14, 0x1

    invoke-virtual {v13, v12, v11, v14}, Lit/necst/grabnrun/FileDownloader;->downloadRemoteUrl(Ljava/net/URL;Ljava/lang/String;Z)Z

    move-result v10

    .line 680
    .local v10, "isDownloadSuccessful":Z
    if-eqz v10, :cond_f

    .line 685
    if-nez v5, :cond_0

    .line 688
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/SecureLoaderFactory;->mFileDownloader:Lit/necst/grabnrun/FileDownloader;

    invoke-virtual {v13}, Lit/necst/grabnrun/FileDownloader;->getDownloadedFileExtension()Ljava/lang/String;

    move-result-object v5

    .line 691
    if-eqz v5, :cond_f

    const-string v13, ".jar"

    invoke-virtual {v5, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_b

    const-string v13, ".apk"

    invoke-virtual {v5, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_f

    .line 694
    :cond_b
    new-instance v3, Ljava/io/File;

    invoke-direct {v3, v11}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 695
    .local v3, "containerToRename":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    new-instance v13, Ljava/lang/StringBuilder;

    invoke-static {v11}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v14

    invoke-direct {v13, v14}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v13, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v13

    invoke-direct {v8, v13}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 697
    .local v8, "finalContainerWithExtension":Ljava/io/File;
    invoke-virtual {v8}, Ljava/io/File;->exists()Z

    move-result v13

    if-eqz v13, :cond_c

    .line 698
    invoke-virtual {v8}, Ljava/io/File;->delete()Z

    move-result v13

    if-nez v13, :cond_c

    .line 699
    sget-object v13, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "Issue while deleting "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v14, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 701
    :cond_c
    invoke-virtual {v3, v8}, Ljava/io/File;->renameTo(Ljava/io/File;)Z

    move-result v13

    if-nez v13, :cond_e

    .line 705
    invoke-virtual {v3}, Ljava/io/File;->delete()Z

    move-result v13

    if-nez v13, :cond_d

    .line 706
    sget-object v13, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "Issue while deleting "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v14, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 708
    :cond_d
    const/4 v11, 0x0

    goto/16 :goto_0

    .line 712
    :cond_e
    new-instance v13, Ljava/lang/StringBuilder;

    invoke-static {v11}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v14

    invoke-direct {v13, v14}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v13, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    goto/16 :goto_0

    .line 723
    .end local v3    # "containerToRename":Ljava/io/File;
    .end local v8    # "finalContainerWithExtension":Ljava/io/File;
    :cond_f
    const/4 v11, 0x0

    goto/16 :goto_0
.end method

.method private sanitizePackageNameToCertificateMap(Ljava/util/Map;)Ljava/util/Map;
    .locals 14
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;)",
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;"
        }
    .end annotation

    .prologue
    .line 534
    .local p1, "packageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    if-eqz p1, :cond_0

    invoke-interface {p1}, Ljava/util/Map;->isEmpty()Z

    move-result v9

    if-eqz v9, :cond_2

    :cond_0
    const/4 v8, 0x0

    .line 606
    :cond_1
    return-object v8

    .line 537
    :cond_2
    new-instance v8, Ljava/util/LinkedHashMap;

    invoke-direct {v8, p1}, Ljava/util/LinkedHashMap;-><init>(Ljava/util/Map;)V

    .line 540
    .local v8, "santiziedPackageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    invoke-interface {v8}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v9

    invoke-interface {v9}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v6

    .line 542
    .local v6, "packageNamesIterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :cond_3
    :goto_0
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-eqz v9, :cond_1

    .line 544
    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    .line 545
    .local v1, "currentPackageName":Ljava/lang/String;
    const-string v9, "\\."

    invoke-virtual {v1, v9}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v5

    .line 546
    .local v5, "packStrings":[Ljava/lang/String;
    const/4 v3, 0x1

    .line 547
    .local v3, "isValidPackageName":Z
    const/4 v7, 0x0

    .line 549
    .local v7, "removeThisPackageName":Z
    array-length v10, v5

    const/4 v9, 0x0

    :goto_1
    if-lt v9, v10, :cond_6

    .line 559
    array-length v9, v5

    const/4 v10, 0x2

    if-ge v9, v10, :cond_4

    .line 560
    const/4 v7, 0x1

    .line 562
    :cond_4
    if-eqz v3, :cond_9

    .line 570
    :try_start_0
    invoke-interface {v8, v1}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/net/URL;

    .line 573
    .local v0, "certificateURL":Ljava/net/URL;
    if-eqz v0, :cond_5

    .line 575
    invoke-virtual {v0}, Ljava/net/URL;->getProtocol()Ljava/lang/String;

    move-result-object v9

    const-string v10, "http"

    invoke-virtual {v9, v10}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_8

    .line 578
    new-instance v9, Ljava/net/URL;

    const-string v10, "https"

    invoke-virtual {v0}, Ljava/net/URL;->getHost()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v0}, Ljava/net/URL;->getPort()I

    move-result v12

    invoke-virtual {v0}, Ljava/net/URL;->getFile()Ljava/lang/String;

    move-result-object v13

    invoke-direct {v9, v10, v11, v12, v13}, Ljava/net/URL;-><init>(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V

    invoke-interface {v8, v1, v9}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_0
    .catch Ljava/net/MalformedURLException; {:try_start_0 .. :try_end_0} :catch_0

    .line 598
    .end local v0    # "certificateURL":Ljava/net/URL;
    :cond_5
    :goto_2
    if-eqz v7, :cond_3

    .line 601
    invoke-interface {v6}, Ljava/util/Iterator;->remove()V

    goto :goto_0

    .line 549
    :cond_6
    aget-object v4, v5, v9

    .line 552
    .local v4, "packString":Ljava/lang/String;
    invoke-virtual {v4}, Ljava/lang/String;->isEmpty()Z

    move-result v11

    if-eqz v11, :cond_7

    .line 553
    const/4 v3, 0x0

    .line 549
    :cond_7
    add-int/lit8 v9, v9, 0x1

    goto :goto_1

    .line 581
    .end local v4    # "packString":Ljava/lang/String;
    .restart local v0    # "certificateURL":Ljava/net/URL;
    :cond_8
    :try_start_1
    invoke-virtual {v0}, Ljava/net/URL;->getProtocol()Ljava/lang/String;

    move-result-object v9

    const-string v10, "https"

    invoke-virtual {v9, v10}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    :try_end_1
    .catch Ljava/net/MalformedURLException; {:try_start_1 .. :try_end_1} :catch_0

    move-result v9

    if-nez v9, :cond_5

    .line 584
    const/4 v7, 0x1

    goto :goto_2

    .line 592
    .end local v0    # "certificateURL":Ljava/net/URL;
    :catch_0
    move-exception v2

    .line 593
    .local v2, "e":Ljava/net/MalformedURLException;
    const/4 v7, 0x1

    .line 595
    goto :goto_2

    .line 596
    .end local v2    # "e":Ljava/net/MalformedURLException;
    :cond_9
    const/4 v7, 0x1

    goto :goto_2
.end method


# virtual methods
.method public createDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/util/Map;)Lit/necst/grabnrun/SecureDexClassLoader;
    .locals 6
    .param p1, "dexPath"    # Ljava/lang/String;
    .param p2, "libraryPath"    # Ljava/lang/String;
    .param p3, "parent"    # Ljava/lang/ClassLoader;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Ljava/lang/ClassLoader;",
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;)",
            "Lit/necst/grabnrun/SecureDexClassLoader;"
        }
    .end annotation

    .prologue
    .line 189
    .local p4, "packageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    const/4 v5, 0x0

    move-object v0, p0

    move-object v1, p1

    move-object v2, p2

    move-object v3, p3

    move-object v4, p4

    invoke-virtual/range {v0 .. v5}, Lit/necst/grabnrun/SecureLoaderFactory;->createDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/util/Map;Z)Lit/necst/grabnrun/SecureDexClassLoader;

    move-result-object v0

    return-object v0
.end method

.method public createDexClassLoader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Ljava/util/Map;Z)Lit/necst/grabnrun/SecureDexClassLoader;
    .locals 37
    .param p1, "dexPath"    # Ljava/lang/String;
    .param p2, "libraryPath"    # Ljava/lang/String;
    .param p3, "parent"    # Ljava/lang/ClassLoader;
    .param p5, "performLazyEvaluation"    # Z
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Ljava/lang/ClassLoader;",
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/net/URL;",
            ">;Z)",
            "Lit/necst/grabnrun/SecureDexClassLoader;"
        }
    .end annotation

    .prologue
    .line 249
    .local p4, "packageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    new-instance v23, Ljava/lang/StringBuilder;

    invoke-direct/range {v23 .. v23}, Ljava/lang/StringBuilder;-><init>()V

    .line 265
    .local v23, "finalDexPath":Ljava/lang/StringBuilder;
    const-string v4, "http://"

    const-string v5, "http//"

    move-object/from16 v0, p1

    invoke-virtual {v0, v4, v5}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v36

    .line 266
    .local v36, "tempPath":Ljava/lang/String;
    const-string v4, "https://"

    const-string v5, "https//"

    move-object/from16 v0, v36

    invoke-virtual {v0, v4, v5}, Ljava/lang/String;->replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v36

    .line 271
    sget-object v4, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-static {v4}, Ljava/util/regex/Pattern;->quote(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v4

    move-object/from16 v0, v36

    invoke-virtual {v0, v4}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v35

    .line 275
    .local v35, "strings":[Ljava/lang/String;
    move-object/from16 v0, p0

    iget-object v4, v0, Lit/necst/grabnrun/SecureLoaderFactory;->mContextWrapper:Landroid/content/ContextWrapper;

    const-string v5, "imported_cont"

    const/4 v6, 0x0

    invoke-virtual {v4, v5, v6}, Landroid/content/ContextWrapper;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v25

    .line 276
    .local v25, "importedContainerDir":Ljava/io/File;
    sget-object v4, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v5, Ljava/lang/StringBuilder;

    const-string v6, "Download Resource Dir has been mounted at: "

    invoke-direct {v5, v6}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual/range {v25 .. v25}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-static {v4, v5}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 278
    new-instance v29, Lit/necst/grabnrun/CacheLogger;

    invoke-virtual/range {v25 .. v25}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v4

    move-object/from16 v0, p0

    iget v5, v0, Lit/necst/grabnrun/SecureLoaderFactory;->daysBeforeContainerCacheExpiration:I

    move-object/from16 v0, v29

    invoke-direct {v0, v4, v5}, Lit/necst/grabnrun/CacheLogger;-><init>(Ljava/lang/String;I)V

    .line 280
    .local v29, "mCacheLogger":Lit/necst/grabnrun/CacheLogger;
    move-object/from16 v0, v35

    array-length v5, v0

    const/4 v4, 0x0

    :goto_0
    if-lt v4, v5, :cond_2

    .line 452
    sget-object v4, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    move-object/from16 v0, v23

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->lastIndexOf(Ljava/lang/String;)I

    move-result v4

    const/4 v5, -0x1

    if-eq v4, v5, :cond_0

    .line 453
    sget-object v4, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    move-object/from16 v0, v23

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->lastIndexOf(Ljava/lang/String;)I

    move-result v4

    move-object/from16 v0, v23

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->deleteCharAt(I)Ljava/lang/StringBuilder;

    .line 456
    :cond_0
    invoke-virtual/range {v29 .. v29}, Lit/necst/grabnrun/CacheLogger;->finalizeLog()V

    .line 463
    move-object/from16 v0, p0

    iget-object v4, v0, Lit/necst/grabnrun/SecureLoaderFactory;->mContextWrapper:Landroid/content/ContextWrapper;

    const-string v5, "dex_classes"

    const/4 v6, 0x0

    invoke-virtual {v4, v5, v6}, Landroid/content/ContextWrapper;->getDir(Ljava/lang/String;I)Ljava/io/File;

    move-result-object v14

    .line 465
    .local v14, "dexOutputDir":Ljava/io/File;
    sget-object v4, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v5, Ljava/lang/StringBuilder;

    const-string v6, "Dex Output Dir has been mounted at: "

    invoke-direct {v5, v6}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v14}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-static {v4, v5}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 473
    move-object/from16 v0, p0

    move-object/from16 v1, p4

    invoke-direct {v0, v1}, Lit/necst/grabnrun/SecureLoaderFactory;->sanitizePackageNameToCertificateMap(Ljava/util/Map;)Ljava/util/Map;

    move-result-object v34

    .line 476
    .local v34, "santiziedPackageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    new-instance v3, Lit/necst/grabnrun/SecureDexClassLoader;

    invoke-virtual/range {v23 .. v23}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    .line 477
    invoke-virtual {v14}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v5

    .line 480
    move-object/from16 v0, p0

    iget-object v8, v0, Lit/necst/grabnrun/SecureLoaderFactory;->mContextWrapper:Landroid/content/ContextWrapper;

    move-object/from16 v6, p2

    move-object/from16 v7, p3

    move/from16 v9, p5

    .line 476
    invoke-direct/range {v3 .. v9}, Lit/necst/grabnrun/SecureDexClassLoader;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Landroid/content/ContextWrapper;Z)V

    .line 484
    .local v3, "mSecureDexClassLoader":Lit/necst/grabnrun/SecureDexClassLoader;
    if-eqz v3, :cond_1

    move-object/from16 v0, v34

    invoke-virtual {v3, v0}, Lit/necst/grabnrun/SecureDexClassLoader;->setCertificateLocationMap(Ljava/util/Map;)V

    .line 486
    :cond_1
    return-object v3

    .line 280
    .end local v3    # "mSecureDexClassLoader":Lit/necst/grabnrun/SecureDexClassLoader;
    .end local v14    # "dexOutputDir":Ljava/io/File;
    .end local v34    # "santiziedPackageNameToCertificateMap":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/String;Ljava/net/URL;>;"
    :cond_2
    aget-object v33, v35, v4

    .line 282
    .local v33, "path":Ljava/lang/String;
    const-string v6, "http//"

    move-object/from16 v0, v33

    invoke-virtual {v0, v6}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v6

    if-nez v6, :cond_3

    const-string v6, "https//"

    move-object/from16 v0, v33

    invoke-virtual {v0, v6}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v6

    if-eqz v6, :cond_a

    .line 288
    :cond_3
    const-string v6, "http//"

    move-object/from16 v0, v33

    invoke-virtual {v0, v6}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v6

    if-eqz v6, :cond_5

    .line 289
    new-instance v6, Ljava/lang/StringBuilder;

    const-string v7, "http:"

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const/4 v7, 0x4

    move-object/from16 v0, v33

    invoke-virtual {v0, v7}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v24

    .line 293
    .local v24, "fixedRemotePath":Ljava/lang/String;
    :goto_1
    move-object/from16 v0, v29

    move-object/from16 v1, v24

    invoke-virtual {v0, v1}, Lit/necst/grabnrun/CacheLogger;->checkForCachedEntry(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v11

    .line 295
    .local v11, "cachedContainerFileName":Ljava/lang/String;
    if-eqz v11, :cond_6

    .line 300
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-virtual/range {v25 .. v25}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v7

    invoke-static {v7}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v7, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    sget-object v7, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    move-object/from16 v0, v23

    invoke-virtual {v0, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 301
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v7, Ljava/lang/StringBuilder;

    const-string v8, "Dex Path has been modified into: "

    invoke-direct {v7, v8}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v23

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v6, v7}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 280
    .end local v11    # "cachedContainerFileName":Ljava/lang/String;
    .end local v24    # "fixedRemotePath":Ljava/lang/String;
    :cond_4
    :goto_2
    add-int/lit8 v4, v4, 0x1

    goto/16 :goto_0

    .line 291
    :cond_5
    new-instance v6, Ljava/lang/StringBuilder;

    const-string v7, "https:"

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    const/4 v7, 0x5

    move-object/from16 v0, v33

    invoke-virtual {v0, v7}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v24

    .restart local v24    # "fixedRemotePath":Ljava/lang/String;
    goto :goto_1

    .line 308
    .restart local v11    # "cachedContainerFileName":Ljava/lang/String;
    :cond_6
    move-object/from16 v0, p0

    move-object/from16 v1, v24

    move-object/from16 v2, v25

    invoke-direct {v0, v1, v2}, Lit/necst/grabnrun/SecureLoaderFactory;->downloadContainerIntoFolder(Ljava/lang/String;Ljava/io/File;)Ljava/lang/String;

    move-result-object v18

    .line 312
    .local v18, "downloadedContainerPath":Ljava/lang/String;
    if-eqz v18, :cond_4

    .line 316
    move-object/from16 v0, p0

    move-object/from16 v1, v18

    invoke-direct {v0, v1}, Lit/necst/grabnrun/SecureLoaderFactory;->computeDigestFromFilePath(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v13

    .line 318
    .local v13, "containerDigest":Ljava/lang/String;
    new-instance v16, Ljava/io/File;

    move-object/from16 v0, v16

    move-object/from16 v1, v18

    invoke-direct {v0, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 320
    .local v16, "downloadedContainer":Ljava/io/File;
    if-nez v13, :cond_7

    .line 324
    invoke-virtual/range {v16 .. v16}, Ljava/io/File;->delete()Z

    move-result v6

    if-nez v6, :cond_4

    .line 325
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v7, Ljava/lang/StringBuilder;

    const-string v8, "Issue while deleting "

    invoke-direct {v7, v8}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v18

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_2

    .line 329
    :cond_7
    const-string v6, "."

    move-object/from16 v0, v18

    invoke-virtual {v0, v6}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v22

    .line 330
    .local v22, "extensionIndex":I
    move-object/from16 v0, v18

    move/from16 v1, v22

    invoke-virtual {v0, v1}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v21

    .line 333
    .local v21, "extension":Ljava/lang/String;
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-virtual/range {v25 .. v25}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v7

    invoke-static {v7}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v7, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    move-object/from16 v0, v21

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v17

    .line 335
    .local v17, "downloadedContainerFinalPath":Ljava/lang/String;
    new-instance v15, Ljava/io/File;

    move-object/from16 v0, v17

    invoke-direct {v15, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 337
    .local v15, "downloadContainerFinalPosition":Ljava/io/File;
    invoke-virtual {v15}, Ljava/io/File;->exists()Z

    move-result v6

    if-eqz v6, :cond_8

    .line 338
    invoke-virtual {v15}, Ljava/io/File;->delete()Z

    move-result v6

    if-nez v6, :cond_8

    .line 339
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v7, Ljava/lang/StringBuilder;

    const-string v8, "Issue while deleting "

    invoke-direct {v7, v8}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v17

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 341
    :cond_8
    move-object/from16 v0, v16

    invoke-virtual {v0, v15}, Ljava/io/File;->renameTo(Ljava/io/File;)Z

    move-result v6

    if-eqz v6, :cond_9

    .line 345
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-static/range {v17 .. v17}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v7, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    move-object/from16 v0, v23

    invoke-virtual {v0, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 346
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v7, Ljava/lang/StringBuilder;

    const-string v8, "Dex Path has been modified into: "

    invoke-direct {v7, v8}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v23

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v6, v7}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 349
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-static {v13}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v21

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    move-object/from16 v0, v29

    move-object/from16 v1, v24

    invoke-virtual {v0, v1, v6}, Lit/necst/grabnrun/CacheLogger;->addCachedEntryToLog(Ljava/lang/String;Ljava/lang/String;)V

    goto/16 :goto_2

    .line 353
    :cond_9
    invoke-virtual/range {v16 .. v16}, Ljava/io/File;->delete()Z

    move-result v6

    if-nez v6, :cond_4

    .line 354
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    new-instance v7, Ljava/lang/StringBuilder;

    const-string v8, "Issue while deleting "

    invoke-direct {v7, v8}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, v18

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_2

    .line 379
    .end local v11    # "cachedContainerFileName":Ljava/lang/String;
    .end local v13    # "containerDigest":Ljava/lang/String;
    .end local v15    # "downloadContainerFinalPosition":Ljava/io/File;
    .end local v16    # "downloadedContainer":Ljava/io/File;
    .end local v17    # "downloadedContainerFinalPath":Ljava/lang/String;
    .end local v18    # "downloadedContainerPath":Ljava/lang/String;
    .end local v21    # "extension":Ljava/lang/String;
    .end local v22    # "extensionIndex":I
    .end local v24    # "fixedRemotePath":Ljava/lang/String;
    :cond_a
    const/16 v20, 0x0

    .line 382
    .local v20, "encodedContainerDigest":Ljava/lang/String;
    new-instance v6, Ljava/io/File;

    move-object/from16 v0, v33

    invoke-direct {v6, v0}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual {v6}, Ljava/io/File;->exists()Z

    move-result v6

    if-eqz v6, :cond_b

    move-object/from16 v0, p0

    move-object/from16 v1, v33

    invoke-direct {v0, v1}, Lit/necst/grabnrun/SecureLoaderFactory;->computeDigestFromFilePath(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v20

    .line 385
    :cond_b
    if-eqz v20, :cond_4

    .line 388
    const-string v6, "."

    move-object/from16 v0, v33

    invoke-virtual {v0, v6}, Ljava/lang/String;->lastIndexOf(Ljava/lang/String;)I

    move-result v22

    .line 389
    .restart local v22    # "extensionIndex":I
    move-object/from16 v0, v33

    move/from16 v1, v22

    invoke-virtual {v0, v1}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v21

    .line 393
    .restart local v21    # "extension":Ljava/lang/String;
    new-instance v6, Lit/necst/grabnrun/FileFilterByName;

    move-object/from16 v0, v20

    move-object/from16 v1, v21

    invoke-direct {v6, v0, v1}, Lit/necst/grabnrun/FileFilterByName;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    move-object/from16 v0, v25

    invoke-virtual {v0, v6}, Ljava/io/File;->listFiles(Ljava/io/FileFilter;)[Ljava/io/File;

    move-result-object v30

    .line 395
    .local v30, "matchingContainerArray":[Ljava/io/File;
    if-eqz v30, :cond_c

    move-object/from16 v0, v30

    array-length v6, v0

    if-lez v6, :cond_c

    .line 399
    new-instance v6, Ljava/lang/StringBuilder;

    const/4 v7, 0x0

    aget-object v7, v30, v7

    invoke-virtual {v7}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v7

    invoke-static {v7}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v7, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    move-object/from16 v0, v23

    invoke-virtual {v0, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto/16 :goto_2

    .line 406
    :cond_c
    const/16 v26, 0x0

    .line 407
    .local v26, "inStream":Ljava/io/InputStream;
    const/16 v31, 0x0

    .line 408
    .local v31, "outStream":Ljava/io/OutputStream;
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-virtual/range {v25 .. v25}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v7

    invoke-static {v7}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v7, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    move-object/from16 v0, v20

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    move-object/from16 v0, v21

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    .line 412
    .local v12, "cachedContainerPath":Ljava/lang/String;
    :try_start_0
    new-instance v27, Ljava/io/FileInputStream;

    move-object/from16 v0, v27

    move-object/from16 v1, v33

    invoke-direct {v0, v1}, Ljava/io/FileInputStream;-><init>(Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_8
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_3
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 413
    .end local v26    # "inStream":Ljava/io/InputStream;
    .local v27, "inStream":Ljava/io/InputStream;
    :try_start_1
    new-instance v32, Ljava/io/FileOutputStream;

    move-object/from16 v0, v32

    invoke-direct {v0, v12}, Ljava/io/FileOutputStream;-><init>(Ljava/lang/String;)V
    :try_end_1
    .catch Ljava/io/FileNotFoundException; {:try_start_1 .. :try_end_1} :catch_9
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_6
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 415
    .end local v31    # "outStream":Ljava/io/OutputStream;
    .local v32, "outStream":Ljava/io/OutputStream;
    const/16 v6, 0x2000

    :try_start_2
    new-array v10, v6, [B

    .line 420
    .local v10, "buf":[B
    :goto_3
    move-object/from16 v0, v27

    invoke-virtual {v0, v10}, Ljava/io/InputStream;->read([B)I

    move-result v28

    .local v28, "length":I
    if-gtz v28, :cond_e

    .line 425
    new-instance v6, Ljava/lang/StringBuilder;

    invoke-static {v12}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    sget-object v7, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    move-object/from16 v0, v23

    invoke-virtual {v0, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    :try_end_2
    .catch Ljava/io/FileNotFoundException; {:try_start_2 .. :try_end_2} :catch_1
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_7
    .catchall {:try_start_2 .. :try_end_2} :catchall_2

    .line 435
    if-eqz v27, :cond_d

    .line 436
    :try_start_3
    invoke-virtual/range {v27 .. v27}, Ljava/io/InputStream;->close()V

    .line 438
    :cond_d
    if-eqz v32, :cond_4

    .line 439
    invoke-virtual/range {v32 .. v32}, Ljava/io/OutputStream;->close()V
    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_0

    goto/16 :goto_2

    .line 442
    :catch_0
    move-exception v19

    .line 443
    .local v19, "e":Ljava/io/IOException;
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v7, "Issue in closing file streams while importing a container!"

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_2

    .line 421
    .end local v19    # "e":Ljava/io/IOException;
    :cond_e
    const/4 v6, 0x0

    :try_start_4
    move-object/from16 v0, v32

    move/from16 v1, v28

    invoke-virtual {v0, v10, v6, v1}, Ljava/io/OutputStream;->write([BII)V
    :try_end_4
    .catch Ljava/io/FileNotFoundException; {:try_start_4 .. :try_end_4} :catch_1
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_7
    .catchall {:try_start_4 .. :try_end_4} :catchall_2

    goto :goto_3

    .line 427
    .end local v10    # "buf":[B
    .end local v28    # "length":I
    :catch_1
    move-exception v19

    move-object/from16 v31, v32

    .end local v32    # "outStream":Ljava/io/OutputStream;
    .restart local v31    # "outStream":Ljava/io/OutputStream;
    move-object/from16 v26, v27

    .line 428
    .end local v27    # "inStream":Ljava/io/InputStream;
    .local v19, "e":Ljava/io/FileNotFoundException;
    .restart local v26    # "inStream":Ljava/io/InputStream;
    :goto_4
    :try_start_5
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v7, "Problem in locating container to import in the application private folder!"

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_0

    .line 435
    if-eqz v26, :cond_f

    .line 436
    :try_start_6
    invoke-virtual/range {v26 .. v26}, Ljava/io/InputStream;->close()V

    .line 438
    :cond_f
    if-eqz v31, :cond_4

    .line 439
    invoke-virtual/range {v31 .. v31}, Ljava/io/OutputStream;->close()V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_2

    goto/16 :goto_2

    .line 442
    :catch_2
    move-exception v19

    .line 443
    .local v19, "e":Ljava/io/IOException;
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v7, "Issue in closing file streams while importing a container!"

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_2

    .line 429
    .end local v19    # "e":Ljava/io/IOException;
    :catch_3
    move-exception v19

    .line 430
    .restart local v19    # "e":Ljava/io/IOException;
    :goto_5
    :try_start_7
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v7, "Problem while importing a local container into the application private folder!"

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_7
    .catchall {:try_start_7 .. :try_end_7} :catchall_0

    .line 435
    if-eqz v26, :cond_10

    .line 436
    :try_start_8
    invoke-virtual/range {v26 .. v26}, Ljava/io/InputStream;->close()V

    .line 438
    :cond_10
    if-eqz v31, :cond_4

    .line 439
    invoke-virtual/range {v31 .. v31}, Ljava/io/OutputStream;->close()V
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_4

    goto/16 :goto_2

    .line 442
    :catch_4
    move-exception v19

    .line 443
    sget-object v6, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v7, "Issue in closing file streams while importing a container!"

    invoke-static {v6, v7}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto/16 :goto_2

    .line 431
    .end local v19    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v4

    .line 435
    :goto_6
    if-eqz v26, :cond_11

    .line 436
    :try_start_9
    invoke-virtual/range {v26 .. v26}, Ljava/io/InputStream;->close()V

    .line 438
    :cond_11
    if-eqz v31, :cond_12

    .line 439
    invoke-virtual/range {v31 .. v31}, Ljava/io/OutputStream;->close()V
    :try_end_9
    .catch Ljava/io/IOException; {:try_start_9 .. :try_end_9} :catch_5

    .line 445
    :cond_12
    :goto_7
    throw v4

    .line 442
    :catch_5
    move-exception v19

    .line 443
    .restart local v19    # "e":Ljava/io/IOException;
    sget-object v5, Lit/necst/grabnrun/SecureLoaderFactory;->TAG_SECURE_FACTORY:Ljava/lang/String;

    const-string v6, "Issue in closing file streams while importing a container!"

    invoke-static {v5, v6}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_7

    .line 431
    .end local v19    # "e":Ljava/io/IOException;
    .end local v26    # "inStream":Ljava/io/InputStream;
    .restart local v27    # "inStream":Ljava/io/InputStream;
    :catchall_1
    move-exception v4

    move-object/from16 v26, v27

    .end local v27    # "inStream":Ljava/io/InputStream;
    .restart local v26    # "inStream":Ljava/io/InputStream;
    goto :goto_6

    .end local v26    # "inStream":Ljava/io/InputStream;
    .end local v31    # "outStream":Ljava/io/OutputStream;
    .restart local v27    # "inStream":Ljava/io/InputStream;
    .restart local v32    # "outStream":Ljava/io/OutputStream;
    :catchall_2
    move-exception v4

    move-object/from16 v31, v32

    .end local v32    # "outStream":Ljava/io/OutputStream;
    .restart local v31    # "outStream":Ljava/io/OutputStream;
    move-object/from16 v26, v27

    .end local v27    # "inStream":Ljava/io/InputStream;
    .restart local v26    # "inStream":Ljava/io/InputStream;
    goto :goto_6

    .line 429
    .end local v26    # "inStream":Ljava/io/InputStream;
    .restart local v27    # "inStream":Ljava/io/InputStream;
    :catch_6
    move-exception v19

    move-object/from16 v26, v27

    .end local v27    # "inStream":Ljava/io/InputStream;
    .restart local v26    # "inStream":Ljava/io/InputStream;
    goto :goto_5

    .end local v26    # "inStream":Ljava/io/InputStream;
    .end local v31    # "outStream":Ljava/io/OutputStream;
    .restart local v27    # "inStream":Ljava/io/InputStream;
    .restart local v32    # "outStream":Ljava/io/OutputStream;
    :catch_7
    move-exception v19

    move-object/from16 v31, v32

    .end local v32    # "outStream":Ljava/io/OutputStream;
    .restart local v31    # "outStream":Ljava/io/OutputStream;
    move-object/from16 v26, v27

    .end local v27    # "inStream":Ljava/io/InputStream;
    .restart local v26    # "inStream":Ljava/io/InputStream;
    goto :goto_5

    .line 427
    :catch_8
    move-exception v19

    goto :goto_4

    .end local v26    # "inStream":Ljava/io/InputStream;
    .restart local v27    # "inStream":Ljava/io/InputStream;
    :catch_9
    move-exception v19

    move-object/from16 v26, v27

    .end local v27    # "inStream":Ljava/io/InputStream;
    .restart local v26    # "inStream":Ljava/io/InputStream;
    goto :goto_4
.end method
