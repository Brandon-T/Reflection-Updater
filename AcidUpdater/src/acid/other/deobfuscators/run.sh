GAMEPACK_VERSION=186

DEOBFUSCATOR_VERSION=188

mkdir -p ./Deobfuscation
cp deobfuscator.jar ./Deobfuscation
cp mapping-updater.jar ./Deobfuscation
cp ./vanilla-gamepack-$GAMEPACK_VERSION.jar ./Deobfuscation
cp ./runescape-client-$DEOBFUSCATOR_VERSION.jar ./Deobfuscation

cd ./Deobfuscation
java -jar ./deobfuscator.jar ./vanilla-gamepack-$GAMEPACK_VERSION.jar ./deobbed-gamepack-$GAMEPACK_VERSION.jar
java -jar ./mapping-updater.jar ./deobbed-gamepack-$GAMEPACK_VERSION.jar ./runescape-client-$DEOBFUSCATOR_VERSION.jar ./refactored-$GAMEPACK_VERSION.jar

rm ./vanilla-gamepack-$GAMEPACK_VERSION.jar
rm ./runescape-client-$DEOBFUSCATOR_VERSION.jar
rm ./deobbed-gamepack-$GAMEPACK_VERSION.jar
rm ./deobfuscator.jar
rm ./mapping-updater.jar
cd ..