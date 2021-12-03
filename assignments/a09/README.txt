I programmi client e server possono essere compilati con il comando 

javac -cp ".:commons-cli-1.5.0.jar" *.java 
(il separatore nel classpath va sostituito con ; in ambiente Windows)

Anche per eseguire i programmi è necessario specificare il classpath.
Può essere specificata una porta su cui il server si mette in ascolto per nuove connessioni
tramite l'opzione -p <porta>, ma se non specificata viene lanciato di default sulla porta 33333.
Il client accetta le opzioni -h <host> e -p <port>, di default localhost e 33333, per individuare il
socket al quale connettersi.
In alternativa è presente un makefile con target per compilare tutti i file e due target per eseguire
rispettivamente client (runclient) e server (runserver).
Per passare argomenti ai target di esecuzione è necessario chiamare make in questo modo:

make <target> ARGS="-p ...."

Il client prende stringhe da standard input interattivamente fino alla ricezione di Ctrl-C o Ctrl-D.
Può essere testato anche tramite il file tests.txt con il comando
make runclient < tests.txt