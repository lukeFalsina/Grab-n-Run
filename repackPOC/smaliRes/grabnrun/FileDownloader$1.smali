.class Lit/necst/grabnrun/FileDownloader$1;
.super Ljava/lang/Thread;
.source "FileDownloader.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lit/necst/grabnrun/FileDownloader;->downloadRemoteUrl(Ljava/net/URL;Ljava/lang/String;Z)Z
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lit/necst/grabnrun/FileDownloader;

.field private final synthetic val$isRedirectAllowed:Z

.field private final synthetic val$localURI:Ljava/lang/String;

.field private final synthetic val$remoteURL:Ljava/net/URL;


# direct methods
.method constructor <init>(Lit/necst/grabnrun/FileDownloader;Ljava/net/URL;ZLjava/lang/String;)V
    .locals 0

    .prologue
    .line 1
    iput-object p1, p0, Lit/necst/grabnrun/FileDownloader$1;->this$0:Lit/necst/grabnrun/FileDownloader;

    iput-object p2, p0, Lit/necst/grabnrun/FileDownloader$1;->val$remoteURL:Ljava/net/URL;

    iput-boolean p3, p0, Lit/necst/grabnrun/FileDownloader$1;->val$isRedirectAllowed:Z

    iput-object p4, p0, Lit/necst/grabnrun/FileDownloader$1;->val$localURI:Ljava/lang/String;

    .line 93
    invoke-direct {p0}, Ljava/lang/Thread;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 16

    .prologue
    .line 98
    const/4 v12, 0x0

    .line 99
    .local v12, "urlConnection":Ljava/net/HttpURLConnection;
    const/4 v5, 0x0

    .line 100
    .local v5, "inputStream":Ljava/io/InputStream;
    const/4 v7, 0x0

    .line 104
    .local v7, "outputStream":Ljava/io/OutputStream;
    :try_start_0
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/FileDownloader$1;->val$remoteURL:Ljava/net/URL;

    invoke-virtual {v13}, Ljava/net/URL;->getProtocol()Ljava/lang/String;

    move-result-object v13

    const-string v14, "https"

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_6

    .line 106
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/FileDownloader$1;->val$remoteURL:Ljava/net/URL;

    invoke-virtual {v13}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    move-result-object v13

    move-object v0, v13

    check-cast v0, Ljavax/net/ssl/HttpsURLConnection;

    move-object v12, v0

    .line 113
    :goto_0
    const/16 v13, 0x3e8

    invoke-virtual {v12, v13}, Ljava/net/HttpURLConnection;->setConnectTimeout(I)V

    .line 117
    # getter for: Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;
    invoke-static {}, Lit/necst/grabnrun/FileDownloader;->access$0()Ljava/lang/String;

    move-result-object v13

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "A connection was set up: "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, p0

    iget-object v15, v0, Lit/necst/grabnrun/FileDownloader$1;->val$remoteURL:Ljava/net/URL;

    invoke-virtual {v15}, Ljava/net/URL;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 120
    move-object/from16 v0, p0

    iget-boolean v13, v0, Lit/necst/grabnrun/FileDownloader$1;->val$isRedirectAllowed:Z

    if-eqz v13, :cond_2

    .line 122
    const/4 v10, 0x0

    .line 125
    .local v10, "redirect":Z
    invoke-virtual {v12}, Ljava/net/HttpURLConnection;->getResponseCode()I

    move-result v2

    .line 126
    .local v2, "connection_status":I
    const/16 v13, 0xc8

    if-eq v2, v13, :cond_1

    .line 127
    const/16 v13, 0x12e

    if-eq v2, v13, :cond_0

    .line 128
    const/16 v13, 0x12d

    if-eq v2, v13, :cond_0

    .line 129
    const/16 v13, 0x12f

    if-ne v2, v13, :cond_1

    .line 131
    :cond_0
    const/4 v10, 0x1

    .line 134
    :cond_1
    if-eqz v10, :cond_2

    .line 137
    new-instance v11, Ljava/net/URL;

    const-string v13, "Location"

    invoke-virtual {v12, v13}, Ljava/net/HttpURLConnection;->getHeaderField(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v13

    invoke-direct {v11, v13}, Ljava/net/URL;-><init>(Ljava/lang/String;)V

    .line 140
    .local v11, "redirectedURL":Ljava/net/URL;
    const-string v13, "Set-Cookie"

    invoke-virtual {v12, v13}, Ljava/net/HttpURLConnection;->getHeaderField(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v3

    .line 143
    .local v3, "cookies":Ljava/lang/String;
    invoke-virtual {v11}, Ljava/net/URL;->getProtocol()Ljava/lang/String;

    move-result-object v13

    const-string v14, "https"

    invoke-virtual {v13, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-eqz v13, :cond_7

    .line 145
    invoke-virtual {v11}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    move-result-object v13

    move-object v0, v13

    check-cast v0, Ljavax/net/ssl/HttpsURLConnection;

    move-object v12, v0

    .line 152
    :goto_1
    const/16 v13, 0x3e8

    invoke-virtual {v12, v13}, Ljava/net/HttpURLConnection;->setConnectTimeout(I)V

    .line 154
    const-string v13, "Cookie"

    invoke-virtual {v12, v13, v3}, Ljava/net/HttpURLConnection;->setRequestProperty(Ljava/lang/String;Ljava/lang/String;)V

    .line 156
    # getter for: Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;
    invoke-static {}, Lit/necst/grabnrun/FileDownloader;->access$0()Ljava/lang/String;

    move-result-object v13

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "The connection was redirected to: "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v11}, Ljava/net/URL;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 161
    .end local v2    # "connection_status":I
    .end local v3    # "cookies":Ljava/lang/String;
    .end local v10    # "redirect":Z
    .end local v11    # "redirectedURL":Ljava/net/URL;
    :cond_2
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/FileDownloader$1;->this$0:Lit/necst/grabnrun/FileDownloader;

    invoke-virtual {v12}, Ljava/net/HttpURLConnection;->getContentType()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Lit/necst/grabnrun/FileDownloader;->access$1(Lit/necst/grabnrun/FileDownloader;Ljava/lang/String;)V

    .line 165
    new-instance v6, Ljava/io/BufferedInputStream;

    invoke-virtual {v12}, Ljava/net/HttpURLConnection;->getInputStream()Ljava/io/InputStream;

    move-result-object v13

    invoke-direct {v6, v13}, Ljava/io/BufferedInputStream;-><init>(Ljava/io/InputStream;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_7
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 166
    .end local v5    # "inputStream":Ljava/io/InputStream;
    .local v6, "inputStream":Ljava/io/InputStream;
    :try_start_1
    new-instance v8, Ljava/io/FileOutputStream;

    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/FileDownloader$1;->val$localURI:Ljava/lang/String;

    invoke-direct {v8, v13}, Ljava/io/FileOutputStream;-><init>(Ljava/lang/String;)V
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_8
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    .line 168
    .end local v7    # "outputStream":Ljava/io/OutputStream;
    .local v8, "outputStream":Ljava/io/OutputStream;
    const/4 v9, 0x0

    .line 169
    .local v9, "read":I
    const/16 v13, 0x400

    :try_start_2
    new-array v1, v13, [B

    .line 171
    .local v1, "bytes":[B
    :goto_2
    invoke-virtual {v6, v1}, Ljava/io/InputStream;->read([B)I

    move-result v9

    if-gtz v9, :cond_8

    .line 175
    # getter for: Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;
    invoke-static {}, Lit/necst/grabnrun/FileDownloader;->access$0()Ljava/lang/String;

    move-result-object v13

    new-instance v14, Ljava/lang/StringBuilder;

    const-string v15, "Download complete. Container Path: "

    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    move-object/from16 v0, p0

    iget-object v15, v0, Lit/necst/grabnrun/FileDownloader$1;->val$localURI:Ljava/lang/String;

    invoke-virtual {v14, v15}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v14

    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v14

    invoke-static {v13, v14}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_0
    .catchall {:try_start_2 .. :try_end_2} :catchall_2

    .line 183
    # getter for: Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;
    invoke-static {}, Lit/necst/grabnrun/FileDownloader;->access$0()Ljava/lang/String;

    move-result-object v13

    const-string v14, "Clean up all pending streams.."

    invoke-static {v13, v14}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 184
    if-eqz v12, :cond_3

    .line 185
    invoke-virtual {v12}, Ljava/net/HttpURLConnection;->disconnect()V

    .line 187
    :cond_3
    if-eqz v6, :cond_4

    .line 189
    :try_start_3
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V
    :try_end_3
    .catch Ljava/io/IOException; {:try_start_3 .. :try_end_3} :catch_5

    .line 194
    :cond_4
    :goto_3
    if-eqz v8, :cond_e

    .line 197
    :try_start_4
    invoke-virtual {v8}, Ljava/io/OutputStream;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_6

    move-object v7, v8

    .end local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v7    # "outputStream":Ljava/io/OutputStream;
    move-object v5, v6

    .line 204
    .end local v1    # "bytes":[B
    .end local v6    # "inputStream":Ljava/io/InputStream;
    .end local v9    # "read":I
    .restart local v5    # "inputStream":Ljava/io/InputStream;
    :cond_5
    :goto_4
    return-void

    .line 109
    :cond_6
    :try_start_5
    move-object/from16 v0, p0

    iget-object v13, v0, Lit/necst/grabnrun/FileDownloader$1;->val$remoteURL:Ljava/net/URL;

    invoke-virtual {v13}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    move-result-object v13

    move-object v0, v13

    check-cast v0, Ljava/net/HttpURLConnection;

    move-object v12, v0

    goto/16 :goto_0

    .line 148
    .restart local v2    # "connection_status":I
    .restart local v3    # "cookies":Ljava/lang/String;
    .restart local v10    # "redirect":Z
    .restart local v11    # "redirectedURL":Ljava/net/URL;
    :cond_7
    invoke-virtual {v11}, Ljava/net/URL;->openConnection()Ljava/net/URLConnection;

    move-result-object v13

    move-object v0, v13

    check-cast v0, Ljava/net/HttpURLConnection;

    move-object v12, v0
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_7
    .catchall {:try_start_5 .. :try_end_5} :catchall_0

    goto/16 :goto_1

    .line 172
    .end local v2    # "connection_status":I
    .end local v3    # "cookies":Ljava/lang/String;
    .end local v5    # "inputStream":Ljava/io/InputStream;
    .end local v7    # "outputStream":Ljava/io/OutputStream;
    .end local v10    # "redirect":Z
    .end local v11    # "redirectedURL":Ljava/net/URL;
    .restart local v1    # "bytes":[B
    .restart local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v9    # "read":I
    :cond_8
    const/4 v13, 0x0

    :try_start_6
    invoke-virtual {v8, v1, v13, v9}, Ljava/io/OutputStream;->write([BII)V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_0
    .catchall {:try_start_6 .. :try_end_6} :catchall_2

    goto :goto_2

    .line 177
    .end local v1    # "bytes":[B
    :catch_0
    move-exception v13

    move-object v7, v8

    .end local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v7    # "outputStream":Ljava/io/OutputStream;
    move-object v5, v6

    .line 183
    .end local v6    # "inputStream":Ljava/io/InputStream;
    .end local v9    # "read":I
    .restart local v5    # "inputStream":Ljava/io/InputStream;
    :goto_5
    # getter for: Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;
    invoke-static {}, Lit/necst/grabnrun/FileDownloader;->access$0()Ljava/lang/String;

    move-result-object v13

    const-string v14, "Clean up all pending streams.."

    invoke-static {v13, v14}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 184
    if-eqz v12, :cond_9

    .line 185
    invoke-virtual {v12}, Ljava/net/HttpURLConnection;->disconnect()V

    .line 187
    :cond_9
    if-eqz v5, :cond_a

    .line 189
    :try_start_7
    invoke-virtual {v5}, Ljava/io/InputStream;->close()V
    :try_end_7
    .catch Ljava/io/IOException; {:try_start_7 .. :try_end_7} :catch_2

    .line 194
    :cond_a
    :goto_6
    if-eqz v7, :cond_5

    .line 197
    :try_start_8
    invoke-virtual {v7}, Ljava/io/OutputStream;->close()V
    :try_end_8
    .catch Ljava/io/IOException; {:try_start_8 .. :try_end_8} :catch_1

    goto :goto_4

    .line 198
    :catch_1
    move-exception v4

    .line 199
    .local v4, "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_4

    .line 190
    .end local v4    # "e":Ljava/io/IOException;
    :catch_2
    move-exception v4

    .line 191
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_6

    .line 182
    .end local v4    # "e":Ljava/io/IOException;
    :catchall_0
    move-exception v13

    .line 183
    :goto_7
    # getter for: Lit/necst/grabnrun/FileDownloader;->TAG_FILE_DOWNLOADER:Ljava/lang/String;
    invoke-static {}, Lit/necst/grabnrun/FileDownloader;->access$0()Ljava/lang/String;

    move-result-object v14

    const-string v15, "Clean up all pending streams.."

    invoke-static {v14, v15}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 184
    if-eqz v12, :cond_b

    .line 185
    invoke-virtual {v12}, Ljava/net/HttpURLConnection;->disconnect()V

    .line 187
    :cond_b
    if-eqz v5, :cond_c

    .line 189
    :try_start_9
    invoke-virtual {v5}, Ljava/io/InputStream;->close()V
    :try_end_9
    .catch Ljava/io/IOException; {:try_start_9 .. :try_end_9} :catch_3

    .line 194
    :cond_c
    :goto_8
    if-eqz v7, :cond_d

    .line 197
    :try_start_a
    invoke-virtual {v7}, Ljava/io/OutputStream;->close()V
    :try_end_a
    .catch Ljava/io/IOException; {:try_start_a .. :try_end_a} :catch_4

    .line 202
    :cond_d
    :goto_9
    throw v13

    .line 190
    :catch_3
    move-exception v4

    .line 191
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_8

    .line 198
    .end local v4    # "e":Ljava/io/IOException;
    :catch_4
    move-exception v4

    .line 199
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_9

    .line 190
    .end local v4    # "e":Ljava/io/IOException;
    .end local v5    # "inputStream":Ljava/io/InputStream;
    .end local v7    # "outputStream":Ljava/io/OutputStream;
    .restart local v1    # "bytes":[B
    .restart local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v9    # "read":I
    :catch_5
    move-exception v4

    .line 191
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_3

    .line 198
    .end local v4    # "e":Ljava/io/IOException;
    :catch_6
    move-exception v4

    .line 199
    .restart local v4    # "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    .end local v4    # "e":Ljava/io/IOException;
    :cond_e
    move-object v7, v8

    .end local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v7    # "outputStream":Ljava/io/OutputStream;
    move-object v5, v6

    .end local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v5    # "inputStream":Ljava/io/InputStream;
    goto :goto_4

    .line 182
    .end local v1    # "bytes":[B
    .end local v5    # "inputStream":Ljava/io/InputStream;
    .end local v9    # "read":I
    .restart local v6    # "inputStream":Ljava/io/InputStream;
    :catchall_1
    move-exception v13

    move-object v5, v6

    .end local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v5    # "inputStream":Ljava/io/InputStream;
    goto :goto_7

    .end local v5    # "inputStream":Ljava/io/InputStream;
    .end local v7    # "outputStream":Ljava/io/OutputStream;
    .restart local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v9    # "read":I
    :catchall_2
    move-exception v13

    move-object v7, v8

    .end local v8    # "outputStream":Ljava/io/OutputStream;
    .restart local v7    # "outputStream":Ljava/io/OutputStream;
    move-object v5, v6

    .end local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v5    # "inputStream":Ljava/io/InputStream;
    goto :goto_7

    .line 177
    .end local v9    # "read":I
    :catch_7
    move-exception v13

    goto :goto_5

    .end local v5    # "inputStream":Ljava/io/InputStream;
    .restart local v6    # "inputStream":Ljava/io/InputStream;
    :catch_8
    move-exception v13

    move-object v5, v6

    .end local v6    # "inputStream":Ljava/io/InputStream;
    .restart local v5    # "inputStream":Ljava/io/InputStream;
    goto :goto_5
.end method
