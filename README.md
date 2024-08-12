# ChatBot
Design and implement a chat bot(ChatServer and ChatClient) in Scala with following functionality/requirements:
1. Use Socket to implement chat bot
2. Both ChatServer and ChatClient/s should be able to run on different machine
3. ChatClient 'A' should be able to broadcast Message to every other connected ChatClient
4. ChatClient 'A' should be able to send private Message to another ChatClient 'B'
5. Every Message will go through ChatServer. ChatServer should have details of every connected ChatClient
6. After connecting to ChatServer, ChatClient should get list of currently active(connected) clients(For sending private Messages)
7. If any ChatClient gets disconnected from server then every other ChatClient should get notified.
8. A ChatClient should be able disconnect from ChatServer(Socket close and resource cleanup should happen properly)
9. In case of ChatClient crash, ChatClient should release all of its resources and shutdown properly. ChatServer should also cleanup resources for that ChatClient(e.g. client socket connection and entry from user list)
10. In case of ChatServer crash, ChatServer and all connected ChatClient should shutdown properly without any resource leak
11. All important events should get logged in a log file e.g user joined, user left, errors etc.
12. Use some proper format(Contract) for Message exchange/s between ChatServer and ChatClient/s
13. There can be any number of ChatClient/s but only one ChatServer. So application should be scalable(Use multithreading and NonBlocking-IO packages)
