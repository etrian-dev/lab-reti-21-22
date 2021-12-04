I programmi server e client non hanno dipendenze aggiuntive oltre alla libreria standard Java,
per cui possono essere compilati con "javac *.java" ed eseguiti con 
"java TimeServer <IP>" e "java TimeClient <IP> <NETIF>" rispettivamente.
Il TimeServer prende in input l'indirizzo IP di un gruppo multicast
al quale inviare i datagrammi contenenti il timestamp.
Il TimeClient prende in input l'indirizzo IP del gruppo multicast al quale unirsi
(lo stesso del server) ed il nome dell'interfaccia di rete da utilizzare, al fine
di non dover modificare manualmente il codice su macchine diverse