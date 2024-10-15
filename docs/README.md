# Dokumentation
Der Spring-Boot Starter ermöglicht den Zugriff auf S3 Bucket Credentials.

Der Starter erwartet seine Konfiguration unter _de.muenchen.mobidam.s3.bucket-credential-config_.

Er wird aktiviert, wenn im Spring-Boot Kontext eine _S3Client.class Bean_ gefunden wird. 

## Source Code holen

Sourcen auf den lokalen Rechner holen 

    git clone https://github.com/it-at-m/mobidam-eai-commons-starter.git

- Das Projekt _mobidam-eai-commons-lib_ enthält die Sourcen.
- Das Projekt _mobidam-eai-commons-starter_ enthält die Starter Klassen.

## Konfiguration
Der Starter ermöglicht den Zugriff auf die S3 Credentials die als Betriebssystem Umgebungvariable definiert sind.

Die S3 Buckets werden als Liste unter ihrem '_Bucket-Namen_' (tenant-default, test-bucket, etc.) und ihrem _access-key-env-var_ + _secret-key-env-var_ konfiguriert.

Der Wert von _access-key-env-var_ / _secret-key-env-var_ pro S3 Bucket z.Bsp. MOBIDAM_ACCESS_KEY / MOBIDAM_SECRET_KEY ist der Name der Betriebssystem Umgebungvariable, die final den _S3-Bucket Access-Key / Secret-Key_ enthält.

```
de.muenchen.mobidam:
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