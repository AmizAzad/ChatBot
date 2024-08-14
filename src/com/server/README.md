Use the below commands to pull the chatserver image to your machine and run:

1. Login to docker. Enter your Docker Hub username and password when prompted.
   docker login

2. Pull the chatserver image.
   docker pull amizazad/chatserver

3. Verify if the image is pulled to youir local machine.
   docker images

4. If both, server and client, are to be run in Docker, they need to be in the same Docker network.
   You can create a custom network and run both containers in that network.
   docker network create chat_network

5. Run the chatserver.
   docker run -it --name chatserver --network chat_network -p 9001:9001 amizazad/chatserver
