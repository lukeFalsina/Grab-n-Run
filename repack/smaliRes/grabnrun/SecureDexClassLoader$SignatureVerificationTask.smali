.class Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;
.super Ljava/lang/Object;
.source "SecureDexClassLoader.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lit/necst/grabnrun/SecureDexClassLoader;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = "SignatureVerificationTask"
.end annotation


# instance fields
.field private containerPath:Ljava/lang/String;

.field private rootPackageNameWithCertificate:Ljava/lang/String;

.field private successVerifiedContainerSet:Ljava/util/Set;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field final synthetic this$0:Lit/necst/grabnrun/SecureDexClassLoader;


# direct methods
.method public constructor <init>(Lit/necst/grabnrun/SecureDexClassLoader;Ljava/lang/String;Ljava/lang/String;Ljava/util/Set;)V
    .locals 0
    .param p2, "containerPath"    # Ljava/lang/String;
    .param p3, "rootPackageNameWithCertificate"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 665
    .local p4, "successVerifiedContainerSet":Ljava/util/Set;, "Ljava/util/Set<Ljava/lang/String;>;"
    iput-object p1, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->this$0:Lit/necst/grabnrun/SecureDexClassLoader;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 668
    iput-object p2, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->containerPath:Ljava/lang/String;

    .line 669
    iput-object p3, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->rootPackageNameWithCertificate:Ljava/lang/String;

    .line 670
    iput-object p4, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->successVerifiedContainerSet:Ljava/util/Set;

    .line 671
    return-void
.end method


# virtual methods
.method public run()V
    .locals 5

    .prologue
    .line 677
    const/4 v2, 0x0

    invoke-static {v2}, Landroid/os/Process;->setThreadPriority(I)V

    .line 683
    iget-object v2, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->this$0:Lit/necst/grabnrun/SecureDexClassLoader;

    iget-object v3, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->rootPackageNameWithCertificate:Ljava/lang/String;

    # invokes: Lit/necst/grabnrun/SecureDexClassLoader;->importCertificateFromPackageName(Ljava/lang/String;)Ljava/security/cert/X509Certificate;
    invoke-static {v2, v3}, Lit/necst/grabnrun/SecureDexClassLoader;->access$0(Lit/necst/grabnrun/SecureDexClassLoader;Ljava/lang/String;)Ljava/security/cert/X509Certificate;

    move-result-object v1

    .line 685
    .local v1, "verifiedCertificate":Ljava/security/cert/X509Certificate;
    if-eqz v1, :cond_0

    .line 691
    iget-object v2, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->this$0:Lit/necst/grabnrun/SecureDexClassLoader;

    iget-object v3, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->containerPath:Ljava/lang/String;

    # invokes: Lit/necst/grabnrun/SecureDexClassLoader;->verifyContainerSignatureAgainstCertificate(Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z
    invoke-static {v2, v3, v1}, Lit/necst/grabnrun/SecureDexClassLoader;->access$1(Lit/necst/grabnrun/SecureDexClassLoader;Ljava/lang/String;Ljava/security/cert/X509Certificate;)Z

    move-result v0

    .line 694
    .local v0, "signatureCheckIsSuccessful":Z
    if-eqz v0, :cond_0

    .line 698
    iget-object v3, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->successVerifiedContainerSet:Ljava/util/Set;

    monitor-enter v3

    .line 700
    :try_start_0
    iget-object v2, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->successVerifiedContainerSet:Ljava/util/Set;

    iget-object v4, p0, Lit/necst/grabnrun/SecureDexClassLoader$SignatureVerificationTask;->containerPath:Ljava/lang/String;

    invoke-interface {v2, v4}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 698
    monitor-exit v3

    .line 704
    .end local v0    # "signatureCheckIsSuccessful":Z
    :cond_0
    return-void

    .line 698
    .restart local v0    # "signatureCheckIsSuccessful":Z
    :catchall_0
    move-exception v2

    monitor-exit v3
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v2
.end method
