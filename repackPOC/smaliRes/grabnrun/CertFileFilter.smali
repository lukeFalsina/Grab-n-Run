.class final Lit/necst/grabnrun/CertFileFilter;
.super Ljava/lang/Object;
.source "CertFileFilter.java"

# interfaces
.implements Ljava/io/FileFilter;


# instance fields
.field private certificateName:Ljava/lang/String;

.field private final okCertsExtensions:[Ljava/lang/String;


# direct methods
.method constructor <init>(Ljava/lang/String;)V
    .locals 3
    .param p1, "certificateName"    # Ljava/lang/String;

    .prologue
    .line 46
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 32
    const/4 v0, 0x1

    new-array v0, v0, [Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, ".pem"

    aput-object v2, v0, v1

    iput-object v0, p0, Lit/necst/grabnrun/CertFileFilter;->okCertsExtensions:[Ljava/lang/String;

    .line 48
    iput-object p1, p0, Lit/necst/grabnrun/CertFileFilter;->certificateName:Ljava/lang/String;

    .line 49
    return-void
.end method


# virtual methods
.method public final accept(Ljava/io/File;)Z
    .locals 8
    .param p1, "file"    # Ljava/io/File;

    .prologue
    const/4 v1, 0x0

    .line 56
    invoke-virtual {p1}, Ljava/io/File;->isDirectory()Z

    move-result v2

    if-eqz v2, :cond_1

    .line 71
    :cond_0
    :goto_0
    return v1

    .line 58
    :cond_1
    invoke-virtual {p1}, Ljava/io/File;->isFile()Z

    move-result v2

    if-eqz v2, :cond_0

    .line 63
    iget-object v3, p0, Lit/necst/grabnrun/CertFileFilter;->okCertsExtensions:[Ljava/lang/String;

    array-length v4, v3

    move v2, v1

    :goto_1
    if-ge v2, v4, :cond_0

    aget-object v0, v3, v2

    .line 65
    .local v0, "extension":Ljava/lang/String;
    invoke-virtual {p1}, Ljava/io/File;->getName()Ljava/lang/String;

    move-result-object v5

    new-instance v6, Ljava/lang/StringBuilder;

    iget-object v7, p0, Lit/necst/grabnrun/CertFileFilter;->certificateName:Ljava/lang/String;

    invoke-static {v7}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    invoke-direct {v6, v7}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v6, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v5

    if-eqz v5, :cond_2

    .line 66
    const/4 v1, 0x1

    goto :goto_0

    .line 63
    :cond_2
    add-int/lit8 v2, v2, 0x1

    goto :goto_1
.end method
