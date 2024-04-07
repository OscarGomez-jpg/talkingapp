# Talking App

## Integrantes

- Juan Manuel Marin Angarita - A00382037
- Óscar Andrés Gómez Lozano - A00394142
- Juan Felipe Jojoa Crespo - A00382042
- Juan Sebastian Gonzalez Sarmiento - A00371810

## Programa

Este repositorio tiene un programa de comunicación diseñado para permitir chats grupales entre diversas personas, incluyendo el envío de notas de voz y llamadas.

## Requerimientos iniciales

### Servidor

El servidor debe:

- Aceptar conexiones de múltiples clientes utilizando sockets TCP.
- Mantener una lista de usuarios conectados a la sala de chat.
- Permitir a los clientes enviar mensajes a la sala de chat, que serán distribuidos a todos los clientes conectados.
- Manejar nombres de usuario únicos y evitar duplicados.
- Manejar posibles problemas de conexión, como conexiones perdidas o interrupciones en la red.

### Cliente

El cliente debe:

- Permitir al usuario establecer una conexión con el servidor utilizando un socket TCP.
- Solicitar al usuario que ingrese un nombre de usuario antes de continuar.
- Enviar el nombre de usuario al servidor y esperar la confirmación.
- Después de la confirmación, permitir al usuario enviar y recibir mensajes en la sala de chat.
- Manejar mensajes entrantes de forma asíncrona mientras el cliente sigue siendo capaz de enviar mensajes.

### Lógica de Comunicación

Debe implementarse una lógica para enviar y recibir mensajes entre el servidor y los clientes utilizando sockets TCP.

## Compilación y Ejecución

### Servidor

- Compilar el servidor:
    ```bash
    javac -cp Server Server/Server.java -d bin
    ```

- Ejecutar el servidor:
    ```bash
    java -cp bin Server
    ```

### Cliente

- Compilar el cliente:
    ```bash
    javac -cp Client Client/Client.java -d bin
    ```

- Ejecutar el cliente:
    ```bash
    java -cp bin Client
    ```

## Comandos de mensajería

### Enviar mensaje:

- Uso: ingresar `<mensaje>`
- Descripción: Este comando envía un mensaje a todos los usuarios conectados.
- Ejemplo: ingresar `Hola a todos`

### Enviar mensaje privado:

- Uso: ingresar `<receptor>: <mensaje>`
- Descripción: Este comando envía un mensaje privado al usuario especificado.
- Ejemplo: ingresar `Juan: Hola Juan`

## Comandos de Notas de Voz

### Enviar nota de voz grupal:

- Iniciar grabación: ingresar `record`
- Parar grabación: ingresar `detain`
- Descripción: Estos comandos inician y detienen la grabación de una nota de voz para enviar a todos los usuarios conectados.

### Enviar nota de voz privada:

- Iniciar grabación: ingresar `record: <receptor>`
- Parar grabación: ingresar `detain`
- Descripción: Estos comandos inician y detienen la grabación de una nota de voz para enviar al usuario especificado.

## Comandos de Llamada

- Iniciar llamada: ingresar `call`
- Detener llamada: ingresar `stop call`
- Descripción: Estos comandos inician y detienen una llamada con todos los usuarios conectados.

## Comandos de Desconexión

- Desconectar cliente: ingresar `disconnect`
- Descripción: Este comando desconecta al cliente del servidor.

