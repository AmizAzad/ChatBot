Use the below commands to pull the chatclient image to your machine and run:

1. Login to docker. Enter your Docker Hub username and password when prompted.
   `docker login`

2. Pull the chatserver image.
   `docker pull amizazad/chatclient`

3. Verify if the image is pulled to your local machine.
   `docker images`

4. If both, server and client, are to be run in Docker, they need to be in the same Docker network.
   You can create a custom network and run both containers in that network.
   `docker network create chat_network`

5. Run the chatclient.
   `docker run -it --name chatclient --network chat_network amizazad/chatclient`

6. Once chatclient is up and running, it will prompt for the following information. Provide the details as mentioned below:
   `Enter server host:
    chatserver
   Enter server port: 
     9001
     ** Port on which chatserver is running **
   Enter your user name:
     ** You can use username of your choice as long as it is already not taken **`

Note: In case multiple instances of chatclient are to be run on the same machine, make sure to choose a different name "chatclient1, chatclient2" for each instance.
