# Dokumentation
Der Spring-Boot Starter ermöglicht den Zugriff auf S3 Bucket Credentials per Apache Camel Processor.

Er wird aktiviert, wenn im Spring-Boot Kontext eine Apachae Camel _AWS2S3Component.class Bean_ gefunden wird.

Der Starter erwartet seine Konfiguration unter _de.muenchen.mobidam.common.s3.bucket-credential-config_. Bitte dazu den _Bekannten Fehler_ s.u. beachten.

## S3 Credentials konfigurieren
Die S3 Credentials (Benutzer, Password) müssen als Umgebungsvariable definiert sein, damit sie vom _mobidam-eai-common-starter_ gefunden werden können.

Da für jeden S3 Bucket eigene Credentials gelten, werden diese im _S3CredentialProvider_ über ihren _Bucket-Namen_ ermittelt.

Dazu müssen die S3 Buckets unter ihrem '_Bucket-Namen_' als Map (z.Bsp. tenant-default, test-bucket, etc.) mit ihren Credentials _access-key-env-var_ und _secret-key-env-var_ in den Anwendungseigenschaften unter _de.muenchen.mobidam.common.s3.bucket-credential-config_ konfiguriert sein.

Der _Wert_ von _access-key-env-var_ und  _secret-key-env-var_ pro S3 Bucket (z.Bsp. MOBIDAM_ACCESS_KEY / MOBIDAM_SECRET_KEY) ist der _Name der Umgebungvariable_, die final die S3 Credentials  _Access-Key_ und _Secret-Key_ enthalten.

Kann zu einem S3 Bucket Namen kein Map-Eintrag gefunden werden, wird standarmässig der "tenant-default" probiert.

### Konfiguration (application.yaml)

```
de.muenchen.mobidam:
  common:
    s3:
      bucket-credential-config:
        tenant-default:
          access-key-env-var: MOBIDAM_ACCESS_KEY
          secret-key-env-var: MOBIDAM_SECRET_KEY
        test-bucket:
          access-key-env-var: FOO_ACCESS_KEY
          secret-key-env-var: FOO_SECRET_KEY
        
printenv (bash) / set (cmd) / Get-ChildItem Env: (Powershell)
MOBIDAM_ACCESS_KEY=[mobidamS3AccessKey]
MOBIDAM_SECRET_KEY=[mobidamS3SecretKey]
FOO_ACCESS_KEY=[fooS3AccessKey]
FOO_SECRET_KEY=[fooS3SecretKey]
        
```
### Bekannter Fehler bei der Initialiserung der S3BucketCredentialConfigAutoConfiguration
Der _S3CredentialProvider_ ist von einer _@ConditionalOnClass(AWS2S3Component.class)_ abhängig und erwartet eine _BucketCredentialConfig_ für seine Initialisierung.
Die _BucketCredentialConfig_ wiederum kann nur konfiguriert werden wenn in der appliction.yml ein Eigenschaft _de.muenchen.mobidam.common.s3.bucket-credential-config_ vorhanden ist.
Dabei handelt es sich um eine _Map<String, BucketCredentialConfig>_ die aber durch eine  _@ConditionalOnProperty_ nicht abgefragt werden kann.
Die _@ConditionalOnProperty_ ist darauf ausgelegt einfache _String_ basierte _Key-Value_ Eigenschaften zu überprüfen. Die Annotation stösst bei einer _Map_ an ihre Grenzen.
Daher ist in der _S3BucketCredentialConfigAutoConfiguration_ keine zusätzliche Prüfung _@ConditionalOnProperty_ enthalten.

## Maven Artefakt dem eigenen Projekt hinzufügen

```
<dependency>
   <groupId>de.muenchen.mobidam</groupId>
   <artifactId>mobidam-eai-commons-starter</artifactId>
   <version>1.0.0</version>
</dependency>
```

### S3CrentialProvider Bean einer Camel Route hinzufügen

```
  public class ErrorResponse implements de.muenchen.mobidam.eai.common.exception.CommonError {
    ...
  }

  from("{{camel.route.common}}")
       ...
       .setHeader(CommonConstants.HEADER_BUCKET_NAME, "test-bucket")
       .process("s3CredentialProvider")
       .toD(String.format("aws2-s3://${header.%1$s}?accessKey=RAW(${header.%2$s})&secretKey=RAW(${header.%3$s})..., CommonConstants.HEADER_BUCKET_NAME, CommonConstants.HEADER_ACCESS_KEY, CommonConstants.HEADER_SECRET_KEY, ...))
       ...
```

## Source Code holen

Sourcen auf den lokalen Rechner holen

    git clone https://github.com/it-at-m/mobidam-eai-commons-starter.git

- Das Projekt _mobidam-eai-commons-lib_ enthält die Sourcen.
- Das Projekt _mobidam-eai-commons-starter_ enthält die Starter Klassen.