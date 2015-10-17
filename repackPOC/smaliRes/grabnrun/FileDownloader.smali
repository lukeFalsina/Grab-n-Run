.class final Lit/necst/grabnrun/FileDownloader;
.super Ljava/lang/Object;
.source "FileDownloader.java"


# static fields
.field private static final TAG_FILE_DOWNLOADER:Ljava/lang/String;


# instance fields
.field private activeNetworkInfo:Landroid/net/NetworkInfo;

.field private fileMimeType:Ljava/lang/String;

.field private mConnectivityManager:Landroid/net/ConnectivityManager;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .prologue
    .line 44
    const-class v0, Lit/necst/grabnrun/FileDownloader;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;

    return-void
.end method

.method constructor <init>(Landroid/content/ContextWrapper;)V
    .locals 1
    .param p1, "parentContextWrapper"    # Landroid/content/ContextWrapper;

    .prologue
    .line 59
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 61
    const-string v0, "connectivity"

    invoke-virtual {p1, v0}, Landroid/content/ContextWrapper;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/net/ConnectivityManager;

    iput-object v0, p0, Lit/necst/grabnrun/FileDownloader;->mConnectivityManager:Landroid/net/ConnectivityManager;

    .line 62
    const/4 v0, 0x0

    iput-object v0, p0, Lit/necst/grabnrun/FileDownloader;->fileMimeType:Ljava/lang/String;

    .line 63
    return-void
.end method

.method static synthetic access$0()Ljava/lang/String;
    .locals 1

    .prologue
    .line 44
    sget-object v0, Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$1(Lit/necst/grabnrun/FileDownloader;Ljava/lang/String;)V
    .locals 0

    .prologue
    .line 49
    iput-object p1, p0, Lit/necst/grabnrun/FileDownloader;->fileMimeType:Ljava/lang/String;

    return-void
.end method


# virtual methods
.method final downloadRemoteUrl(Ljava/net/URL;Ljava/lang/String;Z)Z
    .locals 8
    .param p1, "remoteURL"    # Ljava/net/URL;
    .param p2, "localURI"    # Ljava/lang/String;
    .param p3, "isRedirectAllowed"    # Z

    .prologue
    const/4 v3, 0x0

    .line 85
    iget-object v4, p0, Lit/necst/grabnrun/FileDownloader;->mConnectivityManager:Landroid/net/ConnectivityManager;

    invoke-virtual {v4}, Landroid/net/ConnectivityManager;->getActiveNetworkInfo()Landroid/net/NetworkInfo;

    move-result-object v4

    iput-object v4, p0, Lit/necst/grabnrun/FileDownloader;->activeNetworkInfo:Landroid/net/NetworkInfo;

    .line 86
    iget-object v4, p0, Lit/necst/grabnrun/FileDownloader;->activeNetworkInfo:Landroid/net/NetworkInfo;

    if-eqz v4, :cond_0

    iget-object v4, p0, Lit/necst/grabnrun/FileDownloader;->activeNetworkInfo:Landroid/net/NetworkInfo;

    invoke-virtual {v4}, Landroid/net/NetworkInfo;->isConnected()Z

    move-result v4

    if-nez v4, :cond_2

    .line 88
    :cond_0
    sget-object v4, Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;

    const-string v5, "No connectivity is available. Download failed!"

    invoke-static {v4, v5}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 223
    :cond_1
    :goto_0
    return v3

    .line 93
    :cond_2
    new-instance v0, Lit/necst/grabnrun/FileDownloader$1;

    invoke-direct {v0, p0, p1, p3, p2}, Lit/necst/grabnrun/FileDownloader$1;-><init>(Lit/necst/grabnrun/FileDownloader;Ljava/net/URL;ZLjava/lang/String;)V

    .line 207
    .local v0, "dataThread":Ljava/lang/Thread;
    invoke-virtual {v0}, Ljava/lang/Thread;->start()V

    .line 211
    :try_start_0
    invoke-virtual {v0}, Ljava/lang/Thread;->join()V
    :try_end_0
    .catch Ljava/lang/InterruptedException; {:try_start_0 .. :try_end_0} :catch_0

    .line 217
    new-instance v2, Ljava/io/File;

    invoke-direct {v2, p2}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 220
    .local v2, "fileAtLocalURI":Ljava/io/File;
    invoke-virtual {v2}, Ljava/io/File;->exists()Z

    move-result v4

    if-eqz v4, :cond_1

    invoke-virtual {v2}, Ljava/io/File;->length()J

    move-result-wide v4

    const-wide/16 v6, 0x0

    cmp-long v4, v4, v6

    if-lez v4, :cond_1

    .line 221
    const/4 v3, 0x1

    goto :goto_0

    .line 212
    .end local v2    # "fileAtLocalURI":Ljava/io/File;
    :catch_0
    move-exception v1

    .line 213
    .local v1, "e":Ljava/lang/InterruptedException;
    invoke-virtual {v1}, Ljava/lang/InterruptedException;->printStackTrace()V

    goto :goto_0
.end method

.method getDownloadedFileExtension()Ljava/lang/String;
    .locals 3

    .prologue
    const/4 v0, 0x0

    .line 235
    iget-object v1, p0, Lit/necst/grabnrun/FileDownloader;->fileMimeType:Ljava/lang/String;

    if-nez v1, :cond_1

    .line 254
    :cond_0
    :goto_0
    return-object v0

    .line 238
    :cond_1
    iget-object v1, p0, Lit/necst/grabnrun/FileDownloader;->fileMimeType:Ljava/lang/String;

    invoke-virtual {v1}, Ljava/lang/String;->hashCode()I

    move-result v2

    sparse-switch v2, :sswitch_data_0

    goto :goto_0

    :sswitch_0
    const-string v2, "application/vnd.android.package-archive"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 242
    const-string v0, ".apk"

    goto :goto_0

    .line 238
    :sswitch_1
    const-string v2, "application/octet-stream"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 250
    const-string v0, ".pem"

    goto :goto_0

    .line 238
    :sswitch_2
    const-string v2, "application/java-archive"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 246
    const-string v0, ".jar"

    goto :goto_0

    .line 238
    nop

    :sswitch_data_0
    .sparse-switch
        0x4d6213b -> :sswitch_0
        0x463e3f9d -> :sswitch_1
        0x7a257a76 -> :sswitch_2
    .end sparse-switch
.end method
