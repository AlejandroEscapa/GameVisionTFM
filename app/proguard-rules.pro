# Reglas R8 del proyecto (Fase 4).
#
# Criterio: reglas mínimas y dirigidas. Gson 2.11 y Retrofit 2.11 traen sus
# consumer rules empaquetadas (TypeToken, Signature, builders) — no duplicarlas.

# DTOs serializados por Gson vía reflexión: sin esto, R8 ofuscaría campos y
# Gson dejaría de mapear el JSON de las APIs de noticias y juegos.
-keep class es.androidtfm.gamevision.retrofit.** { *; }

# Trazabilidad de crashes de release: conservar líneas de código para
# reobfuscación con mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
