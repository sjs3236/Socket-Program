JFLAGS= -g
JC=javac

.SUFFIXES: $(SUFFIXES) .class .java

.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = Server.java Client.java

default: classes

classes: $(CLASSES:.java=.class)

executeServer:
	@java Server 127.0.0.1 9050

executeClient1:
	@java Client 127.0.0.1 9045 127.0.0.1 9050
executeClient2:
	@java Client 127.0.0.1 9047 127.0.0.1 9050
executeClient3:
	@java Client 127.0.0.1 9048 127.0.0.1 9050

clean:
	rm -f *.class *~ *.bak
