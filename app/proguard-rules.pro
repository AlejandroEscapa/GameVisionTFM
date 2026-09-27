# Reglas R8 del proyecto.
#
# Criterio: reglas mínimas y dirigidas. Gson ya no está en el classpath
# (kotlinx.serialization genera los serializadores en tiempo de compilación y
# trae sus consumer rules); Retrofit 2.12 también trae las suyas.

# Trazabilidad de crashes de release: conservar líneas de código para
# reobfuscación con mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
