This zip file has a word for summary of the project, java files for executing.

Compile Sever.java and Client.java with command 
"make"
Run the program with commands

For Server: you can run sever with command "make executeServer" or you can manually type  "java Server 127.0.0.1 9050"
The first number is IP address of Server and Second number is Port number of server
The numbers can be vary, but the Port number should be greater than 1024 due to registerd port

For Client: you can run sever with command "make executeClient1" "java Client 127.0.0.1 9049 127.0.0.1 9050"
The following numbers are Client IP address, Client Port number, Server IP Address, Server port number 
The numbers can be vary, but the Port number should be greater than 1024 due to registerd port

MyLogFile.log will have a logs about client's actions.
