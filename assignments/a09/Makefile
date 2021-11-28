CP = ".:commons-cli-1.5.0.jar"
.PHONY: all runserv runclient

all: EchoServer.java EchoClient.java
	javac -cp $(CP) EchoServer.java
	javac -cp $(CP) EchoClient.java
runserv: EchoServer.class
	java -cp $(CP) EchoServer $(ARGS)
runclient: EchoClient.class
	java -cp $(CP) EchoClient localhost $(ARGS) 
