CC = javac
RUN = java
CP = ".:commons-cli-1.5.0.jar"
.PHONY: all clean runserv runclient

all: EchoServer.class EchoClient.class

EchoServer.class: EchoServer.java
	$(CC) -cp $(CP) $<
EchoClient.class: EchoClient.java
	$(CC) -cp $(CP) $<
runserv: EchoServer.class
	$(RUN) -cp $(CP) EchoServer $(ARGS)
runclient: EchoClient.class
	$(RUN) -cp $(CP) EchoClient localhost $(ARGS) 
clean:
	rm $(wildcard *.class)
