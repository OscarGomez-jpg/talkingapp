# talkingapp
A really bad and basic implementation of a chat online

1. Servidor:
 - Debe ser capaz de aceptar conexiones de múltiples clientes utilizando sockets TCP.
 - Mantener una lista de usuarios conectados a la sala de chat.
 - Permitir a los clientes enviar mensajes a la sala de chat, que serán distribuidos a todos los clientes 
conectados.
 - Implementar una estrategia para manejar nombres de usuario únicos y evitar duplicados.
 - Manejar posibles problemas de conexión, como conexiones perdidas o interrupciones en la red.
2. Cliente:
 - Debe permitir al usuario establecer una conexión con el servidor utilizando un socket TCP.
 - Solicitar al usuario que ingrese un nombre de usuario antes de continuar.
 - Enviar el nombre de usuario al servidor y esperar la confirmación.
 - Después de la confirmación, permitir al usuario enviar y recibir mensajes en la sala de chat.
 - Implementar un mecanismo para manejar mensajes entrantes de forma asíncrona mientras el 
cliente sigue siendo capaz de enviar mensajes.
3. Lógica de Comunicación:
 - Implementar una lógica para enviar y recibir mensajes entre el servidor y los clientes utilizando 
sockets TCP
