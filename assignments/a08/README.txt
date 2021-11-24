I sorgenti possono essere compilati ed eseguiti importando la libreria Jackson
utilizzando un IDE (testata su VSCode), oppure copiando i jar da cui dipende nella
subdirectory libs ed eseguendo da terminale (dalla directory padre di ContiCorrenti)
i seguenti comandi
javac -cp "ContiCorrenti/lib/*" ContiCorrenti/src/*.java
java -cp ".:ContiCorrenti/lib/*" ContiCorrenti/src/FileProcessor
